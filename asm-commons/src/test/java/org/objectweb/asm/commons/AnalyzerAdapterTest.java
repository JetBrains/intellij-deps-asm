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
package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * AnalyzerAdapter tests.
 *
 * @author Eric Bruneton
 */
public class AnalyzerAdapterTest extends AsmTest {

  @Test
  public void testConstructor() {
    new AnalyzerAdapter("pkg/Class", Opcodes.ACC_PUBLIC, "name", "()V", null);
    assertThrows(
        IllegalStateException.class,
        () -> new AnalyzerAdapter("pkg/Class", Opcodes.ACC_PUBLIC, "name", "()V", null) {});
  }

  @Test
  public void testVisitFrame() {
    AnalyzerAdapter analyzerAdapter =
        new AnalyzerAdapter("pkg/Class", Opcodes.ACC_PUBLIC, "name", "()V", null);
    analyzerAdapter.visitFrame(Opcodes.F_NEW, 0, null, 0, null);
    assertThrows(
        IllegalArgumentException.class,
        () -> analyzerAdapter.visitFrame(Opcodes.F_FULL, 0, null, 0, null));
  }

  /**
   * Tests that classes with additional frames inserted at each instruction, using the results of an
   * AnalyzerAdapter, can be instantiated and loaded. This makes sure the intermediate frames
   * computed by AnalyzerAdapter are correct, i.e. pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAnalyzeLoadAndInstantiate(
      final PrecompiledClass classParameter, final Api apiParameter) throws Exception {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor =
        new ClassVisitor(apiParameter.value(), classWriter) {

          private String owner;

          @Override
          public void visit(
              final int version,
              final int access,
              final String name,
              final String signature,
              final String superName,
              final String[] interfaces) {
            owner = name;
            super.visit(version, access, name, signature, superName, interfaces);
          }

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            MethodVisitor methodVisitor =
                super.visitMethod(access, name, descriptor, signature, exceptions);
            AnalyzedFramesInserter inserter = new AnalyzedFramesInserter(methodVisitor);
            AnalyzerAdapter analyzerAdapter =
                new AnalyzerAdapter(api, owner, access, name, descriptor, inserter) {

                  @Override
                  public void visitMaxs(final int maxStack, final int maxLocals) {
                    // AnalyzerAdapter should correctly recompute maxLocals from scratch.
                    super.visitMaxs(maxStack, 0);
                  }
                };
            inserter.setAnalyzerAdapter(analyzerAdapter);
            return analyzerAdapter;
          }
        };
    Executable test =
        () -> {
          classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES);
          loadAndInstantiate(classParameter.getName(), classWriter.toByteArray());
        };
    // jdk3.AllInstructions and jdk3.LargeMethod contain jsr/ret instructions,
    // which are not supported.
    if (classParameter == PrecompiledClass.JDK3_ALL_INSTRUCTIONS
        || classParameter == PrecompiledClass.JDK3_LARGE_METHOD
        || classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, test);
    } else {
      assertThat(test)
          .succeedsOrThrows(UnsupportedClassVersionError.class)
          .when(classParameter.isMoreRecentThanCurrentJdk());
    }
  }

  /**
   * Inserts intermediate frames before each instruction, using the types computed with an
   * AnalyzerAdapter.
   */
  static class AnalyzedFramesInserter extends MethodVisitor {

    private AnalyzerAdapter analyzerAdapter;
    private boolean hasOriginalFrame;

    AnalyzedFramesInserter(final MethodVisitor methodVisitor) {
      super(Opcodes.ASM7_EXPERIMENTAL, methodVisitor);
    }

    void setAnalyzerAdapter(final AnalyzerAdapter analyzerAdapter) {
      this.analyzerAdapter = analyzerAdapter;
    }

    @Override
    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack) {
      super.visitFrame(type, nLocal, local, nStack, stack);
      hasOriginalFrame = true;
    }

    private void maybeInsertFrame() {
      // Don't insert a frame if we already have one for this instruction, from the original class.
      if (!hasOriginalFrame) {
        if (analyzerAdapter.locals != null && analyzerAdapter.stack != null) {
          ArrayList<Object> local = toFrameTypes(analyzerAdapter.locals);
          ArrayList<Object> stack = toFrameTypes(analyzerAdapter.stack);
          super.visitFrame(
              Opcodes.F_NEW, local.size(), local.toArray(), stack.size(), stack.toArray());
        }
      }
      hasOriginalFrame = false;
    }

    /**
     * Converts local and stack types from AnalyzerAdapter to visitFrame format (long and double are
     * represented with one element in visitFrame, but with two elements in AnalyzerAdapter).
     */
    private ArrayList<Object> toFrameTypes(final List<Object> analyzerTypes) {
      ArrayList<Object> frameTypes = new ArrayList<Object>();
      for (int i = 0; i < analyzerTypes.size(); ++i) {
        Object value = analyzerTypes.get(i);
        frameTypes.add(value);
        if (value == Opcodes.LONG || value == Opcodes.DOUBLE) {
          ++i;
        }
      }
      return frameTypes;
    }

    @Override
    public void visitInsn(final int opcode) {
      maybeInsertFrame();
      super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
      maybeInsertFrame();
      super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(final int opcode, final int var) {
      maybeInsertFrame();
      super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(final int opcode, final String type) {
      maybeInsertFrame();
      super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(
        final int opcode, final String owner, final String name, final String descriptor) {
      maybeInsertFrame();
      super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String descriptor,
        final boolean isInterface) {
      maybeInsertFrame();
      super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(
        final String name,
        final String descriptor,
        final Handle bootstrapMethodHandle,
        final Object... bootstrapMethodArguments) {
      maybeInsertFrame();
      super.visitInvokeDynamicInsn(
          name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
      maybeInsertFrame();
      super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(final Object value) {
      maybeInsertFrame();
      super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(final int var, final int increment) {
      maybeInsertFrame();
      super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(
        final int min, final int max, final Label dflt, final Label... labels) {
      maybeInsertFrame();
      super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
      maybeInsertFrame();
      super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(final String descriptor, final int numDimensions) {
      maybeInsertFrame();
      super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }
  }
}
