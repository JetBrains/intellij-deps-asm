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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * CheckClassAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckClassAdapterTest extends AsmTest implements Opcodes {

  private CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

  @Test
  public void testMain() throws IOException {
    PrintStream err = System.err;
    PrintStream out = System.out;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    try {
      String thisClassName = getClass().getName();
      String thisClassFilePath =
          ClassLoader.getSystemResource(thisClassName.replace('.', '/') + ".class").getPath();
      CheckClassAdapter.main(new String[0]);
      CheckClassAdapter.main(new String[] {thisClassName});
      CheckClassAdapter.main(new String[] {thisClassFilePath});
      CheckClassAdapter.main(new String[] {"java.lang.Object"});
      assertThrows(
          IOException.class, () -> CheckClassAdapter.main(new String[] {"DoNotExist.class"}));
    } finally {
      System.setErr(err);
      System.setOut(out);
    }
  }

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new CheckClassAdapter(null) {});
  }

  @Test
  public void testVerifyValidClass() throws Exception {
    AtomicBoolean success = new AtomicBoolean(false);
    ClassReader classReader = new ClassReader(getClass().getName());
    CheckClassAdapter.verify(
        classReader,
        true,
        new PrintWriter(new StringWriter()) {
          @Override
          public void flush() {
            super.flush();
            success.set(true);
          }
        });
    assertTrue(success.get());
  }

  @Test
  public void testVerifyInvalidClass() {
    ClassWriter classWriter = new ClassWriter(0);
    classWriter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    MethodVisitor methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "m", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitVarInsn(ISTORE, 30);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(1, 31);
    methodVisitor.visitEnd();
    classWriter.visitEnd();
    ClassReader classReader = new ClassReader(classWriter.toByteArray());
    CheckClassAdapter.verify(classReader, true, new PrintWriter(new StringWriter()));
  }

  @Test
  public void testIllegalClassAccessFlag() {
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, 1 << 20, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testIllegalClassName() {
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, 0, null, null, "java/lang/Object", null));
  }

  @Test
  public void testNonJavaIdentifierClassNamePre15() {
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_4, 0, "class name", null, "java/lang/Object", null));
  }

  @Test
  public void testNonJavaIdentifierClassNamePost15() {
    // Checks that no error is thrown.
    checkClassAdapter.visit(V1_5, 0, "class name", null, "java/lang/Object", null);
  }

  @Test
  public void testIllegalSuperClass() {
    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_PUBLIC, "java/lang/Object", null, "java/lang/Object", null));
  }

  @Test
  public void testModuleInfoSuperClass() {
    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_PUBLIC, "module-info", null, "java/lang/Object", null));
  }

  @Test
  public void testIllegalInterfaceSuperClass() {
    assertThrows(
        Exception.class, () -> checkClassAdapter.visit(V1_1, ACC_INTERFACE, "I", null, "C", null));
  }

  @Test
  public void testIllegalClassSignature() {
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", "LC;I", "java/lang/Object", null));
  }

  @Test
  public void testIllegalClassAccessFlagSet() {
    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_FINAL + ACC_ABSTRACT, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testIllegalClassMemberVisitBeforeStart() {
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testIllegalMultipleVisitCalls() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testIllegalModuleName() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        RuntimeException.class, () -> checkClassAdapter.visitModule("pkg.invalid=name", 0, null));
  }

  @Test
  public void testIllegalMultipleVisitModuleCalls() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitModule("module1", Opcodes.ACC_OPEN, null);
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitModule("module2", 0, null));
  }

  @Test
  public void testIllegalMultipleVisitSourceCalls() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitSource(null, null);
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testIllegalOuterClassName() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitOuterClass(null, null, null));
  }

  @Test
  public void testIllegalMultipleVisitOuterClassCalls() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitOuterClass("name", null, null);
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitOuterClass(null, null, null));
  }

  @Test
  public void testIllegalInnerClassName() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitInnerClass("name", "outerName", "0validInnerName", 0);
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visitInnerClass("name", "outerName", "0illegalInnerName;", 0));
  }

  @Test
  public void testIllegalFieldAccessFlagSet() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visitField(ACC_PUBLIC + ACC_PRIVATE, "i", "I", null, null));
  }

  @Test
  public void testIllegalFieldSignature() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "L;", null));
    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "LC+", null));
    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "LC;I", null));
  }

  @Test
  public void testIllegalClassMemberVisitAfterEnd() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitEnd();
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testIllegalMethodSignature() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visitMethod(
                ACC_PUBLIC, "m", "()V", "<T::LI.J<*+LA;>;>()V^LA;X", null));
  }

  @Test
  public void testIllegalTypeAnnotation() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(
        RuntimeException.class,
        () -> checkClassAdapter.visitTypeAnnotation(0xFFFFFFFF, null, "LA;", true));
    assertThrows(
        RuntimeException.class,
        () -> checkClassAdapter.visitTypeAnnotation(0x00FFFFFF, null, "LA;", true));
  }

  @Test
  public void testIllegalClassAttribute() {
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitAttribute(null));
  }

  /**
   * Tests that classes are unchanged with a ClassReader->CheckClassAdapter->ClassWriter transform.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testCheckClassAdapter_classUnchanged(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor = new CheckClassAdapter(apiParameter.value(), classWriter, true);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classVisitor, attributes(), 0));
      return;
    }
    classReader.accept(classVisitor, attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /** Tests that {@link CheckClassAdapter.verify()} succeeds on all precompiled classes. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testCheckClassAdapter_verify(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    CheckClassAdapter.verify(classReader, /* dump = */ false, printWriter);
    printWriter.close();
    assertEquals("", stringWriter.toString());
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }
}
