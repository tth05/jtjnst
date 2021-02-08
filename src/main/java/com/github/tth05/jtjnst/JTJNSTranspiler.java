package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.tth05.jtjnst.ast.*;

public class JTJNSTranspiler {

    private static int ID = 0;

    private final JTJProgram program = new JTJProgram();
    private final VariableStack variableStack = new VariableStack();

    {
        variableStack.push(VariableStack.ScopeType.GLOBAL);
    }

    public JTJNSTranspiler(String code) {
        ParserConfiguration configuration = new ParserConfiguration();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        configuration.setSymbolResolver(symbolSolver);

        JavaParser parser = new JavaParser(configuration);
        ParseResult<CompilationUnit> result = parser.parse(code);

        result.ifSuccessful(unit -> unit.accept(new CustomVisitor(), null));
    }

    public String getTranspiledCode() {
        StringBuilder builder = new StringBuilder();
        program.appendToStr(builder);
        return builder.toString();
    }

    public static int uniqueID() {
        return ID++;
    }

    private class CustomVisitor extends VoidVisitorAdapter<Object> {
        private JTJChildrenNode currentNode;

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            JTJMethod currentMethod = new JTJMethod();
            currentNode = currentMethod;

            variableStack.push(VariableStack.ScopeType.PARAM);
            for (Parameter parameter : n.getParameters()) {
                variableStack.addVariable(parameter.getNameAsString(), parameter.getTypeAsString());
            }
            variableStack.push(VariableStack.ScopeType.LOCAL);

            super.visit(n, arg);

            variableStack.pop();
            variableStack.pop();

            program.addMethod(currentMethod);
            currentNode = null;
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
        public void visit(VariableDeclarator n, Object arg) {
            variableStack.addVariable(n.getNameAsString(), n.getType().asString());

            VariableStack.Variable variable = variableStack.findVariable(n.getNameAsString());
            JTJVariableDeclaration variableDeclaration = new JTJVariableDeclaration(
                    variable.getScope().getScopeType().getMapName(),
                    variable.getNewName(),
                    currentNode);

            currentNode = variableDeclaration;
            //TODO: types
            n.getInitializer().ifPresent(l -> l.accept(this, arg));
            currentNode = variableDeclaration.getParent();
            currentNode.addChild(variableDeclaration);
        }

        @Override
        public void visit(AssignExpr n, Object arg) {
            if (!n.getTarget().isNameExpr())
                throw new UnsupportedOperationException();

            VariableStack.Variable variable = variableStack.findVariable(n.getTarget().asNameExpr().getNameAsString());
            currentNode.addChild(new JTJString(currentNode, variable.getScope().getScopeType().getMapName() +
                                                            ".compute(" + variable.getNewName() +
                                                            ", (k" + uniqueID() + ", v" + uniqueID() + ") ->"));

            n.getValue().accept(this, arg);

            currentNode.addChild(new JTJString(currentNode, ")"));
        }

        @Override
        public void visit(NameExpr n, Object arg) {
            VariableStack.Variable variable = variableStack.findVariable(n.getNameAsString());

            currentNode.addChild(new JTJVariableAccess(currentNode, variable));
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
        public void visit(UnaryExpr n, Object arg) {
            if (!n.getExpression().isNameExpr()) {
                System.out.println("Unhandled unary expression found");
                return;
            }

            VariableStack.Variable variable = variableStack.findVariable(n.getExpression().asNameExpr().getNameAsString());
            String method = n.isPrefix() ? "merge" : "put";

            currentNode.addChild(new JTJString(currentNode, variable.getScope().getScopeType().getMapName() +
                                                            "." + method + "(" + variable.getNewName() + "," +
                                                            (n.isPrefix() ? "(k" + uniqueID() + ", v" + uniqueID() + ") ->" : "")));

            currentNode.addChild(new JTJVariableAccess(currentNode, variable));
            currentNode.addChild(new JTJString(currentNode, n.getOperator().asString().substring(1) + "1"));
            currentNode.addChild(new JTJString(currentNode, ")"));
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
    }

    public static void main(String[] args) {
        //language=Java
        String input = """
                public class Test {
                    public static void main(String[] args) {
                        int i = 5;
                        System.out.println(i+5);
                    }
                }
                """;

        String code = new JTJNSTranspiler(input).getTranspiledCode();
        System.out.println(code);
    }
}
