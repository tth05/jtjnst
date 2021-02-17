package com.github.tth05.jtjnst.ast.structure;

public abstract class JTJNode {

    private final JTJChildrenNode parent;

    protected JTJNode(JTJChildrenNode parent) {
        this.parent = parent;
    }

    public JTJChildrenNode getParent() {
        return this.parent;
    }

    public abstract void appendToStr(StringBuilder builder);
}
