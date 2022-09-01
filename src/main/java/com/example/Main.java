package com.example;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {
        // nothing important here YET, just testing VMWrapper and ByteCodeGenerator. Frontend will be added later
        VMWrapper vm = new VMWrapper();
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator(false);
        // put "Hello, World!!!" in vm's stack
        vm.run((Object[]) vmByteCodeGenerator.generate(new SyntaxTree.Text("Hello, World!!!")));
        // pop it from vm's stack and print it using System.out.println
        System.out.println(vm.pop());
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("print \"Hello, World!!!\""));
        VMByteCodeGenerator vmByteCodeGenerator1 = new VMByteCodeGenerator();
        VMWrapper vm1 = new VMWrapper();
        Object[] bytes = (Object[]) vmByteCodeGenerator1.generate(program);
        System.out.println(VMWrapper.disassemble(bytes));
        vm1.run(bytes);
    }
}