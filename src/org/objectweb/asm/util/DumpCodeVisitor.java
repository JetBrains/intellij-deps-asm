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

package org.objectweb.asm.util;

import org.objectweb.asm.Label;

import java.util.HashMap;

/**
 * A {@link PrintCodeVisitor PrintCodeVisitor} that prints the ASM code that
 * generates the code it visits.
 */

public class DumpCodeVisitor extends PrintCodeVisitor {

  /**
   * The label names. This map associate String values to Label keys.
   */

  private final HashMap labelNames;

  /**
   * Constructs a new {@link DumpCodeVisitor DumpCodeVisitor} object.
   */

  public DumpCodeVisitor () {
    this.labelNames = new HashMap();
  }

  public void printInsn (final int opcode) {
    buf.append("cv.visitInsn(").
      append(OPCODES[opcode]).
      append(");\n");
  }

  public void printIntInsn (final int opcode, final int operand) {
    buf.append("cv.visitIntInsn(").
      append(OPCODES[opcode]).
      append(", ").
      append(operand).
      append(");\n");
  }

  public void printVarInsn (final int opcode, final int var) {
    buf.append("cv.visitVarInsn(").
      append(OPCODES[opcode]).
      append(", ").
      append(var).
      append(");\n");
  }

  public void printTypeInsn (final int opcode, final String desc) {
    buf.append("cv.visitTypeInsn(").
      append(OPCODES[opcode]).
      append(", ");
    DumpClassVisitor.appendConstant(buf, desc);
    buf.append(");\n");
  }

  public void printFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.append("cv.visitFieldInsn(")
      .append(OPCODES[opcode])
      .append(", ");
    DumpClassVisitor.appendConstant(buf, owner);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, name);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, desc);
    buf.append(");\n");
  }

  public void printMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.append("cv.visitMethodInsn(")
      .append(OPCODES[opcode])
      .append(", ");
    DumpClassVisitor.appendConstant(buf, owner);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, name);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, desc);
    buf.append(");\n");
  }

  public void printJumpInsn (final int opcode, final Label label) {
    declareLabel(label);
    buf.append("cv.visitJumpInsn(")
      .append(OPCODES[opcode])
      .append(", ");
    appendLabel(label);
    buf.append(");\n");
  }

  public void printLabel (final Label label) {
    declareLabel(label);
    buf.append("cv.visitLabel(");
    appendLabel(label);
    buf.append(");\n");
  }

  public void printLdcInsn (final Object cst) {
    buf.append("cv.visitLdcInsn(");
    DumpClassVisitor.appendConstant(buf, cst);
    buf.append(");\n");
  }

  public void printIincInsn (final int var, final int increment) {
    buf.append("cv.visitIincInsn(")
      .append(var)
      .append(", ")
      .append(increment)
      .append(");\n");
  }

  public void printTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    for (int i = 0; i < labels.length; ++i) {
      declareLabel(labels[i]);
    }
    declareLabel(dflt);

    buf.append("cv.visitTableSwitchInsn(")
      .append(min)
      .append(", ")
      .append(max)
      .append(", ");
    appendLabel(dflt);
    buf.append(", new Label[] {");
    for (int i = 0; i < labels.length; ++i) {
      buf.append(i == 0 ? " " : ", ");
      appendLabel(labels[i]);
    }
    buf.append(" });\n");
  }

  public void printLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    for (int i = 0; i < labels.length; ++i) {
      declareLabel(labels[i]);
    }
    declareLabel(dflt);

    buf.append("cv.visitLookupSwitchInsn(");
    appendLabel(dflt);
    buf.append(", new int[] {");
    for (int i = 0; i < keys.length; ++i) {
      buf.append(i == 0 ? " " : ", ").append(keys[i]);
    }
    buf.append(" }, new Label[] {");
    for (int i = 0; i < labels.length; ++i) {
      buf.append(i == 0 ? " " : ", ");
      appendLabel(labels[i]);
    }
    buf.append(" });\n");
  }

  public void printMultiANewArrayInsn (final String desc, final int dims) {
    buf.append("cv.visitMultiANewArrayInsn(");
    DumpClassVisitor.appendConstant(buf, desc);
    buf.append(", ")
      .append(dims)
      .append(");\n");
  }

  public void printTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    buf.append("cv.visitTryCatchBlock(");
    appendLabel(start);
    buf.append(", ");
    appendLabel(end);
    buf.append(", ");
    appendLabel(handler);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, type);
    buf.append(");\n");
  }

  public void printMaxs (final int maxStack, final int maxLocals) {
    buf.append("cv.visitMaxs(")
      .append(maxStack)
      .append(", ")
      .append(maxLocals)
      .append(");\n");
  }

  public void printLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    buf.append("cv.visitLocalVariable(");
    DumpClassVisitor.appendConstant(buf, name);
    buf.append(", ");
    DumpClassVisitor.appendConstant(buf, desc);
    buf.append(", ");
    appendLabel(start);
    buf.append(", ");
    appendLabel(end);
    buf.append(", ").append(index).append(");\n");
  }

  public void printLineNumber (final int line, final Label start) {
    buf.append("cv.visitLineNumber(")
      .append(line)
      .append(", ");
    appendLabel(start);
    buf.append(");\n");
  }

  /**
   * Appends a declaration of the given label to {@link #buf buf}. This
   * declaration is of the form "Label lXXX = new Label();". Does nothing
   * if the given label has already been declared.
   *
   * @param l a label.
   */

  private void declareLabel (final Label l) {
    String name = (String)labelNames.get(l);
    if (name == null) {
      name = "l" + labelNames.size();
      labelNames.put(l, name);
      buf.append("Label ")
        .append(name)
        .append(" = new Label();\n");
    }
  }

  /**
   * Appends the name of the given label to {@link #buf buf}. The given label
   * <i>must</i> already have a name. One way to ensure this is to always call
   * {@link #declareLabel declared} before calling this method.
   *
   * @param l a label.
   */

  private void appendLabel (final Label l) {
    buf.append((String)labelNames.get(l));
  }
}
