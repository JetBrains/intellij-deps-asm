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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.objectweb.asm.test.AsmTest;

/**
 * Unit tests for {@link MethodVisitor}.
 *
 * @author Eric Bruneton
 */
public class MethodVisitorTest extends AsmTest {

  @Test
  public void testConstructor_validApi() {
    Executable constructor = () -> new MethodVisitor(Opcodes.ASM4) {};

    assertDoesNotThrow(constructor);
  }

  @Test
  public void testConstructor_invalidApi() {
    Executable constructor = () -> new MethodVisitor(0) {};

    Exception exception = assertThrows(IllegalArgumentException.class, constructor);
    assertEquals("Unsupported api 0", exception.getMessage());
  }

  @Test
  public void testVisitParameter_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitParameter = () -> methodVisitor.visitParameter(null, 0);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitParameter);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitTypeAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitTypeAnnotation = () -> methodVisitor.visitTypeAnnotation(0, null, null, false);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitTypeAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitInvokeDynamicInsn_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitInvokeDynamicInsn =
        () -> methodVisitor.visitInvokeDynamicInsn(null, null, null);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitInvokeDynamicInsn);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitInsnAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitInsnAnnotation = () -> methodVisitor.visitInsnAnnotation(0, null, null, false);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitInsnAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitTryCatchAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitTryCatchAnnotation =
        () -> methodVisitor.visitTryCatchAnnotation(0, null, null, false);

    Exception exception =
        assertThrows(UnsupportedOperationException.class, visitTryCatchAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitLocalVariableAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitLocalVariableAnnotation =
        () -> methodVisitor.visitLocalVariableAnnotation(0, null, null, null, null, null, false);

    Exception exception =
        assertThrows(UnsupportedOperationException.class, visitLocalVariableAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  public void testVisitFrame_consecutiveFrames_sameFrame() {
    MethodVisitor methodVisitor =
        new ClassWriter(0).visitMethod(Opcodes.ACC_STATIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    Executable visitFrame = () -> methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    assertDoesNotThrow(visitFrame);
  }

  @Test
  public void testVisitFrame_consecutiveFrames() {
    MethodVisitor methodVisitor =
        new ClassWriter(0).visitMethod(Opcodes.ACC_STATIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    Executable visitFrame =
        () ->
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[] {Opcodes.INTEGER}, 0, null);

    assertThrows(IllegalStateException.class, visitFrame);
  }

  /**
   * Tests that we can call the ASM5 visitMethodInsn on an ASM4 visitor, provided isInterface is
   * false.
   */
  @Test
  public void testVisitMethodInsn_asm4Visitor_isNotInterface() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4(nameRecorderMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("m", nameRecorderMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn fails on an ASM4 visitor, if isInterface is true. */
  @Test
  public void testVisitMethodInsn_asm4Visitor_isInterface() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4(nameRecorderMethodVisitor);

    Executable visitMethodInsn5 = () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitMethodInsn5);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  /**
   * Tests that the ASM5 visitMethodInsn of an ASM4 visitor delegates calls to the ASM4
   * visitMethodInsn, provided isInterface is false.
   */
  @Test
  public void testVisitMethodInsn_overridenAsm4Visitor_isNotInterface() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4Override(nameRecorderMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("mv4", nameRecorderMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn fails on an ASM4 visitor, if isInterface is true. */
  @Test
  public void testVisitMethodInsn_overridenAsm4Visitor_isInterface() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor4Override(nameRecorderMethodVisitor);

    Executable visitMethodInsn5 = () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitMethodInsn5);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  /** Tests the ASM5 visitMethodInsn succeeds on an ASM5 visitor. */
  @Test
  public void testVisitMethodInsn_asm5Visitor() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor5(nameRecorderMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("m", nameRecorderMethodVisitor.lastVisitedMethodName);
  }

  /** Tests the ASM5 visitMethodInsn succeeds on an ASM5 visitor which overrides this method. */
  @Test
  public void testVisitMethodInsn_overridenAsm5Visitor() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor = new MethodVisitor5Override(nameRecorderMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("mv5", nameRecorderMethodVisitor.lastVisitedMethodName);
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result.
   */
  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedVisitMethodInsn_mixedVisitorChain() {
    NameRecorderMethodVisitor nameRecorderMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor =
        new MethodVisitor4Override(
            new MethodVisitor4(
                new MethodVisitor5Override(new MethodVisitor5(nameRecorderMethodVisitor))));

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals("mv4v5", nameRecorderMethodVisitor.lastVisitedMethodName);
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result.
   */
  @Test
  public void testVisitMethodInsn_mixedVisitorChain() {
    NameRecorderMethodVisitor checkMethodVisitor = new NameRecorderMethodVisitor();
    MethodVisitor methodVisitor =
        new MethodVisitor5Override(
            new MethodVisitor5(new MethodVisitor4Override(new MethodVisitor4(checkMethodVisitor))));

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("mv5v4", checkMethodVisitor.lastVisitedMethodName);
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
  private static class NameRecorderMethodVisitor extends MethodVisitor {

    public String lastVisitedMethodName;

    NameRecorderMethodVisitor() {
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
