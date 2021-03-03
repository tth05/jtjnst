package com.github.tth05.jtjnst.transpiler.ast.structure;

public abstract class JTJNode {

    private final JTJChildrenNode parent;

    protected JTJNode(JTJChildrenNode parent) {
        this.parent = parent;
    }

    public JTJChildrenNode getParent() {
        return this.parent;
    }

    public abstract void appendToStr(StringBuilder builder);

    public String asString() {
        StringBuilder builder = new StringBuilder();
        appendToStr(builder);
        return builder.toString();
    }
}
