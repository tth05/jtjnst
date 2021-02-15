package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class StaticMethodTest extends TempDirTest {

    @Test
    public void testCall() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        hi();
                    }
                    
                    public static void hi() {
                        System.out.println("Hi");
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }

    @Test
    public void testCallDifferentClass() {
        // language=Java
        String input1 = """
                public class Test {
                    public static void main(String[] args) {
                        Test1.hi();
                    }
                }
                """;
        String input2 = """
                public class Test1 {
                    public static void hi() {
                        System.out.println("Hi");
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(new String[]{input1, input2}, tmpDir, "Hi");
    }

    @Test
    public void testCallWithParameter() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        hi(5, "foo");
                        hi(300, "bar");
                    }
                    
                    public static void hi(int i, String s) {
                        System.out.println(s + i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "foo5", "bar300");
    }
}
