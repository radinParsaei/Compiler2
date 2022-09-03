package com.example;

import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit test for the compiler (Parser).
 */
public class ParserTest {
    /**
     * Test number literals
     */
    @Test
    public void testNumber() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("10"));
        assertTrue(program instanceof SyntaxTree.Number);
        assertEquals(new BigDecimal("10"), ((SyntaxTree.Number) program).getData());

        program = CompilerMain.compile(new Compiler("1234.9876"));
        assertTrue(program instanceof SyntaxTree.Number);
        assertEquals(new BigDecimal("1234.9876"), ((SyntaxTree.Number) program).getData());
    }

    /**
     * Test string literals
     */
    @Test
    public void testString() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("'Hello, World!!!'"));
        assertTrue(program instanceof SyntaxTree.Text);
        assertEquals("Hello, World!!!", ((SyntaxTree.Text) program).getData());
    }

    /**
     * Test basic math operations
     */
    @Test
    public void testMath() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("10 * 2 + 5 / 2"));
        assertTrue(program instanceof SyntaxTree.Add);
        assertTrue(((SyntaxTree.Add) program).getValue1() instanceof SyntaxTree.Mul);
        assertTrue(((SyntaxTree.Add) program).getValue2() instanceof SyntaxTree.Div);
        assertEquals(new BigDecimal(10),
                ((SyntaxTree.Mul) ((SyntaxTree.Add) program).getValue1()).getValue1().getData());
        assertEquals(new BigDecimal(2),
                ((SyntaxTree.Mul) ((SyntaxTree.Add) program).getValue1()).getValue2().getData());
        assertEquals(new BigDecimal(5),
                ((SyntaxTree.Div) ((SyntaxTree.Add) program).getValue2()).getValue1().getData());
        assertEquals(new BigDecimal(2),
                ((SyntaxTree.Div) ((SyntaxTree.Add) program).getValue2()).getValue2().getData());
    }

    /**
     * Test logical operations
     */
    @Test
    public void testLogic() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("true and false || true"));
        assertTrue(program instanceof SyntaxTree.Or);
        assertTrue(((SyntaxTree.Or) program).getValue1() instanceof SyntaxTree.And);
        assertTrue(((SyntaxTree.Or) program).getValue2() instanceof SyntaxTree.Boolean);
        assertEquals(true,
                ((SyntaxTree.And) ((SyntaxTree.Or) program).getValue1()).getValue1().getData());
        assertEquals(false,
                ((SyntaxTree.And) ((SyntaxTree.Or) program).getValue1()).getValue2().getData());
        assertEquals(true, ((SyntaxTree.Or) program).getValue2().getData());
    }

    /**
     * Test comparison operations
     */
    @Test
    public void testComparison() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("100 == 200 != true"));
        assertTrue(program instanceof SyntaxTree.NotEquals);
        assertTrue(((SyntaxTree.NotEquals) program).getValue1() instanceof SyntaxTree.Equals);
        assertTrue(((SyntaxTree.NotEquals) program).getValue2() instanceof SyntaxTree.Boolean);
        assertEquals(new BigDecimal(100),
                ((SyntaxTree.Equals) ((SyntaxTree.NotEquals) program).getValue1()).getValue1().getData());
        assertEquals(new BigDecimal(200),
                ((SyntaxTree.Equals) ((SyntaxTree.NotEquals) program).getValue1()).getValue2().getData());
        assertEquals(true, ((SyntaxTree.NotEquals) program).getValue2().getData());
    }


    /**
     * Test if statement
     */
    @Test
    public void testIf() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("if true { } else { print 10 }"));
        assertTrue(program instanceof SyntaxTree.If);
        assertEquals(0, ((SyntaxTree.If) program).getCode().getCodeBlocks().length);
        assertEquals(1, ((SyntaxTree.If) program).getElseCode().getCodeBlocks().length);
        assertTrue(((SyntaxTree.If) program).getElseCode().getCodeBlocks()[0] instanceof SyntaxTree.Print);
        assertTrue(((SyntaxTree.If) program).getCondition() instanceof SyntaxTree.Boolean);

        program = CompilerMain.compile(new Compiler("if true { print 30 } else if false { } else if true { print 20 } else {  }"));
        assertTrue(program instanceof SyntaxTree.If);
        assertEquals(1, ((SyntaxTree.If) program).getCode().getCodeBlocks().length);
        assertTrue(((SyntaxTree.If) program).getElseCode().getCodeBlocks()[0] instanceof SyntaxTree.If);
        assertTrue(((SyntaxTree.If) ((SyntaxTree.If) program).getElseCode().getCodeBlocks()[0]).getElseCode()
                .getCodeBlocks()[0] instanceof SyntaxTree.If);
    }

    /**
     * Test while statement
     */
    @Test
    public void testWhile() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("while true { print 10 }"));
        assertTrue(program instanceof SyntaxTree.While);
        assertEquals(1, ((SyntaxTree.While) program).getCode().getCodeBlocks().length);
        assertTrue(((SyntaxTree.While) program).getCode().getCodeBlocks()[0] instanceof SyntaxTree.Print);
        assertTrue(((SyntaxTree.While) program).getCondition() instanceof SyntaxTree.Boolean);

        program = CompilerMain.compile(new Compiler("while true {}"));
        assertTrue(program instanceof SyntaxTree.While);
        assertEquals(0, ((SyntaxTree.While) program).getCode().getCodeBlocks().length);
    }
}
