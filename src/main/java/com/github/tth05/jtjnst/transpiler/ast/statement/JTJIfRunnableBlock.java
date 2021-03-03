package com.github.tth05.jtjnst.transpiler.ast.statement;

import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

public class JTJIfRunnableBlock extends JTJBlock {

    public static final String RUNNABLE_IF_START = "{if(true ? ";
    public static final String RUNNABLE_IF_END = ".stream().peek(Runnable::run).allMatch(java.util.Objects::nonNull) : false) {}}";

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
