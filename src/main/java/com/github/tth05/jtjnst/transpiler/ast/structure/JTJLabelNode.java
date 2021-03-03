package com.github.tth05.jtjnst.transpiler.ast.structure;

public abstract class JTJLabelNode extends JTJChildrenNode {
    private final String label;

    public JTJLabelNode(JTJChildrenNode parent, String label) {
        super(parent);
        this.label = label;
    }

    public boolean doesLabelMatch(String otherLabel) {
        if (this.label == null && otherLabel == null)
            return true;

        if (this.label == null)
            return false;

        return this.label.equals(otherLabel);
    }

    public String getLabel() {
        return label;
    }
}
