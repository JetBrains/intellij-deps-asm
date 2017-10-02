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
package org.objectweb.asm.util;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Collection;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.test.AsmTest;

/**
 * CheckSignatureAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckSignatureAdapterTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with ASM6. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM6);
  }

  /**
   * Tests that signatures are unchanged with a
   * SignatureReader->ChecksignatureAdapter->SignatureWriter transform.
   */
  @Test
  public void test() throws Exception {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    classReader.accept(
        new ClassVisitor(apiParameter.value()) {
          @Override
          public void visit(
              int version,
              int access,
              String name,
              String signature,
              String superName,
              String[] interfaces) {
            if (signature != null) {
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.accept(
                  new CheckSignatureAdapter(
                      CheckSignatureAdapter.CLASS_SIGNATURE, signatureWriter));
              assertThat(signature, equalTo(signatureWriter.toString()));
            }
          }

          @Override
          public FieldVisitor visitField(
              int access, String name, String desc, String signature, Object value) {
            if (signature != null) {
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.acceptType(
                  new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, signatureWriter));
              assertThat(signature, equalTo(signatureWriter.toString()));
            }
            return null;
          }

          @Override
          public MethodVisitor visitMethod(
              int access, String name, String desc, String signature, String[] exceptions) {
            if (signature != null) {
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.accept(
                  new CheckSignatureAdapter(
                      CheckSignatureAdapter.METHOD_SIGNATURE, signatureWriter));
              assertThat(signature, equalTo(signatureWriter.toString()));
            }
            return null;
          }
        },
        0);
  }
}
