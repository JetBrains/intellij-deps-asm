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
package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.test.ClassFile;
import org.objectweb.asm.tree.MethodNode;

/**
 * Unit tests for {@link LocalVariablesSorter}.
 *
 * @author Eric Bruneton
 */
class LocalVariablesSorterTest extends AsmTest {

  @Test
  void testConstructor() {
    MethodNode methodNode = new MethodNode();

    assertDoesNotThrow(() -> new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", methodNode));
    assertThrows(
        IllegalStateException.class,
        () -> new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", methodNode) {});
  }

  @Test
  void testVisitFrame_emptyFrame() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", new MethodNode());

    Executable visitFrame = () -> localVariablesSorter.visitFrame(Opcodes.F_NEW, 0, null, 0, null);

    assertDoesNotThrow(visitFrame);
  }

  @Test
  void testVisitFrame_invalidFrameType() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", new MethodNode());

    Executable visitFrame = () -> localVariablesSorter.visitFrame(Opcodes.F_FULL, 0, null, 0, null);

    Exception exception = assertThrows(IllegalArgumentException.class, visitFrame);
    assertEquals(
        "LocalVariablesSorter only accepts expanded frames (see ClassReader.EXPAND_FRAMES)",
        exception.getMessage());
  }

  @Test
  void testNewLocal_boolean() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.BOOLEAN_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_byte() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.BYTE_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_char() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.CHAR_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_short() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.SHORT_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_int() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.INT_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_float() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.FLOAT_TYPE);

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_long() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.LONG_TYPE);
    assertEquals(2, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_double() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.DOUBLE_TYPE);

    assertEquals(2, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_object() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.getObjectType("pkg/Class"));

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @Test
  void testNewLocal_array() {
    LocalVariablesSorter localVariablesSorter =
        new LocalVariablesSorter(Opcodes.ACC_STATIC, "()V", new MethodNode());

    localVariablesSorter.newLocal(Type.getType("[I"));

    assertEquals(1, localVariablesSorter.nextLocal);
  }

  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  void testAllMethods_precompiledClass(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor localVariablesSorter =
        new LocalVariablesSorterClassAdapter(apiParameter.value(), classWriter);

    Executable accept = () -> classReader.accept(localVariablesSorter, ClassReader.EXPAND_FRAMES);

    if (classParameter.isMoreRecentThan(apiParameter)) {
      Exception exception = assertThrows(UnsupportedOperationException.class, accept);
      assertTrue(exception.getMessage().matches(UNSUPPORTED_OPERATION_MESSAGE_PATTERN));
    } else {
      assertDoesNotThrow(accept);
      Executable newInstance = () -> new ClassFile(classWriter.toByteArray()).newInstance();
      if (classParameter.isNotCompatibleWithCurrentJdk()) {
        assertThrows(UnsupportedClassVersionError.class, newInstance);
      } else {
        assertDoesNotThrow(newInstance);
      }
    }
  }

  @Test
  void testAllMethods_issue317586() throws FileNotFoundException, IOException {
    ClassReader classReader =
        new ClassReader(Files.newInputStream(Paths.get("src/test/resources/Issue317586.class")));
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor localVariablesSorter =
        new LocalVariablesSorterClassAdapter(/* latest */ Opcodes.ASM10_EXPERIMENTAL, classWriter);

    classReader.accept(localVariablesSorter, ClassReader.EXPAND_FRAMES);

    assertDoesNotThrow(() -> new ClassFile(classWriter.toByteArray()).newInstance());
  }

  static class LocalVariablesSorterClassAdapter extends ClassVisitor {

    LocalVariablesSorterClassAdapter(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      MethodVisitor methodVisitor =
          super.visitMethod(access, name, descriptor, signature, exceptions);
      return new LocalVariablesSorter(api, access, descriptor, methodVisitor) {};
    }
  }
}
