package com.github.tth05.jtjnst.transpiler.ast.variable;

import com.github.tth05.jtjnst.transpiler.VariableStack;
import com.github.tth05.jtjnst.transpiler.ast.JTJObjectCreation;
import com.github.tth05.jtjnst.transpiler.ast.JTJProgram;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

import java.util.Set;

public class JTJVariableAccess extends JTJNode {

    public static final String VARIABLE_ACCESS_START = "((";
    public static final String VARIABLE_ACCESS_MIDDLE_1 = ")";
    public static final String VARIABLE_ACCESS_MIDDLE_2 = ".get(";
    public static final String VARIABLE_ACCESS_END = "))";

    private static final Set<String> REQUIRE_PARSE_TYPES = Set.of("byte", "short", "long");

    private final VariableStack.Variable variable;
    private final JTJProgram program;
    private String scope;

    public JTJVariableAccess(JTJChildrenNode parent, VariableStack.Variable variable, JTJProgram program) {
        super(parent);
        this.variable = variable;
        this.program = program;
        this.scope = variable.getScope().getScopeType().getMapName();
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        if (this.variable.getScope().getScopeType() == VariableStack.ScopeType.CATCH) {
            builder.append(this.variable.getOldName());
            return;
        }

        String variableType = variable.getType();
        boolean needsCast = !REQUIRE_PARSE_TYPES.contains(variableType);

        if (this.program.findClass(variableType) != null)
            variableType = JTJObjectCreation.TYPE_CAST;

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

        builder.append(this.scope);
        builder.append(VARIABLE_ACCESS_MIDDLE_2);
        builder.append(variable.getNewName());
        builder.append(VARIABLE_ACCESS_END);
    }
}
