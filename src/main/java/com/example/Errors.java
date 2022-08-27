package com.example;

public class Errors {
    public static void accessedUndefinedVariable(String variableName) {
        Utils.printError("Tried to access undefined variable ", "\"", variableName, "\"");
        Utils.exit(1);
    }

    public static void modifiedUndefinedVariable(String variableName) {
        Utils.printError("Tried to modify the value of an undefined variable ", "\"", variableName, "\"");
        Utils.exit(1);
    }
}
