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
import org.objectweb.asm.Label;

/**
 * A node that represents a local variable declaration.
 */

public class LocalVariableNode {

  /**
   * The name of a local variable.
   */

  public String name;

  /**
   * The type descriptor of this local variable.
   */

  public String desc;

  /**
   * The first instruction corresponding to the scope of this local variable
   * (inclusive).
   */

  public Label start;

  /**
   * The last instruction corresponding to the scope of this local variable
   * (exclusive).
   */

  public Label end;

  /**
   * The local variable's index.
   */

  public int index;

  /**
   * Constructs a new {@link LocalVariableNode LocalVariableNode} object.
   *
   * @param name the name of a local variable.
   * @param desc the type descriptor of this local variable.
   * @param start the first instruction corresponding to the scope of this
   *      local variable (inclusive).
   * @param end the last instruction corresponding to the scope of this
   *      local variable (exclusive).
   * @param index the local variable's index.
   */

  public LocalVariableNode (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    this.name = name;
    this.desc = desc;
    this.start = start;
    this.end = end;
    this.index = index;
  }

  /**
   * Makes the given code visitor visit this local variable declaration.
   *
   * @param cv a code visitor.
   */

  public void accept (final CodeVisitor cv) {
    cv.visitLocalVariable(name, desc, start, end, index);
  }
}
