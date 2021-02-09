package com.github.tth05.jtjnst.ast;

public class JTJIfRunnableBlock extends JTJBlock {

    public static final String RUNNABLE_IF_START = "{if(true ? ";
    public static final String RUNNABLE_IF_END = ".stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {}}";

    public JTJIfRunnableBlock(JTJChildrenNode parent) {
        super(parent, false);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(RUNNABLE_IF_START);
        super.appendToStr(builder);
        builder.append(RUNNABLE_IF_END);
    }
}
