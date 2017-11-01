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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassReader tests.
 *
 * @author Eric Bruneton
 */
public class ClassReaderTest extends AsmTest implements Opcodes {

  @Test
  public void testIllegalConstructorArgument() {
    assertThrows(IOException.class, () -> new ClassReader((InputStream) null));
  }

  @Test
  public void testGetItem() throws IOException {
    ClassReader classReader = new ClassReader(getClass().getName());
    int item = classReader.getItem(1);
    assertTrue(item >= 10);
    assertTrue(item < classReader.header);
  }

  @Test
  public void testReadByte() throws IOException {
    ClassReader classReader = new ClassReader(getClass().getName());
    assertEquals(classReader.b[0] & 0xFF, classReader.readByte(0));
  }

  @Test
  public void testGetAccess() throws Exception {
    String name = getClass().getName();
    assertEquals(ACC_PUBLIC | ACC_SUPER, new ClassReader(name).getAccess());
  }

  @Test
  public void testGetClassName() throws Exception {
    String name = getClass().getName();
    assertEquals(name.replace('.', '/'), new ClassReader(name).getClassName());
  }

  @Test
  public void testGetSuperName() throws Exception {
    assertEquals(
        AsmTest.class.getName().replace('.', '/'),
        new ClassReader(getClass().getName()).getSuperName());
    assertEquals(null, new ClassReader(Object.class.getName()).getSuperName());
  }

  @Test
  public void testGetInterfaces() throws Exception {
    String[] interfaces = new ClassReader(getClass().getName()).getInterfaces();
    assertNotNull(interfaces);
    assertEquals(1, interfaces.length);
    assertEquals(Opcodes.class.getName().replace('.', '/'), interfaces[0]);

    interfaces = new ClassReader(Opcodes.class.getName()).getInterfaces();
    assertNotNull(interfaces);
  }

  /** Tests {@link ClassReader(byte[])] and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testByteArrayConstructorAndAccessors(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    assertTrue(classReader.getAccess() != 0);
    assertEquals(classParameter.getInternalName(), classReader.getClassName());
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertTrue(classReader.getSuperName().startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests {@link ClassReader(String)} and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testNameConstructorAndAccessors(PrecompiledClass classParameter, Api apiParameter)
      throws IOException {
    ClassReader classReader = new ClassReader(classParameter.getName());
    assertTrue(classReader.getAccess() != 0);
    assertEquals(classParameter.getInternalName(), classReader.getClassName());
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertTrue(classReader.getSuperName().startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests {@link ClassReader(java.io.InputStream)} and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testStreamConstructorAndAccessors(PrecompiledClass classParameter, Api apiParameter)
      throws IOException {
    ClassReader classReader =
        new ClassReader(
            ClassLoader.getSystemResourceAsStream(
                classParameter.getName().replace('.', '/') + ".class"));
    assertTrue(classReader.getAccess() != 0);
    assertEquals(classParameter.getInternalName(), classReader.getClassName());
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertTrue(classReader.getSuperName().startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());
  }

  /** Tests the ClassReader accept method with an empty visitor. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitor(PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    assertThat(() -> classReader.accept(classVisitor, 0))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_DEBUG. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndSkipDebug(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    assertThat(() -> classReader.accept(classVisitor, ClassReader.SKIP_DEBUG))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /** Tests the ClassReader accept method with an empty visitor and EXPAND_FRAMES. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndExpandFrames(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    assertThat(() -> classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_FRAMES. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndSkipFrames(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    assertThat(() -> classReader.accept(classVisitor, ClassReader.SKIP_FRAMES))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /** Tests the ClassReader accept method with an empty visitor and SKIP_CODE. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndSkipCode(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    assertThat(() -> classReader.accept(classVisitor, ClassReader.SKIP_CODE))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /** Tests the ClassReader accept method with a visitor that skips fields and methods. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndSkipFieldAndMethodContent(
      PrecompiledClass classParameter, Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor =
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
        };
    assertThat(() -> classReader.accept(classVisitor, 0))
        .succeedsOrThrows(RuntimeException.class)
        .when(
            classParameter == PrecompiledClass.JDK9_MODULE
                && classParameter.isMoreRecentThan(apiParameter));
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
