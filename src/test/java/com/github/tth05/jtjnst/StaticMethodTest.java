package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class StaticMethodTest extends TempDirTest {

    @Test
    public void testCall() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        hi();\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void hi() {\n" +
                       "        System.out.println(\"Hi\");\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }

    @Test
    public void testCallExternal() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.out.println(java.util.Arrays.toString(new int[0]));\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "[]");
    }

    @Test
    public void testCallDifferentClass() {
        // language=Java
        String input1 = "public class Test {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        Test1.hi();\n" +
                        "    }\n" +
                        "}\n";
        String input2 = "public class Test1 {\n" +
                        "    public static void hi() {\n" +
                        "        System.out.println(\"Hi\");\n" +
                        "    }\n" +
                        "}\n";

        TestJavaCompilerHelper.runAndExpect(new String[]{input1, input2}, tmpDir, "Hi");
    }

    @Test
    public void testCallWithParameter() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        hi(5, \"foo\");\n" +
                       "        hi(300, \"bar\");\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void hi(int i, String s) {\n" +
                       "        System.out.println(s + i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "foo5", "bar300");
    }

    @Test
    public void testReturn() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.out.println(hi(5, \"foo\"));\n" +
                       "        System.out.println(hi2(300, \"bar\"));\n" +
                       "        returnVoid();\n" +
                       "    }\n" +
                       "\n" +
                       "    public static String hi(int i, String s) {\n" +
                       "        return i + s;\n" +
                       "    }\n" +
                       "\n" +
                       "    public static int hi2(int i, String s) {\n" +
                       "        if(i > 5)\n" +
                       "            return 0;\n" +
                       "\n" +
                       "        System.out.println(\"Don't reach me\");\n" +
                       "        return i + s;\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void returnVoid() {\n" +
                       "        System.out.println(\"Void1\");\n" +
                       "        return;\n" +
                       "        System.out.println(\"Void2\");\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5foo", "0", "Void1");
    }
}
