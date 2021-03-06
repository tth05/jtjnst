package com.github.tth05.jtjnst.transpiler.ast.structure;

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

    public void addChildToFront(JTJNode node) {
        this.children.add(0, node);
    }

    protected void appendChildrenToBuilder(StringBuilder builder) {
        for (JTJNode child : this.children) {
            child.appendToStr(builder);
        }
    }

    protected void appendChildrenToBuilderWithSeparator(StringBuilder builder, String separator) {
        for (int i = 0; i < this.children.size(); i++) {
            JTJNode child = this.children.get(i);
            child.appendToStr(builder);

            if (i != this.children.size() - 1)
                builder.append(separator);
        }
    }

    public void clearChildren() {
        this.children.clear();
    }

    public List<JTJNode> getChildren() {
        return Collections.unmodifiableList(children);
    }
}
