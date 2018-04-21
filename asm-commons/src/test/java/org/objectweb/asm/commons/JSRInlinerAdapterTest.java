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
package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.test.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * JSRInlinerAdapter tests.
 *
 * @author Eric Bruneton
 */
public class JSRInlinerAdapterTest extends AsmTest {

  private JSRInlinerAdapter inlinedMethod = new JSRInlinerAdapter(null, 0, "m", "()V", null, null);
  private MethodNode expectedMethod = new MethodNode(0, "m", "()V", null, null);

  @Test
  public void testConstructor() {
    new JSRInlinerAdapter(null, Opcodes.ACC_PUBLIC, "name", "()V", null, null);
    assertThrows(
        IllegalStateException.class,
        () -> new JSRInlinerAdapter(null, Opcodes.ACC_PUBLIC, "name", "()V", null, null) {});
  }

  /**
   * Tests a method which has the most basic <code>try{}finally</code> form imaginable:
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
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: body of try block.
        .IINC(1, 1)
        .GOTO(l1)
        .LABEL(l2) // L2: exception handler.
        .ASTORE(3)
        .JSR(l3)
        .ALOAD(3)
        .ATHROW()
        .LABEL(l3) // L3: subroutine.
        .ASTORE(2)
        .IINC(1, -1)
        .RET(2)
        .LABEL(l1) // L1: non-exceptional exit from try block.
        .JSR(l3)
        .LABEL(l4)
        .RETURN()
        .TRYCATCH(l0, l2, l2)
        .TRYCATCH(l1, l4, l2)
        .END(1, 4);

    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3_1a = new Label();
    Label L3_1b = new Label();
    Label L3_2a = new Label();
    Label L3_2b = new Label();
    Label L4 = new Label();
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: try/catch block.
        .IINC(1, 1)
        .GOTO(L1)
        .LABEL(L2) // L2: Exception handler:
        .ASTORE(3)
        .ACONST_NULL()
        .GOTO(L3_1a)
        .LABEL(L3_1b)
        .ALOAD(3)
        .ATHROW()
        .LABEL(L1) // L1: On non-exceptional exit, try block leads here:
        .ACONST_NULL()
        .GOTO(L3_2a)
        .LABEL(L3_2b)
        .LABEL(L4)
        .RETURN()
        .LABEL(L3_1a) // L3_1a: First instantiation of subroutine:
        .ASTORE(2)
        .IINC(1, -1)
        .GOTO(L3_1b)
        .LABEL()
        .LABEL(L3_2a) // L3_2a: Second instantiation of subroutine:
        .ASTORE(2)
        .IINC(1, -1)
        .GOTO(L3_2b)
        .LABEL()
        .TRYCATCH(L0, L2, L2)
        .TRYCATCH(L1, L4, L2)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * Tests a method which has an if/else in the finally clause:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *     a++;
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
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();
    Label l6 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: body of try block.
        .IINC(1, 1)
        .GOTO(l1)
        .LABEL(l2) // L2: exception handler.
        .ASTORE(3)
        .JSR(l3)
        .ALOAD(3)
        .ATHROW()
        .LABEL(l3) // L3: subroutine.
        .ASTORE(2)
        .ILOAD(1)
        .IFNE(l4)
        .IINC(1, 2)
        .GOTO(l5)
        .LABEL(l4) // L4: a != 0
        .IINC(1, 3)
        .LABEL(l5) // L5: common exit.
        .RET(2)
        .LABEL(l1) // L1: non-exceptional exit from try block.
        .JSR(l3)
        .LABEL(l6) // L6 is used in the TRYCATCH below.
        .RETURN()
        .TRYCATCH(l0, l2, l2)
        .TRYCATCH(l1, l6, l2)
        .END(1, 4);

    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3_1a = new Label();
    Label L3_1b = new Label();
    Label L3_2a = new Label();
    Label L3_2b = new Label();
    Label L4_1 = new Label();
    Label L4_2 = new Label();
    Label L5_1 = new Label();
    Label L5_2 = new Label();
    Label L6 = new Label();
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: try/catch block.
        .IINC(1, 1)
        .GOTO(L1)
        .LABEL(L2) // L2: Exception handler:
        .ASTORE(3)
        .ACONST_NULL()
        .GOTO(L3_1a)
        .LABEL(L3_1b)
        .ALOAD(3)
        .ATHROW()
        .LABEL(L1) // L1: On non-exceptional exit, try block leads here:
        .ACONST_NULL()
        .GOTO(L3_2a)
        .LABEL(L3_2b)
        .LABEL(L6)
        .RETURN()
        .LABEL(L3_1a) // L3_1a: First instantiation of subroutine:
        .ASTORE(2)
        .ILOAD(1)
        .IFNE(L4_1)
        .IINC(1, 2)
        .GOTO(L5_1)
        .LABEL(L4_1) // L4_1: a != 0
        .IINC(1, 3)
        .LABEL(L5_1) // L5_1: common exit.
        .GOTO(L3_1b)
        .LABEL()
        .LABEL(L3_2a) // L3_2a: Second instantiation of subroutine:
        .ASTORE(2)
        .ILOAD(1)
        .IFNE(L4_2)
        .IINC(1, 2)
        .GOTO(L5_2)
        .LABEL(L4_2) // L4_2: a != 0
        .IINC(1, 3)
        .LABEL(L5_2) // L5_2: common exit.
        .GOTO(L3_2b)
        .LABEL()
        .TRYCATCH(L0, L2, L2)
        .TRYCATCH(L1, L6, L2)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * Tests a method which has a lookupswitch or tableswitch w/in the finally clause:
   *
   * <pre>
   * public void a() {
   *   int a = 0;
   *   try {
   *     a++;
   *   } finally {
   *     switch (a) {
   *       case 0:
   *         a += 2;
   *         break;
   *       default:
   *         a += 3;
   *     }
   *   }
   * }
   * </pre>
   */
  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  public void testLookupOrTableSwitchInFinally(final boolean useTableSwitch) {
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();
    Label l6 = new Label();
    Label l7 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: body of try block.
        .IINC(1, 1)
        .GOTO(l1)
        .LABEL(l2) // L2: exception handler.
        .ASTORE(3)
        .JSR(l3)
        .ALOAD(3)
        .ATHROW()
        .LABEL(l3) // L3: subroutine.
        .ASTORE(2)
        .ILOAD(1)
        .SWITCH(l5, 0, l4, useTableSwitch)
        .LABEL(l4) // L4: 'case 0:'
        .IINC(1, 2)
        .GOTO(l6)
        .LABEL(l5) // L5: 'default:'
        .IINC(1, 3)
        .LABEL(l6) // L6: common exit.
        .RET(2)
        .LABEL(l1) // L1: non-exceptional exit from try block.
        .JSR(l3)
        .LABEL(l7) // L7 is used in the TRYCATCH below
        .RETURN()
        .TRYCATCH(l0, l2, l2)
        .TRYCATCH(l1, l7, l2)
        .END(1, 4);

    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3_1a = new Label();
    Label L3_1b = new Label();
    Label L3_2a = new Label();
    Label L3_2b = new Label();
    Label L4_1 = new Label();
    Label L4_2 = new Label();
    Label L5_1 = new Label();
    Label L5_2 = new Label();
    Label L6_1 = new Label();
    Label L6_2 = new Label();
    Label L7 = new Label();
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: try/catch block.
        .IINC(1, 1)
        .GOTO(L1)
        .LABEL(L2) // L2: Exception handler:
        .ASTORE(3)
        .ACONST_NULL()
        .GOTO(L3_1a)
        .LABEL(L3_1b)
        .ALOAD(3)
        .ATHROW()
        .LABEL(L1) // L1: On non-exceptional exit, try block leads here:
        .ACONST_NULL()
        .GOTO(L3_2a)
        .LABEL(L3_2b)
        .LABEL(L7)
        .RETURN()
        .LABEL(L3_1a) // L3_1a: First instantiation of subroutine:
        .ASTORE(2)
        .ILOAD(1)
        .SWITCH(L5_1, 0, L4_1, useTableSwitch)
        .LABEL(L4_1) // L4_1: 'case 0:'
        .IINC(1, 2)
        .GOTO(L6_1)
        .LABEL(L5_1) // L5_1: 'default:'
        .IINC(1, 3)
        .LABEL(L6_1) // L6_1: common exit.
        .GOTO(L3_1b)
        .LABEL()
        .LABEL(L3_2a) // L3_2a: Second instantiation of subroutine:
        .ASTORE(2)
        .ILOAD(1)
        .SWITCH(L5_2, 0, L4_2, useTableSwitch)
        .LABEL(L4_2) // L4_2: 'case 0:'
        .IINC(1, 2)
        .GOTO(L6_2)
        .LABEL(L5_2) // L5_2: 'default:'
        .IINC(1, 3)
        .LABEL(L6_2) // L6_2: common exit.
        .GOTO(L3_2b)
        .LABEL()
        .TRYCATCH(L0, L2, L2)
        .TRYCATCH(L1, L7, L2)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * Tests a simple nested finally:
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
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: Body of try block:
        .IINC(1, 1)
        .JSR(l3)
        .GOTO(l1)
        .LABEL(l2) // L2: First exception handler:
        .JSR(l3)
        .ATHROW()
        .LABEL(l3) // L3: First subroutine:
        .ASTORE(2)
        .IINC(1, 2)
        .JSR(l4)
        .RET(2)
        .LABEL(l5) // L5: Second exception handler:
        .JSR(l4)
        .ATHROW()
        .LABEL(l4) // L4: Second subroutine:
        .ASTORE(3)
        .IINC(1, 3)
        .RET(3)
        .LABEL(l1) // L1: On normal exit, try block jumps here:
        .RETURN()
        .TRYCATCH(l0, l2, l2)
        .TRYCATCH(l3, l5, l5)
        .END(2, 6);

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
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: Body of try block:
        .IINC(1, 1)
        .ACONST_NULL()
        .GOTO(L3_1a)
        .LABEL(L3_1b)
        .GOTO(L1)
        .LABEL(L2) // L2: First exception handler:
        .ACONST_NULL()
        .GOTO(L3_2a)
        .LABEL(L3_2b)
        .ATHROW()
        .LABEL(L1) // L1: On normal exit, try block jumps here:
        .RETURN()
        .LABEL(L3_1a) // L3_1a: First instantiation of first subroutine:
        .ASTORE(2)
        .IINC(1, 2)
        .ACONST_NULL()
        .GOTO(L4_1a)
        .LABEL(L4_1b)
        .GOTO(L3_1b)
        .LABEL(L5_1)
        .ACONST_NULL()
        .GOTO(L4_2a)
        .LABEL(L4_2b)
        .ATHROW()
        .LABEL()
        .LABEL(L3_2a) // L3_2a: Second instantiation of first subroutine:
        .ASTORE(2)
        .IINC(1, 2)
        .ACONST_NULL()
        .GOTO(L4_3a)
        .LABEL(L4_3b)
        .GOTO(L3_2b)
        .LABEL(L5_2)
        .ACONST_NULL()
        .GOTO(L4_4a)
        .LABEL(L4_4b)
        .ATHROW()
        .LABEL()
        .LABEL(L4_1a) // L4_1a: First instantiation of second subroutine:
        .ASTORE(3)
        .IINC(1, 3)
        .GOTO(L4_1b)
        .LABEL()
        .LABEL(L4_2a) // L4_2a: Second instantiation of second subroutine:
        .ASTORE(3)
        .IINC(1, 3)
        .GOTO(L4_2b)
        .LABEL()
        .LABEL(L4_3a) // L4_3a: Third instantiation of second subroutine:
        .ASTORE(3)
        .IINC(1, 3)
        .GOTO(L4_3b)
        .LABEL()
        .LABEL(L4_4a) // L4_4a: Fourth instantiation of second subroutine:
        .ASTORE(3)
        .IINC(1, 3)
        .GOTO(L4_4b)
        .LABEL()
        .TRYCATCH(L0, L2, L2)
        .TRYCATCH(L3_1a, L5_1, L5_1)
        .TRYCATCH(L3_2a, L5_2, L5_2)
        .END(2, 6);

    assertMethodEquals(expectedMethod, inlinedMethod);
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
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: while loop header/try block.
        .IINC(1, 1)
        .JSR(l1)
        .GOTO(l2)
        .LABEL(l3) // L3: implicit catch block.
        .ASTORE(2)
        .JSR(l1)
        .ALOAD(2)
        .ATHROW()
        .LABEL(l1) // L1: subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(l4) // ... note that it does not return!
        .LABEL(l2) // L2: end of the loop... goes back to the top!
        .GOTO(l0)
        .LABEL(l4) // L4:
        .RETURN()
        .TRYCATCH(l0, l3, l3)
        .END(1, 4);

    Label L0 = new Label();
    Label L1_1a = new Label();
    Label L1_1b = new Label();
    Label L1_2a = new Label();
    Label L1_2b = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4_1 = new Label();
    Label L4_2 = new Label();
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: while loop header/try block.
        .IINC(1, 1)
        .ACONST_NULL()
        .GOTO(L1_1a)
        .LABEL(L1_1b)
        .GOTO(L2)
        .LABEL(L3) // L3: implicit catch block.
        .ASTORE(2)
        .ACONST_NULL()
        .GOTO(L1_2a)
        .LABEL(L1_2b)
        .ALOAD(2)
        .ATHROW()
        .LABEL(L2) // L2: end of the loop... goes back to the top!
        .GOTO(L0)
        .LABEL()
        .LABEL(L1_1a) // L1_1a: first instantiation of subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(L4_1) // ...note that it does not return!
        .LABEL(L4_1)
        .RETURN()
        .LABEL(L1_2a) // L1_2a: second instantiation of subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(L4_2) // ...note that it does not return!
        .LABEL(L4_2)
        .RETURN()
        .TRYCATCH(L0, L3, L3)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * This tests a subroutine which has no ret statement, but ends in a "return" instead. The code
   * after the JSR appears to fall through the end of the method, but is in fact unreachable and
   * therefore valid.
   *
   * <pre>
   *   JSR L0
   *   GOTO L1
   * L0:
   *   ASTORE 0
   *   RETURN
   * L1:
   *   ACONST_NULL
   * </pre>
   */
  @Test
  public void testSubroutineWithNoRet2() {
    Label l0 = new Label();
    Label l1 = new Label();
    new Generator(inlinedMethod)
        .JSR(l0)
        .GOTO(l1)
        .LABEL(l0)
        .ASTORE(0)
        .RETURN()
        .LABEL(l1)
        .ACONST_NULL()
        .END(1, 1);

    Label L0_1a = new Label();
    Label L0_1b = new Label();
    Label L1 = new Label();
    new Generator(expectedMethod)
        .ACONST_NULL()
        .GOTO(L0_1a)
        .LABEL(L0_1b)
        .GOTO(L1)
        .LABEL(L1)
        .ACONST_NULL()
        .LABEL(L0_1a) // L0_1a: First instantiation of subroutine:
        .ASTORE(0)
        .RETURN()
        .LABEL()
        .END(1, 1);

    assertMethodEquals(expectedMethod, inlinedMethod);
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
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    Label l5 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l5) // L5: while loop header.
        .ACONST_NULL()
        .IFNONNULL(l4)
        .LABEL(l0) // L0: try block.
        .IINC(1, 1)
        .JSR(l1)
        .GOTO(l2)
        .LABEL(l3) // L3: implicit catch block.
        .ASTORE(2)
        .JSR(l1)
        .ALOAD(2)
        .ATHROW()
        .LABEL(l1) // L1: subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(l4) // ... note that it does not return!
        .LABEL(l2) // L2: end of the loop... goes back to the top!
        .GOTO(l0)
        .LABEL(l4) // L4:
        .RETURN()
        .TRYCATCH(l0, l3, l3)
        .END(1, 4);

    Label L0 = new Label();
    Label L1_1a = new Label();
    Label L1_1b = new Label();
    Label L1_2a = new Label();
    Label L1_2b = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();
    Label L5 = new Label();
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L5) // L5: while loop header.
        .ACONST_NULL()
        .IFNONNULL(L4)
        .LABEL(L0) // L0: while loop header/try block.
        .IINC(1, 1)
        .ACONST_NULL()
        .GOTO(L1_1a)
        .LABEL(L1_1b)
        .GOTO(L2)
        .LABEL(L3) // L3: implicit catch block.
        .ASTORE(2)
        .ACONST_NULL()
        .GOTO(L1_2a)
        .LABEL(L1_2b)
        .ALOAD(2)
        .ATHROW()
        .LABEL(L2) // L2: end of the loop... goes back to the top!
        .GOTO(L0)
        .LABEL(L4) // L4: exit, not part of subroutine.
        // Note that the two subroutine instantiations branch here.
        .RETURN()
        .LABEL(L1_1a) // L1_1a: first instantiation of subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(L4) // ... note that it does not return!
        .LABEL()
        .LABEL(L1_2a) // L1_2a: second instantiation of subroutine ...
        .ASTORE(3)
        .IINC(1, 2)
        .GOTO(L4) // ... note that it does not return!
        .LABEL()
        .TRYCATCH(L0, L3, L3)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
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
   * This example is from the paper, "Subroutine Inlining and Bytecode Abstraction to Simplify
   * Static and Dynamic Analysis" by Cyrille Artho and Armin Biere.
   */
  @Test
  public void testImplicitExitToAnotherSubroutine() {
    // Variable numbers.
    int b = 1;
    int e1 = 2;
    int e2 = 3;
    int r1 = 4;
    int r2 = 5;

    Label t1 = new Label();
    Label c1 = new Label();
    Label s1 = new Label();
    Label l = new Label();
    Label c2 = new Label();
    Label s2 = new Label();
    Label w = new Label();
    Label x = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(t1) // T1: first try:
        .JSR(s1)
        .RETURN()
        .LABEL(c1) // C1: exception handler for first try.
        .ASTORE(e1)
        .JSR(s1)
        .ALOAD(e1)
        .ATHROW()
        .LABEL(s1) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(w)
        .LABEL(l) // L: body of while loop, also second try.
        .JSR(s2)
        .RETURN()
        .LABEL(c2) // C2: exception handler for second try.
        .ASTORE(e2)
        .JSR(s2)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(s2) // S2: second finally handler.
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(x)
        .RET(r2)
        .LABEL(w) // W: test for the while loop.
        .ILOAD(b)
        .IFNE(l) // Falls through to X.
        .LABEL(x) // X: exit from finally{} block
        .RET(r1)
        .TRYCATCH(t1, c1, c1)
        .TRYCATCH(l, c2, c2)
        .END(1, 6);

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
    new Generator(expectedMethod)
        // --- Main Subroutine ---
        .ICONST_0()
        .ISTORE(1)
        .LABEL(T1) // T1: first try:
        .ACONST_NULL()
        .GOTO(S1_1a)
        .LABEL(S1_1b)
        .RETURN()
        .LABEL(C1) // C1: exception handler for first try.
        .ASTORE(e1)
        .ACONST_NULL()
        .GOTO(S1_2a)
        .LABEL(S1_2b)
        .ALOAD(e1)
        .ATHROW()
        .LABEL()
        // --- First instantiation of first subroutine ---
        .LABEL(S1_1a) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(W_1)
        .LABEL(L_1) // L_1: body of while loop, also second try.
        .ACONST_NULL()
        .GOTO(S2_1_1a)
        .LABEL(S2_1_1b)
        .RETURN()
        .LABEL(C2_1) // C2_1: exception handler for second try.
        .ASTORE(e2)
        .ACONST_NULL()
        .GOTO(S2_1_2a)
        .LABEL(S2_1_2b)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(W_1) // W_1: test for the while loop.
        .ILOAD(b)
        .IFNE(L_1) // Falls through to X_1.
        .LABEL(X_1) // X_1: exit from finally{} block.
        .GOTO(S1_1b)
        // --- Second instantiation of first subroutine ---
        .LABEL(S1_2a) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(W_2)
        .LABEL(L_2) // L_2: body of while loop, also second try.
        .ACONST_NULL()
        .GOTO(S2_2_1a)
        .LABEL(S2_2_1b)
        .RETURN()
        .LABEL(C2_2) // C2_2: exception handler for second try.
        .ASTORE(e2)
        .ACONST_NULL()
        .GOTO(S2_2_2a)
        .LABEL(S2_2_2b)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(W_2) // W_2: test for the while loop.
        .ILOAD(b)
        .IFNE(L_2) // Falls through to X_2.
        .LABEL(X_2) // X_2: exit from finally{} block.
        .GOTO(S1_2b)
        // --- Second subroutine's 4 instantiations ---
        .LABEL(S2_1_1a) // S2_1_1a:
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_1)
        .GOTO(S2_1_1b)
        .LABEL()
        .LABEL(S2_1_2a) // S2_1_2a:
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_1)
        .GOTO(S2_1_2b)
        .LABEL()
        .LABEL(S2_2_1a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_2)
        .GOTO(S2_2_1b)
        .LABEL()
        .LABEL(S2_2_2a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_2)
        .GOTO(S2_2_2b)
        .LABEL()
        .TRYCATCH(T1, C1, C1)
        .TRYCATCH(L_1, C2_1, C2_1)
        .TRYCATCH(L_2, C2_2, C2_2)
        .END(1, 6);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * This tests two subroutines, neither of which exit. Instead, they both branch to a common set of
   * code which returns from the method. This code is not reachable except through these
   * subroutines, and since they do not invoke each other, it must be copied into both of them.
   *
   * <p>I don't believe this can be represented in Java.
   */
  @Test
  public void testCommonCodeWhichMustBeDuplicated() {
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        // Invoke the two subroutines, each twice:
        .JSR(l1)
        .JSR(l1)
        .JSR(l2)
        .JSR(l2)
        .RETURN()
        .LABEL(l1) // L1: subroutine 1.
        .IINC(1, 1)
        .GOTO(l3) // ... note that it does not return!
        .LABEL(l2) // L2: subroutine 2.
        .IINC(1, 2)
        .GOTO(l3) // ... note that it does not return!
        .LABEL(l3) // L3: common code to both subroutines: exit method.
        .RETURN()
        .END(1, 2);

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
    new Generator(expectedMethod)
        .ICONST_0()
        .ISTORE(1)
        // Invoke the two subroutines, each twice:
        .ACONST_NULL()
        .GOTO(L1_1a)
        .LABEL(L1_1b)
        .ACONST_NULL()
        .GOTO(L1_2a)
        .LABEL(L1_2b)
        .ACONST_NULL()
        .GOTO(L2_1a)
        .LABEL(L2_1b)
        .ACONST_NULL()
        .GOTO(L2_2a)
        .LABEL(L2_2b)
        .RETURN()
        .LABEL()
        .LABEL(L1_1a) // L1_1a: instantiation 1 of subroutine 1.
        .IINC(1, 1)
        .GOTO(L3_1) // ... note that it does not return!
        .LABEL(L3_1)
        .RETURN()
        .LABEL(L1_2a) // L1_2a: instantiation 2 of subroutine 1.
        .IINC(1, 1)
        .GOTO(L3_2) // ... note that it does not return!
        .LABEL(L3_2)
        .RETURN()
        .LABEL(L2_1a) // L2_1a: instantiation 1 of subroutine 2.
        .IINC(1, 2)
        .GOTO(L3_3) // ...note that it does not return!
        .LABEL(L3_3)
        .RETURN()
        .LABEL(L2_2a) // L2_2a: instantiation 2 of subroutine 2.
        .IINC(1, 2)
        .GOTO(L3_4) // ... note that it does not return!
        .LABEL(L3_4)
        .RETURN()
        .END(1, 2);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * This tests a simple subroutine where the control flow jumps back and forth between the
   * subroutine and the caller.
   *
   * <p>This would not normally be produced by a java compiler.
   */
  @Test
  public void testInterleavedCode() {
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        // Invoke the subroutine, each twice:
        .JSR(l1)
        .GOTO(l2)
        .LABEL(l1) // L1: subroutine 1.
        .ASTORE(2)
        .IINC(1, 1)
        .GOTO(l3)
        .LABEL(l2) // L2: second part of main subroutine.
        .IINC(1, 2)
        .GOTO(l4)
        .LABEL(l3) // L3: second part of subroutine 1.
        .IINC(1, 4)
        .RET(2)
        .LABEL(l4) // L4: third part of main subroutine.
        .JSR(l1)
        .RETURN()
        .END(1, 3);

    Label L1_1a = new Label();
    Label L1_1b = new Label();
    Label L1_2a = new Label();
    Label L1_2b = new Label();
    Label L2 = new Label();
    Label L3_1 = new Label();
    Label L3_2 = new Label();
    Label L4 = new Label();
    new Generator(expectedMethod)
        // Main routine:
        .ICONST_0()
        .ISTORE(1)
        .ACONST_NULL()
        .GOTO(L1_1a)
        .LABEL(L1_1b)
        .GOTO(L2)
        .LABEL(L2)
        .IINC(1, 2)
        .GOTO(L4)
        .LABEL(L4)
        .ACONST_NULL()
        .GOTO(L1_2a)
        .LABEL(L1_2b)
        .RETURN()
        .LABEL(L1_1a) // L1_1: instantiation #1.
        .ASTORE(2)
        .IINC(1, 1)
        .GOTO(L3_1)
        .LABEL(L3_1)
        .IINC(1, 4)
        .GOTO(L1_1b)
        .LABEL()
        .LABEL(L1_2a) // L1_2: instantiation #2.
        .ASTORE(2)
        .IINC(1, 1)
        .GOTO(L3_2)
        .LABEL(L3_2)
        .IINC(1, 4)
        .GOTO(L1_2b)
        .LABEL()
        .END(1, 3);

    assertMethodEquals(expectedMethod, inlinedMethod);
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
    // Variable numbers:
    int b = 1;
    int e1 = 2;
    int e2 = 3;
    int r1 = 4;
    int r2 = 5;

    Label t1 = new Label();
    Label c1 = new Label();
    Label s1 = new Label();
    Label l = new Label();
    Label c2 = new Label();
    Label s2 = new Label();
    Label w = new Label();
    Label x = new Label();
    Label ot = new Label();
    Label oc = new Label();
    new Generator(inlinedMethod)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(ot) // OT: outermost try.
        .LABEL(t1) // T1: first try:
        .JSR(s1)
        .RETURN()
        .LABEL(c1) // C1: exception handler for first try.
        .ASTORE(e1)
        .JSR(s1)
        .ALOAD(e1)
        .ATHROW()
        .LABEL(s1) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(w)
        .LABEL(l) // L: body of while loop, also second try.
        .JSR(s2)
        .RETURN()
        .LABEL(c2) // C2: exception handler for second try.
        .ASTORE(e2)
        .JSR(s2)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(s2) // S2: second finally handler.
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(x)
        .RET(r2)
        .LABEL(w) // W: test for the while loop.
        .ILOAD(b)
        .IFNE(l) // Falls through to X.
        .LABEL(x) // X: exit from finally{} block.
        .RET(r1)
        .LABEL(oc) // OC: outermost catch.
        .IINC(b, 3)
        .RETURN()
        .TRYCATCH(t1, c1, c1)
        .TRYCATCH(l, c2, c2)
        .TRYCATCH(ot, oc, oc)
        .END(1, 6);

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
    Label OT_1 = S1_1a;
    Label OT_2 = S1_2a;
    Label OT_1_1 = S2_1_1a;
    Label OT_1_2 = S2_1_2a;
    Label OT_2_1 = S2_2_1a;
    Label OT_2_2 = S2_2_2a;
    Label OC = new Label();
    Label OC_1 = new Label();
    Label OC_2 = new Label();
    Label OC_1_1 = new Label();
    Label OC_1_2 = new Label();
    Label OC_2_1 = new Label();
    Label OC_2_2 = new Label();
    new Generator(expectedMethod)
        // --- Main Subroutine ---
        .ICONST_0()
        .ISTORE(1)
        .LABEL(T1) // T1: outermost try / first try:
        .ACONST_NULL()
        .GOTO(S1_1a)
        .LABEL(S1_1b)
        .RETURN()
        .LABEL(C1) // C1: exception handler for first try.
        .ASTORE(e1)
        .ACONST_NULL()
        .GOTO(S1_2a)
        .LABEL(S1_2b)
        .ALOAD(e1)
        .ATHROW()
        .LABEL(OC) // OC: Outermost catch.
        .IINC(b, 3)
        .RETURN()
        // --- First instantiation of first subroutine ---
        .LABEL(S1_1a) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(W_1)
        .LABEL(L_1) // L_1: body of while loop, also second try.
        .ACONST_NULL()
        .GOTO(S2_1_1a)
        .LABEL(S2_1_1b)
        .RETURN()
        .LABEL(C2_1) // C2_1: exception handler for second try.
        .ASTORE(e2)
        .ACONST_NULL()
        .GOTO(S2_1_2a)
        .LABEL(S2_1_2b)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(W_1) // W_1: test for the while loop.
        .ILOAD(b)
        .IFNE(L_1) // Falls through to X_1.
        .LABEL(X_1) // X_1: exit from finally{} block.
        .GOTO(S1_1b)
        .LABEL(OC_1)
        // --- Second instantiation of first subroutine ---
        .LABEL(S1_2a) // S1: first finally handler.
        .ASTORE(r1)
        .GOTO(W_2)
        .LABEL(L_2) // L_2: body of while loop, also second try.
        .ACONST_NULL()
        .GOTO(S2_2_1a)
        .LABEL(S2_2_1b)
        .RETURN()
        .LABEL(C2_2) // C2_2: exception handler for second try.
        .ASTORE(e2)
        .ACONST_NULL()
        .GOTO(S2_2_2a)
        .LABEL(S2_2_2b)
        .ALOAD(e2)
        .ATHROW()
        .LABEL(W_2) // W_2: test for the while loop.
        .ILOAD(b)
        .IFNE(L_2) // Falls through to X_2.
        .LABEL(X_2) // X_2: exit from finally{} block.
        .GOTO(S1_2b)
        .LABEL(OC_2)
        // --- Second subroutine's 4 instantiations ---
        .LABEL(S2_1_1a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_1)
        .GOTO(S2_1_1b)
        .LABEL(OC_1_1)
        .LABEL(S2_1_2a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_1)
        .GOTO(S2_1_2b)
        .LABEL(OC_1_2)
        .LABEL(S2_2_1a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_2)
        .GOTO(S2_2_1b)
        .LABEL(OC_2_1)
        .LABEL(S2_2_2a)
        .ASTORE(r2)
        .ILOAD(b)
        .IFNE(X_2)
        .GOTO(S2_2_2b)
        .LABEL(OC_2_2)
        // Main subroutine handlers:
        .TRYCATCH(T1, C1, C1)
        .TRYCATCH(T1, OC, OC)
        // First instance of first subroutine try/catch handlers:
        // Note: reuses handler code from main subroutine.
        .TRYCATCH(L_1, C2_1, C2_1)
        .TRYCATCH(OT_1, OC_1, OC)
        // Second instance of first sub try/catch handlers:
        .TRYCATCH(L_2, C2_2, C2_2)
        .TRYCATCH(OT_2, OC_2, OC)
        // All 4 instances of second subroutine:
        .TRYCATCH(OT_1_1, OC_1_1, OC)
        .TRYCATCH(OT_1_2, OC_1_2, OC)
        .TRYCATCH(OT_2_1, OC_2_1, OC)
        .TRYCATCH(OT_2_2, OC_2_2, OC)
        .END(1, 6);

    assertMethodEquals(expectedMethod, inlinedMethod);
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

    new Generator(inlinedMethod)
        .LABEL(l0)
        .JSR(l9)
        .LABEL(l1)
        .GOTO(l10)
        .LABEL(l2)
        .POP()
        .JSR(l9)
        .LABEL(l3)
        .ACONST_NULL()
        .ATHROW()
        .LABEL(l9)
        .ASTORE(1)
        .RET(1)
        .LABEL(l10)
        .ACONST_NULL()
        .ACONST_NULL()
        .ACONST_NULL()
        .POP()
        .POP()
        .POP()
        .LABEL(l4)
        .GOTO(l11)
        .LABEL(l5)
        .POP()
        .GOTO(l11)
        .ACONST_NULL()
        .ATHROW()
        .LABEL(l11)
        .ICONST_0()
        .IFNE(l0)
        .JSR(l12)
        .LABEL(l6)
        .RETURN()
        .LABEL(l7)
        .POP()
        .JSR(l12)
        .LABEL(l8)
        .ACONST_NULL()
        .ATHROW()
        .LABEL(l12)
        .ASTORE(2)
        .RET(2)
        .TRYCATCH(l0, l1, l2)
        .TRYCATCH(l2, l3, l2)
        .TRYCATCH(l0, l4, l5)
        .TRYCATCH(l0, l6, l7)
        .TRYCATCH(l7, l8, l7)
        .END(3, 3);

    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3 = new Label();
    Label L4 = new Label();
    Label L5 = new Label();
    Label L6 = new Label();
    Label L7 = new Label();
    Label L8 = new Label();
    Label L9_1a = new Label();
    Label L9_1b = new Label();
    Label L9_1c = new Label();
    Label L9_2a = new Label();
    Label L9_2b = new Label();
    Label L10 = new Label();
    Label L11 = new Label();
    Label L12_1a = new Label();
    Label L12_1b = new Label();
    Label L12_1c = new Label();
    Label L12_2a = new Label();
    Label L12_2b = new Label();
    new Generator(expectedMethod)
        // --- Main Subroutine ---
        .LABEL(L0)
        .ACONST_NULL()
        .GOTO(L9_1a)
        .LABEL(L9_1b)
        .LABEL(L1)
        .GOTO(L10)
        .LABEL(L2)
        .POP()
        .ACONST_NULL()
        .GOTO(L9_2a)
        .LABEL(L9_2b)
        .LABEL(L3)
        .ACONST_NULL()
        .ATHROW()
        .LABEL(L10)
        .ACONST_NULL()
        .ACONST_NULL()
        .ACONST_NULL()
        .POP()
        .POP()
        .POP()
        .LABEL(L4)
        .GOTO(L11)
        .LABEL(L5)
        .POP()
        .GOTO(L11)
        // [some dead code skipped here]
        .LABEL(L11)
        .ICONST_0()
        .IFNE(L0)
        .ACONST_NULL()
        .GOTO(L12_1a)
        .LABEL(L12_1b)
        .LABEL(L6)
        .RETURN()
        .LABEL(L7)
        .POP()
        .ACONST_NULL()
        .GOTO(L12_2a)
        .LABEL(L12_2b)
        .LABEL(L8)
        .ACONST_NULL()
        .ATHROW()
        // --- First instantiation of first subroutine ---
        .LABEL()
        .LABEL(L9_1a)
        .ASTORE(1)
        .GOTO(L9_1b)
        .LABEL(L9_1c)
        // --- Second instantiation of first subroutine ---
        .LABEL(L9_2a)
        .ASTORE(1)
        .GOTO(L9_2b)
        .LABEL(L12_1c)
        // --- First instantiation of second subroutine ---
        .LABEL(L12_1a)
        .ASTORE(2)
        .GOTO(L12_1b)
        // --- Second instantiation of second subroutine ---
        .LABEL(L12_2a)
        .ASTORE(2)
        .GOTO(L12_2b)
        .TRYCATCH(L0, L1, L2)
        .TRYCATCH(L2, L3, L2)
        .TRYCATCH(L0, L4, L5)
        .TRYCATCH(L0, L6, L7)
        .TRYCATCH(L7, L8, L7)
        .TRYCATCH(L9_1a, L9_1c, L5)
        .TRYCATCH(L9_1a, L9_1c, L7)
        .TRYCATCH(L9_2a, L12_1c, L5)
        .TRYCATCH(L9_2a, L12_1c, L7)
        .END(3, 3);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  /**
   * Tests a method which has line numbers and local variable declarations.
   *
   * <pre>
   *   public void a() {
   * 1   int a = 0;
   * 2   try {
   * 3     a++;
   * 4   } finally {
   * 5     a--;
   * 6   }
   *   }
   *   LV "a" from 1 to 6
   * </pre>
   */
  @Test
  public void testBasicLineNumberAndLocalVars() {
    Label lm1 = new Label();
    Label l0 = new Label();
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    Label l4 = new Label();
    new Generator(inlinedMethod)
        .LABEL(lm1)
        .LINE(1, lm1)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(l0) // L0: body of try block.
        .LINE(3, l0)
        .IINC(1, 1)
        .GOTO(l1)
        .LABEL(l2) // L2: exception handler.
        .ASTORE(3)
        .JSR(l3)
        .ALOAD(3)
        .ATHROW()
        .LABEL(l3) // L3: subroutine.
        .LINE(5, l3)
        .ASTORE(2)
        .IINC(1, -1)
        .RET(2)
        .LABEL(l1) // L1: non-exceptional exit from try block.
        .JSR(l3)
        .LABEL(l4)
        .RETURN()
        .TRYCATCH(l0, l2, l2)
        .TRYCATCH(l1, l4, l2)
        .LOCALVAR("a", "I", 1, lm1, l4)
        .END(1, 4);

    Label LM1 = new Label();
    Label L0 = new Label();
    Label L1 = new Label();
    Label L2 = new Label();
    Label L3_1a = new Label();
    Label L3_1b = new Label();
    Label L3_1c = new Label();
    Label L3_2a = new Label();
    Label L3_2b = new Label();
    Label L3_2c = new Label();
    Label L4 = new Label();
    new Generator(expectedMethod)
        .LABEL(LM1)
        .LINE(1, LM1)
        .ICONST_0()
        .ISTORE(1)
        .LABEL(L0) // L0: try/catch block.
        .LINE(3, L0)
        .IINC(1, 1)
        .GOTO(L1)
        .LABEL(L2) // L2: Exception handler:
        .ASTORE(3)
        .ACONST_NULL()
        .GOTO(L3_1a)
        .LABEL(L3_1b)
        .ALOAD(3)
        .ATHROW()
        .LABEL(L1) // L1: On non-exceptional exit, try block leads here:
        .ACONST_NULL()
        .GOTO(L3_2a)
        .LABEL(L3_2b)
        .LABEL(L4)
        .RETURN()
        .LABEL(L3_1a) // L3_1a: First instantiation of subroutine:
        .LINE(5, L3_1a)
        .ASTORE(2)
        .IINC(1, -1)
        .GOTO(L3_1b)
        .LABEL(L3_1c)
        .LABEL(L3_2a) // L3_2a: Second instantiation of subroutine:
        .LINE(5, L3_2a)
        .ASTORE(2)
        .IINC(1, -1)
        .GOTO(L3_2b)
        .LABEL(L3_2c)
        .TRYCATCH(L0, L2, L2)
        .TRYCATCH(L1, L4, L2)
        .LOCALVAR("a", "I", 1, LM1, L4)
        .LOCALVAR("a", "I", 1, L3_1a, L3_1c)
        .LOCALVAR("a", "I", 1, L3_2a, L3_2c)
        .END(1, 4);

    assertMethodEquals(expectedMethod, inlinedMethod);
  }

  private static class Generator {

    private final MethodNode methodNode;

    Generator(final MethodNode methodNode) {
      this.methodNode = methodNode;
    }

    Generator ICONST_0() {
      methodNode.visitInsn(Opcodes.ICONST_0);
      return this;
    }

    Generator POP() {
      methodNode.visitInsn(Opcodes.POP);
      return this;
    }

    Generator ISTORE(final int var) {
      methodNode.visitVarInsn(Opcodes.ISTORE, var);
      return this;
    }

    Generator ALOAD(final int var) {
      methodNode.visitVarInsn(Opcodes.ALOAD, var);
      return this;
    }

    Generator ILOAD(final int var) {
      methodNode.visitVarInsn(Opcodes.ILOAD, var);
      return this;
    }

    Generator ASTORE(final int var) {
      methodNode.visitVarInsn(Opcodes.ASTORE, var);
      return this;
    }

    Generator RET(final int var) {
      methodNode.visitVarInsn(Opcodes.RET, var);
      return this;
    }

    Generator ATHROW() {
      methodNode.visitInsn(Opcodes.ATHROW);
      return this;
    }

    Generator ACONST_NULL() {
      methodNode.visitInsn(Opcodes.ACONST_NULL);
      return this;
    }

    Generator RETURN() {
      methodNode.visitInsn(Opcodes.RETURN);
      return this;
    }

    Generator LABEL() {
      methodNode.visitLabel(new Label());
      return this;
    }

    Generator LABEL(final Label label) {
      methodNode.visitLabel(label);
      return this;
    }

    Generator IINC(final int var, final int increment) {
      methodNode.visitIincInsn(var, increment);
      return this;
    }

    Generator GOTO(final Label label) {
      methodNode.visitJumpInsn(Opcodes.GOTO, label);
      return this;
    }

    Generator JSR(final Label label) {
      methodNode.visitJumpInsn(Opcodes.JSR, label);
      return this;
    }

    Generator IFNONNULL(final Label label) {
      methodNode.visitJumpInsn(Opcodes.IFNONNULL, label);
      return this;
    }

    Generator IFNE(final Label label) {
      methodNode.visitJumpInsn(Opcodes.IFNE, label);
      return this;
    }

    Generator SWITCH(
        final Label defaultLabel, final int key, final Label target, final boolean useTableSwitch) {
      if (useTableSwitch) {
        methodNode.visitTableSwitchInsn(key, key, defaultLabel, new Label[] {target});
      } else {
        methodNode.visitLookupSwitchInsn(defaultLabel, new int[] {key}, new Label[] {target});
      }
      return this;
    }

    Generator TRYCATCH(final Label start, final Label end, final Label handler) {
      methodNode.visitTryCatchBlock(start, end, handler, null);
      return this;
    }

    Generator LINE(final int line, final Label start) {
      methodNode.visitLineNumber(line, start);
      return this;
    }

    Generator LOCALVAR(
        final String name,
        final String descriptor,
        final int index,
        final Label start,
        final Label end) {
      methodNode.visitLocalVariable(name, descriptor, null, start, end, index);
      return this;
    }

    void END(final int maxStack, final int maxLocals) {
      methodNode.visitMaxs(maxStack, maxLocals);
      methodNode.visitEnd();

      ClassWriter classWriter = new ClassWriter(0);
      classWriter.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
      MethodVisitor methodVisitor =
          classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
      methodVisitor.visitCode();
      methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
      methodVisitor.visitMethodInsn(
          Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
      methodVisitor.visitInsn(Opcodes.RETURN);
      methodVisitor.visitMaxs(1, 1);
      methodVisitor.visitEnd();
      methodNode.accept(classWriter);
      classWriter.visitEnd();

      assertTrue(loadAndInstantiate("C", classWriter.toByteArray()));
    }
  }

  private void assertMethodEquals(final MethodNode expected, final MethodNode actual) {
    String expectedText = getText(expected);
    String actualText = getText(actual);
    assertEquals(expectedText, actualText);
  }

  private String getText(final MethodNode methodNode) {
    Textifier textifier = new Textifier();
    methodNode.accept(new TraceMethodVisitor(textifier));

    StringBuilder stringBuilder = new StringBuilder();
    for (Object o : textifier.text) {
      stringBuilder.append(o);
    }
    return stringBuilder.toString();
  }

  /** Tests that classes transformed with JSRInlinerAdapter can be loaded and instantiated. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testInlineJsrAndInstantiate(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter classWriter = new ClassWriter(0);
    classReader.accept(
        new ClassVisitor(apiParameter.value(), classWriter) {
          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            MethodVisitor methodVisitor =
                super.visitMethod(access, name, descriptor, signature, exceptions);
            return new JSRInlinerAdapter(
                api, methodVisitor, access, name, descriptor, signature, exceptions);
          }
        },
        0);
    assertThat(() -> loadAndInstantiate(classParameter.getName(), classWriter.toByteArray()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }
}
