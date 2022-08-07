package com.example;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VMByteCodeGenerator extends SyntaxTree.Generator {
    private void copyArrays(Object[] destination, Object[]... toCopy) {
        int loc = 0;
        for (Object[] objects : toCopy) {
            System.arraycopy(objects, 0, destination, loc, objects.length);
            loc += objects.length;
        }
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
        return new Object[] { VMWrapper.GETVAR, variable.getVariableName() };
    }

    @Override
    public Object generateSetVariable(SyntaxTree.SetVariable setVariable) {
        Object[] data = (Object[]) setVariable.getValue().evaluate(this);
        Object[] res = new Object[data.length + 2];
        System.arraycopy(data, 0, res, 0, data.length);
        res[data.length] = VMWrapper.SETVAR;
        res[data.length + 1] = setVariable.getVariableName();
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
}