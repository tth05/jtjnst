package com.github.tth05.jtjnst.ast.structure;

public class JTJStatement extends JTJChildrenNode {

    public static final String STMT_START = "() -> ";

    public JTJStatement(JTJChildrenNode parent) {
        super(parent);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(STMT_START);
        appendChildrenToBuilder(builder);
    }
}
