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
package org.objectweb.asm.tree.analysis;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * SimpleVerifier tests.
 *
 * @author Eric Bruneton
 */
public class SimpleVerifierTest extends AsmTest implements Opcodes {

  private Analyzer<?> anaylzer;

  private MethodNode methodNode;

  @BeforeEach
  public void setUp() {
    Type baseType = Type.getType("LC;");
    Type superType = Type.getType("Ljava/lang/Number;");
    anaylzer = new Analyzer<BasicValue>(new SimpleVerifier(baseType, superType, false));
    methodNode = new MethodNode(ACC_PUBLIC, "m", "()V", null, null);
  }

  private void assertValid() throws AnalyzerException {
    methodNode.visitInsn(RETURN);
    methodNode.visitMaxs(10, 10);
    anaylzer.analyze("C", methodNode);
    Frame<?>[] frames = anaylzer.getFrames();
    for (int i = 0; i < frames.length; ++i) {
      if (frames[i] != null) {
        frames[i].toString();
      }
    }
    anaylzer.getHandlers(0);
  }

  private void assertInvalid() {
    methodNode.visitInsn(RETURN);
    methodNode.visitMaxs(10, 10);
    assertThrows(AnalyzerException.class, () -> anaylzer.analyze("C", methodNode));
  }

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new SimpleVerifier() {});
  }

  @Test
  public void testInvalidOpcode() {
    methodNode.visitInsn(-1);
    assertInvalid();
  }

  @Test
  public void testInvalidPop() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(POP);
    assertInvalid();
  }

  @Test
  public void testInvalidPop2() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(POP2);
    assertInvalid();
  }

  @Test
  public void testInvalidDup() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(DUP);
    assertInvalid();
  }

  @Test
  public void testInvalidDupx1() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(DUP_X1);
    assertInvalid();
  }

  @Test
  public void testInvalidDupx2() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(DUP_X2);
    assertInvalid();
  }

  @Test
  public void testInvalidDup2() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(DUP2);
    assertInvalid();
  }

  @Test
  public void testInvalidDup2x1() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(DUP2_X1);
    assertInvalid();
  }

  @Test
  public void testInvalidDup2x2() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(DUP2_X2);
    assertInvalid();
  }

  @Test
  public void testInvalidSwap() {
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(SWAP);
    assertInvalid();
  }

  @Test
  public void testInvalidGetLocal() {
    methodNode.visitVarInsn(ALOAD, 10);
    assertInvalid();
  }

  @Test
  public void testInvalidSetLocal() {
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitVarInsn(ASTORE, 10);
    assertInvalid();
  }

  @Test
  public void testInvalidEmptyStack() {
    methodNode.visitInsn(POP);
    assertInvalid();
  }

  @Test
  public void testInvalidFullStack() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    assertInvalid();
  }

  @Test
  public void testInconsistentStackHeights() {
    Label ifLabel = new Label();
    methodNode.visitInsn(ICONST_0);
    methodNode.visitJumpInsn(IFEQ, ifLabel);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitLabel(ifLabel);
    assertInvalid();
  }

  @Test
  public void testInvalidNewArray() {
    methodNode.visitInsn(ICONST_1);
    methodNode.visitIntInsn(NEWARRAY, -1);
    assertInvalid();
  }

  @Test
  public void testInvalidAload() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitVarInsn(ISTORE, 1);
    methodNode.visitVarInsn(ALOAD, 1);
    assertInvalid();
  }

  @Test
  public void testInvalidAstore() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitVarInsn(ASTORE, 1);
    assertInvalid();
  }

  @Test
  public void testInvalidIstore() {
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitVarInsn(ISTORE, 1);
    assertInvalid();
  }

  @Test
  public void testInvalidCheckcast() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitTypeInsn(CHECKCAST, "java/lang/String");
    assertInvalid();
  }

  @Test
  public void testInvalidArraylength() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ARRAYLENGTH);
    assertInvalid();
  }

  @Test
  public void testInvalidAthrow() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ATHROW);
    assertInvalid();
  }

  @Test
  public void testInvalidIneg() {
    methodNode.visitInsn(FCONST_0);
    methodNode.visitInsn(INEG);
    assertInvalid();
  }

  @Test
  public void testInvalidIadd() {
    methodNode.visitInsn(FCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(IADD);
    assertInvalid();
  }

  @Test
  public void testInvalidIsub() {
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(FCONST_0);
    methodNode.visitInsn(ISUB);
    assertInvalid();
  }

  @Test
  public void testInvalidIastore() {
    methodNode.visitInsn(ICONST_1);
    methodNode.visitIntInsn(NEWARRAY, T_INT);
    methodNode.visitInsn(FCONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(IASTORE);
    assertInvalid();
  }

  @Test
  public void testInvalidFastore() {
    methodNode.visitInsn(ICONST_1);
    methodNode.visitIntInsn(NEWARRAY, T_FLOAT);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(FASTORE);
    assertInvalid();
  }

  @Test
  public void testInvalidLastore() {
    methodNode.visitInsn(ICONST_1);
    methodNode.visitInsn(ICONST_0);
    methodNode.visitInsn(LCONST_0);
    methodNode.visitInsn(LASTORE);
    assertInvalid();
  }

  @Test
  public void testInvalidMultianewarray() {
    methodNode.visitInsn(FCONST_1);
    methodNode.visitInsn(ICONST_2);
    methodNode.visitMultiANewArrayInsn("[[I", 2);
    assertInvalid();
  }

  @Test
  public void testInvalidInvokevirtual() {
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "java/lang/Object");
    methodNode.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "size", "()I", false);
    assertInvalid();
  }

  @Test
  public void testInvalidInvokeinterface() {
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "java/util/List");
    methodNode.visitInsn(FCONST_0);
    methodNode.visitMethodInsn(
        INVOKEINTERFACE, "java/util/List", "get", "(I)Ljava/lang/Object;", true);
    assertInvalid();
  }

  @Test
  public void testInvalidRet() {
    methodNode.visitVarInsn(RET, 1);
    assertInvalid();
  }

  @Test
  public void testInvalidFalloff() {
    methodNode.visitMaxs(10, 10);
    assertThrows(AnalyzerException.class, () -> anaylzer.analyze("C", methodNode));
  }

  @Test
  public void testInvalidSubroutineFalloff() {
    Label gotoLabel = new Label();
    Label jsrLabel = new Label();
    methodNode.visitJumpInsn(GOTO, gotoLabel);
    methodNode.visitLabel(jsrLabel);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitVarInsn(RET, 1);
    methodNode.visitLabel(gotoLabel);
    methodNode.visitJumpInsn(JSR, jsrLabel);
    methodNode.visitMaxs(10, 10);
    assertThrows(AnalyzerException.class, () -> anaylzer.analyze("C", methodNode));
  }

  @Test
  public void testNestedSubroutines() throws AnalyzerException {
    Label subroutine1Label = new Label();
    Label subroutine2Label = new Label();
    methodNode.visitJumpInsn(JSR, subroutine1Label);
    methodNode.visitInsn(RETURN);
    methodNode.visitLabel(subroutine1Label);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitJumpInsn(JSR, subroutine2Label);
    methodNode.visitJumpInsn(JSR, subroutine2Label);
    methodNode.visitVarInsn(RET, 1);
    methodNode.visitLabel(subroutine2Label);
    methodNode.visitVarInsn(ASTORE, 2);
    methodNode.visitVarInsn(RET, 2);
    assertValid();
  }

  @Test
  public void testSubroutineLocalsAccess() throws AnalyzerException {
    methodNode.visitCode();
    Label startLabel = new Label();
    Label exceptionHandler1Label = new Label();
    Label exceptionHandler2Label = new Label();
    Label subroutineLabel = new Label();
    methodNode.visitTryCatchBlock(startLabel, startLabel, exceptionHandler1Label, null);
    methodNode.visitTryCatchBlock(
        startLabel, exceptionHandler2Label, exceptionHandler2Label, "java/lang/RuntimeException");
    methodNode.visitLabel(startLabel);
    methodNode.visitJumpInsn(JSR, subroutineLabel);
    methodNode.visitInsn(RETURN);
    methodNode.visitLabel(exceptionHandler1Label);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitJumpInsn(JSR, subroutineLabel);
    methodNode.visitVarInsn(ALOAD, 1);
    methodNode.visitInsn(ATHROW);
    methodNode.visitLabel(subroutineLabel);
    methodNode.visitVarInsn(ASTORE, 2);
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitVarInsn(ASTORE, 3);
    methodNode.visitVarInsn(RET, 2);
    methodNode.visitLabel(exceptionHandler2Label);
    methodNode.visitVarInsn(ASTORE, 4);
    methodNode.visitVarInsn(ALOAD, 4);
    methodNode.visitInsn(ATHROW);
    assertValid();
  }

  @Disabled("TODO currently Analyzer can not detect this situation")
  @Test
  public void testOverlappingSubroutines() {
    // The problem is that other overlapping subroutine situations are valid, such as
    // when a nested subroutine implicitly returns to its parent subroutine, without a RET.
    Label subroutine1Label = new Label();
    Label subroutine2Label = new Label();
    Label endSubroutineLabel = new Label();
    methodNode.visitJumpInsn(JSR, subroutine1Label);
    methodNode.visitJumpInsn(JSR, subroutine2Label);
    methodNode.visitInsn(RETURN);
    methodNode.visitLabel(subroutine1Label);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitJumpInsn(GOTO, endSubroutineLabel);
    methodNode.visitLabel(subroutine2Label);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitLabel(endSubroutineLabel);
    methodNode.visitVarInsn(RET, 1);
    assertInvalid();
  }

  @Test
  public void testMerge() throws AnalyzerException {
    Label loopLabel = new Label();
    methodNode.visitVarInsn(ALOAD, 0);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "java/lang/Number");
    methodNode.visitVarInsn(ASTORE, 2);
    methodNode.visitVarInsn(ALOAD, 0);
    methodNode.visitVarInsn(ASTORE, 3);
    methodNode.visitLabel(loopLabel);
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "java/lang/Number");
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitVarInsn(ALOAD, 0);
    methodNode.visitVarInsn(ASTORE, 2);
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "java/lang/Integer");
    methodNode.visitVarInsn(ASTORE, 3);
    methodNode.visitJumpInsn(GOTO, loopLabel);
    assertValid();
  }

  @Test
  public void testClassNotFound() {
    Label loopLabel = new Label();
    methodNode.visitVarInsn(ALOAD, 0);
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitLabel(loopLabel);
    methodNode.visitInsn(ACONST_NULL);
    methodNode.visitTypeInsn(CHECKCAST, "D");
    methodNode.visitVarInsn(ASTORE, 1);
    methodNode.visitJumpInsn(GOTO, loopLabel);
    methodNode.visitMaxs(10, 10);
    assertThrows(Exception.class, () -> anaylzer.analyze("C", methodNode));
  }

  @Test
  void testIsAssignableFrom() {
    Type baseType = Type.getObjectType("C");
    Type superType = Type.getObjectType("D");
    Type interfaceType = Type.getObjectType("I");
    new SimpleVerifier(
        ASM7_EXPERIMENTAL, baseType, superType, Arrays.asList(interfaceType), false) {

      void test() {
        assertTrue(isAssignableFrom(baseType, baseType));
        assertTrue(isAssignableFrom(superType, baseType));
        assertTrue(isAssignableFrom(interfaceType, baseType));
      }

      @Override
      protected Class<?> getClass(final Type type) {
        // Return dummy classes, to make sure isAssignable in test() does not rely on them.
        if (type == baseType) return int.class;
        if (type == superType) return float.class;
        if (type == interfaceType) return double.class;
        return super.getClass(type);
      }
    }.test();

    new SimpleVerifier(ASM7_EXPERIMENTAL, interfaceType, null, null, true) {

      void test() {
        assertTrue(isAssignableFrom(interfaceType, baseType));
        assertTrue(isAssignableFrom(interfaceType, Type.getObjectType("[I")));
        assertFalse(isAssignableFrom(interfaceType, Type.INT_TYPE));
      }

      @Override
      protected Type getSuperClass(final Type type) {
        return superType;
      }
    }.test();
  }

  /**
   * Tests that the precompiled classes can be successfully analyzed with a SimpleVerifier.
   *
   * @throws AnalyzerException
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testAnalyze(final PrecompiledClass classParameter, final Api apiParameter)
      throws AnalyzerException {
    ClassNode classNode = new ClassNode();
    new ClassReader(classParameter.getBytes()).accept(classNode, 0);
    for (MethodNode methodNode : classNode.methods) {
      Analyzer<BasicValue> analyzer =
          new Analyzer<BasicValue>(
              new SimpleVerifier(
                  Type.getObjectType(classNode.name),
                  Type.getObjectType(classNode.superName),
                  (classNode.access & Opcodes.ACC_INTERFACE) != 0));
      analyzer.analyze(classNode.name, methodNode);
    }
  }
}
