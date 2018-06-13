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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * ByteVector tests.
 *
 * @author Eric Bruneton
 */
public class ByteVectorTest {

  @Test
  public void testPutByte() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putByte(1);
    assertContains(byteVector, 1);
  }

  @Test
  public void testPut11() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.put11(1, 2);
    assertContains(byteVector, 1, 2);
  }

  @Test
  public void testPutShort() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putShort(0x0102);
    assertContains(byteVector, 1, 2);
  }

  @Test
  public void testPut12() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.put12(1, 0x0203);
    assertContains(byteVector, 1, 2, 3);
  }

  @Test
  public void testPut112() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.put112(1, 2, 0x0304);
    assertContains(byteVector, 1, 2, 3, 4);
  }

  @Test
  public void testPutInt() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putInt(0x01020304);
    assertContains(byteVector, 1, 2, 3, 4);
  }

  @Test
  public void testPut122() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.put122(1, 0x0203, 0x0405);
    assertContains(byteVector, 1, 2, 3, 4, 5);
  }

  @Test
  public void testPutLong() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putLong(0x0102030405060708L);
    assertContains(byteVector, 1, 2, 3, 4, 5, 6, 7, 8);
  }

  @Test
  public void testPutUTF8_ascii() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putUTF8("abc");
    assertContains(byteVector, 0, 3, 'a', 'b', 'c');

    char[] charBuffer = new char[65536];
    assertThrows(IllegalArgumentException.class, () -> byteVector.putUTF8(new String(charBuffer)));
  }

  @Test
  public void testPutUTF8_unicode() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putUTF8("a\u0000\u0080\u0800");
    assertContains(byteVector, 0, 8, 'a', -64, -128, -62, -128, -32, -96, -128);

    char[] charBuffer = new char[32768];
    Arrays.fill(charBuffer, '\u07FF');
    IllegalArgumentException thrown =
        assertThrows(
            IllegalArgumentException.class, () -> byteVector.putUTF8(new String(charBuffer)));
    assertEquals("UTF8 string too large", thrown.getMessage());
  }

  @ParameterizedTest
  @ValueSource(ints = {65535, 65536})
  public void testPutUTF8_tooLong(int size) {
    ByteVector byteVector = new ByteVector(0);
    char[] charBuffer = new char[size];
    Arrays.fill(charBuffer, 'A');
    String utf8 = new String(charBuffer);
    if (size > 65535) {
      IllegalArgumentException thrown =
          assertThrows(IllegalArgumentException.class, () -> byteVector.putUTF8(utf8));
      assertEquals("UTF8 string too large", thrown.getMessage());
    } else {
      byteVector.putUTF8(utf8);
    }
  }

  @Test
  public void testPutByteArray() {
    ByteVector byteVector = new ByteVector(0);
    byteVector.putByteArray(new byte[] {0, 1, 2, 3, 4, 5}, 1, 3);
    assertContains(byteVector, 1, 2, 3);
  }

  private void assertContains(final ByteVector byteVector, final int... values) {
    assertTrue(byteVector.data.length >= values.length);
    assertEquals(values.length, byteVector.length);
    for (int i = 0; i < values.length; ++i) {
      assertEquals(values[i], byteVector.data[i]);
    }
  }
}
