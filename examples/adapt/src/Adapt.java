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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeAdapter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Type;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

public class Adapt extends ClassLoader implements Constants {

  protected synchronized Class loadClass (
    final String name,
    final boolean resolve) throws ClassNotFoundException
  {
    if (name.startsWith("java.")) {
      System.err.println(
        "Adapt: loading class '" + name + "' without on the fly adaptation");
      return super.loadClass(name, resolve);
    } else {
      System.err.println(
        "Adapt: loading class '" + name + "' with on the fly adaptation");
    }

    // gets an input stream to read the bytecode of the class
    String resource = name.replace('.','/') + ".class";
    InputStream is = getResourceAsStream(resource);
    byte[] b;

    // adapts the class on the fly
    try {
      ClassReader cr = new ClassReader(is);
      ClassWriter cw = new ClassWriter(false);
      ClassVisitor cv = new TraceFieldClassAdapter(cw);
      cr.accept(cv, false);
      b = cw.toByteArray();
    } catch (Exception e) {
      throw new ClassNotFoundException(name, e);
    }

    // optional: stores the adapted class on disk
    try {
      FileOutputStream fos = new FileOutputStream(resource + ".adapted");
      fos.write(b);
      fos.close();
    } catch (Exception e) {
    }

    // returns the adapted class
    return defineClass(name, b, 0, b.length);
  }

  public static void main (final String args[]) throws Exception {
    // loads the application class (in args[0]) with an Adapt class loader
    ClassLoader loader = new Adapt();
    Class c = loader.loadClass(args[0]);
    // calls the 'main' static method of this class with the
    // application arguments (in args[1] ... args[n]) as parameter
    Method m = c.getMethod("main", new Class[] {String[].class});
    String[] applicationArgs = new String[args.length - 1];
    System.arraycopy(args, 1, applicationArgs, 0, applicationArgs.length);
    m.invoke(null, new Object[] {applicationArgs});
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
