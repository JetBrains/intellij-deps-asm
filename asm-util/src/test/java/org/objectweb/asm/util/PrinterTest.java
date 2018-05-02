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

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * Printer tests.
 *
 * @author Eric Bruneton
 */
public class PrinterTest {

  static class StubPrinter extends Printer {

    StubPrinter(int api) {
      super(api);
    }

    @Override
    public void visit(
        int version,
        int access,
        String name,
        String signature,
        String superName,
        String[] interfaces) {
      // Do nothing.
    }

    @Override
    public void visitSource(String source, String debug) {
      // Do nothing.
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
      // Do nothing.
    }

    @Override
    public Printer visitClassAnnotation(String descriptor, boolean visible) {
      return null;
    }

    @Override
    public void visitClassAttribute(Attribute attribute) {
      // Do nothing.
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
      // Do nothing.
    }

    @Override
    public Printer visitField(
        int access, String name, String descriptor, String signature, Object value) {
      return null;
    }

    @Override
    public Printer visitMethod(
        int access, String name, String descriptor, String signature, String[] exceptions) {
      return null;
    }

    @Override
    public void visitClassEnd() {
      // Do nothing.
    }

    @Override
    public void visit(String name, Object value) {
      // Do nothing.
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
      // Do nothing.
    }

    @Override
    public Printer visitAnnotation(String name, String descriptor) {
      return null;
    }

    @Override
    public Printer visitArray(String name) {
      return null;
    }

    @Override
    public void visitAnnotationEnd() {
      // Do nothing.
    }

    @Override
    public Printer visitFieldAnnotation(String descriptor, boolean visible) {
      return null;
    }

    @Override
    public void visitFieldAttribute(Attribute attribute) {
      // Do nothing.
    }

    @Override
    public void visitFieldEnd() {
      // Do nothing.
    }

    @Override
    public Printer visitAnnotationDefault() {
      return null;
    }

    @Override
    public Printer visitMethodAnnotation(String descriptor, boolean visible) {
      return null;
    }

    @Override
    public Printer visitParameterAnnotation(int parameter, String descriptor, boolean visible) {
      return null;
    }

    @Override
    public void visitMethodAttribute(Attribute attribute) {
      // Do nothing.
    }

    @Override
    public void visitCode() {
      // Do nothing.
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
      // Do nothing.
    }

    @Override
    public void visitInsn(int opcode) {
      // Do nothing.
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      // Do nothing.
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      // Do nothing.
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
      // Do nothing.
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
      // Do nothing.
    }

    @Override
    public void visitInvokeDynamicInsn(
        String name,
        String descriptor,
        Handle bootstrapMethodHandle,
        Object... bootstrapMethodArguments) {
      // Do nothing.
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      // Do nothing.
    }

    @Override
    public void visitLabel(Label label) {
      // Do nothing.
    }

    @Override
    public void visitLdcInsn(Object value) {
      // Do nothing.
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      // Do nothing.
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
      // Do nothing.
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      // Do nothing.
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
      // Do nothing.
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
      // Do nothing.
    }

    @Override
    public void visitLocalVariable(
        String name, String descriptor, String signature, Label start, Label end, int index) {
      // Do nothing.
    }

    @Override
    public void visitLineNumber(int line, Label start) {
      // Do nothing.
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      // Do nothing.
    }

    @Override
    public void visitMethodEnd() {
      // Do nothing.
    }
  }

  @Test
  public void testUnsupportedOperations() {
    Printer printer = new StubPrinter(Opcodes.ASM7_EXPERIMENTAL);
    assertThrows(UnsupportedOperationException.class, () -> printer.visitModule(null, 0, null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitModule(null, 0, null));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitClassTypeAnnotation(0, null, null, false));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitMainClass(null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitPackage(null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitRequire(null, 0, null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitExport(null, 0));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitOpen(null, 0));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitUse(null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitProvide(null));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitModuleEnd());
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitFieldTypeAnnotation(0, null, null, false));
    assertThrows(UnsupportedOperationException.class, () -> printer.visitParameter(null, 0));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodTypeAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class, () -> printer.visitAnnotableParameterCount(0, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitInsnAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitTryCatchAnnotation(0, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitLocalVariableAnnotation(0, null, null, null, null, null, false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V"));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", false));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", true));
  }

  @Test
  public void testUnsupportedOperationsAsm4() {
    Printer printer = new StubPrinter(Opcodes.ASM4);
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V"));
    assertThrows(
        UnsupportedOperationException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", false));
    assertThrows(
        IllegalArgumentException.class,
        () -> printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", true));
  }

  @Test
  public void testBackwardCompatibility() {
    Printer printer =
        new StubPrinter(Opcodes.ASM5) {
          public void visitMethodInsn(
              final int opcode,
              final String owner,
              final String name,
              final String descriptor,
              final boolean isInterface) {
            // Do nothing.
          }
        };
    printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V");
  }

  @Test
  public void testBackwardCompatibilityAsm4() {
    Printer printer =
        new StubPrinter(Opcodes.ASM4) {
          public void visitMethodInsn(
              final int opcode, final String owner, final String name, final String descriptor) {
            // Do nothing.
          }
        };
    printer.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", false);
  }
}
