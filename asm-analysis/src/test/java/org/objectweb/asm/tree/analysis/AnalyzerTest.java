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

  // Some local variable numbers used in tests.
  private static final int LOCAL1 = 1;
  private static final int LOCAL2 = 2;
  private static final int LOCAL3 = 3;
  private static final int LOCAL4 = 4;
  private static final int LOCAL5 = 5;

  private ClassWriter classWriter;
  private MethodVisitor methodVisitor;

  // Labels used to generate test cases.
  private final Label label0 = new Label();
  private final Label label1 = new Label();
  private final Label label2 = new Label();
  private final Label label3 = new Label();
  private final Label label4 = new Label();
  private final Label label5 = new Label();
  private final Label label6 = new Label();
  private final Label label7 = new Label();
  private final Label label8 = new Label();
  private final Label label9 = new Label();
  private final Label label10 = new Label();
  private final Label label11 = new Label();
  private final Label label12 = new Label();

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
  }

  private void nop() {
    methodVisitor.visitInsn(Opcodes.NOP);
  }

  private void push() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void pop() {
    methodVisitor.visitInsn(Opcodes.POP);
  }

  private void iconst_0() {
    methodVisitor.visitInsn(Opcodes.ICONST_0);
  }

  private void istore(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ISTORE, var);
  }

  private void aload(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ALOAD, var);
  }

  private void iload(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ILOAD, var);
  }

  private void astore(final int var) {
    methodVisitor.visitVarInsn(Opcodes.ASTORE, var);
  }

  private void ret(final int var) {
    methodVisitor.visitVarInsn(Opcodes.RET, var);
  }

  private void athrow() {
    methodVisitor.visitInsn(Opcodes.ATHROW);
  }

  private void aconst_null() {
    methodVisitor.visitInsn(Opcodes.ACONST_NULL);
  }

  private void vreturn() {
    methodVisitor.visitInsn(Opcodes.RETURN);
  }

  private void label(final Label label) {
    methodVisitor.visitLabel(label);
  }

  private void iinc(final int var, final int increment) {
    methodVisitor.visitIincInsn(var, increment);
  }

  private void go(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
  }

  private void jsr(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.JSR, label);
  }

  private void ifnonnull(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, label);
  }

  private void ifne(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNE, label);
  }

  private void tryycatch(final Label start, final Label end, final Label handler) {
    methodVisitor.visitTryCatchBlock(start, end, handler, null);
  }

  /**
   * Tests a method which has the most basic <code>try{}finally{}</code> form imaginable. That is:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *     a++;
   *   } finally {
   *     a--;
   *   }
   * }
   * </pre>
   */
  @Test
  public void testBasic() {
    iconst_0();
    istore(1);

    // Body of try block.
    label(label0);
    iinc(1, 1);
    go(label3);

    // Exception handler.
    label(label1);
    astore(3);
    jsr(label2);
    aload(3);
    athrow();

    // Subroutine.
    label(label2);
    astore(2);
    iinc(1, -1);
    push();
    push();
    ret(2);

    // Non-exceptional exit from try block.
    label(label3);
    jsr(label2);
    push();
    push();
    label(label4);
    vreturn();

    tryycatch(label0, label1, label1);
    tryycatch(label3, label4, label1);

    assertMaxs(4, 4);
  }

  /**
   * Tests a method which has an if/else-if w/in the finally clause. More specifically:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *       a++;
   *   } finally {
   *     if (a == 0) {
   *       a += 2;
   *     } else {
   *       a += 3;
   *     }
   *   }
   * }
   * </pre>
   */
  @Test
  public void testIfElseInFinally() {
    iconst_0();
    istore(1);

    // Body of try block.
    label(label0);
    iinc(1, 1);
    go(label5);

    // Exception handler.
    label(label1);
    astore(3);
    jsr(label2);
    push();
    push();
    aload(3);
    athrow();

    // Subroutine.
    label(label2);
    astore(2);
    push();
    push();
    iload(1);
    ifne(label3);
    iinc(1, 2);
    go(label4);

    label(label3);
    iinc(1, 3);

    label(label4);
    ret(2);

    // Non-exceptional exit from try block.
    label(label5);
    jsr(label2);
    label(label6);
    vreturn();

    tryycatch(label0, label1, label1);
    tryycatch(label5, label6, label1);

    assertMaxs(5, 4);
  }

  /**
   * Tests a simple nested finally. More specifically:
   *
   * <pre>
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
   * </pre>
   */
  @Test
  public void testSimpleNestedFinally() {
    iconst_0();
    istore(1);

    // Body of try block.
    label(label0);
    iinc(1, 1);
    jsr(label2);
    go(label5);

    // First exception handler.
    label(label1);
    astore(4);
    jsr(label2);
    aload(4);
    athrow();

    // First subroutine.
    label(label2);
    astore(2);
    iinc(1, 2);
    jsr(label4);
    push();
    push();
    ret(2);

    // Second exception handler.
    label(label3);
    astore(5);
    jsr(label4);
    aload(5);
    athrow();

    // Second subroutine.
    label(label4);
    astore(3);
    push();
    push();
    iinc(1, 3);
    ret(3);

    // On normal exit, try block jumps here.
    label(label5);
    vreturn();

    tryycatch(label0, label1, label1);
    tryycatch(label2, label3, label3);

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
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet() {
    iconst_0();
    istore(1);

    // While loop header/try block.
    label(label0);
    iinc(1, 1);
    jsr(label2);
    go(label3);

    // Implicit catch block.
    label(label1);
    astore(2);
    jsr(label2);
    push();
    push();
    aload(2);
    athrow();

    // Subroutine which does not return.
    label(label2);
    astore(3);
    iinc(1, 2);
    go(label4);

    // End of the loop, goes back to the top.
    label(label3);
    go(label0);

    label(label4);
    vreturn();

    tryycatch(label0, label1, label1);

    assertMaxs(1, 4);
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead.
   *
   * <pre>
   *   aconst_null
   *   jsr l0
   * l0:
   *   astore 0
   *   astore 0
   *   return
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet2() {
    aconst_null();
    jsr(label0);
    nop();
    label(label0);
    astore(0);
    astore(0);
    vreturn();
    label(label1);
    methodVisitor.visitLocalVariable("i", "I", null, label0, label1, 1);

    assertMaxs(2, 2);
  }

  /**
   * This tests a subroutine which has no ret statement, but instead exits implicitly by branching
   * to code which is not part of the subroutine. (Sadly, this is legal)
   *
   * <p>We structure this as a try/finally in a loop with a break in the finally. The loop is not
   * trivially infinite, so the RETURN statement is reachable both from the JSR subroutine and from
   * the main entry point.
   *
   * <pre>
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
   * </pre>
   */
  @Test
  public void testImplicitExit() {
    iconst_0();
    istore(1);

    // While loop header.
    label(label0);
    aconst_null();
    ifnonnull(label5);

    // Try block.
    label(label1);
    iinc(1, 1);
    jsr(label3);
    go(label4);

    // Implicit catch block.
    label(label2);
    astore(2);
    jsr(label3);
    aload(2);
    push();
    push();
    athrow();

    // Subroutine which does not return.
    label(label3);
    astore(3);
    iinc(1, 2);
    go(label5);

    // End of the loop, goes back to the top.
    label(label4);
    go(label1);

    label(label5);
    vreturn();

    tryycatch(label1, label2, label2);

    assertMaxs(1, 4);
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine.
   * Equivalent to the following java code:
   *
   * <pre>
   * void m(boolean b) {
   *   try {
   *     return;
   *   } finally {
   *     while (b) {
   *       try {
   *         return;
   *       } finally {
   *         // NOTE --- this break avoids the second return above (weird)
   *         if (b) {
   *           break;
   *         }
   *       }
   *     }
   *   }
   * }
   * </pre>
   *
   * <p>This example is from the paper, "Subroutine Inlining and Bytecode Abstraction to Simplify
   * Static and Dynamic Analysis" by Cyrille Artho and Armin Biere.
   */
  @Test
  public void testImplicitExitToAnotherSubroutine() {
    iconst_0();
    istore(1);

    // First try.
    label(label0);
    jsr(label2);
    vreturn();

    // Exception handler for first try.
    label(label1);
    astore(LOCAL2);
    jsr(label2);
    push();
    push();
    aload(LOCAL2);
    athrow();

    // First finally handler.
    label(label2);
    astore(LOCAL4);
    push();
    push();
    go(label6);

    // Body of while loop, also second try.
    label(label3);
    jsr(label5);
    vreturn();

    // Exception handler for second try.
    label(label4);
    astore(LOCAL3);
    push();
    push();
    jsr(label5);
    aload(LOCAL3);
    athrow();

    // Second finally handler.
    label(label5);
    astore(LOCAL5);
    iload(LOCAL1);
    ifne(label7);
    ret(LOCAL5);

    // Test for the while loop.
    label(label6);
    iload(LOCAL1);
    ifne(label3);

    // Exit from finally block.
    label(label7);
    ret(LOCAL4);

    tryycatch(label0, label1, label1);
    tryycatch(label3, label4, label4);

    assertMaxs(5, 6);
  }

  @Test
  public void testImplicitExitToAnotherSubroutine2() {
    iconst_0();
    istore(1);
    jsr(label0);
    vreturn();

    label(label0);
    astore(2);
    jsr(label1);
    go(label2);

    label(label1);
    astore(3);
    iload(1);
    ifne(label2);
    ret(3);

    label(label2);
    ret(2);

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
    iconst_0();
    istore(1);
    jsr(label0);
    go(label1);

    // Subroutine 1.
    label(label0);
    astore(2);
    iinc(1, 1);
    go(label2);

    // Second part of main subroutine.
    label(label1);
    iinc(1, 2);
    go(label3);

    // Second part of subroutine 1.
    label(label2);
    iinc(1, 4);
    push();
    push();
    ret(2);

    // Third part of main subroutine.
    label(label3);
    push();
    push();
    vreturn();

    assertMaxs(4, 3);
  }

  /**
   * Tests a nested try/finally with implicit exit from one subroutine to the other subroutine, and
   * with a surrounding try/catch thrown in the mix. Equivalent to the following java code:
   *
   * <pre>
   * void m(int b) {
   *   try {
   *     try {
   *       return;
   *     } finally {
   *       while (b) {
   *         try {
   *           return;
   *         } finally {
   *           // NOTE --- this break avoids the second return above (weird)
   *           if (b) {
   *             break;
   *           }
   *         }
   *       }
   *     }
   *   } catch (Exception e) {
   *     b += 3;
   *     return;
   *   }
   * }
   * </pre>
   */
  @Test
  public void testImplicitExitInTryCatch() {
    iconst_0();
    istore(1);

    // First try.
    label(label0);
    jsr(label2);
    vreturn();

    // Exception handler for first try.
    label(label1);
    astore(LOCAL2);
    jsr(label2);
    aload(LOCAL2);
    athrow();

    // First finally handler.
    label(label2);
    astore(LOCAL4);
    go(label6);

    // Body of while loop, also second try.
    label(label3);
    jsr(label5);
    push();
    push();
    vreturn();

    // Exception handler for second try.
    label(label4);
    astore(LOCAL3);
    jsr(label5);
    aload(LOCAL3);
    athrow();

    // Second finally handler.
    label(label5);
    astore(LOCAL5);
    iload(LOCAL1);
    ifne(label7);
    push();
    push();
    ret(LOCAL5);

    // Test for the while loop.
    label(label6);
    iload(LOCAL1);
    ifne(label3);

    // Exit from finally{} block.
    label(label7);
    ret(LOCAL4);

    // Outermost catch.
    label(label8);
    iinc(LOCAL1, 3);
    vreturn();

    tryycatch(label0, label1, label1);
    tryycatch(label3, label4, label4);
    tryycatch(label0, label8, label8);

    assertMaxs(4, 6);
  }

  /** Tests that Analyzer works correctly on classes with many labels. */
  @Test
  public void testManyLabels() {
    Label target = new Label();
    jsr(target);
    label(target);
    for (int i = 0; i < 8192; i++) {
      Label label = new Label();
      go(label);
      label(label);
    }
    vreturn();
    assertMaxs(1, 1);
  }

  /**
   * Tests an example coming from distilled down version of
   * com/sun/corba/ee/impl/protocol/CorbaClientDelegateImpl from GlassFish 2. See issue #317823.
   */
  @Test
  public void testGlassFish2CorbaClientDelegateImplExample() {
    label(label0);
    jsr(label4);
    label(label1);
    go(label5);
    label(label2);
    pop();
    jsr(label4);
    label(label3);
    aconst_null();
    athrow();
    label(label4);
    astore(1);
    ret(1);
    label(label5);
    aconst_null();
    aconst_null();
    aconst_null();
    pop();
    pop();
    pop();
    label(label6);
    go(label8);
    label(label7);
    pop();
    go(label8);
    aconst_null();
    athrow();
    label(label8);
    iconst_0();
    ifne(label0);
    jsr(label12);
    label(label9);
    vreturn();
    label(label10);
    pop();
    jsr(label12);
    label(label11);
    aconst_null();
    athrow();
    label(label12);
    astore(2);
    ret(2);

    tryycatch(label0, label1, label2);
    tryycatch(label2, label3, label2);
    tryycatch(label0, label6, label7);
    tryycatch(label0, label9, label10);
    tryycatch(label10, label11, label10);

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
                  Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
                  try {
                    Frame<BasicValue>[] frames = analyzer.analyze("C", this);
                    int actualMaxStack = 0;
                    int actualMaxLocals = 0;
                    for (Frame<BasicValue> frame : frames) {
                      if (frame != null) {
                        actualMaxStack = Math.max(actualMaxStack, frame.getStackSize());
                        actualMaxLocals = Math.max(actualMaxLocals, frame.getLocals());
                      }
                    }
                    assertEquals(maxStack, actualMaxStack, "maxStack");
                    assertEquals(maxLocals, actualMaxLocals, "maxLocals");
                  } catch (AnalyzerException e) {
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

    TestClassLoader loader = new TestClassLoader();
    try {
      loader.defineClass("C", classFile).newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      fail(e.getMessage());
    }
  }

  protected static class TestClassLoader extends ClassLoader {

    public TestClassLoader() {}

    public Class<?> defineClass(final String name, final byte[] classFile) {
      return defineClass(name, classFile, 0, classFile.length);
    }
  }
}
