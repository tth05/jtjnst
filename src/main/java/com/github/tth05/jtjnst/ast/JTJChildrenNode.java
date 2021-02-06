package com.github.tth05.jtjnst.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class JTJChildrenNode extends JTJNode {

    private final List<JTJNode> children = new ArrayList<>();

    public JTJChildrenNode(JTJChildrenNode parent) {
        super(parent);
    }

    public void addChild(JTJNode node) {
        this.children.add(node);
    }

    protected void appendChildrenToBuilder(StringBuilder builder) {
        for (JTJNode child : this.children) {
            child.appendToStr(builder);
        }
    }

    public List<JTJNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
