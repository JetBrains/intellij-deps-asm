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

import org.objectweb.asm.Constants;
import org.objectweb.asm.CodeVisitor;

/**
 * A node that represents a MULTIANEWARRAY instruction.
 */

public class MultiANewArrayInsnNode extends AbstractInsnNode {

  /**
   * An array type descriptor (see {@link org.objectweb.asm.Type Type}).
   */

  public String desc;

  /**
   * Number of dimensions of the array to allocate.
   */

  public int dims;

  /**
   * Constructs a new {@link MultiANewArrayInsnNode MultiANewArrayInsnNode}
   * object.
   *
   * @param desc an array type descriptor (see {@link org.objectweb.asm.Type
   *      Type}).
   * @param dims number of dimensions of the array to allocate.
   */

  public MultiANewArrayInsnNode (final String desc, final int dims) {
    super(Constants.MULTIANEWARRAY);
    this.desc = desc;
    this.dims = dims;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitMultiANewArrayInsn(desc, dims);
  }
}
