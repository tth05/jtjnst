package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class IfStatementTest extends TempDirTest {

    @Test
    public void testIf() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        if(true)\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "\n" +
                       "        if(true)\n" +
                       "            if(true)\n" +
                       "                System.out.println(\"Hi2\");\n" +
                       "        if(false)\n" +
                       "            System.out.println(\"Hi3\");\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi2");
    }

    @Test
    public void testIfElseElse() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        if(false)\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "        else if(true)\n" +
                       "            System.out.println(\"Hi2\");\n" +
                       "        else\n" +
                       "            System.out.println(\"Hi3\");\n" +
                       "\n" +
                       "        if(false)\n" +
                       "            System.out.println(\"Hi4\");\n" +
                       "        else if(false)\n" +
                       "            System.out.println(\"Hi5\");\n" +
                       "        else\n" +
                       "            System.out.println(\"Hi6\");\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi2", "Hi6");
    }

    @Test
    public void testTernaryOperator() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        System.out.println(args.length == 0 ? \"Hi\" + 1 : 2);\n" +
                       "        System.out.println(args.length != 0 ? \"Hi\" + 1 : 2);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi1", "2");
    }
}
