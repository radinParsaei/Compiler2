package com.example;

import org.junit.Assume;
import org.junit.Test;

import java.math.BigDecimal;

import static org.junit.Assert.*;

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
        assertEquals(new SyntaxTree.Number("10"), ((SyntaxTree.Number) program).getData());

        program = CompilerMain.compile(new Compiler("1234.9876"));
        assertTrue(program instanceof SyntaxTree.Number);
        assertEquals(new SyntaxTree.Number("1234.9876"), ((SyntaxTree.Number) program).getData());
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
        assertEquals(0, ((SyntaxTree.If) program).getCode().getBlocks().length);
        assertEquals(1, ((SyntaxTree.If) program).getElseCode().getBlocks().length);
        assertTrue(((SyntaxTree.If) program).getElseCode().getBlocks()[0] instanceof SyntaxTree.Print);
        assertTrue(((SyntaxTree.If) program).getCondition() instanceof SyntaxTree.Boolean);

        program = CompilerMain.compile(new Compiler("if true { print 30 } else if false { } else if true { print 20 } else {  }"));
        assertTrue(program instanceof SyntaxTree.If);
        assertEquals(1, ((SyntaxTree.If) program).getCode().getCodeBlocks().length);
        assertTrue(((SyntaxTree.If) program).getElseCode().getBlocks()[0] instanceof SyntaxTree.If);
        assertTrue(((SyntaxTree.If) ((SyntaxTree.If) program).getElseCode().getBlocks()[0]).getElseCode()
                .getBlocks()[0] instanceof SyntaxTree.If);
    }

    /**
     * Test while statement
     */
    @Test
    public void testWhile() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("while true { print 10 }"));
        assertTrue(program instanceof SyntaxTree.While);
        assertEquals(1, ((SyntaxTree.While) program).getCode().getBlocks().length);
        assertTrue(((SyntaxTree.While) program).getCode().getBlocks()[0] instanceof SyntaxTree.Print);
        assertTrue(((SyntaxTree.While) program).getCondition() instanceof SyntaxTree.Boolean);

        program = CompilerMain.compile(new Compiler("while true {}"));
        assertTrue(program instanceof SyntaxTree.While);
        assertEquals(0, ((SyntaxTree.While) program).getCode().getBlocks().length);
    }

    /**
     * Test variables
     */
    @Test
    public void testVariable() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("var a = 10\n" +
                "a = 20\n" +
                "print a\n" +
                "var b"));
        assertTrue(program instanceof SyntaxTree.Blocks);
        assertEquals(4, ((SyntaxTree.Blocks) program).getBlocks().length);
        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[0] instanceof SyntaxTree.SetVariable);
        assertTrue(((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[0]).isDeclaration());
        assertEquals("a", ((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[0])
                .getVariableName());
        assertEquals(new SyntaxTree.Number("10"),
                ((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[0]).getValue());

        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[1] instanceof SyntaxTree.SetVariable);
        assertFalse(((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[1]).isDeclaration());
        assertEquals("a", ((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[1])
                .getVariableName());
        assertEquals(new SyntaxTree.Number("20"),
                ((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[1]).getValue());

        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[2] instanceof SyntaxTree.Print);
        assertTrue(((SyntaxTree.Print) ((SyntaxTree.Blocks) program).getBlocks()[2]).getMessage()
                instanceof SyntaxTree.Variable);
        assertEquals("a", ((SyntaxTree.Variable) ((SyntaxTree.Print) ((SyntaxTree.Blocks) program)
                .getBlocks()[2]).getMessage()).getVariableName());

        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[3] instanceof SyntaxTree.SetVariable);
        assertTrue(((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[3]).isDeclaration());
        assertEquals("b", ((SyntaxTree.SetVariable) ((SyntaxTree.Blocks) program).getBlocks()[3])
                .getVariableName());
    }

    /**
     * Test functions
     */
    @Test
    public void testFunction() {
        SyntaxTree.Block program = CompilerMain.compile(new Compiler("func printSum(a, b) {\n" +
                "   print a + b\n" +
                "}\n" +
                "printSum(1, 2)"));
        assertTrue(program instanceof SyntaxTree.Blocks);
        assertEquals(2, ((SyntaxTree.Blocks) program).getBlocks().length);
        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[0] instanceof SyntaxTree.Function);
        assertEquals("printSum",
                ((SyntaxTree.Function) ((SyntaxTree.Blocks) program).getBlocks()[0]).getFunctionName());
        assertArrayEquals(new String[] { "a", "b" },
                ((SyntaxTree.Function) ((SyntaxTree.Blocks) program).getBlocks()[0]).getArgs());
        assertTrue(((SyntaxTree.Function) ((SyntaxTree.Blocks) program).getBlocks()[0]).getCode()
                .getBlocks()[0] instanceof SyntaxTree.Print);

        assertTrue(((SyntaxTree.Blocks) program).getBlocks()[1] instanceof SyntaxTree.CallFunction);
        assertEquals("printSum",
                ((SyntaxTree.CallFunction) ((SyntaxTree.Blocks) program).getBlocks()[1]).getFunctionName());
        assertArrayEquals(new SyntaxTree.Value[] { new SyntaxTree.Number("1"), new SyntaxTree.Number("2") },
                ((SyntaxTree.CallFunction) ((SyntaxTree.Blocks) program).getBlocks()[1]).getArgs());
    }

    /**
     * Test recursive functions
     */
    @Test
    public void testRecursiveFunction() {

        SyntaxTree.Block program = CompilerMain.compile(new Compiler("func factorial(n) {\n" +
                "  if n == 0 {\n" +
                "    return 1\n" +
                "  }\n" +
                "  return factorial(n - 1) * n\n" +
                "}\n" +
                "\n" +
                "factorial(5)"));
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator(false);
        try {
            VMWrapper vm = new VMWrapper();
            Object[] bytes = (Object[]) vmByteCodeGenerator.generate(program);
            vm.run(bytes);
            assertEquals(120.0, vm.pop().getData());
        } catch (UnsatisfiedLinkError e) {
            // if VMWrapper was not accessible in testing environment most of the tests will be ignored
            Assume.assumeNoException(e.getMessage(), e);
        }
    }
}
