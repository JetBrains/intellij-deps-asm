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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Opcodes;

/**
 * Textifier tests.
 *
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public class TextifierTest {

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new Textifier() {});
  }

  @Test
  public void testBackwardCompatibility() {
    Textifier textifier = new Textifier();
    textifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V");
    assertEquals("    INVOKESPECIAL owner.name ()V\n", textifier.getText().get(0));
  }

  @Test
  public void testBackwardCompatibilityAsm4() {
    Textifier textifier = new Textifier(Opcodes.ASM4) {};
    textifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V");
    textifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", false);
    String expectedText = "    INVOKESPECIAL owner.name ()V\n";
    assertEquals(expectedText, textifier.getText().get(0));
    assertEquals(expectedText, textifier.getText().get(1));
  }

  @Test
  public void testMain() throws Exception {
    PrintStream err = System.err;
    PrintStream out = System.out;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    try {
      String thisClassName = getClass().getName();
      String thisClassFilePath =
          ClassLoader.getSystemResource(thisClassName.replace('.', '/') + ".class").getPath();
      Textifier.main(new String[0]);
      Textifier.main(new String[] {"-debug"});
      Textifier.main(new String[] {thisClassName});
      Textifier.main(new String[] {thisClassFilePath});
      Textifier.main(new String[] {"-debug", thisClassName});
      Textifier.main(new String[] {"-debug", "java.util.function.Predicate"});
      Textifier.main(new String[] {"java.lang.Object"});
      Textifier.main(new String[] {"-debug", thisClassName, "extraArgument"});
      assertThrows(IOException.class, () -> Textifier.main(new String[] {"DoNotExist.class"}));
      assertThrows(IOException.class, () -> Textifier.main(new String[] {"do\\not\\exist"}));
    } finally {
      System.setErr(err);
      System.setOut(out);
    }
  }
}
