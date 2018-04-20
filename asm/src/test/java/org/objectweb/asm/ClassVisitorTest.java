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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassVisitor tests. Also tests FieldVisitor, MethodVisitor, ModuleVisitor and AnnotationVisitor.
 *
 * @author Eric Bruneton
 */
public class ClassVisitorTest extends AsmTest {

  @Test
  public void testConstuctor() {
    assertThrows(IllegalArgumentException.class, () -> new ClassVisitor(0) {});
    assertThrows(IllegalArgumentException.class, () -> new ClassVisitor(Integer.MAX_VALUE) {});
  }

  /**
   * Tests that classes are unchanged when transformed with a ClassReader -> class adapter ->
   * ClassWriter chain, where "class adapter" is a ClassVisitor which returns FieldVisitor,
   * MethodVisitor, ModuleVisitor and AnnotationVisitor instances.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithEmptyVisitor(
      final PrecompiledClass classParameter, final Api apiParameter) {
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
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassWriter classWriterWithCopyPool = new ClassWriter(classReader, 0);
    classReader.accept(new ChangeExceptionAdapter(classWriter), attributes(), 0);
    classReader.accept(new ChangeExceptionAdapter(classWriterWithCopyPool), attributes(), 0);
    assertThatClass(classWriterWithCopyPool.toByteArray()).isEqualTo(classWriter.toByteArray());
  }

  /**
   * Tests that a ClassReader -> class adapter -> ClassWriter chain give the same result with or
   * without the copy pool option.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithCopyPoolAndDeprecatedAdapter(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassWriter classWriterWithCopyPool = new ClassWriter(classReader, 0);
    int access = Opcodes.ACC_DEPRECATED;
    classReader.accept(new ChangeAccessAdapter(classWriter, access), attributes(), 0);
    classReader.accept(new ChangeAccessAdapter(classWriterWithCopyPool, access), attributes(), 0);
    assertThatClass(classWriterWithCopyPool.toByteArray()).isEqualTo(classWriter.toByteArray());
  }

  /**
   * Tests that a ClassReader -> class adapter -> ClassWriter chain give the same result with or
   * without the copy pool option.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWriteWithCopyPoolAndSyntheticAdapter(
      final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassWriter classWriterWithCopyPool = new ClassWriter(classReader, 0);
    int access = Opcodes.ACC_SYNTHETIC;
    classReader.accept(new ChangeAccessAdapter(classWriter, access), attributes(), 0);
    classReader.accept(new ChangeAccessAdapter(classWriterWithCopyPool, access), attributes(), 0);
    assertThatClass(classWriterWithCopyPool.toByteArray()).isEqualTo(classWriter.toByteArray());
  }

  /** Test that classes with only visible or only invisible annotations can be read correctly. */
  @ParameterizedTest
  @ValueSource(strings = {"true", "false"})
  public void testReadAndWriteWithRemoveAnnotationAdapter(final boolean visibilityValue) {
    ClassWriter classWriter = new ClassWriter(0);
    new ClassReader(PrecompiledClass.JDK8_ALL_STRUCTURES.getBytes())
        .accept(new RemoveAnnotationAdapter(classWriter, visibilityValue), 0);
    byte[] classFile = classWriter.toByteArray();

    ClassWriter newClassWriter = new ClassWriter(0);
    new ClassReader(classFile)
        .accept(new RemoveAnnotationAdapter(newClassWriter, visibilityValue), 0);
    assertThatClass(newClassWriter.toByteArray()).isEqualTo(classFile);
  }

  static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }

  private static class AnnotationAdapter extends AnnotationVisitor {

    AnnotationAdapter(final int api, final AnnotationVisitor annotationVisitor) {
      super(api, annotationVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
      return new AnnotationAdapter(api, super.visitAnnotation(name, descriptor));
    }

    @Override
    public AnnotationVisitor visitArray(final String name) {
      return new AnnotationAdapter(api, super.visitArray(name));
    }
  }

  private static class ClassAdapter extends ClassVisitor {

    ClassAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      return new FieldAdapter(api, super.visitField(access, name, descriptor, signature, value));
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return new MethodAdapter(
          api, super.visitMethod(access, name, descriptor, signature, exceptions));
    }

    @Override
    public ModuleVisitor visitModule(final String name, final int access, final String version) {
      return new ModuleVisitor(api, super.visitModule(name, access, version)) {};
    }
  };

  private static class FieldAdapter extends FieldVisitor {

    FieldAdapter(final int api, final FieldVisitor fieldVisitor) {
      super(api, fieldVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }
  }

  private static class MethodAdapter extends MethodVisitor {

    MethodAdapter(final int api, final MethodVisitor methodVisitor) {
      super(api, methodVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotationDefault() {
      return new AnnotationAdapter(api, super.visitAnnotationDefault());
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return new AnnotationAdapter(api, super.visitAnnotation(descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTypeAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(
        final int parameter, final String descriptor, final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitParameterAnnotation(parameter, descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitInsnAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitInsnAnnotation(typeRef, typePath, descriptor, visible));
    }

    @Override
    public AnnotationVisitor visitTryCatchAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return new AnnotationAdapter(
          api, super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible));
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
      return new AnnotationAdapter(
          api,
          super.visitLocalVariableAnnotation(
              typeRef, typePath, start, end, index, descriptor, visible));
    }
  }

  private static class ChangeExceptionAdapter extends ClassVisitor {

    ChangeExceptionAdapter(final ClassVisitor classVisitor) {
      super(Opcodes.ASM6, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      if (exceptions != null && exceptions.length > 0) {
        exceptions[0] = "java/lang/Throwable";
      }
      return super.visitMethod(access, name, descriptor, signature, exceptions);
    }
  }

  private static class ChangeAccessAdapter extends ClassVisitor {

    private final int accessFlags;
    
    ChangeAccessAdapter(final ClassVisitor classVisitor, final int accessFlags) {
      super(Opcodes.ASM6, classVisitor);
      this.accessFlags = accessFlags;
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return super.visitMethod(
          access ^ accessFlags, name, descriptor, signature, exceptions);
    }
  }

  /** A class visitor which removes either all visible or all invisible [type] annotations. */
  private static class RemoveAnnotationAdapter extends ClassVisitor {

    private final boolean visibilityValue;

    RemoveAnnotationAdapter(final ClassVisitor classVisitor, final boolean visibilityValue) {
      super(Opcodes.ASM6, classVisitor);
      this.visibilityValue = visibilityValue;
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      if (visible == visibilityValue) {
        return null;
      }
      return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      if (visible == visibilityValue) {
        return null;
      }
      return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      return new FieldVisitor(api, super.visitField(access, name, descriptor, signature, value)) {

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
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
      return new MethodVisitor(
          api, super.visitMethod(access, name, descriptor, signature, exceptions)) {

        @Override
        public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitAnnotation(descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTypeAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitParameterAnnotation(
            final int parameter, final String descriptor, final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitParameterAnnotation(parameter, descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitInsnAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitInsnAnnotation(typeRef, typePath, descriptor, visible);
        }

        @Override
        public AnnotationVisitor visitTryCatchAnnotation(
            final int typeRef,
            final TypePath typePath,
            final String descriptor,
            final boolean visible) {
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitTryCatchAnnotation(typeRef, typePath, descriptor, visible);
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
          if (visible == visibilityValue) {
            return null;
          }
          return super.visitLocalVariableAnnotation(
              typeRef, typePath, start, end, index, descriptor, visible);
        }
      };
    }
  }
}
