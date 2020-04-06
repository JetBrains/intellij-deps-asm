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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.nio.file.Files;
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
 * A tool to transform classes in order to make them compatible with Java 1.5, and to check that
 * they use only the JDK 1.5 API and JDK 1.5 class file features. The original classes can either be
 * transformed "in place", or be copied first to destination directory and transformed here (leaving
 * the original classes unchanged).
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class Retrofitter {

  /**
   * The fields and methods of the JDK 1.5 API. Each string has the form
   * "&lt;owner&gt;&lt;name&gt;&lt;descriptor&gt;".
   */
  private final HashSet<String> jdkApi = new HashSet<>();

  /**
   * The class hierarchy of the JDK 1.5 API. Maps each class name to the name of its super class.
   */
  private final HashMap<String, String> jdkHierarchy = new HashMap<>();

  /**
   * Constructs a new {@link Retrofitter}.
   *
   * @throws IOException if the JDK API description file can't be read.
   */
  public Retrofitter() throws IOException {
    try (InputStream inputStream =
            new GZIPInputStream(
                Retrofitter.class.getClassLoader().getResourceAsStream("jdk1.5.0.12.txt.gz"));
        BufferedReader reader = new LineNumberReader(new InputStreamReader(inputStream))) {
      while (true) {
        String line = reader.readLine();
        if (line != null) {
          if (line.startsWith("class")) {
            String className = line.substring(6, line.lastIndexOf(' '));
            String superClassName = line.substring(line.lastIndexOf(' ') + 1);
            jdkHierarchy.put(className, superClassName);
          } else {
            jdkApi.add(line);
          }
        } else {
          break;
        }
      }
    } catch (IOException ioe) {
      throw ioe;
    }
  }

  /**
   * Transforms the source class file, or if it is a directory, its files (recursively), in place,
   * in order to make them compatible with the JDK 1.5.
   *
   * @param src source file or directory.
   * @throws IOException if the source files can't be read or written.
   */
  public void retrofit(final File src) throws IOException {
    retrofit(src, null);
  }

  /**
   * Transforms the source class file, or if it is a directory, its files (recursively), either in
   * place or into the destination file or directory, in order to make them compatible with the JDK
   * 1.5.
   *
   * @param src source file or directory.
   * @param dst optional destination file or directory.
   * @throws IOException if the source or destination file can't be read or written.
   */
  public void retrofit(final File src, final File dst) throws IOException {
    if (src.isDirectory()) {
      File[] files = src.listFiles();
      if (files == null) {
        throw new IOException("Unable to read files of " + src);
      }
      for (File file : files) {
        retrofit(file, dst == null ? null : new File(dst, file.getName()));
      }
    } else if (src.getName().endsWith(".class")) {
      if (dst == null || !dst.exists() || dst.lastModified() < src.lastModified()) {
        ClassReader classReader = new ClassReader(Files.newInputStream(src.toPath()));
        ClassWriter classWriter = new ClassWriter(0);
        ClassVerifier classVerifier = new ClassVerifier(classWriter);
        ClassRetrofitter classRetrofitter = new ClassRetrofitter(classVerifier);
        classReader.accept(classRetrofitter, ClassReader.SKIP_FRAMES);

        if (dst != null && !dst.getParentFile().exists() && !dst.getParentFile().mkdirs()) {
          throw new IOException("Cannot create directory " + dst.getParentFile());
        }
        try (OutputStream outputStream =
            Files.newOutputStream((dst == null ? src : dst).toPath())) {
          outputStream.write(classWriter.toByteArray());
        }
      }
    }
  }

  /** A ClassVisitor that retrofits classes to 1.5 version. */
  static class ClassRetrofitter extends ClassVisitor {

    public ClassRetrofitter(final ClassVisitor classVisitor) {
      super(/* latest api =*/ Opcodes.ASM8, classVisitor);
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

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(
          api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
          // Remove the addSuppressed() method calls generated for try-with-resources statements.
          // This method is not defined in JDK1.5.
          if (owner.equals("java/lang/Throwable")
              && name.equals("addSuppressed")
              && descriptor.equals("(Ljava/lang/Throwable;)V")) {
            visitInsn(Opcodes.POP2);
          } else {
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
          }
        }
      };
    }
  }

  /**
   * A ClassVisitor checking that a class uses only JDK 1.5 class file features and the JDK 1.5 API.
   */
  class ClassVerifier extends ClassVisitor {

    /** The name of the visited class. */
    String className;

    /** The name of the currently visited method. */
    String currentMethodName;

    public ClassVerifier(final ClassVisitor classVisitor) {
      // Make sure use we don't use Java 9 or higher classfile features.
      // We also want to make sure we don't use Java 6, 7 or 8 classfile
      // features (invokedynamic), but this can't be done in the same way.
      // Instead, we use manual checks below.
      super(Opcodes.ASM4, classVisitor);
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
        throw new IllegalArgumentException("ERROR: " + name + " version is newer than 1.5");
      }
      className = name;
      super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      currentMethodName = name + descriptor;
      MethodVisitor methodVisitor =
          super.visitMethod(access, name, descriptor, signature, exceptions);
      return new MethodVisitor(Opcodes.ASM4, methodVisitor) {
        @Override
        public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
          check(owner, name);
          super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
          check(owner, name + descriptor);
          super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        }

        @Override
        public void visitLdcInsn(final Object value) {
          if (value instanceof Type) {
            int sort = ((Type) value).getSort();
            if (sort == Type.METHOD) {
              throw new IllegalArgumentException(
                  "ERROR: ldc with a MethodType called in "
                      + className
                      + ' '
                      + currentMethodName
                      + " is not available in JDK 1.5");
            }
          } else if (value instanceof Handle) {
            throw new IllegalArgumentException(
                "ERROR: ldc with a MethodHandle called in "
                    + className
                    + ' '
                    + currentMethodName
                    + " is not available in JDK 1.5");
          }
          super.visitLdcInsn(value);
        }

        @Override
        public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {
          throw new IllegalArgumentException(
              "ERROR: invokedynamic called in "
                  + className
                  + ' '
                  + currentMethodName
                  + " is not available in JDK 1.5");
        }
      };
    }

    /**
     * Checks whether or not a field or method is defined in the JDK 1.5 API.
     *
     * @param owner A class name.
     * @param member A field name or a method name and descriptor.
     */
    void check(final String owner, final String member) {
      if (owner.startsWith("java/")) {
        String currentOwner = owner;
        while (currentOwner != null) {
          if (jdkApi.contains(currentOwner + ' ' + member)) {
            return;
          }
          currentOwner = jdkHierarchy.get(currentOwner);
        }
        throw new IllegalArgumentException(
            "ERROR: "
                + owner
                + ' '
                + member
                + " called in "
                + className
                + ' '
                + currentMethodName
                + " is not defined in the JDK 1.5 API");
      }
    }
  }
}
