package com.github.tth05.jtjnst.transpiler.ast.variable;

import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;
import com.github.tth05.jtjnst.transpiler.VariableStack;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

public class JTJVariableAssign extends JTJChildrenNode {

    private static final String ASSIGN_MIDDLE = ".%s(";
    private static final String ASSIGN_MIDDLE_2 = ", (k%d, v%d) -> ";
    private static final String ASSIGN_MIDDLE_2_PUT = ", ";
    private static final String ASSIGN_MIDDLE_END = ")";

    private final VariableStack.Variable variable;
    private final boolean usePut;
    private String scope;

    public JTJVariableAssign(JTJChildrenNode parent, VariableStack.Variable variable) {
        this(parent, variable, false);
    }

    public JTJVariableAssign(JTJChildrenNode parent, VariableStack.Variable variable, boolean usePut) {
        super(parent);
        this.variable = variable;
        this.usePut = usePut;
        this.scope = variable.getScope().getScopeType().getMapName();
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(this.scope);
        builder.append(String.format(ASSIGN_MIDDLE, usePut ? "put" : "compute"));
        builder.append(variable.getNewName());

        if (usePut)
            builder.append(ASSIGN_MIDDLE_2_PUT);
        else
            builder.append(String.format(ASSIGN_MIDDLE_2, JTJNSTranspiler.uniqueID(), JTJNSTranspiler.uniqueID()));

        appendChildrenToBuilder(builder);
        builder.append(ASSIGN_MIDDLE_END);
    }
}
