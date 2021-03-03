package com.github.tth05.jtjnst.transpiler.ast.exception;

import com.github.tth05.jtjnst.transpiler.ast.statement.JTJIfRunnableBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class JTJTryCatchStatement extends JTJChildrenNode {

    public static final String TRY_CATCH_START = "{try {";
    public static final String TRY_CATCH_MIDDLE_1 = "}";
    public static final String TRY_CATCH_END = "}";
    private final JTJBlock tryBlock = new JTJIfRunnableBlock(this);
    private final List<JTJCatchBlock> catchBlocks = new ArrayList<>();


    public JTJTryCatchStatement(JTJChildrenNode parent) {
        super(parent);
        super.addChild(tryBlock);
    }

    public JTJCatchBlock addCatchBlock(String... exceptions) {
        JTJCatchBlock catchBlock = new JTJCatchBlock(this, Arrays.asList(exceptions));
        this.catchBlocks.add(catchBlock);
        return catchBlock;
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
        for (JTJCatchBlock catchBlock : this.catchBlocks) {
            catchBlock.appendToStr(builder);
        }
        builder.append(TRY_CATCH_END);
    }

    public JTJBlock getTryBlock() {
        return tryBlock;
    }

    public List<JTJCatchBlock> getCatchBlocks() {
        return Collections.unmodifiableList(catchBlocks);
    }
}
