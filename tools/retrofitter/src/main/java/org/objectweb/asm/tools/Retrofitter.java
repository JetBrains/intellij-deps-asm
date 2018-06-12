// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A command line tool to transform classes in order to make them compatible with Java 1.5, and to
 * check that they use only the JDK 1.5 API and JDK 1.5 class file features. The original classes
 * can either be transformed "in place", or be copied first to destination directory and transformed
 * here (leaving the original classes unchanged).
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class Retrofitter {

  /**
   * The fields and methods of the JDK 1.5 API. Each string has the form "<owner>
   * <name><descriptor>".
   */
  static final HashSet<String> API = new HashSet<String>();

  /**
   * The class hierarchy of the JDK 1.5 API. Maps each class name to the name of its super class.
   */
  static final HashMap<String, String> HIERARCHY = new HashMap<String, String>();

  /**
   * Transforms the classes from a source directory into a destination directory to make them
   * compatible with the JDK 1.5, and checks that they only use the JDK 1.5 API.
   *
   * @param args First argument: source directory. Optional second argument: destination directory.
   *     If the second argument is provided classes are copied to this destination directory and
   *     transformed there. Otherwise they are transformed "in place" in the source directory.
   * @throws IOException if a file can't be read or written.
   */
  public static void main(final String[] args) throws IOException {
    InputStream inputStream =
        new GZIPInputStream(ClassLoader.getSystemResourceAsStream("jdk1.5.0.12.txt.gz"));
    BufferedReader reader = new LineNumberReader(new InputStreamReader(inputStream));
    while (true) {
      String line = reader.readLine();
      if (line != null) {
        if (line.startsWith("class")) {
          String className = line.substring(6, line.lastIndexOf(' '));
          String superClassName = line.substring(line.lastIndexOf(' ') + 1);
          HIERARCHY.put(className, superClassName);
        } else {
          API.add(line);
        }
      } else {
        break;
      }
    }

    File src = new File(args[0]);
    File dst = args.length > 1 ? new File(args[1]) : null;
    if (!retrofit(src, dst)) {
      System.exit(1);
    }
  }

  /**
   * Transforms the source class file, or if it is a directory, its files (recursively), either in
   * place or into the destination file or directory, in order to make them compatible with the JDK
   * 1.5.
   *
   * @param src source file or directory
   * @param dst optional destination file or directory
   * @return true if all the source classes use only the JDK 1.5 API.
   * @throws IOException
   */
  static boolean retrofit(final File src, final File dst) throws IOException {
    if (src.isDirectory()) {
      boolean result = true;
      File[] files = src.listFiles();
      if (files == null) {
        throw new IOException("Unable to read files of " + src);
      }
      for (int i = 0; i < files.length; ++i) {
        result &= retrofit(files[i], dst == null ? null : new File(dst, files[i].getName()));
      }
      return result;
    } else if (src.getName().endsWith(".class")) {
      if (dst == null || !dst.exists() || dst.lastModified() < src.lastModified()) {
        ClassReader classReader = new ClassReader(new FileInputStream(src));
        ClassWriter classWriter = new ClassWriter(0);
        ClassVerifier classVerifier = new ClassVerifier(classWriter);
        ClassRetrofitter classRetrofitter = new ClassRetrofitter(classVerifier);
        classReader.accept(classRetrofitter, ClassReader.SKIP_FRAMES);

        if (dst != null && !dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
          throw new IOException("Cannot create directory " + dst.getParentFile());
        }
        OutputStream os = new FileOutputStream(dst == null ? src : dst);
        try {
          os.write(classWriter.toByteArray());
        } finally {
          os.close();
        }
        return classVerifier.ok;
      }
      return true;
    } else {
      return true;
    }
  }

  /** A ClassVisitor that retrofits classes from 1.6 to 1.5 version. */
  static class ClassRetrofitter extends ClassVisitor {

    public ClassRetrofitter(ClassVisitor classVisitor) {
      super(Opcodes.ASM7_EXPERIMENTAL, classVisitor);
    }

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {
      super.visit(Opcodes.V1_5, access, name, signature, superName, interfaces);
    }
  }

  /**
   * A ClassVisitor checking that a class uses only JDK 1.5 class file features and the JDK 1.5 API.
   */
  static class ClassVerifier extends ClassVisitor {

    /** The name of the visited class/ */
    String className;

    /** The name of the currently visited method. */
    String currentMethodName;

    /** Whether the class uses only JDK 1.5 class file features and APIs. */
    boolean ok;

    public ClassVerifier(final ClassVisitor classVisitor) {
      // Make sure use we don't use Java 9 or higher classfile features.
      // We also want to make sure we don't use Java 6, 7 or 8 classfile
      // features (invokedynamic), but this can't be done in the same way.
      // Instead, we use manual checks below.
      super(Opcodes.ASM4, classVisitor);
      ok = true;
    }

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {
      if ((version & 0xFFFF) > Opcodes.V1_5) {
        System.err.println("ERROR: " + name + " version is newer than 1.5");
        ok = false;
      }
      className = name;
      super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      currentMethodName = name + desc;
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      return new MethodVisitor(Opcodes.ASM4, mv) {
        @Override
        public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String desc) {
          check(owner, name);
          super.visitFieldInsn(opcode, owner, name, desc);
        }

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String desc,
            final boolean itf) {
          check(owner, name + desc);
          super.visitMethodInsn(opcode, owner, name, desc, itf);
        }

        @Override
        public void visitLdcInsn(Object cst) {
          if (cst instanceof Type) {
            int sort = ((Type) cst).getSort();
            if (sort == Type.METHOD) {
              System.err.println(
                  "ERROR: ldc with a MethodType called in "
                      + className
                      + ' '
                      + currentMethodName
                      + " is not available in JDK 1.5");
              ok = false;
            }
          } else if (cst instanceof Handle) {
            System.err.println(
                "ERROR: ldc with a MethodHandle called in "
                    + className
                    + ' '
                    + currentMethodName
                    + " is not available in JDK 1.5");
            ok = false;
          }
          super.visitLdcInsn(cst);
        }

        @Override
        public void visitInvokeDynamicInsn(
            String name, String desc, Handle bsm, Object... bsmArgs) {
          System.err.println(
              "ERROR: invokedynamic called in "
                  + className
                  + ' '
                  + currentMethodName
                  + " is not available in JDK 1.5");
          ok = false;
          super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
        }
      };
    }

    /**
     * Checks whether or not a field or method is defined in the JDK 1.5 API.
     *
     * @param owner A class name.
     * @param member A field name or a method name and descriptor.
     */
    void check(String owner, String member) {
      if (owner.startsWith("java/")) {
        String o = owner;
        while (o != null) {
          if (API.contains(o + ' ' + member)) {
            return;
          }
          o = HIERARCHY.get(o);
        }
        System.err.println(
            "ERROR: "
                + owner
                + ' '
                + member
                + " called in "
                + className
                + ' '
                + currentMethodName
                + " is not defined in the JDK 1.5 API");
        ok = false;
      }
    }
  }
}
