package com.github.tth05.jtjnst.ast;

public class JTJStatement extends JTJChildrenNode {

    public static final String STMT_START = "() -> ";

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(STMT_START);
        appendChildrenToBuilder(builder);
    }
}
