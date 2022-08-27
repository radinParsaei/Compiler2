package com.example;

public class Utils {
    public static void copyArrays(Object[] destination, int loc, Object[]... toCopy) {
        for (Object[] objects : toCopy) {
            System.arraycopy(objects, 0, destination, loc, objects.length);
            loc += objects.length;
        }
    }

    public static void printError(String... msg) {
        for (String s : msg) {
            System.err.print(s);
        }
        System.err.println();
    }

    /**
     * Terminates the compiler
     *
     * implemented in the Utils class to make changes easier while porting to platforms not supporting "System.exit" (e.g. TeaVM)
     */
    public static void exit(int i) {
        System.exit(i);
    }
}
