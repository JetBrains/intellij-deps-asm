/***
 * ASM performance test: measures the performances of asm package
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

package org.objectweb.asm.test.perf;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;

import java.io.InputStream;

public class ASM extends ALL {

  public static void main (final String args[]) throws Exception {
    System.out.println("ASM PERFORMANCES\n");
    new ASM().perfs(args);
  }

  ALL newInstance () {
    return new ASM();
  }

  byte[] nullAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    ClassReader cr = new ClassReader(is);
    ClassWriter cw = new ClassWriter(compute);
    ClassAdapter ca = new ClassAdapter(cw);
    cr.accept(ca, skipDebug);
    return cw.toByteArray();
  }

  byte[] counterAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    ClassReader cr = new ClassReader(is);
    ClassWriter cw = new ClassWriter(false);
    ClassAdapter ca = new CounterClassAdapter(cw);
    cr.accept(ca, false);
    return cw.toByteArray();
  }

  static class CounterClassAdapter extends ClassAdapter implements Constants {

    private String owner;

    CounterClassAdapter (ClassVisitor cv) {
      super(cv);
    }

    public void visit (
      int access,
      String name,
      String superName,
      String[] interfaces,
      String sourceFile)
    {
      super.visit(access, name, superName, interfaces, sourceFile);
      if ((access & ACC_INTERFACE) == 0) {
        cv.visitField(ACC_PUBLIC, "_counter", "I", null);
      }
      owner = name;
    }

    public CodeVisitor visitMethod (
      int access,
      String name,
      String desc,
      String[] exceptions)
    {
      CodeVisitor cv = super.visitMethod(access, name, desc, exceptions);
      if (!name.equals("<init>") &&
          (access & (ACC_STATIC + ACC_NATIVE + ACC_ABSTRACT)) == 0)
      {
        cv.visitVarInsn(ALOAD, 0);
        cv.visitVarInsn(ALOAD, 0);
        cv.visitFieldInsn(GETFIELD, owner, "_counter", "I");
        cv.visitInsn(ICONST_1);
        cv.visitInsn(IADD);
        cv.visitFieldInsn(PUTFIELD, owner, "_counter", "I");
        return new CounterCodeAdapter(cv);
      }
      return cv;
    }
  }

  static class CounterCodeAdapter extends CodeAdapter {

    CounterCodeAdapter (CodeVisitor cv) {
      super(cv);
    }

    public void visitMaxs (int maxStack, int maxLocals) {
      super.visitMaxs(Math.max(maxStack, 2), maxLocals);
    }
  }
}
