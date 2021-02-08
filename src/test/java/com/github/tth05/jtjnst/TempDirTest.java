package com.github.tth05.jtjnst;

import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

public abstract class TempDirTest {

    @TempDir
    static Path tmpDir;
}
