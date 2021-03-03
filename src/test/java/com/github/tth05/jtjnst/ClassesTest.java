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

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "");
    }

    @Test
    public void testCallConstructor() {
        // language=Java
        String input = """
                public class Test {
                
                    public Test(String str) {
                        System.out.println(str);
                    }
                
                    public static void main(String[] args) {
                        Test test = new Test("Hi");
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi");
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
                        sayHi2();
                    }
                    
                    public void sayHi2() {
                        System.out.println("Hi2");
                    }
                    
                    public static void main(String[] args) {
                        Test test = new Test();
                        test.sayHi();
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "Hi", "Hi2");
    }

    @Test
    public void testAccessField() {
        //TODO: static fields
        // language=Java
        String input = """
                public class Test {
                    
//                    public static int staticI = 5;
                    int i = 5;
                    long l = 5045674734636L;
                    double d = 50.345645673473d;
                    float f = 373.12345f;
                    char c = 't';
                    boolean b = true;
                    byte bb = -5;
                    short s = 4574;
                    String str = "test";
                
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
                        System.out.println("" + test.i + test.l + test.d + test.f + test.c + test.b + test.bb + test.s + test.str);
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "5", "Hi", "5", "5504567473463650.345645673473373.12344ttrue-54574test");
    }

    @Test
    public void testAssignField() {
        // language=Java
        String input = """
                public class Test {
                
                    public int i = 1;
                
                    public Test(int j) {
                        i = 5;
                        this.i = j;
                        System.out.println(++i);
                    }
                    
                    public void sayHi() {
                        System.out.println("Hi");
                        System.out.println(++this.i);
                        i++;
                    }
                    
                    public static Test getInstance() {
                        return new Test(78);
                    }
                    
                    public static void main(String[] args) {
                        Test test = new Test(123);
                        test.sayHi();
                        System.out.println(++test.i);
                        getInstance().i = 6;
                    }
                }
                """;

        TestJavaCompilerHelper.runAndExpect(input, tmpDir, "124", "Hi", "125", "127", "79");
    }
}
