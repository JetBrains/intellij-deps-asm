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

package org.objectweb.asm.tree;

import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.Label;

/**
 * A {@link CodeAdapter CodeAdapter} that constructs a tree representation of
 * the methods it vists. Each <tt>visit</tt><i>XXX</i> method of this class
 * constructs an <i>XXX</i><tt>Node</tt> and adds it to the {@link #methodNode
 * methodNode} node.
 */

public class TreeCodeAdapter extends CodeAdapter {

  /**
   * A tree representation of the method that is being visited by this visitor.
   */

  public MethodNode methodNode;

  /**
   * Constructs a new {@link TreeCodeAdapter TreeCodeAdapter} object.
   *
   * @param methodNode the method node to be used to store the tree
   *      representation constructed by this code visitor.
   */

  public TreeCodeAdapter (final MethodNode methodNode) {
    super(null);
    this.methodNode = methodNode;
  }

  public void visitInsn (final int opcode) {
    AbstractInsnNode n = new InsnNode(opcode);
    methodNode.instructions.add(n);
  }

  public void visitIntInsn (final int opcode, final int operand) {
    AbstractInsnNode n = new IntInsnNode(opcode, operand);
    methodNode.instructions.add(n);
  }

  public void visitVarInsn (final int opcode, final int var) {
    AbstractInsnNode n = new VarInsnNode(opcode, var);
    methodNode.instructions.add(n);
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    AbstractInsnNode n = new TypeInsnNode(opcode, desc);
    methodNode.instructions.add(n);
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    AbstractInsnNode n = new FieldInsnNode(opcode, owner, name, desc);
    methodNode.instructions.add(n);
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    AbstractInsnNode n = new MethodInsnNode(opcode, owner, name, desc);
    methodNode.instructions.add(n);
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    AbstractInsnNode n = new JumpInsnNode(opcode, label);
    methodNode.instructions.add(n);
  }

  public void visitLabel (final Label label) {
    methodNode.instructions.add(label);
  }

  public void visitLdcInsn (final Object cst) {
    AbstractInsnNode n = new LdcInsnNode(cst);
    methodNode.instructions.add(n);
  }

  public void visitIincInsn (final int var, final int increment) {
    AbstractInsnNode n = new IincInsnNode(var, increment);
    methodNode.instructions.add(n);
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    AbstractInsnNode n = new TableSwitchInsnNode(min, max, dflt, labels);
    methodNode.instructions.add(n);
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    AbstractInsnNode n = new LookupSwitchInsnNode(dflt, keys, labels);
    methodNode.instructions.add(n);
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    AbstractInsnNode n = new MultiANewArrayInsnNode(desc, dims);
    methodNode.instructions.add(n);
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    TryCatchBlockNode n = new TryCatchBlockNode(start, end, handler, type);
    methodNode.tryCatchBlocks.add(n);
  }

  public void visitMaxs (final int maxStack, final int maxLocals) {
    methodNode.maxStack = maxStack;
    methodNode.maxLocals = maxLocals;
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    LocalVariableNode n = new LocalVariableNode(name, desc, start, end, index);
    methodNode.localVariables.add(n);
  }

  public void visitLineNumber (final int line, final Label start) {
    LineNumberNode n = new LineNumberNode(line, start);
    methodNode.lineNumbers.add(n);
  }
}
