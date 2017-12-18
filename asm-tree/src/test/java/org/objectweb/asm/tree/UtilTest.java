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
package org.objectweb.asm.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Util tests.
 *
 * @author Eric Bruneton
 */
public class UtilTest {

  @Test
  public void testNullAsArrayList() {
    assertTrue(Util.asArrayList((Object[]) null).isEmpty());
    assertTrue(Util.asArrayList((byte[]) null).isEmpty());
    assertTrue(Util.asArrayList((boolean[]) null).isEmpty());
    assertTrue(Util.asArrayList((short[]) null).isEmpty());
    assertTrue(Util.asArrayList((char[]) null).isEmpty());
    assertTrue(Util.asArrayList((int[]) null).isEmpty());
    assertTrue(Util.asArrayList((float[]) null).isEmpty());
    assertTrue(Util.asArrayList((long[]) null).isEmpty());
    assertTrue(Util.asArrayList((double[]) null).isEmpty());
  }

  @Test
  public void testAsArrayListWithLength() {
    assertEquals(3, Util.asArrayList(3).size());
    assertEquals(3, Util.asArrayList(3, new Object[] {1, 2, 3, 4, 5}).size());
  }
}
