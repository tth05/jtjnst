package com.github.tth05.jtjnst.ast;

import java.util.*;

public class JTJClass {

    private final String name;

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
        return Collections.unmodifiableCollection(methodMap.values());
    }

    public String getName() {
        return name;
    }
}
