package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

public class ClassesTest extends TempDirTest {

    @Test
    public void testCallConstructorWithoutConstructorDeclaration() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        Test test = new Test();
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "");
    }

    @Test
    public void testCallConstructor() {
        // language=Java
        String input = """
                public class Test {
                
                    public Test() {
                        System.out.println("Hi");
                    }
                
                    public static void main(String[] args) {
                        Test test = new Test();
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }

    @Test
    public void testCallMethodOnInstance() {
        // language=Java
        String input = """
                public class Test {
                
                    public Test() {
                    }
                    
                    public void sayHi() {
                        System.out.println("Hi");
                    }
                    
                    public static void main(String[] args) {
                        Test test = new Test();
                        test.sayHi();
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
    }
}
