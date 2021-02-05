package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMain {

    @TempDir
    Path tmpDir;

    @Test
    public void testRunBasicProgram() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hi");
                        System.out.println(5+5);
                    }
                }
                """;

        String code = new JTJNSTranspiler(input).getTranspiledCode();
        //TODO: remove imports
        code = "import java.util.*;import java.util.stream.*;import java.util.function.*;" + code;

        JavaCompilerHelper.compile(code, tmpDir);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        JavaCompilerHelper.run("Main", tmpDir, new ByteArrayInputStream(new byte[0]), out);

        assertEquals("Hi" + System.lineSeparator() + "10" + System.lineSeparator(),out.toString());
    }
}
