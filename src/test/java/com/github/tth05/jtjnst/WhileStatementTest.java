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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }

    @Test
    public void testWhileContinue() {
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi", "Hi2");
    }

    @Test
    public void testWhileContinueWithLabel() {
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testWhileBreak() {
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "1");
    }

    @Test
    public void testWhileBreakWithLabel() {
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "2");
    }

    @Test
    public void testWhileBreakAndContinue() {
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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "3");
    }

    @Test
    public void testBasicForStatement() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        for(int i = 0; i < 5; i++) {
                            System.out.println("Hi");
                        }
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi");
    }

    @Test
    public void testForContinue() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int j = 0;
                        for (int i = 0; i < 5; i++, j++) {
                            System.out.println("Hi");
                            i++;
                            if (i < 5)
                                continue;
                            System.out.println("Hi2");
                        }
                        
                        System.out.println(j);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi", "Hi", "Hi", "Hi", "Hi2", "1");
    }

    @Test
    public void testForContinueWithLabel() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        outer:
                        for(; i < 5; i++) {
                            for(;;) {
                                i++;
                                continue outer;
                            }
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5");
    }

    @Test
    public void testForBreak() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        for (;i < 5; ++i) {
                            i++;
                            if (i < 5)
                                break;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "1");
    }

    @Test
    public void testForBreakWithLabel() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        outer:
                        for(; i < 5; i++) {
                            i++;
                            for(;;) {
                                i++;
                                break outer;
                            }
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "2");
    }

    @Test
    public void testForBreakAndContinue() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 0;
                        for(;i < 5; i++) {
                            i++;
                            if(i < 3)
                                continue;
                            break;
                        }
                        
                        System.out.println(i);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "3");
    }
}
