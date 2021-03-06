package com.github.tth05.jtjnst.transpiler.ast;

import com.github.tth05.jtjnst.transpiler.VariableStack;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

public class JTJField extends JTJChildrenNode {

    private static final String FIELD_START = "((%s)args.get(0)).put(%d,";
    private static final String FIELD_END = ")";

    private final VariableStack.Variable variable;

    public JTJField(JTJChildrenNode parent, VariableStack.Variable variable) {
        super(parent);
        this.variable = variable;
    }

    public VariableStack.Variable getVariable() {
        return variable;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(String.format(FIELD_START, JTJObjectCreation.TYPE_CAST, variable.getNewName()));
        appendChildrenToBuilder(builder);
        builder.append(FIELD_END);
    }
}
