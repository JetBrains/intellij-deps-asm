/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm;

/**
 * An empty {@link CodeVisitor CodeVisitor} that delegates to another {@link
 * CodeVisitor CodeVisitor}. This class can be used as a super class to quickly
 * implement usefull code adapter classes, just by overriding the necessary
 * methods.
 */

public class CodeAdapter implements CodeVisitor {

  /**
   * The {@link CodeVisitor CodeVisitor} to which this adapter delegates calls.
   */

  protected final CodeVisitor cv;

  /**
   * Constructs a new {@link CodeAdapter CodeAdapter} object.
   *
   * @param cv the code visitor to which this adapter must delegate calls.
   */

  public CodeAdapter (final CodeVisitor cv) {
    this.cv = cv;
  }

  public void visitInsn (final int opcode) {
    cv.visitInsn(opcode);
  }

  public void visitIntInsn (final int opcode, final int operand) {
    cv.visitIntInsn(opcode, operand);
  }

  public void visitVarInsn (final int opcode, final int var) {
    cv.visitVarInsn(opcode, var);
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    cv.visitTypeInsn(opcode, desc);
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    cv.visitFieldInsn(opcode, owner, name, desc);
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    cv.visitMethodInsn(opcode, owner, name, desc);
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    cv.visitJumpInsn(opcode, label);
  }

  public void visitLabel (final Label label) {
    cv.visitLabel(label);
  }

  public void visitLdcInsn (final Object cst) {
    cv.visitLdcInsn(cst);
  }

  public void visitIincInsn (final int var, final int increment) {
    cv.visitIincInsn(var, increment);
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    cv.visitTableSwitchInsn(min, max, dflt, labels);
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    cv.visitLookupSwitchInsn(dflt, keys, labels);
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    cv.visitMultiANewArrayInsn(desc, dims);
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    cv.visitTryCatchBlock(start, end, handler, type);
  }

  public void visitMaxs (final int maxStack, final int maxLocals) {
    cv.visitMaxs(maxStack, maxLocals);
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    cv.visitLocalVariable(name, desc, start, end, index);
  }

  public void visitLineNumber (final int line, final Label start) {
    cv.visitLineNumber(line, start);
  }
}
