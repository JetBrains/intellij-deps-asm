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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.objectweb.asm.test.AsmTest;

/**
 * Unit tests for {@link MethodVisitor}.
 *
 * @author Eric Bruneton
 */
class MethodVisitorTest extends AsmTest {

  @Test
  void testConstructor_validApi() {
    Executable constructor = () -> new MethodVisitor(Opcodes.ASM4) {};

    assertDoesNotThrow(constructor);
  }

  @Test
  void testConstructor_invalidApi() {
    Executable constructor = () -> new MethodVisitor(0) {};

    Exception exception = assertThrows(IllegalArgumentException.class, constructor);
    assertEquals("Unsupported api 0", exception.getMessage());
  }

  @Test
  void testGetDelegate() {
    MethodVisitor delegate = new MethodVisitor(Opcodes.ASM4) {};
    MethodVisitor visitor = new MethodVisitor(Opcodes.ASM4, delegate) {};

    assertSame(delegate, visitor.getDelegate());
  }

  @Test
  void testVisitParameter_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitParameter = () -> methodVisitor.visitParameter(null, 0);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitParameter);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitTypeAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitTypeAnnotation = () -> methodVisitor.visitTypeAnnotation(0, null, null, false);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitTypeAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitInvokeDynamicInsn_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitInvokeDynamicInsn =
        () -> methodVisitor.visitInvokeDynamicInsn(null, null, null);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitInvokeDynamicInsn);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitInsnAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitInsnAnnotation = () -> methodVisitor.visitInsnAnnotation(0, null, null, false);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitInsnAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitTryCatchAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitTryCatchAnnotation =
        () -> methodVisitor.visitTryCatchAnnotation(0, null, null, false);

    Exception exception =
        assertThrows(UnsupportedOperationException.class, visitTryCatchAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitLocalVariableAnnotation_asm4Visitor() {
    MethodVisitor methodVisitor = new MethodVisitor(Opcodes.ASM4, null) {};

    Executable visitLocalVariableAnnotation =
        () -> methodVisitor.visitLocalVariableAnnotation(0, null, null, null, null, null, false);

    Exception exception =
        assertThrows(UnsupportedOperationException.class, visitLocalVariableAnnotation);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  @Test
  void testVisitFrame_consecutiveFrames_sameFrame() {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "C", null, "D", null);
    MethodVisitor methodVisitor =
        classWriter.visitMethod(Opcodes.ACC_STATIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    Executable visitFrame = () -> methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    assertDoesNotThrow(visitFrame);
  }

  @Test
  void testVisitFrame_consecutiveFrames() {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(Opcodes.V1_7, Opcodes.ACC_PUBLIC, "C", null, "D", null);
    MethodVisitor methodVisitor =
        classWriter.visitMethod(Opcodes.ACC_STATIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    Executable visitFrame =
        () ->
            methodVisitor.visitFrame(Opcodes.F_APPEND, 1, new Object[] {Opcodes.INTEGER}, 0, null);

    assertThrows(IllegalStateException.class, visitFrame);
  }

  @Test
  void testVisitFrame_compressedFrameWithV1_5class() {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "C", null, "D", null);
    MethodVisitor methodVisitor =
        new ClassWriter(0).visitMethod(Opcodes.ACC_STATIC, "m", "()V", null, null);
    methodVisitor.visitCode();

    Executable visitFrame = () -> methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

    Exception exception = assertThrows(IllegalArgumentException.class, visitFrame);
    assertTrue(exception.getMessage().contains("versions V1_5 or less must use F_NEW frames."));
  }

  /** Tests the ASM4 visitMethodInsn on an ASM4 visitor. */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_asm4Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4(logMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals("LogMethodVisitor:m()V;", log.toString());
  }

  /** Tests the ASM4 visitMethodInsn on an ASM4 visitor which overrides this method. */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_overridenAsm4Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4Override(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals("LogMethodVisitor:m4()V;MethodVisitor4:m()V;", log.toString());
  }

  /** Tests the ASM4 visitMethodInsn on an ASM5 visitor. */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_asm5Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor5(logMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals("LogMethodVisitor:m()V;", log.toString());
  }

  /** Tests the ASM4 visitMethodInsn on an ASM5 visitor which overrides the ASM5 method. */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_overridenAsm5Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor5Override(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals("LogMethodVisitor:m5()V;MethodVisitor5:m()V;", log.toString());
  }

  /**
   * Tests the ASM4 visitMethodInsn on an ASM4 visitor which overrides this method, and is a
   * subclass of an ASM subclass of MethodVisitor.
   */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_userTraceMethodVisitor4() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new UserTraceMethodVisitor4(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals(
        "LogMethodVisitor:m()V;TraceMethodVisitor:m()V;UserTraceMethodVisitor4:m()V;",
        log.toString());
  }

  /**
   * Tests the ASM4 visitMethodInsn on an ASM5 visitor which overrides the ASM5 method, and is a
   * subclass of an ASM subclass of MethodVisitor.
   */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_userTraceMethodVisitor5() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new UserTraceMethodVisitor5(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals(
        "LogMethodVisitor:m()V;TraceMethodVisitor:m()V;UserTraceMethodVisitor5:m()V;",
        log.toString());
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result, when calling the ASM4 visitMethodInsn on the first visitor.
   */
  @Test
  @SuppressWarnings("deprecation")
  void testDeprecatedVisitMethodInsn_mixedVisitorChain() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor =
        new MethodVisitor4Override(
            new MethodVisitor4(
                new MethodVisitor5Override(new MethodVisitor5(logMethodVisitor), log)),
            log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V");

    assertEquals(
        "LogMethodVisitor:m45()V;MethodVisitor5:m4()V;MethodVisitor4:m()V;", log.toString());
  }

  /** Tests the ASM5 visitMethodInsn on an ASM4 visitor, with isInterface set to false. */
  @Test
  void testVisitMethodInsn_asm4Visitor_isNotInterface() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4(logMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("LogMethodVisitor:m()V;", log.toString());
  }

  /** Tests the ASM5 visitMethodInsn on an ASM4 visitor, with isInterface set to true. */
  @Test
  void testVisitMethodInsn_asm4Visitor_isInterface() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4(logMethodVisitor);

    Executable visitMethodInsn = () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitMethodInsn);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  /**
   * Tests the ASM5 visitMethodInsn on an ASM4 visitor which overrides the ASM4 method, with
   * isInterface set to false.
   */
  @Test
  void testVisitMethodInsn_overridenAsm4Visitor_isNotInterface() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4Override(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("LogMethodVisitor:m4()V;MethodVisitor4:m()V;", log.toString());
  }

  /**
   * Tests the ASM5 visitMethodInsn on an ASM4 visitor which overrides the ASM4 method, with
   * isInterface set to true.
   */
  @Test
  void testVisitMethodInsn_overridenAsm4Visitor_isInterface() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor4Override(logMethodVisitor, log);

    Executable visitMethodInsn = () -> methodVisitor.visitMethodInsn(0, "C", "m", "()V", true);

    Exception exception = assertThrows(UnsupportedOperationException.class, visitMethodInsn);
    assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
  }

  /** Tests the ASM5 visitMethodInsn on an ASM5 visitor. */
  @Test
  void testVisitMethodInsn_asm5Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor5(logMethodVisitor);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("LogMethodVisitor:m()V;", log.toString());
  }

  /** Tests the ASM5 visitMethodInsn on an ASM5 visitor which overrides this method. */
  @Test
  void testVisitMethodInsn_overridenAsm5Visitor() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new MethodVisitor5Override(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals("LogMethodVisitor:m5()V;MethodVisitor5:m()V;", log.toString());
  }

  /**
   * Tests the ASM5 visitMethodInsn on an ASM4 visitor which overrides this method, and is a
   * subclass of an ASM subclass of MethodVisitor.
   */
  @Test
  void testVisitMethodInsn_userTraceMethodVisitor4() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new UserTraceMethodVisitor4(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals(
        "LogMethodVisitor:m()V;TraceMethodVisitor:m()V;UserTraceMethodVisitor4:m()V;",
        log.toString());
  }

  /**
   * Tests the ASM5 visitMethodInsn on an ASM5 visitor which overrides this method, and is a
   * subclass of an ASM subclass of MethodVisitor.
   */
  @Test
  void testVisitMethodInsn_userTraceMethodVisitor5() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor = new UserTraceMethodVisitor5(logMethodVisitor, log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals(
        "LogMethodVisitor:m()V;TraceMethodVisitor:m()V;UserTraceMethodVisitor5:m()V;",
        log.toString());
  }

  /**
   * Tests that method visitors with different API versions can be chained together and produce the
   * expected result, when calling the ASM5 visitMethodInsn on the first visitor.
   */
  @Test
  void testVisitMethodInsn_mixedVisitorChain() {
    StringWriter log = new StringWriter();
    LogMethodVisitor logMethodVisitor = new LogMethodVisitor(log);
    MethodVisitor methodVisitor =
        new MethodVisitor5Override(
            new MethodVisitor5(
                new MethodVisitor4Override(new MethodVisitor4(logMethodVisitor), log)),
            log);

    methodVisitor.visitMethodInsn(0, "C", "m", "()V", false);

    assertEquals(
        "LogMethodVisitor:m54()V;MethodVisitor4:m5()V;MethodVisitor5:m()V;", log.toString());
  }

  /** An ASM4 {@link MethodVisitor} which does not override the ASM4 visitMethodInsn method. */
  private static class MethodVisitor4 extends MethodVisitor {
    MethodVisitor4(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM4, methodVisitor);
    }
  }

  /**
   * An ASM4 {@link MethodVisitor} which overrides the ASM4 visitMethodInsn method, by adding "v4"
   * at the end of method names and by duplicating the instruction.
   */
  private static class MethodVisitor4Override extends MethodVisitor {

    private final StringWriter log;

    MethodVisitor4Override(final MethodVisitor methodVisitor, final StringWriter log) {
      super(Opcodes.ASM4, methodVisitor);
      this.log = log;
    }

    @Deprecated
    @Override
    public void visitMethodInsn(
        final int opcode, final String owner, final String name, final String descriptor) {
      super.visitMethodInsn(opcode, owner, name + "4", descriptor);
      log.append("MethodVisitor4:" + name + descriptor + ";");
    }
  }

  /** An ASM5 {@link MethodVisitor} which does not override the ASM5 visitMethodInsn method. */
  private static class MethodVisitor5 extends MethodVisitor {
    MethodVisitor5(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM5, methodVisitor);
    }
  }

  /** An ASM5 {@link MethodVisitor} which overrides the ASM5 visitMethodInsn method. */
  private static class MethodVisitor5Override extends MethodVisitor {

    private final StringWriter log;

    MethodVisitor5Override(final MethodVisitor methodVisitor, final StringWriter log) {
      super(Opcodes.ASM5, methodVisitor);
      this.log = log;
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      super.visitMethodInsn(opcode, owner, name + "5", descriptor, isInterface);
      log.append("MethodVisitor5:" + name + descriptor + ";");
    }
  }

  /**
   * An ASM-like {@link MethodVisitor} subclass, which overrides the ASM5 visitMethodInsn method,
   * but can be used with any API version.
   */
  private static class TraceMethodVisitor extends MethodVisitor {

    protected final StringWriter log;

    TraceMethodVisitor(final int api, final MethodVisitor methodVisitor, final StringWriter log) {
      super(api, methodVisitor);
      this.log = log;
    }

    @Override
    public void visitMethodInsn(
        final int opcodeAndSource,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      if (api < Opcodes.ASM5 && (opcodeAndSource & Opcodes.SOURCE_DEPRECATED) == 0) {
        // Redirect the call to the deprecated version of this method.
        super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);
        return;
      }
      super.visitMethodInsn(opcodeAndSource, owner, name, descriptor, isInterface);

      log.append("TraceMethodVisitor:" + name + descriptor + ";");
    }
  }

  /** A user subclass of {@link TraceMethodVisitor}, implemented for ASM4. */
  private static class UserTraceMethodVisitor4 extends TraceMethodVisitor {

    UserTraceMethodVisitor4(final MethodVisitor methodVisitor, final StringWriter log) {
      super(Opcodes.ASM4, methodVisitor, log);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void visitMethodInsn(
        final int opcode, final String owner, final String name, final String descriptor) {
      super.visitMethodInsn(opcode, owner, name, descriptor);
      log.append("UserTraceMethodVisitor4:" + name + descriptor + ";");
    }
  }

  /** A user subclass of {@link TraceMethodVisitor}, implemented for ASM5. */
  private static class UserTraceMethodVisitor5 extends TraceMethodVisitor {

    UserTraceMethodVisitor5(final MethodVisitor methodVisitor, final StringWriter log) {
      super(Opcodes.ASM5, methodVisitor, log);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      log.append("UserTraceMethodVisitor5:" + name + descriptor + ";");
    }
  }

  /** A {@link MethodVisitor} that logs the calls to its visitMethodInsn method. */
  private static class LogMethodVisitor extends MethodVisitor {

    private final StringWriter log;

    LogMethodVisitor(final StringWriter log) {
      super(/* latest */ Opcodes.ASM10_EXPERIMENTAL);
      this.log = log;
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      log.append("LogMethodVisitor:" + name + descriptor + ";");
    }
  }
}
