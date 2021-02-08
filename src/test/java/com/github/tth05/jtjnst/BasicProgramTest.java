package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class BasicProgramTest extends TempDirTest {

    @Test
    public void testRunBasicProgram() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hi");
                        System.out.println(5 + 5);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "10");
    }
}
