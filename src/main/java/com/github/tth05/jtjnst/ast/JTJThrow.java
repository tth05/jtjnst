package com.github.tth05.jtjnst.ast;

public class JTJThrow extends JTJNode {

    public static final String THROW_ID_PREFIX = "jtjThrow";

    public static final String THROWS = JTJProgram.ACCESS_UNSAFE_INSTANCE + ".throwException(new RuntimeException(\"" + THROW_ID_PREFIX + "%d\"))";

    private final int id;

    public JTJThrow(JTJChildrenNode parent, int id) {
        super(parent);
        this.id = id;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(THROWS.formatted(id));
    }
}
