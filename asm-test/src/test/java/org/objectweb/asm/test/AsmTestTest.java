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
package org.objectweb.asm.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.objectweb.asm.test.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link AsmTest}.
 *
 * @author Eric Bruneton
 */
public class AsmTestTest extends AsmTest {

  /** Tests the isMoreRecentThan method. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testIsMoreRecentThan(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classContent = classParameter.getBytes();
    int majorVersion = (classContent[6] & 0xFF) << 8 | (classContent[7] & 0xFF);
    boolean isMoreRecent = classParameter.isMoreRecentThan(apiParameter);
    switch (apiParameter) {
      case ASM4:
        assertEquals(majorVersion > /* V7 = */ 51, isMoreRecent);
        break;
      case ASM5:
        assertEquals(majorVersion > /* V8 = */ 52, isMoreRecent);
        break;
      case ASM6:
        assertEquals(majorVersion > /* V10 = */ 54, isMoreRecent);
        break;
      case ASM7:
        assertEquals(majorVersion > /* V11 = */ 55, isMoreRecent);
        break;
      default:
        fail("Unknown API value");
    }
  }

  /** Tests that we can get the byte array content of each precompiled class. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testGetBytes(final PrecompiledClass classParameter, final Api apiParameter) {
    assertEquals(Api.ASM7, apiParameter);
    assertEquals(0x01070000, apiParameter.value());
    assertEquals("ASM7", apiParameter.toString());
    byte[] classContent = classParameter.getBytes();
    assertThatClass(classContent).contains(classParameter.getInternalName());
    assertThatClass(classContent).isEqualTo(classContent);
  }

  /** Tests that we can load (and instantiate) each (non-abstract) precompiled class. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testLoadAndInstantiate(
      final PrecompiledClass classParameter, final Api apiParameter) {
    assertThat(() -> loadAndInstantiate(classParameter.getName(), classParameter.getBytes()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that {@link #loadAndInstantiate(String, byte[])} fails when trying to load a class which
   * is not well formed.
   */
  @ParameterizedTest
  @EnumSource(InvalidClass.class)
  public void testLoadAndInstantiate_invalidClass(final InvalidClass invalidClass) {
    byte[] classContent = invalidClass.getBytes();
    assertThrows(
        AssertionError.class, () -> loadAndInstantiate(invalidClass.toString(), classContent));
  }

  /**
   * Tests that {@link #doLoadAndInstantiate(String, byte[])} fails when trying to load an invalid
   * or unverifiable class.
   */
  @ParameterizedTest
  @EnumSource(InvalidClass.class)
  public void testDoLoadAndInstantiate_invalidClass(final InvalidClass invalidClass) {
    byte[] classContent = invalidClass.getBytes();
    switch (invalidClass) {
      case INVALID_ELEMENT_VALUE:
      case INVALID_TYPE_ANNOTATION_TARGET_TYPE:
      case INVALID_INSN_TYPE_ANNOTATION_TARGET_TYPE:
        break;
      case INVALID_BYTECODE_OFFSET:
      case INVALID_OPCODE:
      case INVALID_WIDE_OPCODE:
        assertThrows(
            VerifyError.class, () -> doLoadAndInstantiate(invalidClass.toString(), classContent));
        break;
      case INVALID_CLASS_VERSION:
      case INVALID_CONSTANT_POOL_INDEX:
      case INVALID_CONSTANT_POOL_REFERENCE:
      case INVALID_CP_INFO_TAG:
      case INVALID_STACK_MAP_FRAME_TYPE:
      case INVALID_VERIFICATION_TYPE_INFO:
        assertThrows(
            ClassFormatError.class,
            () -> doLoadAndInstantiate(invalidClass.toString(), classContent));
        break;
      default:
        fail("Unknown invalid class");
    }
  }
}
