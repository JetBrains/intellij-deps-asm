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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassVisitor tests. Also tests FieldVisitor, MethodVisitor, ModuleVisitor and AnnotationVisitor.
 *
 * @author Eric Bruneton
 */
public class ClassVisitorTest extends AsmTest {

  /**
   * Tests that classes are unchanged when transformed with a ClassReader -> class adapter ->
   * ClassWriter chain, where "class adapter" is a ClassVisitor which returns FieldVisitor,
   * MethodVisitor, ModuleVisitor and AnnotationVisitor instances.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithEmptyVisitor(PrecompiledClass classParameter, Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassAdapter classAdapter = new ClassAdapter(apiParameter.value(), classWriter);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classAdapter, attributes(), 0));
    } else {
      classReader.accept(classAdapter, attributes(), 0);
      assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
    }
  }

  /**
   * Tests that a ClassReader -> class adapter -> ClassWriter chain give the same result with or
   * without the copy pool option.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithCopyPoolAndExceptionAdapter(
      PrecompiledClass classParameter, Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassWriter classWriterWithCopyPool = new ClassWriter(classReader, 0);
    classReader.accept(new ChangeExceptionAdapter(classWriter), attributes(), 0);
    classReader.accept(new ChangeExceptionAdapter(classWriterWithCopyPool), attributes(), 0);
    assertThatClass(classWriterWithCopyPool.toByteArray()).isEqualTo(classWriter.toByteArray());
  }

  static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }

  private static class AnnotationAdapter extends AnnotationVisitor {

    AnnotationAdapter(int api, AnnotationVisitor av) {
      super(api, av);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return new AnnotationAdapter(api, super.visitAnnotation(name, desc));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      return new AnnotationAdapter(api, super.visitArray(name));
    }
  }

  private static class ClassAdapter extends ClassVisitor {

    ClassAdapter(int api, ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(desc, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    }

    @Override
    public FieldVisitor visitField(
        int access, String name, String desc, String signature, Object value) {
      return new FieldAdapter(api, super.visitField(access, name, desc, signature, value));
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      return new MethodAdapter(api, super.visitMethod(access, name, desc, signature, exceptions));
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
      return new ModuleVisitor(api, super.visitModule(name, access, version)) {};
    }
  };

  private static class FieldAdapter extends FieldVisitor {

    FieldAdapter(int api, FieldVisitor fv) {
      super(api, fv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(desc, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    }
  }

  private static class MethodAdapter extends MethodVisitor {

    MethodAdapter(int api, MethodVisitor mv) {
      super(api, mv);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return new AnnotationAdapter(api, super.visitAnnotationDefault());
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(desc, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, desc, visible));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      return new AnnotationAdapter(api, super.visitParameterAnnotation(parameter, desc, visible));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new AnnotationAdapter(
          api, super.visitInsnAnnotation(typeRef, typePath, desc, visible));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTryCatchAnnotation(typeRef, typePath, desc, visible));
    }

    @Override
    public AnnotationVisitor visitLocalVariableAnnotation(
        int typeRef,
        TypePath typePath,
        Label[] start,
        Label[] end,
        int[] index,
        String desc,
        boolean visible) {
      return new AnnotationAdapter(
          api,
          super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible));
    }
  }

  private static class ChangeExceptionAdapter extends ClassVisitor {

    ChangeExceptionAdapter(final ClassVisitor cv) {
      super(Opcodes.ASM6, cv);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      if (exceptions != null && exceptions.length > 0) {
        exceptions[0] = "java/lang/Throwable";
      }
      return super.visitMethod(access, name, desc, signature, exceptions);
    }
  }
}
