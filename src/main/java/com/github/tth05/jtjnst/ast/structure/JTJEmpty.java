package com.github.tth05.jtjnst.ast.structure;

public class JTJEmpty extends JTJChildrenNode {
    public JTJEmpty(JTJChildrenNode parent) {
        super(parent);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        appendChildrenToBuilder(builder);
    }
}
