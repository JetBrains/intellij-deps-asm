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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
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

  /** Tests {@link ClassReader(byte[])} and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testByteArrayConstructorAndAccessors(
      final PrecompiledClass classParameter, final Api apiParameter) {
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

  /** Tests {@link ClassReader(byte[],int,int)} and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testByteArrayConstructorWithOffsetAndAccessors(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    byte[] byteBuffer = new byte[classFile.length + 1];
    System.arraycopy(classFile, 0, byteBuffer, 1, classFile.length);
    ClassReader classReader = new ClassReader(byteBuffer, 1, classFile.length);
    assertTrue(classReader.getAccess() != 0);
    assertEquals(classParameter.getInternalName(), classReader.getClassName());
    if (classParameter.getInternalName().equals("module-info")) {
      assertNull(classReader.getSuperName());
    } else {
      assertTrue(classReader.getSuperName().startsWith("java"));
    }
    assertNotNull(classReader.getInterfaces());

    classReader.accept(
        new ClassVisitor(apiParameter.value()) {
          @Override
          public void visit(
              final int version,
              final int access,
              final String name,
              final String signature,
              final String superName,
              final String[] interfaces) {
            assertTrue((version & 0xFFFF) >= (Opcodes.V1_1 & 0xFFFF));
          }
        },
        0);
  }

  /** Tests {@link ClassReader(String)} and the basic ClassReader accessors. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testNameConstructorAndAccessors(
      final PrecompiledClass classParameter, final Api apiParameter) throws IOException {
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
  public void testStreamConstructorAndAccessors(
      final PrecompiledClass classParameter, final Api apiParameter) throws IOException {
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

  /** Tests {@link ClassReader(java.io.InputStream)} with an empty stream. */
  @Test
  public void testStreamConstructorWithEmptyStream() throws IOException {
    InputStream inputStream =
        new InputStream() {

          @Override
          public int available() throws IOException {
            return 0;
          }

          @Override
          public int read() throws IOException {
            return -1;
          }
        };
    assertTimeoutPreemptively(
        Duration.ofMillis(100),
        () -> assertThrows(RuntimeException.class, () -> new ClassReader(inputStream)));
  }

  /** Tests the ClassReader accept method with a default visitor. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithDefaultVisitor(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new ClassVisitor(apiParameter.value()) {};
    boolean hasNestHostOrMembers =
        classParameter == PrecompiledClass.JDK11_ALL_STRUCTURES
            || classParameter == PrecompiledClass.JDK11_ALL_STRUCTURES_NESTED;
    boolean hasModules = classParameter == PrecompiledClass.JDK9_MODULE;
    boolean hasTypeAnnotations = classParameter == PrecompiledClass.JDK8_ALL_STRUCTURES;
    assertThat(() -> classReader.accept(classVisitor, 0))
        .succeedsOrThrows(RuntimeException.class)
        .when(
            (hasNestHostOrMembers && apiParameter.value() < ASM7_EXPERIMENTAL)
                || (hasModules && apiParameter.value() < ASM6)
                || (hasTypeAnnotations && apiParameter.value() < ASM5));
  }

  /** Tests the ClassReader accept method with an empty visitor. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitor(
      final PrecompiledClass classParameter, final Api apiParameter) {
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
      final PrecompiledClass classParameter, final Api apiParameter) {
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
      final PrecompiledClass classParameter, final Api apiParameter) {
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
      final PrecompiledClass classParameter, final Api apiParameter) {
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
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor = new EmptyClassVisitor(apiParameter.value());
    // jdk8.ArtificialStructures contains structures which require ASM5, but only inside the method
    // code. Here we skip the code, so this class can be read with ASM4. Likewise for
    // jdk11.AllInstructions and jdk11.LambdaCondy.
    assertThat(() -> classReader.accept(classVisitor, ClassReader.SKIP_CODE))
        .succeedsOrThrows(RuntimeException.class)
        .when(
            classParameter.isMoreRecentThan(apiParameter)
                && classParameter != PrecompiledClass.JDK8_ARTIFICIAL_STRUCTURES
                && classParameter != PrecompiledClass.JDK11_ALL_INSTRUCTIONS
                && classParameter != PrecompiledClass.JDK11_LAMBDA_CONDY);
  }

  /**
   * Tests the ClassReader accept method with default annotation, field, method and module visitors.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithDefaultAnnotationFieldMethodAndModuleVisitor(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor =
        new EmptyClassVisitor(apiParameter.value()) {

          @Override
          public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
            return new AnnotationVisitor(api) {};
          }

          @Override
          public AnnotationVisitor visitTypeAnnotation(
              final int typeRef,
              final TypePath typePath,
              final String descriptor,
              final boolean visible) {
            return new AnnotationVisitor(api) {};
          }

          @Override
          public ModuleVisitor visitModule(
              final String name, final int access, final String version) {
            return new ModuleVisitor(api) {};
          }

          @Override
          public FieldVisitor visitField(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final Object value) {
            return new FieldVisitor(api) {};
          }

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            return new MethodVisitor(api) {};
          }
        };
    assertThat(() -> classReader.accept(classVisitor, 0))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /**
   * Tests the ClassReader accept method with a visitor that skips fields, methods, modules and nest
   * host and members.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAcceptWithEmptyVisitorAndSkipFieldMethodAndModuleContent(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassVisitor classVisitor =
        new EmptyClassVisitor(apiParameter.value()) {

          @Override
          public ModuleVisitor visitModule(
              final String name, final int access, final String version) {
            return null;
          }

          @Override
          public FieldVisitor visitField(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final Object value) {
            return null;
          }

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            return null;
          }

          @Override
          public void visitNestHostExperimental(final String nestHost) {}

          @Override
          public void visitNestMemberExperimental(final String nestMember) {}
        };
    classReader.accept(classVisitor, 0);
  }

  @Test
  public void testParameterAnnotationIndices() {
    final AtomicBoolean success = new AtomicBoolean(false);
    ClassReader classReader = new ClassReader(PrecompiledClass.JDK5_LOCAL_CLASS.getBytes());
    classReader.accept(
        new ClassVisitor(Opcodes.ASM7_EXPERIMENTAL) {
          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            return new MethodVisitor(api, null) {
              @Override
              public AnnotationVisitor visitParameterAnnotation(
                  final int parameter, final String descriptor, final boolean visible) {
                if (descriptor.equals("Ljava/lang/Deprecated;")) {
                  assertEquals(0, parameter);
                  success.set(true);
                }
                return null;
              }
            };
          }
        },
        0);
    assertTrue(success.get());
  }

  @Test
  public void testPreviewMinorVersion() {
    ClassReader classReader = new ClassReader(PrecompiledClass.JDK11_LAMBDA_CONDY.getBytes());
    classReader.accept(
        new ClassVisitor(Opcodes.ASM7_EXPERIMENTAL) {
          @Override
          public void visit(
              int version,
              int access,
              String name,
              String signature,
              String superName,
              String[] interfaces) {
            assertTrue(
                (version & Opcodes.V_PREVIEW_EXPERIMENTAL) == Opcodes.V_PREVIEW_EXPERIMENTAL);
          }
        },
        0);
  }

  /** Tests that reading an invalid class throws an exception. */
  @ParameterizedTest
  @EnumSource(InvalidClass.class)
  public void testInvalidClasses(final InvalidClass invalidClass) {
    if (invalidClass == InvalidClass.INVALID_CLASS_VERSION
        || invalidClass == InvalidClass.INVALID_CP_INFO_TAG) {
      assertThrows(IllegalArgumentException.class, () -> new ClassReader(invalidClass.getBytes()));
    } else {
      ClassReader classReader = new ClassReader(invalidClass.getBytes());
      if (invalidClass == InvalidClass.INVALID_CONSTANT_POOL_INDEX
          || invalidClass == InvalidClass.INVALID_CONSTANT_POOL_REFERENCE
          || invalidClass == InvalidClass.INVALID_BYTECODE_OFFSET) {
        assertThrows(
            ArrayIndexOutOfBoundsException.class,
            () -> classReader.accept(new EmptyClassVisitor(ASM7_EXPERIMENTAL), 0));
      } else {
        assertThrows(
            IllegalArgumentException.class,
            () -> classReader.accept(new EmptyClassVisitor(ASM7_EXPERIMENTAL), 0));
      }
    }
  }

  private static class EmptyClassVisitor extends ClassVisitor {

    AnnotationVisitor annotationVisitor =
        new AnnotationVisitor(api) {

          @Override
          public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
            return this;
          }

          @Override
          public AnnotationVisitor visitArray(final String name) {
            return this;
          }
        };

    EmptyClassVisitor(final int api) {
      super(api);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return annotationVisitor;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return annotationVisitor;
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      return new FieldVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return annotationVisitor;
        }
      };
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return new MethodVisitor(api) {

        @Override
        public AnnotationVisitor visitAnnotationDefault() {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible) {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return annotationVisitor;
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          return annotationVisitor;
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
          return annotationVisitor;
        }
      };
    }
  }
}
