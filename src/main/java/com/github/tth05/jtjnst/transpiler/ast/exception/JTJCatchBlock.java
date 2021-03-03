package com.github.tth05.jtjnst.transpiler.ast.exception;

import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;
import com.github.tth05.jtjnst.transpiler.ast.JTJString;
import com.github.tth05.jtjnst.transpiler.ast.statement.JTJIfRunnableBlock;
import com.github.tth05.jtjnst.transpiler.ast.statement.JTJIfStatement;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JTJCatchBlock extends JTJIfRunnableBlock {

    public static final String CATCH_VARIABLE_PREFIX = "jtjEx";
    private static final String CATCH_START = "catch(";
    private static final String CATCH_MIDDLE = "){";
    private static final String CATCH_END = "}";

    private String variableName = CATCH_VARIABLE_PREFIX + JTJNSTranspiler.uniqueID();
    private final List<String> exceptions;

    public JTJCatchBlock(JTJChildrenNode parent, Collection<String> exceptions) {
        super(parent);
        if (exceptions.isEmpty())
            throw new IllegalArgumentException();

        this.exceptions = new ArrayList<>(exceptions);
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(CATCH_START);
        builder.append("Throwable").append(" ").append(this.variableName);
        builder.append(CATCH_MIDDLE);

        if (!this.variableName.startsWith("jtjEx")) {
            //compiler -> "exception [...] is never thrown in body of corresponding try statement"
            JTJIfRunnableBlock wrapper = new JTJIfRunnableBlock(null);
            JTJIfStatement ifStatement = new JTJIfStatement(this);
            ifStatement.getCondition().addChild(new JTJString(ifStatement,
                    "!java.util.Optional.ofNullable(" + this.variableName + ".getMessage()).orElse(\"\")" +
                    ".startsWith(\"" + JTJThrow.THROW_ID_PREFIX + "\")&&(" +
                    //convert all exceptions to big or check -> e.getClass().equals(java.io.IOException.class) || [...]
                    exceptions.stream().map((e) -> this.variableName + ".getClass().equals(" + e + ".class)").collect(Collectors.joining("||")) +
                    ")"
            ));

            //add catch code when condition is true
            StringBuilder tmpBuilder = new StringBuilder();
            super.appendToStr(tmpBuilder);
            ifStatement.getThenBlock().addChild(new JTJString(null, tmpBuilder.toString()));

            //re-throw otherwise
            ifStatement.getElseBlock().addChild(new JTJThrow(null, this.variableName));

            wrapper.addChild(ifStatement);
            wrapper.appendToStr(builder);
        } else {
            super.appendToStr(builder);
        }

        builder.append(CATCH_END);
    }
}
