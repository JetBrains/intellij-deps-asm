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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * SignatureReader tests.
 *
 * @author Eric Bruneton
 */
class SignatureReaderTest extends AsmTest {

  @ParameterizedTest
  @MethodSource({
    "org.objectweb.asm.signature.SignaturesProviders#classSignatures",
    "org.objectweb.asm.signature.SignaturesProviders#methodSignatures"
  })
  void testAccept_validClassOrMethodSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureVisitor signatureVisitor =
        new SignatureVisitor(/* latest */ Opcodes.ASM10_EXPERIMENTAL) {};

    Executable acceptVisitor = () -> signatureReader.accept(signatureVisitor);

    assertDoesNotThrow(acceptVisitor);
  }

  @ParameterizedTest
  @MethodSource("org.objectweb.asm.signature.SignaturesProviders#fieldSignatures")
  void testAccept_validFieldSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureVisitor signatureVisitor =
        new SignatureVisitor(/* latest */ Opcodes.ASM10_EXPERIMENTAL) {};

    Executable acceptVisitor = () -> signatureReader.acceptType(signatureVisitor);

    assertDoesNotThrow(acceptVisitor);
  }

  @Test
  void testAccept_invalidSignature() {
    String invalidSignature = "-";
    SignatureReader signatureReader = new SignatureReader(invalidSignature);
    SignatureVisitor signatureVisitor =
        new SignatureVisitor(/* latest */ Opcodes.ASM10_EXPERIMENTAL) {};

    Executable acceptVisitor = () -> signatureReader.accept(signatureVisitor);

    assertThrows(IllegalArgumentException.class, acceptVisitor);
  }
}
