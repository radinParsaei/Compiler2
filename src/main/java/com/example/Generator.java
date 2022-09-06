package com.example;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Each backend needs to extend this class and override its methods to generate output(e.g. JVM class, source code, llvm IR...)
 */
public abstract class Generator {
    private final ArrayList<Tool> tools = new ArrayList<>();

    public abstract Object generateNumber(SyntaxTree.Number n);
    public abstract Object generateText(SyntaxTree.Text n);
    public abstract Object generateBoolean(SyntaxTree.Boolean bool);
    public abstract Object generateNull();
    public abstract Object generateList(SyntaxTree.List list);
    public abstract Object generateMap(SyntaxTree.Map map);
    public abstract Object generateVariable(SyntaxTree.Variable variable);
    public abstract Object generateSetVariable(SyntaxTree.SetVariable setVariable);
    public abstract Object generateAdd(SyntaxTree.Add add);
    public abstract Object generateSub(SyntaxTree.Sub sub);
    public abstract Object generateMul(SyntaxTree.Mul mul);
    public abstract Object generateDiv(SyntaxTree.Div div);
    public abstract Object generateMod(SyntaxTree.Mod mod);
    public abstract Object generatePow(SyntaxTree.Pow pow);
    public abstract Object generateEquals(SyntaxTree.Equals equals);
    public abstract Object generateNotEquals(SyntaxTree.NotEquals notEquals);
    public abstract Object generateLooksEquals(SyntaxTree.LooksEquals looksEquals);
    public abstract Object generateGreaterThan(SyntaxTree.GreaterThan greaterThan);
    public abstract Object generateLesserThan(SyntaxTree.LesserThan lesserThan);
    public abstract Object generateGreaterThanOrEqual(SyntaxTree.GreaterThanOrEqual greaterThanOrEqual);
    public abstract Object generateLesserThanOrEqual(SyntaxTree.LesserThanOrEqual lesserThanOrEqual);
    public abstract Object generateAnd(SyntaxTree.And and);
    public abstract Object generateOr(SyntaxTree.Or or);
    public abstract Object generateBitwiseAnd(SyntaxTree.BitwiseAnd bitwiseAnd);
    public abstract Object generateBitwiseOr(SyntaxTree.BitwiseOr bitwiseOr);
    public abstract Object generateLeftShift(SyntaxTree.LeftShift leftShift);
    public abstract Object generateRightShift(SyntaxTree.RightShift rightShift);
    public abstract Object generateXor(SyntaxTree.Xor xor);
    public abstract Object generateNegative(SyntaxTree.Negative negative);
    public abstract Object generateNot(SyntaxTree.Not not);
    public abstract Object generateBitwiseNot(SyntaxTree.BitwiseNot bitwiseNot);
    public abstract Object generateIf(SyntaxTree.If anIf);
    public abstract Object generatePrint(SyntaxTree.Print print);
    public abstract Object generateWhile(SyntaxTree.While aWhile);
    public abstract Object generateFree(ScopeTool.Free free);
    public abstract Object generatePop(SyntaxTree.Value value);
    public abstract Object generateFunc(SyntaxTree.Function function);
    public abstract Object generateCall(SyntaxTree.CallFunction callFunction);
    public abstract Object generateReturn(SyntaxTree.Return aReturn);
    public abstract Object generateContinue(SyntaxTree.Continue aContinue);
    public abstract Object generateBreak(SyntaxTree.Break aBreak);


    public void addTool(Tool... tool) {
        tools.addAll(Arrays.asList(tool));
    }

    public Generator(Tool... tools) {
        for (Tool tool : tools)
            addTool(tool);
    }

    private void runTools(SyntaxTree.Block block) {
        for (Tool tool : tools) {
            if (block.getExtraData("parent") == null && block instanceof SyntaxTree.Value) {
                block.setExtraData("unneededResult", true); // result of this block is unneeded and should be popped out from the stack
                tool.processValue((SyntaxTree.Value) block, null);
            }
            if (block.getCodeBlocks() != null) {
                for (SyntaxTree.Block block1 : block.getCodeBlocks()) {
                    block1.setExtraData("parent", block);
                    if (block1 instanceof SyntaxTree.Value) { // in case there was a value inside the list of blocks (such as function calls)
                        block1.setExtraData("unneededResult", true); // result of this block is unneeded and should be popped out from the stack
                        tool.processValue((SyntaxTree.Value) block1, block);
                        runTools(block1);
                        tool.finalizeValue((SyntaxTree.Value) block1, block);
                    } else {
                        tool.processBlock(block1, block);
                        runTools(block1);
                        tool.finalizeBlock(block1, block);
                    }
                }
            }
            if (block.getValues() != null) {
                for (SyntaxTree.Value value : block.getValues()) {
                    value.setExtraData("parent", block);
                    tool.processValue(value, block);
                    runTools(value);
                    tool.finalizeValue(value, block);
                }
            }
            if (block.getExtraData("parent") == null && block instanceof SyntaxTree.Value) {
                tool.finalizeValue((SyntaxTree.Value) block, null);
            }
        }
    }

    public Object generate(SyntaxTree.Block block) {
        runTools(block);
        return block.evaluate(this);
    }
}