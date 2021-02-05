package com.github.tth05.jtjnst.ast;

import java.util.List;

public class JTJMethod extends JTJChildrenNode {

    public static final String METHOD_START = """
            global.put(0, (BiConsumer<List<Object>, List<Object>>)(
                (args, retPtr)->
                    ((Consumer<HashMap<Integer, Object>>)(local ->
            """;

    public static final String METHOD_END = """
                    )).accept(new HashMap<>())
                )
            )
            """;

    private final JTJBlock body = new JTJBlock();

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
        builder.append(METHOD_START);
        this.body.appendToStr(builder);
        builder.append(METHOD_END);
    }
}
