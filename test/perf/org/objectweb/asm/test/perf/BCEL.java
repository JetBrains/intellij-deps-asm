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

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;

import java.io.InputStream;

public class BCEL extends ALL implements Constants {

  public static void main (final String args[]) throws Exception {
    System.out.println("BCEL PERFORMANCES\n");
    new BCEL().perfs(args);
  }

  ALL newInstance () {
    return new BCEL();
  }

  byte[] nullAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    JavaClass jc = new ClassParser(is, name + ".class").parse();
    ClassGen cg = new ClassGen(jc);
    String cName = cg.getClassName();
    ConstantPoolGen cp = cg.getConstantPool();
    Method[] ms = cg.getMethods();
    for (int j = 0; j < ms.length; ++j) {
      MethodGen mg = new MethodGen(ms[j], cg.getClassName(), cp);
      boolean lv = ms[j].getLocalVariableTable() == null;
      boolean ln = ms[j].getLineNumberTable() == null;
      if (lv) {
        mg.removeLocalVariables();
      }
      if (ln) {
        mg.removeLineNumbers();
      }
      mg.stripAttributes(skipDebug);
      InstructionList il = mg.getInstructionList();
      if (il != null) {
        InstructionHandle ih = il.getStart();
        while (ih != null) {
          ih = ih.getNext();
        }
        if (compute) {
          mg.setMaxStack();
          mg.setMaxLocals();
        }
      }
      cg.replaceMethod(ms[j], mg.getMethod());
    }
    return cg.getJavaClass().getBytes();
  }

  byte[] counterAdaptClass (final InputStream is, final String name)
    throws Exception
  {
    JavaClass jc = new ClassParser(is, name + ".class").parse();
    ClassGen cg = new ClassGen(jc);
    String cName = cg.getClassName();
    ConstantPoolGen cp = cg.getConstantPool();
    if (!cg.isInterface()) {
      FieldGen fg = new FieldGen(ACC_PUBLIC, Type.getType("I"), "_counter", cp);
      cg.addField(fg.getField());
    }
    Method[] ms = cg.getMethods();
    for (int j = 0; j < ms.length; ++j) {
      MethodGen mg = new MethodGen(ms[j], cg.getClassName() ,cp);
      if (!mg.getName().equals("<init>") &&
          !mg.isStatic() && !mg.isAbstract() && !mg.isNative())
      {
        if (mg.getInstructionList() != null) {
          InstructionList il = new InstructionList();
          il.append(new ALOAD(0));
          il.append(new ALOAD(0));
          il.append(new GETFIELD(cp.addFieldref(name, "_counter", "I")));
          il.append(new ICONST(1));
          il.append(new IADD());
          il.append(new PUTFIELD(cp.addFieldref(name, "_counter", "I")));
          mg.getInstructionList().insert(il);
          mg.setMaxStack(Math.max(mg.getMaxStack(), 2));
          boolean lv = ms[j].getLocalVariableTable() == null;
          boolean ln = ms[j].getLineNumberTable() == null;
          if (lv) {
            mg.removeLocalVariables();
          }
          if (ln) {
            mg.removeLineNumbers();
          }
          cg.replaceMethod(ms[j], mg.getMethod());
        }
      }
    }
    return cg.getJavaClass().getBytes();
  }
}
