package com.example;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;

public class VMWrapper {
    public static final byte PUT = 1;
    public static final byte ADD = 2;
    public static final byte SUB = 3;
    public static final byte MUL = 4;
    public static final byte DIV = 5;
    public static final byte MOD = 6;
    public static final byte POW = 7;
    public static final byte SETVAR = 8;
    public static final byte GETVAR = 9;
    public static final byte DELVAR = 10;
    public static final byte REC = 11;
    public static final byte END = 12;
    public static final byte SKIPIF = 13;
    public static final byte SKIPIFN = 14;
    public static final byte WHILE = 15;
    public static final byte NEQ = 16;
    public static final byte EQ = 17;
    public static final byte GT = 18;
    public static final byte GE = 19;
    public static final byte LT = 20;
    public static final byte LE = 21;
    public static final byte LEQ = 22;
    public static final byte AND = 23;
    public static final byte OR = 24;
    public static final byte B_AND = 25;
    public static final byte B_OR = 26;
    public static final byte B_NOT = 27;
    public static final byte NOT = 28;
    public static final byte LSHIFT = 29;
    public static final byte RSHIFT = 30;
    public static final byte XOR = 31;
    public static final byte NEGATE = 32;
    public static final byte POP = 33;
    public static final byte CREATE_ARR = 34;
    public static final byte GET = 35;
    public static final byte SET = 36;
    public static final byte CREATE_MAP = 37;
    public static final byte MKFUNC = 38;
    public static final byte CALLFUNC = 39;
    public static final byte GETPARAM = 40;
    public static final byte CONTINUE = 41;
    public static final byte BREAK = 42;
    public static final byte RETURN = 43;
    public static final byte WHILET = 44;
    public static final byte SKIP = 45;
    public static final byte CREATE_CLASS = 46;
    public static final byte CREATE_INSTANCE = 47;
    public static final byte CALLFUNCFROMINS = 48;
    public static final byte CALLMETHOD = 49;
    public static final byte THIS = 50;
    public static final byte GETPTRTOLASTFUNC = 51;
    public static final byte IS = 52;
    public static final byte INCREASE = 53;
    public static final byte DECREASE = 54;
    public static final byte INPLACE_MUL = 55;
    public static final byte INPLACE_DIV = 56;
    public static final byte INPLACE_MOD = 57;
    public static final byte INPLACE_POW = 58;
    public static final byte INPLACE_AND = 59;
    public static final byte INPLACE_OR = 60;
    public static final byte INPLACE_LSHIFT = 61;
    public static final byte INPLACE_RSHIFT = 62;
    public static final byte INPLACE_XOR = 63;
    public static final byte DLCALL = 64;

    private final int vm;

    static {
        if (!Utils.IS_AOT) System.loadLibrary("vmwrapper");
    }

    public VMWrapper() {
        vm = init();
    }

    /**
     * Creates a new VM instance
     */
    private static native int init();
    /**
     * Run a single opcode on VM
     */
    public static native void run(int vm, int opcode, int type, String data);
    /**
     * Run a single opcode on VM which its data is a double type (e.g. PUT NUM1)
     */
    public static native void runDouble(int vm, int opcode, int type, double data);

    /**
     * Disassemble a single opcode
     */
    public static native String disassemble(int opcode, int type, String data);
    public static native String disassembleDouble(int opcode, int type, double data);

    // functions to get access to the vm's stack (needed in pop())
    private static native byte getTypeOfStackTop(int vm);
    private static native String getStackTop(int vm);
    private static native double getStackTopDouble(int vm);
    private static native int stackTopLen(int vm);
    private static native String stackTopSlice(int vm, int j, boolean keyOrValue /* when the last item in the stack is a map */);
    private static native double stackTopSliceDouble(int vm, int j, boolean keyOrValue /* when the last item in the stack is a map */);
    private static native byte getTypeOfStackTopSlice(int vm, int j, boolean keyOrValue /* when the last item in the stack is a map */);
    private static native void _pop(int vm);
    public static native void flush();

    /**
     * Get the item in the top of vm's stack
     */
    public SyntaxTree.Value stackTop() {
        int i = getTypeOfStackTop(vm);
        switch (i) {
            case 0:
                return new SyntaxTree.Null();
            case 1:
                return new SyntaxTree.Boolean(true);
            case 2:
                return new SyntaxTree.Boolean(false);
            case 3:
                return new SyntaxTree.Number(getStackTopDouble(vm));
            case 4:
                return new SyntaxTree.Number(new BigDecimal(getStackTop(vm)));
            case 5:
                return new SyntaxTree.Text(getStackTop(vm));
            case 6: {
                ArrayList<SyntaxTree.Value> values = new ArrayList<>();
                for (int j = 0; j < stackTopLen(vm); j++) {
                    values.add(slice(j, false));
                }
                return new SyntaxTree.List(values);
            }
            case 7: {
                HashMap<SyntaxTree.Value, SyntaxTree.Value> values = new HashMap<>();
                for (int j = 0; j < stackTopLen(vm); j++) {
                    values.put(slice(j, true), slice(j, false));
                }
                return new SyntaxTree.Map(values);
            }
        }
        return new SyntaxTree.Number(1);
    }

    /**
     * Pop(get and remove) the item in the top of vm's stack
     */
    public SyntaxTree.Value pop() {
        SyntaxTree.Value tmp = stackTop();
        _pop(vm);
        return tmp;
    }

    private SyntaxTree.Value slice(int j, boolean isKeyOrValue) {
        int type = getTypeOfStackTopSlice(vm, j, isKeyOrValue);
        switch (type) {
            case 0:
                return new SyntaxTree.Null();
            case 1:
                return new SyntaxTree.Boolean(true);
            case 2:
                return new SyntaxTree.Boolean(false);
            case 3:
                return new SyntaxTree.Number(stackTopSliceDouble(vm, j, isKeyOrValue));
            case 4:
                return new SyntaxTree.Number(new BigDecimal(stackTopSlice(vm, j, isKeyOrValue)));
            case 5:
                return new SyntaxTree.Text(stackTopSlice(vm, j, isKeyOrValue));
        }
        return null;
    }

    /**
     * Run a set of opcodes on vm
     */
    public void run(Object... objects) {
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Byte) {
                if (needsParameter((Byte) objects[i])) {
                    if (objects.length <= i + 1 || objects[i + 1] == null) {
                        run(vm, (byte) objects[i], 0, null);
                        i++;
                    } else if (objects[i + 1] instanceof Boolean) {
                        if ((Boolean) objects[i + 1]) {
                            run(vm, (byte) objects[i], 1, null);
                        } else {
                            run(vm, (byte) objects[i], 2, null);
                        }
                        i++;
                    } else if (objects[i + 1] instanceof Double) {
                        runDouble(vm, (byte) objects[i], 3, (double) objects[i + 1]);
                        i++;
                    } else if (objects[i + 1] instanceof Integer) {
                        runDouble(vm, (byte) objects[i], 3, (int) objects[i + 1]);
                        i++;
                    } else if (objects[i + 1] instanceof BigDecimal) {
                        run(vm, (byte) objects[i], 4, objects[i + 1] + "");
                        i++;
                    } else if (objects[i + 1] instanceof String) {
                        run(vm, (byte) objects[i], 5, objects[i + 1] + "");
                        i++;
                    }
                } else {
                    run(vm, (byte) objects[i], 0, null);
                }
            }
        }
    }

    /**
     * Disassemble a set of opcodes
     */
    public static String disassemble(Object... objects) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objects.length; i++) {
            if (objects[i] instanceof Byte) {
                if (needsParameter((Byte) objects[i])) {
                    if (objects.length <= i + 1 || objects[i + 1] == null) {
                        stringBuilder.append(disassemble((byte) objects[i], 0, null)).append('\n');
                        i++;
                    } else if (objects[i + 1] instanceof Boolean) {
                        if ((Boolean) objects[i + 1]) {
                            stringBuilder.append(disassemble((byte) objects[i], 1, null)).append('\n');
                        } else {
                            stringBuilder.append(disassemble((byte) objects[i], 2, null)).append('\n');
                        }
                        i++;
                    } else if (objects[i + 1] instanceof Integer) {
                        stringBuilder.append(disassembleDouble((byte) objects[i], 3, (int) objects[i + 1])).append('\n');
                        i++;
                    } else if (objects[i + 1] instanceof Double) {
                        stringBuilder.append(disassembleDouble((byte) objects[i], 3, (double) objects[i + 1])).append('\n');
                        i++;
                    } else if (objects[i + 1] instanceof BigDecimal) {
                        stringBuilder.append(disassemble((byte) objects[i], 4, objects[i + 1] + "")).append('\n');
                        i++;
                    } else if (objects[i + 1] instanceof String) {
                        stringBuilder.append(disassemble((byte) objects[i], 5, objects[i + 1] + "")).append('\n');
                        i++;
                    }
                } else {
                    stringBuilder.append(disassemble((byte) objects[i], 0, null)).append('\n');
                }
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Check if an opcode needs a parameter
     */
    public static boolean needsParameter(int opcode) {
        return opcode == PUT || opcode == GETVAR || opcode == SETVAR || opcode == DELVAR || opcode == CREATE_ARR ||
                opcode == CREATE_MAP || opcode == INCREASE || opcode == MKFUNC || opcode == CALLFUNC ||
                opcode == GETPARAM || opcode == SKIPIFN || opcode == SKIPIF || opcode == SKIP ||
                opcode == CREATE_CLASS || opcode == CREATE_INSTANCE || opcode == CALLFUNCFROMINS ||
                opcode == CALLMETHOD || opcode == IS || opcode == DECREASE || opcode == INPLACE_MUL ||
                opcode == INPLACE_DIV || opcode == INPLACE_MOD || opcode == INPLACE_POW || opcode == INPLACE_AND ||
                opcode == INPLACE_OR || opcode == INPLACE_LSHIFT || opcode == INPLACE_RSHIFT || opcode == INPLACE_XOR ||
                opcode == DLCALL;
    }
}
