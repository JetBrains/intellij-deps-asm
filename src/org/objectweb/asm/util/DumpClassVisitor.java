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

import org.objectweb.asm.Constants;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.CodeVisitor;

import java.io.PrintWriter;

/**
 * A {@link PrintClassVisitor PrintClassVisitor} that prints the ASM code that
 * generates the classes it visits. This class visitor can be used to quickly
 * write ASM code to generate some given bytecode:
 * <ul>
 * <li>write the Java source code equivalent to the bytecode you want to
 * generate;</li>
 * <li>compile it with <tt>javac</tt>;</li>
 * <li>make a {@link DumpClassVisitor DumpClassVisitor} visit this compiled
 * class (see the {@link #main main} method);</li>
 * <li>edit the generated source code, if necessary.</li>
 * </ul>
 * The source code printed when visiting the <tt>Hello</tt> class is the
 * following:
 * <p>
 * <blockquote>
 * <pre>
 * import org.objectweb.asm.*;
 * import java.io.FileOutputStream;
 *
 * public class Dump implements Constants {
 *
 * public static void main (String[] args) throws Exception {
 *
 * ClassWriter cw = new ClassWriter(false);
 * CodeVisitor cv;
 *
 * cw.visit(ACC_PUBLIC + ACC_SUPER, "Hello", "java/lang/Object", null, "Hello.java");
 *
 * {
 * cv = cw.visitMethod(ACC_PUBLIC + ACC_STATIC, "main", "([Ljava/lang/String;)V", null);
 * cv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
 * cv.visitLdcInsn("hello");
 * cv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
 * cv.visitInsn(RETURN);
 * cv.visitMaxs(2, 1);
 * }
 * {
 * cv = cw.visitMethod(ACC_PUBLIC, "&lt;init&gt;", "()V", null);
 * cv.visitVarInsn(ALOAD, 0);
 * cv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "&lt;init&gt;", "()V");
 * cv.visitInsn(RETURN);
 * cv.visitMaxs(1, 1);
 * }
 * cw.visitEnd();
 *
 * FileOutputStream os = new FileOutputStream("Dumped.class");
 * os.write(cw.toByteArray());
 * os.close();
 * }
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

public class DumpClassVisitor extends PrintClassVisitor {

  /**
   * Prints the ASM source code to generate the given class to the standard
   * output.
   * <p>
   * Usage: DumpClassVisitor &lt;fully qualified class name&gt;
   *
   * @param args the command line arguments.
   *
   * @throws Exception if the class cannot be found, or if an IO exception
   *      occurs.
   */

  public static void main (final String[] args) throws Exception {
    if (args.length == 0) {
      System.err.println("Prints the ASM code to generate the given class.");
      System.err.println("Usage: DumpClassVisitor <fully qualified class name>");
      System.exit(-1);
    }
    ClassReader cr = new ClassReader(args[0]);
    cr.accept(new DumpClassVisitor(new PrintWriter(System.out)), true);
  }

  /**
   * Constructs a new {@link DumpClassVisitor DumpClassVisitor} object.
   *
   * @param pw the print writer to be used to print the class.
   */

  public DumpClassVisitor (final PrintWriter pw) {
    super(pw);
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    text.add("import org.objectweb.asm.*;\n");
    text.add("import java.io.FileOutputStream;\n\n");
    text.add("public class Dump implements Constants {\n\n");
    text.add("public static void main (String[] args) throws Exception {\n\n");
    text.add("ClassWriter cw = new ClassWriter(false);\n");
    text.add("CodeVisitor cv;\n\n");

    buf.setLength(0);
    buf.append("cw.visit(");
    appendAccess(access | 262144);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, superName);
    buf.append(", ");
    if (interfaces != null && interfaces.length > 0) {
      buf.append("new String[] {");
      for (int i = 0; i < interfaces.length; ++i) {
        buf.append(i == 0 ? " " : ", ");
        appendConstant(buf, interfaces[i]);
      }
      buf.append(" }");
    } else {
      buf.append("null");
    }
    buf.append(", ");
    appendConstant(buf, sourceFile);
    buf.append(");\n\n");
    text.add(buf.toString());
  }

  public void visitInnerClass (
    final String name,
    final String outerName,
    final String innerName,
    final int access)
  {
    buf.setLength(0);
    buf.append("cw.visitInnerClass(");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, outerName);
    buf.append(", ");
    appendConstant(buf, innerName);
    buf.append(", ");
    appendAccess(access);
    buf.append(");\n\n");
    text.add(buf.toString());
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value)
  {
    buf.setLength(0);
    buf.append("cw.visitField(");
    appendAccess(access);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, desc);
    buf.append(", ");
    appendConstant(buf, value);
    buf.append(");\n\n");
    text.add(buf.toString());
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    buf.setLength(0);
    buf.append("{\n").append("cv = cw.visitMethod(");
    appendAccess(access);
    buf.append(", ");
    appendConstant(buf, name);
    buf.append(", ");
    appendConstant(buf, desc);
    buf.append(", ");
    if (exceptions != null && exceptions.length > 0) {
      buf.append("new String[] {");
      for (int i = 0; i < exceptions.length; ++i) {
        buf.append(i == 0 ? " " : ", ");
        appendConstant(buf, exceptions[i]);
      }
      buf.append(" });");
    } else {
      buf.append("null);");
    }
    buf.append("\n");
    text.add(buf.toString());
    PrintCodeVisitor pcv = new DumpCodeVisitor();
    text.add(pcv.getText());
    text.add("}\n");
    return pcv;
  }

  public void visitEnd () {
    text.add("cw.visitEnd();\n\n");
    text.add("FileOutputStream os = new FileOutputStream(\"Dumped.class\");\n");
    text.add("os.write(cw.toByteArray());\n");
    text.add("os.close();\n");
    text.add("}\n");
    text.add("}\n");
    super.visitEnd();
  }

  /**
   * Appends a string representation of the given access modifiers to {@link
   * #buf buf}.
   *
   * @param access some access modifiers.
   */

  void appendAccess (final int access) {
    boolean first = true;
    if ((access & Constants.ACC_PUBLIC) != 0) {
      buf.append("ACC_PUBLIC");
      first = false;
    }
    if ((access & Constants.ACC_PRIVATE) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_PRIVATE");
      first = false;
    }
    if ((access & Constants.ACC_PROTECTED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_PROTECTED");
      first = false;
    }
    if ((access & Constants.ACC_FINAL) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_FINAL");
      first = false;
    }
    if ((access & Constants.ACC_STATIC) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_STATIC");
      first = false;
    }
    if ((access & Constants.ACC_SYNCHRONIZED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      if ((access & 262144) != 0) {
        buf.append("ACC_SUPER");
      } else {
        buf.append("ACC_SYNCHRONIZED");
      }
      first = false;
    }
    if ((access & Constants.ACC_VOLATILE) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_VOLATILE");
      first = false;
    }
    if ((access & Constants.ACC_TRANSIENT) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_TRANSIENT");
      first = false;
    }
    if ((access & Constants.ACC_NATIVE) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_NATIVE");
      first = false;
    }
    if ((access & Constants.ACC_ABSTRACT) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_ABSTRACT");
      first = false;
    }
    if ((access & Constants.ACC_STRICT) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_STRICT");
      first = false;
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_SYNTHETIC");
      first = false;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      if (!first) {
        buf.append(" + ");
      }
      buf.append("ACC_DEPRECATED");
    }
    if (first) {
      buf.append("0");
    }
  }

  /**
   * Appends a string representation of the given constant to the given buffer.
   *
   * @param buf a string buffer.
   * @param cst an {@link java.lang.Integer Integer}, {@link java.lang.Float
   *      Float}, {@link java.lang.Long Long}, {@link java.lang.Double Double}
   *      or {@link String String} object. May be <tt>null</tt>.
   */

  static void appendConstant (final StringBuffer buf, final Object cst) {
    if (cst == null) {
      buf.append("null");
    } else if (cst instanceof String) {
      String s = (String)cst;
      buf.append("\"");
      for (int i = 0; i < s.length(); ++i) {
        char c = s.charAt(i);
        if (c == '\n') {
          buf.append("\\n");
        } else if (c == '\\') {
          buf.append("\\\\");
        } else if (c == '"') {
          buf.append("\\\"");
        } else {
          buf.append(c);
        }
      }
      buf.append("\"");
    } else if (cst instanceof Integer) {
      buf.append("new Integer(")
        .append(cst)
        .append(")");
    } else if (cst instanceof Float) {
      buf.append("new Float(")
        .append(cst)
        .append("F)");
    } else if (cst instanceof Long) {
      buf.append("new Long(")
        .append(cst)
        .append("L)");
    } else if (cst instanceof Double) {
      buf.append("new Double(")
        .append(cst)
        .append(")");
    }
  }
}
