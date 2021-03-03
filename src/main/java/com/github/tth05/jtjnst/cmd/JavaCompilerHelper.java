package com.github.tth05.jtjnst.cmd;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class JavaCompilerHelper {

    private static final Set<Process> PROCESSES = new HashSet<>();
    private static final byte[] BUFFER = new byte[8192];

    public static boolean compile(String name, String code, Path tmpDir) {
        Path path = tmpDir.resolve(name + ".java");
        try {
            Files.writeString(path, code);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return compile(path);
    }

    public static boolean compile(Path file) {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, Locale.ENGLISH, StandardCharsets.UTF_8);

        try {
            Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);

            ByteArrayOutputStream errStream = new ByteArrayOutputStream();
            PrintWriter errWriter = new PrintWriter(errStream);
            boolean result = compiler.getTask(errWriter, fileManager, null,
                    List.of("--release", "15", "-nowarn"), null, javaFileObjects).call();

            if (!result)
                System.err.write(errStream.toByteArray());

            return result;
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }

    public static InputStream run(String name, Path tmpDir, InputStream in, OutputStream out, boolean redirectErr) {
        boolean win = System.getProperty("os.name").toLowerCase().contains("win");
        String exePath = System.getProperties().getProperty("java.home") + File.separator + "bin" + File.separator +
                         "java" +
                         (win ? ".exe" : "");

        try {
            Process process = new ProcessBuilder(win ? "cmd" : "bash", win ? "/c" : "-c",
                    "\"" + exePath + "\"" + " " + name)
                    .redirectErrorStream(redirectErr)
                    .directory(tmpDir.toFile())
                    .start();
            PROCESSES.add(process);

            InputStream processIn = process.getInputStream();
            OutputStream processOut = process.getOutputStream();

            while (process.isAlive()) {
                handleIO(processIn, processOut, in, out);

                try {
                    Thread.sleep(20);
                } catch (InterruptedException ignored) {
                }
            }

            handleIO(processIn, processOut, in, out);

            PROCESSES.remove(process);
            return process.getErrorStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void killAll() {
        for (Process process : PROCESSES) {
            process.descendants().forEach(ProcessHandle::destroyForcibly);
            process.destroyForcibly();
        }
    }

    private static void handleIO(InputStream processIn, OutputStream processOut, InputStream in, OutputStream out) {
        try {
            if (in.available() > 0) {
                int read = in.read(BUFFER, 0, Math.min(BUFFER.length, in.available()));
                processOut.write(BUFFER, 0, read);
                processOut.flush();
            }

            if (processIn.available() > 0) {
                int read = processIn.read(BUFFER, 0, Math.min(BUFFER.length, processIn.available()));
                out.write(BUFFER, 0, read);
            }
        } catch (IOException e) {
        }
    }
}
