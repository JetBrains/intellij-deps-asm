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

import java.util.Collection;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.test.AsmTest;

/**
 * AnnotationVisitor tests.
 *
 * @author Eric Bruneton
 */
public class AnnotationVisitorTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  /**
   * Tests that ClassReader accepts visitor which return null AnnotationVisitor, and that returning
   * null AnnotationVisitor is equivalent to returning an EmptyAnnotationVisitor.
   */
  @Test
  public void testRemoveOrDelete() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter classWriter1 = new ClassWriter(0);
    ClassWriter classWriter2 = new ClassWriter(0);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new RemoveAnnotationsAdapter(apiParameter.value(), classWriter1), 0);
    classReader.accept(new DeleteAnnotationsAdapter(apiParameter.value(), classWriter2), 0);
    assertThatClass(classWriter1.toByteArray()).isEqualTo(classWriter2.toByteArray());
  }

  static class EmptyAnnotationVisitor extends AnnotationVisitor {

    public EmptyAnnotationVisitor(final int api) {
      super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return this;
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
      return this;
    }
  }

  static class RemoveAnnotationsAdapter extends ClassVisitor {

    public RemoveAnnotationsAdapter(final int api, final ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
      return new EmptyAnnotationVisitor(api);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return new EmptyAnnotationVisitor(api);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions)) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            int parameter, String desc, boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return new EmptyAnnotationVisitor(api);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return new EmptyAnnotationVisitor(api);
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
          return new EmptyAnnotationVisitor(api);
        }
      };
    }
  }

  static class DeleteAnnotationsAdapter extends ClassVisitor {

    public DeleteAnnotationsAdapter(final int api, final ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
      return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return null;
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(api, cv.visitMethod(access, name, desc, signature, exceptions)) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return null;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            int parameter, String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return null;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return null;
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
          return null;
        }
      };
    }
  }
}
