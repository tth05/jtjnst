package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.VariableStack;

public class JTJVariableAccess extends JTJNode {

    public static final String VARIABLE_ACCESS_START = "((";
    public static final String VARIABLE_ACCESS_MIDDLE_1 = ")";
    public static final String VARIABLE_ACCESS_MIDDLE_2 = ".get(";
    public static final String VARIABLE_ACCESS_END = "))";

    private final VariableStack.Variable variable;

    public JTJVariableAccess(JTJChildrenNode parent, VariableStack.Variable variable) {
        super(parent);
        this.variable = variable;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(VARIABLE_ACCESS_START);
        builder.append(variable.getType());
        builder.append(VARIABLE_ACCESS_MIDDLE_1);
        builder.append(variable.getScope().getScopeType().getMapName());
        builder.append(VARIABLE_ACCESS_MIDDLE_2);
        builder.append(variable.getNewName());
        builder.append(VARIABLE_ACCESS_END);
    }
}
