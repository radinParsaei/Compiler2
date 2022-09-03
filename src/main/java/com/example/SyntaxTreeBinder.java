package com.example;

/**
 * this class will help "Compiler" convert tokens to SyntaxTree
 */
public class SyntaxTreeBinder {
    private static final SyntaxTree.Block nullInstance = new SyntaxTree.Null();

    // NUM (e.g. 1, 2, 1234)
    public static SyntaxTree.Block numberExpression(Parser parser) {
        return new SyntaxTree.Number(parser.getTokens().get(0).getText());
    }

    // null
    public static SyntaxTree.Block nullExpression(Parser parser) {
        return nullInstance;
    }

    // true | false
    public static SyntaxTree.Block boolExpression(Parser parser) {
        return new SyntaxTree.Boolean(parser.getTokens().get(0).getText().equals("true"));
    }

    // "Hello!"
    public static SyntaxTree.Block textExpression(Parser parser) {
        String text = parser.getTokens().get(0).getText();
        return new SyntaxTree.Text(text.substring(1, text.length() - 1));
    }

    // 10 ** 2
    public static SyntaxTree.Block exponentiation(Parser parser) {
        return new SyntaxTree.Pow((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                (SyntaxTree.Value) parser.getTokens().get(2).getObject());
    }

    // 10 * 2, 10 / 2, 10 % 2
    public static SyntaxTree.Block operationsWithPriority1(Parser parser) {
        if (parser.getTokens().get(1).getText().equals("*")) {
            return new SyntaxTree.Mul((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                    (SyntaxTree.Value) parser.getTokens().get(2).getObject());
        } else if (parser.getTokens().get(1).getText().equals("%")) {
            return new SyntaxTree.Mod((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                    (SyntaxTree.Value) parser.getTokens().get(2).getObject());
        }
        return new SyntaxTree.Div((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                (SyntaxTree.Value) parser.getTokens().get(2).getObject());
    }

    // 10 + 2, 10 - 2
    public static SyntaxTree.Block operationsWithPriority2(Parser parser) {
        if (parser.getTokens().get(1).getText().equals("+")) {
            return new SyntaxTree.Add((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                    (SyntaxTree.Value) parser.getTokens().get(2).getObject());
        }
        return new SyntaxTree.Sub((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                (SyntaxTree.Value) parser.getTokens().get(2).getObject());
    }

    // ==, <, > , <=, >=, !=
    public static SyntaxTree.Block comparisonOperators(Parser parser) {
        switch (parser.getTokens().get(1).getText()) {
            case "==":
                return new SyntaxTree.Equals((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case ">":
                return new SyntaxTree.GreaterThan((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "<":
                return new SyntaxTree.LesserThan((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "<=":
                return new SyntaxTree.LesserThanOrEqual((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "!=":
                return new SyntaxTree.NotEquals((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            default:
                return new SyntaxTree.GreaterThanOrEqual((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
        }
    }

    // >>, <<, ^, &, |, [and, &&], [or, ||]
    public static SyntaxTree.Block operationsWithPriority3(Parser parser) {
        switch (parser.getTokens().get(1).getText()) {
            case ">>":
                return new SyntaxTree.RightShift((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "<<":
                return new SyntaxTree.LeftShift((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "^":
                return new SyntaxTree.Xor((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "&":
                return new SyntaxTree.BitwiseAnd((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "and":
            case "&&":
                return new SyntaxTree.And((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            case "or":
            case "||":
                return new SyntaxTree.Or((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
            default:
                return new SyntaxTree.BitwiseOr((SyntaxTree.Value) parser.getTokens().get(0).getObject(),
                        (SyntaxTree.Value) parser.getTokens().get(2).getObject());
        }
    }

    // PRINT exp (print 1, print "Hello")
    public static SyntaxTree.Block print(Parser parser) {
        return new SyntaxTree.Print((SyntaxTree.Value) parser.getTokens().get(1).getObject());
    }

    // program SEMICOLON|NEWLINE program
    // print 10; print 20
    // OR
    // print 10
    // print 20
    public static SyntaxTree.Block programs(Parser parser) {
        parser.purge("SEMICOLON");
        parser.purge("NEWLINE");
        SyntaxTree.Blocks blocks = new SyntaxTree.Blocks();
        for (Token token : parser.getTokens()) {
            blocks.addCodeBlock(token.getObject());
        }
        return blocks;
    }

    public static SyntaxTree.Block ifStatement(Parser parser) {
        SyntaxTree.Block tmp = parser.getTokens().get(3).getObject();
        return new SyntaxTree.If((SyntaxTree.Value) parser.getTokens().get(1).getObject(),
                tmp != null? tmp : new SyntaxTree.Blocks());
    }

    public static SyntaxTree.Block whileStatement(Parser parser) {
        SyntaxTree.Block tmp = parser.getTokens().get(3).getObject();
        return new SyntaxTree.While((SyntaxTree.Value) parser.getTokens().get(1).getObject(),
                tmp != null? tmp : new SyntaxTree.Blocks());
    }

    public static SyntaxTree.Block elseIfStatement(Parser parser) {
        if (parser.getTokens().get(0).getObject() instanceof SyntaxTree.If) {
            SyntaxTree.If tmp = (SyntaxTree.If) parser.getTokens().get(0).getObject();
            SyntaxTree.Block program = parser.getTokens().get(4).getObject();
            if (program == null) program = new SyntaxTree.Blocks();
            if (tmp.getExtraData("else") != null) {
                Errors.useOfElseIfAfterElse(parser.getTokens().get(1).getLine());
                return null;
            }
            SyntaxTree.If lastElseIf = tmp;
            if (tmp.getExtraData("lastElseIf") != null) {
                lastElseIf = ((SyntaxTree.If) tmp.getExtraData("lastElseIf"));
            }
            SyntaxTree.If statement = new SyntaxTree.If((SyntaxTree.Value) parser.getTokens().get(2).getObject(), program);
            tmp.setExtraData("lastElseIf", statement);
            lastElseIf.setElseCode(statement);
            return tmp;
        } else {
            Errors.invalidUseOfElseIfStatement(parser.getTokens().get(0).getLine());
            return null;
        }
    }

    public static SyntaxTree.Block elseStatement(Parser parser) {
        if (parser.getTokens().get(0).getObject() instanceof SyntaxTree.If) {
            SyntaxTree.If tmp = (SyntaxTree.If) parser.getTokens().get(0).getObject();
            SyntaxTree.Block program = parser.getTokens().get(3).getObject();
            if (program == null) program = new SyntaxTree.Blocks();
            SyntaxTree.If lastElseIf = tmp;
            if (tmp.getExtraData("lastElseIf") != null) {
                lastElseIf = ((SyntaxTree.If) tmp.getExtraData("lastElseIf"));
            }
            tmp.setExtraData("else", true);
            lastElseIf.setElseCode(program);
            return tmp;
        } else {
            Errors.invalidUseOfElseStatement(parser.getTokens().get(0).getLine());
            return null;
        }
    }
}
