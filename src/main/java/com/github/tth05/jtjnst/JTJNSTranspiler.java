package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.tth05.jtjnst.ast.*;
import com.github.tth05.jtjnst.ast.util.ASTUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JTJNSTranspiler {

    //start at 10 to reserve some values for special stuff
    private static int ID = 10;

    private final JTJProgram program = new JTJProgram();

    private final VariableStack variableStack = new VariableStack();
    {
        variableStack.push(VariableStack.ScopeType.GLOBAL);
    }

    public JTJNSTranspiler(String... files) {
        ParserConfiguration configuration = new ParserConfiguration();
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        configuration.setSymbolResolver(symbolSolver);

        JavaParser parser = new JavaParser(configuration);

        List<CompilationUnit> parsedUnits = new ArrayList<>(files.length);
        for (String file : files) {
            Optional<CompilationUnit> result = parser.parse(file).getResult();
            if (result.isEmpty())
                throw new IllegalArgumentException("Invalid file supplied");

            parsedUnits.add(result.get());
        }

        combinedTypeSolver.add(new PreParsedJavaParserTypeSolver(parsedUnits));
        buildProgram(parsedUnits);
    }

    private void buildProgram(List<CompilationUnit> units) {
        //collect all classes and methods to generate unique ids
        for (CompilationUnit unit : units) {
            for (TypeDeclaration<?> type : unit.getTypes()) {
                JTJClass clazz = new JTJClass(ASTUtils.resolveFullName(type));
                for (BodyDeclaration<?> member : type.getMembers()) {
                    //TODO: constructor declaration
                    if (member instanceof MethodDeclaration) {
                        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
                        String signature = ASTUtils.generateSignature(methodDeclaration);

                        //detect main method
                        if (signature.endsWith("main([Ljava.lang.String;)") &&
                            methodDeclaration.isStatic() &&
                            methodDeclaration.getModifiers().contains(Modifier.staticModifier()) &&
                            methodDeclaration.getModifiers().contains(Modifier.publicModifier()) &&
                            methodDeclaration.getType().isVoidType()) {
                            clazz.addMethod(new JTJMethod(signature, true));
                        } else {
                            clazz.addMethod(new JTJMethod(signature));
                        }
                    }
                }

                this.program.addClass(clazz);
            }
        }
//        type.accept(new CustomVisitor(clazz), null);

        for (CompilationUnit unit : units) {
            for (TypeDeclaration<?> type : unit.getTypes()) {
                JTJClass clazz = this.program.findClass(ASTUtils.resolveFullName(type));

                for (BodyDeclaration<?> member : type.getMembers()) {
                    //TODO: constructor declaration
                    if (member instanceof MethodDeclaration) {
                        JTJMethod jtjMethod = clazz.findMethod(ASTUtils.generateSignature((MethodDeclaration) member));

                        member.accept(new CustomVisitor(jtjMethod), null);
                    }
                }
            }
        }

        System.out.println("Done");
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

        private final JTJMethod currentMethod;
        private JTJChildrenNode currentNode;

        private CustomVisitor(JTJMethod currentMethod) {
            this.currentMethod = currentMethod;
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            currentNode = currentMethod;

            variableStack.push(VariableStack.ScopeType.PARAM);
            for (Parameter parameter : n.getParameters()) {
                variableStack.addVariable(parameter.getNameAsString(), parameter.getTypeAsString());
            }
            variableStack.push(VariableStack.ScopeType.LOCAL);

            super.visit(n, arg);

            variableStack.pop();
            variableStack.pop();
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
            ResolvedMethodDeclaration resolvedMethod = n.resolve();

            n.getScope().ifPresent(s -> {
                //if the scope is a class name, we don't want to parse it
                if (!(s instanceof NameExpr))
                    s.accept(this, arg);
            });

            //TODO: type arguments
            JTJMethodCall jtjMethodCall = new JTJMethodCall(this.currentNode, program, resolvedMethod);
            this.currentNode = jtjMethodCall;
            n.getArguments().forEach(p -> p.accept(this, arg));
            this.currentNode = this.currentNode.getParent();
            this.currentNode.addChild(jtjMethodCall);
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

            JTJVariableAssign assign = new JTJVariableAssign(currentNode, variableStack.findVariable(n.getTarget().asNameExpr().getNameAsString()));
            currentNode = assign;

            n.getValue().accept(this, arg);

            currentNode = assign.getParent();
            currentNode.addChild(assign);
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
            JTJEmpty jtjEmpty = new JTJEmpty(currentNode);
            currentNode = jtjEmpty;

            n.getLeft().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, n.getOperator().asString()));
            n.getRight().accept(this, arg);

            currentNode = currentNode.getParent();
            currentNode.addChild(jtjEmpty);
        }

        @Override
        public void visit(UnaryExpr n, Object arg) {
            if (!n.getExpression().isNameExpr())
                throw new UnsupportedOperationException();

            VariableStack.Variable variable = variableStack.findVariable(n.getExpression().asNameExpr().getNameAsString());
            String method = n.isPrefix() ? "compute" : "put";

            currentNode.addChild(new JTJString(currentNode, variable.getScope().getScopeType().getMapName() +
                                                            "." + method + "(" + variable.getNewName() + "," +
                                                            (n.isPrefix() ? "(k" + uniqueID() + ", v" + uniqueID() + ") ->" : "")));

            currentNode.addChild(new JTJVariableAccess(currentNode, variable));
            currentNode.addChild(new JTJString(currentNode, n.getOperator().asString().substring(1) + "1"));
            currentNode.addChild(new JTJString(currentNode, ")"));
        }

        @Override
        public void visit(IfStmt n, Object arg) {
            JTJIfStatement ifStatement = new JTJIfStatement(currentNode);
            currentNode = ifStatement.getCondition();
            n.getCondition().accept(this, arg);
            currentNode = ifStatement.getThenBlock();
            n.getThenStmt().accept(this, arg);
            currentNode = ifStatement.getElseBlock();
            n.getElseStmt().ifPresent(l -> l.accept(this, arg));

            currentNode = ifStatement.getParent();

            currentNode.addChild(ifStatement);
        }

        @Override
        public void visit(WhileStmt n, Object arg) {
            JTJWhileStatement whileStatement = new JTJWhileStatement(currentNode, ASTUtils.getLabelFromParentNode(n));

            currentNode = whileStatement.getCondition();
            n.getCondition().accept(this, arg);
            currentNode = whileStatement.getBody();
            n.getBody().accept(this, arg);

            currentNode = whileStatement.getParent();

            currentNode.addChild(whileStatement);
        }

        @Override
        public void visit(BreakStmt n, Object arg) {
            String label = n.getLabel().map(SimpleName::asString).orElse(null);

            JTJWhileStatement whileStatement = ASTUtils.findClosestWhile(currentNode, label);
            int id = uniqueID();
            whileStatement.addBreakStatement(id);

            currentNode.addChild(new JTJThrow(currentNode, id));
        }

        @Override
        public void visit(ContinueStmt n, Object arg) {
            String label = n.getLabel().map(SimpleName::asString).orElse(null);

            JTJWhileStatement whileStatement = ASTUtils.findClosestWhile(currentNode, label);
            int id = uniqueID();
            whileStatement.addContinueStatement(id);

            currentNode.addChild(new JTJThrow(currentNode, id));
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
            currentNode.addChild(new JTJString(currentNode, "\"" + n.asString().replace(";", "\\u003b") + "\""));
        }

        @Override
        public void visit(BooleanLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, n.getValue() + ""));
        }
    }
}
