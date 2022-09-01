package com.example;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final LinkedHashMap<String, String> lexerConf = new LinkedHashMap<>();
    private final LinkedHashMap<String, CustomToken> lexerConfWithStringChecker = new LinkedHashMap<>();
    private int line = 1;

    private String addStrings(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String item : strings) {
            builder.append(item);
        }
        return builder.toString();
    }

    public void add(String name, String regex) {
        lexerConf.put(name, addStrings("(", regex, ")"));
    }

    public void add(String name, CustomToken checker) {
        lexerConfWithStringChecker.put(name, checker);
    }

    private String findFromText(String text, String regex) {
        Pattern p = Pattern.compile(addStrings("^", lexerConf.get(regex)));
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(0);
        }
        return "";
    }

    public Token getToken(String input) {
        for (Map.Entry<String, CustomToken> conf : lexerConfWithStringChecker.entrySet()) {
            if (conf.getValue().check(input)) {
                String text = conf.getValue().getText(input);
                for (char c : text.toCharArray())
                    if (c == '\n') line++;
                return new Token(conf.getKey(), text, line);
            }
        }
        for (Map.Entry<String, String> conf : lexerConf.entrySet()) {
            String result = findFromText(input, conf.getKey());
            if (!result.equals("")) {
                for (char c : result.toCharArray())
                    if (c == '\n') line++;
                return new Token(conf.getKey(), result, line);
            }
        }
        return new Token();
    }

    public ArrayList<Token> lex(String input) {
        String line = input;
        String previousInput;
        ArrayList<Token> tokens = new ArrayList<>();
        while (input.length() != 0) {
            Token token = getToken(input);
            tokens.add(token);
            previousInput = input;
            input = input.substring(token.getText().length());
            if (previousInput.equals(input)) {
                Errors.syntaxError(line.length() - input.length(), line);
                return new ArrayList<>();
            }
        }
        return tokens;
    }
}
