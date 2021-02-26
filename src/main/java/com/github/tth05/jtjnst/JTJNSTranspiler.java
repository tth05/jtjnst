package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.tth05.jtjnst.ast.*;
import com.github.tth05.jtjnst.ast.statement.JTJIfStatement;
import com.github.tth05.jtjnst.ast.statement.JTJWhileStatement;
import com.github.tth05.jtjnst.ast.structure.*;
import com.github.tth05.jtjnst.util.ASTUtils;
import com.github.tth05.jtjnst.ast.variable.JTJVariableAccess;
import com.github.tth05.jtjnst.ast.variable.JTJVariableAssign;
import com.github.tth05.jtjnst.ast.variable.JTJVariableDeclaration;

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
                        if (ASTUtils.isMainMethod(signature, methodDeclaration)) {
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
            pushNode(new JTJStatement(currentNode));
            n.getExpression().accept(this, arg);
            popNode();
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
            pushNode(new JTJMethodCall(this.currentNode, program, resolvedMethod));
            n.getArguments().forEach(p -> {
                pushNode(new JTJEmpty(currentNode));
                p.accept(this, arg);
                popNode();
            });
            popNode();
        }

        @Override
        public void visit(VariableDeclarationExpr n, Object arg) {
            if (n.getVariables().size() > 1) {
                pushNode(new JTJBlock(currentNode));
            }

            n.getVariables().forEach(p -> p.accept(this, arg));

            if (n.getVariables().size() > 1) {
                popNode();
            }
        }

        @Override
        public void visit(VariableDeclarator n, Object arg) {
            variableStack.addVariable(n.getNameAsString(), n.getType().asString());

            VariableStack.Variable variable = variableStack.findVariable(n.getNameAsString());
            pushNode(new JTJVariableDeclaration(
                    variable.getScope().getScopeType().getMapName(),
                    variable.getNewName(),
                    currentNode));

            //TODO: types
            n.getInitializer().ifPresent(l -> l.accept(this, arg));
            popNode();
        }

        @Override
        public void visit(AssignExpr n, Object arg) {
            if (n.getTarget().isNameExpr()) {
                pushNode(new JTJVariableAssign(currentNode, variableStack.findVariable(n.getTarget().asNameExpr().getNameAsString())));
                n.getValue().accept(this, arg);
                popNode();
            } else if (n.getTarget().isArrayAccessExpr()) {
                n.getTarget().accept(this, arg);
                currentNode.addChild(new JTJString(currentNode, "="));
                n.getValue().accept(this, arg);
            } else throw new UnsupportedOperationException();
        }

        @Override
        public void visit(NameExpr n, Object arg) {
            VariableStack.Variable variable = variableStack.findVariable(n.getNameAsString());

            if (variable != null)
                currentNode.addChild(new JTJVariableAccess(currentNode, variable));
            else
                currentNode.addChild(new JTJString(currentNode, n.calculateResolvedType().describe()));
        }

        @Override
        public void visit(FieldAccessExpr n, Object arg) {
            n.getScope().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, "."));
            //TODO: fields of our custom types
            currentNode.addChild(new JTJString(currentNode, n.getNameAsString()));
        }

        @Override
        public void visit(BinaryExpr n, Object arg) {
            pushNode(new JTJEmpty(currentNode));

            n.getLeft().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, n.getOperator().asString()));
            n.getRight().accept(this, arg);

            popNode();
        }

        @Override
        public void visit(UnaryExpr n, Object arg) {
            String operator = n.getOperator().asString().length() > 1 ?
                    n.getOperator().asString().substring(1) + "1" :
                    n.getOperator().asString();

            //variable access needs to use compute for prefix operators
            if (n.getExpression().isNameExpr()) {
                VariableStack.Variable variable = variableStack.findVariable(n.getExpression().asNameExpr().getNameAsString());
                String method = n.isPrefix() ? "compute" : "put";

                currentNode.addChild(new JTJString(currentNode, variable.getScope().getScopeType().getMapName() +
                                                                "." + method + "(" + variable.getNewName() + "," +
                                                                (n.isPrefix() ? "(k" + uniqueID() + ", v" + uniqueID() + ") ->" : "")));

                currentNode.addChild(new JTJVariableAccess(currentNode, variable));
                currentNode.addChild(new JTJString(currentNode, operator));
                currentNode.addChild(new JTJString(currentNode, ")"));
            } else {
                if (n.isPrefix())
                    currentNode.addChild(new JTJString(currentNode, operator));

                n.getExpression().accept(this, arg);

                if (n.isPostfix())
                    currentNode.addChild(new JTJString(currentNode, operator));
            }
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
        public void visit(ConditionalExpr n, Object arg) {
            pushNode(new JTJEmpty(currentNode));

            n.getCondition().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, "?"));
            n.getThenExpr().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, ":"));
            n.getElseExpr().accept(this, arg);

            popNode();
        }

        @Override
        public void visit(ForStmt n, Object arg) {
            //convert for loop to while loop
            variableStack.push(VariableStack.ScopeType.FOR_LOOP);

            JTJWhileStatement whileStatement = new JTJWhileStatement(currentNode, ASTUtils.getLabelFromParentNode(n));

            n.getInitialization().forEach(i -> {
                pushNode(new JTJStatement(currentNode));
                i.accept(this, arg);
                popNode();
            });

            currentNode = whileStatement.getCondition();
            n.getCompare().ifPresentOrElse(c -> c.accept(this, arg), () -> currentNode.addChild(new JTJString(currentNode, "true")));
            currentNode = whileStatement.getBody();
            n.getBody().accept(this, arg);
            n.getUpdate().forEach(u -> {
                pushNode(new JTJStatement(currentNode));
                u.accept(this, arg);
                popNode();
            });

            currentNode = whileStatement.getParent();
            currentNode.addChild(whileStatement);

            variableStack.pop();
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
        public void visit(ReturnStmt n, Object arg) {
            //TODO: return needs to actually return
            pushNode(new JTJStatement(currentNode));

            currentNode.addChild(new JTJString(currentNode, "retPtr[0] ="));
            n.getExpression().ifPresent(e -> e.accept(this, arg));

            popNode();
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
        public void visit(ArrayAccessExpr n, Object arg) {
            //TODO: array of our custom types

            n.getName().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, "["));
            n.getIndex().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, "]"));
        }

        @Override
        public void visit(ArrayCreationExpr n, Object arg) {
            //TODO: array of our custom types
            currentNode.addChild(new JTJString(currentNode, "new " + n.getElementType().asString()));
            n.getLevels().forEach(p -> currentNode.addChild(new JTJString(currentNode, p.getTokenRange().get().toString())));

            n.getInitializer().ifPresent(l -> l.accept(this, arg));
        }

        @Override
        public void visit(ArrayInitializerExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, "{"));

            NodeList<Expression> values = n.getValues();
            for (int i = 0; i < values.size(); i++) {
                Expression p = values.get(i);
                p.accept(this, arg);

                if (i != values.size() - 1)
                    currentNode.addChild(new JTJString(currentNode, ","));
            }

            currentNode.addChild(new JTJString(currentNode, "}"));
        }

        @Override
        public void visit(EnclosedExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, "("));
            n.getInner().accept(this, arg);
            currentNode.addChild(new JTJString(currentNode, ")"));
        }

        @Override
        public void visit(IntegerLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, n.asNumber() + ""));
        }

        @Override
        public void visit(StringLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, "\"" + n.getValue().replace(";", "\\u003b") + "\""));
        }

        @Override
        public void visit(BooleanLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, n.getValue() + ""));
        }

        @Override
        public void visit(CharLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, "'" + n.getValue().replace(";", "\\u003b") + "'"));
        }

        @Override
        public void visit(DoubleLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, n.getValue()));
        }

        @Override
        public void visit(LongLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, n.getValue()));
        }

        @Override
        public void visit(NullLiteralExpr n, Object arg) {
            currentNode.addChild(new JTJString(currentNode, "null"));
        }

        private void pushNode(JTJChildrenNode node) {
            currentNode = node;
        }

        private void popNode() {
            currentNode.getParent().addChild(currentNode);
            currentNode = currentNode.getParent();
        }
    }
}
