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
 * A node that represents a field instruction. A field instruction is an
 * instruction that loads or stores the value of a field of an object.
 */

public class FieldInsnNode extends AbstractInsnNode {

  /**
   * The internal name of the field's owner class (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}).
   */

  public String owner;

  /**
   * The field's name.
   */

  public String name;

  /**
   * The field's descriptor (see {@link org.objectweb.asm.Type Type}).
   */

  public String desc;

  /**
   * Constructs a new {@link FieldInsnNode FieldInsnNode} object.
   *
   * @param opcode the opcode of the type instruction to be constructed. This
   *      opcode must be GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
   * @param owner the internal name of the field's owner class (see {@link
   *      org.objectweb.asm.Type#getInternalName getInternalName}).
   * @param name the field's name.
   * @param desc the field's descriptor (see {@link org.objectweb.asm.Type
   *      Type}).
   */

  public FieldInsnNode (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    super(opcode);
    this.owner = owner;
    this.name = name;
    this.desc = desc;
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be GETSTATIC,
   *      PUTSTATIC, GETFIELD or PUTFIELD.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitFieldInsn(opcode, owner, name, desc);
  }
}
