package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.JTJNSTranspiler;
import com.github.tth05.jtjnst.VariableStack;

public class JTJVariableAssign extends JTJChildrenNode {

    private static final String ASSIGN_MIDDLE = ".compute(";
    private static final String ASSIGN_MIDDLE_2 = ", (k%d, v%d) -> ";
    private static final String ASSIGN_MIDDLE_END = ")";

    private final VariableStack.Variable variable;

    public JTJVariableAssign(JTJChildrenNode parent, VariableStack.Variable variable) {
        super(parent);
        this.variable = variable;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(variable.getScope().getScopeType().getMapName());
        builder.append(ASSIGN_MIDDLE);
        builder.append(variable.getNewName());
        builder.append(ASSIGN_MIDDLE_2.formatted(JTJNSTranspiler.uniqueID(), JTJNSTranspiler.uniqueID()));
        appendChildrenToBuilder(builder);
        builder.append(ASSIGN_MIDDLE_END);
    }
}
