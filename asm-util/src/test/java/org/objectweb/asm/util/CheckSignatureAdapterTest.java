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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

  private CheckSignatureAdapter checkSignatureAdapter;

  private void setup(final int type) {
    checkSignatureAdapter = new CheckSignatureAdapter(type, null);
  }

  @Test
  public void testNonJavaIdentifier() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    checkSignatureAdapter.visitSuperclass().visitClassType("Foo Bar");
  }

  @Test
  public void testIllegalFormalTypeParam() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    checkSignatureAdapter.visitSuperclass();
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitFormalTypeParameter("T"));

    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitFormalTypeParameter("T"));
  }

  @Test
  public void testIllegalClassBound() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassBound());

    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassBound());
  }

  @Test
  public void testIllegalInterfaceBound() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitInterfaceBound());

    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitInterfaceBound());
  }

  @Test
  public void testIllegalSuperclass() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    checkSignatureAdapter.visitSuperclass();
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitSuperclass());

    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitSuperclass());
  }

  @Test
  public void testIllegalInterface() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitInterface());

    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitInterface());
  }

  @Test
  public void testIllegalParameterType() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    checkSignatureAdapter.visitReturnType();
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitParameterType());

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitParameterType());
  }

  @Test
  public void testIllegalReturnType() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    checkSignatureAdapter.visitReturnType();
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitReturnType());

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitReturnType());
  }

  @Test
  public void testIllegalExceptionType() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitExceptionType());

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitExceptionType());
  }

  @Test
  public void testIllegalBaseType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    checkSignatureAdapter.visitBaseType('I');
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitBaseType('I'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitBaseType('V'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitBaseType('A'));

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitBaseType('I'));
  }

  @Test
  public void testIllegalTypeVariable() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeVariable(null));
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeVariable(""));
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeVariable("LT;"));
    checkSignatureAdapter.visitTypeVariable("T");
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeVariable("T"));

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeVariable("T"));
  }

  @Test
  public void testIllegalArrayType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    checkSignatureAdapter.visitArrayType();
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitArrayType());

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitArrayType());
  }

  @Test
  public void testIllegalClassType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType(null));
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType(""));
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType("<A>"));
    checkSignatureAdapter.visitClassType("A");
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType("A"));

    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType("A"));
  }

  @Test
  public void testIllegalInnerClassType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitInnerClassType("A"));
  }

  @Test
  public void testIllegalTypeArgument() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeArgument());
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeArgument('+'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    checkSignatureAdapter.visitClassType("A");
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitTypeArgument('*'));
  }

  @Test
  public void testIllegalEnd() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitEnd());
  }

  /**
   * Tests that signatures are unchanged with a
   * SignatureReader->CheckSignatureAdapter->SignatureWriter transform.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void test(final PrecompiledClass classParameter, final Api apiParameter) throws Exception {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
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
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.accept(
                  new CheckSignatureAdapter(
                      CheckSignatureAdapter.CLASS_SIGNATURE, signatureWriter));
              signatureReader.accept(
                  new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null));
              assertEquals(signature, signatureWriter.toString());
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
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.acceptType(
                  new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, signatureWriter));
              signatureReader.acceptType(
                  new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null));
              assertEquals(signature, signatureWriter.toString());
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
              SignatureReader signatureReader = new SignatureReader(signature);
              SignatureWriter signatureWriter = new SignatureWriter();
              signatureReader.accept(
                  new CheckSignatureAdapter(
                      CheckSignatureAdapter.METHOD_SIGNATURE, signatureWriter));
              signatureReader.accept(
                  new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null));
              assertEquals(signature, signatureWriter.toString());
            }
            return null;
          }
        },
        0);
  }
}
