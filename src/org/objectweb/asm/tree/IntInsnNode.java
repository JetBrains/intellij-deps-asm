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

import org.objectweb.asm.CodeVisitor;

/**
 * A node that represents an instruction with a single int operand.
 */

public class IntInsnNode extends AbstractInsnNode {

  /**
   * The operand of this instruction.
   */

  public int operand;

  /**
   * Constructs a new {@link IntInsnNode IntInsnNode} object.
   *
   * @param opcode the opcode of the instruction to be constructed. This opcode
   *      must be BIPUSH, SIPUSH or NEWARRAY.
   * @param operand the operand of the instruction to be constructed.
   */

  public IntInsnNode (final int opcode, final int operand) {
    super(opcode);
    this.operand = operand;
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be BIPUSH,
   *      SIPUSH or NEWARRAY.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitIntInsn(opcode, operand);
  }
}
