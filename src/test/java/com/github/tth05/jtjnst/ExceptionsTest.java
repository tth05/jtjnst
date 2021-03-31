package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class ExceptionsTest extends TempDirTest {

    @Test
    public void testCatchException() {
        // language=Java
        String input = "import java.io.IOException;public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        for(int i = 0; i < 3; i++) {\n" +
                       "            try {\n" +
                       "                try {\n" +
                       "                    if(i == 0)\n" +
                       "                        throw new IOException(\"Hi\");\n" +
                       "                    else if(i == 1)\n" +
                       "                        throw new IllegalStateException();\n" +
                       "                    else\n" +
                       "                        throw new RuntimeException();\n" +
                       "                } catch (IOException | RuntimeException e) {\n" +
                       "                    System.out.println(\"Success\" + e.getMessage());\n" +
                       "                }\n" +
                       "            } catch (IllegalStateException e) {\n" +
                       "                System.out.println(\"Success2\");\n" +
                       "            }\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "SuccessHi", "Success2", "Successnull");
    }

    @Test
    public void testCallMethodMarkedWithThrows() {
        // language=Java
        String input = "import java.io.BufferedReader;import java.io.File;import java.io.IOException;import java.io.InputStreamReader;public class Test {\n" +
                       "    public static void main(String[] args) {\n" +
                       "        try {\n" +
                       "            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));\n" +
                       "            reader.skip(-5);\n" +
                       "        } catch (IllegalArgumentException e) {\n" +
                       "            System.out.println(\"Success\");\n" +
                       "        }\n" +
                       "    }\n" +
                       "}\n";

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Success");
    }
}
