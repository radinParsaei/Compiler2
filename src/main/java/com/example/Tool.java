package com.example;

public abstract class Tool {
    public abstract void processBlock(SyntaxTree.Block block, SyntaxTree.Block parent);

    public abstract void processValue(SyntaxTree.Value value, SyntaxTree.Block parent);

    public abstract void finalizeBlock(SyntaxTree.Block block, SyntaxTree.Block parent);

    public abstract void finalizeValue(SyntaxTree.Value value, SyntaxTree.Block block);
}
