package com.github.tth05.jtjnst.ast.variable;

import com.github.tth05.jtjnst.VariableStack;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.ast.structure.JTJNode;

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
        String variableType = variable.getType();
        boolean needsCast = !variableType.equals("short") && !variableType.equals("byte");

        if (needsCast) {
            builder.append(VARIABLE_ACCESS_START);
            builder.append(variableType);
            builder.append(VARIABLE_ACCESS_MIDDLE_1);
        } else {
            //Not the best solution, converts short and byte back to their types because they will be put as integers
            // into the maps.
            // byte b = 5; -> local.put(10, 5); -> local.get(10) returns a integer
            String capitalizedType = variableType.substring(0, 1).toUpperCase() + variableType.substring(1);
            builder.append("java.lang.")
                    .append(capitalizedType)
                    .append(".parse")
                    .append(capitalizedType)
                    .append("(\"\" + ");
        }

        builder.append(variable.getScope().getScopeType().getMapName());
        builder.append(VARIABLE_ACCESS_MIDDLE_2);
        builder.append(variable.getNewName());
        builder.append(VARIABLE_ACCESS_END);
    }
}
