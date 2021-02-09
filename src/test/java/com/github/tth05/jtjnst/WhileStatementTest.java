package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@Timeout(10)
public class WhileStatementTest extends TempDirTest {

    @Test
    public void testBasicWhileStatement() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        //is there a way to test this without variables?
                        while (i < 5) {
                            System.out.println("Hi");
                            ++i;
                        }
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }

    @Test
    public void testContinue() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        while (i < 5) {
                            System.out.println("Hi");
                            ++i;
                            if (i < 5)
                                continue;
                            System.out.println("Hi2");
                        }
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi", "Hi2");
    }

    @Test
    public void testContinueWithLabel() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        outer:
                        while(i < 5) {
                            while(true) {
                                i++;
                                continue outer;
                            }
                            i++;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testBreak() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        while (i < 5) {
                            i++;
                            if(i < 5)
                                break;
                            ++i;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "1");
    }

    @Test
    public void testBreakWithLabel() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        outer:
                        while(i < 5) {
                            i++;
                            while(true) {
                                i++;
                                break outer;
                            }
                            i++;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "2");
    }

    @Test
    public void testBreakAndContinue() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        while(i < 5) {
                            i++;
                            if(i < 3)
                                continue;
                            if(i >= 3)
                                break;
                            i++;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "3");
    }
}
