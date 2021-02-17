package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.ast.structure.JTJNode;

public class JTJString extends JTJNode {

    private final String value;

    public JTJString(JTJChildrenNode parent, String value) {
        super(parent);
        this.value = value;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(value);
    }
}
