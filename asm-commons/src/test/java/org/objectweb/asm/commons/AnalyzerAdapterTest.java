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

  /**
   * Tests that classes with additional frames inserted at each instruction, using the results of an
   * AnalyzerAdapter, can be instantiated and loaded. This makes sure the intermediate frames
   * computed by AnalyzerAdapter are correct, i.e. pass bytecode verification.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAnalyzeLoadAndInstantiate(PrecompiledClass classParameter, Api apiParameter)
      throws Exception {
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
              final String desc,
              final String signature,
              final String[] exceptions) {
            MethodVisitor methodVisitor =
                super.visitMethod(access, name, desc, signature, exceptions);
            AnalyzedFramesInserter inserter = new AnalyzedFramesInserter(methodVisitor);
            AnalyzerAdapter analyzerAdapter =
                new AnalyzerAdapter(api, owner, access, name, desc, inserter);
            inserter.analyzer = analyzerAdapter;
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

    AnalyzerAdapter analyzer;
    private boolean hasOriginalFrame;

    public AnalyzedFramesInserter(MethodVisitor mv) {
      super(Opcodes.ASM6, mv);
    }

    @Override
    public void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
      super.visitFrame(type, nLocal, local, nStack, stack);
      hasOriginalFrame = true;
    }

    private void maybeInsertFrame() {
      // Don't insert a frame if we already have one for this instruction, from the original class.
      if (!hasOriginalFrame) {
        if (analyzer.locals != null && analyzer.stack != null) {
          ArrayList<Object> local = toFrameTypes(analyzer.locals);
          ArrayList<Object> stack = toFrameTypes(analyzer.stack);
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
    private ArrayList<Object> toFrameTypes(List<Object> analyzerTypes) {
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
    public void visitInsn(int opcode) {
      maybeInsertFrame();
      super.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
      maybeInsertFrame();
      super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
      maybeInsertFrame();
      super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
      maybeInsertFrame();
      super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
      maybeInsertFrame();
      super.visitFieldInsn(opcode, owner, name, desc);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
      maybeInsertFrame();
      super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
      maybeInsertFrame();
      super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
      maybeInsertFrame();
      super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object cst) {
      maybeInsertFrame();
      super.visitLdcInsn(cst);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
      maybeInsertFrame();
      super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
      maybeInsertFrame();
      super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
      maybeInsertFrame();
      super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String desc, int dims) {
      maybeInsertFrame();
      super.visitMultiANewArrayInsn(desc, dims);
    }
  }
}
