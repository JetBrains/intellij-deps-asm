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
 * A node that represents a line number declaration.
 */

public class LineNumberNode {

  /**
   * A line number. This number refers to the source file from which the class
   * was compiled.
   */

  public int line;

  /**
   * The first instruction corresponding to this line number.
   */

  public Label start;

  /**
   * Constructs a new {@link LineNumberNode LineNumberNode} object.
   *
   * @param line a line number. This number refers to the source file
   *      from which the class was compiled.
   * @param start the first instruction corresponding to this line number.
   */

  public LineNumberNode (final int line, final Label start) {
    this.line = line;
    this.start = start;
  }

  /**
   * Makes the given code visitor visit this line number declaration.
   *
   * @param cv a code visitor.
   */

  public void accept (final CodeVisitor cv) {
    cv.visitLineNumber(line, start);
  }
}
