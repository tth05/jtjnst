package com.github.tth05.jtjnst.ast.exception;

import com.github.tth05.jtjnst.JTJNSTranspiler;
import com.github.tth05.jtjnst.ast.statement.JTJIfRunnableBlock;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;

import java.util.Collection;

public class JTJCatchBlock extends JTJIfRunnableBlock {

    public static final String CATCH_VARIABLE_PREFIX = "jtjEx";
    private static final String CATCH_START = "catch(";
    private static final String CATCH_MIDDLE = "){";
    private static final String CATCH_END = "}";

    private final String variableName = CATCH_VARIABLE_PREFIX + JTJNSTranspiler.uniqueID();
    private final Collection<String> exceptions;

    public JTJCatchBlock(JTJChildrenNode parent, Collection<String> exceptions) {
        super(parent);
        if (exceptions.isEmpty())
            throw new IllegalArgumentException();

        this.exceptions = exceptions;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(CATCH_START);
        builder.append(String.join("|", exceptions)).append(" ").append(this.variableName);
        builder.append(CATCH_MIDDLE);
        super.appendToStr(builder);
        builder.append(CATCH_END);
    }
}
