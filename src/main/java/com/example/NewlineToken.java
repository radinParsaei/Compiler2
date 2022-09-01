package com.example;

public class NewlineToken extends CustomToken {
    @Override
    public boolean check(String data) {
        return data.startsWith("\n");
    }

    @Override
    public String getText(String data) {
        int i = 0;
        while ((i < data.length()) && (data.charAt(i) == '\n')) {
            i++;
        }
        return data.substring(0, i);
    }
}
