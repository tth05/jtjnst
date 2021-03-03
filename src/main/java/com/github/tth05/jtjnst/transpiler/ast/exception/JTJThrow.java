package com.github.tth05.jtjnst.transpiler.ast.exception;

import com.github.tth05.jtjnst.transpiler.ast.JTJProgram;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

public class JTJThrow extends JTJNode {

    public static final String THROW_ID_PREFIX = "jtjThrow";

    private static final String THROWS = JTJProgram.ACCESS_UNSAFE_INSTANCE + ".throwException(%s)";

    private final String id;

    public JTJThrow(JTJChildrenNode parent, int id) {
        super(parent);
        this.id = "new RuntimeException(\"" + THROW_ID_PREFIX + id + "\")";
    }

    public JTJThrow(JTJChildrenNode parent, String exception) {
        super(parent);
        this.id = exception;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(THROWS.formatted(id));
    }
}
