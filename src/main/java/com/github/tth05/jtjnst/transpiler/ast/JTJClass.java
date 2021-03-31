package com.github.tth05.jtjnst.transpiler.ast;

import com.github.tth05.jtjnst.transpiler.JTJNSTranspiler;
import com.github.tth05.jtjnst.transpiler.VariableStack;
import com.github.tth05.jtjnst.transpiler.ast.structure.JTJBlock;

import java.util.*;

public class JTJClass {

    public static final VariableStack.Scope DUMMY_SCOPE = new VariableStack.Scope(VariableStack.ScopeType.INSTANCE_FIELDS);

    private final String name;

    private final JTJMethod initMethod = new JTJClassInitMethod();

    private final Map<String, JTJMethod> constructorMap = new HashMap<>();
    private final Map<String, JTJMethod> methodMap = new HashMap<>();
    private final Map<String, JTJField> fieldMap = new HashMap<>();

    public JTJClass(String name) {
        this.name = name;
    }

    public void addConstructor(JTJMethod method) {
        this.constructorMap.put(method.getSignature(), method);
    }

    public JTJMethod findConstructor(String signature) {
        return this.constructorMap.get(signature);
    }

    public Collection<JTJMethod> getConstructorMap() {
        return Collections.unmodifiableCollection(constructorMap.values());
    }

    public void addMethod(JTJMethod method) {
        this.methodMap.put(method.getSignature(), method);
    }

    public JTJMethod findMethod(String signature) {
        return this.methodMap.get(signature);
    }

    public Collection<JTJMethod> getMethodMap() {
        Collection<JTJMethod> values = new ArrayList<>(this.methodMap.values());
        values.add(this.initMethod);
        return Collections.unmodifiableCollection(values);
    }

    public void addField(VariableStack.Variable field) {
        this.fieldMap.put(field.getOldName(), new JTJField(null, field));
    }

    public Map<String, JTJField> getFieldMap() {
        return this.fieldMap;
    }

    public JTJField findField(String name) {
        return this.fieldMap.get(name);
    }

    public String getName() {
        return name;
    }

    public JTJMethod getInitMethod() {
        return initMethod;
    }

    public final class JTJClassInitMethod extends JTJMethod {

        public JTJClassInitMethod() {
            super(JTJClass.this, "");
        }

        @Override
        public void appendToStr(StringBuilder builder) {
            JTJBlock newBody = new JTJBlock(null);

            for (JTJField field : fieldMap.values()) {
                newBody.addChild(field);
            }

            //return instance
            newBody.addChild(new JTJString(null, "retPtr[0] = args.get(0)"));

            builder.append(String.format(METHOD_START, this.getId(), JTJNSTranspiler.uniqueID()));
            newBody.appendToStr(builder);
            builder.append(METHOD_END);
        }
    }
}
