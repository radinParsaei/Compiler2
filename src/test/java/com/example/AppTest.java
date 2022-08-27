package com.example;

import org.junit.Assume;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * Unit test for the compiler.
 */
public class AppTest 
{
    /**
     * Test VMWrapper
     */
    @Test
    public void testVMWrapper() {
        try {
            VMWrapper vm = new VMWrapper();
            vm.run(VMWrapper.PUT, 1);
            assertEquals(1.0, vm.pop().getData());
        } catch (UnsatisfiedLinkError e) {
            // if VMWrapper was not accessible in testing environment most of the tests will be ignored
            Assume.assumeNoException(e.getMessage(), e);
        }
    }

    /**
     * Test basic syntaxTree and generator features
     */
    @Test
    public void testCreateText() {
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator();
        Object[] res = (Object[]) new SyntaxTree.Text("Hello").evaluate(vmByteCodeGenerator);
        assertEquals(res[0], VMWrapper.PUT);
        assertEquals(res[1], "Hello");
        try {
            VMWrapper vm = new VMWrapper();
            vm.run(res);
            assertEquals("Hello", vm.pop().getData());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Assume.assumeNoException(e.getMessage(), e);
        }
    }

    /**
     * Create a map and test different types
     */
    @Test
    public void testMap() {
        VMByteCodeGenerator vmByteCodeGenerator = new VMByteCodeGenerator();

        HashMap<SyntaxTree.Value, SyntaxTree.Value> v = new HashMap<>();
        v.put(new SyntaxTree.Text("ten"), new SyntaxTree.Number(10));
        v.put(new SyntaxTree.Number(10), new SyntaxTree.Text("ten!"));
        v.put(new SyntaxTree.Null(), new SyntaxTree.Text("null"));
        v.put(new SyntaxTree.Null(), new SyntaxTree.Text("null!"));

        Object[] res = (Object[]) new SyntaxTree.Map(v).evaluate(vmByteCodeGenerator);
        assertArrayEquals(res, new Object[] {
                VMWrapper.PUT, null, VMWrapper.PUT, "null!", VMWrapper.PUT, 10.0, VMWrapper.PUT, "ten!",
                VMWrapper.PUT, "ten", VMWrapper.PUT, 10.0, VMWrapper.CREATE_MAP, 3
        });
        try {
            VMWrapper vm = new VMWrapper();

            assertEquals("PUT\tnull\n" +
                    "PUT\tTXTnull!\n" +
                    "PUT\tNUM10\n" +
                    "PUT\tTXTten!\n" +
                    "PUT\tTXTten\n" +
                    "PUT\tNUM10\n" +
                    "CREATE_MAP\tNUM3", VMWrapper.disassemble(res).trim());

            vm.run(res);
            assertEquals(v, vm.pop().getData());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Assume.assumeNoException(e.getMessage(), e);
        }
    }
    /**
     * Test if condition
     */
    @Test
    public void testIf() {
        Object[] simpleIf = (Object[]) new SyntaxTree.If(
                new SyntaxTree.Boolean(true),
                new SyntaxTree.Print(new SyntaxTree.Text("Hello"))
        ).evaluate(new VMByteCodeGenerator());
        assertArrayEquals(new Object[] {
                VMWrapper.PUT, true, VMWrapper.SKIPIFN, 2, VMWrapper.PUT, "Hello", VMWrapper.CALLFUNC, null
        }, simpleIf);
        // VM assembly:
        // PUT          BOOLTrue
        // SKIPIFN      NUM2
        // PUT          TXTHello
        // CALLFUNC     null
    }

    /**
     * Test if-else condition
     */
    @Test
    public void testIfElse() {
        Object[] ifElse = (Object[]) new SyntaxTree.If(
                new SyntaxTree.Boolean(true),
                new SyntaxTree.Print(new SyntaxTree.Text("Hello"))
        )
                .setElseCode(new SyntaxTree.Print(new SyntaxTree.Text("Oops.")))
                .evaluate(new VMByteCodeGenerator());
        assertArrayEquals(new Object[] {
                VMWrapper.PUT, true, VMWrapper.SKIPIFN, 3, VMWrapper.PUT, "Hello", VMWrapper.CALLFUNC, null,
                VMWrapper.SKIP, 2, VMWrapper.PUT, "Oops.", VMWrapper.CALLFUNC, null
        }, ifElse);
        // VM assembly:
        // PUT          BOOLTrue
        // SKIPIFN      NUM3
        // PUT          TXTHello
        // CALLFUNC     null
        // SKIP         NUM2
        // PUT          TXTOops.
        // CALLFUNC     null
    }

    /**
     * Test if variable is freed at the end of the scope
     */
    @Test
    public void testNamespace() {
        Object[] ifElse = (Object[]) new VMByteCodeGenerator().generate(new SyntaxTree.If(
                new SyntaxTree.Boolean(true),
                new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Hello")).setDeclaration(true),
                new SyntaxTree.Print(new SyntaxTree.Variable("msg"))
        )
                .setElseCode(
                        new SyntaxTree.SetVariable("msg", new SyntaxTree.Text("Oops.")).setDeclaration(true),
                        new SyntaxTree.Print(new SyntaxTree.Variable("msg"))
                )
        );
        assertArrayEquals(new Object[] {
                VMWrapper.PUT, true, VMWrapper.SKIPIFN, 6, VMWrapper.PUT, "Hello", VMWrapper.SETVAR, 0,
                VMWrapper.GETVAR, 0, VMWrapper.CALLFUNC, null, VMWrapper.DELVAR, 0, VMWrapper.SKIP, 5,
                VMWrapper.PUT, "Oops.", VMWrapper.SETVAR, 0, VMWrapper.GETVAR, 0, VMWrapper.CALLFUNC, null,
                VMWrapper.DELVAR, 0
        }, ifElse);
        // PUT          BOOLTrue
        // SKIPIFN      NUM6
        // PUT          TXTHello
        // SETVAR       NUM0
        // GETVAR       NUM0
        // CALLFUNC     null
        // DELVAR       NUM0
        // SKIP         NUM5
        // PUT          TXTOops.
        // SETVAR       NUM0
        // GETVAR       NUM0
        // CALLFUNC     null
        // DELVAR       NUM0
    }

    /**
     * Test if scopes are working correctly with if and else
     * (Make sure the variables in the if statement do not cross over into the else statement.)
     */
    @Test
    public void testIfElseScopes() {
        try {
            // var a = 30
            // if true {
            //     a                        <- insert the variable into the stack
            //     var a = 10
            //     var b = 10
            //     if true {
            //         b = 20
            //         var a = 40
            //         a
            //         b
            //     }
            //     b
            // } else {
            //     a
            // }
            // a
            VMWrapper vm = new VMWrapper();
            Object[] program = (Object[]) new VMByteCodeGenerator().generate(new SyntaxTree.Blocks(
                    new SyntaxTree.SetVariable("a", new SyntaxTree.Number(30)).setDeclaration(true),
                    new SyntaxTree.If(new SyntaxTree.Boolean(true),
                            new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("a")),
                            new SyntaxTree.SetVariable("a", new SyntaxTree.Number(10)).setDeclaration(true),
                            new SyntaxTree.SetVariable("b", new SyntaxTree.Number(10)).setDeclaration(true),
                            new SyntaxTree.If(new SyntaxTree.Boolean(true),
                                    new SyntaxTree.SetVariable("b", new SyntaxTree.Number(20)),
                                    new SyntaxTree.SetVariable("a", new SyntaxTree.Number(40)).setDeclaration(true),
                                    new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("a")),
                                    new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("b"))
                            ),
                            new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("b"))
                    ).setElseCode(new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("a"))),
                    new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("a"))
            ));
            vm.run(program);
            assertEquals(new SyntaxTree.Number(30), vm.pop());
            assertEquals(new SyntaxTree.Number(20), vm.pop());
            assertEquals(new SyntaxTree.Number(20), vm.pop());
            assertEquals(new SyntaxTree.Number(40), vm.pop());
            assertEquals(new SyntaxTree.Number(30), vm.pop());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Assume.assumeNoException(e.getMessage(), e);
        }
    }

    /**
     * Test while loop
     */
    @Test
    public void testWhile() {
        /*
            var i = 0
            while i < 10 {
                i                   <- insert the variable into the stack
                i += 1
            }
        */
        // stack should be like [1, 2, 3, 4, 5, 6, 7, 8, 9]
        Object[] program = (Object[]) new VMByteCodeGenerator().generate(
                new SyntaxTree.Blocks(
                        new SyntaxTree.SetVariable("i", new SyntaxTree.Number(0)).setDeclaration(true),
                        new SyntaxTree.While(
                                new SyntaxTree.LesserThan(new SyntaxTree.Variable("i"), new SyntaxTree.Number(10)),
                                new SyntaxTree.ValueAsProgram(new SyntaxTree.Variable("i")),
                                new SyntaxTree.SetVariable("i",
                                        new SyntaxTree.Add(new SyntaxTree.Variable("i"), new SyntaxTree.Number(1)))
                        )
                )
        );
        assertArrayEquals(new Object[] {
                VMWrapper.PUT, 0.0, VMWrapper.SETVAR, "i", VMWrapper.REC, VMWrapper.GETVAR, "i", VMWrapper.GETVAR, "i",
                VMWrapper.PUT, 1.0, VMWrapper.ADD, VMWrapper.SETVAR, "i", VMWrapper.END, VMWrapper.REC,
                VMWrapper.GETVAR, "i", VMWrapper.PUT, 10.0, VMWrapper.LT, VMWrapper.END, VMWrapper.WHILE
        }, program);
    }

    /**
     * test nested while loop
     */
    @Test
    public void testNestedWhile() {
        /*
            var i = 0
            while i < 10 {
                while i % 2 != 0 {
                    i += 1
                }
                i                   <- insert the variable into the stack
                i += 1
            }
        */
        // stack should be like [1, 2, 3, 4, 5, 6, 7, 8, 9]
        Object[] program = (Object[]) new VMByteCodeGenerator().generate(
                new SyntaxTree.Blocks(
                        new SyntaxTree.SetVariable("i", new SyntaxTree.Number(0)).setDeclaration(true),
                        new SyntaxTree.While(
                                new SyntaxTree.LesserThan(new SyntaxTree.Variable("i"), new SyntaxTree.Number(10)),
                                new SyntaxTree.While(new SyntaxTree.NotEquals(new SyntaxTree.Mod(
                                        new SyntaxTree.Variable("i"), new SyntaxTree.Number(2)),
                                        new SyntaxTree.Number(0)),
                                            new SyntaxTree.SetVariable("i",
                                                    new SyntaxTree.Add(new SyntaxTree.Variable("i"),
                                                            new SyntaxTree.Number(1)))
                                        ),
                                new SyntaxTree.Print(new SyntaxTree.Variable("i")),
                                new SyntaxTree.SetVariable("i",
                                        new SyntaxTree.Add(new SyntaxTree.Variable("i"), new SyntaxTree.Number(1)))
                        )
                )
        );
        assertArrayEquals(new Object[] {
                VMWrapper.PUT, 0.0, VMWrapper.SETVAR, "i", VMWrapper.REC, VMWrapper.SKIP, 7, VMWrapper.GETVAR, "i",
                VMWrapper.PUT, 1.0, VMWrapper.ADD, VMWrapper.SETVAR, "i", VMWrapper.GETVAR, "i", VMWrapper.PUT, 2.0,
                VMWrapper.MOD, VMWrapper.PUT, 0.0, VMWrapper.NEQ, VMWrapper.SKIPIF, -17, VMWrapper.GETVAR, "i",
                VMWrapper.CALLFUNC, null, VMWrapper.GETVAR, "i", VMWrapper.PUT, 1.0, VMWrapper.ADD,
                VMWrapper.SETVAR, "i", VMWrapper.END, VMWrapper.REC, VMWrapper.GETVAR, "i", VMWrapper.PUT, 10.0,
                VMWrapper.LT, VMWrapper.END, VMWrapper.WHILE
        }, program);
    }
}
