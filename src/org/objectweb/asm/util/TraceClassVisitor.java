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

package org.objectweb.asm.util;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.ClassReader;

import java.io.PrintWriter;

/**
 * A {@link PrintClassVisitor PrintClassVisitor} that prints a disassembled
 * view of the classes it visits. This class visitor can be used alone (see the
 * {@link #main main} method) to disassemble a class. It can also be used in
 * the middle of class visitor chain to trace the class that is visited at a
 * given point in this chain. This may be uselful for debugging purposes.
 * <p>
 * The trace printed when visiting the <tt>Hello</tt> class is the following:
 * <p>
 * <blockquote>
 * <pre>
 * // compiled from Hello.java
 * public class Hello {
 *
 *   public static main ([Ljava/lang/String;)V
 *     GETSTATIC java/lang/System out Ljava/io/PrintStream;
 *     LDC "hello"
 *     INVOKEVIRTUAL java/io/PrintStream println (Ljava/lang/String;)V
 *     RETURN
 *     MAXSTACK = 2
 *     MAXLOCALS = 1
 *
 *   public &lt;init&gt; ()V
 *     ALOAD 0
 *     INVOKESPECIAL java/lang/Object &lt;init&gt; ()V
 *     RETURN
 *     MAXSTACK = 1
 *     MAXLOCALS = 1
 *
 * }
 * </pre>
 * </blockquote>
 * where <tt>Hello</tt> is defined by:
 * <p>
 * <blockquote>
 * <pre>
 * public class Hello {
 *
 *   public static void main (String[] args) {
 *     System.out.println("hello");
 *   }
 * }
 * </pre>
 * </blockquote>
 */

public class TraceClassVisitor extends PrintClassVisitor {

  /**
   * The {@link ClassVisitor ClassVisitor} to which this visitor delegates
   * calls. May be <tt>null</tt>.
   */

  protected final ClassVisitor cv;

  /**
   * Prints a disassembled view of the given class to the standard output.
   * <p>
   * Usage: TraceClassVisitor &lt;fully qualified class name&gt;
   *
   * @param args the command line arguments.
   *
   * @throws Exception if the class cannot be found, or if an IO exception
   *      occurs.
   */

  public static void main (final String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Prints a disassembled view of the given class.");
      System.err.println("Usage: TraceClassVisitor <fully qualified class name>");
    }
    ClassReader cr = new ClassReader(args[0]);
    cr.accept(new TraceClassVisitor(null, new PrintWriter(System.out)), true);
  }

  /**
   * Constructs a new {@link TraceClassVisitor TraceClassVisitor} object.
   *
   * @param cv the class visitor to which this adapter must delegate calls. May
   *      be <tt>null</tt>.
   * @param pw the print writer to be used to print the class.
   */

  public TraceClassVisitor (final ClassVisitor cv, final PrintWriter pw) {
    super(pw);
    this.cv = cv;
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    buf.setLength(0);
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      buf.append("// DEPRECATED\n");
    }
    if (sourceFile != null) {
      buf.append("// compiled from ").append(sourceFile).append("\n");
    }
    appendAccess(access & ~Constants.ACC_SUPER);
    if ((access & Constants.ACC_INTERFACE) != 0) {
      buf.append("interface ");
    } else {
      buf.append("class ");
    }
    buf.append(name).append(" ");
    if (!superName.equals("java/lang/Object")) {
      buf.append("extends ").append(superName).append(" ");
    }
    if (interfaces != null && interfaces.length > 0) {
      buf.append("implements ");
      for (int i = 0; i < interfaces.length; ++i) {
        buf.append(interfaces[i]).append(" ");
      }
    }
    buf.append("{\n\n");
    text.add(buf.toString());

    if (cv != null) {
      cv.visit(access, name, superName, interfaces, sourceFile);
    }
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    buf.setLength(0);
    buf.append("  INNERCLASS ")
      .append(name)
      .append(" ")
      .append(outerName)
      .append(" ")
      .append(innerName)
      .append(" ")
      .append(access)
      .append("\n");
    text.add(buf.toString());

    if (cv != null) {
      cv.visitInnerClass(name, outerName, innerName, access);
    }
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    buf.setLength(0);
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      buf.append("  // DEPRECATED\n");
    }
    buf.append("  ");
    appendAccess(access);
    buf.append(desc)
      .append(" ")
      .append(name);
    if (value != null) {
      buf.append(" = ");
      if (value instanceof String) {
        buf.append("\"").append(value).append("\"");
      } else {
        buf.append(value);
      }
    }
    buf.append("\n\n");
    text.add(buf.toString());

    if (cv != null) {
      cv.visitField(access, name, desc, value);
    }
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    buf.setLength(0);
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      buf.append("  // DEPRECATED\n");
    }
    buf.append("  ");
    appendAccess(access);
    buf.append(name).
      append(" ").
      append(desc);
    if (exceptions != null && exceptions.length > 0) {
      buf.append(" throws ");
      for (int i = 0; i < exceptions.length; ++i) {
        buf.append(exceptions[i]).append(" ");
      }
    }
    buf.append("\n");
    text.add(buf.toString());

    CodeVisitor cv;
    if (this.cv != null) {
      cv = this.cv.visitMethod(access, name, desc, exceptions);
    } else {
      cv = null;
    }
    PrintCodeVisitor pcv = new TraceCodeVisitor(cv);
    text.add(pcv.getText());
    return pcv;
  }

  public void visitEnd () {
    text.add("}\n");

    if (cv != null) {
      cv.visitEnd();
    }

    super.visitEnd();
  }

  /**
   * Appends a string representation of the given access modifiers to {@link
   * #buf buf}.
   *
   * @param access some access modifiers.
   */

  private void appendAccess (final int access) {
    if ((access & Constants.ACC_PUBLIC) != 0) {
      buf.append("public ");
    }
    if ((access & Constants.ACC_PRIVATE) != 0) {
      buf.append("private ");
    }
    if ((access & Constants.ACC_PROTECTED) != 0) {
      buf.append("protected ");
    }
    if ((access & Constants.ACC_FINAL) != 0) {
      buf.append("final ");
    }
    if ((access & Constants.ACC_STATIC) != 0) {
      buf.append("static ");
    }
    if ((access & Constants.ACC_SYNCHRONIZED) != 0) {
      buf.append("synchronized ");
    }
    if ((access & Constants.ACC_VOLATILE) != 0) {
      buf.append("volatile ");
    }
    if ((access & Constants.ACC_TRANSIENT) != 0) {
      buf.append("transient ");
    }
    if ((access & Constants.ACC_NATIVE) != 0) {
      buf.append("native ");
    }
    if ((access & Constants.ACC_ABSTRACT) != 0) {
      buf.append("abstract ");
    }
    if ((access & Constants.ACC_STRICT) != 0) {
      buf.append("strictfp ");
    }
  }
}
