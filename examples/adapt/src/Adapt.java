/***
 * ASM examples: examples showing how asm can be used
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

import org.objectweb.asm.*;
import java.lang.reflect.*;
import java.io.FileOutputStream;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

public class Adapt extends ClassLoader implements Constants {

  public static void main (final String args[]) throws Exception {
    // loads the orginal class and adapts it
    ClassReader cr = new ClassReader("ArraySet");
    ClassWriter cw = new ClassWriter(false);
    ClassVisitor cv = new TraceFieldClassAdapter(cw);
    cr.accept(cv, false);
    byte[] b = cw.toByteArray();

    // stores the adapted class on disk
    FileOutputStream fos = new FileOutputStream("ArraySet.class.adapted");
    fos.write(b);
    fos.close();

    // dynamically loads the adapted class
    Adapt loader = new Adapt();
    Class arraySetClass = loader.defineClass("ArraySet", b, 0, b.length);

    // uses the adapted class
    Set s = (Set)arraySetClass.newInstance();
    System.err.println("add 1");
    s.add(1);
    System.err.println("add 1");
    s.add(1);
    System.err.println("add 2");
    s.add(2);
    System.err.println("add 4");
    s.add(4);
    System.err.println("add 8");
    s.add(8);
    System.err.println("contains 3 = " + s.contains(3));
    System.err.println("contains 1 = " + s.contains(1));
    System.err.println("remove 1");
    s.remove(1);
    System.err.println("contains 1 = " + s.contains(1));
  }
}

class TraceFieldClassAdapter extends ClassAdapter implements Constants {

  private String owner;

  public TraceFieldClassAdapter (final ClassVisitor cv) {
    super(cv);
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    owner = name;
    super.visit(access, name, superName, interfaces, sourceFile);
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    super.visitField(access, name, desc, value);
    if ((access & ACC_STATIC) == 0) {
      Type t = Type.getType(desc);
      int size = t.getSize();

      // generates getter method
      String gDesc = "()" + desc;
      CodeVisitor gv = cv.visitMethod(ACC_PRIVATE, "_get" + name, gDesc, null);
      gv.visitFieldInsn(GETSTATIC,
        "java/lang/System", "err", "Ljava/io/PrintStream;");
      gv.visitLdcInsn("_get" + name + " called");
      gv.visitMethodInsn(INVOKEVIRTUAL,
        "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      gv.visitVarInsn(ALOAD, 0);
      gv.visitFieldInsn(GETFIELD, owner, name, desc);
      gv.visitInsn(t.getOpcode(IRETURN));
      gv.visitMaxs(1 + size, 1);

      // generates setter method
      String sDesc = "(" + desc + ")V";
      CodeVisitor sv = cv.visitMethod(ACC_PRIVATE, "_set" + name, sDesc, null);
      sv.visitFieldInsn(GETSTATIC,
        "java/lang/System", "err", "Ljava/io/PrintStream;");
      sv.visitLdcInsn("_set" + name + " called");
      sv.visitMethodInsn(INVOKEVIRTUAL,
        "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
      sv.visitVarInsn(ALOAD, 0);
      sv.visitIntInsn(t.getOpcode(ILOAD), 1);
      sv.visitFieldInsn(PUTFIELD, owner, name, desc);
      sv.visitInsn(RETURN);
      sv.visitMaxs(1 + size, 1 + size);
    }
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    CodeVisitor mv = cv.visitMethod(access, name, desc, exceptions);
    return mv == null ? null : new TraceFieldCodeAdapter(mv, owner);
  }
}

class TraceFieldCodeAdapter extends CodeAdapter implements Constants {

  private String owner;

  public TraceFieldCodeAdapter (final CodeVisitor cv, final String owner) {
    super(cv);
    this.owner = owner;
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    if (owner.equals(this.owner)) {
      if (opcode == GETFIELD) {
        // replaces GETFIELD f by INVOKESPECIAL _getf
        String gDesc = "()" + desc;
        visitMethodInsn(INVOKESPECIAL, owner, "_get" + name, gDesc);
        return;
      } else if (opcode == PUTFIELD) {
        // replaces PUTFIELD f by INVOKESPECIAL _setf
        String sDesc = "(" + desc + ")V";
        visitMethodInsn(INVOKESPECIAL, owner, "_set" + name, sDesc);
        return;
      }
    }
    super.visitFieldInsn(opcode, owner, name, desc);
  }
}
