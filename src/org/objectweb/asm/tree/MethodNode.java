/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.objectweb.asm.tree;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * A node that represents a method.
 * 
 * @author Eric Bruneton
 */

public class MethodNode extends MemberNode implements MethodVisitor {

  /**
   * The method's access flags (see {@link Opcodes}). This
   * field also indicates if the method is synthetic and/or deprecated.
   */

  public int access;

  /**
   * The method's name.
   */

  public String name;

  /**
   * The method's descriptor (see {@link Type}).
   */

  public String desc;

  /**
   * The method's signature. May be <tt>null</tt>.
   */
  
  public String signature;
  
  /**
   * The internal names of the method's exception classes (see 
   * {@link Type#getInternalName() getInternalName}). This list is a list of 
   * {@link String} objects.
   */

  public final List exceptions;

  /**
   * The default value of this annotation interface method. This field must be
   * a {@link Byte}, {@link Boolean}, {@link Character}, {@link Short}, 
   * {@link Integer}, {@link Long}, {@link Float}, {@link Double}, 
   * {@link String} or {@link Type}, or an two elements String array (for 
   * enumeration values), a {@link AnnotationNode}, or a {@link List} of values 
   * of one of the preceding types. May be <tt>null</tt>.
   */
  
  public Object annotationDefault;
  
  /**
   * The runtime visible parameter annotations of this method. These lists are
   * lists of {@link AnnotationNode} objects.
   */
  
  public List[] visibleParameterAnnotations;
  
  /**
   * The runtime invisible parameter annotations of this method. These lists are
   * lists of {@link AnnotationNode} objects.
   */
  
  public List[] invisibleParameterAnnotations;

  /**
   * The instructions of this method. This list is a list of 
   * {@link AbstractInsnNode} objects.
   */

  public final List instructions;

  /**
   * The try catch blocks of this method. This list is a list of 
   * {@link TryCatchBlockNode} objects.
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
   * The local variables of this method. This list is a list of 
   * {@link LocalVariableNode} objects.
   */

  public final List localVariables;

  /**
   * The line numbers of this method. This list is a list of
   * {@link LineNumberNode} objects.
   */

  public final List lineNumbers;

  /**
   * Constructs a new {@link MethodNode}.
   *
   * @param access the method's access flags (see {@link Opcodes}). This 
   *      parameter also indicates if the method is synthetic and/or deprecated.
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link Type}).
   * @param signature the method's signature. May be <tt>null</tt>.
   * @param exceptions the internal names of the method's exception
   *      classes (see {@link Type#getInternalName() getInternalName}). 
   *      May be <tt>null</tt>.
   */

  public MethodNode (
    final int access,
    final String name,
    final String desc,
    final String signature,
    final String[] exceptions)
  {
    this.access = access;
    this.name = name;
    this.desc = desc;
    this.signature = signature;
    this.exceptions = new ArrayList();
    int params = Type.getArgumentTypes(desc).length;
    this.visibleParameterAnnotations = new List[params];
    this.invisibleParameterAnnotations = new List[params];
    for (int i = 0; i < params; ++i) {
      this.visibleParameterAnnotations[i] = new ArrayList();
      this.invisibleParameterAnnotations[i] = new ArrayList();
    }
    this.instructions = new ArrayList();
    this.tryCatchBlocks = new ArrayList();
    this.localVariables = new ArrayList();
    this.lineNumbers = new ArrayList();
    if (exceptions != null) {
      this.exceptions.addAll(Arrays.asList(exceptions));
    }    
  }
  
  // --------------------------------------------------------------------------
  // Implementation of the MethodVisitor interface
  // --------------------------------------------------------------------------

  public AnnotationVisitor visitAnnotationDefault () {
    return new AnnotationNode(new ArrayList() {
      public boolean add (Object o) {
        annotationDefault = o;
        return super.add(o);
      }
    });
  }

  public AnnotationVisitor visitParameterAnnotation (
    final int parameter,
    final String desc,
    final boolean visible) 
  {
    AnnotationNode an = new AnnotationNode(desc);
    if (visible) {
      visibleParameterAnnotations[parameter].add(an);
    } else {
      invisibleParameterAnnotations[parameter].add(an);
    }
    return an;
  }
  
  public void visitInsn (final int opcode) {
    AbstractInsnNode n = new InsnNode(opcode);
    instructions.add(n);
  }

  public void visitIntInsn (final int opcode, final int operand) {
    AbstractInsnNode n = new IntInsnNode(opcode, operand);
    instructions.add(n);
  }

  public void visitVarInsn (final int opcode, final int var) {
    AbstractInsnNode n = new VarInsnNode(opcode, var);
    instructions.add(n);
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    AbstractInsnNode n = new TypeInsnNode(opcode, desc);
    instructions.add(n);
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    AbstractInsnNode n = new FieldInsnNode(opcode, owner, name, desc);
    instructions.add(n);
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    AbstractInsnNode n = new MethodInsnNode(opcode, owner, name, desc);
    instructions.add(n);
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    AbstractInsnNode n = new JumpInsnNode(opcode, label);
    instructions.add(n);
  }

  public void visitLabel (final Label label) {
    instructions.add(new LabelNode(label));
  }

  public void visitLdcInsn (final Object cst) {
    AbstractInsnNode n = new LdcInsnNode(cst);
    instructions.add(n);
  }

  public void visitIincInsn (final int var, final int increment) {
    AbstractInsnNode n = new IincInsnNode(var, increment);
    instructions.add(n);
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    AbstractInsnNode n = new TableSwitchInsnNode(min, max, dflt, labels);
    instructions.add(n);
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    AbstractInsnNode n = new LookupSwitchInsnNode(dflt, keys, labels);
    instructions.add(n);
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    AbstractInsnNode n = new MultiANewArrayInsnNode(desc, dims);
    instructions.add(n);
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    TryCatchBlockNode n = new TryCatchBlockNode(start, end, handler, type);
    tryCatchBlocks.add(n);
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final String signature,
    final Label start,
    final Label end,
    final int index)
  {
    LocalVariableNode n;
    n = new LocalVariableNode(name, desc, signature, start, end, index);
    localVariables.add(n);
  }

  public void visitLineNumber (final int line, final Label start) {
    LineNumberNode n = new LineNumberNode(line, start);
    lineNumbers.add(n);
  }
  
  public void visitMaxs (final int maxStack, final int maxLocals) {
    this.maxStack = maxStack;
    this.maxLocals = maxLocals;
  }
  
  // --------------------------------------------------------------------------
  // Accept method
  // --------------------------------------------------------------------------

  /**
   * Makes the given class visitor visit this method.
   *
   * @param cv a class visitor.
   */

  public void accept (final ClassVisitor cv) {
    String[] exceptions = new String[this.exceptions.size()];
    this.exceptions.toArray(exceptions);
    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    // visits the method attributes
    int i, j;
    if (annotationDefault != null) {
      AnnotationVisitor av = mv.visitAnnotationDefault();
      AnnotationNode.accept(av, null, annotationDefault);
      av.visitEnd();
    }
    for (i = 0; i < visibleAnnotations.size(); ++i) {
      AnnotationNode an = (AnnotationNode)visibleAnnotations.get(i); 
      an.accept(mv.visitAnnotation(an.desc, true));
    }
    for (i = 0; i < invisibleAnnotations.size(); ++i) {
      AnnotationNode an = (AnnotationNode)invisibleAnnotations.get(i); 
      an.accept(mv.visitAnnotation(an.desc, false));
    }
    for (i = 0; i < visibleParameterAnnotations.length; ++i) {
      List l = visibleParameterAnnotations[i];
      for (j = 0; j < l.size(); ++j) {
        AnnotationNode an = (AnnotationNode)l.get(j);
        an.accept(mv.visitParameterAnnotation(i, an.desc, true));
      }
    }
    for (i = 0; i < invisibleParameterAnnotations.length; ++i) {
      List l = invisibleParameterAnnotations[i];
      for (j = 0; j < l.size(); ++j) {
        AnnotationNode an = (AnnotationNode)l.get(j);
        an.accept(mv.visitParameterAnnotation(i, an.desc, false));
      }
    }
    for (i = 0; i < attrs.size(); ++i) {
      mv.visitAttribute((Attribute)attrs.get(i));
    }
    // visits the method's code
    if (mv != null && instructions.size() > 0) {
      // visits instructions
      for (i = 0; i < instructions.size(); ++i) {
        ((AbstractInsnNode)instructions.get(i)).accept(mv);
      }
      // visits try catch blocks
      for (i = 0; i < tryCatchBlocks.size(); ++i) {
        ((TryCatchBlockNode)tryCatchBlocks.get(i)).accept(mv);
      }
      // visits local variables
      for (i = 0; i < localVariables.size(); ++i) {
        ((LocalVariableNode)localVariables.get(i)).accept(mv);
      }
      // visits line numbers
      for (i = 0; i < lineNumbers.size(); ++i) {
        ((LineNumberNode)lineNumbers.get(i)).accept(mv);
      }
      // visits maxs
      mv.visitMaxs(maxStack, maxLocals);
    }
    mv.visitEnd();
  }
}
