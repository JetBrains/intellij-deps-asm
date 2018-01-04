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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.util.HashSet;
import java.util.Random;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassWriter tests.
 *
 * @author Eric Bruneton
 */
public class ClassWriterTest extends AsmTest {

  @Test
  public void testNewConst() {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.newConst(new Byte((byte) 0));
    classWriter.newConst(new Character('0'));
    classWriter.newConst(new Short((short) 0));
    classWriter.newConst(Boolean.FALSE);
    classWriter.newUTF8("A");
    classWriter.newClass("A");
    classWriter.newMethodType("()V");
    classWriter.newModule("A");
    classWriter.newPackage("A");
    classWriter.newHandle(Opcodes.H_GETFIELD, "A", "h", "I");
    classWriter.newHandle(Opcodes.H_GETFIELD, "A", "h", "I", false);
    classWriter.newInvokeDynamic("m", "()V", new Handle(Opcodes.H_GETFIELD, "A", "h", "I", false));
    classWriter.newCondy(
        "m", "Ljava/lang/String;", new Handle(Opcodes.H_INVOKESTATIC, "A", "m", "()V", false));
    classWriter.newField("A", "f", "I");
    classWriter.newMethod("A", "m", "()V", false);
    classWriter.newNameType("m", "()V");

    assertThrows(IllegalArgumentException.class, () -> classWriter.newConst(new Object()));
  }

  @Test
  public void testConstantPoolSizeTooLarge() {
    ClassWriter classWriter = new ClassWriter(0);
    for (int i = 0; i < 65536; ++i) {
      classWriter.newConst(Integer.valueOf(i));
    }
    assertThrows(IndexOutOfBoundsException.class, () -> classWriter.toByteArray());
  }

  @Test
  public void testGetCommonSuperClass() {
    ClassWriter classWriter = new ClassWriter(0);
    assertEquals(
        "java/lang/Object",
        classWriter.getCommonSuperClass("java/lang/Object", "java/lang/Integer"));
    assertEquals(
        "java/lang/Object",
        classWriter.getCommonSuperClass("java/lang/Integer", "java/lang/Object"));
    assertEquals(
        "java/lang/Object",
        classWriter.getCommonSuperClass("java/lang/Integer", "java/lang/Runnable"));
    assertEquals(
        "java/lang/Object",
        classWriter.getCommonSuperClass("java/lang/Runnable", "java/lang/Integer"));
    assertEquals(
        "java/lang/Throwable",
        classWriter.getCommonSuperClass(
            "java/lang/IndexOutOfBoundsException", "java/lang/AssertionError"));
    assertThrows(
        TypeNotPresentException.class,
        () -> classWriter.getCommonSuperClass("-", "java/lang/Object"));
    assertThrows(
        TypeNotPresentException.class,
        () -> classWriter.getCommonSuperClass("java/lang/Object", "-"));
  }

  /** Tests that a ClassReader -> ClassWriter transform leaves classes unchanged. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWrite(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    classReader.accept(classWriter, attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that a ClassReader -> ClassWriter transform with the SKIP_CODE option produces a valid
   * class.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithSkipCode(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    classReader.accept(classWriter, attributes(), ClassReader.SKIP_CODE);
    assertThatClass(classWriter.toByteArray()).contains(classParameter.getInternalName());
  }

  /**
   * Tests that a ClassReader -> ClassWriter transform with the copy pool option leaves classes
   * unchanged.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithCopyPool(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(classReader, 0);
    classReader.accept(classWriter, attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that a ClassReader -> ClassWriter transform with the EXPAND_FRAMES option leaves classes
   * unchanged.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithExpandFrames(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    classReader.accept(classWriter, attributes(), ClassReader.EXPAND_FRAMES);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that a ClassReader -> ClassWriter transform with the COMPUTE_MAXS option leaves classes
   * unchanged. This is not true in general (the valid max stack and max locals for a given method),
   * but this should be the case with our precompiled classes.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithComputeMaxs(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classReader.accept(classWriter, attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that classes going through a ClassReader -> ClassWriter transform with the COMPUTE_MAXS
   * option can be loaded and pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadWriteAndLoadWithComputeMaxs(
      final PrecompiledClass classParameter, final Api apiParameter) throws Exception {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classReader.accept(classWriter, attributes(), 0);
    assertThat(() -> loadAndInstantiate(classParameter.getName(), classWriter.toByteArray()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that classes going through a ClassReader -> ClassWriter transform with the COMPUTE_FRAMES
   * option can be loaded and pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithComputeFrames(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    // jdk3.AllInstructions and jdk3.LargeMethod contain JSR/RET instructions,
    // incompatible with COMPUTE_FRAMES.
    if (classParameter == PrecompiledClass.JDK3_ALL_INSTRUCTIONS
        || classParameter == PrecompiledClass.JDK3_LARGE_METHOD) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classWriter, attributes(), 0));
      return;
    }
    classReader.accept(classWriter, attributes(), 0);

    byte[] newClassFile = classWriter.toByteArray();
    // The computed stack map frames should be equal to the original ones, if any (classes before
    // JDK8 don't have ones). This is not true in general (the valid frames for a given method are
    // not unique), but this should be the case with our precompiled classes.
    if (classParameter.isMoreRecentThan(Api.ASM4)) {
      assertThatClass(newClassFile).isEqualTo(classFile);
    }
    assertThat(() -> loadAndInstantiate(classParameter.getName(), newClassFile))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that classes going through a ClassReader -> ClassWriter transform with the SKIP_FRAMES
   * and COMPUTE_FRAMES options can be loaded and pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithSkipAndComputeFrames(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

    // jdk3.AllInstructions and jdk3.LargeMethod contain JSR/RET instructions,
    // incompatible with COMPUTE_FRAMES.
    if (classParameter == PrecompiledClass.JDK3_ALL_INSTRUCTIONS
        || classParameter == PrecompiledClass.JDK3_LARGE_METHOD) {
      assertThrows(
          RuntimeException.class,
          () -> classReader.accept(classWriter, attributes(), ClassReader.SKIP_FRAMES));
      return;
    }
    classReader.accept(classWriter, attributes(), ClassReader.SKIP_FRAMES);

    byte[] newClassFile = classWriter.toByteArray();
    // The computed stack map frames should be equal to the original ones, if any (classes before
    // JDK8 don't have ones). This is not true in general (the valid frames for a given method are
    // not unique), but this should be the case with our precompiled classes.
    if (classParameter.isMoreRecentThan(Api.ASM4)) {
      assertThatClass(newClassFile).isEqualTo(classFile);
    }
    assertThat(() -> loadAndInstantiate(classParameter.getName(), newClassFile))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that classes with dead code going through a ClassWriter with the COMPUTE_FRAMES option
   * can be loaded and pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithComputeFramesAndDeadCode(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    ClassVisitor classVisitor = new DeadCodeInserter(apiParameter.value(), classWriter);

    // jdk3.AllInstructions and jdk3.LargeMethod contain JSR/RET instructions,
    // incompatible with COMPUTE_FRAMES.
    if (classParameter == PrecompiledClass.JDK3_ALL_INSTRUCTIONS
        || classParameter == PrecompiledClass.JDK3_LARGE_METHOD
        || classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(
          RuntimeException.class,
          () -> classReader.accept(classVisitor, attributes(), ClassReader.SKIP_FRAMES));
      return;
    }
    classReader.accept(classVisitor, attributes(), ClassReader.SKIP_FRAMES);

    assertThat(() -> loadAndInstantiate(classParameter.getName(), classWriter.toByteArray()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that classes with large methods (more than 32k) going through a ClassWriter with no
   * option can be loaded and pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithResizeMethod(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    if (classFile.length > Short.MAX_VALUE) return;

    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor = new NopInserter(apiParameter.value(), classWriter);

    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classVisitor, attributes(), 0));
      return;
    }
    classReader.accept(classVisitor, attributes(), 0);

    byte[] transformedClass = classWriter.toByteArray();
    assertThat(() -> loadAndInstantiate(classParameter.getName(), transformedClass))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());

    // The transformed class should have the same structure as the original one (#317792).
    ClassWriter originalClassWithoutCode = new ClassWriter(0);
    classReader.accept(originalClassWithoutCode, ClassReader.SKIP_CODE);
    ClassWriter transformedClassWithoutCode = new ClassWriter(0);
    new ClassReader(transformedClass).accept(transformedClassWithoutCode, ClassReader.SKIP_CODE);
    assertThatClass(transformedClassWithoutCode.toByteArray())
        .isEqualTo(originalClassWithoutCode.toByteArray());
  }

  /** Tests modules without any optional data (ModulePackage, ModuleMainClass, etc). */
  @Test
  public void testReadAndWriteWithBasicModule() {
    byte[] classFile = PrecompiledClass.JDK9_MODULE.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor =
        new ClassVisitor(Opcodes.ASM6, classWriter) {

          @Override
          public ModuleVisitor visitModule(
              final String name, final int access, final String version) {
            return new ModuleVisitor(api, super.visitModule(name, access, version)) {

              @Override
              public void visitMainClass(final String mainClass) {}

              @Override
              public void visitPackage(final String packaze) {}

              @Override
              public void visitRequire(
                  final String module, final int access, final String version) {
                super.visitRequire(module, access, null);
              }

              @Override
              public void visitExport(
                  final String packaze, final int access, final String... modules) {
                super.visitExport(packaze, access, (String[]) null);
              }

              @Override
              public void visitOpen(
                  final String packaze, final int access, final String... modules) {
                super.visitOpen(packaze, access, (String[]) null);
              }
            };
          }
        };
    classReader.accept(classVisitor, null, 0);
    classWriter.toByteArray();
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }

  private static class DeadCodeInserter extends ClassVisitor {

    private String className;

    DeadCodeInserter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public void visit(
        final int version,
        final int access,
        final String name,
        final String signature,
        final String superName,
        final String[] interfaces) {
      className = name;
      // Set V1_7 version to prevent fallback to old verifier.
      super.visit(
          (version & 0xFFFF) < Opcodes.V1_7 ? Opcodes.V1_7 : version,
          access,
          name,
          signature,
          superName,
          interfaces);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      int seed = (className + "." + name + descriptor).hashCode();
      return new MethodDeadCodeInserter(
          api, seed, super.visitMethod(access, name, descriptor, signature, exceptions));
    }
  }

  private static class MethodDeadCodeInserter extends MethodVisitor implements Opcodes {

    private Random random;
    private boolean inserted;

    MethodDeadCodeInserter(final int api, final int seed, final MethodVisitor methodVisitor) {
      super(api, methodVisitor);
      random = new Random(seed);
    }

    @Override
    public void visitInsn(final int opcode) {
      super.visitInsn(opcode);
      maybeInsertDeadCode();
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
      super.visitIntInsn(opcode, operand);
      maybeInsertDeadCode();
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
      super.visitVarInsn(opcode, var);
      maybeInsertDeadCode();
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
      super.visitTypeInsn(opcode, type);
      maybeInsertDeadCode();
    }

    @Override
    public void visitFieldInsn(
        final int opcode, final String owner, final String name, final String descriptor) {
      super.visitFieldInsn(opcode, owner, name, descriptor);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      maybeInsertDeadCode();
    }

    @Override
    public void visitInvokeDynamicInsn(
        final String name,
        final String descriptor,
        final Handle bootstrapMethodHandle,
        final Object... bootstrapMethodArguments) {
      super.visitInvokeDynamicInsn(
          name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
      maybeInsertDeadCode();
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
      super.visitJumpInsn(opcode, label);
      maybeInsertDeadCode();
    }

    @Override
    public void visitLdcInsn(final Object value) {
      super.visitLdcInsn(value);
      maybeInsertDeadCode();
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
      super.visitIincInsn(var, increment);
      maybeInsertDeadCode();
    }

    @Override
    public void visitTableSwitchInsn(
        final int min, final int max, final Label dflt, final Label... labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      maybeInsertDeadCode();
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
      super.visitMultiANewArrayInsn(descriptor, numDimensions);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMaxs(final int maxStack, final int maxLocals) {
      if (!inserted) {
        insertDeadCode();
      }
      super.visitMaxs(maxStack, maxLocals);
    }

    private void maybeInsertDeadCode() {
      // Inserts dead code once every 50 instructions in average.
      if (!inserted && random.nextFloat() < 1.0 / 50.0) {
        insertDeadCode();
      }
    }

    private void insertDeadCode() {
      Label end = new Label();
      visitJumpInsn(Opcodes.GOTO, end);
      visitLdcInsn("DEAD CODE");
      visitLabel(end);
      inserted = true;
    }
  }

  private static class NopInserter extends ClassVisitor {

    boolean transformed = false;

    NopInserter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
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
        private final HashSet<Label> labels = new HashSet<Label>();

        @Override
        public void visitLabel(final Label label) {
          super.visitLabel(label);
          labels.add(label);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
          if (!transformed && labels.contains(label)) {
            transformed = true;
            for (int i = 0; i <= Short.MAX_VALUE; ++i) {
              visitInsn(Opcodes.NOP);
            }
          }
          super.visitJumpInsn(opcode, label);
        }
      };
    }
  }
}
