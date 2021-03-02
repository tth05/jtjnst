package com.github.tth05.jtjnst.ast.statement;

import com.github.javaparser.utils.Pair;
import com.github.tth05.jtjnst.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.ast.structure.JTJNode;

import java.util.ArrayList;
import java.util.List;

public class JTJTryCatchStatement extends JTJChildrenNode {

    public static final String CATCH_VARIABLE_PREFIX = "jtjEx";

    public static final String TRY_CATCH_START = "{try {";
    public static final String TRY_CATCH_MIDDLE_1 = "} catch(";
    public static final String TRY_CATCH_MIDDLE_2 = ") {";

    public static final String TRY_CATCH_END = "}}";
    private final JTJBlock tryBlock = new JTJIfRunnableBlock(this);
    private final JTJBlock catchBlock = new JTJIfRunnableBlock(this);

    //TODO: repalce with multiple catch blocks
    private final List<Pair<String, Integer>> exceptionsToCatch = new ArrayList<>();

    public JTJTryCatchStatement(JTJChildrenNode parent) {
        super(parent);
        super.addChild(tryBlock);
        super.addChild(catchBlock);
    }

    public void addException(String type, int id) {
        exceptionsToCatch.add(new Pair<>(type, id));
    }

    @Override
    public void addChild(JTJNode node) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(TRY_CATCH_START);
        this.tryBlock.appendToStr(builder);
        builder.append(TRY_CATCH_MIDDLE_1);
        //TODO: multiple catch blocks
        for (Pair<String, Integer> pair : exceptionsToCatch) {
            //TODO: REMOVE THIS TRASH LOL
            builder.append(pair.a).append(' ').append(CATCH_VARIABLE_PREFIX).append(pair.b);
        }
        builder.append(TRY_CATCH_MIDDLE_2);
        this.catchBlock.appendToStr(builder);
        builder.append(TRY_CATCH_END);
    }

    public JTJBlock getTryBlock() {
        return tryBlock;
    }

    public JTJBlock getCatchBlock() {
        return catchBlock;
    }
}
