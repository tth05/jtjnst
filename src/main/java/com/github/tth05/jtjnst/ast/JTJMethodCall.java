package com.github.tth05.jtjnst.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.util.ASTUtils;

public class JTJMethodCall extends JTJChildrenNode {

    public static final String METHOD_CALL_START_WITH_RETURN = "((%s)";
    public static final String METHOD_CALL_START = "((BiFunction<List<Object>, Object[], Object[]>)global.get(%d)).apply(java.util.List.of(";
    public static final String METHOD_CALL_END = "), new Object[0])";
    public static final String METHOD_CALL_END_WITH_RETURN = "), new Object[1])[0])";

    private final JTJProgram program;
    private final ResolvedMethodDeclaration declaration;

    public JTJMethodCall(JTJChildrenNode parent, JTJProgram program, ResolvedMethodDeclaration declaration) {
        super(parent);
        this.program = program;
        this.declaration = declaration;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        if (declaration instanceof JavaParserMethodDeclaration) {
            String returnType = declaration.getReturnType().isVoid() ? null : declaration.getReturnType().describe();
            if (this.program.findClass(returnType) != null)
                returnType = JTJObjectCreation.TYPE_CAST;

            MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) declaration).getWrappedNode();

            JTJMethod jtjMethod = this.program.findMethod(ASTUtils.generateSignatureForMethod(methodDeclaration));
            if (jtjMethod == null)
                throw new IllegalStateException("Member with signature " + ASTUtils.generateSignatureForMethod(methodDeclaration) + " not found");

            //add cast to start if method has return type
            if (returnType != null)
                builder.append(METHOD_CALL_START_WITH_RETURN.formatted(returnType));

            builder.append(METHOD_CALL_START.formatted(jtjMethod.getId()));

            //add dummy argument for static methods
            if (methodDeclaration.isStatic())
                addChildToFront(new JTJString(null, "0"));

            appendChildrenToBuilderWithSeparator(builder, ",");

            //get return value from returned list
            if (returnType != null)
                builder.append(METHOD_CALL_END_WITH_RETURN);
            else
                builder.append(METHOD_CALL_END);
        } else {
            if (declaration.isStatic())
                builder.append(declaration.declaringType().getQualifiedName());
            builder.append(".");
            builder.append(declaration.getName());
            builder.append("(");
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(")");
        }
    }
}
