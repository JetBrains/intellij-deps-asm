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

import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * AdviceAdapter tests.
 *
 * @author Eric Bruneton
 */
public class AdviceAdapterTest extends AsmTest {

  @Test
  public void testSimpleConstructor() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithTwoSuperInitInTwoBranches() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          Label label0 = new Label();
          Label label1 = new Label();
          methodGenerator.visitVarInsn(Opcodes.ILOAD, 1);
          methodGenerator.visitInsn(Opcodes.ICONST_1);
          methodGenerator.visitInsn(Opcodes.ICONST_2);
          methodGenerator.visitInsn(Opcodes.IADD);
          methodGenerator.visitJumpInsn(Opcodes.IF_ICMPEQ, label0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.visitJumpInsn(Opcodes.GOTO, label1);

          methodGenerator.visitLabel(label0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();

          methodGenerator.visitLabel(label1);
          methodGenerator.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
          methodGenerator.visitInsn(Opcodes.DUP);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.ATHROW);
        });
  }

  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  public void testConstructorWithTwoSuperInitInTwoSwitchBranches(final boolean useTableSwitch) {
    testCase(
        (MethodGenerator methodGenerator) -> {
          Label label0 = new Label();
          Label label1 = new Label();
          Label label2 = new Label();
          methodGenerator.visitVarInsn(Opcodes.ILOAD, 1);
          methodGenerator.visitInsn(Opcodes.ICONST_1);
          methodGenerator.visitInsn(Opcodes.ICONST_2);
          methodGenerator.visitInsn(Opcodes.IADD);
          if (useTableSwitch) {
            methodGenerator.visitTableSwitchInsn(0, 1, label0, new Label[] {label0, label1});
          } else {
            methodGenerator.visitLookupSwitchInsn(label0, new int[] {1}, new Label[] {label1});
          }
          for (int i = 0; i < 2; ++i) {
            methodGenerator.visitLabel(i == 0 ? label0 : label1);
            methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
            methodGenerator.visitMethodInsn(
                Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
            methodGenerator.expectMethodEnter();
            methodGenerator.visitJumpInsn(Opcodes.GOTO, label2);
          }
          methodGenerator.visitLabel(label2);
          methodGenerator.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
          methodGenerator.visitInsn(Opcodes.DUP);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false);
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.ATHROW);
        });
  }

  @Test
  public void testConstructorWithSuperInitsInNormalAndHandlerBranches() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          Label label0 = new Label();
          Label label1 = new Label();
          Label label2 = new Label();
          Label label3 = new Label();
          methodGenerator.visitTryCatchBlock(label0, label1, label2, "java/lang/Exception");
          methodGenerator.visitLabel(label0);
          methodGenerator.visitInsn(Opcodes.NOP);
          methodGenerator.visitLabel(label1);

          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.visitJumpInsn(Opcodes.GOTO, label3);

          methodGenerator.visitLabel(label2);
          methodGenerator.visitInsn(Opcodes.POP);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();

          methodGenerator.visitLabel(label3);
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithUnitThisInTwoBranches() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          Label label0 = new Label();
          Label label1 = new Label();
          methodGenerator.visitVarInsn(Opcodes.ILOAD, 1);
          methodGenerator.visitInsn(Opcodes.I2L);
          methodGenerator.visitInsn(Opcodes.LCONST_0);
          methodGenerator.visitInsn(Opcodes.LCONST_1);
          methodGenerator.visitInsn(Opcodes.LADD);
          methodGenerator.visitInsn(Opcodes.LCMP);
          methodGenerator.visitJumpInsn(Opcodes.IFEQ, label0);
          methodGenerator.visitInsn(Opcodes.NOP);
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitInsn(Opcodes.DUP);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitJumpInsn(Opcodes.GOTO, label1);
          methodGenerator.visitLabel(label0);
          methodGenerator.visitInsn(Opcodes.ICONST_0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitInsn(Opcodes.SWAP);
          methodGenerator.visitInsn(Opcodes.POP);
          methodGenerator.visitLabel(label1);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithDupX1() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 1);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitInsn(Opcodes.DUP_X1);
          methodGenerator.visitInsn(Opcodes.POP);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithDupX2() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 1);
          methodGenerator.visitInsn(Opcodes.ACONST_NULL);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitInsn(Opcodes.DUP_X2);
          methodGenerator.visitInsn(Opcodes.POP2);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithDup2() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 1);
          methodGenerator.visitInsn(Opcodes.DUP2);
          methodGenerator.visitInsn(Opcodes.POP2);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithDup2X1() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitInsn(Opcodes.ACONST_NULL);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 1);
          methodGenerator.visitInsn(Opcodes.DUP2_X1);
          methodGenerator.visitInsn(Opcodes.POP2);
          methodGenerator.visitInsn(Opcodes.POP);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithDup2X2() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitVarInsn(Opcodes.ASTORE, 1);
          methodGenerator.visitInsn(Opcodes.LCONST_0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 1);
          methodGenerator.visitInsn(Opcodes.DUP2_X2);
          methodGenerator.visitInsn(Opcodes.POP2);
          methodGenerator.visitInsn(Opcodes.POP2);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithLongsAndArrays() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitTypeInsn(Opcodes.NEW, "C");
          methodGenerator.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/Long", "MAX_VALUE", "J");
          methodGenerator.visitVarInsn(Opcodes.LSTORE, 2);
          methodGenerator.visitInsn(Opcodes.ICONST_1);
          methodGenerator.visitIntInsn(Opcodes.NEWARRAY, Opcodes.T_LONG);
          methodGenerator.visitInsn(Opcodes.ICONST_0);
          methodGenerator.visitLdcInsn(123L);
          methodGenerator.visitInsn(Opcodes.LASTORE);
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          // No method enter here because the above call does not initializes 'this'.
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithMultiAnewArray() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitInsn(Opcodes.ICONST_1);
          methodGenerator.visitInsn(Opcodes.ICONST_2);
          methodGenerator.visitMultiANewArrayInsn("[[I", 2);
          methodGenerator.visitFieldInsn(Opcodes.PUTSTATIC, "C", "f", "[[I");
          methodGenerator.visitMethodInsn(Opcodes.INVOKESPECIAL, "C", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
        });
  }

  @Test
  public void testConstructorWithBranchesAfterSuperInit() {
    testCase(
        (MethodGenerator methodGenerator) -> {
          methodGenerator.visitVarInsn(Opcodes.ALOAD, 0);
          methodGenerator.visitMethodInsn(
              Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
          methodGenerator.expectMethodEnter();
          Label label1 = new Label();
          methodGenerator.visitJumpInsn(Opcodes.GOTO, label1);
          Label label2 = new Label();
          methodGenerator.visitLabel(label2);
          methodGenerator.visitInsn(Opcodes.POP);
          methodGenerator.expectMethodExit();
          methodGenerator.visitInsn(Opcodes.RETURN);
          methodGenerator.visitLabel(label1);
          methodGenerator.visitLdcInsn(Opcodes.ICONST_0);
          methodGenerator.visitJumpInsn(Opcodes.GOTO, label2);
        });
  }

  @Test
  public void testInvalidConstructor() {
    Consumer<MethodGenerator> constructorGenerator =
        new Consumer<MethodGenerator>() {

          @Override
          public void accept(final MethodGenerator methodGenerator) {
            methodGenerator.visitInsn(Opcodes.IRETURN);
          }
        };
    assertThrows(
        IllegalArgumentException.class,
        () -> generateClass(constructorGenerator, /* expectedClass= */ false));
  }

  private static void testCase(final Consumer<MethodGenerator> testCaseGenerator) {
    byte[] actualClass = generateClass(testCaseGenerator, /* expectedClass= */ false);
    byte[] expectedClass = generateClass(testCaseGenerator, /* expectedClass= */ true);
    assertThatClass(actualClass).isEqualTo(expectedClass);
    loadAndInstantiate("C", actualClass);
  }

  private static byte[] generateClass(
      final Consumer<MethodGenerator> constructorGenerator, final boolean expectedClass) {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    classWriter.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);

    classWriter.visitField(Opcodes.ACC_STATIC, "f", "[[I", null, null);

    String descriptor = "(I)V";
    MethodVisitor methodVisitor =
        classWriter.visitMethod(Opcodes.ACC_PUBLIC, "<init>", descriptor, null, null);
    MethodGenerator methodGenerator;
    if (expectedClass) {
      methodGenerator = new MethodGenerator(methodVisitor, /* expectedClass= */ true);
    } else {
      methodGenerator =
          new MethodGenerator(
              new AdviceAdapter(
                  Opcodes.ASM6, methodVisitor, Opcodes.ACC_PUBLIC, "<init>", descriptor) {

                @Override
                protected void onMethodEnter() {
                  generateAdvice(this, /* enter= */ true);
                }

                @Override
                protected void onMethodExit(final int opcode) {
                  generateAdvice(this, /* enter= */ false);
                }
              },
              /* expectedClass= */ false);
    }
    methodGenerator.visitCode();
    constructorGenerator.accept(methodGenerator);
    methodGenerator.visitMaxs(0, 0);
    methodGenerator.visitEnd();

    classWriter.visitEnd();
    return classWriter.toByteArray();
  }

  private static class MethodGenerator extends MethodVisitor {

    private final boolean expectedClass;

    MethodGenerator(final MethodVisitor methodVisitor, final boolean expectedClass) {
      super(Opcodes.ASM6, methodVisitor);
      this.expectedClass = expectedClass;
    }

    public void expectMethodEnter() {
      if (expectedClass) {
        generateAdvice(this, /* enter= */ true);
      }
    }

    public void expectMethodExit() {
      if (expectedClass) {
        generateAdvice(this, /* enter= */ false);
      }
    }
  }

  private static void generateAdvice(final MethodVisitor methodVisitor, final boolean enter) {
    methodVisitor.visitFieldInsn(
        Opcodes.GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
    methodVisitor.visitLdcInsn(enter ? "enter" : "exit");
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKEVIRTUAL,
        "java/io/PrintStream",
        "println",
        "(Ljava/lang/String;)V",
        /* isInterface= */ false);
  }

  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testEmptyAdviceAdapter(final PrecompiledClass classParameter, final Api apiParameter)
      throws Exception {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter expectedClassWriter = new ClassWriter(0);
    ClassWriter actualClassWriter = new ClassWriter(0);
    ClassVisitor expectedClassVisitor =
        new ReferenceClassAdapter(apiParameter.value(), expectedClassWriter);
    ClassVisitor actualClassVisitor =
        new EmptyAdviceClassAdapter(apiParameter.value(), actualClassWriter);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(
          RuntimeException.class,
          () -> classReader.accept(expectedClassVisitor, ClassReader.EXPAND_FRAMES));
      assertThrows(
          RuntimeException.class,
          () -> classReader.accept(actualClassVisitor, ClassReader.EXPAND_FRAMES));
      return;
    }
    classReader.accept(expectedClassVisitor, ClassReader.EXPAND_FRAMES);
    classReader.accept(actualClassVisitor, ClassReader.EXPAND_FRAMES);
    assertThatClass(actualClassWriter.toByteArray()).isEqualTo(expectedClassWriter.toByteArray());
  }

  private static class ReferenceClassAdapter extends ClassVisitor {

    ReferenceClassAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
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
      if (methodVisitor == null || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) > 0) {
        return methodVisitor;
      }
      return new LocalVariablesSorter(api, access, descriptor, methodVisitor);
    }
  }

  private static class EmptyAdviceClassAdapter extends ClassVisitor {

    EmptyAdviceClassAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
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
      if (methodVisitor == null || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) > 0) {
        return methodVisitor;
      }
      return new AdviceAdapter(api, methodVisitor, access, name, descriptor) {};
    }
  }
}
