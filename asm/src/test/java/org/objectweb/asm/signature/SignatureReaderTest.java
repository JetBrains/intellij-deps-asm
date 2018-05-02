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
package org.objectweb.asm.signature;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * SignatureReader tests.
 *
 * @author Eric Bruneton
 */
public class SignatureReaderTest extends AsmTest {

  /**
   * Tests that class, field and method signatures can be read successfully with a SignatureReader.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testReadSignature(final PrecompiledClass classParameter, final Api apiParameter)
      throws Exception {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
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
            if (signature != null) {
              new SignatureReader(signature).accept(new SignatureVisitor(api) {});
            }
          }

          @Override
          public FieldVisitor visitField(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final Object value) {
            if (signature != null) {
              new SignatureReader(signature).acceptType(new SignatureVisitor(api) {});
            }
            return null;
          }

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            if (signature != null) {
              new SignatureReader(signature).accept(new SignatureVisitor(api) {});
            }
            return null;
          }
        },
        0);
  }

  @Test
  public void testReadInvalidSignature() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SignatureReader("-").accept(new SignatureVisitor(Opcodes.ASM7_EXPERIMENTAL) {}));
  }
}
