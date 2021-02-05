package com.github.tth05.jtjnst.ast;

public class JTJString extends JTJNode {

    private final String value;

    public JTJString(String value) {
        this.value = value;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(value);
    }
}
