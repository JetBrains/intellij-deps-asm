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
 * A node that represents an LDC instruction.
 */

public class LdcInsnNode extends AbstractInsnNode {

  /**
   * The constant to be loaded on the stack. This parameter must be a non null
   * {@link java.lang.Integer Integer}, a {@link java.lang.Float Float}, a
   * {@link java.lang.Long Long}, a {@link java.lang.Double Double} or a {@link
   * String String}.
   */

  public Object cst;

  /**
   * Constructs a new {@link LdcInsnNode LdcInsnNode} object.
   *
   * @param cst the constant to be loaded on the stack. This parameter must be
   *      a non null {@link java.lang.Integer Integer}, a {@link java.lang.Float
   *      Float}, a {@link java.lang.Long Long}, a {@link java.lang.Double
   *      Double} or a {@link String String}.
   */

  public LdcInsnNode (final Object cst) {
    super(Constants.LDC);
    this.cst = cst;
  }

  public void accept (final CodeVisitor cv) {
    cv.visitLdcInsn(cst);
  }
}
