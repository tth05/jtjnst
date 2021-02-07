package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.javaparser.utils.Pair;
import com.github.tth05.jtjnst.ast.*;

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
                private JTJChildrenNode currentNode;

                @Override
                public void visit(MethodDeclaration n, Object arg) {
                    currentMethod = new JTJMethod();
                    currentNode = currentMethod;

                    super.visit(n, arg);

                    program.addMethod(currentMethod);
                    currentNode = null;
                    currentMethod = null;
                }

                @Override
                public void visit(ExpressionStmt n, Object arg) {
                    JTJStatement statement = new JTJStatement(currentNode);
                    currentNode.addChild(statement);
                    currentNode = statement;
                    n.getExpression().accept(this, arg);
                    currentNode = currentNode.getParent();
                }

                @Override
                public void visit(MethodCallExpr n, Object arg) {
                    n.getScope().ifPresent(l -> l.accept(this, arg));

                    String prefix = n.getScope().isPresent() ? "." : "";
                    currentNode.addChild(new JTJString(currentNode, prefix + n.getName().getIdentifier() + "("));
                    n.getArguments().forEach(p -> p.accept(this, arg));
                    currentNode.addChild(new JTJString(currentNode, ")"));

                    //TODO: type arguments
//                    n.getTypeArguments().ifPresent(l -> l.forEach(v -> v.accept(this, arg)));
                }

                @Override
                public void visit(FieldAccessExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, n.getTokenRange().get().toString()));
                }

                @Override
                public void visit(BinaryExpr n, Object arg) {
                    n.getLeft().accept(this, arg);
                    currentNode.addChild(new JTJString(currentNode, n.getOperator().asString()));
                    n.getRight().accept(this, arg);
                }

                @Override
                public void visit(IfStmt n, Object arg) {
                    JTJStatement statement = new JTJStatement(currentNode);
                    JTJIfStatement ifStatement = new JTJIfStatement(statement);
                    currentNode = ifStatement.getCondition();
                    n.getCondition().accept(this, arg);
                    currentNode = ifStatement.getThenBlock();
                    n.getThenStmt().accept(this, arg);
                    currentNode = ifStatement.getElseBlock();
                    n.getElseStmt().ifPresent(l -> l.accept(this, arg));

                    currentNode = statement.getParent();

                    statement.addChild(ifStatement);
                    currentNode.addChild(statement);
                }

                @Override
                public void visit(WhileStmt n, Object arg) {
                    JTJStatement statement = new JTJStatement(currentNode);
                    JTJWhileStatement whileStatement = new JTJWhileStatement(statement);
                    currentNode = whileStatement.getCondition();
                    n.getCondition().accept(this, arg);
                    currentNode = whileStatement.getBody();
                    n.getBody().accept(this, arg);

                    currentNode = statement.getParent();

                    statement.addChild(whileStatement);
                    currentNode.addChild(statement);
                }

                @Override
                public void visit(ObjectCreationExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, n.getTokenRange().get().toString()));
                }

                @Override
                public void visit(ThisExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, "this"));
                }

                @Override
                public void visit(IntegerLiteralExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, n.asNumber() + ""));
                }

                @Override
                public void visit(StringLiteralExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, "\"" + n.asString() + "\""));
                }

                @Override
                public void visit(BooleanLiteralExpr n, Object arg) {
                    currentNode.addChild(new JTJString(currentNode, n.getValue() + ""));
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
