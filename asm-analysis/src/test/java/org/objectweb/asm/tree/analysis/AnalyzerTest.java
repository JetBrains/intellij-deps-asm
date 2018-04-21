// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.tree.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

/**
 * Analyzer tests.
 *
 * @author Eric Bruneton
 */
public class AnalyzerTest {

  protected ClassWriter classWriter;

  protected MethodVisitor methodVisitor;

  private Label startLabel;

  @BeforeEach
  public void setUp() throws Exception {
    classWriter = new ClassWriter(0);
    classWriter.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
    methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitInsn(Opcodes.RETURN);
    methodVisitor.visitMaxs(1, 1);
    methodVisitor.visitEnd();
    methodVisitor = classWriter.visitMethod(Opcodes.ACC_PUBLIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    startLabel = new Label();
    LABEL(startLabel);
  }

  private void NOP() {
    methodVisitor.visitInsn(Opcodes.NOP);
  }

  private void PUSH() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void POP() {
    methodVisitor.visitInsn(Opcodes.POP);
  }

  private void ICONST_0() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void ISTORE(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ISTORE, var);
  }

  private void ALOAD(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ALOAD, var);
  }

  private void ILOAD(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ILOAD, var);
  }

  private void ASTORE(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ASTORE, var);
  }

  private void RET(final int var) {
    methodVisitor.visitVarInsn(Opcodes.RET, var);
  }

  private void ATHROW() {
    methodVisitor.visitInsn(Opcodes.ATHROW);
  }

  private void ACONST_NULL() {
    methodVisitor.visitInsn(Opcodes.ACONST_NULL);
  }

  private void RETURN() {
    methodVisitor.visitInsn(Opcodes.RETURN);
  }

  private void LABEL(final Label l) {
    methodVisitor.visitLabel(l);
  }

  private void IINC(final int var, final int amnt) {
    methodVisitor.visitIincInsn(var, amnt);
  }

  private void GOTO(final Label l) {
    methodVisitor.visitJumpInsn(Opcodes.GOTO, l);
  }

  private void JSR(final Label l) {
    methodVisitor.visitJumpInsn(Opcodes.JSR, l);
  }

  private void IFNONNULL(final Label l) {
    methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, l);
  }

  private void IFNE(final Label l) {
    methodVisitor.visitJumpInsn(Opcodes.IFNE, l);
  }

  private void TRYCATCH(final Label start, final Label end, final Label handler) {
    methodVisitor.visitTryCatchBlock(start, end, handler, null);
  }

  protected static class TestClassLoader extends ClassLoader {

    public TestClassLoader() {}

    public Class<?> defineClass(final String name, final byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }

  /**
   * Tests a method which has the most basic <code>try{}finally</code> form imaginable:
   *
   * <pre>
   * public void a() {
   *     int a = 0;
   *     try {
   *         a++;
   *     } finally {
   *         a--;
   *     }
   * }
   * </pre>
   */
  @Test
  public void testBasic() {
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();

    ICONST_0();
    ISTORE(1);

    /* L0: body of try block. */
    LABEL(L0);
    IINC(1, 1);
    GOTO(L1);

    /* L2: exception handler. */
    LABEL(L2);
    ASTORE(3);
    JSR(L3);
    ALOAD(3);
    ATHROW();

    /* L3: subroutine. */
    LABEL(L3);
    ASTORE(2);
    IINC(1, -1);
    PUSH();
    PUSH();
    RET(2);

    /* L1: non-exceptional exit from try block. */
    LABEL(L1);
    JSR(L3);
    PUSH();
    PUSH();
    LABEL(L4);
    RETURN();

    TRYCATCH(L0, L2, L2);
    TRYCATCH(L1, L4, L2);

    assertMaxs(4, 4);
  }

  /**
   * Tests a method which has an if/else-if w/in the finally clause:
   *
   * <pre>
   * public void a() {
   *     int a = 0;
   *     try {
   *         a++;
   *     } finally {
   *         if (a == 0)
   *             a += 2;
   *         else
   *             a += 3;
   *     }
   * }
   * </pre>
   */
  @Test
  public void testIfElseInFinally() {
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();
    Label L5 = new Label();
    Label L6 = new Label();

    ICONST_0();
    ISTORE(1);

    /* L0: body of try block. */
    LABEL(L0);
    IINC(1, 1);
    GOTO(L1);

    /* L2: exception handler. */
    LABEL(L2);
    ASTORE(3);
    JSR(L3);
    PUSH();
    PUSH();
    ALOAD(3);
    ATHROW();

    /* L3: subroutine. */
    LABEL(L3);
    ASTORE(2);
    PUSH();
    PUSH();
    ILOAD(1);
    IFNE(L4);
    IINC(1, 2);
    GOTO(L5);

    LABEL(L4);
    IINC(1, 3);

    LABEL(L5);
    RET(2);

    /* L1: non-exceptional exit from try block. */
    LABEL(L1);
    JSR(L3);
    LABEL(L6);
    RETURN();

    TRYCATCH(L0, L2, L2);
    TRYCATCH(L1, L6, L2);

    assertMaxs(5, 4);
  }

  /**
   * Tests a simple nested finally:
   *
   * <pre>
   * public void a1() {
   *     int a = 0;
   *     try {
   *         a += 1;
   *     } finally {
   *         try {
   *             a += 2;
   *         } finally {
   *             a += 3;
   *         }
   *     }
   * }
   * </pre>
   */
  @Test
  public void testSimpleNestedFinally() {
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();
    Label L5 = new Label();

    ICONST_0();
    ISTORE(1);

    // L0: Body of try block.
    LABEL(L0);
    IINC(1, 1);
    JSR(L3);
    GOTO(L1);

    // L2: First exception handler.
    LABEL(L2);
    ASTORE(4);
    JSR(L3);
    ALOAD(4);
    ATHROW();

    // L3: First subroutine.
    LABEL(L3);
    ASTORE(2);
    IINC(1, 2);
    JSR(L4);
    PUSH();
    PUSH();
    RET(2);

    // L5: Second exception handler.
    LABEL(L5);
    ASTORE(5);
    JSR(L4);
    ALOAD(5);
    ATHROW();

    // L4: Second subroutine.
    LABEL(L4);
    ASTORE(3);
    PUSH();
    PUSH();
    IINC(1, 3);
    RET(3);

    // L1: On normal exit, try block jumps here.
    LABEL(L1);
    RETURN();

    TRYCATCH(L0, L2, L2);
    TRYCATCH(L3, L5, L5);

    assertMaxs(5, 6);
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead.
   *
   * <p>We structure this as a try/finally with a break in the finally. Because the while loop is
   * infinite, it's clear from the byte code that the only path which reaches the RETURN instruction
   * is through the subroutine.
   *
   * <pre>
   * public void a1() {
   *     int a = 0;
   *     while (true) {
   *         try {
   *             a += 1;
   *         } finally {
   *             a += 2;
   *             break;
   *         }
   *     }
   * }
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet() {
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();

    ICONST_0();
    ISTORE(1);

    // L0: while loop header/try block.
    LABEL(L0);
    IINC(1, 1);
    JSR(L1);
    GOTO(L2);

    // L3: implicit catch block.
    LABEL(L3);
    ASTORE(2);
    JSR(L1);
    PUSH();
    PUSH();
    ALOAD(2);
    ATHROW();

    // L1: subroutine which does not return.
    LABEL(L1);
    ASTORE(3);
    IINC(1, 2);
    GOTO(L4);

    // L2: end of the loop, goes back to the top.
    LABEL(L2);
    GOTO(L0);

    // L4:
    LABEL(L4);
    RETURN();

    TRYCATCH(L0, L3, L3);

    assertMaxs(1, 4);
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead.
   *
   * <pre>
   *   ACONST_NULL
   *   JSR L0
   * L0:
   *   ASTORE 0
   *   ASTORE 0
   *   RETURN
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet2() {
    Label L0 = new Label();
    Label L1 = new Label();

    ACONST_NULL();
    JSR(L0);
    NOP();
    LABEL(L0);
    ASTORE(0);
    ASTORE(0);
    RETURN();
    LABEL(L1);
    methodVisitor.visitLocalVariable("i", "I", null, L0, L1, 1);

    assertMaxs(2, 2);
  }

  /**
   * This tests a subroutine which has no ret statement, but instead exits implicitely by branching
   * to code which is not part of the subroutine. (Sadly, this is legal)
   *
   * <p>We structure this as a try/finally in a loop with a break in the finally. The loop is not
   * trivially infinite, so the RETURN statement is reachable both from the JSR subroutine and from
   * the main entry point.
   *
   * <pre>
   * public void a1() {
   *     int a = 0;
   *     while (null == null) {
   *         try {
   *             a += 1;
   *         } finally {
   *             a += 2;
   *             break;
   *         }
   *     }
   * }
   * </pre>
   */
  @Test
  public void testImplicitExit() {
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();
    Label L5 = new Label();

    ICONST_0();
    ISTORE(1);

    // L5: while loop header.
    LABEL(L5);
    ACONST_NULL();
    IFNONNULL(L4);

    // L0: try block.
    LABEL(L0);
    IINC(1, 1);
    JSR(L1);
    GOTO(L2);

    // L3: implicit catch block.
    LABEL(L3);
    ASTORE(2);
    JSR(L1);
    ALOAD(2);
    PUSH();
    PUSH();
    ATHROW();

    // L1: subroutine which does not return.
    LABEL(L1);
    ASTORE(3);
    IINC(1, 2);
    GOTO(L4);

    // L2: end of the loop, goes back to the top.
    LABEL(L2);
    GOTO(L0);

    // L4:
    LABEL(L4);
    RETURN();

    TRYCATCH(L0, L3, L3);

    assertMaxs(1, 4);
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine.
   * Equivalent to the following java code:
   *
   * <pre>
   * void m(boolean b) {
   *     try {
   *         return;
   *     } finally {
   *         while (b) {
   *             try {
   *                 return;
   *             } finally {
   *                 // NOTE --- this break avoids the second return above (weird)
   *                 if (b)
   *                     break;
   *             }
   *         }
   *     }
   * }
   * </pre>
   *
   * This example is from the paper, "Subroutine Inlining and Bytecode Abstraction to Simplify
   * Static and Dynamic Analysis" by Cyrille Artho and Armin Biere.
   */
  @Test
  public void testImplicitExitToAnotherSubroutine() {
    Label T1 = new Label();
    Label C1 = new Label();
    Label S1 = new Label();
    Label L = new Label();
    Label C2 = new Label();
    Label S2 = new Label();
    Label W = new Label();
    Label X = new Label();

    // Variable numbers:
    int b = 1;
    int e1 = 2;
    int e2 = 3;
    int r1 = 4;
    int r2 = 5;

    ICONST_0();
    ISTORE(1);

    // T1: first try.
    LABEL(T1);
    JSR(S1);
    RETURN();

    // C1: exception handler for first try.
    LABEL(C1);
    ASTORE(e1);
    JSR(S1);
    PUSH();
    PUSH();
    ALOAD(e1);
    ATHROW();

    // S1: first finally handler.
    LABEL(S1);
    ASTORE(r1);
    PUSH();
    PUSH();
    GOTO(W);

    // L: body of while loop, also second try.
    LABEL(L);
    JSR(S2);
    RETURN();

    // C2: exception handler for second try.
    LABEL(C2);
    ASTORE(e2);
    PUSH();
    PUSH();
    JSR(S2);
    ALOAD(e2);
    ATHROW();

    // S2: second finally handler.
    LABEL(S2);
    ASTORE(r2);
    ILOAD(b);
    IFNE(X);
    RET(r2);

    // W: test for the while loop.
    LABEL(W);
    ILOAD(b);
    IFNE(L); // falls through to X.

    // X: exit from finally{} block.
    LABEL(X);
    RET(r1);

    TRYCATCH(T1, C1, C1);
    TRYCATCH(L, C2, C2);

    assertMaxs(5, 6);
  }

  @Test
  public void testImplicitExitToAnotherSubroutine2() {
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();

    ICONST_0();
    ISTORE(1);
    JSR(L1);
    RETURN();

    LABEL(L1);
    ASTORE(2);
    JSR(L2);
    GOTO(L3);

    LABEL(L2);
    ASTORE(3);
    ILOAD(1);
    IFNE(L3);
    RET(3);

    LABEL(L3);
    RET(2);

    assertMaxs(1, 4);
  }

  /**
   * This tests a simple subroutine where the control flow jumps back and forth between the
   * subroutine and the caller.
   *
   * <p>This would not normally be produced by a java compiler.
   */
  @Test
  public void testInterleavedCode() {
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();

    ICONST_0();
    ISTORE(1);
    JSR(L1);
    GOTO(L2);

    // L1: subroutine 1.
    LABEL(L1);
    ASTORE(2);
    IINC(1, 1);
    GOTO(L3);

    // L2: second part of main subroutine.
    LABEL(L2);
    IINC(1, 2);
    GOTO(L4);

    // L3: second part of subroutine 1.
    LABEL(L3);
    IINC(1, 4);
    PUSH();
    PUSH();
    RET(2);

    // L4: third part of main subroutine.
    LABEL(L4);
    PUSH();
    PUSH();
    RETURN();

    assertMaxs(4, 3);
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine, and
   * with a surrounding try/catch thrown in the mix. Equivalent to the following java code:
   *
   * <pre>
   * void m(int b) {
   *     try {
   *         try {
   *             return;
   *         } finally {
   *             while (b) {
   *                 try {
   *                     return;
   *                 } finally {
   *                     // NOTE --- this break avoids the second return above
   *                     // (weird)
   *                     if (b)
   *                         break;
   *                 }
   *             }
   *         }
   *     } catch (Exception e) {
   *         b += 3;
   *         return;
   *     }
   * }
   * </pre>
   */
  @Test
  public void testImplicitExitInTryCatch() {
    Label T1 = new Label();
    Label C1 = new Label();
    Label S1 = new Label();
    Label L = new Label();
    Label C2 = new Label();
    Label S2 = new Label();
    Label W = new Label();
    Label X = new Label();
    Label OC = new Label();

    // Variable numbers.
    int b = 1;
    int e1 = 2;
    int e2 = 3;
    int r1 = 4;
    int r2 = 5;

    ICONST_0();
    ISTORE(1);

    // T1: first try.
    LABEL(T1);
    JSR(S1);
    RETURN();

    // C1: exception handler for first try.
    LABEL(C1);
    ASTORE(e1);
    JSR(S1);
    ALOAD(e1);
    ATHROW();

    // S1: first finally handler.
    LABEL(S1);
    ASTORE(r1);
    GOTO(W);

    // L: body of while loop, also second try.
    LABEL(L);
    JSR(S2);
    PUSH();
    PUSH();
    RETURN();

    // C2: exception handler for second try.
    LABEL(C2);
    ASTORE(e2);
    JSR(S2);
    ALOAD(e2);
    ATHROW();

    // S2: second finally handler.
    LABEL(S2);
    ASTORE(r2);
    ILOAD(b);
    IFNE(X);
    PUSH();
    PUSH();
    RET(r2);

    // W: test for the while loop.
    LABEL(W);
    ILOAD(b);
    IFNE(L); // falls through to X.

    // X: exit from finally{} block.
    LABEL(X);
    RET(r1);

    // OC: outermost catch.
    LABEL(OC);
    IINC(b, 3);
    RETURN();

    TRYCATCH(T1, C1, C1);
    TRYCATCH(L, C2, C2);
    TRYCATCH(T1, OC, OC);

    assertMaxs(4, 6);
  }

  /**
   * Tests an example coming from distilled down version of
   * com/sun/corba/ee/impl/protocol/CorbaClientDelegateImpl from GlassFish 2. See issue #317823.
   */
  @Test
  public void testGlassFish2CorbaClientDelegateImplExample() {
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();
    Label l6 = new Label();
    Label l7 = new Label();
    Label l8 = new Label();
    Label l9 = new Label();
    Label l10 = new Label();
    Label l11 = new Label();
    Label l12 = new Label();

    LABEL(l0);
    JSR(l9);
    LABEL(l1);
    GOTO(l10);
    LABEL(l2);
    POP();
    JSR(l9);
    LABEL(l3);
    ACONST_NULL();
    ATHROW();
    LABEL(l9);
    ASTORE(1);
    RET(1);
    LABEL(l10);
    ACONST_NULL();
    ACONST_NULL();
    ACONST_NULL();
    POP();
    POP();
    POP();
    LABEL(l4);
    GOTO(l11);
    LABEL(l5);
    POP();
    GOTO(l11);
    ACONST_NULL();
    ATHROW();
    LABEL(l11);
    ICONST_0();
    IFNE(l0);
    JSR(l12);
    LABEL(l6);
    RETURN();
    LABEL(l7);
    POP();
    JSR(l12);
    LABEL(l8);
    ACONST_NULL();
    ATHROW();
    LABEL(l12);
    ASTORE(2);
    RET(2);

    TRYCATCH(l0, l1, l2);
    TRYCATCH(l2, l3, l2);
    TRYCATCH(l0, l4, l5);
    TRYCATCH(l0, l6, l7);
    TRYCATCH(l7, l8, l7);

    assertMaxs(3, 3);
  }

  protected void assertMaxs(final int maxStack, final int maxLocals) {
    methodVisitor.visitMaxs(maxStack, maxLocals);
    methodVisitor.visitEnd();
    classWriter.visitEnd();
    byte[] classFile = classWriter.toByteArray();
    ClassReader classReader = new ClassReader(classFile);
    classReader.accept(
        new ClassVisitor(Opcodes.ASM5) {
          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String desc,
              final String signature,
              final String[] exceptions) {
            if (name.equals("m")) {
              return new MethodNode(Opcodes.ASM5, access, name, desc, signature, exceptions) {
                @Override
                public void visitEnd() {
                  Analyzer<BasicValue> a = new Analyzer<BasicValue>(new BasicInterpreter());
                  try {
                    Frame<BasicValue>[] frames = a.analyze("C", this);
                    int mStack = 0;
                    int mLocals = 0;
                    for (int i = 0; i < frames.length; ++i) {
                      if (frames[i] != null) {
                        mStack = Math.max(mStack, frames[i].getStackSize());
                        mLocals = Math.max(mLocals, frames[i].getLocals());
                      }
                    }
                    assertEquals(maxStack, mStack, "maxStack");
                    assertEquals(maxLocals, mLocals, "maxLocals");
                  } catch (Exception e) {
                    fail(e.getMessage());
                  }
                }
              };
            } else {
              return null;
            }
          }
        },
        0);

    try {
      TestClassLoader loader = new TestClassLoader();
      loader.defineClass("C", classFile).newInstance();
    } catch (Throwable t) {
      fail(t.getMessage());
    }
  }
}
