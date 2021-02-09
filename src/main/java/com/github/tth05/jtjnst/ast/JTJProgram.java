package com.github.tth05.jtjnst.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JTJProgram extends JTJNode {

    private static final String MAIN_METHOD_RUN_STMT = "((BiConsumer<List<Object>, List<Object>>)global.get(0)).accept(Arrays.asList(__args), new ArrayList<>())";
    private static final String PROGRAM_START = """
            public class Main {
                public static void main(String[] __args) {
                    if(((Function<HashMap<Integer, Object>, Boolean>)((global)->Stream.<Runnable>of(() ->
            """;

    private static final String PROGRAM_END = """
                        ).peek(Runnable::run).findFirst() == null)).apply(new HashMap<>())) {}
                    }
                }
            """;

    private final List<JTJMethod> methodList = new ArrayList<>();

    public JTJProgram() {
        super(null);
    }

    public void addMethod(JTJMethod method) {
        this.methodList.add(method);
    }

    public List<JTJMethod> getMethodList() {
        return Collections.unmodifiableList(methodList);
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(PROGRAM_START);

        JTJBlock inner = new JTJBlock(null);

        for (JTJMethod method : this.methodList) {
            inner.addChild(method);
        }

        inner.addChild(new JTJString(null, MAIN_METHOD_RUN_STMT));
        inner.appendToStr(builder);

        builder.append(PROGRAM_END);
    }
}
