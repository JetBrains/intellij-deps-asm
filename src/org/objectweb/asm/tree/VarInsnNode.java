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
 * A node that represents a local variable instruction. A local variable
 * instruction is an instruction that loads or stores the value of a local
 * variable.
 */

public class VarInsnNode extends AbstractInsnNode {

  /**
   * The operand of this instruction. This operand is the index of a local
   * variable.
   */

  public int var;

  /**
   * Visits a local variable instruction. A local variable instruction is an
   * instruction that loads or stores the value of a local variable.
   *
   * @param opcode the opcode of the local variable instruction to be
   *      constructed. This opcode must be ILOAD, LLOAD, FLOAD, DLOAD, ALOAD,
   *      ISTORE, LSTORE, FSTORE, DSTORE, ASTORE or RET.
   * @param var the operand of the instruction to be constructed. This operand
   *      is the index of a local variable.
   */

  public VarInsnNode (final int opcode, final int var) {
    super(opcode);
    this.var = var;
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be ILOAD, LLOAD,
   *      FLOAD, DLOAD, ALOAD, ISTORE, LSTORE, FSTORE, DSTORE, ASTORE or RET.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitVarInsn(opcode, var);
  }
}
