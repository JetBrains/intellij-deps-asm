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
 * A node that represents a field.
 */

public class FieldNode {

  /**
   * The field's access flags (see {@link org.objectweb.asm.Constants}). This
   * field also indicates if the field is synthetic and/or deprecated.
   */

  public int access;

  /**
   * The field's name.
   */

  public String name;

  /**
   * The field's descriptor (see {@link org.objectweb.asm.Type Type}).
   */

  public String desc;

  /**
   * The field's initial value. This field, which may be <tt>null</tt> if the
   * field does not have an initial value, must be an {@link java.lang.Integer
   * Integer}, a {@link java.lang.Float Float}, a {@link java.lang.Long Long},
   * a {@link java.lang.Double Double} or a {@link String String}.
   */

  public Object value;

  /**
   * Constructs a new {@link FieldNode FieldNode} object.
   *
   * @param access the field's access flags (see {@link
   *      org.objectweb.asm.Constants}). This parameter also indicates if the
   *      field is synthetic and/or deprecated.
   * @param name the field's name.
   * @param desc the field's descriptor (see {@link org.objectweb.asm.Type
   *      Type}).
   * @param value the field's initial value. This parameter, which may be
   *      <tt>null</tt> if the field does not have an initial value, must be an
   *      {@link java.lang.Integer Integer}, a {@link java.lang.Float Float}, a
   *      {@link java.lang.Long Long}, a {@link java.lang.Double Double} or a
   *      {@link String String}.
   */

  public FieldNode (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.value = value;
  }

  /**
   * Makes the given class visitor visit this field.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    cv.visitField(access, name, desc, value);
  }
}
