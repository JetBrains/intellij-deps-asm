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
 * A node that represents a try catch block.
 */

public class TryCatchBlockNode {

  /**
   * Beginning of the exception handler's scope (inclusive).
   */

  public Label start;

  /**
   * End of the exception handler's scope (exclusive).
   */

  public Label end;

  /**
   * Beginning of the exception handler's code.
   */

  public Label handler;

  /**
   * Internal name of the type of exceptions handled by the handler. May be
   * <tt>null</tt> to catch any exceptions (for "finally" blocks).
   */

  public String type;

  /**
   * Constructs a new {@link TryCatchBlockNode TryCatchBlockNode} object.
   *
   * @param start beginning of the exception handler's scope (inclusive).
   * @param end end of the exception handler's scope (exclusive).
   * @param handler beginning of the exception handler's code.
   * @param type internal name of the type of exceptions handled by the handler,
   *      or <tt>null</tt> to catch any exceptions (for "finally" blocks).
   */

  public TryCatchBlockNode (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    this.start = start;
    this.end = end;
    this.handler = handler;
    this.type = type;
  }

  /**
   * Makes the given code visitor visit this try catch block.
   *
   * @param cv a code visitor.
   */

  public void accept (final CodeVisitor cv) {
    cv.visitTryCatchBlock(start, end, handler, type);
  }
}
