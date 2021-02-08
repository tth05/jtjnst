package com.github.tth05.jtjnst.ast;

public class JTJVariableDeclaration extends JTJChildrenNode {

    public static final String DECLARATION_START = "%s.put(%d,";
    public static final String DECLARATION_END = ")";

    private final String mapName;
    private final int name;

    public JTJVariableDeclaration(String mapName, int name, JTJChildrenNode parent) {
        super(parent);
        this.mapName = mapName;
        this.name = name;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(DECLARATION_START.formatted(mapName, name));
        appendChildrenToBuilder(builder);
        builder.append(DECLARATION_END);
    }
}
