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
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit tests for {@link AsmTest}.
 *
 * @author Eric Bruneton
 */
public class AsmTestTest extends AsmTest {

  /** Tests that we can get the byte array content of each precompiled class. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testGetBytes(PrecompiledClass classParameter, Api apiParameter) {
    assertEquals(Api.ASM6, apiParameter);
    assertEquals("ASM6", apiParameter.toString());
    assertThatClass(classParameter.getBytes()).contains(classParameter.getInternalName());
  }

  /** Tests that we can load (and instantiate) each (non-abstract) precompiled class. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testLoadAndInstantiate(PrecompiledClass classParameter, Api apiParameter) {
    assertThat(() -> loadAndInstantiate(classParameter.getName(), classParameter.getBytes()))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that {@link #loadAndInstantiate(String, byte[])} fails when trying to load a class which
   * is not well formed.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testLoadAndInstantiate_invalidClass(
      PrecompiledClass classParameter, Api apiParameter) {
    byte[] classContent = classParameter.getBytes();
    switch (classParameter) {
      case DEFAULT_PACKAGE:
      case JDK3_ATTRIBUTE:
      case JDK5_ANNOTATION:
      case JDK9_MODULE:
        return;
      case JDK3_ALL_INSTRUCTIONS:
      case JDK3_ALL_STRUCTURES:
      case JDK3_ANONYMOUS_INNER_CLASS:
      case JDK3_INNER_CLASS:
      case JDK3_LARGE_METHOD:
      case JDK3_STACK_MAP_ATTRIBUTE:
      case JDK5_ALL_INSTRUCTIONS:
      case JDK5_ALL_STRUCTURES:
      case JDK5_ENUM:
      case JDK5_LOCAL_CLASS:
      case JDK8_ALL_STRUCTURES:
      case JDK8_ANONYMOUS_INNER_CLASS:
      case JDK8_INNER_CLASS:
      case JDK8_ALL_FRAMES:
      case JDK8_ALL_INSTRUCTIONS:
      case JDK8_LARGE_METHOD:
        removeAttributes(classContent, "Code");
        assertThrows(
            AssertionError.class, () -> loadAndInstantiate(classParameter.getName(), classContent));
        break;
      default:
        fail("Unknown precompiled class");
    }
  }

  /**
   * Tests that {@link #doLoadAndInstantiate(String, byte[])} fails when trying to load an invalid
   * or unverifiable class.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testDoLoadAndInstantiate_invalidClass(
      PrecompiledClass classParameter, Api apiParameter) {
    if (classParameter.isMoreRecentThanCurrentJdk()) {
      return;
    }
    byte[] classContent = classParameter.getBytes();
    switch (classParameter) {
      case DEFAULT_PACKAGE:
      case JDK3_ATTRIBUTE:
      case JDK5_ANNOTATION:
      case JDK9_MODULE:
        doLoadAndInstantiate(classParameter.getName(), classContent);
        break;
      case JDK3_ALL_INSTRUCTIONS:
      case JDK3_ALL_STRUCTURES:
      case JDK3_ANONYMOUS_INNER_CLASS:
      case JDK3_INNER_CLASS:
      case JDK3_LARGE_METHOD:
      case JDK3_STACK_MAP_ATTRIBUTE:
      case JDK5_ALL_INSTRUCTIONS:
      case JDK5_ALL_STRUCTURES:
      case JDK5_ENUM:
      case JDK5_LOCAL_CLASS:
      case JDK8_ALL_STRUCTURES:
      case JDK8_ANONYMOUS_INNER_CLASS:
      case JDK8_INNER_CLASS:
        removeAttributes(classContent, "Code");
        assertThrows(
            ClassFormatError.class,
            () -> doLoadAndInstantiate(classParameter.getName(), classContent));
        break;
      case JDK8_ALL_FRAMES:
      case JDK8_ALL_INSTRUCTIONS:
      case JDK8_LARGE_METHOD:
        removeAttributes(classContent, "StackMapTable");
        assertThrows(
            VerifyError.class, () -> doLoadAndInstantiate(classParameter.getName(), classContent));
        break;
      default:
        fail("Unknown precompiled class");
    }
  }

  /**
   * "Removes" all the attributes of the given type in a class by altering its name in the constant
   * pool of the class, to make it unrecognizable. Fails if there is not exactly one occurrence of
   * attributeName in classContent.
   */
  private static void removeAttributes(byte[] classContent, String attributeName) {
    int occurrenceCount = 0;
    for (int i = 0; i < classContent.length - attributeName.length(); ++i) {
      boolean occurrenceFound = true;
      for (int j = 0; j < attributeName.length(); ++j) {
        if (classContent[i + j] != attributeName.charAt(j)) {
          occurrenceFound = false;
          break;
        }
      }
      if (occurrenceFound) {
        classContent[i] += 1;
        occurrenceCount += 1;
      }
    }
    assertEquals(1, occurrenceCount);
  }
}
