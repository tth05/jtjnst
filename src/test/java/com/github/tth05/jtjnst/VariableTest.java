package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class VariableTest extends TempDirTest {

    @Test
    public void testDeclaration() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 5;\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testTypes() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 5;\n" +
                       "        long l = 5045674734636L;\n" +
                       "        double d = 50.345645673473d;\n" +
                       "        float f = 373.12345f;\n" +
                       "        char c = 't';\n" +
                       "        boolean b = true;\n" +
                       "        byte bb = -5;\n" +
                       "        short s = 4574;\n" +
                       "        String str = \"test\";\n" +
                       "        System.out.println(\"\" + i + l + d + f + c + b + bb + s + str);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5504567473463650.345645673473373.12344ttrue-54574test");
    }

    @Test
    public void testMultiDeclaration() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 5, j = 6;\n" +
                       "        System.out.println(i);\n" +
                       "        System.out.println(6);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5", "6");
    }

    @Test
    public void testAssignment() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 5;\n" +
                       "        i = 25;\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "25");
    }

    @Test
    public void testUnary() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 5;\n" +
                       "        i--;\n" +
                       "        i--;\n" +
                       "        i++;\n" +
                       "        System.out.println(i);\n" +
                       "        System.out.println(++i);\n" +
                       "        System.out.println(i++);\n" +
                       "        System.out.println(i);\n" +
                       "        System.out.println(--i);\n" +
                       "\n" +
                       "        i = -i;\n" +
                       "        System.out.println(i);\n" +
                       "\n" +
                       "        boolean val = true;\n" +
                       "        System.out.println(!(!(!val)));\n" +
                       "        i = 2;\n" +
                       "        System.out.println(~i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "4", "5", "5", "6", "5", "-5", "false", "-3");
    }

    @Test
    public void testArrays() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int[] ar2 = new int[5];\n" +
                       "        int[] ar = new int[] {5, 6, 7};\n" +
                       "        System.out.println(ar[1]);\n" +
                       "        System.out.println((ar[1] = 25) == 25);\n" +
                       "        System.out.println(ar[1]);\n" +
                       "        System.out.println(ar2[4]);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "6", "true", "25", "0");
    }
}
