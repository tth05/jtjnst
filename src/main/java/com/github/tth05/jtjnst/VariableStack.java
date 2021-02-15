package com.github.tth05.jtjnst;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class VariableStack {

    private final Deque<Scope> stack = new LinkedList<>();

    public void push(ScopeType type) {
        stack.push(new Scope(type));
    }

    public void pop() {
        stack.pop();
    }

    public void addVariable(String name, String type) {
        stack.getFirst().addVariable(name, type);
    }

    public Variable findVariable(String oldName) {
        for (Scope scope : stack) {
            Variable variable = scope.getVariable(oldName);
            if (variable != null)
                return variable;
        }

        return null;
    }

    public class Variable {
        private final Scope scope;
        private final String oldName;
        private final int newName;
        private final String type;

        Variable(Scope scope, String oldName, int newName, String type) {
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

    public class Scope {

        private final ScopeType scopeType;
        //old name -> new name
        private final Map<String, Variable> variableMap = new HashMap<>();

        Scope(ScopeType scopeType) {
            this.scopeType = scopeType;
        }

        public void addVariable(String name, String type) {
            variableMap.put(name, new Variable(this, name,
                    scopeType == ScopeType.PARAM ? variableMap.size() : JTJNSTranspiler.uniqueID(), type));
        }

        public Variable getVariable(String oldName) {
            return variableMap.get(oldName);
        }

        public ScopeType getScopeType() {
            return scopeType;
        }
    }

    public enum ScopeType {
        GLOBAL("global"),
        OBJECT("--"),
        PARAM("args"),
        LOCAL("local"),
        OTHER("--");

        private final String mapName;

        ScopeType(String mapName) {
            this.mapName = mapName;
        }

        public String getMapName() {
            return mapName;
        }
    }
}
