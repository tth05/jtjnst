package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class ClassesTest extends TempDirTest {

    @Test
    public void testCallConstructorWithoutConstructorDeclaration() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        Test test = new Test();\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "");
    }

    @Test
    public void testCallConstructor() {
        // language=Java
        String input = "public class Test {\n" +
                       "\n" +
                       "    public Test(String str) {\n" +
                       "        System.out.println(str);\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void main(String[] args) {\n" +
                       "        Test test = new Test(\"Hi\");\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }

    @Test
    public void testCallMethodOnInstance() {
        // language=Java
        String input = "public class Test {\n" +
                       "\n" +
                       "    public Test() {\n" +
                       "    }\n" +
                       "\n" +
                       "    public void sayHi() {\n" +
                       "        System.out.println(\"Hi\");\n" +
                       "        sayHi2();\n" +
                       "    }\n" +
                       "\n" +
                       "    public void sayHi2() {\n" +
                       "        System.out.println(\"Hi2\");\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void main(String[] args) {\n" +
                       "        Test test = new Test();\n" +
                       "        test.sayHi();\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi2");
    }

    @Test
    public void testAccessField() {
        //TODO: static fields
        // language=Java
        String input = "                public class Test {\n" +
                       "\n" +
                       "//                    public static int staticI = 5;\n" +
                       "                    int i = 5;\n" +
                       "                    long l = 5045674734636L;\n" +
                       "                    double d = 50.345645673473d;\n" +
                       "                    float f = 373.12345f;\n" +
                       "                    char c = 't';\n" +
                       "                    boolean b = true;\n" +
                       "                    byte bb = -5;\n" +
                       "                    short s = 4574;\n" +
                       "                    String str = \"test\";\n" +
                       "\n" +
                       "                    public Test() {\n" +
                       "                        System.out.println(i);\n" +
                       "                    }\n" +
                       "\n" +
                       "                    public void sayHi() {\n" +
                       "                        System.out.println(\"Hi\");\n" +
                       "                        System.out.println(this.i);\n" +
                       "                    }\n" +
                       "\n" +
                       "                    public static void main(String[] args) {\n" +
                       "                        Test test = new Test();\n" +
                       "                        test.sayHi();\n" +
                       "                        System.out.println(\"\" + test.i + test.l + test.d + test.f + test.c + test.b + test.bb + test.s + test.str);\n" +
                       "                    }\n" +
                       "                }\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5", "Hi", "5", "5504567473463650.345645673473373.12344ttrue-54574test");
    }

    @Test
    public void testAssignField() {
        // language=Java
        String input = "public class Test {\n" +
                       "\n" +
                       "    public int i = 1;\n" +
                       "\n" +
                       "    public Test(int j) {\n" +
                       "        i = 5;\n" +
                       "        this.i = j;\n" +
                       "        System.out.println(++i);\n" +
                       "    }\n" +
                       "\n" +
                       "    public void sayHi() {\n" +
                       "        System.out.println(\"Hi\");\n" +
                       "        System.out.println(++this.i);\n" +
                       "        i++;\n" +
                       "    }\n" +
                       "\n" +
                       "    public static Test getInstance() {\n" +
                       "        return new Test(78);\n" +
                       "    }\n" +
                       "\n" +
                       "    public static void main(String[] args) {\n" +
                       "        Test test = new Test(123);\n" +
                       "        test.sayHi();\n" +
                       "        System.out.println(++test.i);\n" +
                       "        getInstance().i = 6;\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "124", "Hi", "125", "127", "79");
    }
}
