package com.github.tth05.jtjnst.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.tth05.jtjnst.ast.util.ASTUtils;

public class JTJMethodCall extends JTJChildrenNode {
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
            MethodDeclaration methodDeclaration = ((JavaParserMethodDeclaration) declaration).getWrappedNode();
            //TODO:
            if (!methodDeclaration.isStatic())
                throw new UnsupportedOperationException();

            JTJMethod jtjMethod = this.program.findMethod(ASTUtils.generateSignature(methodDeclaration));
            if (jtjMethod == null)
                throw new IllegalStateException();

            builder.append("((BiConsumer<List<Object>, List<Object>>) global.get(%d)).accept(Arrays.asList(".formatted(jtjMethod.getId()));
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append("), null)");
        } else {
            builder.append(".");
            builder.append(declaration.getName());
            builder.append("(");
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(")");
        }
    }
}
