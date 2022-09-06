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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.IntStream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.test.AsmTest;

/**
 * SignatureWriter tests.
 *
 * @author Eric Bruneton
 */
class SignatureWriterTest extends AsmTest {

  @ParameterizedTest
  @MethodSource({
    "org.objectweb.asm.signature.SignaturesProviders#classSignatures",
    "org.objectweb.asm.signature.SignaturesProviders#methodSignatures"
  })
  void testReadAndWrite_classOrMethodSignature(final String signature) {
    SignatureWriter signatureWriter = new SignatureWriter();

    new SignatureReader(signature).accept(signatureWriter);

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource("org.objectweb.asm.signature.SignaturesProviders#fieldSignatures")
  void testReadAndWrite_fieldSignature(final String signature) {
    SignatureWriter signatureWriter = new SignatureWriter();

    new SignatureReader(signature).acceptType(signatureWriter);

    assertEquals(signature, signatureWriter.toString());
  }

  static IntStream deepSignatures() {
    return IntStream.range(0, 48);
  }

  @ParameterizedTest
  @MethodSource("deepSignatures")
  void testWrite_deepSignature(int depth) {
    SignatureWriter signatureWriter = new SignatureWriter();
    String expected = writeDeepSignature(signatureWriter, depth);
    assertEquals(expected, signatureWriter.toString(), "depth=" + depth);
  }

  private String writeDeepSignature(SignatureVisitor signatureVisitor, int maxDepth) {
    StringBuilder expected = new StringBuilder();
    writeDeepSignatureInner(signatureVisitor, expected, 0, maxDepth);
    return expected.toString();
  }

  private void writeDeepSignatureInner(SignatureVisitor signatureVisitor, StringBuilder expected, int currentDepth, int maxDepth) {
    signatureVisitor.visitClassType(TEST_GENERIC);
    expected.append('L' + TEST_GENERIC);
    if (currentDepth < maxDepth) {
      expected.append("<L" + TEST_OPEN + ';');
      SignatureVisitor v = signatureVisitor.visitTypeArgument('=');
      v.visitClassType(TEST_OPEN);
      v.visitEnd();

      writeDeepSignatureInner(signatureVisitor.visitTypeArgument('='), expected, currentDepth + 1, maxDepth);

      v = signatureVisitor.visitTypeArgument('=');
      v.visitClassType(TEST_CLOSE);
      v.visitEnd();
      expected.append('L' + TEST_CLOSE + ";>");
    }
    signatureVisitor.visitEnd();
    expected.append(';');
  }

  private static final String TEST_OPEN = "TestOpen";
  private static final String TEST_GENERIC = "TestGeneric";
  private static final String TEST_CLOSE = "TestClose";
}
