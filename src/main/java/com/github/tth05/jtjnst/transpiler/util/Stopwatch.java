package com.github.tth05.jtjnst.transpiler.util;

import java.util.logging.Logger;

public class Stopwatch {

    private static long time;

    public static void start() {
        time = System.nanoTime();
    }

    public static void stopMessage(Logger logger) {
        logger.info("Done: " + stop() + "ms");
    }

    public static long stop() {
        return (System.nanoTime() - time) / 1_000_000;
    }
}
