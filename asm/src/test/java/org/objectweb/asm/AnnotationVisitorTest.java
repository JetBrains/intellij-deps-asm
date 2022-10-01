// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
// contributors may be used to endorse or promote products derived from
// this software without specific prior written permission.
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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.test.ClassFile;

/**
 * Unit tests for {@link AnnotationVisitor}.
 *
 * @author Eric Bruneton
 */
class AnnotationVisitorTest extends AsmTest {

  @Test
  void testConstructor_validApi() {
    Executable constructor = () -> new AnnotationVisitor(Opcodes.ASM4) {};

    assertDoesNotThrow(constructor);
  }

  @Test
  void testConstructor_invalidApi() {
    Executable constructor = () -> new AnnotationVisitor(0) {};

    Exception exception = assertThrows(IllegalArgumentException.class, constructor);
    assertEquals("Unsupported api 0", exception.getMessage());
  }

  @Test
  void testGetDelegate() {
    AnnotationVisitor delegate = new AnnotationVisitor(Opcodes.ASM4) {};
    AnnotationVisitor visitor = new AnnotationVisitor(Opcodes.ASM4, delegate) {};

    assertSame(delegate, visitor.getDelegate());
  }

  /**
   * Tests that ClassReader accepts visitor which return null AnnotationVisitor, and that returning
   * null AnnotationVisitor is equivalent to returning an EmptyAnnotationVisitor.
   */
  @ParameterizedTest
  @MethodSource("allClassesAndAllApis")
  void testReadAndWrite_removeOrDeleteAnnotations(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter removedAnnotationsClassWriter = new ClassWriter(0);
    ClassWriter deletedAnnotationsClassWriter = new ClassWriter(0);
    ClassVisitor removeAnnotationsAdapter =
        new RemoveAnnotationsAdapter(apiParameter.value(), removedAnnotationsClassWriter);
    ClassVisitor deleteAnnotationsAdapter =
        new DeleteAnnotationsAdapter(apiParameter.value(), deletedAnnotationsClassWriter);

    Executable removeAnnotations = () -> classReader.accept(removeAnnotationsAdapter, 0);
    Executable deleteAnnotations = () -> classReader.accept(deleteAnnotationsAdapter, 0);

    if (classParameter.isMoreRecentThan(apiParameter)) {
      Exception removeException =
          assertThrows(UnsupportedOperationException.class, removeAnnotations);
      Exception deleteException =
          assertThrows(UnsupportedOperationException.class, deleteAnnotations);
      assertTrue(removeException.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
      assertTrue(deleteException.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
    } else {
      assertDoesNotThrow(removeAnnotations);
      assertDoesNotThrow(deleteAnnotations);
      assertEquals(
          new ClassFile(removedAnnotationsClassWriter.toByteArray()),
          new ClassFile(deletedAnnotationsClassWriter.toByteArray()));
    }
  }

  private static class EmptyAnnotationVisitor extends AnnotationVisitor {

    EmptyAnnotationVisitor(final int api) {
      super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
      return this;
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
      return this;
    }
  }

  private static class RemoveAnnotationsAdapter extends ClassVisitor {

    RemoveAnnotationsAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return new EmptyAnnotationVisitor(api);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new EmptyAnnotationVisitor(api);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(
          api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(
            final int typeRef,
            final TypePath typePath,
            final Label[] start,
            final Label[] end,
            final int[] index,
            final String descriptor,
            final boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }
      };
    }
  }

  private static class DeleteAnnotationsAdapter extends ClassVisitor {

    DeleteAnnotationsAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return null;
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(
          api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitLocalVariableAnnotation(
            final int typeRef,
            final TypePath typePath,
            final Label[] start,
            final Label[] end,
            final int[] index,
            final String descriptor,
            final boolean visible) {
          return null;
        }
      };
    }
  }
}
