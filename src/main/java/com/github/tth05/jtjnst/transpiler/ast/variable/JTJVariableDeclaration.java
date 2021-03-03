package com.github.tth05.jtjnst.transpiler.ast.variable;

import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

public class JTJVariableDeclaration extends JTJChildrenNode {

    public static final String DECLARATION_START = "%s.put(%d,";
    public static final String DECLARATION_END = ")";

    private final String mapName;
    private final int newName;

    public JTJVariableDeclaration(JTJChildrenNode parent, String mapName, int newName) {
        super(parent);
        this.mapName = mapName;
        this.newName = newName;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(DECLARATION_START.formatted(mapName, newName));
        appendChildrenToBuilder(builder);
        builder.append(DECLARATION_END);
    }
}
