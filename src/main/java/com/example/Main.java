package com.example;

/**
 * Main class
 */
public class Main {
    public static void main(String[] args) {
        // nothing important here YET, just testing VMWrapper and ByteCodeGenerator. Frontend will be added later
        VMWrapper vm = new VMWrapper();
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator();
        // put "Hello, World!!!" in vm's stack
        vm.run((Object[]) new SyntaxTree.Text("Hello, World!!!").evaluate(vmByteCodeGenerator));
        // pop it from vm's stack and print it using System.out.println
        System.out.println(vm.pop());
    }
}