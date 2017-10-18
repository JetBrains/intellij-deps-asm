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

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashSet;
import java.util.Random;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassWriter tests.
 *
 * @author Eric Bruneton
 */
public class ClassWriterTest extends AsmTest {

  /** Tests that a ClassReader -> ClassWriter transform leaves classes unchanged. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWrite(PrecompiledClass classParameter, Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    classReader.accept(classWriter, attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that a ClassReader -> ClassWriter transform with the copy pool option leaves classes
   * unchanged.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithCopyPool(PrecompiledClass classParameter, Api apiParameter) {
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
  public void testReadAndWriteWithExpandFrames(PrecompiledClass classParameter, Api apiParameter) {
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
  public void testReadAndWriteWithComputeMaxs(PrecompiledClass classParameter, Api apiParameter) {
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
  public void testReadWriteAndLoadWithComputeMaxs(PrecompiledClass classParameter, Api apiParameter)
      throws Exception {
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
  public void testReadAndWriteWithComputeFrames(PrecompiledClass classParameter, Api apiParameter) {
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
      PrecompiledClass classParameter, Api apiParameter) {
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
      PrecompiledClass classParameter, Api apiParameter) {
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
  public void testReadAndWriteWithResizeMethod(PrecompiledClass classParameter, Api apiParameter) {
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

    assertThat(() -> loadAndInstantiate(classParameter.getName(), classWriter.toByteArray()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }

  private static class DeadCodeInserter extends ClassVisitor {

    private String className;

    DeadCodeInserter(int api, ClassVisitor cv) {
      super(api, cv);
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
        int access, String name, String desc, String signature, String[] exceptions) {
      int seed = (className + "." + name + desc).hashCode();
      return new MethodDeadCodeInserter(
          api, seed, super.visitMethod(access, name, desc, signature, exceptions));
    }
  }

  private static class MethodDeadCodeInserter extends MethodVisitor implements Opcodes {

    private Random r;
    private boolean inserted;

    MethodDeadCodeInserter(int api, int seed, final MethodVisitor mv) {
      super(api, mv);
      r = new Random(seed);
    }

    @Override
    public void visitInsn(int opcode) {
      super.visitInsn(opcode);
      maybeInsertDeadCode();
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      super.visitIntInsn(opcode, operand);
      maybeInsertDeadCode();
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      super.visitVarInsn(opcode, var);
      maybeInsertDeadCode();
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
      super.visitTypeInsn(opcode, type);
      maybeInsertDeadCode();
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      super.visitFieldInsn(opcode, owner, name, desc);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
      maybeInsertDeadCode();
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
      maybeInsertDeadCode();
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      super.visitJumpInsn(opcode, label);
      maybeInsertDeadCode();
    }

    @Override
    public void visitLdcInsn(Object cst) {
      super.visitLdcInsn(cst);
      maybeInsertDeadCode();
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      super.visitIincInsn(var, increment);
      maybeInsertDeadCode();
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
      super.visitTableSwitchInsn(min, max, dflt, labels);
      maybeInsertDeadCode();
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      super.visitLookupSwitchInsn(dflt, keys, labels);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      super.visitMultiANewArrayInsn(desc, dims);
      maybeInsertDeadCode();
    }

    @Override
    public void visitMaxs(int maxStack, int maxLocals) {
      if (!inserted) {
        insertDeadCode();
      }
      super.visitMaxs(maxStack, maxLocals);
    }

    private void maybeInsertDeadCode() {
      // Inserts dead code once every 50 instructions in average.
      if (!inserted && r.nextFloat() < 1.0 / 50.0) {
        insertDeadCode();
      }
    }

    private void insertDeadCode() {
      Label end = new Label();
      mv.visitJumpInsn(Opcodes.GOTO, end);
      mv.visitLdcInsn("DEAD CODE");
      mv.visitLabel(end);
      inserted = true;
    }
  }

  private static class NopInserter extends ClassVisitor {

    boolean transformed = false;

    NopInserter(int api, ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      return new MethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions)) {
        private final HashSet<Label> labels = new HashSet<Label>();

        @Override
        public void visitLabel(final Label label) {
          super.visitLabel(label);
          labels.add(label);
        }

        @Override
        public void visitJumpInsn(final int opcode, final Label label) {
          super.visitJumpInsn(opcode, label);
          if (opcode != Opcodes.GOTO) {
            if (!transformed && !labels.contains(label)) {
              transformed = true;
              for (int i = 0; i <= Short.MAX_VALUE; ++i) {
                mv.visitInsn(Opcodes.NOP);
              }
            }
          }
        }
      };
    }
  }
}
