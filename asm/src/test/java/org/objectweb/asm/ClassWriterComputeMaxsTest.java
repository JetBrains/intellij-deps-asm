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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ClassWriter unit tests for COMPUTE_MAXS option with JSR instructions.
 *
 * @author Eric Bruneton
 */
public class ClassWriterComputeMaxsTest {

  private ClassWriter classWriter;

  private MethodVisitor methodVisitor;

  private Label start;

  @BeforeEach
  public void setUp() throws Exception {
    init(Opcodes.V1_1);
  }

  private void init(final int classVersion) {
    classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classWriter.visit(classVersion, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
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
    start = new Label();
    LABEL(start);
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

  private void LABEL(final Label label) {
    methodVisitor.visitLabel(label);
  }

  private void IINC(final int var, final int increment) {
    methodVisitor.visitIincInsn(var, increment);
  }

  private void GOTO(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.GOTO, label);
  }

  private void JSR(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.JSR, label);
  }

  private void IFNONNULL(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNONNULL, label);
  }

  private void IFNE(final Label label) {
    methodVisitor.visitJumpInsn(Opcodes.IFNE, label);
  }

  private void TRYCATCH(final Label start, final Label end, final Label handler) {
    methodVisitor.visitTryCatchBlock(start, end, handler, null);
  }

  private void assertMaxs(final int maxStack, final int maxLocals) {
    methodVisitor.visitMaxs(0, 0);
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
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            if (name.equals("m")) {
              return new MethodVisitor(Opcodes.ASM5) {
                @Override
                public void visitMaxs(final int realMaxStack, final int realMaxLocals) {
                  assertEquals(maxStack, realMaxStack, "maxStack");
                  assertEquals(maxLocals, realMaxLocals, "maxLocals");
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

  private void assertGraph(final String... nodes) {
    Map<String, Set<String>> expected = new HashMap<String, Set<String>>();
    for (String node : nodes) {
      StringTokenizer stringTokenizer = new StringTokenizer(node, "=,");
      String key = stringTokenizer.nextToken();
      Set<String> values = new HashSet<String>();
      while (stringTokenizer.hasMoreTokens()) {
        values.add(stringTokenizer.nextToken());
      }
      expected.put(key, values);
    }

    Map<String, Set<String>> actual = new HashMap<String, Set<String>>();
    Label currentLabel = start;
    while (currentLabel != null) {
      String key = "N" + currentLabel.getOffset();
      Set<String> value = new HashSet<String>();
      Edge outgoingEdge = currentLabel.outgoingEdges;
      if ((currentLabel.flags & Label.FLAG_SUBROUTINE_CALLER) != 0) {
        // Ignore the first outgoing edge of the basic blocks ending with a jsr: these are virtual
        // edges which lead to the instruction just after the jsr, and do not correspond to a
        // possible execution path (see {@link #visitJumpInsn} and
        // {@link Label#FLAG_SUBROUTINE_CALLER}).
        assertNotNull(outgoingEdge);
        outgoingEdge = outgoingEdge.nextEdge;
      }
      while (outgoingEdge != null) {
        value.add("N" + outgoingEdge.successor.getOffset());
        outgoingEdge = outgoingEdge.nextEdge;
      }
      actual.put(key, value);
      currentLabel = currentLabel.nextBasicBlock;
    }

    assertEquals(expected, actual);
  }

  private static class TestClassLoader extends ClassLoader {

    public TestClassLoader() {}

    public Class<?> defineClass(final String name, final byte[] classFile) {
      return defineClass(name, classFile, 0, classFile.length);
    }
  }

  /**
   * Tests a method which has the most basic <code>try{}finally{}</code> form imaginable (repeated
   * one or more times):
   *
   * <pre>
   * public void a() {
   *     int a = 0;
   *     try {
   *         a++;
   *     } finally {
   *         a--;
   *     }
   *     // ... same try {} finally {} repeated 0 or more times ...
   * }
   * </pre>
   */
  @ParameterizedTest
  @ValueSource(ints = {1, 31, 32, 33})
  public void testBasic(final int numSubroutines) {
    for (int i = 0; i < numSubroutines; ++i) {
      Label L0 = new Label();
      Label L1 = new Label();
      Label L2 = new Label();
      Label L3 = new Label();
      Label L4 = new Label();

      ICONST_0(); // N0
      ISTORE(1);

      // L0: body of try block.
      LABEL(L0); // N2
      IINC(1, 1);
      GOTO(L1);

      // L2: exception handler.
      LABEL(L2); // N8
      ASTORE(3);
      JSR(L3);
      ALOAD(3); // N12
      ATHROW();

      // L3: subroutine.
      LABEL(L3); // N14
      ASTORE(2);
      IINC(1, -1);
      PUSH();
      PUSH();
      RET(2);

      // L1: non-exceptional exit from try block.
      LABEL(L1); // N22
      JSR(L3);
      PUSH(); // N25
      PUSH();
      LABEL(L4); // N27
      RETURN();

      TRYCATCH(L0, L2, L2);
      TRYCATCH(L1, L4, L2);
    }

    assertMaxs(4, 4);
    if (numSubroutines == 1) {
      assertGraph(
          "N0=N2",
          "N2=N22,N8",
          "N8=N14",
          "N12=",
          "N14=N12,N25",
          "N22=N14,N8",
          "N25=N27,N8",
          "N27=");
    }
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

    ICONST_0(); // N0
    ISTORE(1);

    // L0: body of try block.
    LABEL(L0); // N2
    IINC(1, 1);
    GOTO(L1);

    // L2: exception handler.
    LABEL(L2); // N8
    ASTORE(3);
    JSR(L3);
    PUSH(); // N12
    PUSH();
    ALOAD(3);
    ATHROW();

    // L3: subroutine.
    LABEL(L3); // N16
    ASTORE(2);
    PUSH();
    PUSH();
    ILOAD(1);
    IFNE(L4);
    IINC(1, 2);
    GOTO(L5);

    LABEL(L4); // N29
    IINC(1, 3);

    LABEL(L5); // N32 common exit
    RET(2);

    // L1: non-exceptional exit from try block.
    LABEL(L1); // N34
    JSR(L3);
    LABEL(L6); // N37
    RETURN();

    TRYCATCH(L0, L2, L2);
    TRYCATCH(L1, L6, L2);

    assertMaxs(5, 4);
    assertGraph(
        "N0=N2",
        "N2=N34,N8",
        "N8=N16",
        "N12=",
        "N16=N29,N32",
        "N29=N32",
        "N32=N37,N12",
        "N34=N16,N8",
        "N37=");
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

    ICONST_0(); // N0
    ISTORE(1);

    // L0: Body of try block.
    LABEL(L0); // N2
    IINC(1, 1);
    JSR(L3);
    GOTO(L1); // N8

    // L2: First exception handler.
    LABEL(L2); // N11
    ASTORE(4);
    JSR(L3);
    ALOAD(4); // N16
    ATHROW();

    // L3: First subroutine.
    LABEL(L3); // N19
    ASTORE(2);
    IINC(1, 2);
    JSR(L4);
    PUSH(); // N26
    PUSH();
    RET(2);

    // L5: Second exception handler;
    LABEL(L5); // N30
    ASTORE(5);
    JSR(L4);
    ALOAD(5); // N35
    ATHROW();

    // L4: Second subroutine.
    LABEL(L4); // N38
    ASTORE(3);
    PUSH();
    PUSH();
    IINC(1, 3);
    RET(3);

    // L1: On normal exit, try block jumps here:
    LABEL(L1); // N46
    RETURN();

    TRYCATCH(L0, L2, L2);
    TRYCATCH(L3, L5, L5);

    assertMaxs(5, 6);
    assertGraph(
        "N0=N2",
        "N2=N11,N19",
        "N8=N11,N46",
        "N11=N19",
        "N16=",
        "N19=N30,N38",
        "N26=N16,N30,N8",
        "N30=N38",
        "N35=",
        "N38=N26,N35",
        "N46=");
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

    ICONST_0(); // N0
    ISTORE(1);

    // L0: while loop header/try block.
    LABEL(L0); // N2
    IINC(1, 1);
    JSR(L1);
    GOTO(L2); // N8

    // L3: implicit catch block.
    LABEL(L3); // N11
    ASTORE(2);
    JSR(L1);
    PUSH(); // N15
    PUSH();
    ALOAD(2);
    ATHROW();

    // L1: subroutine which does not return.
    LABEL(L1); // N19
    ASTORE(3);
    IINC(1, 2);
    GOTO(L4);

    // L2: end of the loop, goes back to the top.
    LABEL(L2); // N26
    GOTO(L0);

    // L4:
    LABEL(L4); // N29
    RETURN();

    TRYCATCH(L0, L3, L3);

    assertMaxs(1, 4);
    assertGraph(
        "N0=N2", "N2=N11,N19", "N8=N11,N26", "N11=N19", "N15=", "N19=N29", "N26=N2", "N29=");
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

    ACONST_NULL(); // N0
    JSR(L0);
    NOP(); // N4
    LABEL(L0); // N5
    ASTORE(0);
    ASTORE(0);
    RETURN();
    LABEL(L1); // N8
    methodVisitor.visitLocalVariable("i", "I", null, L0, L1, 1);

    assertMaxs(2, 2);
    assertGraph("N0=N5", "N4=N5", "N5=", "N8=");
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

    ICONST_0(); // N0
    ISTORE(1);

    // L5: while loop header.
    LABEL(L5); // N2
    ACONST_NULL();
    IFNONNULL(L4);

    // L0: try block.
    LABEL(L0); // N6
    IINC(1, 1);
    JSR(L1);
    GOTO(L2); // N12

    // L3: implicit catch block.
    LABEL(L3); // N15
    ASTORE(2);
    JSR(L1);
    ALOAD(2); // N19
    PUSH();
    PUSH();
    ATHROW();

    // L1: subroutine which does not return.
    LABEL(L1); // N23
    ASTORE(3);
    IINC(1, 2);
    GOTO(L4);

    // L2: end of the loop, goes back to the top.
    LABEL(L2); // N30
    GOTO(L0);

    // L4:
    LABEL(L4); // N33
    RETURN();

    TRYCATCH(L0, L3, L3);

    assertMaxs(1, 4);
    assertGraph(
        "N0=N2",
        "N2=N6,N33",
        "N6=N23,N15",
        "N12=N30,N15",
        "N15=N23",
        "N19=",
        "N23=N33",
        "N30=N6",
        "N33=");
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

    ICONST_0(); // N0
    ISTORE(1);

    // T1: first try.
    LABEL(T1); // N2
    JSR(S1);
    RETURN(); // N5

    // C1: exception handler for first try.
    LABEL(C1); // N6
    ASTORE(e1);
    JSR(S1);
    PUSH(); // N10
    PUSH();
    ALOAD(e1);
    ATHROW();

    // S1: first finally handler
    LABEL(S1); // N14
    ASTORE(r1);
    PUSH();
    PUSH();
    GOTO(W);

    // L: body of while loop, also second try;
    LABEL(L); // N21
    JSR(S2);
    RETURN(); // N24

    // C2: exception handler for second try.
    LABEL(C2); // N25
    ASTORE(e2);
    PUSH();
    PUSH();
    JSR(S2);
    ALOAD(e2); // N31
    ATHROW();

    // S2: second finally handler.
    LABEL(S2); // N33
    ASTORE(r2);
    ILOAD(b);
    IFNE(X);
    RET(r2);

    // W: test for the while loop.
    LABEL(W); // N41
    ILOAD(b);
    IFNE(L); // falls through to X.

    // X: exit from finally block.
    LABEL(X); // N45
    RET(r1);

    TRYCATCH(T1, C1, C1);
    TRYCATCH(L, C2, C2);

    assertMaxs(5, 6);
    assertGraph(
        "N0=N2",
        "N2=N6,N14",
        "N5=N6",
        "N6=N14",
        "N10=",
        "N14=N41",
        "N21=N25,N33",
        "N24=N25",
        "N25=N33",
        "N31=",
        "N33=N31,N45,N24",
        "N41=N45,N21",
        "N45=N5,N10");
  }

  @Test
  public void testImplicitExitToAnotherSubroutine2() {
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();

    ICONST_0(); // N0
    ISTORE(1);
    JSR(L1);
    RETURN(); // N5

    LABEL(L1); // N6
    ASTORE(2);
    JSR(L2);
    GOTO(L3); // N10

    LABEL(L2); // N13
    ASTORE(3);
    ILOAD(1);
    IFNE(L3);
    RET(3);

    LABEL(L3); // N20
    RET(2);

    assertMaxs(1, 4);
    assertGraph("N0=N6", "N5=", "N6=N13", "N10=N20", "N13=N20,N10", "N20=N5");
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

    ICONST_0(); // N0
    ISTORE(1);
    JSR(L1);
    GOTO(L2); // N5

    // L1: subroutine 1.
    LABEL(L1); // N8
    ASTORE(2);
    IINC(1, 1);
    GOTO(L3);

    // L2: second part of main subroutine.
    LABEL(L2); // N15
    IINC(1, 2);
    GOTO(L4);

    // L3: second part of subroutine 1.
    LABEL(L3); // N21
    IINC(1, 4);
    PUSH();
    PUSH();
    RET(2);

    // L4: third part of main subroutine.
    LABEL(L4); // N28
    PUSH();
    PUSH();
    RETURN();

    assertMaxs(4, 3);
    assertGraph("N0=N8", "N5=N15", "N8=N21", "N15=N28", "N21=N5", "N28=");
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

    // Variable numbers:
    int b = 1;
    int e1 = 2;
    int e2 = 3;
    int r1 = 4;
    int r2 = 5;

    ICONST_0(); // N0
    ISTORE(1);

    // T1: first try:
    LABEL(T1); // N2
    JSR(S1);
    RETURN(); // N5

    // C1: exception handler for first try.
    LABEL(C1); // N6
    ASTORE(e1);
    JSR(S1);
    ALOAD(e1); // N10
    ATHROW();

    // S1: first finally handler.
    LABEL(S1); // N12
    ASTORE(r1);
    GOTO(W);

    // L: body of while loop, also second try.
    LABEL(L); // N17
    JSR(S2);
    PUSH(); // N20
    PUSH();
    RETURN();

    // C2: exception handler for second try.
    LABEL(C2); // N23
    ASTORE(e2);
    JSR(S2);
    ALOAD(e2); // N27
    ATHROW();

    // S2: second finally handler.
    LABEL(S2); // N29
    ASTORE(r2);
    ILOAD(b);
    IFNE(X);
    PUSH();
    PUSH();
    RET(r2);

    // W: test for the while loop.
    LABEL(W); // N39
    ILOAD(b);
    IFNE(L); // falls through to X.

    // X: exit from finally block.
    LABEL(X); // N43
    RET(r1);

    // OC: outermost catch.
    LABEL(OC); // N45
    IINC(b, 3);
    RETURN();

    TRYCATCH(T1, C1, C1);
    TRYCATCH(L, C2, C2);
    TRYCATCH(T1, OC, OC);

    assertMaxs(4, 6);
    assertGraph(
        "N0=N2",
        "N2=N6,N45,N12",
        "N5=N6,N45",
        "N6=N45,N12",
        "N10=N45",
        "N12=N39,N45",
        "N17=N23,N45,N29",
        "N20=N23,N45",
        "N23=N45,N29",
        "N27=N45",
        "N29=N43,N45,N20,N27",
        "N39=N43,N45,N17",
        "N43=N45,N5,N10",
        "N45=");
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

    LABEL(l0); // N0
    JSR(l9);
    LABEL(l1); // N3
    GOTO(l10);
    LABEL(l2); // N6
    POP();
    JSR(l9);
    LABEL(l3); // N10
    ACONST_NULL();
    ATHROW();
    LABEL(l9); // N12
    ASTORE(1);
    RET(1);
    LABEL(l10); // N15
    ACONST_NULL();
    ACONST_NULL();
    ACONST_NULL();
    POP();
    POP();
    POP();
    LABEL(l4); // N21
    GOTO(l11);
    LABEL(l5); // N24
    POP();
    GOTO(l11);
    ACONST_NULL();
    ATHROW();
    LABEL(l11); // N30
    ICONST_0();
    IFNE(l0);
    JSR(l12);
    LABEL(l6); // N37
    RETURN();
    LABEL(l7); // N38
    POP();
    JSR(l12);
    LABEL(l8); // N42
    ACONST_NULL();
    ATHROW();
    LABEL(l12); // N44
    ASTORE(2);
    RET(2);

    TRYCATCH(l0, l1, l2);
    TRYCATCH(l2, l3, l2);
    TRYCATCH(l0, l4, l5);
    TRYCATCH(l0, l6, l7);
    TRYCATCH(l7, l8, l7);

    assertMaxs(3, 3);
    assertGraph(
        "N0=N6,N12,N24,N38",
        "N3=N15,N24,N38",
        "N6=N6,N12,N24,N38",
        "N10=N24,N38",
        "N12=N3,N10,N24,N38",
        "N15=N21,N24,N38",
        "N21=N30,N38",
        "N24=N30,N38",
        "N30=N0,N38,N44",
        "N37=",
        "N38=N38,N44",
        "N42=",
        "N44=N37,N42");
  }

  /**
   * Tests a nested subroutine with implicit exit from the nested subroutine to the outer one, with
   * the second subroutine coming first in the bytecode instructions sequence.
   */
  @Test
  public void testImplicitExitToAnotherSubroutineInverted() {
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();

    GOTO(l4); // N0
    // Second subroutine, returns to caller of first subroutine.
    LABEL(l2); // N3
    ASTORE(2);
    LABEL(l3); // N4
    RET(1);
    // First subroutine.
    LABEL(l1); // N6
    ASTORE(1);
    ALOAD(0);
    IFNONNULL(l3);
    JSR(l2); // This JSR never returns, the following code is unreachable.
    ACONST_NULL(); // N14
    ACONST_NULL();
    ACONST_NULL();
    RETURN();
    // Main "subroutine".
    LABEL(l4); // N18
    JSR(l1);
    LABEL(l5); // N21
    RETURN();

    assertMaxs(1, 3);
    assertGraph("N0=N18", "N3=N4", "N4=N21", "N6=N3,N4", "N14=", "N18=N6", "N21=");
  }

  /**
   * Tests computing the maximum stack size from the existing stack map frames and the instructions
   * in between, when dead code is present.
   */
  @Test
  public void testComputeMaxsFromFramesWithDeadCode() {
    init(Opcodes.V1_7);
    Label l0 = new Label();

    RETURN();
    // With the default compute maxs algorithm, this dead code block is not considered for the
    // maximum stack size, which works fine for classes up to V1_6. Starting with V1_7, stack map
    // frames are mandatory, even for dead code, and the maximum stack size must take dead code into
    // account. Hopefully it can be computed from the stack map frames, and the instructions in
    // between (without any control flow graph construction or algorithm).
    LABEL(l0);
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
    ACONST_NULL();
    RETURN();

    assertMaxs(1, 1);
  }

  @Test
  public void testComputeMaxsFromFrameWithLong() {
    init(Opcodes.V1_7);
    Label l0 = new Label();

    methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC, "ABC", "get", "()J", false);
    GOTO(l0);
    LABEL(l0);
    methodVisitor.visitFrame(Opcodes.F_NEW, 0, null, 1, new Object[] {Opcodes.LONG});
    ACONST_NULL();
    RETURN();

    assertMaxs(3, 1);
  }
}
