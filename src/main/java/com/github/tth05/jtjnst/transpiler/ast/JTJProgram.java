package com.github.tth05.jtjnst.transpiler.ast;

import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJNode;

import java.util.HashMap;
import java.util.Map;

public class JTJProgram extends JTJNode {

    public static final int MAIN_METHOD_INDEX = 0;
    public static final int UNSAFE_INDEX = 1;
    public static final int CALL_METHOD_USING_REFLECTION_METHOD_INDEX = 2;
    public static final int CALL_CONSTRUCTOR_USING_REFLECTION_METHOD_INDEX = 3;

    public static final String ACCESS_UNSAFE_INSTANCE = "((sun.misc.Unsafe)global.get(%d))".formatted(UNSAFE_INDEX);
    public static final String CALL_REFLECTION_METHOD = "java.util.Optional.ofNullable(" +
                                                        JTJMethodCall.METHOD_CALL_START_WITH_RETURN +
                                                        JTJMethodCall.METHOD_CALL_START.formatted(CALL_METHOD_USING_REFLECTION_METHOD_INDEX) +
                                                        "0, %s, \"%s\", %s, new Class[] {%s}, new Object[] {%s}" +
                                                        JTJMethodCall.METHOD_CALL_END_WITH_RETURN + ").orElse(null)";
    public static final String CALL_REFLECTION_CONSTRUCTOR = "java.util.Optional.ofNullable(" +
                                                        JTJMethodCall.METHOD_CALL_START_WITH_RETURN +
                                                        JTJMethodCall.METHOD_CALL_START.formatted(CALL_CONSTRUCTOR_USING_REFLECTION_METHOD_INDEX) +
                                                        "0, %s, new Class[] {%s}, new Object[] {%s}" +
                                                        JTJMethodCall.METHOD_CALL_END_WITH_RETURN + ").orElseThrow()";

    private static final String GET_UNSAFE_INSTANCE = """
            java.util.List.<Runnable>of(\
            () -> {try {if (global.put(%d, sun.misc.Unsafe.class.getDeclaredField("theUnsafe")) != null) {}} catch (NoSuchFieldException e) {}},\
            () -> ((java.lang.reflect.Field) global.get(%d)).setAccessible(true),\
            () -> {try {if(global.put(%d, ((java.lang.reflect.Field)global.get(%d)).get(null)) != null){}} catch (IllegalAccessException e) {}}\
            ).forEach(Runnable::run)\
            """;

    // public Object callReflectionMethod(Object target, String methodName, Class clazz, Class[] paramTypes, Object[] params)
    private static final String CALL_METHOD_USING_REFLECTION_METHOD = """
            {try {\
            if(local.put(0, ((Class)args.get(3)).getDeclaredMethod((String) args.get(2), (Class<?>[]) args.get(4))) != null){}\
            if((retPtr[0] = ((java.lang.reflect.Method)local.get(0)).invoke(args.get(1).getClass() == int.class ? null : args.get(1), ((Object[]) args.get(5)))) != null){}\
            } catch (Throwable e) {\
            if(java.util.stream.Stream.of(false).peek((I_LOVE_JTJNST) -> %s).findFirst().get()){}\
            }}\
            """.formatted(ACCESS_UNSAFE_INSTANCE + ".throwException(e.getCause())");

    // public Object callReflectionConstructor(Class clazz, Class[] paramTypes, Object[] params)
    private static final String CALL_CONSTRUCTOR_USING_REFLECTION_METHOD = """
            {try {\
            if(local.put(0, ((Class)args.get(1)).getDeclaredConstructor((Class<?>[]) args.get(2))) != null){}\
            if((retPtr[0] = ((java.lang.reflect.Constructor)local.get(0)).newInstance(((Object[]) args.get(3)))) != null){}\
            } catch (Throwable e) {\
            if(java.util.stream.Stream.of(false).peek((I_LOVE_JTJNST) -> %s).findFirst().get()){}\
            }}\
            """.formatted(ACCESS_UNSAFE_INSTANCE + ".throwException(e.getCause())");

    private static final String MAIN_METHOD_RUN_STMT = JTJMethodCall.METHOD_CALL_START.formatted(MAIN_METHOD_INDEX) +
                                                       "0, __args" + JTJMethodCall.METHOD_CALL_END;

    private static final String PROGRAM_START = """
            public class Main {\
            public static void main(String[] __args) {\
            if(((java.util.function.Function<java.util.HashMap<Integer, Object>, Boolean>)((global)->java.util.stream.Stream.<Runnable>of(() ->\
            """.strip();

    private static final String PROGRAM_END = """
            ).peek(Runnable::run).findFirst() == null)).apply(new java.util.HashMap<>())) {} \
            }\
            }\
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

    public JTJMethod findConstructor(String name) {
        for (JTJClass value : this.classMap.values()) {
            JTJMethod method = value.findConstructor(name);
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
            //TODO: create init method which initializes instance fields and returns an instance
            for (JTJMethod method : clazz.getConstructorMap()) {
                inner.addChild(method);
            }

            for (JTJMethod method : clazz.getMethodMap()) {
                inner.addChild(method);
            }
        }

        JTJMethod reflectionConstructor = new JTJMethod(null, null);
        reflectionConstructor.addChild(new JTJString(null, CALL_CONSTRUCTOR_USING_REFLECTION_METHOD));
        reflectionConstructor.setId(CALL_CONSTRUCTOR_USING_REFLECTION_METHOD_INDEX);

        JTJMethod reflectionMethod = new JTJMethod(null, null);
        reflectionMethod.addChild(new JTJString(null, CALL_METHOD_USING_REFLECTION_METHOD));
        reflectionMethod.setId(CALL_METHOD_USING_REFLECTION_METHOD_INDEX);

        inner.addChild(reflectionConstructor);
        inner.addChild(reflectionMethod);
        inner.addChild(new JTJString(null, GET_UNSAFE_INSTANCE.formatted(UNSAFE_INDEX, UNSAFE_INDEX, UNSAFE_INDEX, UNSAFE_INDEX)));
        inner.addChild(new JTJString(null, MAIN_METHOD_RUN_STMT));
        inner.appendToStr(builder);

        builder.append(PROGRAM_END);
    }
}
