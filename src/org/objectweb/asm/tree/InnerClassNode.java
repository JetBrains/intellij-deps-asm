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

import org.objectweb.asm.ClassVisitor;

/**
 * A node that represents an inner class.
 */

public class InnerClassNode {

  /**
   * The internal name of an inner class (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}).
   */

  public String name;

  /**
   * The internal name of the class to which the inner class belongs (see
   * {@link org.objectweb.asm.Type#getInternalName getInternalName}). May be
   * <tt>null</tt>.
   */

  public String outerName;

  /**
   * The (simple) name of the inner class inside its enclosing class. May be
   * <tt>null</tt> for anonymous inner classes.
   */

  public String innerName;

  /**
   * The access flags of the inner class as originally declared in the enclosing
   * class.
   */

  public int access;

  /**
   * Constructs a new {@link InnerClassNode InnerClassNode} object.
   *
   * @param name the internal name of an inner class (see {@link
   *      org.objectweb.asm.Type#getInternalName getInternalName}).
   * @param outerName the internal name of the class to which the inner class
   *      belongs (see {@link org.objectweb.asm.Type#getInternalName
   *      getInternalName}). May be <tt>null</tt>.
   * @param innerName the (simple) name of the inner class inside its enclosing
   *      class. May be <tt>null</tt> for anonymous inner classes.
   * @param access the access flags of the inner class as originally declared
   *      in the enclosing class.
   */

  public InnerClassNode (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    this.name = name;
    this.outerName = outerName;
    this.innerName = innerName;
    this.access = access;
  }

  /**
   * Makes the given class visitor visit this inner class.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    cv.visitInnerClass(name, outerName, innerName, access);
  }
}
