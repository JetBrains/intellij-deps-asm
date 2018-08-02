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
package org.objectweb.asm.util;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.test.AsmTest;

/**
 * CheckMethodAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckMethodAdapterTest extends AsmTest implements Opcodes {

  private CheckMethodAdapter checkMethodAdapter = new CheckMethodAdapter(null);

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new CheckMethodAdapter(null) {});
    assertThrows(
        IllegalStateException.class, () -> new CheckMethodAdapter(0, "name", "()V", null, null) {});
  }

  @Test
  public void testDataflowCheckRequiresMaxLocalsAndMaxStack() {
    CheckMethodAdapter checkMethodAdapterWithDataFlowCheck =
        new CheckMethodAdapter(0, "m", "()V", null, new HashMap<Label, Integer>());
    checkMethodAdapterWithDataFlowCheck.visitCode();
    checkMethodAdapterWithDataFlowCheck.visitVarInsn(ALOAD, 0);
    checkMethodAdapterWithDataFlowCheck.visitInsn(RETURN);
    checkMethodAdapterWithDataFlowCheck.visitMaxs(0, 0);
    IllegalArgumentException exception =
        assertThrows(
            IllegalArgumentException.class, () -> checkMethodAdapterWithDataFlowCheck.visitEnd());
    assertTrue(exception.getMessage().contains("non zero maxLocals and maxStack"));
  }

  @Test
  public void testIllegalAnnotation() {
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitParameterAnnotation(0, "'", true));
  }

  @Test
  public void testIllegalTypeAnnotation() {
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTypeAnnotation(0xFFFFFFFF, null, "LA;", true));
  }

  @Test
  public void testIllegalParameterAnnotation() {
    checkMethodAdapter.visitAnnotableParameterCount(1, false);
    checkMethodAdapter.visitAnnotableParameterCount(2, true);
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitParameterAnnotation(1, "LA;", false));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitParameterAnnotation(2, "LA;", true));
  }

  @Test
  public void testIllegalCode() {
    checkMethodAdapter =
        new CheckMethodAdapter(
            Opcodes.ACC_ABSTRACT, "m", "()V", null, new HashMap<Label, Integer>());
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitCode());
  }

  @Test
  public void testIllegalMethodMemberVisitAfterEnd() {
    checkMethodAdapter.visitEnd();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitAttribute(new Comment()));
  }

  @Test
  public void testIllegalMethodAttribute() {
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitAttribute(null));
  }

  @Test
  public void testIllegalMethodInsnVisitBeforeStart() {
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitInsn(NOP));
  }

  @Test
  public void testIllegalFrameType() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFrame(123, 0, null, 0, null));
  }

  @Test
  public void testIllegalFrameLocalCount() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME, 1, new Object[] {INTEGER}, 0, null));
  }

  @Test
  public void testIllegalFrameStackCount() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME, 0, null, 1, new Object[] {INTEGER}));
  }

  @Test
  public void testIllegalFrameLocalArray() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_APPEND, 1, new Object[0], 0, null));
  }

  @Test
  public void testIllegalFrameStackArray() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME1, 0, null, 1, new Object[0]));
  }

  @Test
  public void testIllegalFrameValue() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {"LC;"}, 0, null));
    checkMethodAdapter.visitInsn(NOP);
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {new Integer(0)}, 0, null));
    checkMethodAdapter.visitInsn(NOP);
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {new Float(0.0f)}, 0, null));
  }

  @Test
  public void testIllegalMixedFrameTypes() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitFrame(F_NEW, 0, null, 0, null);
    checkMethodAdapter.visitInsn(NOP);
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFrame(F_FULL, 0, null, 0, null));
  }

  @Test
  public void testIllegalIntInsn() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitIntInsn(-1, 0));
  }

  @Test
  public void testIllegalMethodInsn() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitMethodInsn(-1, "o", "m", "()V"));
  }

  @Test
  public void testIllegalInvokeDynamicInsn() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitInvokeDynamicInsn(
                "m", "()V", new Handle(Opcodes.GETFIELD, "o", "m", "()V", false)));
  }

  @Test
  public void testIllegalByteInsnOperand() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitIntInsn(BIPUSH, Integer.MAX_VALUE));
  }

  @Test
  public void testIllegalShortInsnOperand() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitIntInsn(SIPUSH, Integer.MAX_VALUE));
  }

  @Test
  public void testIllegalVarInsnOperand() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitVarInsn(ALOAD, -1));
  }

  @Test
  public void testIllegalIntInsnOperand() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitIntInsn(NEWARRAY, 0));
  }

  @Test
  public void testIllegalTypeInsnOperand() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitTypeInsn(NEW, "[I"));
  }

  @Test
  public void testIllegalLabelInsnOperand() {
    checkMethodAdapter.visitCode();
    Label l = new Label();
    checkMethodAdapter.visitLabel(l);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitLabel(l));
  }

  @Test
  public void testIllegalDebugLabelUse() throws IOException {
    ClassReader classReader = new ClassReader("java.lang.Object");
    ClassWriter classWriter = new ClassWriter(classReader, ClassWriter.COMPUTE_MAXS);
    ClassVisitor classVisitor =
        new ClassVisitor(Opcodes.ASM7_EXPERIMENTAL, classWriter) {
          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            final MethodVisitor next =
                super.visitMethod(access, name, descriptor, signature, exceptions);
            if (next == null) {
              return next;
            }
            return new MethodVisitor(api, new CheckMethodAdapter(next)) {

              private Label entryLabel = null;

              @Override
              public void visitLabel(final Label label) {
                if (entryLabel == null) {
                  entryLabel = label;
                }
                checkMethodAdapter.visitLabel(label);
              }

              @Override
              public void visitMaxs(final int maxStack, final int maxLocals) {
                Label unwindHandler = new Label();
                checkMethodAdapter.visitLabel(unwindHandler);
                checkMethodAdapter.visitInsn(Opcodes.ATHROW); // Re-throw.
                checkMethodAdapter.visitTryCatchBlock(
                    entryLabel, unwindHandler, unwindHandler, null);
                checkMethodAdapter.visitMaxs(maxStack, maxLocals);
              }
            };
          }
        };
    assertThrows(
        RuntimeException.class, () -> classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES));
  }

  @Test
  public void testIllegalTableSwitchParameters() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTableSwitchInsn(1, 0, new Label(), new Label[0]));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, null, new Label[0]));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, new Label(), (Label[]) null));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, new Label(), new Label[0]));
  }

  @Test
  public void testIllegalLookupSwitchParameters() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), null, new Label[0]));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), new int[0], null));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), new int[0], new Label[1]));
  }

  @Test
  public void testIllegalFieldInsnNullOwner() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, null, "i", "I"));
  }

  @Test
  public void testIllegalFieldInsnOwner() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "-", "i", "I"));
  }

  @Test
  public void testIllegalFieldInsnNullName() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", null, "I"));
  }

  @Test
  public void testIllegalFieldInsnName() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "-", "I"));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "a-", "I"));
  }

  @Test
  public void testIllegalFieldInsnNullDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", null));
  }

  @Test
  public void testIllegalFieldInsnVoidDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "V"));
  }

  @Test
  public void testIllegalFieldInsnPrimitiveDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "II"));
  }

  @Test
  public void testIllegalFieldInsnArrayDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "["));
  }

  @Test
  public void testIllegalFieldInsnReferenceDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "L"));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "L-;"));
  }

  @Test
  public void testIllegalMethodInsnNullName() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", null, "()V", false));
  }

  @Test
  public void testIllegalMethodInsnName() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "-", "()V", false));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "a-", "()V", false));
  }

  @Test
  public void testIllegalMethodInsnNullDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", null, false));
  }

  @Test
  public void testIllegalMethodInsnDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "I", false));
  }

  @Test
  public void testIllegalMethodInsnParameterDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "(V)V", false));
  }

  @Test
  public void testIllegalMethodInsnReturnDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "()VV", false));
  }

  @Test
  public void testIllegalMethodInsnItf() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEINTERFACE, "C", "m", "()V", false));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "()V", true));
  }

  @Test
  public void testIllegalMethodInsnItf2() {
    checkMethodAdapter.version = Opcodes.V1_7;
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKESPECIAL, "C", "m", "()V", true));
  }

  @Test
  public void testMethodInsnItf() {
    checkMethodAdapter.version = Opcodes.V1_8;
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitMethodInsn(INVOKESPECIAL, "C", "m", "()V", true);
  }

  @Test
  public void testIllegalLdcInsnOperand() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitLdcInsn(new Object()));
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitLdcInsn(Type.getType("I")));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitLdcInsn(Type.getObjectType("I")));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitLdcInsn(Type.getMethodType("()V")));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLdcInsn(new Handle(Opcodes.GETFIELD, "o", "m", "()V", false)));

    checkMethodAdapter.version = Opcodes.V1_8;
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitLdcInsn(new Handle(-1, "o", "m", "()V", false)));

    checkMethodAdapter.visitLdcInsn(
        new Handle(Opcodes.H_NEWINVOKESPECIAL, "o", "<init>", "()V", false));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLdcInsn(
                new Handle(Opcodes.H_INVOKEVIRTUAL, "o", "<init>", "()V", false)));
  }

  @Test
  public void testIllegalMultiANewArrayDesc() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitMultiANewArrayInsn("I", 1));
  }

  @Test
  public void testIllegalMultiANewArrayDims() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitMultiANewArrayInsn("[[I", 0));
    assertThrows(
        RuntimeException.class, () -> checkMethodAdapter.visitMultiANewArrayInsn("[[I", 3));
  }

  @Test
  public void testIllegalInsnAnnotation() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitInsnAnnotation(
                TypeReference.newSuperTypeReference(0).getValue(), null, "LA;", true));
  }

  @Test
  public void testIllegalTryCatchBlock() {
    checkMethodAdapter.visitCode();
    Label label0 = new Label();
    Label label1 = new Label();
    checkMethodAdapter.visitLabel(label0);
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTryCatchBlock(label0, label1, label1, null));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTryCatchBlock(label1, label0, label1, null));
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitTryCatchBlock(label1, label1, label0, null));
  }

  @Test
  public void testIllegalTryCatchAnnotation() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitTryCatchAnnotation(
                TypeReference.newSuperTypeReference(0).getValue(), null, "LA;", true));
  }

  @Test
  public void testIllegalLocalVariableAnnotation() {
    checkMethodAdapter.visitCode();
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.newSuperTypeReference(0).getValue(),
                null,
                new Label[0],
                new Label[0],
                new int[0],
                "LA;",
                true));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.LOCAL_VARIABLE << 24,
                null,
                null,
                new Label[0],
                new int[0],
                "LA;",
                true));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.LOCAL_VARIABLE << 24,
                null,
                new Label[0],
                null,
                new int[0],
                "LA;",
                true));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.LOCAL_VARIABLE << 24,
                null,
                new Label[0],
                new Label[1],
                new int[0],
                "LA;",
                true));
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.RESOURCE_VARIABLE << 24,
                null,
                new Label[0],
                new Label[0],
                new int[1],
                "LA;",
                true));

    Label startLabel = new Label();
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    Label endLabel = new Label();
    checkMethodAdapter.visitLabel(endLabel);
    assertThrows(
        RuntimeException.class,
        () ->
            checkMethodAdapter.visitLocalVariableAnnotation(
                TypeReference.RESOURCE_VARIABLE << 24,
                null,
                new Label[] {endLabel},
                new Label[] {startLabel},
                new int[1],
                "LA;",
                true));
  }

  @Test
  public void testIllegalDataflow() {
    MethodVisitor checkMethodAdapter =
        new CheckMethodAdapter(ACC_PUBLIC, "m", "(I)V", null, new HashMap<>());
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitVarInsn(ILOAD, 1);
    checkMethodAdapter.visitInsn(IRETURN);
    checkMethodAdapter.visitMaxs(1, 2);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitEnd());
  }

  @Test
  public void testIllegalDataflobjectweb() {
    MethodVisitor checkMethodAdapter =
        new CheckMethodAdapter(ACC_PUBLIC, "m", "(I)I", null, new HashMap<>());
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitInsn(RETURN);
    checkMethodAdapter.visitMaxs(0, 2);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitEnd());
  }

  @Test
  public void testIllegalLocalVariableLabels() {
    checkMethodAdapter.visitCode();
    Label startLabel = new Label();
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    Label endLabel = new Label();
    checkMethodAdapter.visitLabel(endLabel);
    assertThrows(
        RuntimeException.class,
        () -> checkMethodAdapter.visitLocalVariable("i", "I", null, endLabel, startLabel, 0));
  }

  @Test
  public void testIllegalLineNumerLabel() {
    checkMethodAdapter.visitCode();
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitLineNumber(0, new Label()));
  }

  @Test
  public void testIllegalLabelNotVisited() {
    checkMethodAdapter.visitCode();
    Label label = new Label();
    checkMethodAdapter.visitJumpInsn(IFEQ, label);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
  }

  @Test
  public void testIllegalTryCatchLabelNotVisited() {
    checkMethodAdapter.visitCode();
    Label startLabel = new Label();
    Label endLabel = new Label();
    Label handlerLabel = new Label();
    checkMethodAdapter.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "E");
    checkMethodAdapter.visitLabel(endLabel);
    checkMethodAdapter.visitLabel(handlerLabel);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
  }

  @Test
  public void testIllegalTryCatchLabelOrder() {
    checkMethodAdapter.visitCode();
    Label startLabel = new Label();
    Label endLabel = new Label();
    Label handlerLabel = new Label();
    checkMethodAdapter.visitTryCatchBlock(endLabel, startLabel, handlerLabel, "E");
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    checkMethodAdapter.visitLabel(endLabel);
    checkMethodAdapter.visitLabel(handlerLabel);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
  }

  @Test
  public void testIllegalInsnVisitAfterEnd() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitMaxs(0, 0);
    assertThrows(RuntimeException.class, () -> checkMethodAdapter.visitInsn(NOP));
  }
}
