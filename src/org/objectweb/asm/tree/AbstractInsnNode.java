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
 * A node that represents a bytecode instruction.
 */

public abstract class AbstractInsnNode {

  /**
   * The opcode of this instruction.
   */

  protected int opcode;

  /**
   * Constructs a new {@link AbstractInsnNode AbstractInsnNode} object.
   *
   * @param opcode the opcode of the instruction to be constructed.
   */

  protected AbstractInsnNode (final int opcode) {
    this.opcode = opcode;
  }

  /**
   * Returns the opcode of this instruction.
   *
   * @return the opcode of this instruction.
   */

  public int getOpcode () {
    return opcode;
  }

  /**
   * Makes the given code visitor visit this instruction.
   *
   * @param cv a code visitor.
   */

  public abstract void accept (final CodeVisitor cv);
}
