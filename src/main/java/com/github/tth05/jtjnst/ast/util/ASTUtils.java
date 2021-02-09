package com.github.tth05.jtjnst.ast.util;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.tth05.jtjnst.ast.JTJChildrenNode;
import com.github.tth05.jtjnst.ast.JTJWhileStatement;

public class ASTUtils {

    public static String getLabelFromParentNode(Node node) {
        if (node.getParentNode().isEmpty())
            throw new IllegalStateException("No parent");

        Node parentNode = node.getParentNode().get();

        return parentNode instanceof LabeledStmt ?
                ((LabeledStmt) parentNode).getLabel().asString() : null;
    }

    public static JTJWhileStatement findClosestWhile(JTJChildrenNode node, String label) {
        while ((node = node.getParent()) != null) {
            if (node instanceof JTJWhileStatement && ((JTJWhileStatement) node).doesLabelMatch(label)) {
                return (JTJWhileStatement) node;
            }
        }

        throw new IllegalStateException("No while found");
    }
}
