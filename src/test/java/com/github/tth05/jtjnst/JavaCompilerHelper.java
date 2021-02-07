package com.github.tth05.jtjnst;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JavaCompilerHelper {

    public static boolean compile(String name, String code, Path tmpDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8);

        Path path = tmpDir.resolve(name + ".java");
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            Files.writeString(path, code, StandardOpenOption.CREATE);
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(path);
            compiler.getTask(null, fileManager, null, Arrays.asList("--release", "15", "-nowarn"), null, javaFileObjects).call();
            return true;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void run(String name, Path tmpDir, InputStream in, OutputStream out) {
        boolean win = System.getProperty("os.name").toLowerCase().contains("win");
        String exePath = System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator +
                         "java" +
                         (win ? ".exe" : "");

        try {
            Process process = new ProcessBuilder(win ? "cmd" : "bash", win ? "/c" : "-c", "\"" + exePath + "\"" + " " + name)
                    .directory(tmpDir.toFile())
                    .start();
            process.getOutputStream().write(in.readAllBytes());
            process.waitFor();
            out.write(process.getInputStream().readAllBytes());
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void runAndExpect(String input, Path tmpDir, String...lines) {
        String code = new JTJNSTranspiler(input).getTranspiledCode();
        //TODO: remove imports
        code = "import java.util.*;import java.util.stream.*;import java.util.function.*;" + code;

        assertTrue(JavaCompilerHelper.compile("Main", code, tmpDir));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JavaCompilerHelper.run("Main", tmpDir, new ByteArrayInputStream(new byte[0]), out);

        assertEquals(JavaCompilerHelper.concatLines(lines), out.toString());
    }

    public static String concatLines(String...lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
