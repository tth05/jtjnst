package com.github.tth05.jtjnst;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestMain {

    @Test
    public void testRunBasicProgram() {
        // language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        System.out.println("Hi");
                    }
                }
                """;

        //TODO: convert

        // language=Java
        String expectedOutput = """
                import java.util.function.BiFunction;
                public class Test {
                    public static void main(String[] args) {
                        if(((Function<HashMap<Integer, Object>, Boolean>) ((map) ->
                            Steam.of(((Runnable) (() ->
                                System.out.println("Hi")
                            )).peek(Runnable::run).findFirst().get() != null)
                        ).apply(new HashMap<>()))) {}
                    }
                }
                """;

        assertEquals(expectedOutput, input /* converted result */);
    }
}
