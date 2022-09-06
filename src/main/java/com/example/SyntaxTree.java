package com.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import static com.example.Utils.copyArrays;

/**
 * Definition of code blocks shared between frontend and backend
 */
public class SyntaxTree {
    /**
     * The smallest block of code
     */
    public static abstract class Block {
        private final HashMap<Object, Object> extraData = new HashMap<>(); // defined to hold extra information (namespaces, ...)
        public abstract Object evaluate(Generator generator);
        public Value[] getValues() {
            return null;
        }
        public Block[] getCodeBlocks() {
            return null;
        }

        public Object getExtraData(Object key) {
            return extraData.get(key);
        }

        public void setExtraData(Object key, Object value) {
            extraData.put(key, value);
        }
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

        public Object evaluateValue(Generator generator) {
            return getData() + "";
        }

        @Override
        public Object evaluate(Generator generator) {
            if (getExtraData("unneededResult") != null) {
                return generator.generatePop(this);
            } else {
                return evaluateValue(generator);
            }
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

        public Number(String number) {
            setData(new BigDecimal(number));
        }

        @Override
        public Object evaluateValue(Generator generator) {
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
        public Object evaluateValue(Generator generator) {
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
        public Object evaluateValue(Generator generator) {
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
        public Object evaluateValue(Generator generator) {
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
        public Object evaluateValue(Generator generator) {
            return generator.generateList(this);
        }

        @Override
        public Value[] getValues() {
            ArrayList<Value> values = (ArrayList<Value>) getData();
            Value[] v = new Value[values.size()];
            v = values.toArray(v);
            return v;
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
        public Object evaluateValue(Generator generator) {
            return generator.generateMap(this);
        }

        @Override
        public Value[] getValues() {
            HashMap<Value, Value> values = (HashMap<Value, Value>) getData();
            Value[] res = new Value[values.size() * 2];
            int i = 0;
            for (java.util.Map.Entry<Value, Value> entry : values.entrySet()) {
                res[i++] = entry.getKey();
                res[i++] = entry.getValue();
            }
            return res;
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
        public Object evaluateValue(Generator generator) {
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
        private boolean isDeclaration;

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

        public boolean isDeclaration() {
            return isDeclaration;
        }

        public SetVariable setDeclaration(boolean declaration) {
            isDeclaration = declaration;
            return this;
        }

        public SetVariable(String variableName, Value value) {
            this.variableName = variableName;
            this.value = value;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateSetVariable(this);
        }

        @Override
        public Value[] getValues() {
            return new Value[] { value };
        }
    }

    /**
     * Class representing all operators (e.g. *, +, >>, ^, !)
     */
    public static abstract class Operator extends Value {
        private final Value value1;
        private final Value value2;
        public Operator(Value value1, Value value2) {
            this.value1 = value1;
            this.value2 = value2;
        }
        @Override
        public abstract Object evaluateValue(Generator generator);

        public Value getValue1() {
            return value1;
        }

        public Value getValue2() {
            return value2;
        }

        @Override
        public Value[] getValues() {
            return new Value[] { value1, value2 };
        }
    }

    public static class Add extends Operator {
        public Add(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateAdd(this);
        }
    }
    public static class Sub extends Operator {
        public Sub(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateSub(this);
        }
    }
    public static class Mul extends Operator {
        public Mul(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateMul(this);
        }
    }
    public static class Div extends Operator {
        public Div(Value value1, Value value2) {
            super(value1, value2);
        }

        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateDiv(this);
        }
    }
    public static class Mod extends Operator {
        public Mod(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateMod(this);
        }
    }
    public static class Pow extends Operator {
        public Pow(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generatePow(this);
        }
    }
    public static class Equals extends Operator {
        public Equals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateEquals(this);
        }
    }
    public static class NotEquals extends Operator {
        public NotEquals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateNotEquals(this);
        }
    }
    public static class LooksEquals extends Operator {
        public LooksEquals(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateLooksEquals(this);
        }
    }
    public static class GreaterThan extends Operator {
        public GreaterThan(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateGreaterThan(this);
        }
    }
    public static class LesserThan extends Operator {
        public LesserThan(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateLesserThan(this);
        }
    }
    public static class GreaterThanOrEqual extends Operator {
        public GreaterThanOrEqual(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateGreaterThanOrEqual(this);
        }
    }
    public static class LesserThanOrEqual extends Operator {
        public LesserThanOrEqual(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateLesserThanOrEqual(this);
        }
    }
    public static class And extends Operator {
        public And(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateAnd(this);
        }
    }
    public static class Or extends Operator {
        public Or(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateOr(this);
        }
    }
    public static class BitwiseAnd extends Operator {
        public BitwiseAnd(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateBitwiseAnd(this);
        }
    }
    public static class BitwiseOr extends Operator {
        public BitwiseOr(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateBitwiseOr(this);
        }
    }
    public static class LeftShift extends Operator {
        public LeftShift(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateLeftShift(this);
        }
    }
    public static class RightShift extends Operator {
        public RightShift(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateRightShift(this);
        }
    }
    public static class Xor extends Operator {
        public Xor(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateXor(this);
        }
    }
    public static class Negative extends Operator {
        public Negative(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateNegative(this);
        }
    }
    public static class Not extends Operator {
        public Not(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateNot(this);
        }
    }
    public static class BitwiseNot extends Operator {
        public BitwiseNot(Value value1, Value value2) {
            super(value1, value2);
        }
        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateBitwiseNot(this);
        }
    }
    // this class will be removed (replaced with native function calls)
    public static class Print extends Block {
        private Value message;

        public Print(Value message) {
            this.message = message;
        }

        public Value getMessage() {
            return message;
        }

        public void setMessage(Value message) {
            this.message = message;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generatePrint(this);
        }

        @Override
        public Value[] getValues() {
            return new Value[] { message };
        }
    }

    public static class ControlFLowBlock extends Block {
        private Blocks code;

        public ControlFLowBlock(Block... code) {
            setExtraData("locals", true);
            if (code.length == 1 && code[0] instanceof Blocks) this.code = (Blocks) code[0];
            else this.code = new Blocks(code);
        }

        public Blocks getCode() {
            return code;
        }

        public void setCode(Block... code) {
            this.code = new Blocks(code);
        }

        public void addCodeBlock(Block... block) {
            code = new Blocks((Blocks) code, block);
        }

        @Override
        public Object evaluate(Generator generator) {
            return null;
        }
    }

    /**
     * Block representing an if statement
     */
    public static class If extends ControlFLowBlock {
        private Value condition;
        private Blocks elseCode;

        public If(Value condition, Block... code) {
            super(code);
            this.condition = condition;
            setExtraData("condition", true);
        }

        public Value getCondition() {
            return condition;
        }

        public void setCondition(Value condition) {
            this.condition = condition;
        }

        public Blocks getElseCode() {
            return elseCode;
        }

        public If setElseCode(Block... elseCode) {
            this.elseCode = new Blocks(elseCode);
            return this;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateIf(this);
        }

        @Override
        public Value[] getValues() {
            return new Value[] { condition };
        }

        @Override
        public Block[] getCodeBlocks() {
            if (elseCode == null) {
                return new Block[] { getCode() };
            } else {
                return new Block[] { getCode(), elseCode };
            }
        }
    }

    /**
     * Block representing a while statement
     */
    public static class While extends ControlFLowBlock {
        private Value condition;

        public While(Value condition, Block... code) {
            super(code);
            this.condition = condition;
            setExtraData("condition", true);
        }

        public Value getCondition() {
            return condition;
        }

        public void setCondition(Value condition) {
            this.condition = condition;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateWhile(this);
        }

        @Override
        public Value[] getValues() {
            return new Value[] { condition };
        }

        @Override
        public Block[] getCodeBlocks() {
            return new Block[] { getCode() };
        }
    }

    public static class Blocks extends Block {
        private Block[] blocks;

        public Blocks(Block... blocks) {
            this.blocks = blocks;
        }

        public Blocks(Blocks code, Block... block) {
            Block[] blocks = new Block[code.getCodeBlocks().length + block.length];
            copyArrays(blocks, 0, code.getCodeBlocks(), block);
            this.blocks = blocks;
        }

        public Block[] getBlocks() {
            return blocks;
        }

        public void setBlocks(Block... blocks) {
            this.blocks = blocks;
        }

        public void addCodeBlock(Block... code) {
            Block[] res = new Block[code.length + blocks.length];
            copyArrays(res, 0, blocks, code);
            blocks = res;
        }

        @Override
        public Object evaluate(Generator generator) {
            ArrayList<Object> code = new ArrayList<>();
            for (Block i : blocks) {
                code.addAll(Arrays.asList((Object[]) i.evaluate(generator)));
            }
            Object[] res = new Object[code.size()];
            res = code.toArray(res);
            return res;
        }

        @Override
        public Block[] getCodeBlocks() {
            return blocks;
        }
    }

    public static class Function extends ControlFLowBlock {
        private final String functionName;

        public Function(String functionName, Block... code) {
            super(code);
            this.functionName = functionName;
            setExtraData("args", new String[] {});
        }

        public Function withArgs(String... args) {
            setExtraData("args", args);
            return this;
        }

        public String[] getArgs() {
            return (String[]) getExtraData("args");
        }

        public String getFunctionName() {
            return functionName;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateFunc(this);
        }

        @Override
        public Block[] getCodeBlocks() {
            return new Block[] { getCode() };
        }
    }

    public static class CallFunction extends Value {
        private final String functionName;
        private Value[] args;

        public CallFunction(String functionName, Value... args) {
            this.functionName = functionName;
            this.args = args;
        }

        public String getFunctionName() {
            return functionName;
        }

        public Value[] getArgs() {
            return args;
        }

        public void setArgs(Value... args) {
            this.args = args;
        }

        @Override
        public Object evaluateValue(Generator generator) {
            return generator.generateCall(this);
        }

        @Override
        public Value[] getValues() {
            return args;
        }
    }

    public static class Return extends Block {
        private Value value;

        public Return(Value value) {
            this.value = value;
        }

        public Value getValue() {
            return value;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        @Override
        public Object evaluate(Generator generator) {
            return generator.generateReturn(this);
        }

        @Override
        public Value[] getValues() {
            return new Value[] { value };
        }
    }

    public static class Continue extends Block {
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateContinue(this);
        }
    }
    public static class Break extends Block {
        @Override
        public Object evaluate(Generator generator) {
            return generator.generateBreak(this);
        }
    }
}
