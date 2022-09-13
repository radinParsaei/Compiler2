package com.example;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Tool designed to manage scopes.
 */
public class ScopeTool extends Tool {

    private int idCounter;
    private final HashSet<Integer> ids = new HashSet<>();
    private final HashMap<SyntaxTree.Block, HashMap<String, Integer>> info = new HashMap<>();
    private final HashSet<String> globals = new HashSet<>();

    /**
     * returns an unoccupied ID to use instead of the variable's name (where appropriate).
     */
    private int nextId() {
        if (ids.isEmpty()) {
            return idCounter++;
        }
        int id = ids.stream().min(Integer::compare).get();
        ids.remove(id);
        return id;
    }

    /**
     * free local variables at the end of the scope (if needed).
     * (In VMByteCodeGenerator, the DELVAR opcode is used to remove unneeded variables.)
     */
    public static class Free extends SyntaxTree.Block {
        private String variableName;

        public Free(String variableName) {
            this.variableName = variableName;
        }

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateFree(this);
        }
    }

    @Override
    public void processBlock(SyntaxTree.Block block, SyntaxTree.Block parent) {
        SyntaxTree.Block initialParent = parent; // expected to be an instance of Blocks (in case the change needs to be performed)
        SyntaxTree.Block pParent = null;
        while (parent != null && parent.getExtraData("locals") == null) {
            pParent = parent;
            parent = (SyntaxTree.Block) parent.getExtraData("parent");
        }
        if (block instanceof SyntaxTree.SetVariable) {
            // TODO: check type of the instance and check if the property exists
            if (((SyntaxTree.SetVariable) block).getInstance() != null) return;
            if (parent == null && ((SyntaxTree.SetVariable) block).isDeclaration()) {
                globals.add(((SyntaxTree.SetVariable) block).getVariableName());
                return;
            }
            if (!((SyntaxTree.SetVariable) block).isDeclaration() && (!info.containsKey(pParent) ||
                    !info.get(pParent).containsKey(((SyntaxTree.SetVariable) block).getVariableName()))) {
                if (globals.contains(((SyntaxTree.SetVariable) block).getVariableName())) return; // don't make any changes
                Errors.modifiedUndefinedVariable(((SyntaxTree.SetVariable) block).getVariableName(), block);
            }
            int id;
            if (((SyntaxTree.SetVariable) block).isDeclaration()) {
                id = nextId();
                if (!info.containsKey(pParent)) info.put(pParent, new HashMap<>());
                if (initialParent instanceof SyntaxTree.Blocks) {
                    Free free = new Free(((SyntaxTree.SetVariable) block).getVariableName());
                    free.setExtraData("id", id);
                    ((SyntaxTree.Blocks) initialParent).addCodeBlock(free);
                }
            } else {
                id = info.get(pParent).get(((SyntaxTree.SetVariable) block).getVariableName());
            }
            block.setExtraData("id", id);
            info.get(pParent).put(((SyntaxTree.SetVariable) block).getVariableName(), id);
        } else if (block instanceof SyntaxTree.Blocks) {
            if (!info.containsKey(block)) info.put(block, new HashMap<>());
            if (parent != null) {
                int i = 0;
                if (parent.getExtraData("args") != null) {
                    for (String arg : (String[]) parent.getExtraData("args")) {
                        info.get(block).put(arg, --i);
                    }
                }
            }
            while (parent != null) {
                if (parent.getExtraData("locals") != null) {
                    if (info.containsKey(pParent))
                        info.get(block).putAll(info.get(pParent));
                }
                pParent = parent;
                parent = (SyntaxTree.Block) parent.getExtraData("parent");
            }
        }
    }

    @Override
    public void processValue(SyntaxTree.Value value, SyntaxTree.Block parent) {
        if (value instanceof SyntaxTree.Variable) {
            // TODO: check type of the instance and check if the property exists
            if (((SyntaxTree.Variable) value).getInstance() != null) return;
            SyntaxTree.Block pParent = null;
            while (parent != null && parent.getExtraData("locals") == null) {
                pParent = parent;
                parent = (SyntaxTree.Block) parent.getExtraData("parent");
            }
            if (parent != null && parent.getExtraData("condition") != null &&
                    (pParent instanceof SyntaxTree.Value || pParent == null)) {
                do {
                    pParent = parent;
                    parent = (SyntaxTree.Block) parent.getExtraData("parent");
                } while (parent != null && parent.getExtraData("locals") == null);
            }
            boolean error = true;
            if (info.containsKey(pParent) && info.get(pParent).containsKey(((SyntaxTree.Variable) value).getVariableName())) {
                value.setExtraData("id", info.get(pParent).get(((SyntaxTree.Variable) value).getVariableName()));
                error = false;
            } else if (globals.contains(((SyntaxTree.Variable) value).getVariableName())) {
                error = false;
            }
            if (error) {
                Errors.accessedUndefinedVariable(((SyntaxTree.Variable) value).getVariableName(), value);
            }
        }
    }

    @Override
    public void finalizeBlock(SyntaxTree.Block block, SyntaxTree.Block parent) {
        if (info.containsKey(block)) {
            // Mark the freed block's ID(s) as accessible
            ids.addAll(info.get(block).values());
            info.remove(block);
        }
    }

    @Override
    public void finalizeValue(SyntaxTree.Value value, SyntaxTree.Block block) {}
}
