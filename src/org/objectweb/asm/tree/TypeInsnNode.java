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
 * A node that represents a type instruction. A type instruction is an
 * instruction that takes a type descriptor as parameter.
 */

public class TypeInsnNode extends AbstractInsnNode {

  /**
   * The operand of this instruction. This operand is a type descriptor (see
   * {@link org.objectweb.asm.Type Type}).
   */

  public String desc;

  /**
   * Constructs a new {@link TypeInsnNode TypeInsnNode} object.
   *
   * @param opcode the opcode of the type instruction to be constructed. This
   *      opcode must be NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
   * @param desc the operand of the instruction to be constructed. This operand
   *      is a type descriptor (see {@link org.objectweb.asm.Type Type}).
   */

  public TypeInsnNode (final int opcode, final String desc) {
    super(opcode);
    this.desc = desc;
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be
   *      NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitTypeInsn(opcode, desc);
  }
}
