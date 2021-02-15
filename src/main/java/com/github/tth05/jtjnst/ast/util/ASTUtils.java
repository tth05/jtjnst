package com.github.tth05.jtjnst.ast.util;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
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

    public static String resolveFullName(TypeDeclaration<?> type) {
        ResolvedReferenceTypeDeclaration resolved = type.resolve();
        String packageName = resolved.getPackageName();
        return (packageName.length() > 0 ? packageName + "." : "") + resolved.getClassName();
    }

    /**
     * @return a jvm type signature of the given method; does not replace {@code .} with {@code /}
     */
    public static String generateSignature(MethodDeclaration declaration) {
        StringBuilder signature = new StringBuilder(resolveFullName((TypeDeclaration<?>) declaration.getParentNode().get()))
                .append(".")
                .append(declaration.getNameAsString())
                .append("(");

        for (Parameter parameter : declaration.getParameters()) {
            signature.append(getTypeSignatureFromType(parameter.getType().resolve()));
        }

        signature.append(")");
        signature.append(getTypeSignatureFromType(declaration.getType().resolve()));
        return signature.toString();
    }

    private static String getTypeSignatureFromType(ResolvedType type) {
        String typeStr = null;

        //TODO: multidimensional arrays
        boolean isArray = type.isArray();
        if (type.isArray())
            type = type.asArrayType().getComponentType();

        if (type.isReferenceType())
            typeStr = type.asReferenceType().getId();

        if (type.isPrimitive())
            typeStr = type.asPrimitive().describe();

        if(type.isVoid())
            return "";

        if (typeStr == null)
            throw new IllegalStateException();

        String result = switch (typeStr) {
            case "boolean" -> "Z";
            case "byte" -> "B";
            case "short" -> "S";
            case "char" -> "C";
            case "int" -> "I";
            case "long" -> "J";
            case "float" -> "F";
            case "double" -> "D";
            default -> "L" + typeStr + ";";
        };

        return (isArray ? "[" : "") + result;
    }
}
