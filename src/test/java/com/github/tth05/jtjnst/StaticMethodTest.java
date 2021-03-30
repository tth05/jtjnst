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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }

    @Test
    public void testCallExternal() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println(java.util.Arrays.toString(new int[0]));
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "[]");
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

        TestJavaCompilerHelper.runAndExpect(new String[]{input1, input2}, tmpDir, "Hi");
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "foo5", "bar300");
    }

    @Test
    public void testReturn() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println(hi(5, "foo"));
                        System.out.println(hi2(300, "bar"));
                        returnVoid();
                    }
                    
                    public static String hi(int i, String s) {
                        return i + s;
                    }
                    
                    public static int hi2(int i, String s) {
                        if(i > 5)
                            return 0;
                    
                        System.out.println("Don't reach me");
                        return i + s;
                    }
                    
                    public static void returnVoid() {
                        System.out.println("Void1");
                        return;
                        System.out.println("Void2");
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5foo", "0", "Void1");
    }
}
