package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class IfStatementTest extends TempDirTest {

    @Test
    public void testIf() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        if(true)
                            System.out.println("Hi");
                            
                        if(true)
                            if(true)
                                System.out.println("Hi2");
                        if(false)
                            System.out.println("Hi3");
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi2");
    }

    @Test
    public void testIfElseElse() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        if(false)
                            System.out.println("Hi");
                        else if(true)
                            System.out.println("Hi2");
                        else
                            System.out.println("Hi3");
                            
                        if(false)
                            System.out.println("Hi4");
                        else if(false)
                            System.out.println("Hi5");
                        else
                            System.out.println("Hi6");
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi2", "Hi6");
    }

    @Test
    public void testTernaryOperator() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println(args.length == 0 ? "Hi" + 1 : 2);
                        System.out.println(args.length != 0 ? "Hi" + 1 : 2);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi1", "2");
    }
}
