package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class WhileStatementTest {

    @TempDir
    static Path tmpDir;

    @Test
    public void testWhileStatement() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        //is there a way to test this without variables?
                        while (i < 5) {
                            System.out.println("Hi");
                            ++i;
                        }
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }
}
