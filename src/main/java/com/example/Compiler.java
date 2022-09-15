package com.example;

/**
 * this class initializes the lexer and runs grammar rules
 */
public class Compiler {

    private String code;

    public Compiler(String code) {
        this.code = code;
        Errors.clear();
    }

    public Compiler() {
        Errors.clear();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getInputCode() {
        return code;
    }

    public static void initLexer(Lexer lexer) {
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
        lexer.add("PRINT", "print ");
        // semicolon
        lexer.add("SEMICOLON", ";");
        // operators
        lexer.add("EXPONENTIATION", "\\*\\*"); // exponentiation
        lexer.add("OP1", "\\*|\\/|%"); // operators with priority 1
        lexer.add("OP2", "\\-|\\+"); // operators with priority 2
        lexer.add("OP3", "\\|\\||\\||and|&&|&|or|\\^|>>|<<"); // operators with priority 3
        lexer.add("COMP", "!=|==|<=|>=|<|>"); // comparison operators
        lexer.add("SET", "=");
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
        // parenthesis
        lexer.add("LEFT_PARENTHESIS", "\\(");
        lexer.add("RIGHT_PARENTHESIS", "\\)");
        // var (keyword)
        lexer.add("VAR", "var ");
        // func (keyword)
        lexer.add("FUNC", "func ");
        // return (keyword)
        lexer.add("RETURN", "return");
        // continue (keyword)
        lexer.add("CONTINUE", "continue");
        // return (keyword)
        lexer.add("BREAK", "break");
        // comma
        lexer.add("COMMA", ",");
        // identifier (e.g. variable name)
        lexer.add("ID", "([A-Za-z]*\\d*_*)+");
    }

    public boolean afterLex(Parser result) {
        // Remove (and book-keep) new lines so that they can be skipped in rules that are not using them.
        result.remove("NEWLINE");
        return !Errors.wasThereAnError();
    }

    public void parse(Parser tokens) {
        tokens.replace("NUM", "expression", SyntaxTreeBinder::numberExpression);
        tokens.replace("TXT", "expression", SyntaxTreeBinder::textExpression);
        tokens.replace("BOOL", "expression", SyntaxTreeBinder::boolExpression);
        tokens.replace("NULL", "expression", SyntaxTreeBinder::nullExpression);
        tokens.replace("(VAR )?ID SET", "variable_set", SyntaxTreeBinder::setVariable);
        tokens.replace("VAR ID", "program", SyntaxTreeBinder::variableDeclaration);
        tokens.replace("FUNC ID LEFT_PARENTHESIS( ID( COMMA ID)*)? RIGHT_PARENTHESIS LEFT_BRACE",
                "declare_function", SyntaxTreeBinder::functionDeclaration);
        tokens.replace("ID LEFT_PARENTHESIS", "call_function", SyntaxTreeBinder::callFunction);
        tokens.replace("ID", "expression", SyntaxTreeBinder::variable);
        tokens.replace("expression EXPONENTIATION expression", "expression", SyntaxTreeBinder::exponentiation);
        tokens.replace("expression OP1 expression", "expression", SyntaxTreeBinder::operationsWithPriority1);
        tokens.replace("expression OP2 expression", "expression", SyntaxTreeBinder::operationsWithPriority2);
        tokens.replace("expression COMP expression", "expression", SyntaxTreeBinder::comparisonOperators);
        tokens.replace("expression OP3 expression", "expression", SyntaxTreeBinder::operationsWithPriority3);
        tokens.replace("call_function (expression (COMMA expression )*)?RIGHT_PARENTHESIS",
                "expression", SyntaxTreeBinder::callFunction1);
        tokens.replace("CONTINUE", "program", SyntaxTreeBinder::continueStatement);
        tokens.replace("BREAK", "program", SyntaxTreeBinder::breakStatement);
        tokens.replace("PRINT expression", "program", SyntaxTreeBinder::print);
        tokens.replace("RETURN( expression)?", "program", SyntaxTreeBinder::returnStatement, ((parser, index) -> {
            if (index == 0) return true;
            String tmp = parser.getTokens().get(index + 2).getName();
            return !(tmp.startsWith("OP") || tmp.equals("EXPONENTIATION") || tmp.equals("COMP"));
        }));
        tokens.replace("variable_set expression", "program", SyntaxTreeBinder::setVariable1);
        tokens.replace("IF expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::ifStatement);
        tokens.replace("program ELSEIF expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::elseIfStatement);
        tokens.replace("program ELSE LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::elseStatement);
        tokens.replace("WHILE expression LEFT_BRACE (program )?RIGHT_BRACE", "program", SyntaxTreeBinder::whileStatement);
        tokens.replace("declare_function( program)? RIGHT_BRACE", "program", SyntaxTreeBinder::functionDeclaration1);
        tokens.replace("program( (NEWLINE|SEMICOLON) program)+", "program", SyntaxTreeBinder::programs, "NEWLINE");
        tokens.replace("expression", "program", SyntaxTreeBinder::valueAsProgram, (parser, index) -> {
            if (index == 0) return true;
            String name = parser.getTokens().get(index - 1).getName();
            return !name.equals(name.toUpperCase()); // IF, RETURN (any token that is not parsed yet and is from lexer)
        });
    }

    public SyntaxTree.Block afterParse(Parser result) {
        return result.getTokens().get(0).getObject();
    }
}