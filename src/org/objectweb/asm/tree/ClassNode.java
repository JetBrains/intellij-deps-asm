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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A node that represents a class.
 */

public class ClassNode {

  /**
   * The class's access flags (see {@link org.objectweb.asm.Constants}). This
   * field also indicates if the class is deprecated.
   */

  public int access;

  /**
   * The internal name of the class (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}).
   */

  public String name;

  /**
   * The internal of name of the super class (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}). For interfaces,
   * the super class is {@link Object}.
   */

  public String superName;

  /**
   * The internal names of the class's interfaces (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}). This list is a
   * list of {@link String} objects.
   */

  public final List interfaces;

  /**
   * The name of the source file from which this class was compiled. May be
   * <tt>null</tt>.
   */

  public String sourceFile;

  /**
   * Informations about the inner classes of this class. This list is a list of
   * {@link InnerClassNode InnerClassNode} objects.
   */

  public final List innerClasses;

  /**
   * The fields of this class. This list is a list of {@link FieldNode
   * FieldNode} objects.
   */

  public final List fields;

  /**
   * The methods of this class. This list is a list of {@link MethodNode
   * MethodNode} objects.
   */

  public final List methods;

  /**
   * Constructs a new {@link ClassNode ClassNode} object.
   *
   * @param access the class's access flags (see {@link
   *      org.objectweb.asm.Constants}). This parameter also indicates if the
   *      class is deprecated.
   * @param name the internal name of the class (see {@link
   *      org.objectweb.asm.Type#getInternalName getInternalName}).
   * @param superName the internal of name of the super class (see {@link
   *      org.objectweb.asm.Type#getInternalName getInternalName}). For
   *      interfaces, the super class is {@link Object}.
   * @param interfaces the internal names of the class's interfaces (see {@link
   *      org.objectweb.asm.Type#getInternalName getInternalName}). May be
   *      <tt>null</tt>.
   * @param sourceFile the name of the source file from which this class was
   *      compiled. May be <tt>null</tt>.
   */

  public ClassNode (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    this.access = access;
    this.name = name;
    this.superName = superName;
    this.interfaces = new ArrayList();
    this.sourceFile = sourceFile;
    this.innerClasses = new ArrayList();
    this.fields = new ArrayList();
    this.methods = new ArrayList();
    if (interfaces != null) {
      this.interfaces.addAll(Arrays.asList(interfaces));
    }
  }

  /**
   * Makes the given class visitor visit this class.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    // visits header
    String[] interfaces = new String[this.interfaces.size()];
    this.interfaces.toArray(interfaces);
    cv.visit(access, name, superName, interfaces, sourceFile);
    // visits inner classes
    int i;
    for (i = 0; i < innerClasses.size(); ++i) {
      ((InnerClassNode)innerClasses.get(i)).accept(cv);
    }
    // visits fields
    for (i = 0; i < fields.size(); ++i) {
      ((FieldNode)fields.get(i)).accept(cv);
    }
    // visits methods
    for (i = 0; i < methods.size(); ++i) {
      ((MethodNode)methods.get(i)).accept(cv);
    }
    // visits end
    cv.visitEnd();
  }
}
