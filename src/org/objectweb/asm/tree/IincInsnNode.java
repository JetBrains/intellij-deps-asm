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
import org.objectweb.asm.Constants;

/**
 * A node that represents an IINC instruction.
 */

public class IincInsnNode extends AbstractInsnNode {

  /**
   * Index of the local variable to be incremented.
   */

  public int var;

  /**
   * Amount to increment the local variable by.
   */

  public int incr;

  /**
   * Constructs a new {@link IincInsnNode IincInsnNode} node.
   *
   * @param var index of the local variable to be incremented.
   * @param incr increment amount to increment the local variable by.
   */

  public IincInsnNode (final int var, final int incr) {
    super(Constants.IINC);
    this.var = var;
    this.incr = incr;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitIincInsn(var, incr);
  }
}
