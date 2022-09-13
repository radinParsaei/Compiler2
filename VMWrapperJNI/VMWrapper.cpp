#include "./com_example_VMWrapper.h"
#include "../VM/VM.h"

#define PREFIX(x) Java_com_example_VMWrapper_##x

std::vector<VM> vms;

Value disassemble(int prog, Value val);

JNIEXPORT void JNICALL PREFIX(run) (JNIEnv *env, jclass, jint vm, jint opcode, jint type, jstring data) {
    switch ((int) type) {
    case (int) Types::Null:
        vms[vm].run1(opcode);
        break;
    case (int) Types::False:
        vms[vm].run1(opcode, Types::False);
        break;
    case (int) Types::True:
        vms[vm].run1(opcode, Types::True);
        break;
    case (int) Types::BigNumber:
        vms[vm].run1(opcode, NUMBER_FROM_STRING(env->GetStringUTFChars(data, new jboolean(1))));
        break;
    case (int) Types::Text:
        vms[vm].run1(opcode, env->GetStringUTFChars(data, new jboolean(1)));
        break;
    }
}

JNIEXPORT void JNICALL PREFIX(runDouble) (JNIEnv *env, jclass, jint vm, jint opcode, jint type, jdouble data) {
    switch ((int) type) {
    case (int) Types::Number:
        vms[vm].run1(opcode, data);
        break;
    case (int) Types::SmallNumber:
        Value value = data;
        value.setType(Types::SmallNumber);
        vms[vm].run1(opcode, value);
        break;
    }
}

JNIEXPORT jstring JNICALL PREFIX(disassemble) (JNIEnv *env, jclass, jint opcode, jint type, jstring data) {
    switch ((int) type) {
    case (int) Types::Null:
        return env->NewStringUTF(disassemble(opcode, Types::Null).toString().c_str());
        break;
    case (int) Types::False:
        return env->NewStringUTF(disassemble(opcode, Types::False).toString().c_str());
        break;
    case (int) Types::True:
        return env->NewStringUTF(disassemble(opcode, Types::True).toString().c_str());
        break;
    case (int) Types::BigNumber:
        return env->NewStringUTF(disassemble(opcode, NUMBER_FROM_STRING(env->GetStringUTFChars(data, new jboolean(1)))).toString().c_str());
        break;
    case (int) Types::Text:
        return env->NewStringUTF(disassemble(opcode, env->GetStringUTFChars(data, new jboolean(1))).toString().c_str());
        break;
    }
}

JNIEXPORT jstring JNICALL PREFIX(disassembleDouble) (JNIEnv *env, jclass, jint opcode, jint type, jdouble data) {
    switch ((int) type) {
    case (int) Types::Number:
        return env->NewStringUTF(disassemble(opcode, data).toString().c_str());
        break;
    case (int) Types::SmallNumber:
        Value value = data;
        value.setType(Types::SmallNumber);
        return env->NewStringUTF(disassemble(opcode, value).toString().c_str());
        break;
    }
}

JNIEXPORT jint JNICALL PREFIX(init) (JNIEnv *, jclass) {
    vms.push_back(VM());
    return vms.size() - 1;
}

JNIEXPORT jbyte JNICALL PREFIX(getTypeOfStackTop) (JNIEnv *, jclass, jint vm) {
    return (char) vms[vm].getStackTop().getType();
}

JNIEXPORT jstring JNICALL PREFIX(getStackTop) (JNIEnv *env, jclass, jint vm) {
    return env->NewStringUTF(vms[vm].getStackTop().toString().c_str());
}

JNIEXPORT jdouble JNICALL PREFIX(getStackTopDouble) (JNIEnv *env, jclass, jint vm) {
    return (double) vms[vm].getStackTop();
}

JNIEXPORT jint JNICALL PREFIX(stackTopLen) (JNIEnv *env, jclass, jint vm) {
    return vms[vm].getStackTop().length();
}

JNIEXPORT jbyte JNICALL PREFIX(getTypeOfStackTopSlice) (JNIEnv *env, jclass, jint vm, jint i, jboolean key) {
    const Value& v = vms[vm].getStackTop();
    if (v.getType() == Types::Map) {
        if (key) {
            return (char) v.getKeyAt(i).getType();
        } else {
            return (char) v.getValueAt(i).getType();
        }
    }
    return (char) v[i].getType();
}

JNIEXPORT jstring JNICALL PREFIX(stackTopSlice) (JNIEnv *env, jclass, jint vm, jint i, jboolean key) {
    const Value& v = vms[vm].getStackTop();
    if (v.getType() == Types::Map) {
        if (key) {
            return env->NewStringUTF(v.getKeyAt(i).toString().c_str());
        } else {
            return env->NewStringUTF(v.getValueAt(i).toString().c_str());
        }
    }
    return env->NewStringUTF(v[i].toString().c_str());
}

JNIEXPORT jdouble JNICALL PREFIX(stackTopSliceDouble) (JNIEnv *env, jclass, jint vm, jint i, jboolean key) {
    const Value& v = vms[vm].getStackTop();
    if (v.getType() == Types::Map) {
        if (key) {
            return (double) vms[vm].getStackTop().getKeyAt(i);
        } else {
            return (double) vms[vm].getStackTop().getValueAt(i);
        }
    }
    return (double) v[i];
}

JNIEXPORT void JNICALL PREFIX(_1pop) (JNIEnv *env, jclass, jint vm) {
    vms[vm].run1(OPCODE_POP);
}