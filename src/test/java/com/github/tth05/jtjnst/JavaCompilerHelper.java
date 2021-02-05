package com.github.tth05.jtjnst;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Locale;

public class JavaCompilerHelper {

    public static boolean compile(String code, Path tmpDir) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8);

        Path path = tmpDir.resolve("Main.java");
        try {
            Files.deleteIfExists(path);
            Files.createFile(path);
            Files.writeString(path, code, StandardOpenOption.CREATE);
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(path);
            compiler.getTask(null, fileManager, null, Arrays.asList("--release", "15", "-nowarn"), null, javaFileObjects).call();
            return true;
        } catch (Throwable ignored) {
            ignored.printStackTrace();
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

    public static String concatLines(String...lines) {
        return String.join(System.lineSeparator(), lines) + System.lineSeparator();
    }

    public static class JavaStringFileObject extends SimpleJavaFileObject {

        private final String code;

        /**
         * Constructs a new JavaSourceFromString.
         *
         * @param name the name of the compilation unit represented by this file object
         * @param code the source code for the compilation unit represented by this file object
         */
        JavaStringFileObject(String name, String code) {
            super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
            this.code = code;
        }

        @Override
        public CharSequence getCharContent(boolean ignoreEncodingErrors) {
            return code;
        }
    }
}
