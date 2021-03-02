package com.github.tth05.jtjnst.ast.statement;

import com.github.tth05.jtjnst.ast.JTJString;
import com.github.tth05.jtjnst.ast.exception.JTJConditionalTryCatchStatement;
import com.github.tth05.jtjnst.ast.exception.JTJTryCatchStatement;
import com.github.tth05.jtjnst.ast.structure.*;

import java.util.ArrayList;
import java.util.List;

public class JTJWhileStatement extends JTJLabelNode {

    public static final String WHILE_START = "{while(";
    //TODO: add random id to lambda variable name
    public static final String WHILE_MIDDLE = "?";
    public static final String WHILE_END = ".stream().peek(Runnable::run).allMatch(Objects::nonNull) : false) {}}";

    private final JTJChildrenNode condition = new JTJEmpty(this);
    private final JTJBlock bodyBlock = new JTJBlock(this, true);

    private final List<Integer> breakStatementIds = new ArrayList<>();
    private final List<Integer> continueStatementIds = new ArrayList<>();

    public JTJWhileStatement(JTJChildrenNode parent, String label) {
        super(parent, label);
        super.addChild(bodyBlock);
    }

    public void addBreakStatement(int id) {
        this.breakStatementIds.add(id);
    }

    public void addContinueStatement(int id) {
        this.continueStatementIds.add(id);
    }

    @Override
    public void addChild(JTJNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        JTJBlock inner = new JTJBlock(this, false);

        if (!continueStatementIds.isEmpty()) {
            JTJTryCatchStatement tryCatchStatement = new JTJConditionalTryCatchStatement(null, continueStatementIds);
            for (JTJNode child : this.bodyBlock.getChildren()) {
                tryCatchStatement.getTryBlock().addChild(child);
            }

            inner.addChild(tryCatchStatement);
        } else {
            for (JTJNode child : this.bodyBlock.getChildren()) {
                inner.addChild(child);
            }
        }

        this.clearChildren();
        super.addChild(inner);

        StringBuilder whileBuilder = new StringBuilder();
        whileBuilder.append(WHILE_START);
        this.condition.appendToStr(whileBuilder);
        whileBuilder.append(WHILE_MIDDLE);
        appendChildrenToBuilder(whileBuilder);
        whileBuilder.append(WHILE_END);

        if (!breakStatementIds.isEmpty()) {
            JTJTryCatchStatement tryCatchStatement = new JTJConditionalTryCatchStatement(null, breakStatementIds);

            tryCatchStatement.getTryBlock().addChild(new JTJString(tryCatchStatement, whileBuilder.toString()));
            tryCatchStatement.appendToStr(builder);
        } else {
            builder.append(whileBuilder);
        }
    }

    public JTJChildrenNode getCondition() {
        return condition;
    }

    public JTJBlock getBody() {
        return bodyBlock;
    }
}
