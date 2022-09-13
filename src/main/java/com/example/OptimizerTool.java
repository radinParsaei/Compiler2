package com.example;

public class OptimizerTool extends Tool {
    @Override
    public void processBlock(SyntaxTree.Block block, SyntaxTree.Block parent) {
        if (block instanceof SyntaxTree.SetVariable &&
                ((SyntaxTree.SetVariable) block).getValue() instanceof SyntaxTree.Operator &&
                ((SyntaxTree.Operator) ((SyntaxTree.SetVariable) block).getValue()).getValue1()
                        instanceof SyntaxTree.Variable &&
                ((SyntaxTree.Variable) ((SyntaxTree.Operator) ((SyntaxTree.SetVariable) block).getValue()).getValue1())
                        .getVariableName().equals(((SyntaxTree.SetVariable) block).getVariableName())) {
            ((SyntaxTree.SetVariable) block).getValue().setExtraData("inplace",
                    ((SyntaxTree.SetVariable) block).getVariableName());
        }
    }

    @Override
    public void processValue(SyntaxTree.Value value, SyntaxTree.Block parent) {

    }

    @Override
    public void finalizeBlock(SyntaxTree.Block block, SyntaxTree.Block parent) {

    }

    @Override
    public void finalizeValue(SyntaxTree.Value value, SyntaxTree.Block block) {

    }
}
