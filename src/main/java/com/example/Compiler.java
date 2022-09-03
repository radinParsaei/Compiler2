package com.example;

/**
 * this class initializes the lexer and runs grammar rules
 */
public class Compiler {

    private final String code;

    public Compiler(String code) {
        this.code = code;
    }

    public String getInputCode() {
        return code;
    }

    public void initLexer(Lexer lexer) {
        lexer.add("NEWLINE", new NewlineToken());
        // Text
        lexer.add("TXT", new StringLiteralToken());
        // Number
        lexer.add("NUM", "\\d+(\\.\\d+)?");
        // Boolean
        lexer.add("BOOL", "true|false");
        // Null
        lexer.add("NULL", "null");
        // print (Will be removed)
        lexer.add("PRINT", "print");
        // print (Will be removed)
        lexer.add("SEMICOLON", ";");
        // operators
        lexer.add("EXPONENTIATION", "\\*\\*"); // exponentiation
        lexer.add("OP1", "\\*|\\/|%"); // operators with priority 1
        lexer.add("OP2", "\\-|\\+"); // operators with priority 2
        lexer.add("OP3", "\\|\\||\\||and|&&|&|or|\\^|>>|<<"); // operators with priority 3
        lexer.add("COMP", "!=|==|<=|>=|<|>"); // comparison operators
        // while (keyword)
        lexer.add("WHILE", "while ");
        // if (keyword)
        lexer.add("IF", "if ");
        // else (keyword)
        lexer.add("ELSEIF", "else if ");
        lexer.add("ELSE", "else");

        lexer.add("IGNORE", " |\t+");
        // brackets
        lexer.add("LEFT_BRACE", "\\{");
        lexer.add("RIGHT_BRACE", "\\}");
    }

    public void afterLex(Parser result) {
        // Remove (and book-keep) new lines so that they can be skipped in rules that are not using them.
        result.remove("NEWLINE");
    }

    public void parse(Parser tokens) {
        tokens.replace("NUM", "expression", SyntaxTreeBinder::numberExpression);
        tokens.replace("TXT", "expression", SyntaxTreeBinder::textExpression);
        tokens.replace("BOOL", "expression", SyntaxTreeBinder::boolExpression);
        tokens.replace("NULL", "expression", SyntaxTreeBinder::nullExpression);
        tokens.replace("expression EXPONENTIATION expression", "expression", SyntaxTreeBinder::exponentiation);
        tokens.replace("expression OP1 expression", "expression", SyntaxTreeBinder::operationsWithPriority1);
        tokens.replace("expression OP2 expression", "expression", SyntaxTreeBinder::operationsWithPriority2);
        tokens.replace("expression COMP expression", "expression", SyntaxTreeBinder::comparisonOperators);
        tokens.replace("expression OP3 expression", "expression", SyntaxTreeBinder::operationsWithPriority3);
        tokens.replace("PRINT expression", "program", SyntaxTreeBinder::print);
        tokens.replace("IF expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::ifStatement);
        tokens.replace("program ELSEIF expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::elseIfStatement);
        tokens.replace("program ELSE LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::elseStatement);
        tokens.replace("WHILE expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::whileStatement);
        tokens.replace("program( (NEWLINE|SEMICOLON) program)+", "program", SyntaxTreeBinder::programs, "NEWLINE");
    }

    public SyntaxTree.Block afterParse(Parser result) {
        return result.getTokens().get(0).getObject();
    }
}