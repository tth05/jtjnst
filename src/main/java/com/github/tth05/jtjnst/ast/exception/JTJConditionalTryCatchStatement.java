package com.github.tth05.jtjnst.ast.exception;

import com.github.tth05.jtjnst.ast.JTJString;
import com.github.tth05.jtjnst.ast.statement.JTJIfStatement;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JTJConditionalTryCatchStatement extends JTJTryCatchStatement {

    public JTJConditionalTryCatchStatement(JTJChildrenNode parent, Collection<Integer> ids) {
        super(parent);

        JTJCatchBlock catchBlock = super.addCatchBlock("Throwable");

        String exId = catchBlock.getVariableName();

        List<JTJIfStatement> statementList = ids.stream().map((id) -> {
            JTJIfStatement ifStatement = new JTJIfStatement(this);
            //add the equals check to only detect the exceptions thrown by break and continue
            ifStatement.getCondition().addChild(new JTJString(ifStatement.getCondition(),
                    exId + ".getMessage().equals(\"" + JTJThrow.THROW_ID_PREFIX + id + "\")"));
            return ifStatement;
        }).collect(Collectors.toList());

        //combine all if statements into one
        for (int i = 1; i < statementList.size(); i++) {
            statementList.get(i - 1).getElseBlock().addChild(statementList.get(i));
        }

        JTJIfStatement catchIfStatement = statementList.get(0);
        catchIfStatement.getElseBlock().addChild(new JTJThrow(catchIfStatement.getElseBlock(), exId));

        catchBlock.addChild(catchIfStatement);
    }

    @Override
    public JTJCatchBlock addCatchBlock(String... exceptions) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        super.appendToStr(builder);
    }
}
