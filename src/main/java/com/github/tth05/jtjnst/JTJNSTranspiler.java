package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.tth05.jtjnst.ast.JTJMethod;
import com.github.tth05.jtjnst.ast.JTJProgram;
import com.github.tth05.jtjnst.ast.JTJStatement;
import com.github.tth05.jtjnst.ast.JTJString;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class JTJNSTranspiler {

    private int counter = 0;

    private final JTJProgram program = new JTJProgram();
    private final Deque<Pair<String, List<String>>> variableStack = new ArrayDeque<>();

    public JTJNSTranspiler(String code) {
        ParserConfiguration configuration = new ParserConfiguration();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        configuration.setSymbolResolver(symbolSolver);

        JavaParser parser = new JavaParser(configuration);
        ParseResult<CompilationUnit> result = parser.parse(code);

        result.ifSuccessful(unit -> {
            unit.accept(new VoidVisitorAdapter<>() {

                private JTJMethod currentMethod = null;

                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    currentMethod = new JTJMethod();
                    super.visit(n, arg);
                    program.addMethod(currentMethod);
                }

                @Override
                public void visit(ExpressionStmt n, Object arg) {
                    JTJStatement statement = new JTJStatement();
                    statement.addChild(new JTJString(n.getExpression().getTokenRange().get().toString()));
                    currentMethod.addChild(statement);
                }
            }, null);
        });
    }

    public String getTranspiledCode() {
        StringBuilder builder = new StringBuilder();
        program.appendToStr(builder);
        return builder.toString();
    }
}
