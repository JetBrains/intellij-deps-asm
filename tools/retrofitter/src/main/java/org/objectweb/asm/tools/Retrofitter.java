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

import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
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

  /** The name of the module-info file. */
  private static final String MODULE_INFO = "module-info.class";

  /** The name of the java.base module. */
  private static final String JAVA_BASE_MODULE = "java.base";

  /**
   * The fields and methods of the JDK 1.5 API. Each string has the form
   * "&lt;owner&gt;&lt;name&gt;&lt;descriptor&gt;".
   */
  private final HashSet<String> jdkApi = new HashSet<>();

  /**
   * The class hierarchy of the JDK 1.5 API. Maps each class name to the name of its super class.
   */
  private final HashMap<String, String> jdkHierarchy = new HashMap<>();

  /** The internal names of the packages exported by the retrofitted classes. */
  private final HashSet<String> exports = new HashSet<>();

  /** The internal names of the packages imported by the retrofitted classes. */
  private final HashSet<String> imports = new HashSet<>();

  /**
   * Transforms the class files in the given directory, in place, in order to make them compatible
   * with the JDK 1.5. Also generates a module-info class in this directory, with the given module
   * version.
   *
   * @param args a directory containing compiled classes and the ASM release version.
   * @throws IOException if a file can't be read or written.
   */
  public static void main(final String[] args) throws IOException {
    if (args.length == 2) {
      new Retrofitter().retrofit(new File(args[0]), args[1]);
    } else {
      System.err.println("Usage: Retrofitter <classes directory> <ASM release version>"); // NOPMD
    }
  }

  /**
   * Transforms the class files in the given directory, in place, in order to make them compatible
   * with the JDK 1.5. Also generates a module-info class in this directory, with the given module
   * version.
   *
   * @param classesDir a directory containing compiled classes.
   * @param version the module-info version.
   * @throws IOException if a file can't be read or written.
   */
  public void retrofit(final File classesDir, final String version) throws IOException {
    for (File classFile : getAllClasses(classesDir, new ArrayList<File>())) {
      ClassReader classReader = new ClassReader(Files.newInputStream(classFile.toPath()));
      ClassWriter classWriter = new ClassWriter(0);
      classReader.accept(new ClassRetrofitter(classWriter), ClassReader.SKIP_FRAMES);
      Files.write(classFile.toPath(), classWriter.toByteArray());
    }
    generateModuleInfoClass(classesDir, version);
  }

  /**
   * Verify that the class files in the given directory only use JDK 1.5 APIs, and that a
   * module-info class is present with the expected content.
   *
   * @param classesDir a directory containing compiled classes.
   * @param expectedVersion the expected module-info version.
   * @param expectedExports the expected module-info exported packages.
   * @param expectedRequires the expected module-info required modules.
   * @throws IOException if a file can't be read.
   * @throws IllegalArgumentException if the module-info class does not have the expected content.
   */
  public void verify(
      final File classesDir,
      final String expectedVersion,
      final List<String> expectedExports,
      final List<String> expectedRequires)
      throws IOException {
    if (jdkApi.isEmpty()) {
      readJdkApi();
    }
    for (File classFile : getAllClasses(classesDir, new ArrayList<File>())) {
      if (!classFile.getName().equals(MODULE_INFO)) {
        new ClassReader(Files.newInputStream(classFile.toPath())).accept(new ClassVerifier(), 0);
      }
    }
    verifyModuleInfoClass(
        classesDir,
        expectedVersion,
        new HashSet<String>(expectedExports),
        Stream.concat(expectedRequires.stream(), Stream.of(JAVA_BASE_MODULE)).collect(toSet()));
  }

  private List<File> getAllClasses(final File file, final List<File> allClasses)
      throws IOException {
    if (file.isDirectory()) {
      File[] children = file.listFiles();
      if (children == null) {
        throw new IOException("Unable to read files of " + file);
      }
      for (File child : children) {
        getAllClasses(child, allClasses);
      }
    } else if (file.getName().endsWith(".class")) {
      allClasses.add(file);
    }
    return allClasses;
  }

  private void generateModuleInfoClass(final File dstDir, final String version) throws IOException {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(Opcodes.V9, Opcodes.ACC_MODULE, "module-info", null, null, null);
    ArrayList<String> moduleNames = new ArrayList<>();
    for (String exportName : exports) {
      if (isAsmModule(exportName)) {
        moduleNames.add(exportName);
      }
    }
    if (moduleNames.size() != 1) {
      throw new IllegalArgumentException("Module name can't be infered from classes");
    }
    ModuleVisitor moduleVisitor =
        classWriter.visitModule(moduleNames.get(0).replace('/', '.'), Opcodes.ACC_OPEN, version);

    for (String importName : imports) {
      if (isAsmModule(importName) && !exports.contains(importName)) {
        moduleVisitor.visitRequire(importName.replace('/', '.'), Opcodes.ACC_TRANSITIVE, null);
      }
    }
    moduleVisitor.visitRequire(JAVA_BASE_MODULE, Opcodes.ACC_MANDATED, null);

    for (String exportName : exports) {
      moduleVisitor.visitExport(exportName, 0);
    }
    moduleVisitor.visitEnd();
    classWriter.visitEnd();
    Files.write(Path.of(dstDir.getAbsolutePath(), MODULE_INFO), classWriter.toByteArray());
  }

  private void verifyModuleInfoClass(
      final File dstDir,
      final String expectedVersion,
      final Set<String> expectedExports,
      final Set<String> expectedRequires)
      throws IOException {
    ModuleDescriptor module =
        ModuleDescriptor.read(Files.newInputStream(Path.of(dstDir.getAbsolutePath(), MODULE_INFO)));
    String version = module.version().map(ModuleDescriptor.Version::toString).orElse("");
    if (!version.equals(expectedVersion)) {
      throw new IllegalArgumentException(
          format("Wrong module-info version '%s' (expected '%s')", version, expectedVersion));
    }
    Set<String> exports =
        module.exports().stream().map(ModuleDescriptor.Exports::source).collect(toSet());
    if (!exports.equals(expectedExports)) {
      throw new IllegalArgumentException(
          format("Wrong module-info exports %s (expected %s)", exports, expectedExports));
    }
    Set<String> requires =
        module.requires().stream().map(ModuleDescriptor.Requires::name).collect(toSet());
    if (!requires.equals(expectedRequires)) {
      throw new IllegalArgumentException(
          format("Wrong module-info requires %s (expected %s)", requires, expectedRequires));
    }
  }

  private static boolean isAsmModule(final String packageName) {
    return packageName.startsWith("org/objectweb/asm")
        && !packageName.equals("org/objectweb/asm/signature");
  }

  private void readJdkApi() throws IOException {
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

  /** A ClassVisitor that retrofits classes to 1.5 version. */
  class ClassRetrofitter extends ClassVisitor {

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
      addPackageReferences(Type.getObjectType(name), /* export = */ true);
      super.visit(Opcodes.V1_5, access, name, signature, superName, interfaces);
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      addPackageReferences(Type.getType(descriptor), /* export = */ false);
      return super.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      addPackageReferences(Type.getType(descriptor), /* export = */ false);
      return new MethodVisitor(
          api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

        @Override
        public void visitFieldInsn(
            final int opcode, final String owner, final String name, final String descriptor) {
          addPackageReferences(Type.getType(descriptor), /* export = */ false);
          super.visitFieldInsn(opcode, owner, name, descriptor);
        }

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
          addPackageReferences(Type.getType(descriptor), /* export = */ false);
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

        @Override
        public void visitTypeInsn(final int opcode, final String type) {
          addPackageReferences(Type.getObjectType(type), /* export = */ false);
          super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
          addPackageReferences(Type.getType(descriptor), /* export = */ false);
          super.visitMultiANewArrayInsn(descriptor, numDimensions);
        }

        @Override
        public void visitTryCatchBlock(
            final Label start, final Label end, final Label handler, final String type) {
          if (type != null) {
            addPackageReferences(Type.getObjectType(type), /* export = */ false);
          }
          super.visitTryCatchBlock(start, end, handler, type);
        }
      };
    }

    private void addPackageReferences(final Type type, final boolean export) {
      switch (type.getSort()) {
        case Type.ARRAY:
          addPackageReferences(type.getElementType(), export);
          break;
        case Type.METHOD:
          for (Type argumentType : type.getArgumentTypes()) {
            addPackageReferences(argumentType, export);
          }
          addPackageReferences(type.getReturnType(), export);
          break;
        case Type.OBJECT:
          String internalName = type.getInternalName();
          int lastSlashIndex = internalName.lastIndexOf('/');
          if (lastSlashIndex != -1) {
            (export ? exports : imports).add(internalName.substring(0, lastSlashIndex));
          }
          break;
        default:
          break;
      }
    }
  }

  /**
   * A ClassVisitor checking that a class uses only JDK 1.5 class file features and the JDK 1.5 API.
   */
  class ClassVerifier extends ClassVisitor {

    /** The internal name of the visited class. */
    String className;

    /** The name of the currently visited method. */
    String currentMethodName;

    public ClassVerifier() {
      // Make sure use we don't use Java 9 or higher classfile features.
      // We also want to make sure we don't use Java 6, 7 or 8 classfile
      // features (invokedynamic), but this can't be done in the same way.
      // Instead, we use manual checks below.
      super(Opcodes.ASM4, null);
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
        throw new IllegalArgumentException(format("ERROR: %d version is newer than 1.5", version));
      }
      className = name;
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
        }

        @Override
        public void visitMethodInsn(
            final int opcode,
            final String owner,
            final String name,
            final String descriptor,
            final boolean isInterface) {
          check(owner, name + descriptor);
        }

        @Override
        public void visitLdcInsn(final Object value) {
          if (value instanceof Type) {
            int sort = ((Type) value).getSort();
            if (sort == Type.METHOD) {
              throw new IllegalArgumentException(
                  format(
                      "ERROR: ldc with a MethodType called in %s %s is not available in JDK 1.5",
                      className, currentMethodName));
            }
          } else if (value instanceof Handle) {
            throw new IllegalArgumentException(
                format(
                    "ERROR: ldc with a MethodHandle called in %s %s is not available in JDK 1.5",
                    className, currentMethodName));
          }
        }

        @Override
        public void visitInvokeDynamicInsn(
            final String name,
            final String descriptor,
            final Handle bootstrapMethodHandle,
            final Object... bootstrapMethodArguments) {
          throw new IllegalArgumentException(
              format(
                  "ERROR: invokedynamic called in %s %s is not available in JDK 1.5",
                  className, currentMethodName));
        }
      };
    }

    /**
     * Checks whether or not a field or method is defined in the JDK 1.5 API.
     *
     * @param owner A class name.
     * @param member A field name or a method name and descriptor.
     */
    private void check(final String owner, final String member) {
      if (owner.startsWith("java/")) {
        String currentOwner = owner;
        while (currentOwner != null) {
          if (jdkApi.contains(currentOwner + ' ' + member)) {
            return;
          }
          currentOwner = jdkHierarchy.get(currentOwner);
        }
        throw new IllegalArgumentException(
            format(
                "ERROR: %s %s called in %s %s is not defined in the JDK 1.5 API",
                owner, member, className, currentMethodName));
      }
    }
  }
}
