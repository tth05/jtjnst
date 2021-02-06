package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public class IfStatementTest {

    @TempDir
    Path tmpDir;

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
}
