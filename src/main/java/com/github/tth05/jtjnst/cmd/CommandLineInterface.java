package com.github.tth05.jtjnst.cmd;

import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;
import com.github.tth05.jtjnst.transpiler.util.Stopwatch;
import org.fusesource.jansi.AnsiConsole;
import picocli.CommandLine;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@CommandLine.Command(name = "JTJNST", version = "JTJNST 1.0",
        mixinStandardHelpOptions = true,
        headerHeading = "@|bold,underline Usage|@:%n",
        synopsisHeading = "%n",
        descriptionHeading = "%n@|bold,underline Description|@:%n%n",
        parameterListHeading = "%n@|bold,underline Parameters|@:%n",
        optionListHeading = "%n@|bold,underline Options|@:%n",
        description = "Java To Java No Semi Transpiler https://github.com/tth05/jtjnst", sortOptions = false)
public class CommandLineInterface implements Runnable {

    @CommandLine.Option(names = {"-v", "--verbose"}, description = "Enable logging")
    boolean verbose;

    @CommandLine.Option(names = {"-c", "--compile"}, description = "Compile the transpiled code")
    boolean compile;

    @CommandLine.Option(names = {"-r", "--run"}, description = "Compile and run the transpiled code")
    boolean run;

    @CommandLine.Option(names = {"-f"}, description = "Format the output file using the JTJNST-Formatter")
    boolean format;

    @CommandLine.Option(names = {"-o", "--output"}, description = "The output file")
    Path outputFile;

    @CommandLine.Parameters(
            paramLabel = "srcDirs",
            description = "One or more directories which contain the source files to transpile",
            arity = "1..*")
    Path[] paths;

    @Override
    public void run() {
        System.setProperty("java.util.logging.SimpleFormatter.format", "[%4$-7s] %5$s %n");

        Logger logger = Logger.getAnonymousLogger();
        if (!verbose)
            logger.setLevel(Level.OFF);

        if (outputFile != null && !outputFile.toString().endsWith("Main.java")) {
            System.err.println("Output file has to be named Main.java");
            return;
        }

        if (outputFile == null) {
            outputFile = Paths.get("Main.java");
        }

        outputFile = outputFile.toAbsolutePath();

        logger.info("Reading files...");
        Stopwatch.start();
        List<String> files = readAllFiles();
        Stopwatch.stopMessage(logger);

        JTJNSTranspiler transpiler = new JTJNSTranspiler(logger, files.toArray(new String[0]));
        try {
            String transpiledCode = transpiler.getTranspiledCode();
            if (format)
                transpiledCode = JTJNSTFormatter.format(transpiledCode);

            Files.writeString(outputFile, transpiledCode, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Unable to write to output file: " + e.getMessage());
        }

        if (run || compile) {
            logger.info("Compiling...");
            Stopwatch.start();
            boolean result = JavaCompilerHelper.compile(outputFile);
            if (!result) {
                System.err.println("Compilation failed!");
                return;
            }

            Stopwatch.stopMessage(logger);
        }

        if (run) {
            logger.info("Executing program...");
            JavaCompilerHelper.run("Main", outputFile.getParent(), System.in, System.out, true);
        }
    }

    private List<String> readAllFiles() {
        return Arrays.stream(paths).filter(Files::isDirectory).flatMap(dir -> {
            try {
                return Files.walk(dir);
            } catch (IOException e) {
                return Stream.of();
            }
        }).filter(file -> file.toString().endsWith(".java")).map(file -> {
            try {
                return Files.readString(file, StandardCharsets.UTF_8);
            } catch (IOException ignored) {
                return null;
            }
        }).collect(Collectors.toList());
    }

    public static void main(String[] args) {
        AnsiConsole.systemInstall();

        CommandLine commandLine = new CommandLine(new CommandLineInterface());
        commandLine.setParameterExceptionHandler((ex, params) -> {
            System.err.println(ex.getMessage());
            commandLine.usage(System.out);
            return -1;
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            JavaCompilerHelper.killAll();
        }));

        int returnCode = commandLine.execute(args);
        System.exit(returnCode);
    }
}
