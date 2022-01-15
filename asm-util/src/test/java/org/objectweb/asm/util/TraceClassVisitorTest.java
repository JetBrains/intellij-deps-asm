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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.PrintWriter;
import java.io.StringWriter;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.test.ClassFile;

/**
 * Unit tests for {@link TraceClassVisitor}.
 *
 * @author Eric Bruneton
 */
class TraceClassVisitorTest extends AsmTest {

  /**
   * Tests that classes are unchanged with a ClassReader->TraceClassVisitor->ClassWriter transform.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  void testVisitMethods(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor traceClassVisitor =
        new TraceClassVisitor(classWriter, new PrintWriter(new StringWriter()));

    classReader.accept(traceClassVisitor, new Attribute[] {new Comment(), new CodeComment()}, 0);

    assertEquals(new ClassFile(classFile), new ClassFile(classWriter.toByteArray()));
  }

  /** Tests that ClassReader can accept a TraceClassVisitor without delegate. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  void testVisitMethods_noDelegate(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    StringWriter output = new StringWriter();

    classReader.accept(new TraceClassVisitor(new PrintWriter(output, true)), 0);

    assertTrue(output.toString().contains(classParameter.getInternalName()));
  }

  /**
   * Tests that ClassReader can accept a TraceAnnotationVisitor, TraceFieldVisitor,
   * TraceMethodVisitor or TraceModuleVisitor without delegate.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  void testVisitMethods_noNestedDelegate(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);

    Executable accept =
        () ->
            classReader.accept(
                new ClassVisitor(apiParameter.value()) {

                  @Override
                  public ModuleVisitor visitModule(
                      final String name, final int access, final String version) {
                    return new TraceModuleVisitor(new Textifier());
                  }

                  @Override
                  public AnnotationVisitor visitAnnotation(
                      final String descriptor, final boolean visible) {
                    return new TraceAnnotationVisitor(new Textifier());
                  }

                  @Override
                  public FieldVisitor visitField(
                      final int access,
                      final String name,
                      final String descriptor,
                      final String signature,
                      final Object value) {
                    return new TraceFieldVisitor(new Textifier());
                  }

                  @Override
                  public MethodVisitor visitMethod(
                      final int access,
                      final String name,
                      final String descriptor,
                      final String signature,
                      final String[] exceptions) {
                    return new TraceMethodVisitor(new Textifier());
                  }
                },
                0);

    assertDoesNotThrow(accept);
  }
}
