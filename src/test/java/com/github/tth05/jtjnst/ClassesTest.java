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

    @Test
    public void testAccessField() {
        //TODO: static fields
        // language=Java
        String input = """
                public class Test {
                    
                    //public static int i = 5;
                    public int i;
                
                    public Test() {
                        System.out.println(i);
                    }
                    
                    public void sayHi() {
                        System.out.println("Hi");
                        System.out.println(this.i);
                    }
                    
                    public static void main(String[] args) {
                        Test test = new Test();
                        test.sayHi();
                        System.out.println(test.i);
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "0", "Hi", "0", "0");
    }

    @Test
    public void testAssignField() {
        // language=Java
        String input = """
                public class Test {
                
                    public int i = 1;
                
                    public Test() {
                        i = 5;
                        System.out.println(++i);
                    }
                    
                    public void sayHi() {
                        System.out.println("Hi");
                        System.out.println(++this.i);
                        i++;
                    }
                    
                    public static Test getInstance() {
                        return new Test();
                    }
                    
                    public static void main(String[] args) {
                        Test test = new Test();
                        test.sayHi();
                        System.out.println(++test.i);
                        getInstance().i = 6;
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "6", "Hi", "7", "9", "6");
    }
}
