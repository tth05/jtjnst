package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.JTJNSTranspiler;

import java.util.List;

public class JTJMethod extends JTJChildrenNode {

    public static final String METHOD_START = """
            global.put(%d, (BiConsumer<List<Object>, List<Object>>)(
                (args, retPtr)->
                    ((Consumer<HashMap<Integer, Object>>)(local ->
            """;

    public static final String METHOD_END = """
                    )).accept(new HashMap<>())
                )
            )
            """;

    private final JTJBlock body = new JTJBlock(this);

    private final int id;
    private final String signature;

    public JTJMethod(String signature) {
        this(signature, false);
    }

    public JTJMethod(String signature, boolean mainMethod) {
        super(null);
        this.signature = signature;
        this.id = mainMethod ? 0 : JTJNSTranspiler.uniqueID();
    }

    @Override
    public void addChild(JTJNode node) {
        this.body.addChild(node);
    }

    @Override
    public List<JTJNode> getChildren() {
        return this.body.getChildren();
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(METHOD_START.formatted(this.id));
        this.body.appendToStr(builder);
        builder.append(METHOD_END);
    }

    public int getId() {
        return id;
    }

    public String getSignature() {
        return this.signature;
    }
}
