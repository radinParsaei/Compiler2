package com.example;

import java.util.*;

import static com.example.Utils.copyArrays;

public class VMByteCodeGenerator extends Generator {
    private boolean recording = false;
    private boolean generatePops = true;

    public VMByteCodeGenerator(Tool... tools) {
        super(new ScopeTool());
        addTool(tools);
    }

    public VMByteCodeGenerator(boolean generatePops, Tool... tools) {
        super(new ScopeTool());
        addTool(tools);
        this.generatePops = generatePops;
    }

    private int sizeof(Object[] code) {
        int size = code.length;
        if (recording) return size;
        for (Object i : code) {
            if (!(i instanceof Byte)) size--;
        }
        return size;
    }

    private Object[] generateOperator(SyntaxTree.Operator operator, byte opCode) {
        Object[] value1 = (Object[]) operator.getValue1().evaluate(this);
        Object[] value2 = (Object[]) operator.getValue2().evaluate(this);
        Object[] res = new Object[value1.length + value2.length + 1];
        System.arraycopy(value1, 0, res, 0, value1.length);
        System.arraycopy(value2, 0, res, value1.length, value2.length);
        res[res.length - 1] = opCode;
        return res;
    }

    @Override
    public Object generateNumber(SyntaxTree.Number number) {
        return new Object[]{VMWrapper.PUT, number.getData()};
    }

    @Override
    public Object generateText(SyntaxTree.Text text) {
        return new Object[]{VMWrapper.PUT, text + ""};
    }

    @Override
    public Object generateBoolean(SyntaxTree.Boolean bool) {
        return new Object[]{VMWrapper.PUT, bool.getData()};
    }

    @Override
    public Object generateNull() {
        return new Object[]{VMWrapper.PUT, null};
    }

    @Override
    public Object generateList(SyntaxTree.List list) {
        ArrayList<Object> res = new ArrayList<>();
        ArrayList<SyntaxTree.Value> values = (ArrayList<SyntaxTree.Value>) list.getData();
        for (SyntaxTree.Value value : values) {
            res.addAll(0, Arrays.asList((Object[]) value.evaluate(this)));
        }
        res.add(VMWrapper.CREATE_ARR);
        res.add(values.size());

        Object[] finalResult = new Object[res.size()];
        finalResult = res.toArray(finalResult);
        return finalResult;
    }

    @Override
    public Object generateMap(SyntaxTree.Map map) {
        ArrayList<Object> res = new ArrayList<>();
        HashMap<SyntaxTree.Value, SyntaxTree.Value> values = (HashMap<SyntaxTree.Value, SyntaxTree.Value>) map.getData();
        for (Map.Entry<SyntaxTree.Value, SyntaxTree.Value> value : values.entrySet()) {
            res.addAll(Arrays.asList((Object[]) value.getKey().evaluate(this)));
            res.addAll(Arrays.asList((Object[]) value.getValue().evaluate(this)));
        }
        res.add(VMWrapper.CREATE_MAP);
        res.add(values.size());

        Object[] finalResult = new Object[res.size()];
        finalResult = res.toArray(finalResult);
        return finalResult;
    }

    @Override
    public Object generateVariable(SyntaxTree.Variable variable) {
        Object variableName = variable.getExtraData("id");
        if (variableName == null) {
            variableName = variable.getVariableName();
        } else if ((int) variableName < 0) {
            // negative numbers are for function parameters
            // func f(a, b) -> access to "a" will be represented using -1 and "b" using -2
            return new Object[] { VMWrapper.GETPARAM, - ((int) variableName) };
        }
        return new Object[] { VMWrapper.GETVAR, variableName };
    }

    @Override
    public Object generateSetVariable(SyntaxTree.SetVariable setVariable) {
        Object variableName = setVariable.getExtraData("id");
        if (variableName == null) variableName = setVariable.getVariableName();
        Object[] data = (Object[]) setVariable.getValue().evaluate(this);
        Object[] res = new Object[data.length + 2];
        System.arraycopy(data, 0, res, 0, data.length);
        res[data.length] = VMWrapper.SETVAR;
        res[data.length + 1] = variableName;
        return res;
    }

    @Override
    public Object generateAdd(SyntaxTree.Add add) {
        return generateOperator(add, VMWrapper.ADD);
    }

    @Override
    public Object generateSub(SyntaxTree.Sub sub) {
        return generateOperator(sub, VMWrapper.SUB);
    }

    @Override
    public Object generateMul(SyntaxTree.Mul mul) {
        return generateOperator(mul, VMWrapper.MUL);
    }

    @Override
    public Object generateDiv(SyntaxTree.Div div) {
        return generateOperator(div, VMWrapper.DIV);
    }

    @Override
    public Object generateMod(SyntaxTree.Mod mod) {
        return generateOperator(mod, VMWrapper.MOD);
    }

    @Override
    public Object generatePow(SyntaxTree.Pow pow) {
        return generateOperator(pow, VMWrapper.POW);
    }

    @Override
    public Object generateEquals(SyntaxTree.Equals equals) {
        return generateOperator(equals, VMWrapper.EQ);
    }

    @Override
    public Object generateNotEquals(SyntaxTree.NotEquals notEquals) {
        return generateOperator(notEquals, VMWrapper.NEQ);
    }

    @Override
    public Object generateLooksEquals(SyntaxTree.LooksEquals looksEquals) {
        return generateOperator(looksEquals, VMWrapper.LEQ);
    }

    @Override
    public Object generateGreaterThan(SyntaxTree.GreaterThan greaterThan) {
        return generateOperator(greaterThan, VMWrapper.GT);
    }

    @Override
    public Object generateLesserThan(SyntaxTree.LesserThan lesserThan) {
        return generateOperator(lesserThan, VMWrapper.LT);
    }

    @Override
    public Object generateGreaterThanOrEqual(SyntaxTree.GreaterThanOrEqual greaterThanOrEqual) {
        return generateOperator(greaterThanOrEqual, VMWrapper.GE);
    }

    @Override
    public Object generateLesserThanOrEqual(SyntaxTree.LesserThanOrEqual lesserThanOrEqual) {
        return generateOperator(lesserThanOrEqual, VMWrapper.LE);
    }

    @Override
    public Object generateAnd(SyntaxTree.And and) {
        return generateOperator(and, VMWrapper.AND);
    }

    @Override
    public Object generateOr(SyntaxTree.Or or) {
        return generateOperator(or, VMWrapper.OR);
    }

    @Override
    public Object generateBitwiseAnd(SyntaxTree.BitwiseAnd bitwiseAnd) {
        return generateOperator(bitwiseAnd, VMWrapper.B_AND);
    }

    @Override
    public Object generateBitwiseOr(SyntaxTree.BitwiseOr bitwiseOr) {
        return generateOperator(bitwiseOr, VMWrapper.B_OR);
    }

    @Override
    public Object generateLeftShift(SyntaxTree.LeftShift leftShift) {
        return generateOperator(leftShift, VMWrapper.LSHIFT);
    }

    @Override
    public Object generateRightShift(SyntaxTree.RightShift rightShift) {
        return generateOperator(rightShift, VMWrapper.RSHIFT);
    }

    @Override
    public Object generateXor(SyntaxTree.Xor xor) {
        return generateOperator(xor, VMWrapper.XOR);
    }

    @Override
    public Object generateNegative(SyntaxTree.Negative negative) {
        return generateOperator(negative, VMWrapper.NEGATE);
    }

    @Override
    public Object generateNot(SyntaxTree.Not not) {
        return generateOperator(not, VMWrapper.NOT);
    }

    @Override
    public Object generateBitwiseNot(SyntaxTree.BitwiseNot bitwiseNot) {
        return generateOperator(bitwiseNot, VMWrapper.B_NOT);
    }

    @Override
    public Object generateIf(SyntaxTree.If anIf) {
        Object[] code = (Object[]) anIf.getCode().evaluate(this);
        Object[] condition = (Object[]) anIf.getCondition().evaluate(this);
        Object[] elseCode = null;
        int elseSize = 0;
        if (anIf.getElseCode() != null) {
            elseCode = (Object[]) anIf.getElseCode().evaluate(this);
            elseSize = elseCode.length + 2;
        }

        Object[] res = new Object[code.length + condition.length + 2 + elseSize];
        copyArrays(res, 0, condition, new Object[]{VMWrapper.SKIPIFN, sizeof(code)}, code);
        if (anIf.getElseCode() != null) {
            res[condition.length + 1] = ((int) res[condition.length + 1]) + 1;
            copyArrays(res, condition.length + 2 + code.length, new Object[]{VMWrapper.SKIP, sizeof(elseCode)}, elseCode);
        }
        return res;
    }

    @Override
    public Object generatePrint(SyntaxTree.Print print) {
        Object[] message = (Object[]) print.getMessage().evaluate(this);
        Object[] res = new Object[message.length + 2];
        copyArrays(res, 0, message);
        res[message.length] = VMWrapper.CALLFUNC;
        res[message.length + 1] = null;
        return res;
    }

    @Override
    public Object generateWhile(SyntaxTree.While aWhile) {
        boolean isInRecordBlock = recording;
        recording = true;
        Object[] code = (Object[]) aWhile.getCode().evaluate(this);
        Object[] condition = (Object[]) aWhile.getCondition().evaluate(this);
        if (!isInRecordBlock) recording = false;
        Object[] res;
        if (isInRecordBlock) {
            res = new Object[condition.length + code.length + 4];
            copyArrays(res, 0, new Object[] { VMWrapper.SKIP, sizeof(code) } , code, condition,
                    new Object[] { VMWrapper.SKIPIF, -(sizeof(code) + sizeof(condition) + 2) });
            for (int i = 0; i < res.length; i++) {
                if (Objects.equals(res[i], VMWrapper.CONTINUE)) {
                    res[i] = VMWrapper.SKIP;
                    res[i + 1] = res.length - i - condition.length - 4;
                } else if (Objects.equals(res[i], VMWrapper.BREAK)) {
                    res[i] = VMWrapper.SKIP;
                    res[i + 1] = res.length - i - 2;
                }
            }
        } else {
            res = new Object[condition.length + code.length + 5];
            copyArrays(res, 0, new Object[] { VMWrapper.REC }, code, new Object[] { VMWrapper.END },
                    new Object[] { VMWrapper.REC }, condition, new Object[] { VMWrapper.END, VMWrapper.WHILE });
        }
        return res;
    }

    public Object generateFree(ScopeTool.Free free) {
        Object variableName = free.getExtraData("id");
        if (variableName == null) variableName = free.getVariableName();
        return new Object[] { VMWrapper.DELVAR, variableName };
    }

    @Override
    public Object generatePop(SyntaxTree.Value value) {
        if (generatePops) {
            Object[] val = (Object[]) value.evaluateValue(this);
            Object[] res = new Object[val.length + 1];
            copyArrays(res, 0, val, new Object[] { VMWrapper.POP });
            return res;
        } else {
            return value.evaluateValue(this);
        }
    }

    @Override
    public Object generateFunc(SyntaxTree.Function function) {
        recording = true;
        Object[] code = (Object[]) function.getCode().evaluate(this);
        Object[] res = new Object[code.length + 6];
        copyArrays(res, 0, new Object[] { VMWrapper.REC }, code, new Object[] { VMWrapper.END },
                new Object[] { VMWrapper.PUT, function.getArgs().length, VMWrapper.MKFUNC, function.getFunctionName() });
        recording = false;
        return res;
    }

    @Override
    public Object generateCall(SyntaxTree.CallFunction callFunction) {
        ArrayList<Object> res = new ArrayList<>();
        SyntaxTree.Value[] values = callFunction.getArgs();
        for (SyntaxTree.Value value : values) {
            res.addAll(Arrays.asList((Object[]) value.evaluate(this)));
        }
        res.add(VMWrapper.CALLFUNC);
        res.add(callFunction.getFunctionName());

        Object[] finalResult = new Object[res.size()];
        finalResult = res.toArray(finalResult);
        return finalResult;
    }

    @Override
    public Object generateReturn(SyntaxTree.Return aReturn) {
        Object[] value = (Object[]) aReturn.getValue().evaluate(this);
        Object[] res = new Object[value.length + 1];
        copyArrays(res, 0, value, new Byte[] { VMWrapper.RETURN });
        return res;
    }

    @Override
    public Object generateContinue(SyntaxTree.Continue aContinue) {
        if (recording) return new Object[] { VMWrapper.CONTINUE, null }; // will be replaced with SKIP <N>
        return new Object[] { VMWrapper.CONTINUE };
    }

    @Override
    public Object generateBreak(SyntaxTree.Break aBreak) {
        if (recording) return new Object[] { VMWrapper.BREAK, null }; // will be replaced with SKIP <N>
        return new Object[] { VMWrapper.BREAK };
    }
}