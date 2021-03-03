package com.github.tth05.jtjnst.transpiler.ast.statement;

import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJEmpty;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

public class JTJIfStatement extends JTJChildrenNode {

    public static final String IF_START = "(";
    public static final String IF_MIDDLE_1 = "?";
    public static final String IF_MIDDLE_2 = ":";
    public static final String IF_END = ").forEach(Runnable::run)";

    private final JTJChildrenNode condition = new JTJEmpty(this);
    private final JTJBlock thenBlock = new JTJBlock(this, false);
    private final JTJBlock elseBlock = new JTJBlock(this, false);

    public JTJIfStatement(JTJChildrenNode parent) {
        super(parent);
        super.addChild(thenBlock);
        super.addChild(elseBlock);
    }

    @Override
    public void addChild(JTJNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(IF_START);
        this.condition.appendToStr(builder);
        builder.append(IF_MIDDLE_1);
        this.thenBlock.appendToStr(builder);
        builder.append(IF_MIDDLE_2);
        this.elseBlock.appendToStr(builder);
        builder.append(IF_END);
    }

    public JTJChildrenNode getCondition() {
        return condition;
    }

    public JTJBlock getThenBlock() {
        return thenBlock;
    }

    public JTJBlock getElseBlock() {
        return elseBlock;
    }
}
