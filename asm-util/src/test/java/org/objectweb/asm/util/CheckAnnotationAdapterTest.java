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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.test.AsmTest;

/**
 * CheckAnnotationAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckAnnotationAdapterTest extends AsmTest implements Opcodes {

  private CheckAnnotationAdapter checkAnnotationAdapter = new CheckAnnotationAdapter(null);

  @Test
  public void testIllegalAnnotationName() {
    assertThrows(Exception.class, () -> checkAnnotationAdapter.visit(null, new Integer(0)));
  }

  @Test
  public void testIllegalAnnotationValue() {
    assertThrows(Exception.class, () -> checkAnnotationAdapter.visit("name", new Object()));
    assertThrows(
        Exception.class, () -> checkAnnotationAdapter.visit("name", Type.getMethodType("()V")));
  }

  @Test
  public void testIllegalAnnotationEnumValue() {
    assertThrows(
        Exception.class, () -> checkAnnotationAdapter.visitEnum("name", "Lpkg/Enum;", null));
  }

  @Test
  public void testIllegalAnnotationValueAfterEnd() {
    checkAnnotationAdapter.visitEnd();
    assertThrows(Exception.class, () -> checkAnnotationAdapter.visit("name", new Integer(0)));
  }
}
