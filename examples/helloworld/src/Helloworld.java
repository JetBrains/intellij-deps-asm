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

public class Helloworld extends ClassLoader implements Constants {

  public static void main (final String args[]) throws Exception {

    /*
     * Generates the bytecode corresponding to the following Java class:
     *
     * public class Example {
     *   public static void main (String[] args) {
     *     System.out.println("Hello world!");
     *   }
     * }
     *
     */

    // creates a ClassWriter for the Example public class,
    // which inherits from Object
    ClassWriter cw = new ClassWriter(false);
    cw.visit(ACC_PUBLIC, "Example", "java/lang/Object", null, null);

    // creates a MethodWriter for the (implicit) constructor
    CodeVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null);
    // pushes the 'this' variable
    mw.visitVarInsn(ALOAD, 0);
    // invokes the super class constructor
    mw.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    mw.visitInsn(RETURN);
    // this code uses a maximum of one stack element and one local variable
    mw.visitMaxs(1, 1);

    // creates a MethodWriter for the 'main' method
    mw = cw.visitMethod(
      ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null);
    // pushes the 'out' field (of type PrintStream) of the System class
    mw.visitFieldInsn(
      GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    // pushes the "Hello World!" String constant
    mw.visitLdcInsn("Hello world!");
    // invokes the 'println' method (defined in the PrintStream class)
    mw.visitMethodInsn(
      INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    mw.visitInsn(RETURN);
    // this code uses a maximum of two stack elements and two local variables
    mw.visitMaxs(2, 2);

    // gets the bytecode of the Example class, and loads it dynamically
    byte[] code = cw.toByteArray();

    FileOutputStream fos = new FileOutputStream("Example.class");
    fos.write(code);
    fos.close();

    Helloworld loader = new Helloworld();
    Class exampleClass = loader.defineClass("Example", code, 0, code.length);

    // uses the dynamically generated class to print 'Helloworld'
    Method main = exampleClass.getMethods()[0];
    main.invoke(null, new Object[] {null});
  }
}
