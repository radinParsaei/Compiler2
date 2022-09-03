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

    public static void syntaxError(int i, String line) {

    }

    public static void invalidUseOfElseIfStatement(int line) {
        Utils.printError("Else-if statements can only be used after an if statement (line: ", String.valueOf(line), ")");
        Utils.exit(1);
    }

    public static void invalidUseOfElseStatement(int line) {
        Utils.printError("Else statements can only be used after an if statement (line: ", String.valueOf(line), ")");
        Utils.exit(1);
    }

    public static void useOfElseIfAfterElse(int line) {
        Utils.printError("Else-if statements can not be used after an else statement (line: ", String.valueOf(line), ")");
        Utils.exit(1);
    }
}
