/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.objectweb.asm.commons;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.TraceMethodVisitor;
import junit.framework.TestCase;

/**
 * JsrInlinerTest
 * 
 * @author Eugene Kuleshov, Niko Matsakis
 */
public class JSRInlinerAdapterUnitTest extends TestCase {

    private JSRInlinerAdapter jsr;
    private MethodNode exp;
    private MethodVisitor current;

    protected void setUp() throws Exception {
        super.setUp();
        jsr = new JSRInlinerAdapter(null, 0, "name", "desc", null, null);
        exp = new MethodNode(0, "name", "desc", null, null);
    }

    private void setCurrent(final MethodVisitor cv) {
        this.current = cv;
    }

    private void ICONST_0() {
        this.current.visitInsn(Opcodes.ICONST_0);
    }

    private void ISTORE(final int var) {
        this.current.visitVarInsn(Opcodes.ISTORE, var);
    }

    private void ALOAD(final int var) {
        this.current.visitVarInsn(Opcodes.ALOAD, var);
    }

    private void ILOAD(final int var) {
        this.current.visitVarInsn(Opcodes.ILOAD, var);
    }

    private void ASTORE(final int var) {
        this.current.visitVarInsn(Opcodes.ASTORE, var);
    }

    private void RET(final int var) {
        this.current.visitVarInsn(Opcodes.RET, var);
    }

    private void ATHROW() {
        this.current.visitInsn(Opcodes.ATHROW);
    }

    private void ACONST_NULL() {
        this.current.visitInsn(Opcodes.ACONST_NULL);
    }

    private void RETURN() {
        this.current.visitInsn(Opcodes.RETURN);
    }

    private void LABEL(final Label l) {
        this.current.visitLabel(l);
    }

    private void IINC(final int var, final int amnt) {
        this.current.visitIincInsn(var, amnt);
    }

    private void GOTO(final Label l) {
        this.current.visitJumpInsn(Opcodes.GOTO, l);
    }

    private void JSR(final Label l) {
        this.current.visitJumpInsn(Opcodes.JSR, l);
    }

    private void IFNONNULL(final Label l) {
        this.current.visitJumpInsn(Opcodes.IFNONNULL, l);
    }

    private void IFNE(final Label l) {
        this.current.visitJumpInsn(Opcodes.IFNE, l);
    }

    private void TRYCATCH(
        final Label start,
        final Label end,
        final Label handler)
    {
        this.current.visitTryCatchBlock(start, end, handler, null);
    }

    private void END() {
        this.current.visitEnd();
        this.current = null;
    }

    /**
     * Tests a method which has the most basic <code>try{}finally</code> form
     * imaginable:
     * 
     * <code>
     *   public void a() {
     *     int a = 0;
     *     try {
     *       a++;
     *     } finally {
     *       a--;
     *     }
     *   }
     * </code>
     */
    public void testBasic() {
        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4 = new Label();

            setCurrent(jsr);
            ICONST_0();
            ISTORE(1);

            /* L0: body of try block */
            LABEL(L0);
            IINC(1, 1);
            GOTO(L1);

            /* L2: exception handler */
            LABEL(L2);
            ASTORE(3);
            JSR(L3);
            ALOAD(3);
            ATHROW();

            /* L3: subroutine */
            LABEL(L3);
            ASTORE(2);
            IINC(1, -1);
            RET(2);

            /* L1: non-exceptional exit from try block */
            LABEL(L1);
            JSR(L3);
            LABEL(L4); // L4
            RETURN();

            TRYCATCH(L0, L2, L2);
            TRYCATCH(L1, L4, L2);

            END();
        }

        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3_1a = new Label();
            Label L3_1b = new Label();
            Label L3_2a = new Label();
            Label L3_2b = new Label();
            Label L4 = new Label();

            setCurrent(exp);
            ICONST_0();
            ISTORE(1);
            // L0: try/catch block
            LABEL(L0);
            IINC(1, 1);
            GOTO(L1);

            // L2: Exception handler:
            LABEL(L2);
            ASTORE(3);
            ACONST_NULL();
            GOTO(L3_1a);
            LABEL(L3_1b); // L3_1b;
            ALOAD(3);
            ATHROW();

            // L1: On non-exceptional exit, try block leads here:
            LABEL(L1);
            ACONST_NULL();
            GOTO(L3_2a);
            LABEL(L3_2b); // L3_2b
            LABEL(L4); // L4
            RETURN();

            // L3_1a: First instantiation of subroutine:
            LABEL(L3_1a);
            ASTORE(2);
            IINC(1, -1);
            GOTO(L3_1b);

            // L3_2a: Second instantiation of subroutine:
            LABEL(L3_2a);
            ASTORE(2);
            IINC(1, -1);
            GOTO(L3_2b);

            TRYCATCH(L0, L2, L2);
            TRYCATCH(L1, L4, L2);

            END();
        }

        assertEquals(exp, jsr);
    }

    /**
     * Tests a simple nested finally: <code>
     * public void a1() {
     *   int a = 0;
     *   try {
     *     a += 1;
     *   } finally {
     *     try {
     *       a += 2;
     *     } finally {
     *       a += 3;
     *     }
     *   }
     * }
     * </code>
     */
    public void testSimpleNestedFinally() {
        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4 = new Label();
            Label L5 = new Label();

            setCurrent(jsr);

            ICONST_0();
            ISTORE(1);

            // L0: Body of try block:
            LABEL(L0);
            IINC(1, 1);
            JSR(L3);
            GOTO(L1);

            // L2: First exception handler:
            LABEL(L2);
            JSR(L3);
            ATHROW();

            // L3: First subroutine:
            LABEL(L3);
            ASTORE(2);
            IINC(1, 2);
            JSR(L4);
            RET(2);

            // L5: Second exception handler:
            LABEL(L5);
            JSR(L4);
            ATHROW();

            // L4: Second subroutine:
            LABEL(L4);
            ASTORE(3);
            IINC(1, 3);
            RET(3);

            // L1: On normal exit, try block jumps here:
            LABEL(L1);
            RETURN();

            TRYCATCH(L0, L2, L2);
            TRYCATCH(L3, L5, L5);

            END();
        }

        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3_1a = new Label();
            Label L3_1b = new Label();
            Label L3_2a = new Label();
            Label L3_2b = new Label();
            Label L4_1a = new Label();
            Label L4_1b = new Label();
            Label L4_2a = new Label();
            Label L4_2b = new Label();
            Label L4_3a = new Label();
            Label L4_3b = new Label();
            Label L4_4a = new Label();
            Label L4_4b = new Label();
            Label L5_1 = new Label();
            Label L5_2 = new Label();

            setCurrent(exp);

            ICONST_0();
            ISTORE(1);

            // L0: Body of try block:
            LABEL(L0);
            IINC(1, 1);
            ACONST_NULL();
            GOTO(L3_1a);
            LABEL(L3_1b); // L3_1b
            GOTO(L1);

            // L2: First exception handler:
            LABEL(L2);
            ACONST_NULL();
            GOTO(L3_2a);
            LABEL(L3_2b); // L3_2b
            ATHROW();

            // L1: On normal exit, try block jumps here:
            LABEL(L1);
            RETURN();

            // L3_1a: First instantiation of first subroutine:
            LABEL(L3_1a);
            ASTORE(2);
            IINC(1, 2);
            ACONST_NULL();
            GOTO(L4_1a);
            LABEL(L4_1b); // L4_1b
            GOTO(L3_1b);
            LABEL(L5_1); // L5_1
            ACONST_NULL();
            GOTO(L4_2a);
            LABEL(L4_2b); // L4_2b
            ATHROW();

            // L3_2a: Second instantiation of first subroutine:
            LABEL(L3_2a);
            ASTORE(2);
            IINC(1, 2);
            ACONST_NULL();
            GOTO(L4_3a);
            LABEL(L4_3b); // L4_3b
            GOTO(L3_2b);
            LABEL(L5_2); // L5_2
            ACONST_NULL();
            GOTO(L4_4a);
            LABEL(L4_4b); // L4_4b
            ATHROW();

            // L4_1a: First instantiation of second subroutine:
            LABEL(L4_1a);
            ASTORE(3);
            IINC(1, 3);
            GOTO(L4_1b);

            // L4_2a: Second instantiation of second subroutine:
            LABEL(L4_2a);
            ASTORE(3);
            IINC(1, 3);
            GOTO(L4_2b);

            // L4_3a: Third instantiation of second subroutine:
            LABEL(L4_3a);
            ASTORE(3);
            IINC(1, 3);
            GOTO(L4_3b);

            // L4_4a: Fourth instantiation of second subroutine:
            LABEL(L4_4a);
            ASTORE(3);
            IINC(1, 3);
            GOTO(L4_4b);

            TRYCATCH(L0, L2, L2);
            TRYCATCH(L3_1a, L5_1, L5_1);
            TRYCATCH(L3_2a, L5_2, L5_2);

            END();
        }

        assertEquals(exp, jsr);
    }

    /**
     * This tests a subroutine which has no ret statement, but ends in a
     * "return" instead.
     * 
     * We structure this as a try/finally with a break in the finally. Because
     * the while loop is infinite, it's clear from the byte code that the only
     * path which reaches the RETURN instruction is through the subroutine.
     * 
     * <code>
     * public void a1() {
     *   int a = 0;
     *   while (true) {
     *     try {
     *       a += 1;
     *     } finally {
     *       a += 2;
     *       break;
     *     }
     *   }
     * }
     * </code>
     */
    public void testSubroutineWithNoRet() {
        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4 = new Label();

            setCurrent(jsr);
            ICONST_0();
            ASTORE(1);

            // L0: while loop header/try block
            LABEL(L0);
            IINC(1, 1);
            JSR(L1);
            GOTO(L2);

            // L3: implicit catch block
            LABEL(L3);
            JSR(L1);
            ATHROW();

            // L1: subroutine ...
            LABEL(L1);
            IINC(1, 2);
            GOTO(L4); // ...not that it does not return!

            // L2: end of the loop... goes back to the top!
            LABEL(L2);
            GOTO(L0);

            // L4:
            LABEL(L4);
            RETURN();

            TRYCATCH(L0, L3, L3);

            END();
        }

        {
            Label L0 = new Label();
            Label L1_1a = new Label();
            Label L1_1b = new Label();
            Label L1_2a = new Label();
            Label L1_2b = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4_1 = new Label();
            Label L4_2 = new Label();

            setCurrent(exp);
            ICONST_0();
            ASTORE(1);

            // L0: while loop header/try block
            LABEL(L0);
            IINC(1, 1);
            ACONST_NULL();
            GOTO(L1_1a);
            LABEL(L1_1b); // L1_1b
            GOTO(L2);

            // L3: implicit catch block
            LABEL(L3);
            ACONST_NULL();
            GOTO(L1_2a);
            LABEL(L1_2b); // L1_2b
            ATHROW();

            // L2: end of the loop... goes back to the top!
            LABEL(L2);
            GOTO(L0);

            // L1_1a: first instantiation of subroutine ...
            LABEL(L1_1a);
            IINC(1, 2);
            GOTO(L4_1); // ...not that it does not return!
            LABEL(L4_1);
            RETURN();

            // L1_2a: first instantiation of subroutine ...
            LABEL(L1_2a);
            IINC(1, 2);
            GOTO(L4_2); // ...not that it does not return!
            LABEL(L4_2);
            RETURN();

            TRYCATCH(L0, L3, L3);

            END();
        }

        assertEquals(exp, jsr);
    }

    /**
     * This tests a subroutine which has no ret statement, but instead exits
     * implicitely by branching to code which is not part of the subroutine.
     * (Sadly, this is legal)
     * 
     * We structure this as a try/finally in a loop with a break in the finally.
     * The loop is not trivially infinite, so the RETURN statement is reachable
     * both from the JSR subroutine and from the main entry point.
     * 
     * <code>
     * public void a1() {
     *   int a = 0;
     *   while (null == null) {
     *     try {
     *       a += 1;
     *     } finally {
     *       a += 2;
     *       break;
     *     }
     *   }
     * }
     * </code>
     */
    public void testImplicitExit() {
        {
            Label L0 = new Label();
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4 = new Label();
            Label L5 = new Label();

            setCurrent(jsr);
            ICONST_0();
            ASTORE(1);

            // L5: while loop header
            LABEL(L5);
            ACONST_NULL();
            IFNONNULL(L4);

            // L0: try block
            LABEL(L0);
            IINC(1, 1);
            JSR(L1);
            GOTO(L2);

            // L3: implicit catch block
            LABEL(L3);
            JSR(L1);
            ATHROW();

            // L1: subroutine ...
            LABEL(L1);
            IINC(1, 2);
            GOTO(L4); // ...not that it does not return!

            // L2: end of the loop... goes back to the top!
            LABEL(L2);
            GOTO(L0);

            // L4:
            LABEL(L4);
            RETURN();

            TRYCATCH(L0, L3, L3);

            END();
        }

        {
            Label L0 = new Label();
            Label L1_1a = new Label();
            Label L1_1b = new Label();
            Label L1_2a = new Label();
            Label L1_2b = new Label();
            Label L2 = new Label();
            Label L3 = new Label();
            Label L4 = new Label();
            Label L5 = new Label();

            setCurrent(exp);
            ICONST_0();
            ASTORE(1);

            // L5: while loop header
            LABEL(L5);
            ACONST_NULL();
            IFNONNULL(L4);

            // L0: while loop header/try block
            LABEL(L0);
            IINC(1, 1);
            ACONST_NULL();
            GOTO(L1_1a);
            LABEL(L1_1b); // L1_1b
            GOTO(L2);

            // L3: implicit catch block
            LABEL(L3);
            ACONST_NULL();
            GOTO(L1_2a);
            LABEL(L1_2b); // L1_2b
            ATHROW();

            // L2: end of the loop... goes back to the top!
            LABEL(L2);
            GOTO(L0);

            // L4: exit, not part of subroutine
            // Note that the two subroutine instantiations branch here
            LABEL(L4);
            RETURN();

            // L1_1a: first instantiation of subroutine ...
            LABEL(L1_1a);
            IINC(1, 2);
            GOTO(L4); // ...note that it does not return!

            // L1_2a: first instantiation of subroutine ...
            LABEL(L1_2a);
            IINC(1, 2);
            GOTO(L4); // ...note that it does not return!

            TRYCATCH(L0, L3, L3);

            END();
        }

        assertEquals(exp, jsr);
    }

    /**
     * Tests a nested try/finally with implicit exit from one subroutine to the
     * other subroutine. Equivalent to the following java code:
     * 
     * <code>
     void m(boolean b) {
     try {
     return;
     } finally {
     while (b) {
     try {
     return;
     } finally {
     // NOTE --- this break avoids the second return above (weird)
     if (b) break;
     }
     }
     }
     }
     </code>
     * 
     * This example is from the paper, "Subroutine Inlining and Bytecode
     * Abstraction to Simplify Static and Dynamic Analysis" by Cyrille Artho and
     * Armin Biere.
     */
    public void testImplicitExitToAnotherSubroutine() {
        {
            Label T1 = new Label();
            Label C1 = new Label();
            Label S1 = new Label();
            Label L = new Label();
            Label C2 = new Label();
            Label S2 = new Label();
            Label W = new Label();
            Label X = new Label();

            // variable numbers:
            int b = 1;
            int e1 = 2;
            int e2 = 3;
            int r1 = 4;
            int r2 = 5;

            setCurrent(jsr);

            // T1: first try:
            LABEL(T1);
            JSR(S1);
            RETURN();

            // C1: exception handler for first try
            LABEL(C1);
            ASTORE(e1);
            JSR(S1);
            ALOAD(e1);
            ATHROW();

            // S1: first finally handler
            LABEL(S1);
            ASTORE(r1);
            GOTO(W);

            // L: body of while loop, also second try
            LABEL(L);
            JSR(S2);
            RETURN();

            // C2: exception handler for second try
            LABEL(C2);
            ASTORE(e2);
            JSR(S2);
            ALOAD(e2);
            ATHROW();

            // S2: second finally handler
            LABEL(S2);
            ASTORE(r2);
            ILOAD(b);
            IFNE(X);
            RET(r2);

            // W: test for the while loop
            LABEL(W);
            ILOAD(b);
            IFNE(L); // falls through to X

            // X: exit from finally{} block
            LABEL(X);
            RET(r1);

            TRYCATCH(T1, C1, C1);
            TRYCATCH(L, C2, C2);

            END();
        }

        {
            Label T1 = new Label();
            Label C1 = new Label();
            Label S1_1a = new Label();
            Label S1_1b = new Label();
            Label S1_2a = new Label();
            Label S1_2b = new Label();
            Label L_1 = new Label();
            Label L_2 = new Label();
            Label C2_1 = new Label();
            Label C2_2 = new Label();
            Label S2_1_1a = new Label();
            Label S2_1_1b = new Label();
            Label S2_1_2a = new Label();
            Label S2_1_2b = new Label();
            Label S2_2_1a = new Label();
            Label S2_2_1b = new Label();
            Label S2_2_2a = new Label();
            Label S2_2_2b = new Label();
            Label W_1 = new Label();
            Label W_2 = new Label();
            Label X_1 = new Label();
            Label X_2 = new Label();

            // variable numbers:
            int b = 1;
            int e1 = 2;
            int e2 = 3;
            int r1 = 4;
            int r2 = 5;

            setCurrent(exp);

            // --- Main Subroutine ---

            // T1: first try:
            LABEL(T1);
            ACONST_NULL();
            GOTO(S1_1a);
            LABEL(S1_1b);
            RETURN();

            // C1: exception handler for first try
            LABEL(C1);
            ASTORE(e1);
            ACONST_NULL();
            GOTO(S1_2a);
            LABEL(S1_2b);
            ALOAD(e1);
            ATHROW();

            // --- First instantiation of first subroutine ---

            // S1: first finally handler
            LABEL(S1_1a);
            ASTORE(r1);
            GOTO(W_1);

            // L_1: body of while loop, also second try
            LABEL(L_1);
            ACONST_NULL();
            GOTO(S2_1_1a);
            LABEL(S2_1_1b);
            RETURN();

            // C2_1: exception handler for second try
            LABEL(C2_1);
            ASTORE(e2);
            ACONST_NULL();
            GOTO(S2_1_2a);
            LABEL(S2_1_2b);
            ALOAD(e2);
            ATHROW();

            // W_1: test for the while loop
            LABEL(W_1);
            ILOAD(b);
            IFNE(L_1); // falls through to X_1

            // X_1: exit from finally{} block
            LABEL(X_1);
            GOTO(S1_1b);

            // --- First instantiation of first subroutine ---

            // S1: first finally handler
            LABEL(S1_2a);
            ASTORE(r1);
            GOTO(W_2);

            // L_2: body of while loop, also second try
            LABEL(L_2);
            ACONST_NULL();
            GOTO(S2_2_1a);
            LABEL(S2_2_1b);
            RETURN();

            // C2_2: exception handler for second try
            LABEL(C2_2);
            ASTORE(e2);
            ACONST_NULL();
            GOTO(S2_2_2a);
            LABEL(S2_2_2b);
            ALOAD(e2);
            ATHROW();

            // W_2: test for the while loop
            LABEL(W_2);
            ILOAD(b);
            IFNE(L_2); // falls through to X_2

            // X_2: exit from finally{} block
            LABEL(X_2);
            GOTO(S1_2b);

            // --- Second subroutine's 4 instantiations ---

            // S2_1_1a:
            LABEL(S2_1_1a);
            ASTORE(r2);
            ILOAD(b);
            IFNE(X_1);
            GOTO(S2_1_1b);

            // S2_1_2a:
            LABEL(S2_1_2a);
            ASTORE(r2);
            ILOAD(b);
            IFNE(X_1);
            GOTO(S2_1_2b);

            // S2_2_1a:
            LABEL(S2_2_1a);
            ASTORE(r2);
            ILOAD(b);
            IFNE(X_2);
            GOTO(S2_2_1b);

            // S2_2_2a:
            LABEL(S2_2_2a);
            ASTORE(r2);
            ILOAD(b);
            IFNE(X_2);
            GOTO(S2_2_2b);

            TRYCATCH(T1, C1, C1);
            TRYCATCH(L_1, C2_1, C2_1); // duplicated try/finally for each...
            TRYCATCH(L_2, C2_2, C2_2); // ...instantiation of first sub

            END();
        }

        assertEquals(exp, jsr);
    }

    /**
     * This tests two subroutines, neither of which exit. Instead, they both
     * branch to a common set of code which returns from the method. This code
     * is not reachable except through these subroutines, and since they do not
     * invoke each other, it must be copied into both of them.
     * 
     * I don't believe this can be represented in Java.
     */
    public void testCommonCodeWhichMustBeDuplicated() {
        {
            Label L1 = new Label();
            Label L2 = new Label();
            Label L3 = new Label();

            setCurrent(jsr);
            ICONST_0();
            ASTORE(1);

            // Invoke the two subroutines, each twice:
            JSR(L1);
            JSR(L1);
            JSR(L2);
            JSR(L2);
            RETURN();

            // L1: subroutine 1
            LABEL(L1);
            IINC(1, 1);
            GOTO(L3); // ...note that it does not return!

            // L2: subroutine 2
            LABEL(L2);
            IINC(1, 2);
            GOTO(L3); // ...note that it does not return!

            // L3: common code to both subroutines: exit method
            LABEL(L3);
            RETURN();

            END();
        }

        {
            Label L1_1a = new Label();
            Label L1_1b = new Label();
            Label L1_2a = new Label();
            Label L1_2b = new Label();
            Label L2_1a = new Label();
            Label L2_1b = new Label();
            Label L2_2a = new Label();
            Label L2_2b = new Label();
            Label L3_1 = new Label();
            Label L3_2 = new Label();
            Label L3_3 = new Label();
            Label L3_4 = new Label();

            setCurrent(exp);
            ICONST_0();
            ASTORE(1);

            // Invoke the two subroutines, each twice:
            ACONST_NULL();
            GOTO(L1_1a);
            LABEL(L1_1b);
            ACONST_NULL();
            GOTO(L1_2a);
            LABEL(L1_2b);
            ACONST_NULL();
            GOTO(L2_1a);
            LABEL(L2_1b);
            ACONST_NULL();
            GOTO(L2_2a);
            LABEL(L2_2b);
            RETURN();

            // L1_1a: instantiation 1 of subroutine 1
            LABEL(L1_1a);
            IINC(1, 1);
            GOTO(L3_1); // ...note that it does not return!
            LABEL(L3_1);
            RETURN();

            // L1_2a: instantiation 2 of subroutine 1
            LABEL(L1_2a);
            IINC(1, 1);
            GOTO(L3_2); // ...note that it does not return!
            LABEL(L3_2);
            RETURN();

            // L2_1a: instantiation 1 of subroutine 2
            LABEL(L2_1a);
            IINC(1, 2);
            GOTO(L3_3); // ...note that it does not return!
            LABEL(L3_3);
            RETURN();

            // L2_2a: instantiation 2 of subroutine 2
            LABEL(L2_2a);
            IINC(1, 2);
            GOTO(L3_4); // ...note that it does not return!
            LABEL(L3_4);
            RETURN();

            END();
        }

        assertEquals(exp, jsr);
    }

    public void assertEquals(final MethodNode exp, final MethodNode actual) {
        String textexp = getText(exp);
        String textact = getText(actual);
        System.err.println("Expected=" + textexp);
        System.err.println("Actual=" + textact);
        assertEquals(textexp, textact);
    }

    private String getText(final MethodNode mn) {
        TraceMethodVisitor tmv = new TraceMethodVisitor(null);
        mn.accept(tmv);

        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < tmv.text.size(); i++) {
            sb.append(tmv.text.get(i));
        }
        return sb.toString();
    }
}
