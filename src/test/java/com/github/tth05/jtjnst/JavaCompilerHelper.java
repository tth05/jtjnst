package com.github.tth05.jtjnst;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

public class JavaCompilerHelper {

    public static boolean compile(String name, String code, Path tmpDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8);

        Path path = tmpDir.resolve(name + ".java");
        try {
            Files.writeString(path, code);
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(path);

            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            PrintWriter errWriter = new PrintWriter(errStream);
            boolean result = compiler.getTask(errWriter, fileManager, null,
                    Arrays.asList("--release", "15", "-nowarn"), null, javaFileObjects).call();

            if (!result)
                System.err.write(errStream.toByteArray());

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static InputStream run(String name, Path tmpDir, InputStream in, OutputStream out) {
        boolean win = System.getProperty("os.name").toLowerCase().contains("win");
        String exePath = System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator +
                         "java" +
                         (win ? ".exe" : "");

        try {
            Process process = new ProcessBuilder(win ? "cmd" : "bash", win ? "/c" : "-c",
                    "\"" + exePath + "\"" + " " + name)
                    .directory(tmpDir.toFile())
                    .start();
            process.getOutputStream().write(in.readAllBytes());
            process.waitFor();
            out.write(process.getInputStream().readAllBytes());
            return process.getErrorStream();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void runAndExpect(String input, Path tmpDir, String... lines) {
        runAndExpect(new String[]{input}, tmpDir, lines);
    }

    public static void runAndExpect(String[] inputs, Path tmpDir, String... lines) {
        String code = new JTJNSTranspiler(inputs).getTranspiledCode();
        //TODO: remove imports
        code = "import java.util.*;import java.util.stream.*;import java.util.function.*;" + code;

        assertTrue(JavaCompilerHelper.compile("Main", code, tmpDir));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream errStream = JavaCompilerHelper.run("Main", tmpDir, new ByteArrayInputStream(new byte[0]), out);
        assertNotNull(errStream);
        assertDoesNotThrow(() -> assertEquals("", new String(errStream.readAllBytes(), StandardCharsets.UTF_8)));

        assertEquals(JavaCompilerHelper.concatLines(lines), out.toString());
    }

    public static String concatLines(String... lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }
}
