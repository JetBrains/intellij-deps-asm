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

import org.objectweb.asm.Label;
import org.objectweb.asm.CodeVisitor;

/**
 * A node that represents a jump instruction. A jump instruction is an
 * instruction that may jump to another instruction.
 */

public class JumpInsnNode extends AbstractInsnNode {

  /**
   * The operand of this instruction. This operand is a label that designates
   * the instruction to which this instruction may jump.
   */

  public Label label;

  /**
   * Constructs a new {@link JumpInsnNode JumpInsnNode} object.
   *
   * @param opcode the opcode of the type instruction to be constructed. This
   *      opcode must be IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ,
   *      IF_ICMPNE, IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ,
   *      IF_ACMPNE, GOTO, JSR, IFNULL or IFNONNULL.
   * @param label the operand of the instruction to be constructed. This operand
   *      is a label that designates the instruction to which the jump
   *      instruction may jump.
   */

  public JumpInsnNode (final int opcode, final Label label) {
    super(opcode);
    this.label = label;
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be
   *      IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE,
   *      IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
   *      GOTO, JSR, IFNULL or IFNONNULL.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitJumpInsn(opcode, label);
  }
}
