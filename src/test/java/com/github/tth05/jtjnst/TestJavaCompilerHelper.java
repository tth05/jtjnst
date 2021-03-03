package com.github.tth05.jtjnst;

import com.github.tth05.jtjnst.cmd.JavaCompilerHelper;
import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestJavaCompilerHelper {

    public static void runAndExpect(String input, Path tmpDir, String... lines) {
        runAndExpect(new String[]{input}, tmpDir, lines);
    }

    public static void runAndExpect(String[] inputs, Path tmpDir, String... lines) {
        String code = new JTJNSTranspiler(inputs).getTranspiledCode();
        assertTrue(JavaCompilerHelper.compile("Main", code, tmpDir));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream errStream = JavaCompilerHelper.run("Main", tmpDir, new ByteArrayInputStream(new byte[0]), out, false);
        assertNotNull(errStream);
        assertDoesNotThrow(() -> assertEquals("", new String(errStream.readAllBytes(), StandardCharsets.UTF_8)));

        assertEquals(concatLines(lines), out.toString());
    }

    public static String concatLines(String... lines) {
        return String.join(System.lineSeparator(), lines) + (lines.length == 1 && lines[0].isBlank() ? "" : System.lineSeparator());
    }
}
