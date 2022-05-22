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
package org.objectweb.asm.util;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.util.ArrayList;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.analysis.Analyzer;
import org.objectweb.asm.tree.analysis.AnalyzerException;
import org.objectweb.asm.tree.analysis.BasicValue;
import org.objectweb.asm.tree.analysis.BasicVerifier;
import org.objectweb.asm.tree.analysis.Frame;

/**
 * Unit tests for {@link CheckFrameAnalyzer}.
 *
 * @author Eric Bruneton
 */
class CheckFrameAnalyzerTest extends AsmTest {

  private static final String CLASS_NAME = "C";

  // Labels used to generate test cases.
  private final Label label0 = new Label();

  @Test
  void testAnalyze_invalidJsr() {
    MethodNode methodNode = new MethodNodeBuilder().jsr(label0).label(label0).vreturn().build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(message.contains("Error at instruction 0: JSR instructions are unsupported"));
  }

  @Test
  void testAnalyze_invalidRet() {
    MethodNode methodNode = new MethodNodeBuilder().ret(0).vreturn().build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(message.contains("Error at instruction 0: RET instructions are unsupported"));
  }

  @Test
  void testAnalyze_missingFrameAtJumpTarget() {
    MethodNode methodNode =
        new MethodNodeBuilder().iconst_0().ifne(label0).label(label0).vreturn().build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains("Error at instruction 1: Expected stack map frame at instruction 2"));
  }

  @Test
  void testAnalyze_missingFrameAfterGoto() {
    MethodNode methodNode =
        new MethodNodeBuilder()
            .nop()
            .go(label0)
            .nop()
            .label(label0)
            .frame(Opcodes.F_SAME, null, null)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains("Error at instruction 1: Expected stack map frame at instruction 2"));
  }

  @Test
  void testAnalyze_illegalFrameType() {
    MethodNode methodNode =
        new MethodNodeBuilder()
            .nop()
            .go(label0)
            .frame(123456, null, null)
            .nop()
            .label(label0)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(message.contains("Error at instruction 2: Illegal frame type 123456"));
  }

  @Test
  void testAnalyze_invalidAppendFrame() {
    MethodNode methodNode =
        new MethodNodeBuilder(/* maxStack = */ 0, /* maxLocals = */ 1)
            .nop()
            .frame(Opcodes.F_APPEND, new Object[] {Opcodes.INTEGER}, null)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains("Error at instruction 1: Cannot append more locals than maxLocals"));
  }

  @Test
  void testAnalyze_invalidChopFrame() {
    MethodNode methodNode =
        new MethodNodeBuilder(/* maxStack = */ 0, /* maxLocals = */ 1)
            .nop()
            .frame(Opcodes.F_CHOP, new Object[] {null, null}, null)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(message.contains("Error at instruction 1: Cannot chop more locals than defined"));
  }

  @Test
  void testAnalyze_illegalStackMapFrameValue() {
    MethodNode methodNode =
        new MethodNodeBuilder(/* maxStack = */ 0, /* maxLocals = */ 2)
            .nop()
            .frame(Opcodes.F_APPEND, new Object[] {new Object()}, null)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains("Error at instruction 1: Illegal stack map frame value java.lang.Object"));
  }

  @Test
  void testAnalyze_illegalLabelNodeStackMapFrameValue() {
    MethodNode methodNode =
        new MethodNodeBuilder(/* maxStack = */ 0, /* maxLocals = */ 2)
            .nop()
            .frame(Opcodes.F_APPEND, new Object[] {new LabelNode(label0)}, null)
            .label(label0)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains("Error at instruction 1: LabelNode does not designate a NEW instruction"));
  }

  @Test
  void testAnalyze_frameAtJumpTargetHasIncompatibleStackHeight() {
    MethodNode methodNode =
        new MethodNodeBuilder()
            .iconst_0()
            .ifne(label0)
            .iconst_0()
            .label(label0)
            .frame(Opcodes.F_SAME1, null, new Object[] {Opcodes.INTEGER})
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains(
            "Error at instruction 1: Stack map frame incompatible with frame at instruction 3 "
                + "(incompatible stack heights)"));
  }

  @Test
  void testAnalyze_frameAtJumpTargetHasIncompatibleLocalValues() {
    MethodNode methodNode =
        new MethodNodeBuilder()
            .iconst_0()
            .ifne(label0)
            .iconst_0()
            .label(label0)
            .frame(Opcodes.F_NEW, new Object[] {Opcodes.INTEGER}, null)
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains(
            "Error at instruction 1: Stack map frame incompatible with frame at instruction 3 "
                + "(incompatible types at local 0: R and I)"));
  }

  @Test
  void testAnalyze_frameAtJumpTargetHasIncompatibleStackValues() {
    MethodNode methodNode =
        new MethodNodeBuilder()
            .iconst_0()
            .iconst_0()
            .ifne(label0)
            .iconst_0()
            .iconst_0()
            .label(label0)
            .frame(Opcodes.F_NEW, new Object[] {"C"}, new Object[] {"C"})
            .vreturn()
            .build();

    Executable analyze = () -> newAnalyzer().analyze(CLASS_NAME, methodNode);

    String message = assertThrows(AnalyzerException.class, analyze).getMessage();
    assertTrue(
        message.contains(
            "Error at instruction 2: Stack map frame incompatible with frame at instruction 5 "
                + "(incompatible types at stack item 0: I and R)"));
  }

  /**
   * Tests that the precompiled classes can be successfully analyzed from their existing stack map
   * frames with a BasicVerifier.
   *
   * @throws AnalyzerException if the test class can't be analyzed.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  void testAnalyze_basicVerifier(final PrecompiledClass classParameter, final Api apiParameter)
      throws AnalyzerException {
    assumeFalse(hasJsrOrRetInstructions(classParameter));
    ClassNode classNode = computeFrames(classParameter);
    Analyzer<BasicValue> analyzer = newAnalyzer();

    ArrayList<Frame<? extends BasicValue>[]> methodFrames = new ArrayList<>();
    for (MethodNode methodNode : classNode.methods) {
      Frame<? extends BasicValue>[] result = analyzer.analyze(classNode.name, methodNode);
      methodFrames.add(result);
    }

    for (int i = 0; i < classNode.methods.size(); ++i) {
      Frame<? extends BasicValue>[] frames = methodFrames.get(i);
      for (int j = 0; j < lastJvmInsnIndex(classNode.methods.get(i)); ++j) {
        assertNotNull(frames[j]);
      }
    }
  }

  /**
   * Tests that the precompiled classes can be successfully analyzed from their existing stack map
   * frames with a BasicVerifier, even if the method node's max locals and max stack size are not
   * set.
   *
   * @throws AnalyzerException if the test class can't be analyzed.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  void testAnalyzeAndComputeMaxs_basicVerifier(
      final PrecompiledClass classParameter, final Api apiParameter) throws AnalyzerException {
    assumeFalse(hasJsrOrRetInstructions(classParameter));
    ClassNode classNode = computeFrames(classParameter);
    ArrayList<MethodMaxs> methodMaxs = MethodMaxs.getAndClear(classNode);
    Analyzer<BasicValue> analyzer = newAnalyzer();

    ArrayList<MethodMaxs> analyzedMethodMaxs = new ArrayList<>();
    for (MethodNode methodNode : classNode.methods) {
      analyzer.analyzeAndComputeMaxs(classNode.name, methodNode);
      analyzedMethodMaxs.add(new MethodMaxs(methodNode.maxStack, methodNode.maxLocals));
    }

    for (int i = 0; i < analyzedMethodMaxs.size(); ++i) {
      assertTrue(analyzedMethodMaxs.get(i).maxLocals >= methodMaxs.get(i).maxLocals);
      assertTrue(analyzedMethodMaxs.get(i).maxStack >= methodMaxs.get(i).maxStack);
    }
  }

  private static boolean hasJsrOrRetInstructions(final PrecompiledClass classParameter) {
    return classParameter == PrecompiledClass.JDK3_ALL_INSTRUCTIONS
        || classParameter == PrecompiledClass.JDK3_LARGE_METHOD;
  }

  private static ClassNode computeFrames(final PrecompiledClass classParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    classReader.accept(classWriter, 0);
    classFile = classWriter.toByteArray();
    ClassNode classNode = new ClassNode();
    new ClassReader(classFile).accept(classNode, 0);
    return classNode;
  }

  private static Analyzer<BasicValue> newAnalyzer() {
    return new CheckFrameAnalyzer<>(new BasicVerifier());
  }

  private static int lastJvmInsnIndex(final MethodNode method) {
    for (int i = method.instructions.size() - 1; i >= 0; --i) {
      if (method.instructions.get(i).getOpcode() >= 0) {
        return i;
      }
    }
    return -1;
  }

  private static class MethodMaxs {

    final int maxStack;
    final int maxLocals;

    MethodMaxs(final int maxStack, final int maxLocals) {
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
    }

    static ArrayList<MethodMaxs> getAndClear(final ClassNode classNode) {
      ArrayList<MethodMaxs> methodMaxs = new ArrayList<>();
      for (MethodNode methodNode : classNode.methods) {
        methodMaxs.add(new MethodMaxs(methodNode.maxStack, methodNode.maxLocals));
        methodNode.maxLocals = 0;
        methodNode.maxStack = 0;
      }
      return methodMaxs;
    }
  }
}
