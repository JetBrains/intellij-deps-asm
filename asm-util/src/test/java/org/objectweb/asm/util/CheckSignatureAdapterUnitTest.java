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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.signature.SignatureVisitor;

/**
 * CheckSignatureAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckSignatureAdapterUnitTest {

  private SignatureVisitor sv;

  @Test
  public void testNonJavaIdentifier() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    sv.visitSuperclass().visitClassType("Foo Bar");
  }

  @Test
  public void testIllegalFormalTypeParam() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitFormalTypeParameter("T"));
  }

  @Test
  public void testIllegalClassBound() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitClassBound());
  }

  @Test
  public void testIllegalInterfaceBound() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitInterfaceBound());
  }

  @Test
  public void testIllegalSuperclass() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitSuperclass());
  }

  @Test
  public void testIllegalInterface() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitInterface());
  }

  @Test
  public void testIllegalParameterType() {
    setup(CheckSignatureAdapter.CLASS_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitParameterType());
  }

  @Test
  public void testIllegalReturnType() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    sv.visitReturnType();
    assertThrows(Exception.class, () -> sv.visitReturnType());
  }

  @Test
  public void testIllegalExceptionType() {
    setup(CheckSignatureAdapter.METHOD_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitExceptionType());
  }

  @Test
  public void testIllegalBaseType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    sv.visitBaseType('I');
    assertThrows(Exception.class, () -> sv.visitBaseType('I'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitBaseType('V'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitBaseType('A'));
  }

  @Test
  public void testIllegalTypeVariable() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    sv.visitTypeVariable("T");
    assertThrows(Exception.class, () -> sv.visitTypeVariable("T"));
  }

  @Test
  public void testIllegalArrayType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    sv.visitArrayType();
    assertThrows(Exception.class, () -> sv.visitArrayType());
  }

  @Test
  public void testIllegalClassType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    sv.visitClassType("A");
    assertThrows(Exception.class, () -> sv.visitClassType("A"));
  }

  @Test
  public void testIllegalInnerClassType() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitInnerClassType("A"));
  }

  @Test
  public void testIllegalTypeArgument() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitTypeArgument());
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitTypeArgument('+'));
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    sv.visitClassType("A");
    assertThrows(Exception.class, () -> sv.visitTypeArgument('*'));
  }

  @Test
  public void testIllegalEnd() {
    setup(CheckSignatureAdapter.TYPE_SIGNATURE);
    assertThrows(Exception.class, () -> sv.visitEnd());
  }

  private void setup(int type) {
    sv = new CheckSignatureAdapter(type, null);
  }
}
