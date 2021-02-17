package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.ast.structure.JTJNode;

import java.util.HashMap;
import java.util.Map;

public class JTJProgram extends JTJNode {

    public static final int UNSAFE_INDEX = 1;
    public static final String ACCESS_UNSAFE_INSTANCE = "((sun.misc.Unsafe)global.get(%d))".formatted(UNSAFE_INDEX);

    private static final String GET_UNSAFE_INSTANCE = """
            Arrays.<Runnable>asList(
                () -> {try {if (global.put(%d, sun.misc.Unsafe.class.getDeclaredField("theUnsafe")) != null) {}} catch (NoSuchFieldException e) {}},
                () -> ((java.lang.reflect.Field) global.get(%d)).setAccessible(true),
                () -> {try {if(global.put(%d, ((java.lang.reflect.Field)global.get(%d)).get(null)) != null){}} catch (IllegalAccessException e) {}}
            ).forEach(Runnable::run)
            """;
    private static final String MAIN_METHOD_RUN_STMT = "((BiFunction<List<Object>, Object[], Object[]>)global.get(0)).apply(Arrays.asList(__args), new Object[0])";
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

    private final Map<String, JTJClass> classMap = new HashMap<>();

    public JTJProgram() {
        super(null);
    }

    public void addClass(JTJClass clazz) {
        this.classMap.put(clazz.getName(), clazz);
    }

    public JTJClass findClass(String name) {
        return this.classMap.get(name);
    }

    public JTJMethod findMethod(String name) {
        for (JTJClass value : this.classMap.values()) {
            JTJMethod method = value.findMethod(name);
            if (method != null)
                return method;
        }

        return null;
    }

    @Override
    public void appendToStr(StringBuilder builder) {
        builder.append(PROGRAM_START);

        JTJBlock inner = new JTJBlock(null);

        for (JTJClass clazz : classMap.values()) {
            for (JTJMethod method : clazz.getMethodMap()) {
                inner.addChild(method);
            }
        }

        inner.addChild(new JTJString(null, GET_UNSAFE_INSTANCE.formatted(UNSAFE_INDEX, UNSAFE_INDEX, UNSAFE_INDEX, UNSAFE_INDEX)));
        inner.addChild(new JTJString(null, MAIN_METHOD_RUN_STMT));
        inner.appendToStr(builder);

        builder.append(PROGRAM_END);
    }
}
