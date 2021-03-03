package com.github.tth05.jtjnst.transpiler.ast;

import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;
import com.github.tth05.jtjnst.transpiler.ast.exception.JTJConditionalTryCatchStatement;
import com.github.tth05.jtjnst.transpiler.ast.exception.JTJTryCatchStatement;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJChildrenNode;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

import java.util.ArrayList;
import java.util.List;

public class JTJMethod extends JTJChildrenNode {

    public static final String METHOD_START = """
            global.put(%d, (java.util.function.BiFunction<java.util.List<Object>, Object[], Object[]>)( \
                (args, retPtr)-> \
                    (Object[]) java.util.stream.Stream.of((Object)retPtr).peek((jtjLambda%d) -> ((java.util.function.Consumer<java.util.HashMap<Integer, Object>>) (local -> \
            """;

    public static final String METHOD_END = """
                    )).accept(new java.util.HashMap<>())).findFirst().get() \
                ) \
            ) \
            """;

    private final JTJBlock body = new JTJBlock(this);

    private final List<Integer> returnIds = new ArrayList<>();

    private int id;
    private final String signature;
    private final JTJClass jtjClass;

    public JTJMethod(JTJClass jtjClass, String signature) {
        this(jtjClass, signature, false);
    }

    public JTJMethod(JTJClass jtjClass, String signature, boolean mainMethod) {
        super(null);
        this.signature = signature;
        this.id = mainMethod ? 0 : JTJNSTranspiler.uniqueID();
        this.jtjClass = jtjClass;
    }

    void setId(int id) {
        this.id = id;
    }

    @Override
    public void addChild(JTJNode node) {
        this.body.addChild(node);
    }

    public void addReturnStatementId(int id) {
        this.returnIds.add(id);
    }

    @Override
    public List<JTJNode> getChildren() {
        return this.body.getChildren();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(METHOD_START.formatted(this.id, JTJNSTranspiler.uniqueID()));

        if (!this.returnIds.isEmpty()) {
            JTJTryCatchStatement tryCatchStatement = new JTJConditionalTryCatchStatement(this, returnIds);

            for (JTJNode child : this.body.getChildren()) {
                tryCatchStatement.getTryBlock().addChild(child);
            }

            tryCatchStatement.appendToStr(builder);
        } else {
            this.body.appendToStr(builder);
        }

        builder.append(METHOD_END);
    }

    public int getId() {
        return id;
    }

    public String getSignature() {
        return this.signature;
    }

    public JTJClass getContainingClass() {
        return this.jtjClass;
    }
}
