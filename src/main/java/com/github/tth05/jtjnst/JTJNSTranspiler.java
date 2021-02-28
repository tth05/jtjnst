package com.github.tth05.jtjnst;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.declarations.ResolvedMethodDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedValueDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserClassDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserFieldDeclaration;
import com.github.javaparser.symbolsolver.javaparsermodel.declarations.JavaParserMethodDeclaration;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.github.tth05.jtjnst.ast.*;
import com.github.tth05.jtjnst.ast.statement.JTJIfStatement;
import com.github.tth05.jtjnst.ast.statement.JTJWhileStatement;
import com.github.tth05.jtjnst.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.ast.structure.JTJEmpty;
import com.github.tth05.jtjnst.ast.structure.JTJStatement;
import com.github.tth05.jtjnst.ast.variable.JTJVariableAccess;
import com.github.tth05.jtjnst.ast.variable.JTJVariableAssign;
import com.github.tth05.jtjnst.ast.variable.JTJVariableDeclaration;
import com.github.tth05.jtjnst.util.ASTUtils;

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

                //force add default constructor
                if (type.getConstructors().isEmpty()) {
                    type.addConstructor();
                }

                for (BodyDeclaration<?> member : type.getMembers()) {
                    if (member instanceof MethodDeclaration) {
                        MethodDeclaration methodDeclaration = (MethodDeclaration) member;
                        String signature = ASTUtils.generateSignatureForMethod(methodDeclaration);

                        //detect main method
                        if (ASTUtils.isMainMethod(signature, methodDeclaration)) {
                            clazz.addMethod(new JTJMethod(clazz, signature, true));
                        } else {
                            clazz.addMethod(new JTJMethod(clazz, signature));
                        }
                    } else if (member instanceof ConstructorDeclaration) {
                        clazz.addConstructor(new JTJMethod(clazz, ASTUtils.generateSignatureForMethod((ConstructorDeclaration) member)));
                    } else if (member instanceof FieldDeclaration) {
                        NodeList<VariableDeclarator> variables = ((FieldDeclaration) member).getVariables();
                        for (VariableDeclarator variable : variables) {
                            clazz.addField(new VariableStack.Variable(JTJClass.DUMMY_SCOPE, variable.getNameAsString(),
                                    JTJNSTranspiler.uniqueID(), variable.getTypeAsString()));
                        }
                    }
                }

                this.program.addClass(clazz);
            }
        }

        //parse all methods and fields
        for (CompilationUnit unit : units) {
            for (TypeDeclaration<?> type : unit.getTypes()) {
                JTJClass clazz = this.program.findClass(ASTUtils.resolveFullName(type));

                for (BodyDeclaration<?> member : type.getMembers()) {
                    if (member instanceof MethodDeclaration) {
                        JTJMethod jtjMethod = clazz.findMethod(ASTUtils.generateSignatureForMethod((MethodDeclaration) member));

                        member.accept(new CustomVisitor(jtjMethod), null);
                    } else if (member instanceof ConstructorDeclaration) {
                        JTJMethod jtjMethod = clazz.findConstructor(ASTUtils.generateSignatureForMethod((ConstructorDeclaration) member));

                        member.accept(new CustomVisitor(jtjMethod), null);

                        //return instance from constructor
                        jtjMethod.addChild(new JTJString(null, "retPtr[0] = args.get(0)"));
                    } else if (member instanceof FieldDeclaration) {
                        member.accept(new CustomVisitor(null), null);
                    } else {
                        throw new UnsupportedOperationException("Declarations of type " + member.getClass() + " not supported");
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
        public void visit(ConstructorDeclaration n, Object arg) {
            handleMethod(n, n.getBody());
        }

        @Override
        public void visit(ExplicitConstructorInvocationStmt n, Object arg) {
            super.visit(n, arg);
        }

        @Override
        public void visit(MethodDeclaration n, Object arg) {
            handleMethod(n, n.getBody().get());
        }

        private void handleMethod(CallableDeclaration<?> method, BlockStmt body) {
            //TODO: throws & return

            currentNode = currentMethod;

            if (!method.isStatic()) {
                variableStack.push(VariableStack.ScopeType.INSTANCE_FIELDS);
                currentMethod.getContainingClass().getFieldMap().forEach((k, v) -> {
                    variableStack.addVariable(v.getVariable());
                });
            }

            variableStack.push(VariableStack.ScopeType.THIS_INSTANCE);
            variableStack.addVariable("this", JTJObjectCreation.TYPE_CAST);
            variableStack.push(VariableStack.ScopeType.PARAM);
            for (Parameter parameter : method.getParameters()) {
                variableStack.addVariable(parameter.getNameAsString(), parameter.getTypeAsString());
            }
            variableStack.push(VariableStack.ScopeType.LOCAL);

            body.accept(this, null);

            variableStack.pop();
            variableStack.pop();
            variableStack.pop();

            if (!method.isStatic())
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

            boolean isStatic = resolvedMethod.isStatic();
            boolean isJavaParserDeclaration = resolvedMethod instanceof JavaParserMethodDeclaration;
            boolean switchScope = !isStatic && isJavaParserDeclaration;

            //If we call a method on a custom instance, the scope will the first parameter of that method.
            //  Otherwise it will come first as usual.
            if (!switchScope) {
                n.getScope().ifPresent(s -> {
                    //don't parse the scope for static methods
                    if (!isStatic)
                        s.accept(this, arg);
                });
            }

            //TODO: type arguments
            pushNode(new JTJMethodCall(this.currentNode, program, resolvedMethod));

            if (switchScope) {
                //instance methods get the scope as the first param, which should be the instance
                n.getScope().ifPresent(s -> s.accept(this, arg));
            }

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
                    currentNode,
                    variable.getScope().getScopeType().getMapName(),
                    variable.getNewName()));

            n.getInitializer().ifPresent(l -> l.accept(this, arg));
            popNode();
        }

        @Override
        public void visit(AssignExpr n, Object arg) {
            if (n.getTarget() instanceof NodeWithSimpleName<?>) {
                String variableName = ((NodeWithSimpleName<?>) n.getTarget()).getNameAsString();
                VariableStack.Variable variable = n.getTarget().isFieldAccessExpr() ?
                        findVariableFromFieldAccess((FieldAccessExpr) n.getTarget()) :
                        variableStack.findVariable(variableName);

                JTJVariableAssign jtjVariableAssign = new JTJVariableAssign(currentNode, variable);

                //if we're not accessing a field of the current instance -> actually parse the scope
                if (n.getTarget().isFieldAccessExpr() && !n.getTarget().asFieldAccessExpr().getScope().isThisExpr()) {
                    pushNode(new JTJEmpty(null));
                    n.getTarget().asFieldAccessExpr().getScope().accept(this, arg);
                    jtjVariableAssign.setScope(currentNode.asString());
                    popNode();
                }

                pushNode(jtjVariableAssign);
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

            if (variable != null) {
                currentNode.addChild(new JTJVariableAccess(currentNode, variable, program));
            } else {
                currentNode.addChild(new JTJString(currentNode, n.calculateResolvedType().describe()));
            }
        }

        @Override
        public void visit(FieldAccessExpr n, Object arg) {
            ResolvedValueDeclaration resolvedValueDeclaration = n.resolve();
            //just append the field name for all other fields
            if (!(resolvedValueDeclaration instanceof JavaParserFieldDeclaration)) {
                n.getScope().accept(this, arg);
                currentNode.addChild(new JTJString(currentNode, "."));
                currentNode.addChild(new JTJString(currentNode, n.getNameAsString()));
                return;
            }

            String declaringType = ASTUtils.resolveFullName(
                    ((JavaParserClassDeclaration) ((JavaParserFieldDeclaration) resolvedValueDeclaration).declaringType()).getWrappedNode()
            );

            JTJClass jtjClass = program.findClass(declaringType);

            if (jtjClass != null) {
                VariableStack.Variable instanceVar = variableStack.findScope(VariableStack.ScopeType.THIS_INSTANCE).getVariable("this");
                JTJField field = jtjClass.findField(n.getNameAsString());
                if (field == null)
                    throw new IllegalStateException();

                //TODO: fields of custom types need a cast to Map instead
                currentNode.addChild(new JTJString(currentNode, "((%s)".formatted(field.getVariable().getType())));

                //ignore "this" keyword
                if (!n.getScope().isThisExpr())
                    n.getScope().accept(this, arg);
                else
                    currentNode.addChild(new JTJVariableAccess(currentNode, instanceVar, program));

                currentNode.addChild(new JTJString(currentNode, ".get(%d))".formatted(field.getVariable().getNewName())));
            }
        }

        @Override
        public void visit(FieldDeclaration n, Object arg) {
            String declaringType = ASTUtils.resolveFullName(
                    ((JavaParserClassDeclaration) n.resolve().declaringType()).getWrappedNode()
            );

            JTJClass jtjClass = program.findClass(declaringType);

            for (VariableDeclarator variable : n.getVariables()) {
                pushNode(jtjClass.findField(variable.getNameAsString()));
                variable.getInitializer().ifPresentOrElse(
                        l -> l.accept(this, arg),
                        () -> {
                            currentNode.addChild(new JTJString(currentNode,
                                    switch (variable.getTypeAsString()) {
                                        case "byte", "short", "int", "char", "float", "double", "long" -> "0";
                                        case "boolean" -> "false";
                                        default -> "null";
                                    }
                            ));
                        }
                );
                popNode();
            }
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
            String operator = n.getOperator().asString();
            //i++ -> i + 1
            operator = operator.length() > 1 ? operator.substring(1) + "1" : operator;

            if (n.getExpression() instanceof NodeWithSimpleName<?>) {
                String variableName = ((NodeWithSimpleName<?>) n.getExpression()).getNameAsString();
                VariableStack.Variable variable = n.getExpression().isFieldAccessExpr() ?
                        findVariableFromFieldAccess((FieldAccessExpr) n.getExpression()) :
                        variableStack.findVariable(variableName);

                JTJVariableAssign jtjVariableAssign = new JTJVariableAssign(currentNode, variable, n.isPostfix());
                JTJVariableAccess jtjVariableAccess = new JTJVariableAccess(currentNode, variable, program);

                //if we're not accessing a field of the current instance -> actually parse the scope
                if (n.getExpression().isFieldAccessExpr() && !n.getExpression().asFieldAccessExpr().getScope().isThisExpr()) {
                    pushNode(new JTJEmpty(null));
                    n.getExpression().asFieldAccessExpr().getScope().accept(this, arg);

                    String variableScope = currentNode.asString();
                    jtjVariableAssign.setScope(variableScope);
                    jtjVariableAccess.setScope(variableScope);
                    popNode();
                }

                pushNode(jtjVariableAssign);
                currentNode.addChild(jtjVariableAccess);
                currentNode.addChild(new JTJString(currentNode, operator));
                popNode();
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
            pushNode(new JTJObjectCreation(currentNode, program, n.resolve()));
            n.getArguments().forEach(p -> {
                pushNode(new JTJEmpty(currentNode));
                p.accept(this, arg);
                popNode();
            });
            popNode();
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

        private VariableStack.Variable findVariableFromFieldAccess(FieldAccessExpr expr) {
            ResolvedValueDeclaration declaration = expr.resolve();

            if (!(declaration instanceof JavaParserFieldDeclaration))
                return null;

            JTJClass jtjClass = program.findClass(((JavaParserFieldDeclaration) declaration).declaringType().getQualifiedName());

            if (jtjClass == null)
                throw new IllegalStateException();

            JTJField jtjField = jtjClass.findField(expr.getNameAsString());
            if (jtjField == null)
                throw new IllegalStateException();

            return jtjField.getVariable();
        }

        private void pushNode(JTJChildrenNode node) {
            currentNode = node;
        }

        private void popNode() {
            JTJChildrenNode parent = currentNode.getParent();
            if (parent == null) {
                currentNode = null;
                return;
            }

            parent.addChild(currentNode);
            currentNode = parent;
        }
    }
}
