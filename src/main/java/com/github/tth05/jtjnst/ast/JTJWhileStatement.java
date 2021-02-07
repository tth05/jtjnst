package com.github.tth05.jtjnst.ast;

public class JTJWhileStatement extends JTJChildrenNode {

    public static final String WHILE_START = "{while(";
    //TODO: add random id to lambda variable name
    public static final String WHILE_MIDDLE = "?";
    public static final String WHILE_END = ".stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {}}";

    private final JTJChildrenNode condition = new JTJEmpty(this);
    private final JTJBlock thenBlock = new JTJBlock(this, false);

    public JTJWhileStatement(JTJChildrenNode parent) {
        super(parent);
        super.addChild(thenBlock);
    }

    @Override
    public void addChild(JTJNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(WHILE_START);
        this.condition.appendToStr(builder);
        builder.append(WHILE_MIDDLE);
        this.thenBlock.appendToStr(builder);
        builder.append(WHILE_END);
    }

    public JTJChildrenNode getCondition() {
        return condition;
    }

    public JTJBlock getBody() {
        return thenBlock;
    }
}
