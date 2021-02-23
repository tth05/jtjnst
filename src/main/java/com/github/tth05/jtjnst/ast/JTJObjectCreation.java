package com.github.tth05.jtjnst.ast;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.util.ASTUtils;

public class JTJObjectCreation extends JTJChildrenNode {

    public static final String TYPE_CAST = "java.util.Map<Integer, Object>";

    private final JTJProgram program;
    private final ResolvedConstructorDeclaration declaration;

    public JTJObjectCreation(JTJChildrenNode parent, JTJProgram program, ResolvedConstructorDeclaration declaration) {
        super(parent);
        this.program = program;
        this.declaration = declaration;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        if (declaration instanceof JavaParserConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = ((JavaParserConstructorDeclaration<?>) declaration).getWrappedNode();

            JTJMethod jtjMethod = this.program.findConstructor(ASTUtils.generateSignatureForMethod(constructorDeclaration));
            if (jtjMethod == null)
                throw new IllegalStateException("Member with signature " + ASTUtils.generateSignatureForMethod(constructorDeclaration) + " not found");

            builder.append(JTJMethodCall.METHOD_CALL_START_WITH_RETURN.formatted(TYPE_CAST));
            builder.append(JTJMethodCall.METHOD_CALL_START.formatted(jtjMethod.getId()));

            appendChildrenToBuilderWithSeparator(builder, ",");

            builder.append(JTJMethodCall.METHOD_CALL_END_WITH_RETURN);
        } else {
            builder.append("new ");
            builder.append(declaration.getPackageName());
            builder.append(".");
            builder.append(declaration.getName());
            builder.append("(");
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(")");
        }
    }
}
