package com.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Definition of code blocks shared between frontend and backend
 */
public class SyntaxTree {
    /**
     * Each backend needs to extend this class and override its methods to generate output(e.g. JVM class, source code, llvm IR...)
     */
    public static abstract class Generator {
        public abstract Object generateNumber(Number n);
        public abstract Object generateText(Text n);
        public abstract Object generateBoolean(Boolean bool);
        public abstract Object generateNull();
        public abstract Object generateList(List list);
        public abstract Object generateMap(Map map);
        public abstract Object generateVariable(Variable variable);
        public abstract Object generateSetVariable(SetVariable setVariable);
        public abstract Object generateAdd(Add add);
        public abstract Object generateSub(Sub sub);
        public abstract Object generateMul(Mul mul);
        public abstract Object generateDiv(Div div);
        public abstract Object generateMod(Mod mod);
        public abstract Object generatePow(Pow pow);
        public abstract Object generateEquals(Equals equals);
        public abstract Object generateNotEquals(NotEquals notEquals);
        public abstract Object generateLooksEquals(LooksEquals looksEquals);
        public abstract Object generateGreaterThan(GreaterThan greaterThan);
        public abstract Object generateLesserThan(LesserThan lesserThan);
        public abstract Object generateGreaterThanOrEqual(GreaterThanOrEqual greaterThanOrEqual);
        public abstract Object generateLesserThanOrEqual(LesserThanOrEqual lesserThanOrEqual);
        public abstract Object generateAnd(And and);
        public abstract Object generateOr(Or or);
        public abstract Object generateBitwiseAnd(BitwiseAnd bitwiseAnd);
        public abstract Object generateBitwiseOr(BitwiseOr bitwiseOr);
        public abstract Object generateLeftShift(LeftShift leftShift);
        public abstract Object generateRightShift(RightShift rightShift);
        public abstract Object generateXor(Xor xor);
        public abstract Object generateNegative(Negative negative);
        public abstract Object generateNot(Not not);
        public abstract Object generateBitwiseNot(BitwiseNot bitwiseNot);
    }

    /**
     * The smallest block of code
     */
    public static abstract class Block {
        public abstract Object evaluate(Generator generator);
    }

    /**
     * Blocks representing a value (e.g. literals, variables)
     */
    public static class Value extends Block {
        private Object data;

        public void setData(Object data) {
            this.data = data;
        }

        public Object getData() {
            return data;
        }

        @Override
        public String toString() {
            return getData() + "";
        }

        @Override
        public Object evaluate(Generator generator) {
            return getData() + "";
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Value) {
                obj = ((Value) obj).getData();
            }
            if (getData() == null) {
                return null == obj;
            }
            return getData().equals(obj);
        }

        @Override
        public int hashCode() {
            return getData() != null ? getData().hashCode() : 0;
        }
    }

    /**
     * Class representing a number (e.g. 1, 2, 3, -100, 100, ...)
     */
    public static class Number extends Value {
        public Number(BigDecimal number) {
            setData(number);
        }

        public Number(double number) {
            setData(number);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateNumber(this);
        }
    }

    /**
     * Class representing a text (String literal) (e.g. "Hello")
     */
    public static class Text extends Value {
        public Text(String str) {
            setData(str);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateText(this);
        }
    }

    /**
     * Class representing a boolean (true/false)
     */
    public static class Boolean extends Value {
        public Boolean(boolean bool) {
            setData(bool);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateBoolean(this);
        }
    }

    /**
     * Class representing null
     */
    public static class Null extends Value {
        public Null() {
            setData(null);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateNull();
        }
    }

    /**
     * Class representing a List (e.g. [], [1], [1, 2, 3])
     */
    public static class List extends Value {
        public List(Value... values) {
            setData(new ArrayList<>(Arrays.asList(values)));
        }

        public List(ArrayList<Value> values) {
            setData(values);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateList(this);
        }
    }

    /**
     * Class representing a Map (e.g. {}, {1: "one"})
     */
    public static class Map extends Value {
        public Map(HashMap<Value, Value> values) {
            setData(values);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateMap(this);
        }
    }

    /**
     * Class representing variables
     */
    public static class Variable extends Value {
        private String variableName;
        private Value instance;

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        public Value getInstance() {
            return instance;
        }

        public void setInstance(Value instance) {
            this.instance = instance;
        }

        public Variable(String variableName) {
            this.variableName = variableName;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateVariable(this);
        }
    }

    /**
     * Class representing a set operator (e.g. a = 10)
     */
    public static class SetVariable extends Block {
        private String variableName;
        private Value instance;
        private Value value;

        public String getVariableName() {
            return variableName;
        }

        public void setVariableName(String variableName) {
            this.variableName = variableName;
        }

        public Value getInstance() {
            return instance;
        }

        public void setInstance(Value instance) {
            this.instance = instance;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public SetVariable(String variableName, Value value) {
            this.variableName = variableName;
            this.value = value;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateSetVariable(this);
        }
    }

    /**
     * Class representing all operators (e.g. *, +, >>, ^, !)
     */
    public static abstract class Operator extends Value {
        protected final Value value1;
        protected final Value value2;
        public Operator(Value value1, Value value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
        @Override
        public abstract Object evaluate(Generator generator);

        public Value getValue1() {
            return value1;
        }

        public Value getValue2() {
            return value2;
        }
    }

    public static class Add extends Operator {
        public Add(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateAdd(this);
        }
    }
    public static class Sub extends Operator {
        public Sub(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateSub(this);
        }
    }
    public static class Mul extends Operator {
        public Mul(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateMul(this);
        }
    }
    public static class Div extends Operator {
        public Div(Value value1, Value value2) {
            super(value1, value2);
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateDiv(this);
        }
    }
    public static class Mod extends Operator {
        public Mod(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateMod(this);
        }
    }
    public static class Pow extends Operator {
        public Pow(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generatePow(this);
        }
    }
    public static class Equals extends Operator {
        public Equals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateEquals(this);
        }
    }
    public static class NotEquals extends Operator {
        public NotEquals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateNotEquals(this);
        }
    }
    public static class LooksEquals extends Operator {
        public LooksEquals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateLooksEquals(this);
        }
    }
    public static class GreaterThan extends Operator {
        public GreaterThan(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateGreaterThan(this);
        }
    }
    public static class LesserThan extends Operator {
        public LesserThan(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateLesserThan(this);
        }
    }
    public static class GreaterThanOrEqual extends Operator {
        public GreaterThanOrEqual(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateGreaterThanOrEqual(this);
        }
    }
    public static class LesserThanOrEqual extends Operator {
        public LesserThanOrEqual(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateLesserThanOrEqual(this);
        }
    }
    public static class And extends Operator {
        public And(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateAnd(this);
        }
    }
    public static class Or extends Operator {
        public Or(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateOr(this);
        }
    }
    public static class BitwiseAnd extends Operator {
        public BitwiseAnd(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateBitwiseAnd(this);
        }
    }
    public static class BitwiseOr extends Operator {
        public BitwiseOr(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateBitwiseOr(this);
        }
    }
    public static class LeftShift extends Operator {
        public LeftShift(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateLeftShift(this);
        }
    }
    public static class RightShift extends Operator {
        public RightShift(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateRightShift(this);
        }
    }
    public static class Xor extends Operator {
        public Xor(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateXor(this);
        }
    }
    public static class Negative extends Operator {
        public Negative(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateNegative(this);
        }
    }
    public static class Not extends Operator {
        public Not(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateNot(this);
        }
    }
    public static class BitwiseNot extends Operator {
        public BitwiseNot(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateBitwiseNot(this);
        }
    }
}
