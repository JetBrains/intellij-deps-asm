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
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * MethodVisitor tests.
 *
 * @author Eric Bruneton
 */
public class MethodVisitorTest {

  @Test
  public void testConstuctor() {
    assertThrows(IllegalArgumentException.class, () -> new MethodVisitor(0) {});
    assertThrows(IllegalArgumentException.class, () -> new MethodVisitor(Integer.MAX_VALUE) {});
  }

  @Test
  public void testAsm5Features() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};
    assertThrows(UnsupportedOperationException.class, () -> methodVisitor.visitParameter(null, 0));
    assertThrows(
        UnsupportedOperationException.class,
        () -> methodVisitor.visitTypeAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> methodVisitor.visitInvokeDynamicInsn(null, null, null));
    assertThrows(
        UnsupportedOperationException.class,
        () -> methodVisitor.visitInsnAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> methodVisitor.visitTryCatchAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> methodVisitor.visitLocalVariableAnnotation(0, null, null, null, null, null, false));
  }

  /**
   * Tests that we can call the ASM5 visitMethodInsn on an ASM4 visitor, provided isInterface is
   * false.
   */
  @Test
  public void testBackwardCompatibilityOk() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4(checkMethodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);
    assertEquals("m", checkMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn fails on an ASM4 visitor, if isInterface is true. */
  @Test
  public void testBackwardCompatibilityFail() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4(checkMethodVisitor);
    assertThrows(
        RuntimeException.class, () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true));
  }

  /**
   * Tests that the ASM5 visitMethodInsn of an ASM4 visitor delegates calls to the ASM4
   * visitMethodInsn, provided isInterface is false.
   */
  @Test
  public void testBackwardCompatibilityOverride() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4Override(checkMethodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);
    assertEquals("mv4", checkMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn fails on an ASM4 visitor, if isInterface is true. */
  @Test
  public void testBackwardCompatibilityOverrideFail() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4Override(checkMethodVisitor);
    assertThrows(
        RuntimeException.class, () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true));
  }

  /** Tests the ASM5 visitMethodInsn succeeds on an ASM5 visitor. */
  @Test
  public void testNewMethod() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor5(checkMethodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);
    assertEquals("m", checkMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn succeeds on an ASM5 visitor which overrides this method. */
  @Test
  public void testNewMethodOverride() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor5Override(checkMethodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);
    assertEquals("mv5", checkMethodVisitor.lastVisitedMethodName);
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result.
   */
  @Test
  public void testBackwardCompatibilityMixedChain5() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4(checkMethodVisitor);
    methodVisitor = new MethodVisitor4Override(methodVisitor);
    methodVisitor = new MethodVisitor5(methodVisitor);
    methodVisitor = new MethodVisitor5Override(methodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);
    assertEquals("mv5v4", checkMethodVisitor.lastVisitedMethodName);
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result.
   */
  @Test
  public void testBackwardCompatibilityMixedChain4() {
    CheckMethodVisitor checkMethodVisitor = new CheckMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor5(checkMethodVisitor);
    methodVisitor = new MethodVisitor5Override(methodVisitor);
    methodVisitor = new MethodVisitor4(methodVisitor);
    methodVisitor = new MethodVisitor4Override(methodVisitor);
    methodVisitor.visitMethodInsn(0, "C", "m", "()V");
    assertEquals("mv4v5", checkMethodVisitor.lastVisitedMethodName);
  }

  /** An ASM4 {@link MethodVisitor} which does not override the ASM4 visitMethodInsn method. */
  private static class MethodVisitor4 extends MethodVisitor {
    MethodVisitor4(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM4, methodVisitor);
    }
  }

  /**
   * An ASM4 {@link MethodVisitor} which overrides the ASM4 visitMethodInsn method, by adding "v4"
   * at the end of method names.
   */
  private static class MethodVisitor4Override extends MethodVisitor {
    MethodVisitor4Override(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM4, methodVisitor);
    }

    @Deprecated
    @Override
    public void visitMethodInsn(
        final int opcode, final String owner, final String name, final String descriptor) {
      super.visitMethodInsn(opcode, owner, name + "v4", descriptor);
    }
  }

  /** An ASM5 {@link MethodVisitor} which does not override the ASM5 visitMethodInsn method. */
  private static class MethodVisitor5 extends MethodVisitor {
    MethodVisitor5(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM5, methodVisitor);
    }
  }

  /**
   * An ASM5 {@link MethodVisitor} which overrides the ASM5 visitMethodInsn method, by adding "v5"
   * at the end of method names.
   */
  private static class MethodVisitor5Override extends MethodVisitor {
    MethodVisitor5Override(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM5, methodVisitor);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      super.visitMethodInsn(opcode, owner, name + "v5", descriptor, isInterface);
    }
  }

  /**
   * A {@link MethodVisitor} that records the name of the last visited method instruction, via the
   * ASM5 visitMethodInsn method.
   */
  private static class CheckMethodVisitor extends MethodVisitor {
    public String lastVisitedMethodName;

    CheckMethodVisitor() {
      super(Opcodes.ASM5);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      this.lastVisitedMethodName = name;
    }
  }
}
