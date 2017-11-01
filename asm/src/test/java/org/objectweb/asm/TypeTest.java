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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/**
 * Type tests.
 *
 * @author Eric Bruneton
 */
public class TypeTest implements Opcodes {

  /** Tests that {@link Type.getType(Class)} returns correct values for primitive types. */
  @Test
  public void testGetTypeForPrimitiveTypes() {
    assertEquals(Type.INT_TYPE, Type.getType(Integer.TYPE));
    assertEquals(Type.VOID_TYPE, Type.getType(Void.TYPE));
    assertEquals(Type.BOOLEAN_TYPE, Type.getType(Boolean.TYPE));
    assertEquals(Type.BYTE_TYPE, Type.getType(Byte.TYPE));
    assertEquals(Type.CHAR_TYPE, Type.getType(Character.TYPE));
    assertEquals(Type.SHORT_TYPE, Type.getType(Short.TYPE));
    assertEquals(Type.DOUBLE_TYPE, Type.getType(Double.TYPE));
    assertEquals(Type.FLOAT_TYPE, Type.getType(Float.TYPE));
    assertEquals(Type.LONG_TYPE, Type.getType(Long.TYPE));
  }

  /**
   * Tests that {@link Type.getInternalName()} and {@link Type.getInternalName(Class)} return
   * correct values.
   */
  @Test
  public void testGetInternalName() {
    String expectedInternalName = "org/objectweb/asm/TypeTest";
    assertEquals(expectedInternalName, Type.getType(TypeTest.class).getInternalName());
    assertEquals(expectedInternalName, Type.getInternalName(TypeTest.class));
  }

  /**
   * Tests that {@link Type.getConstructorDescriptor(Class)} returns correct values.
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  @Test
  public void testGetConstructorDescriptor() throws NoSuchMethodException, SecurityException {
    String constructorDescriptor =
        Type.getConstructorDescriptor(
            ClassReader.class.getConstructor(byte[].class, int.class, int.class));
    assertEquals("([BII)V", constructorDescriptor);
  }

  /**
   * Tests that {@link Type.getMethodDescriptor(Method)} returns correct values.
   *
   * @throws SecurityException
   * @throws NoSuchMethodException
   */
  @Test
  public void testGetMethodDescriptor() throws NoSuchMethodException, SecurityException {
    Method method = Arrays.class.getMethod("binarySearch", byte[].class, byte.class);
    Type[] argumentTypes = Type.getArgumentTypes(method);
    for (int i = 0; i < argumentTypes.length; ++i) {
      assertEquals(Type.getType(method.getParameterTypes()[i]), argumentTypes[i]);
    }
    Type returnType = Type.getReturnType(method);
    assertEquals(Type.getType(method.getReturnType()), returnType);
    assertEquals("([BB)I", Type.getMethodDescriptor(returnType, argumentTypes));
    assertEquals("([BB)I", Type.getMethodDescriptor(method));
  }

  /** Tests that {@link Type.getOpcode(int)} returns correct values. */
  @Test
  public void testGetOpcode() {
    Type objectType = Type.getType("Ljava/lang/Object;");
    assertEquals(BALOAD, Type.BOOLEAN_TYPE.getOpcode(IALOAD));
    assertEquals(BALOAD, Type.BYTE_TYPE.getOpcode(IALOAD));
    assertEquals(CALOAD, Type.CHAR_TYPE.getOpcode(IALOAD));
    assertEquals(SALOAD, Type.SHORT_TYPE.getOpcode(IALOAD));
    assertEquals(IALOAD, Type.INT_TYPE.getOpcode(IALOAD));
    assertEquals(FALOAD, Type.FLOAT_TYPE.getOpcode(IALOAD));
    assertEquals(LALOAD, Type.LONG_TYPE.getOpcode(IALOAD));
    assertEquals(DALOAD, Type.DOUBLE_TYPE.getOpcode(IALOAD));
    assertEquals(AALOAD, objectType.getOpcode(IALOAD));
    assertEquals(IADD, Type.BOOLEAN_TYPE.getOpcode(IADD));
    assertEquals(IADD, Type.BYTE_TYPE.getOpcode(IADD));
    assertEquals(IADD, Type.CHAR_TYPE.getOpcode(IADD));
    assertEquals(IADD, Type.SHORT_TYPE.getOpcode(IADD));
    assertEquals(IADD, Type.INT_TYPE.getOpcode(IADD));
    assertEquals(FADD, Type.FLOAT_TYPE.getOpcode(IADD));
    assertEquals(LADD, Type.LONG_TYPE.getOpcode(IADD));
    assertEquals(DADD, Type.DOUBLE_TYPE.getOpcode(IADD));
  }

  /** Tests that {@link Type.hashCode()} returns correct values. */
  @Test
  public void testHashcode() {
    assertTrue(Type.getType("Ljava/lang/Object;").hashCode() != 0);
  }

  /** Tests that {@link Type.getObjectType(String)} returns correct values. */
  @Test
  public void testGetObjectType() throws Exception {
    Type objectType = Type.getObjectType("java/lang/Object");
    assertEquals(Type.OBJECT, objectType.getSort());
    assertEquals("java.lang.Object", objectType.getClassName());
    assertEquals("Ljava/lang/Object;", objectType.getDescriptor());
    assertEquals(Type.getType("Ljava/lang/Object;"), objectType);
  }
}
