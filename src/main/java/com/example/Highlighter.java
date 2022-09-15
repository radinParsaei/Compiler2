package com.example;

import org.jline.reader.LineReader;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;

public class Highlighter implements org.jline.reader.Highlighter {
    HashMap<String, String> colorConfigs = null;

    @Override
    public AttributedString highlight(LineReader lineReader, String s) {
        init();
        AttributedStyle attributedStyle = new AttributedStyle();
        AttributedStringBuilder builder = new AttributedStringBuilder();
        Lexer lexer = new Lexer();
        lexer.ignoreErrors(true);
        Compiler.initLexer(lexer);
        ArrayList<Token> tokens = lexer.lex(s);
        for (Token token : tokens) {
            if (colorConfigs.containsKey(token.getName())) {
                String conf = colorConfigs.get(token.getName());
                AttributedStyle attributedStyle1 = new AttributedStyle();
                int bgIndex = conf.indexOf("bg=");
                if (bgIndex != -1) {
                    attributedStyle1 = attributedStyle1.background(Integer.parseInt(conf.substring(bgIndex + 3, bgIndex + 6)));
                }
                int fgIndex = conf.indexOf("fg=");
                if (fgIndex != -1) {
                    attributedStyle1 = attributedStyle1.foreground(Integer.parseInt(conf.substring(fgIndex + 3, fgIndex + 6)));
                }
                if (conf.contains("bold")) {
                    attributedStyle1 = attributedStyle1.bold();
                }
                if (conf.contains("inverse")) {
                    attributedStyle1 = attributedStyle1.inverse();
                }
                if (conf.contains("underlined")) {
                    attributedStyle1 = attributedStyle1.underline();
                }
                builder.styled(attributedStyle1, token.getText());
            } else {
                switch (token.getName()) {
                    case "TXT":
                        builder.styled(attributedStyle.foreground(2), token.getText());
                        break;
                    case "NUM":
                        builder.styled(attributedStyle.foreground(AttributedStyle.BLUE), token.getText());
                        break;
                    case "BOOL":
                    case "NULL":
                        builder.styled(attributedStyle.foreground(215), token.getText());
                        break;
                    case "OP1":
                    case "OP2":
                    case "OP3":
                    case "EXPONENTIATION":
                    case "COMP":
                    case "SET":
                        builder.styled(attributedStyle.foreground(AttributedStyle.MAGENTA), token.getText());
                        break;
                    case "IF":
                    case "ELSE":
                    case "ELSEIF":
                    case "FUNC":
                    case "VAR":
                    case "RETURN":
                    case "WHILE":
                    case "BREAK":
                    case "CONTINUE":
                        builder.styled(attributedStyle.foreground(140), token.getText());
                        break;
                    case "ID":
                        builder.styled(attributedStyle.underline(), token.getText());
                        break;
                    default:
                        builder.styled(AttributedStyle.DEFAULT, token.getText());
                        break;
                }
            }
        }
        return builder.toAttributedString();
    }

    @Override
    public void setErrorPattern(Pattern pattern) {

    }

    @Override
    public void setErrorIndex(int i) {

    }

    private void init() {
        if (colorConfigs == null) {
            BufferedReader bufferedReader;
            try {
                bufferedReader = new BufferedReader(new FileReader("./.colors"));
            } catch (FileNotFoundException e) {
                return;
            }
            colorConfigs = new HashMap<>();
            try {
                String line = bufferedReader.readLine();
                while (line != null) {
                    colorConfigs.put(line.split(":")[0].trim(), line.split(":")[1].trim().toLowerCase());
                    line = bufferedReader.readLine();
                }
            } catch (IOException ignore) {
            } finally {
                try {
                    bufferedReader.close();
                } catch (IOException ignored) {}
            }
        }
    }
}