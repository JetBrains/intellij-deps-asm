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

import java.util.HashMap;

/**
 * A {@link PrintCodeVisitor PrintCodeVisitor} that prints a disassembled view
 * of the code it visits.
 */

public class TraceCodeVisitor extends PrintCodeVisitor {

  /**
   * The {@link CodeVisitor CodeVisitor} to which this visitor delegates calls.
   * May be <tt>null</tt>.
   */

  protected final CodeVisitor cv;

  /**
   * The label names. This map associate String values to Label keys.
   */

  private final HashMap labelNames;

  /**
   * Constructs a new {@link TraceCodeVisitor TraceCodeVisitor} object.
   *
   * @param cv the code visitor to which this adapter must delegate calls. May
   *      be <tt>null</tt>.
   */

  public TraceCodeVisitor (final CodeVisitor cv) {
    this.cv = cv;
    this.labelNames = new HashMap();
  }

  public void printInsn (final int opcode) {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append("\n");

    if (cv != null) {
      cv.visitInsn(opcode);
    }
  }

  public void printIntInsn (final int opcode, final int operand) {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append(" ").append(operand)
      .append("\n");

    if (cv != null) {
      cv.visitIntInsn(opcode, operand);
    }
  }

  public void printVarInsn (final int opcode, final int var) {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append(" ")
      .append(var)
      .append("\n");

    if (cv != null) {
      cv.visitVarInsn(opcode, var);
    }
  }

  public void printTypeInsn (final int opcode, final String desc) {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append(" ")
      .append(desc)
      .append("\n");

    if (cv != null) {
      cv.visitTypeInsn(opcode, desc);
    }
  }

  public void printFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append(" ")
      .append(owner)
      .append(" ")
      .append(name)
      .append(" ")
      .append(desc)
      .append("\n");

    if (cv != null) {
      cv.visitFieldInsn(opcode, owner, name, desc);
    }
  }

  public void printMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.append("    ")
      .append(OPCODES[opcode])
      .append(" ")
      .append(owner)
      .append(" ")
      .append(name)
      .append(" ")
      .append(desc)
      .append("\n");

    if (cv != null) {
      cv.visitMethodInsn(opcode, owner, name, desc);
    }
  }

  public void printJumpInsn (final int opcode, final Label label) {
    buf.append("    ")
      .append(OPCODES[opcode]).
      append(" ");
    appendLabel(label);
    buf.append("\n");

    if (cv != null) {
      cv.visitJumpInsn(opcode, label);
    }
  }

  public void printLabel (final Label label) {
    buf.append("   ");
    appendLabel(label);
    buf.append(":\n");

    if (cv != null) {
      cv.visitLabel(label);
    }
  }

  public void printLdcInsn (final Object cst) {
    buf.append("    LDC ");
    if (cst instanceof String) {
      buf.append("\"").append(cst).append("\"");
    } else {
      buf.append(cst);
    }
    buf.append("\n");

    if (cv != null) {
      cv.visitLdcInsn(cst);
    }
  }

  public void printIincInsn (final int var, final int increment) {
    buf.append("    IINC ")
      .append(var)
      .append(" ")
      .append(increment)
      .append("\n");

    if (cv != null) {
      cv.visitIincInsn(var, increment);
    }
  }

  public void printTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    buf.append("    TABLESWITCH\n");
    for (int i = 0; i < labels.length; ++i) {
      buf.append("      ")
        .append(min + i)
        .append(": ");
      appendLabel(labels[i]);
      buf.append("\n");
    }
    buf.append("      default: ");
    appendLabel(dflt);
    buf.append("\n");

    if (cv != null) {
      cv.visitTableSwitchInsn(min, max, dflt, labels);
    }
  }

  public void printLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    buf.append("    LOOKUPSWITCH\n");
    for (int i = 0; i < labels.length; ++i) {
      buf.append("      ")
        .append(keys[i])
        .append(": ");
      appendLabel(labels[i]);
      buf.append("\n");
    }
    buf.append("      default: ");
    appendLabel(dflt);
    buf.append("\n");

    if (cv != null) {
      cv.visitLookupSwitchInsn(dflt, keys, labels);
    }
  }

  public void printMultiANewArrayInsn (final String desc, final int dims) {
    buf.append("    MULTIANEWARRAY ")
      .append(desc)
      .append(" ")
      .append(dims)
      .append("\n");

    if (cv != null) {
      cv.visitMultiANewArrayInsn(desc, dims);
    }
  }

  public void printTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    buf.append("    TRYCATCHBLOCK ");
    appendLabel(start);
    buf.append(" ");
    appendLabel(end);
    buf.append(" ");
    appendLabel(handler);
    buf.append(" ")
      .append(type)
      .append("\n");

    if (cv != null) {
      cv.visitTryCatchBlock(start, end, handler, type);
    }
  }

  public void printMaxs (final int maxStack, final int maxLocals) {
    buf.append("    MAXSTACK = ")
      .append(maxStack)
      .append("\n    MAXLOCALS = ")
      .append(maxLocals)
      .append("\n\n");

    if (cv != null) {
      cv.visitMaxs(maxStack, maxLocals);
    }
  }

  public void printLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    buf.append("    LOCALVARIABLE ")
      .append(name)
      .append(" ")
      .append(desc)
      .append(" ");
    appendLabel(start);
    buf.append(" ");
    appendLabel(end);
    buf.append(" ")
      .append(index)
      .append("\n");

    if (cv != null) {
      cv.visitLocalVariable(name, desc, start, end, index);
    }
  }

  public void printLineNumber (final int line, final Label start) {
    buf.append("    LINENUMBER ")
      .append(line)
      .append(" ");
    appendLabel(start);
    buf.append("\n");

    if (cv != null) {
      cv.visitLineNumber(line, start);
    }
  }

  /**
   * Appends the name of the given label to {@link #buf buf}. Creates a new
   * label name if the given label does not yet have one.
   *
   * @param l a label.
   */

  private void appendLabel (final Label l) {
    String name = (String)labelNames.get(l);
    if (name == null) {
      name = "L" + labelNames.size();
      labelNames.put(l, name);
    }
    buf.append(name);
  }
}
