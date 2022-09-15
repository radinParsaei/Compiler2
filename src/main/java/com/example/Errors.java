package com.example;

public class Errors {
    private static boolean thereWasAnError = false;

    public static boolean wasThereAnError() {
        return thereWasAnError;
    }

    public static void clear() {
        thereWasAnError = false;
    }

    public static void accessedUndefinedVariable(String variableName, SyntaxTree.Block block) {
        thereWasAnError = true;
        Utils.printError("Tried to access undefined variable ", "\"", variableName, "\"",
                block.getExtraData("lineNumber") != null? " in line " + block.getExtraData("lineNumber") : "");
    }

    public static void modifiedUndefinedVariable(String variableName, SyntaxTree.Block block) {
        thereWasAnError = true;
        Utils.printError("Tried to modify the value of an undefined variable ", "\"", variableName, "\"",
                block.getExtraData("lineNumber") != null? " in line " + block.getExtraData("lineNumber") : "");
    }

    public static void syntaxError(int i, String line) {
        thereWasAnError = true;
    }

    public static void invalidUseOfElseIfStatement(int line) {
        thereWasAnError = true;
        Utils.printError("Else-if statements can only be used after an if statement (line: ", String.valueOf(line), ")");
    }

    public static void invalidUseOfElseStatement(int line) {
        thereWasAnError = true;
        Utils.printError("Else statements can only be used after an if statement (line: ", String.valueOf(line), ")");
    }

    public static void useOfElseIfAfterElse(int line) {
        thereWasAnError = true;
        Utils.printError("Else-if statements can not be used after an else statement (line: ", String.valueOf(line), ")");
    }
}
