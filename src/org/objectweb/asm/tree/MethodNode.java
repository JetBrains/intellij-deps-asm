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
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A node that represents a method.
 */

public class MethodNode {

  /**
   * The method's access flags (see {@link org.objectweb.asm.Constants}). This
   * field also indicates if the method is synthetic and/or deprecated.
   */

  public int access;

  /**
   * The method's name.
   */

  public String name;

  /**
   * The method's descriptor (see {@link org.objectweb.asm.Type Type}).
   */

  public String desc;

  /**
   * The internal names of the method's exception classes (see {@link
   * org.objectweb.asm.Type#getInternalName getInternalName}). This list is a
   * list of {@link String} objects.
   */

  public final List exceptions;

  /**
   * The instructions of this method. This list is a list of {@link
   * AbstractInsnNode AbstractInsnNode} and {@link Label Label} objects.
   */

  public final List instructions;

  /**
   * The try catch blocks of this method. This list is a list of {@link
   * TryCatchBlockNode TryCatchBlockNode} objects.
   */

  public final List tryCatchBlocks;

  /**
   * The maximum stack size of this method.
   */

  public int maxStack;

  /**
   * The maximum number of local variables of this method.
   */

  public int maxLocals;

  /**
   * The local variables of this method. This list is a list of {@link
   * LocalVariableNode LocalVariableNode} objects.
   */

  public final List localVariables;

  /**
   * The line numbers of this method. This list is a list of {@link
   * LineNumberNode LineNumberNode} objects.
   */

  public final List lineNumbers;

  /**
   * Constructs a new {@link MethodNode MethodNode} object.
   *
   * @param access the method's access flags (see {@link
   *      org.objectweb.asm.Constants}). This parameter also indicates if the
   *      method is synthetic and/or deprecated.
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link org.objectweb.asm.Type
   *      Type}).
   * @param exceptions the internal names of the method's exception
   *      classes (see {@link org.objectweb.asm.Type#getInternalName
   *      getInternalName}). May be <tt>null</tt>.
   */

  public MethodNode (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.exceptions = new ArrayList();
    this.instructions = new ArrayList();
    this.tryCatchBlocks = new ArrayList();
    this.localVariables = new ArrayList();
    this.lineNumbers = new ArrayList();
    if (exceptions != null) {
      this.exceptions.addAll(Arrays.asList(exceptions));
    }
  }

  /**
   * Makes the given class visitor visit this method.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    String[] exceptions = new String[this.exceptions.size()];
    this.exceptions.toArray(exceptions);
    CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions);
    if (mv != null && instructions.size() > 0) {
      int i;
      // visits instructions
      for (i = 0; i < instructions.size(); ++i) {
        Object insn = instructions.get(i);
        if (insn instanceof Label) {
          mv.visitLabel((Label)insn);
        } else {
          ((AbstractInsnNode)insn).accept(mv);
        }
      }
      // visits try catch blocks
      for (i = 0; i < tryCatchBlocks.size(); ++i) {
        ((TryCatchBlockNode)tryCatchBlocks.get(i)).accept(mv);
      }
      // visits maxs
      mv.visitMaxs(maxStack, maxLocals);
      // visits local variables
      for (i = 0; i < localVariables.size(); ++i) {
        ((LocalVariableNode)localVariables.get(i)).accept(mv);
      }
      // visits line numbers
      for (i = 0; i < lineNumbers.size(); ++i) {
        ((LineNumberNode)lineNumbers.get(i)).accept(mv);
      }
    }
  }
}
