/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.objectweb.asm.util;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.util.HashMap;

/**
 * A {@link MethodVisitor} that prints a disassembled view of the methods it
 * visits.
 *
 * @author Eric Bruneton
 */

public class TraceMethodVisitor extends TraceAbstractVisitor
  implements MethodVisitor
{

  /**
   * Tab for bytecode instructions.
   */

  protected String tab2 = "    ";

  /**
   * Tab for table and lookup switch instructions.
   */

  protected String tab3 = "      ";

  /**
   * Tab for labels.
   */

  protected String ltab = "   ";

  /**
   * The label names. This map associate String values to Label keys.
   */

  protected final HashMap labelNames;

  /**
   * Constructs a new {@link TraceMethodVisitor}.
   */

  public TraceMethodVisitor () {
    this.labelNames = new HashMap();
  }

  // --------------------------------------------------------------------------
  // Implementation of the MethodVisitor interface
  // --------------------------------------------------------------------------

  public AnnotationVisitor visitAnnotationDefault () {
    text.add(tab2 + "default=");
    TraceAnnotationVisitor tav = new TraceAnnotationVisitor();
    text.add(tav.getText());
    text.add("\n");
    return tav;
  }

  public AnnotationVisitor visitParameterAnnotation (
    final int parameter,
    final String desc,
    final boolean visible)
  {
    buf.setLength(0);
    buf.append(tab2).append('@');
    appendDescriptor(FIELD_DESCRIPTOR, desc);
    buf.append('(');
    text.add(buf.toString());
    TraceAnnotationVisitor tav = new TraceAnnotationVisitor();
    text.add(tav.getText());
    text.add(visible ? ") // parameter " : ") // invisible, parameter ");
    text.add(new Integer(parameter));
    text.add("\n");
    return tav;
  }

  public void visitCode () {
      // does nothing
  }

  public void visitInsn (final int opcode) {
    buf.setLength(0);
    buf.append(tab2).append(OPCODES[opcode]).append('\n');
    text.add(buf.toString());
  }

  public void visitIntInsn (final int opcode, final int operand) {
    buf.setLength(0);
    buf.append(tab2)
      .append(OPCODES[opcode])
      .append(' ').append(operand)
      .append('\n');
    text.add(buf.toString());
  }

  public void visitVarInsn (final int opcode, final int var) {
    buf.setLength(0);
    buf.append(tab2)
      .append(OPCODES[opcode])
      .append(' ')
      .append(var)
      .append('\n');
    text.add(buf.toString());
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    buf.setLength(0);
    buf.append(tab2).append(OPCODES[opcode]).append(' ');
    if (desc.startsWith("[")) {
      appendDescriptor(FIELD_DESCRIPTOR, desc);
    } else {
      appendDescriptor(INTERNAL_NAME, desc);
    }
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.setLength(0);
    buf.append(tab2).append(OPCODES[opcode]).append(' ');
    appendDescriptor(INTERNAL_NAME, owner);
    buf.append('.').append(name).append(" : ");
    appendDescriptor(FIELD_DESCRIPTOR, desc);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    buf.setLength(0);
    buf.append(tab2).append(OPCODES[opcode]).append(' ');
    appendDescriptor(INTERNAL_NAME, owner);
    buf.append('.').append(name).append(' ');
    appendDescriptor(METHOD_DESCRIPTOR, desc);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    buf.setLength(0);
    buf.append(tab2).append(OPCODES[opcode]).append(' ');
    appendLabel(label);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitLabel (final Label label) {
    buf.setLength(0);
    buf.append(ltab);
    appendLabel(label);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitLdcInsn (final Object cst) {
    buf.setLength(0);
    buf.append(tab2).append("LDC ");
    if (cst instanceof String) {
      if (cst == null) {
        buf.append("null");
      } else if (cst instanceof String) {
        AbstractVisitor.appendString(buf, (String)cst);
      }
    } else if (cst instanceof Type) {
      buf.append(((Type)cst).getDescriptor() + ".class");
    } else {
      buf.append(cst);
    }
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitIincInsn (final int var, final int increment) {
    buf.setLength(0);
    buf.append(tab2)
      .append("IINC ")
      .append(var)
      .append(' ')
      .append(increment)
      .append('\n');
    text.add(buf.toString());
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    buf.setLength(0);
    buf.append(tab2).append("TABLESWITCH\n");
    for (int i = 0; i < labels.length; ++i) {
      buf.append(tab3).append(min + i).append(": ");
      appendLabel(labels[i]);
      buf.append('\n');
    }
    buf.append(tab3).append("default: ");
    appendLabel(dflt);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    buf.setLength(0);
    buf.append(tab2).append("LOOKUPSWITCH\n");
    for (int i = 0; i < labels.length; ++i) {
      buf.append(tab3).append(keys[i]).append(": ");
      appendLabel(labels[i]);
      buf.append('\n');
    }
    buf.append(tab3).append("default: ");
    appendLabel(dflt);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    buf.setLength(0);
    buf.append(tab2).append("MULTIANEWARRAY ");
    appendDescriptor(FIELD_DESCRIPTOR, desc);
    buf.append(' ').append(dims).append('\n');
    text.add(buf.toString());
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    buf.setLength(0);
    buf.append(tab2).append("TRYCATCHBLOCK ");
    appendLabel(start);
    buf.append(' ');
    appendLabel(end);
    buf.append(' ');
    appendLabel(handler);
    buf.append(' ');
    appendDescriptor(INTERNAL_NAME, type);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final String signature,
    final Label start,
    final Label end,
    final int index)
  {
    buf.setLength(0);
    buf.append(tab2).append("LOCALVARIABLE ").append(name).append(' ');
    appendDescriptor(FIELD_DESCRIPTOR, desc);
    buf.append(' ');
    appendLabel(start);
    buf.append(' ');
    appendLabel(end);
    buf.append(' ').append(index);
    if(signature != null){
        buf.append(' ');
        appendDescriptor(FIELD_SIGNATURE, signature);
    } else {
        buf.append('\n');
    }
    text.add(buf.toString());
  }

  public void visitLineNumber (final int line, final Label start) {
    buf.setLength(0);
    buf.append(tab2).append("LINENUMBER ").append(line).append(' ');
    appendLabel(start);
    buf.append('\n');
    text.add(buf.toString());
  }

  public void visitMaxs (final int maxStack, final int maxLocals) {
    buf.setLength(0);
    buf.append(tab2).append("MAXSTACK = ")
      .append(maxStack)
      .append('\n')
      .append(tab2)
      .append("MAXLOCALS = ")
      .append(maxLocals)
      .append('\n');
    text.add(buf.toString());
  }

  // --------------------------------------------------------------------------
  // Utility methods
  // --------------------------------------------------------------------------

  /**
   * Appends the name of the given label to {@link #buf buf}. Creates a new
   * label name if the given label does not yet have one.
   *
   * @param l a label.
   */

  protected void appendLabel (final Label l) {
    String name = (String)labelNames.get(l);
    if (name == null) {
      name = "L" + labelNames.size();
      labelNames.put(l, name);
    }
    buf.append(name);
  }
}
