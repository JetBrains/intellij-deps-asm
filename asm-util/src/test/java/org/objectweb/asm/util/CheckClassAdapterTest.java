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

import static org.junit.Assert.assertEquals;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.test.AsmTest;

/**
 * CheckClassAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckClassAdapterTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  /**
   * Tests that classes are unchanged with a ClassReader->CheckClassAdapter->ClassWriter transform.
   */
  @Test
  public void testCheckClassAdapter_classUnchanged() {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(
        new CheckClassAdapter(apiParameter.value(), classWriter, false), attributes(), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /** Tests that {@link CheckClassAdapter.verify()} succeeds on all precompiled classes. */
  @Test
  public void testCheckClassAdapter_verify() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    StringWriter stringWriter = new StringWriter();
    PrintWriter printWriter = new PrintWriter(stringWriter);
    CheckClassAdapter.verify(classReader, /* dump = */ false, printWriter);
    printWriter.close();
    assertEquals("", stringWriter.toString());
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }
}
