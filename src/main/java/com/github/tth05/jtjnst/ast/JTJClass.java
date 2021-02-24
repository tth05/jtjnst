package com.github.tth05.jtjnst.ast;

import com.github.tth05.jtjnst.JTJNSTranspiler;
import com.github.tth05.jtjnst.ast.structure.JTJBlock;
import com.github.tth05.jtjnst.ast.structure.JTJNode;

import java.util.*;

public class JTJClass {

    private final String name;

    private final JTJMethod initMethod = new JTJClassInitMethod();

    private final Map<String, JTJMethod> constructorMap = new HashMap<>();
    private final Map<String, JTJMethod> methodMap = new HashMap<>();

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

    public String getName() {
        return name;
    }

    private String getOwnClassName() {
        int i = this.name.lastIndexOf('.');
        if (i == -1)
            return this.name;

        return this.name.substring(i + 1);
    }

    public JTJMethod getInitMethod() {
        return initMethod;
    }

    public static final class JTJClassInitMethod extends JTJMethod {

        public JTJClassInitMethod() {
            super("");
        }

        @Override
        public void appendToStr(StringBuilder builder) {
            JTJBlock newBody = new JTJBlock(null);

            //create instance
            newBody.addChild(new JTJString(null, "local.put(0, new HashMap<Integer, String>())"));
            for (JTJNode child : this.getChildren()) {
                newBody.addChild(child);
            }
            //return instance
            newBody.addChild(new JTJString(null, "retPtr[0] = local.get(0)"));

            builder.append(METHOD_START.formatted(this.getId(), JTJNSTranspiler.uniqueID()));
            newBody.appendToStr(builder);
            builder.append(METHOD_END);
        }
    }
}
