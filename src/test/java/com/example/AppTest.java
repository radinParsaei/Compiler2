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
                    "CREATE_MAP\tNUM3", vm.disassemble(res).trim());

            vm.run(res);
            assertEquals(v, vm.pop().getData());
        } catch (UnsatisfiedLinkError | NoClassDefFoundError e) {
            Assume.assumeNoException(e.getMessage(), e);
        }
    }
}
