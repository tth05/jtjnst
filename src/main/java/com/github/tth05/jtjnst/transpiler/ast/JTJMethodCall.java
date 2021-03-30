package com.github.tth05.jtjnst.transpiler.ast;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.tth05.jtjnst.transpiler.VariableStack;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJEmpty;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJStatement;
import com.github.tth05.jtjnst.transpiler.util.ASTUtils;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class JTJMethodCall extends JTJChildrenNode {

    public static final String METHOD_CALL_START_WITH_RETURN = "((%s)";
    public static final String METHOD_CALL_START = "((java.util.function.BiFunction<java.util.List<Object>, Object[], Object[]>)global.get(%d)).apply(java.util.List.of(";
    public static final String METHOD_CALL_END = "), new Object[0])";
    public static final String METHOD_CALL_END_WITH_RETURN = "), new Object[1])[0])";

    private final JTJProgram program;
    private final ResolvedMethodDeclaration declaration;

    private final JTJEmpty scope = new JTJEmpty(this);

    public JTJMethodCall(JTJChildrenNode parent, JTJProgram program, ResolvedMethodDeclaration declaration) {
        super(parent);
        this.program = program;
        this.declaration = declaration;
    }

    public JTJEmpty getScope() {
        return scope;
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

            //add cast to start if method has return type and the return type is not ignored
            if (returnType != null && !(this.getParent() instanceof JTJStatement && this.getParent().getChildren().size() == 1))
                builder.append(METHOD_CALL_START_WITH_RETURN.formatted(returnType));

            builder.append(METHOD_CALL_START.formatted(jtjMethod.getId()));

            //add dummy argument for static methods
            if (methodDeclaration.isStatic()) {
                addChildToFront(new JTJString(null, "0"));
            } else {
                if (!scope.getChildren().isEmpty()) {
                    scope.appendToStr(builder);
                } else {
                    builder.append(VariableStack.ScopeType.INSTANCE_FIELDS.getMapName().formatted(JTJObjectCreation.TYPE_CAST));
                }

                if (!this.getChildren().isEmpty())
                    builder.append(",");
            }

            appendChildrenToBuilderWithSeparator(builder, ",");

            //get return value from returned list if the return value is not ignored
            if (returnType != null && !(this.getParent() instanceof JTJStatement && this.getParent().getChildren().size() == 1))
                builder.append(METHOD_CALL_END_WITH_RETURN);
            else
                builder.append(METHOD_CALL_END);
        } else if (declaration.getNumberOfSpecifiedExceptions() < 1) {
            if (declaration.isStatic())
                builder.append(declaration.declaringType().getQualifiedName());
            else
                scope.appendToStr(builder);
            builder.append(".");
            builder.append(declaration.getName());
            builder.append("(");
            appendChildrenToBuilderWithSeparator(builder, ",");
            builder.append(")");
        } else {
            StringBuilder paramBuilder = new StringBuilder();
            appendChildrenToBuilderWithSeparator(paramBuilder, ",");

            String scopeStr = scope.asString();

            //invoke the given method using reflection to avoid catching checked exceptions
            builder.append(JTJProgram.CALL_REFLECTION_METHOD.formatted(
                    //return type cast
                    declaration.getReturnType().describe().replace("void", "Object"),
                    //instance
                    scopeStr.isBlank() ? "0" : scopeStr,
                    //method name
                    declaration.getName(),
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
