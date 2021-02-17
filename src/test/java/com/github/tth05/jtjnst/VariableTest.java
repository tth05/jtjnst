package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class VariableTest extends TempDirTest {

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
                        System.out.println(++i);
                        System.out.println(i++);
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "4", "5", "5", "6");
    }

    @Test
    public void testArrays() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int[] ar2 = new int[5];
                        int[] ar = new int[] {5, 6, 7};
                        System.out.println(ar[1]);
                        System.out.println((ar[1] = 25) == 25);
                        System.out.println(ar[1]);
                        System.out.println(ar2[4]);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "6", "true", "25", "0");
    }
}
