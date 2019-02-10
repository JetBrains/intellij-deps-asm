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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;
import org.objectweb.asm.test.AsmTest;

/**
 * Unit tests for {@link CheckMethodAdapter}.
 *
 * @author Eric Bruneton
 */
public class CheckMethodAdapterTest extends AsmTest implements Opcodes {

  private final CheckMethodAdapter checkMethodAdapter = new CheckMethodAdapter(null);

  @Test
  public void testConstructor() {
    assertDoesNotThrow(() -> new CheckMethodAdapter(null));
    assertThrows(IllegalStateException.class, () -> new CheckMethodAdapter(null) {});
    assertThrows(
        IllegalStateException.class, () -> new CheckMethodAdapter(0, "name", "()V", null, null) {});
  }

  @Test
  public void testVisitTypeAnnotation_illegalTypeRef() {
    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitTypeAnnotation(0xFFFFFFFF, null, "LA;", true));
  }

  @Test
  public void testVisitParameterAnnotation_invisibleAnnotation_illegalParameterIndex() {
    checkMethodAdapter.visitAnnotableParameterCount(1, false);

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitParameterAnnotation(1, "LA;", false));
  }

  @Test
  public void testVisitParameterAnnotation_visibleAnnotation_illegalParameterIndex() {
    checkMethodAdapter.visitAnnotableParameterCount(2, true);

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitParameterAnnotation(2, "LA;", true));
  }

  @Test
  public void testVisitParameterAnnotation_illegalDescriptor() {
    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitParameterAnnotation(0, "'", true));
  }

  @Test
  public void testVisitAttribute_afterEnd() {
    checkMethodAdapter.visitEnd();

    assertThrows(
        IllegalStateException.class, () -> checkMethodAdapter.visitAttribute(new Comment()));
  }

  @Test
  public void testVisitAttribute_nullAttribute() {
    assertThrows(IllegalArgumentException.class, () -> checkMethodAdapter.visitAttribute(null));
  }

  @Test
  public void testVisitCode_abstractMethod() {
    CheckMethodAdapter checkAbstractMethodAdapter =
        new CheckMethodAdapter(Opcodes.ACC_ABSTRACT, "m", "()V", null, new HashMap<>());

    assertThrows(UnsupportedOperationException.class, () -> checkAbstractMethodAdapter.visitCode());
  }

  @Test
  public void testVisitFrame_illegalFrameType() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class, () -> checkMethodAdapter.visitFrame(123, 0, null, 0, null));
  }

  @Test
  public void testVisitFrame_illegalLocalCount() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME, 1, new Object[] {INTEGER}, 0, null));
  }

  @Test
  public void testVisitFrame_illegalStackCount() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME, 0, null, 1, new Object[] {INTEGER}));
  }

  @Test
  public void testVisitFrame_illegalLocalArray() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_APPEND, 1, new Object[0], 0, null));
  }

  @Test
  public void testVisitFrame_illegalStackArray() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_SAME1, 0, null, 1, new Object[0]));
  }

  @Test
  public void testVisitFrame_illegalDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {"LC;"}, 0, null));
  }

  @Test
  public void testVisitFrame_illegalPrimitiveType() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitInsn(NOP);
    Integer invalidFrameValue =
        new Integer(0); // NOPMD(IntegerInstantiation): needed to build an invalid value.

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {invalidFrameValue}, 0, null));
  }

  @Test
  public void testVisitFrame_illegalValueClass() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitInsn(NOP);

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 1, new Object[] {new Float(0.0f)}, 0, null));
  }

  @Test
  public void testVisitFrame_illegalMixedFrameTypes() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitFrame(F_NEW, 0, null, 0, null);
    checkMethodAdapter.visitInsn(NOP);

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFrame(F_FULL, 0, null, 0, null));
  }

  @Test
  public void testVisitInsn_beforeStart() {
    assertThrows(IllegalStateException.class, () -> checkMethodAdapter.visitInsn(NOP));
  }

  @Test
  public void testVisitInsn_IllegalInsnVisitAfterEnd() {
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitMaxs(0, 0);

    assertThrows(IllegalStateException.class, () -> checkMethodAdapter.visitInsn(NOP));
  }

  @Test
  public void testVisitIntInsn_illegalOpcode() {
    checkMethodAdapter.visitCode();

    assertThrows(IllegalArgumentException.class, () -> checkMethodAdapter.visitIntInsn(-1, 0));
  }

  @Test
  public void testVisitIntInsn_illegalOperand() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class, () -> checkMethodAdapter.visitIntInsn(NEWARRAY, 0));
  }

  @Test
  public void testVisitIntInsn_illegalByteOperand() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitIntInsn(BIPUSH, Integer.MAX_VALUE));
  }

  @Test
  public void testVisitIntInsn_illegalShortOperand() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitIntInsn(SIPUSH, Integer.MAX_VALUE));
  }

  @Test
  public void testVisitVarInsn_illegalOperand() {
    checkMethodAdapter.visitCode();

    assertThrows(IllegalArgumentException.class, () -> checkMethodAdapter.visitVarInsn(ALOAD, -1));
  }

  @Test
  public void testVisitTypeInsn_illegalOperand() {
    checkMethodAdapter.visitCode();

    assertThrows(IllegalArgumentException.class, () -> checkMethodAdapter.visitTypeInsn(NEW, "[I"));
  }

  @Test
  public void testVisitFieldInsn_nullOwner() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, null, "i", "I"));
  }

  @Test
  public void testVisitFieldInsn_invalidOwnerDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "-", "i", "I"));
  }

  @Test
  public void testVisitFieldInsn_nullName() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", null, "I"));
  }

  @Test
  public void testVisitFieldInsn_invalidFieldName() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "-", "I"));
  }

  @Test
  public void testVisitFieldInsn_invalidFieldName2() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "a-", "I"));
  }

  @Test
  public void testVisitFieldInsn_nullDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", null));
  }

  @Test
  public void testVisitFieldInsn_voidDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "V"));
  }

  @Test
  public void testVisitFieldInsn_invalidPrimitiveDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "II"));
  }

  @Test
  public void testVisitFieldInsn_illegalArrayDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "["));
  }

  @Test
  public void testVisitFieldInsn_invalidReferenceDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "L"));
  }

  @Test
  public void testVisitFieldInsn_invalidReferenceDescriptor2() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitFieldInsn(GETFIELD, "C", "i", "L-;"));
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testDeprecatedVisitMethodInsn_invalidOpcode() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(-1, "o", "m", "()V"));
  }

  @Test
  public void testVisitMethodInsn_invalidOpcode() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(-1, "o", "m", "()V", false));
  }

  @Test
  public void testVisitMethodInsn_nullName() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", null, "()V", false));
  }

  @Test
  public void testVisitMethodInsn_invalidName() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "-", "()V", false));
  }

  @Test
  public void testVisitMethodInsn_invalidName2() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "a-", "()V", false));
  }

  @Test
  public void testVisitMethodInsn_nullDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", null, false));
  }

  @Test
  public void testVisitMethodInsn_invalidDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "I", false));
  }

  @Test
  public void testVisitMethodInsn_invalidParameterDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "(V)V", false));
  }

  @Test
  public void testVisitMethodInsn_invalidReturnDescriptor() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "()VV", false));
  }

  @Test
  public void testVisitMethodInsn_illegalInvokeInterface() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEINTERFACE, "C", "m", "()V", false));
  }

  @Test
  public void testVisitMethodInsn_illegalInvokeInterface2() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKEVIRTUAL, "C", "m", "()V", true));
  }

  @Test
  public void testVisitMethodInsn_illegalInvokeSpecial() {
    checkMethodAdapter.version = Opcodes.V1_7;
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () -> checkMethodAdapter.visitMethodInsn(INVOKESPECIAL, "C", "m", "()V", true));
  }

  @Test
  public void testVisitMethodInsn_invokeSpecialOnInterface() {
    checkMethodAdapter.version = Opcodes.V1_8;
    checkMethodAdapter.visitCode();

    assertDoesNotThrow(
        () -> checkMethodAdapter.visitMethodInsn(INVOKESPECIAL, "C", "m", "()V", true));
  }

  @Test
  public void testVisitInvokeDynamicInsn_illegalHandleTag() {
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class,
        () ->
            checkMethodAdapter.visitInvokeDynamicInsn(
                "m", "()V", new Handle(Opcodes.GETFIELD, "o", "m", "()V", false)));
  }

  @Test
  public void testVisitLabel_alreadyVisitedLabel() {
    Label label = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(label);

    assertThrows(IllegalArgumentException.class, () -> checkMethodAdapter.visitLabel(label));
  }

  @Test
  public void testVisitLdcInsn_v11_illegalOperandType() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class, () -> checkMethodAdapter.visitLdcInsn(new Object()));
  }

  @Test
  public void testVisitLdcInsn_v11_primitiveDescriptor() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();

    assertThrows(
        IllegalArgumentException.class, () -> checkMethodAdapter.visitLdcInsn(Type.getType("I")));
  }

  @Test
  public void testVisitLdcInsn_v11_illegalConstantClass() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLdcInsn(Type.getObjectType("I")));
    assertEquals("ldc of a constant class requires at least version 1.5", exception.getMessage());
  }

  @Test
  public void testVisitLdcInsn_v11_methodDescriptor() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLdcInsn(Type.getMethodType("()V")));
    assertEquals("ldc of a method type requires at least version 1.7", exception.getMessage());
  }

  @Test
  public void testVisitLdcInsn_v11_handle() {
    checkMethodAdapter.version = Opcodes.V1_1;
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLdcInsn(
                    new Handle(Opcodes.GETFIELD, "o", "m", "()V", false)));
    assertEquals("ldc of a Handle requires at least version 1.7", exception.getMessage());
  }

  @Test
  public void testVisitLdcInsn_v18_invalidHandleTag() {
    checkMethodAdapter.version = Opcodes.V1_8;
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLdcInsn(new Handle(-1, "o", "m", "()V", false)));
    assertEquals("invalid handle tag -1", exception.getMessage());
  }

  @Test
  public void testVisitLdcInsn_v18_handle() {
    checkMethodAdapter.version = Opcodes.V1_8;
    checkMethodAdapter.visitCode();

    assertDoesNotThrow(
        () ->
            checkMethodAdapter.visitLdcInsn(
                new Handle(Opcodes.H_NEWINVOKESPECIAL, "o", "<init>", "()V", false)));
  }

  @Test
  public void testVisitLdcInsn_v18_illegalHandleName() {
    checkMethodAdapter.version = Opcodes.V1_8;
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLdcInsn(
                    new Handle(Opcodes.H_INVOKEVIRTUAL, "o", "<init>", "()V", false)));
    assertEquals(
        "Invalid handle name (must be a valid unqualified name): <init>", exception.getMessage());
  }

  @Test
  public void testVisitTableSwitchInsn_invalidMinMax() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitTableSwitchInsn(1, 0, new Label(), new Label[0]));
    assertEquals("Max = 0 must be greater than or equal to min = 1", exception.getMessage());
  }

  @Test
  public void testVisitTableSwitchInsn_invalidDefaultLabel() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, null, new Label[0]));
    assertEquals("Invalid default label (must not be null)", exception.getMessage());
  }

  @Test
  public void testVisitTableSwitchInsn_nullKeyLabels() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, new Label(), (Label[]) null));
    assertEquals("There must be max - min + 1 labels", exception.getMessage());
  }

  @Test
  public void testVisitTableSwitchInsn_invalidKeyLabelCount() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitTableSwitchInsn(0, 1, new Label(), new Label[0]));
    assertEquals("There must be max - min + 1 labels", exception.getMessage());
  }

  @Test
  public void testVisitLookupSwitchInsn_nullKeyArray_oneLabel() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), null, new Label[0]));
    assertEquals("There must be the same number of keys and labels", exception.getMessage());
  }

  @Test
  public void testVisitLookupSwitchInsn_noKey_nullLabelArray() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), new int[0], null));
    assertEquals("There must be the same number of keys and labels", exception.getMessage());
  }

  @Test
  public void testVisitLookupSwitchInsn_noKey_oneNullLabel() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLookupSwitchInsn(new Label(), new int[0], new Label[1]));
    assertEquals("There must be the same number of keys and labels", exception.getMessage());
  }

  @Test
  public void testVisitMultiANewArrayInsn_invalidDescriptor() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitMultiANewArrayInsn("I", 1));
    assertEquals(
        "Invalid descriptor (must be an array type descriptor): I", exception.getMessage());
  }

  @Test
  public void testVisitMultiANewArrayInsn_notEnoughDimensions() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitMultiANewArrayInsn("[[I", 0));
    assertEquals("Invalid dimensions (must be greater than 0): 0", exception.getMessage());
  }

  @Test
  public void testVisitMultiANewArrayInsn_tooManyDimensions() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitMultiANewArrayInsn("[[I", 3));
    assertEquals(
        "Invalid dimensions (must not be greater than numDimensions(descriptor)): 3",
        exception.getMessage());
  }

  @Test
  public void testVisitInsnAnnotation_invalidTypeReference() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitInsnAnnotation(
                    TypeReference.newSuperTypeReference(0).getValue(), null, "LA;", true));
    assertEquals("Invalid type reference sort 0x10", exception.getMessage());
  }

  @Test
  public void testVisitTryCatchBlock_afterStartLabel() {
    Label label0 = new Label();
    Label label1 = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(label0);

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> checkMethodAdapter.visitTryCatchBlock(label0, label1, label1, null));
    assertEquals("Try catch blocks must be visited before their labels", exception.getMessage());
  }

  @Test
  public void testVisitTryCatchBlock_afterEndLabel() {
    Label label0 = new Label();
    Label label1 = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(label0);

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> checkMethodAdapter.visitTryCatchBlock(label1, label0, label1, null));
    assertEquals("Try catch blocks must be visited before their labels", exception.getMessage());
  }

  @Test
  public void testVisitTryCatchBlock_afterHandlerLabel() {
    Label label0 = new Label();
    Label label1 = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(label0);

    Exception exception =
        assertThrows(
            IllegalStateException.class,
            () -> checkMethodAdapter.visitTryCatchBlock(label1, label1, label0, null));
    assertEquals("Try catch blocks must be visited before their labels", exception.getMessage());
  }

  @Test
  public void testVisitTryCatchAnnotation_invalidTypeReference() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitTryCatchAnnotation(
                    TypeReference.newSuperTypeReference(0).getValue(), null, "LA;", true));
    assertEquals("Invalid type reference sort 0x10", exception.getMessage());
  }

  @Test
  public void testVisitLocalVariable_invalidRange() {
    Label startLabel = new Label();
    Label endLabel = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    checkMethodAdapter.visitLabel(endLabel);

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLocalVariable("i", "I", null, endLabel, startLabel, 0));
    assertEquals(
        "Invalid start and end labels (end must be greater than start)", exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_invalidTypeReference() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.newSuperTypeReference(0).getValue(),
                    null,
                    new Label[0],
                    new Label[0],
                    new int[0],
                    "LA;",
                    true));
    assertEquals("Invalid type reference sort 0x10", exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_nullStart_noEnd_noIndex() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.LOCAL_VARIABLE << 24,
                    null,
                    null,
                    new Label[0],
                    new int[0],
                    "LA;",
                    true));
    assertEquals(
        "Invalid start, end and index arrays (must be non null and of identical length",
        exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_noStart_nullEnd_noIndex() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.LOCAL_VARIABLE << 24,
                    null,
                    new Label[0],
                    null,
                    new int[0],
                    "LA;",
                    true));
    assertEquals(
        "Invalid start, end and index arrays (must be non null and of identical length",
        exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_noStart_oneNullEnd_noIndex() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.LOCAL_VARIABLE << 24,
                    null,
                    new Label[0],
                    new Label[1],
                    new int[0],
                    "LA;",
                    true));
    assertEquals(
        "Invalid start, end and index arrays (must be non null and of identical length",
        exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_noStart_noEnd_oneIndex() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.RESOURCE_VARIABLE << 24,
                    null,
                    new Label[0],
                    new Label[0],
                    new int[1],
                    "LA;",
                    true));
    assertEquals(
        "Invalid start, end and index arrays (must be non null and of identical length",
        exception.getMessage());
  }

  @Test
  public void testVisitLocalVariableAnnotation_invalidRange() {
    Label startLabel = new Label();
    Label endLabel = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    checkMethodAdapter.visitLabel(endLabel);

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () ->
                checkMethodAdapter.visitLocalVariableAnnotation(
                    TypeReference.RESOURCE_VARIABLE << 24,
                    null,
                    new Label[] {endLabel},
                    new Label[] {startLabel},
                    new int[1],
                    "LA;",
                    true));
    assertEquals(
        "Invalid start and end labels (end must be greater than start)", exception.getMessage());
  }

  @Test
  public void testVisitLineNumber_beforeLabel() {
    checkMethodAdapter.visitCode();

    Exception exception =
        assertThrows(
            IllegalArgumentException.class,
            () -> checkMethodAdapter.visitLineNumber(0, new Label()));
    assertEquals("Invalid start label (must be visited first)", exception.getMessage());
  }

  @Test
  public void testVisitMaxs_unvisitedJumpLabels() {
    Label label = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitJumpInsn(IFEQ, label);

    Exception exception =
        assertThrows(IllegalStateException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
    assertEquals("Undefined label used", exception.getMessage());
  }

  @Test
  public void testVisitMaxs_unvisitedTryCatchLabels() {
    Label startLabel = new Label();
    Label endLabel = new Label();
    Label handlerLabel = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitTryCatchBlock(startLabel, endLabel, handlerLabel, "E");
    checkMethodAdapter.visitLabel(endLabel);
    checkMethodAdapter.visitLabel(handlerLabel);

    Exception exception =
        assertThrows(IllegalStateException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
    assertEquals("Undefined try catch block labels", exception.getMessage());
  }

  @Test
  public void testVisitMaxs_invalidTryCatchRange() {
    Label startLabel = new Label();
    Label endLabel = new Label();
    Label handlerLabel = new Label();
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitTryCatchBlock(endLabel, startLabel, handlerLabel, "E");
    checkMethodAdapter.visitLabel(startLabel);
    checkMethodAdapter.visitInsn(NOP);
    checkMethodAdapter.visitLabel(endLabel);
    checkMethodAdapter.visitLabel(handlerLabel);

    Exception exception =
        assertThrows(IllegalStateException.class, () -> checkMethodAdapter.visitMaxs(0, 0));
    assertEquals("Emty try catch block handler range", exception.getMessage());
  }

  @Test
  public void testVisitEnd_invalidDataFlow() {
    MethodVisitor dataFlowCheckMethodAdapter =
        new CheckMethodAdapter(ACC_PUBLIC, "m", "(I)I", null, new HashMap<>());
    dataFlowCheckMethodAdapter.visitCode();
    dataFlowCheckMethodAdapter.visitInsn(RETURN);
    dataFlowCheckMethodAdapter.visitMaxs(0, 2);

    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> dataFlowCheckMethodAdapter.visitEnd());
    assertTrue(
        exception
            .getMessage()
            .startsWith("Error at instruction 0: Incompatible return type m(I)I"));
  }

  @Test
  public void testVisitEnd_invalidReturnType() {
    MethodVisitor dataFlowCheckMethodAdapter =
        new CheckMethodAdapter(ACC_PUBLIC, "m", "(I)V", null, new HashMap<>());
    dataFlowCheckMethodAdapter.visitCode();
    dataFlowCheckMethodAdapter.visitVarInsn(ILOAD, 1);
    dataFlowCheckMethodAdapter.visitInsn(IRETURN);
    dataFlowCheckMethodAdapter.visitMaxs(1, 2);

    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> dataFlowCheckMethodAdapter.visitEnd());
    assertTrue(
        exception
            .getMessage()
            .startsWith(
                "Error at instruction 1: Incompatible return type: expected null, but found I m(I)V"));
  }

  @Test
  public void testVisitEnd_dataflowCheckRequiresMaxLocalsAndMaxStack() {
    CheckMethodAdapter dataFlowCheckMethodAdapter =
        new CheckMethodAdapter(0, "m", "()V", null, new HashMap<>());
    dataFlowCheckMethodAdapter.visitCode();
    dataFlowCheckMethodAdapter.visitVarInsn(ALOAD, 0);
    dataFlowCheckMethodAdapter.visitInsn(RETURN);
    dataFlowCheckMethodAdapter.visitMaxs(0, 0);

    Exception exception =
        assertThrows(IllegalArgumentException.class, () -> dataFlowCheckMethodAdapter.visitEnd());
    assertEquals(
        "Data flow checking option requires valid, non zero maxLocals and maxStack.",
        exception.getMessage());
  }
}
