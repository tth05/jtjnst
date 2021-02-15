package com.github.tth05.jtjnst.ast;

public class JTJBlock extends JTJChildrenNode {

    public static final String BLOCK_START = "Arrays.<Runnable>asList(";
    public static final String BLOCK_END = ").forEach(Runnable::run)";
    public static final String BLOCK_END_NO_RUN = ")";

    private final boolean run;

    public JTJBlock(JTJChildrenNode parent) {
        this(parent, true);
    }

    public JTJBlock(JTJChildrenNode parent, boolean run) {
        super(parent);
        this.run = run;
    }

    @Override
    public void addChild(JTJNode node) {
        if (node instanceof JTJStatement) {
            super.addChild(node);
            return;
        }


        JTJStatement statement = new JTJStatement(this);
        statement.addChild(node);
        super.addChild(statement);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(BLOCK_START);

        appendChildrenToBuilderWithSeparator(builder, ",");

        if (run)
            builder.append(BLOCK_END);
        else
            builder.append(BLOCK_END_NO_RUN);
    }
}
