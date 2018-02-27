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
package jdk8;

import java.io.UnsupportedEncodingException;

/**
 * Class which, compiled with the JDK 1.8.0, produces all the stack map frame types. Must be
 * compiled with "javac -g -parameters".
 */
public class AllFrames {

  Object o;
  String s;
  int f;

  public AllFrames(Object o, String s) {
    this.o = o;
    this.s = s;
  }

  // Frame types: full_frame.
  // Element types: null, uninitialized_this.
  public AllFrames(boolean b) {
    this(null, b ? "true" : "false");
  }

  // Frame types: full_frame.
  // Element types: null, uninitialized.
  public static AllFrames create(String s) {
    return new AllFrames(null, s == null ? "" : s);
  }

  // Frame types: same, same_locals_1_stack_item, full_frame.
  // Element types: primitive types and object.
  public int m0(
      boolean b,
      byte y,
      char c,
      short s,
      int i,
      float f,
      long l,
      double d,
      Object o,
      Object[] p,
      Object[][] q) {
    return b
        ? m0(!b, y, c, s, i - 1, f - 1f, l - 1l, d - 1d, o, p, q)
        : m0(!b, y, c, s, i + 1, f + 1f, l + 1l, d + 1d, o, p, q);
  }

  // Element types: uninitialized (multiple per frame).
  public String m0(byte[] bytes, boolean b) {
    try {
      return bytes == null ? null : new String(bytes, b ? "a" : "b");
    } catch (UnsupportedEncodingException e) {
      return null;
    }
  }

  // Frame types: append.
  // Element types: top.
  public void m1(int i, int j) {
    int k;
    int l = j;
    if (i < 0) {
      i = -i;
    }
  }

  // Frame types: chop.
  public long m2(int n, boolean b) {
    long total = 0;
    if (b) {
      int i = 0;
      do {
        total += i++;
      } while (i < n);
    } else {
      long i = 0;
      do {
        total += i++;
      } while (i < n);
    }
    return total;
  }

  // Frame types: same_frame_extended.
  public int m3(int i) {
    if (i < 0) {
      i = i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i;
      i = i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i;
    }
    return i;
  }

  // Frame types: same_locals_1_stack_item_frame_extended.
  public void m4(int i) {
    i = i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i;
    i = i + i + i + i + i + i + i + i + i + i + i + i + i + i + i + i;
    s = i == 0 ? "true" : "false";
  }

  // Frame merges: two non-array objects.
  public static Number m5(boolean b) {
    return b ? new Integer(1) : new Float(1);
  }

  // Frame merges: two single-dimensional arrays with object type elements.
  public static Number[] m6(boolean b) {
    return b ? new Integer[1] : new Float[1];
  }

  // Frame merges: two bi-dimensional arrays with object type elements.
  public static Number[][] m7(boolean b) {
    return b ? new Integer[1][1] : new Float[1][1];
  }

  // Frame merges: two bi-dimensional arrays with primitive type elements.
  public static Object[] m8(boolean b) {
    return b ? (Object[]) new byte[1][1] : (Object[]) new short[1][1];
  }

  // Frame merges: two single-dimensional arrays with mixed primitive / object type elements.
  public static Object m9(boolean b) {
    return b ? new byte[1] : new Float[1];
  }

  // Frame merges: two bi-dimensional arrays with mixed primitive / object type elements.
  public static Object[] m10(boolean b) {
    return b ? (Object[]) new byte[1][1] : (Object[]) new Float[1][];
  }

  // Frame merges: one and two dimensions arrays with identical element type.
  public static Object m11(boolean b) {
    return b ? new byte[1] : new byte[1][1];
  }

  // Frame merges: one and two dimensions arrays with mixed primitive / object type elements.
  public static Object[] m12(boolean b) {
    return b ? new Object[1] : new byte[1][1];
  }

  // Frame merges: two and three dimensions arrays with primitive type elements.
  public static Object[] m13(boolean b) {
    return b ? (Object[]) new byte[1][1] : (Object[]) new byte[1][1][1];
  }

  // Frame merges: three and four dimensions arrays with primitive type elements.
  public static Object[][] m14(boolean b) {
    return b ? (Object[][]) new byte[1][1][1] : (Object[][]) new byte[1][1][1][1];
  }

  // Frame merges: object type and single-dimensional array with primitive type elements.
  public static Object m15(boolean b) {
    return b ? new Integer(1) : new char[1];
  }

  // Frame merges: object type and single-dimensional array with object type elements.
  public static Object m16(boolean b) {
    return b ? new Integer(1) : new Float[1];
  }

  // Frame merges: two single-dimensional arrays with different primitive type elements.
  public Object m17(boolean b) {
    return b ? new int[0] : new boolean[0];
  }

  // Frame merges: two single-dimensional arrays with different primitive type elements.
  public Object m18(boolean b) {
    return b ? new short[0] : new float[0];
  }

  // Frame merges: two single-dimensional arrays with different primitive type elements.
  public Object m19(boolean b) {
    return b ? new double[0] : new long[0];
  }

  // Frame merges: null type and object type.
  public static Object m20(boolean b) {
    return b ? null : new Integer(1);
  }

  // Frame merges: object type and null type.
  public static Object m21(boolean b) {
    return b ? new Integer(1) : null;
  }

  // Frame AALOAD from null array (no frame in original class because ASM can't compute the exact
  // same frame as javac, but usefull for tests that compute frame types at each instruction).
  public static int m23() {
    Integer[] array = null;
    return array[0].intValue();
  }
}
