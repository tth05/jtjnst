package com.github.tth05.jtjnst.ast;

import java.util.List;

public class JTJBlock extends JTJChildrenNode {

    public static final String BLOCK_START = "Arrays.<Runnable>asList(";
    public static final String BLOCK_END = ").forEach(Runnable::run)";

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(BLOCK_START);

        List<JTJNode> children = this.getChildren();
        for (int i = 0; i < children.size(); i++) {
            JTJNode child = children.get(i);
            child.appendToStr(builder);

            if (i != children.size() - 1)
                builder.append(",");
        }

        builder.append(BLOCK_END);
    }
}
