package com.github.tth05.jtjnst.transpiler.util;

import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.tth05.jtjnst.transpiler.ast.statement.JTJWhileStatement;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;

public class ASTUtils {

    public static boolean isMainMethod(String signature, MethodDeclaration declaration) {
        return signature.endsWith("main([Ljava.lang.String;)") &&
               declaration.isStatic() &&
               declaration.getModifiers().contains(Modifier.staticModifier()) &&
               declaration.getModifiers().contains(Modifier.publicModifier()) &&
               declaration.getType().isVoidType();
    }

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
     * @return a jvm type signature of the given method or constructor; does not replace {@code .} with {@code /}
     */
    public static String generateSignatureForMethod(CallableDeclaration<?> declaration) {
        StringBuilder signature = new StringBuilder(resolveFullName((TypeDeclaration<?>) declaration.getParentNode().get()))
                .append(".")
                .append(declaration.getNameAsString())
                .append("(");

        for (Parameter parameter : declaration.getParameters()) {
            signature.append(getTypeSignatureFromType(parameter.getType().resolve()));
        }

        signature.append(")");
        if (declaration instanceof MethodDeclaration)
            signature.append(getTypeSignatureFromType(((MethodDeclaration) declaration).getType().resolve()));

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

        if (type.isVoid())
            return "";

        if (typeStr == null)
            throw new IllegalStateException();

        String result;
        switch (typeStr) {
            case "boolean":
                result = "Z";
                break;
            case "byte":
                result = "B";
                break;
            case "short":
                result = "S";
                break;
            case "char":
                result = "C";
                break;
            case "int":
                result = "I";
                break;
            case "long":
                result = "J";
                break;
            case "float":
                result = "F";
                break;
            case "double":
                result = "D";
                break;
            default:
                result = "L" + typeStr + ";";
                break;
        }

        return (isArray ? "[" : "") + result;
    }
}
