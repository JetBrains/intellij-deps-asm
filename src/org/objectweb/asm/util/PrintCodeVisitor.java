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

import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;

import java.util.ArrayList;

/**
 * An abstract code visitor that prints the code it visits.
 */

public abstract class PrintCodeVisitor implements CodeVisitor {

  protected final StringBuffer buf;

  protected final ArrayList dump;

  protected final static String[] OPCODES = {
    "NOP",
    "ACONST_NULL",
    "ICONST_M1",
    "ICONST_0",
    "ICONST_1",
    "ICONST_2",
    "ICONST_3",
    "ICONST_4",
    "ICONST_5",
    "LCONST_0",
    "LCONST_1",
    "FCONST_0",
    "FCONST_1",
    "FCONST_2",
    "DCONST_0",
    "DCONST_1",
    "BIPUSH",
    "SIPUSH",
    "LDC",
    null,
    null,
    "ILOAD",
    "LLOAD",
    "FLOAD",
    "DLOAD",
    "ALOAD",
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    "IALOAD",
    "LALOAD",
    "FALOAD",
    "DALOAD",
    "AALOAD",
    "BALOAD",
    "CALOAD",
    "SALOAD",
    "ISTORE",
    "LSTORE",
    "FSTORE",
    "DSTORE",
    "ASTORE",
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    null,
    "IASTORE",
    "LASTORE",
    "FASTORE",
    "DASTORE",
    "AASTORE",
    "BASTORE",
    "CASTORE",
    "SASTORE",
    "POP",
    "POP2",
    "DUP",
    "DUP_X1",
    "DUP_X2",
    "DUP2",
    "DUP2_X1",
    "DUP2_X2",
    "SWAP",
    "IADD",
    "LADD",
    "FADD",
    "DADD",
    "ISUB",
    "LSUB",
    "FSUB",
    "DSUB",
    "IMUL",
    "LMUL",
    "FMUL",
    "DMUL",
    "IDIV",
    "LDIV",
    "FDIV",
    "DDIV",
    "IREM",
    "LREM",
    "FREM",
    "DREM",
    "INEG",
    "LNEG",
    "FNEG",
    "DNEG",
    "ISHL",
    "LSHL",
    "ISHR",
    "LSHR",
    "IUSHR",
    "LUSHR",
    "IAND",
    "LAND",
    "IOR",
    "LOR",
    "IXOR",
    "LXOR",
    "IINC",
    "I2L",
    "I2F",
    "I2D",
    "L2I",
    "L2F",
    "L2D",
    "F2I",
    "F2L",
    "F2D",
    "D2I",
    "D2L",
    "D2F",
    "I2B",
    "I2C",
    "I2S",
    "LCMP",
    "FCMPL",
    "FCMPG",
    "DCMPL",
    "DCMPG",
    "IFEQ",
    "IFNE",
    "IFLT",
    "IFGE",
    "IFGT",
    "IFLE",
    "IF_ICMPEQ",
    "IF_ICMPNE",
    "IF_ICMPLT",
    "IF_ICMPGE",
    "IF_ICMPGT",
    "IF_ICMPLE",
    "IF_ACMPEQ",
    "IF_ACMPNE",
    "GOTO",
    "JSR",
    "RET",
    "TABLESWITCH",
    "LOOKUPSWITCH",
    "IRETURN",
    "LRETURN",
    "FRETURN",
    "DRETURN",
    "ARETURN",
    "RETURN",
    "GETSTATIC",
    "PUTSTATIC",
    "GETFIELD",
    "PUTFIELD",
    "INVOKEVIRTUAL",
    "INVOKESPECIAL",
    "INVOKESTATIC",
    "INVOKEINTERFACE",
    null,
    "NEW",
    "NEWARRAY",
    "ANEWARRAY",
    "ARRAYLENGTH",
    "ATHROW",
    "CHECKCAST",
    "INSTANCEOF",
    "MONITORENTER",
    "MONITOREXIT",
    null,
    "MULTIANEWARRAY",
    "IFNULL",
    "IFNONNULL",
    null,
    null
  };

  /**
   * Constructs a new {@link PrintCodeVisitor PrintCodeVisitor} object.
   */

  public PrintCodeVisitor () {
    this.buf = new StringBuffer();
    this.dump = new ArrayList();
  }

  public void visitInsn (final int opcode) {
    buf.setLength(0);
    printInsn(opcode);
    dump.add(buf.toString());
  }

  public void visitIntInsn (final int opcode, final int operand) {
    buf.setLength(0);
    printIntInsn(opcode, operand);
    dump.add(buf.toString());
  }

  public void visitVarInsn (final int opcode, final int var) {
    buf.setLength(0);
    printVarInsn(opcode, var);
    dump.add(buf.toString());
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    buf.setLength(0);
    printTypeInsn(opcode, desc);
    dump.add(buf.toString());
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.setLength(0);
    printFieldInsn(opcode, owner, name, desc);
    dump.add(buf.toString());
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.setLength(0);
    printMethodInsn(opcode, owner, name, desc);
    dump.add(buf.toString());
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    buf.setLength(0);
    printJumpInsn(opcode, label);
    dump.add(buf.toString());
  }

  public void visitLabel (final Label label) {
    buf.setLength(0);
    printLabel(label);
    dump.add(buf.toString());
  }

  public void visitLdcInsn (final Object cst) {
    buf.setLength(0);
    printLdcInsn(cst);
    dump.add(buf.toString());
  }

  public void visitIincInsn (final int var, final int increment) {
    buf.setLength(0);
    printIincInsn(var, increment);
    dump.add(buf.toString());
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    buf.setLength(0);
    printTableSwitchInsn(min, max, dflt, labels);
    dump.add(buf.toString());
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    buf.setLength(0);
    printLookupSwitchInsn(dflt, keys, labels);
    dump.add(buf.toString());
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    buf.setLength(0);
    printMultiANewArrayInsn(desc, dims);
    dump.add(buf.toString());
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    buf.setLength(0);
    printTryCatchBlock(start, end, handler, type);
    dump.add(buf.toString());
  }

  public void visitMaxs (final int maxStack, final int maxLocals) {
    buf.setLength(0);
    printMaxs(maxStack, maxLocals);
    dump.add(buf.toString());
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    buf.setLength(0);
    printLocalVariable(name, desc, start, end, index);
    dump.add(buf.toString());
  }

  public void visitLineNumber (final int line, final Label start) {
    buf.setLength(0);
    printLineNumber(line, start);
    dump.add(buf.toString());
  }

  public ArrayList getText () {
    return dump;
  }

  public abstract void printInsn (final int opcode);

  public abstract void printIntInsn (final int opcode, final int operand);

  public abstract void printVarInsn (final int opcode, final int var);

  public abstract void printTypeInsn (final int opcode, final String desc);

  public abstract void printFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc);

  public abstract void printMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc);

  public abstract void printJumpInsn (final int opcode, final Label label);

  public abstract void printLabel (final Label label);

  public abstract void printLdcInsn (final Object cst);

  public abstract void printIincInsn (final int var, final int increment);

  public abstract void printTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[]);

  public abstract void printLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[]);

  public abstract void printMultiANewArrayInsn (
    final String desc,
    final int dims);

  public abstract void printTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type);

  public abstract void printMaxs (final int maxStack, final int maxLocals);

  public abstract void printLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index);

  public abstract void printLineNumber (final int line, final Label start);
}
