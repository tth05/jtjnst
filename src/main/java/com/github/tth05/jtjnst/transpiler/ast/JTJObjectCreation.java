package com.github.tth05.jtjnst.transpiler.ast;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedConstructorDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserConstructorDeclaration;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;
import com.github.tth05.jtjnst.transpiler.util.ASTUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void addChild(JTJNode node) {
        super.addChild(node);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        if (declaration instanceof JavaParserConstructorDeclaration) {
            ConstructorDeclaration constructorDeclaration = ((JavaParserConstructorDeclaration<?>) declaration).getWrappedNode();

            JTJMethod jtjMethod = this.program.findConstructor(ASTUtils.generateSignatureForMethod(constructorDeclaration));
            if (jtjMethod == null)
                throw new IllegalStateException("Member with signature " + ASTUtils.generateSignatureForMethod(constructorDeclaration) + " not found");

            JTJClass jtjClass = this.program.findClass(ASTUtils.resolveFullName((TypeDeclaration<?>) constructorDeclaration.getParentNode().get()));
            if (jtjClass == null)
                throw new IllegalStateException();


            builder.append(JTJMethodCall.METHOD_CALL_START_WITH_RETURN.formatted(TYPE_CAST));
            builder.append(JTJMethodCall.METHOD_CALL_START.formatted(jtjMethod.getId()));

            //add init method call as first argument
            builder.append(JTJMethodCall.METHOD_CALL_START_WITH_RETURN.formatted("Object"))
                    .append(JTJMethodCall.METHOD_CALL_START.formatted(jtjClass.getInitMethod().getId()))
                    .append("new java.util.HashMap<Integer,String>()")
                    .append(JTJMethodCall.METHOD_CALL_END_WITH_RETURN)
                    .append(!this.getChildren().isEmpty() ? "," : "");

            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(JTJMethodCall.METHOD_CALL_END_WITH_RETURN);
        } else if (declaration.getNumberOfSpecifiedExceptions() < 1) {
            builder.append("new ");
            builder.append(declaration.getPackageName());
            builder.append(".");
            builder.append(declaration.getName());
            builder.append("(");
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(")");
        } else {
            StringBuilder paramBuilder = new StringBuilder();
            appendChildrenToBuilderWithSeparator(paramBuilder, ",");

            //invoke the given constructor using reflection to avoid catching checked exceptions
            builder.append(JTJProgram.CALL_REFLECTION_CONSTRUCTOR.formatted(
                    //return type cast
                    declaration.declaringType().getQualifiedName(),
                    //class
                    declaration.declaringType().getQualifiedName() + ".class",
                    //array of parameters
                    IntStream.range(0, declaration.getNumberOfParams())
                            .mapToObj(i -> declaration.getParam(i).describeType() + ".class")
                            .collect(Collectors.joining(",")),
                    //parameters
                    paramBuilder.toString()
            ));
        }
    }
}
