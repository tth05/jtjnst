package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExceptionsTest extends TempDirTest {

    @Test
    public void testCatchException() {
        // language=Java
        String input = """
                import java.io.IOException;public class Test {
                    public static void main(String[] args) {
                        for(int i = 0; i < 3; i++) {
                            try {
                                try {
                                    if(i == 0)
                                        throw new IOException("Hi");
                                    else if(i == 1)
                                        throw new IllegalStateException();
                                    else
                                        throw new RuntimeException();
                                } catch (IOException | RuntimeException e) {
                                    System.out.println("Success" + e.getMessage());        
                                }    
                            } catch (IllegalStateException e) {
                                System.out.println("Success2");
                            }
                        }
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "SuccessHi", "Success2", "Successnull");
    }

    @Disabled
    @Test
    public void testCallMethodMarkedWithThrows() {
        // language=Java
        String input = """
                import java.io.BufferedReader;import java.io.File;import java.io.IOException;import java.io.InputStreamReader;public class Test {
                    public static void main(String[] args) {
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                            reader.skip(-5);
                        } catch (IOException e) {
                            System.out.println("Success");
                        } 
                    }
                }
                """;

        JavaCompilerHelper.runAndExpect(input, tmpDir, "Success");
    }
}
