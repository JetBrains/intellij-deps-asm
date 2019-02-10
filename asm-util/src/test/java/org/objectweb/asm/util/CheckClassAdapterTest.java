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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.test.ClassFile;
import org.objectweb.asm.tree.analysis.AnalyzerException;

/**
 * Unit tests for {@link CheckClassAdapter}.
 *
 * @author Eric Bruneton
 */
public class CheckClassAdapterTest extends AsmTest implements Opcodes {

  private static final String EXPECTED_USAGE =
      "Verifies the given class.\n"
          + "Usage: CheckClassAdapter <fully qualified class name or class file name>\n";

  @Test
  public void testConstructor() {
    assertDoesNotThrow(() -> new CheckClassAdapter(null));
    assertThrows(IllegalStateException.class, () -> new CheckClassAdapter(null) {});
  }

  @Test
  public void testVisit_illegalClassAccessFlag() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, 1 << 20, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_illegalClassName() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, 0, null, null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_nonJavaIdentifierClassNamePre15() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_4, 0, "class name", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_nonJavaIdentifierClassNamePost15() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertDoesNotThrow(
        () -> checkClassAdapter.visit(V1_5, 0, "class name", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_illegalSuperClass() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_PUBLIC, "java/lang/Object", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_moduleInfoSuperClass() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_PUBLIC, "module-info", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_illegalInterfaceSuperClass() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class, () -> checkClassAdapter.visit(V1_1, ACC_INTERFACE, "I", null, "C", null));
  }

  @Test
  public void testVisit_illegalSignature() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", "LC;I", "java/lang/Object", null));
  }

  @Test
  public void testVisit_illegalAccessFlagSet() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visit(
                V1_1, ACC_FINAL + ACC_ABSTRACT, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testVisit_illegalMultipleCalls() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null));
  }

  @Test
  public void testVisitModule_illegalModuleName() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        RuntimeException.class, () -> checkClassAdapter.visitModule("pkg.invalid=name", 0, null));
  }

  @Test
  public void testVisitModule_illegalMultipleCalls() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitModule("module1", Opcodes.ACC_OPEN, null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitModule("module2", 0, null));
  }

  @Test
  public void testVisitSource_beforeStart() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testVisitSource_afterEnd() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitEnd();

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testVisitSource_illegalMultipleCalls() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitSource(null, null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitSource(null, null));
  }

  @Test
  public void testVisitOuterClass_illegalOuterClassName() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitOuterClass(null, null, null));
  }

  @Test
  public void testVisitOuterClass_illegalMultipleCalls() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitOuterClass("name", null, null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitOuterClass(null, null, null));
  }

  @Test
  public void testInnerClass_illegalInnerClassName() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);
    checkClassAdapter.visitInnerClass("name", "outerName", "0validInnerName", 0);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visitInnerClass("name", "outerName", "0illegalInnerName;", 0));
  }

  @Test
  public void testVisitField_illegalAccessFlagSet() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class,
        () -> checkClassAdapter.visitField(ACC_PUBLIC + ACC_PRIVATE, "i", "I", null, null));
  }

  @Test
  public void testVisitField_illegalFieldSignature1() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "L;", null));
  }

  @Test
  public void testVisitField_illegalFieldSignature2() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "LC+", null));
  }

  @Test
  public void testVisitField_illegalFieldSignature3() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class, () -> checkClassAdapter.visitField(ACC_PUBLIC, "i", "I", "LC;I", null));
  }

  @Test
  public void testVisitMethod_illegalSignature() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        Exception.class,
        () ->
            checkClassAdapter.visitMethod(
                ACC_PUBLIC, "m", "()V", "<T::LI.J<*+LA;>;>()V^LA;X", null));
  }

  @Test
  public void testVisitTypeAnnotation_illegalAnnotation1() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        RuntimeException.class,
        () -> checkClassAdapter.visitTypeAnnotation(0xFFFFFFFF, null, "LA;", true));
  }

  @Test
  public void testVisitTypeAnnotation_illegalAnnotation2() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(
        RuntimeException.class,
        () -> checkClassAdapter.visitTypeAnnotation(0x00FFFFFF, null, "LA;", true));
  }

  @Test
  public void testVisitAttribute_illegalAttribute() {
    CheckClassAdapter checkClassAdapter = new CheckClassAdapter(null);
    checkClassAdapter.visit(V1_1, ACC_PUBLIC, "C", null, "java/lang/Object", null);

    assertThrows(RuntimeException.class, () -> checkClassAdapter.visitAttribute(null));
  }

  /**
   * Tests that classes are unchanged with a ClassReader->CheckClassAdapter->ClassWriter transform.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testVisitMethods(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor = new CheckClassAdapter(apiParameter.value(), classWriter, true);

    Executable accept = () -> classReader.accept(classVisitor, attributes(), 0);

    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, accept);
    } else {
      classReader.accept(classVisitor, attributes(), 0);
      assertEquals(new ClassFile(classFile), new ClassFile(classWriter.toByteArray()));
    }
  }

  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testVerify(final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    StringWriter logger = new StringWriter();

    CheckClassAdapter.verify(
        classReader, /* printResults = */ false, new PrintWriter(logger, true));

    assertEquals("", logger.toString());
  }

  @Test
  public void testMain_missingClassName() throws IOException {
    StringWriter logger = new StringWriter();
    String[] args = new String[0];

    CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertEquals(EXPECTED_USAGE, logger.toString());
  }

  @Test
  public void testMain_tooManyArguments() throws IOException {
    StringWriter logger = new StringWriter();
    String[] args = {getClass().getName(), "extraArgument"};

    CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertEquals(EXPECTED_USAGE, logger.toString());
  }

  @Test
  public void testMain_classFileNotFound() {
    StringWriter logger = new StringWriter();
    String[] args = {"DoNotExist.class"};

    Executable main = () -> CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertThrows(IOException.class, main);
    assertEquals("", logger.toString());
  }

  @Test
  public void testMain_classNotFound() {
    StringWriter logger = new StringWriter();
    String[] args = {"do\\not\\exist"};

    Executable main = () -> CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertThrows(IOException.class, main);
    assertEquals("", logger.toString());
  }

  @Test
  public void testMain_className() throws IOException {
    StringWriter logger = new StringWriter();
    String[] args = {getClass().getName()};

    CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertEquals("", logger.toString());
  }

  @Test
  public void testMain_classFile() throws IOException {
    StringWriter logger = new StringWriter();
    String[] args = {
      ClassLoader.getSystemResource(getClass().getName().replace('.', '/') + ".class").getPath()
    };

    CheckClassAdapter.main(args, new PrintWriter(logger, true));

    assertEquals("", logger.toString());
  }

  @Test
  public void testVerify_validClass() throws Exception {
    ClassReader classReader = new ClassReader(getClass().getName());
    StringWriter logger = new StringWriter();

    CheckClassAdapter.verify(classReader, true, new PrintWriter(logger, true));

    String log = logger.toString();
    assertFalse(log.startsWith(AnalyzerException.class.getName() + ": Error at instruction"));
    assertTrue(log.contains("00000 CheckClassAdapterTest  :  :     ALOAD 0"));
  }

  @Test
  public void testVerify_invalidClass() {
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
    StringWriter logger = new StringWriter();

    CheckClassAdapter.verify(classReader, true, new PrintWriter(logger, true));

    String log = logger.toString();
    assertTrue(
        log.startsWith(
            AnalyzerException.class.getName()
                + ": Error at instruction 1: Expected I, but found LC;"));
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }
}
