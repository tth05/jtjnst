package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
public class WhileStatementTest extends TempDirTest {

    @Test
    public void testBasicWhileStatement() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        //is there a way to test this without variables?\n" +
                       "        while (i < 5) {\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "            ++i;\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }

    @Test
    public void testWhileContinue() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        while (i < 5) {\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "            ++i;\n" +
                       "            if (i < 5)\n" +
                       "                continue;\n" +
                       "            System.out.println(\"Hi2\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi", "Hi2");
    }

    @Test
    public void testWhileContinueWithLabel() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        outer:\n" +
                       "        while(i < 5) {\n" +
                       "            while(true) {\n" +
                       "                i++;\n" +
                       "                continue outer;\n" +
                       "            }\n" +
                       "            i++;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testWhileBreak() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        while (i < 5) {\n" +
                       "            i++;\n" +
                       "            if(i < 5)\n" +
                       "                break;\n" +
                       "            ++i;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "1");
    }

    @Test
    public void testWhileBreakWithLabel() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        outer:\n" +
                       "        while(i < 5) {\n" +
                       "            i++;\n" +
                       "            while(true) {\n" +
                       "                i++;\n" +
                       "                break outer;\n" +
                       "            }\n" +
                       "            i++;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "2");
    }

    @Test
    public void testWhileBreakAndContinue() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        while(i < 5) {\n" +
                       "            i++;\n" +
                       "            if(i < 3)\n" +
                       "                continue;\n" +
                       "            if(i >= 3)\n" +
                       "                break;\n" +
                       "            i++;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "3");
    }

    @Test
    public void testBasicForStatement() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        for(int i = 0; i < 5; i++) {\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }

    @Test
    public void testForContinue() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int j = 0;\n" +
                       "        for (int i = 0; i < 5; i++, j++) {\n" +
                       "            System.out.println(\"Hi\");\n" +
                       "            i++;\n" +
                       "            if (i < 5)\n" +
                       "                continue;\n" +
                       "            System.out.println(\"Hi2\");\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(j);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi", "Hi2", "1");
    }

    @Test
    public void testForContinueWithLabel() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        outer:\n" +
                       "        for(; i < 5; i++) {\n" +
                       "            for(;;) {\n" +
                       "                i++;\n" +
                       "                continue outer;\n" +
                       "            }\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testForBreak() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        for (;i < 5; ++i) {\n" +
                       "            i++;\n" +
                       "            if (i < 5)\n" +
                       "                break;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "1");
    }

    @Test
    public void testForBreakWithLabel() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        outer:\n" +
                       "        for(; i < 5; i++) {\n" +
                       "            i++;\n" +
                       "            for(;;) {\n" +
                       "                i++;\n" +
                       "                break outer;\n" +
                       "            }\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "2");
    }

    @Test
    public void testForBreakAndContinue() {
        // language=Java
        String input = "public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        int i = 0;\n" +
                       "        for(;i < 5; i++) {\n" +
                       "            i++;\n" +
                       "            if(i < 3)\n" +
                       "                continue;\n" +
                       "            break;\n" +
                       "        }\n" +
                       "\n" +
                       "        System.out.println(i);\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "3");
    }
}
