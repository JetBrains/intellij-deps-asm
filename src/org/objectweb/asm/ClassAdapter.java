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

package org.objectweb.asm;

/**
 * An empty {@link ClassVisitor ClassVisitor} that delegates to another {@link
 * ClassVisitor ClassVisitor}. This class can be used as a super class to
 * quickly implement usefull class adapter classes, just by overriding the
 * necessary methods.
 */

public class ClassAdapter implements ClassVisitor {

  /**
   * The {@link ClassVisitor ClassVisitor} to which this adapter delegates
   * calls.
   */

  protected ClassVisitor cv;

  /**
   * Constructs a new {@link ClassAdapter ClassAdapter} object.
   *
   * @param cv the class visitor to which this adapter must delegate calls.
   */

  public ClassAdapter (final ClassVisitor cv) {
    this.cv = cv;
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    cv.visit(access, name, superName, interfaces, sourceFile);
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    cv.visitInnerClass(name, outerName, innerName, access);
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    cv.visitField(access, name, desc, value);
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    return new CodeAdapter(cv.visitMethod(access, name, desc, exceptions));
  }

  public void visitEnd () {
    cv.visitEnd();
  }
}
