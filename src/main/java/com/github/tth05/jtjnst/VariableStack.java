package com.github.tth05.jtjnst;

import com.github.tth05.jtjnst.ast.JTJObjectCreation;

import java.util.*;

public class VariableStack {

    private final Deque<Scope> stack = new ArrayDeque<>();

    public void push(ScopeType type) {
        stack.push(new Scope(type));
    }

    public void pop() {
        stack.pop();
    }

    public void addVariable(Variable variable) {
        this.stack.getFirst().addVariable(variable);
    }

    public void addVariable(String name, String type) {
        stack.getFirst().addVariable(name, type);
    }

    public Scope findScope(ScopeType type) {
        for (Scope scope : stack) {
            if (scope.getScopeType() == type)
                return scope;
        }

        return null;
    }

    public Variable findVariable(String oldName) {
        for (Scope scope : stack) {
            Variable variable = scope.getVariable(oldName);
            if (variable != null)
                return variable;
        }

        return null;
    }

    public static class Variable {
        private final Scope scope;
        private final String oldName;
        private final int newName;
        private final String type;

        public Variable(Scope scope, String oldName, int newName, String type) {
            this.scope = scope;
            this.oldName = oldName;
            this.newName = newName;
            this.type = type;
        }

        public int getNewName() {
            return newName;
        }

        public Scope getScope() {
            return scope;
        }

        public String getOldName() {
            return oldName;
        }

        public String getType() {
            return type;
        }
    }

    public static class Scope {

        private final ScopeType scopeType;
        //old name -> new name
        private final Map<String, Variable> variableMap = new HashMap<>();

        public Scope(ScopeType scopeType) {
            this.scopeType = scopeType;
        }

        public void addVariable(String name, String type) {
            int newName = scopeType == ScopeType.PARAM ? variableMap.size() + 1 :
                    scopeType == ScopeType.THIS_INSTANCE ? 0 :
                            JTJNSTranspiler.uniqueID();

            variableMap.put(name, new Variable(this, name, newName, type));
        }

        public void addVariable(Variable variable) {
            variableMap.put(variable.getOldName(), variable);
        }

        public Variable getVariable(String oldName) {
            return variableMap.get(oldName);
        }

        public ScopeType getScopeType() {
            return scopeType;
        }

        public Map<String, Variable> getVariableMap() {
            return Collections.unmodifiableMap(variableMap);
        }
    }

    public enum ScopeType {
        GLOBAL("global"),
        INSTANCE_FIELDS("((%s)args.get(0))".formatted(JTJObjectCreation.TYPE_CAST)),
        THIS_INSTANCE("args"),
        PARAM("args"),
        LOCAL("local"),
        FOR_LOOP("local");

        private final String mapName;

        ScopeType(String mapName) {
            this.mapName = mapName;
        }

        public String getMapName() {
            return mapName;
        }
    }
}
