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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.ClassVisitor;

/**
 * A {@link ClassAdapter ClassAdapter} that constructs a tree representation of
 * the classes it vists. Each <tt>visit</tt><i>XXX</i> method of this class
 * constructs an <i>XXX</i><tt>Node</tt> and adds it to the {@link #classNode
 * classNode} node (except the {@link #visitEnd visitEnd} method, which just
 * makes the {@link #cv cv} class visitor visit the tree that has just been
 * constructed).
 * <p>
 * In order to implement a usefull class adapter based on a tree representation
 * of classes, one just need to override the {@link #visitEnd visitEnd} method
 * with a method of the following form:
 * <pre>
 * public void visitEnd () {
 *   // ...
 *   // code to modify the classNode tree, can be arbitrary complex
 *   // ...
 *   // makes the cv visitor visit this modified class:
 *   classNode.accept(cv);
 * }
 * </pre>
 */

public class TreeClassAdapter extends ClassAdapter {

  /**
   * A tree representation of the class that is being visited by this visitor.
   */

  public ClassNode classNode;

  /**
   * Constructs a new {@link TreeClassAdapter TreeClassAdapter} object.
   *
   * @param cv the class visitor to which this adapter must delegate calls.
   */

  public TreeClassAdapter (final ClassVisitor cv) {
    super(cv);
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    classNode = new ClassNode(access, name, superName, interfaces, sourceFile);
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    InnerClassNode icn = new InnerClassNode(name, outerName, innerName, access);
    classNode.innerClasses.add(icn);
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    FieldNode fn = new FieldNode(access, name, desc, value);
    classNode.fields.add(fn);
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    MethodNode mn = new MethodNode(access, name, desc, exceptions);
    classNode.methods.add(mn);
    return new TreeCodeAdapter(mn);
  }

  public void visitEnd () {
    classNode.accept(cv);
  }
}
