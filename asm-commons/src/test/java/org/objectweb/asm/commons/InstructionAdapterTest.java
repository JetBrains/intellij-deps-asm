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

import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * InstructionAdapter tests.
 *
 * @author Eric Bruneton
 */
public class InstructionAdapterTest extends AsmTest {

  @Test
  public void testConstructor() {
    new InstructionAdapter(new MethodNode());
    assertThrows(IllegalStateException.class, () -> new InstructionAdapter(new MethodNode()) {});
  }

  @Test
  public void testIllegalVisitInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitInsn(Opcodes.GOTO));
  }

  @Test
  public void testIllegalVisitIntInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitIntInsn(Opcodes.GOTO, 0));
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitIntInsn(Opcodes.NEWARRAY, 0));
  }

  @Test
  public void testIllegalVisitVarInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitVarInsn(Opcodes.GOTO, 0));
  }

  @Test
  public void testIllegalVisitTypeInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitTypeInsn(Opcodes.GOTO, "pkg/Class"));
  }

  @Test
  public void testIllegalVisitFieldInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new InstructionAdapter(new MethodNode())
                .visitFieldInsn(Opcodes.INVOKEVIRTUAL, "pkg/Class", "name", "I"));
  }

  @Test
  public void testIllegalVisitMethodInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new InstructionAdapter(new MethodNode())
                .visitMethodInsn(Opcodes.GETFIELD, "pkg/Class", "name", "I"));
  }

  @Test
  public void testIllegalVisitJumpInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitJumpInsn(Opcodes.NOP, new Label()));
  }

  @Test
  public void testVisitLdcInsn() {
    Textifier textifier = new Textifier();
    InstructionAdapter instructionAdapter =
        new InstructionAdapter(new TraceMethodVisitor(textifier));
    instructionAdapter.visitLdcInsn(Boolean.FALSE);
    instructionAdapter.visitLdcInsn(Boolean.TRUE);
    instructionAdapter.visitLdcInsn(Byte.valueOf((byte) 2));
    instructionAdapter.visitLdcInsn(Character.valueOf('3'));
    instructionAdapter.visitLdcInsn(Short.valueOf((short) 4));
    instructionAdapter.visitLdcInsn(Integer.valueOf(5));
    instructionAdapter.visitLdcInsn(Long.valueOf(6));
    instructionAdapter.visitLdcInsn(Float.valueOf(7.0f));
    instructionAdapter.visitLdcInsn(Double.valueOf(8.0));
    instructionAdapter.visitLdcInsn("9");
    instructionAdapter.visitLdcInsn(Type.getObjectType("pkg/Class"));
    instructionAdapter.visitLdcInsn(
        new Handle(Opcodes.H_GETFIELD, "pkg/Class", "name", "I", /* isInterface= */ false));

    assertEquals(
        "ICONST_0 ICONST_1 ICONST_2 BIPUSH 51 ICONST_4 ICONST_5 LDC 6 LDC 7.0 LDC 8.0 LDC \"9\" "
            + "LDC Lpkg/Class;.class LDC pkg/Class.nameI (1)",
        textifier
            .text
            .stream()
            .map(text -> text.toString().trim())
            .collect(Collectors.joining(" ")));
  }

  @Test
  public void testInvokeSpecial() {
    new InstructionAdapter(new MethodNode()).invokespecial("pkg/Class", "name", "()V");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new InstructionAdapter(Opcodes.ASM4, null)
                .invokespecial("pkg/Class", "name", "()V", /* isInterface= */ true));
  }

  @Test
  public void testInvokeVirtual() {
    new InstructionAdapter(new MethodNode()).invokevirtual("pkg/Class", "name", "()V");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new InstructionAdapter(Opcodes.ASM4, null)
                .invokevirtual("pkg/Class", "name", "()V", /* isInterface= */ true));
  }

  @Test
  public void testInvokeStatic() {
    new InstructionAdapter(new MethodNode()).invokestatic("pkg/Class", "name", "()V");
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new InstructionAdapter(Opcodes.ASM4, null)
                .invokestatic("pkg/Class", "name", "()V", /* isInterface= */ true));
  }

  @Test
  public void testIllegalVisitLdcInsn() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new InstructionAdapter(new MethodNode()).visitLdcInsn(new Object()));
  }

  /** Tests that classes transformed with an InstructionAdapter are unchanged. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAdaptInstructions(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor =
        new ClassVisitor(apiParameter.value(), classWriter) {

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            return new InstructionAdapter(
                api, super.visitMethod(access, name, descriptor, signature, exceptions)) {};
          }
        };
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classVisitor, attributes(), 0));
    } else {
      classReader.accept(classVisitor, attributes(), 0);
      assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
    }
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }
}
