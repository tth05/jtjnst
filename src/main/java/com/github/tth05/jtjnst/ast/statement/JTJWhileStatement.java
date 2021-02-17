package com.github.tth05.jtjnst.ast.statement;

import com.github.tth05.jtjnst.JTJNSTranspiler;
import com.github.tth05.jtjnst.ast.*;
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
            JTJTryCatchStatement tryCatchStatement = generateTryCatchBlockFromIds(continueStatementIds);
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
            JTJTryCatchStatement tryCatchStatement = generateTryCatchBlockFromIds(breakStatementIds);

            tryCatchStatement.getTryBlock().addChild(new JTJString(tryCatchStatement, whileBuilder.toString()));
            tryCatchStatement.appendToStr(builder);
        } else {
            builder.append(whileBuilder);
        }
    }

    private JTJTryCatchStatement generateTryCatchBlockFromIds(List<Integer> ids) {
        int exId = JTJNSTranspiler.uniqueID();

        JTJTryCatchStatement tryCatchStatement = new JTJTryCatchStatement(this);
        tryCatchStatement.addException("Throwable", exId);

        JTJIfStatement catchIfStatement = ids.stream().map((id) -> {
            JTJIfStatement ifStatement = new JTJIfStatement(tryCatchStatement);
            //add the equals check to only detect the exceptions thrown by break and continue
            ifStatement.getCondition().addChild(new JTJString(ifStatement.getCondition(),
                    JTJTryCatchStatement.CATCH_VARIABLE_PREFIX + exId +
                    ".getMessage().equals(\"" + JTJThrow.THROW_ID_PREFIX + id + "\")"));
            return ifStatement;
        }).reduce((if1, if2) -> {
            if1.getElseBlock().addChild(if2);
            return if2;
        }).get();

        catchIfStatement.getElseBlock().addChild(new JTJString(catchIfStatement.getElseBlock(),
                JTJProgram.ACCESS_UNSAFE_INSTANCE + ".throwException(" +
                JTJTryCatchStatement.CATCH_VARIABLE_PREFIX + exId + ")"));

        tryCatchStatement.getCatchBlock().addChild(catchIfStatement);

        return tryCatchStatement;
    }

    public JTJChildrenNode getCondition() {
        return condition;
    }

    public JTJBlock getBody() {
        return bodyBlock;
    }
}
