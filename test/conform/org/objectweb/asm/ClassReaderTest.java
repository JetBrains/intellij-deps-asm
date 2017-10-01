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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassReader tests.
 *
 * @author Eric Bruneton
 */
public class ClassReaderTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  /** Tests {@link ClassReader(byte[])] and the basic ClassReader accessors. */
  @Test
  public void testByteArrayConstructorAndAccessors() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    assertTrue(classReader.getAccess() != 0);
    assertThat(classReader.getClassName(), equalTo(classParameter.getInternalName()));
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertThat(classReader.getSuperName(), startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests {@link ClassReader(String)} and the basic ClassReader accessors. */
  @Test
  public void testNameConstructorAndAccessors() throws IOException {
    ClassReader classReader = new ClassReader(classParameter.getName());
    assertTrue(classReader.getAccess() != 0);
    assertThat(classReader.getClassName(), equalTo(classParameter.getInternalName()));
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertThat(classReader.getSuperName(), startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests {@link ClassReader(java.io.InputStream)} and the basic ClassReader accessors. */
  @Test
  public void testStreamConstructorAndAccessors() throws IOException {
    ClassReader classReader =
        new ClassReader(
            ClassLoader.getSystemResourceAsStream(
                classParameter.getName().replace('.', '/') + ".class"));
    assertTrue(classReader.getAccess() != 0);
    assertThat(classReader.getClassName(), equalTo(classParameter.getInternalName()));
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertThat(classReader.getSuperName(), startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests the ClassReader accept method with an empty visitor. */
  @Test
  public void testAcceptWithEmptyVisitor() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new EmptyClassVisitor(apiParameter.value()), 0);
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_DEBUG. */
  @Test
  public void testAcceptWithEmptyVisitorAndSkipDebug() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new EmptyClassVisitor(apiParameter.value()), ClassReader.SKIP_DEBUG);
  }

  /** Tests the ClassReader accept method with an empty visitor and EXPAND_FRAMES. */
  @Test
  public void testAcceptWithEmptyVisitorAndExpandFrames() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new EmptyClassVisitor(apiParameter.value()), ClassReader.EXPAND_FRAMES);
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_FRAMES. */
  @Test
  public void testAcceptWithEmptyVisitorAndSkipFrames() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new EmptyClassVisitor(apiParameter.value()), ClassReader.SKIP_FRAMES);
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_CODE. */
  @Test
  public void testAcceptWithEmptyVisitorAndSkipCode() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(new EmptyClassVisitor(apiParameter.value()), ClassReader.SKIP_CODE);
  }

  /** Tests the ClassReader accept method with a visitor that skips fields and methods. */
  @Test
  public void testAcceptWithEmptyVisitorAndSkipFieldAndMethodContent() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    if (classParameter == PrecompiledClass.JDK9_MODULE
        && classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(
        new EmptyClassVisitor(apiParameter.value()) {
          @Override
          public FieldVisitor visitField(
              int access, String name, String desc, String signature, Object value) {
            return null;
          }

          @Override
          public MethodVisitor visitMethod(
              int access, String name, String desc, String signature, String[] exceptions) {
            return null;
          }
        },
        0);
  }

  private static class EmptyClassVisitor extends ClassVisitor {

    AnnotationVisitor av =
        new AnnotationVisitor(api) {

          @Override
          public AnnotationVisitor visitAnnotation(String name, String desc) {
            return this;
          }

          @Override
          public AnnotationVisitor visitArray(String name) {
            return this;
          }
        };

    EmptyClassVisitor(int api) {
      super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return av;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return av;
    }

    @Override
    public FieldVisitor visitField(
        int access, String name, String desc, String signature, Object value) {
      return new FieldVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          return av;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return av;
        }
      };
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      return new MethodVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return av;
        }

        @Override
        public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
          return av;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return av;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            int parameter, String desc, boolean visible) {
          return av;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return av;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            int typeRef, TypePath typePath, String desc, boolean visible) {
          return av;
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
          return av;
        }
      };
    }
  }
}
