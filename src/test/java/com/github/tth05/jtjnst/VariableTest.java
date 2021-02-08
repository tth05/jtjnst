package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class VariableTest {

    @TempDir
    static Path tmpDir;

    @Test
    public void testDeclaration() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 5;
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testAssignment() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 5;
                        i = 25;
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "25");
    }

    @Test
    public void testUnary() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 5;
                        i--;
                        i--;
                        i++;
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "4");
    }
}
