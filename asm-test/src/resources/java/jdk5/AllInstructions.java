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
package jdk5;

/**
 * Class which, compiled with the JDK 1.5.0, produces the new JVM instructions from that version
 * (compared to JDK 1.3.0). Also contain all the other instructions that can be used in JDK 1.5
 * classes (which excludes jsr and ret), so that ASM classes which don't support jsr or ret (and
 * therefore can't be tested with jdk3.AllInstructions) can still be tested on all instructions.
 * Must be compiled with "javac -g".
 */
class AllInstructions {
  private Class c;
  private Class d;
  private int f;
  private long g;
  private AllInstructions field;
  private static AllInstructions staticField;

  AllInstructions() {}

  AllInstructions(int v0, float v1, long v2, double v3, Object v4) {}

  // New instruction in JDK 1.5.
  public void ldcWithClassConstant() {
    c = AllInstructions.class;
    d = AllInstructions[].class;
  }

  public static int intInstructions(
      int v0, int v1, int v2, int v3, int v4, int v5, int v6, int v7, int v8) {
    boolean b0 = v0 < -1;
    boolean b1 = v1 > 1;
    boolean b2 = v2 <= 2;
    boolean b3 = v3 >= 3;
    boolean b4 = v4 == 4;
    boolean b5 = v5 != 5;
    v0 = b0 ? (v6 + 5) : (v6 - 5);
    v1 = b1 ? (v7 * 100) : (v7 / 100);
    v2 = b2 ? (v8 % 10000) : (~v8);
    v3 = b3 ? (v0 & 1000000) : (v0 | 1000000);
    v4 = b4 ? (v1 ^ v2) : (v1 << v2);
    v5 = b5 ? (v2 >> v3) : (v2 >>> v3);
    v6 += 1;
    v7 = v6 < 0 ? -v6 : v6;
    v1 = v0 < 0 ? v1 : v2;
    v2 = v1 > 0 ? v2 : v3;
    v3 = v2 <= 0 ? v3 : v4;
    v4 = v3 >= 0 ? v4 : v5;
    v5 = v4 == 0 ? v5 : v6;
    v6 = v5 != 0 ? v6 : v7;
    return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
  }

  public static long longInstructions(
      long v0, long v1, long v2, long v3, long v4, long v5, long v6, long v7, long v8) {
    boolean b0 = v0 < -1L;
    boolean b1 = v1 > 1L;
    boolean b2 = v2 <= 2L;
    boolean b3 = v3 >= 3L;
    boolean b4 = v4 == 4L;
    boolean b5 = v5 != 5L;
    v0 = b0 ? (v6 + 5L) : (v6 - 5L);
    v1 = b1 ? (v7 * 100L) : (v7 / 100L);
    v2 = b2 ? (v8 % 10000L) : (~v8);
    v3 = b3 ? (v0 & 1000000L) : (v0 | 1000000L);
    v4 = b4 ? (v1 ^ v2) : (v1 << v2);
    v5 = b5 ? (v2 >> v3) : (v2 >>> v3);
    v6 += 1L;
    v7 = v6 < 0L ? -v6 : v6;
    return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
  }

  public static float floatInstructions(
      float v0, float v1, float v2, float v3, float v4, float v5, float v6, float v7, float v8) {
    boolean b0 = v0 < -1f;
    boolean b1 = v1 > 1f;
    boolean b2 = v2 <= 2f;
    boolean b3 = v3 >= 3f;
    boolean b4 = v4 == 4f;
    boolean b5 = v5 != 5f;
    v0 = b0 ? (v6 + 5f) : (v6 - 5f);
    v1 = b1 ? (v7 * 100f) : (v7 / 100f);
    v2 = b2 ? (v8 % 10000f) : v8;
    v3 = b3 ? -v3 : v3;
    v4 = b4 ? -v4 : v4;
    v5 = b5 ? -v5 : v5;
    v6 += 1f;
    v7 = v6 < 0f ? -v6 : v6;
    v8 = v7;
    return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
  }

  public static double doubleInstructions(
      double v0,
      double v1,
      double v2,
      double v3,
      double v4,
      double v5,
      double v6,
      double v7,
      double v8) {
    boolean b0 = v0 < -1d;
    boolean b1 = v1 > 1d;
    boolean b2 = v2 <= 2d;
    boolean b3 = v3 >= 3d;
    boolean b4 = v4 == 4d;
    boolean b5 = v5 != 5d;
    v0 = b0 ? (v6 + 5d) : (v6 - 5d);
    v1 = b1 ? (v7 * 100d) : (v7 / 100d);
    v2 = b2 ? (v8 % 10000d) : v8;
    v3 = b3 ? -v3 : v3;
    v4 = b4 ? -v4 : v4;
    v5 = b5 ? -v5 : v5;
    v6 += 1d;
    v7 = v6 < 0d ? -v6 : v6;
    return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
  }

  public static double castInstructions(int v0, long v1, long v2) {
    byte v3 = (byte) v0;
    char v4 = (char) v1;
    short v5 = (short) v2;
    long v6 = (long) v3;
    float v7 = (float) v4;
    double v8 = (double) v5;
    v1 = v6;
    v2 = v1;
    v6 = (long) v8;
    return v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8;
  }

  public static float castInstructions(float v0, double v1, double v2) {
    byte v3 = (byte) v0;
    char v4 = (char) v1;
    short v5 = (short) v2;
    long v6 = (long) v3;
    float v7 = (float) v4;
    double v8 = (double) v5;
    v1 = v6;
    v2 = v1;
    v6 = (long) v7;
    return (float) (v0 + v1 + v2 + v3 + v4 + v5 + v6 + v7 + v8);
  }

  public static Object objectInstructions(Object v0, Object v1, Object v2, Object v3, Object v4) {
    boolean b0 = v0 == v1;
    boolean b1 = v1 != v2;
    boolean b2 = v2 == null;
    boolean b3 = v3 != null;
    boolean b4 = v4 instanceof String;
    v0 = b0 ? null : v0;
    v1 = b1 ? v1 : v0;
    v2 = b2 ? v2 : v1;
    v3 = b3 ? v3 : v2;
    v4 = b4 ? new Integer(((String) v4).length()) : v3;
    return v4;
  }

  public static Object[] arrayInstructions(
      byte[] v0, char[] v1, short[] v2, int[] v3, long[] v4, float[] v5, double[] v6, Object[] v7) {
    v0[1] = v0[0];
    v1[1] = v1[0];
    v2[1] = v2[0];
    v3[1] = v3[0];
    v4[1] = v4[0];
    v5[1] = v5[0];
    v6[1] = v6[0];
    v7[1] = v7[0];
    Object[] v8 = new Object[v7.length];
    v8[0] = new int[4][8][16];
    return v8;
  }

  public void fieldInstructions() {
    AllInstructions c = field;
    field = staticField;
    staticField = c;
  }

  public void methodInstructions(Runnable v0) {
    AllInstructions c = new AllInstructions();
    c.fieldInstructions();
    monitorInstructions(c);
    v0.run();
  }

  public static int lookupSwitchInstruction(int v0) {
    switch (v0) {
      case 1000:
        return 1;
      case 10000:
        return 2;
      case 100000:
        return 3;
      default:
        return -1;
    }
  }

  public static int tableSwitchInstruction(int v0) {
    switch (v0) {
      case 0:
        return 1;
      case 1:
        return 2;
      case 2:
        return 3;
      default:
        return -1;
    }
  }

  public static String monitorInstructions(Object v0) {
    synchronized (v0) {
      return v0.toString();
    }
  }

  public int dupX1Instruction() {
    return f++;
  }

  public long dup2X1Instruction() {
    return g++;
  }

  public void dupX2Instruction(int[] v0, int[] v1) {
    v0[0] = v1[0] = 0;
  }

  public void dup2X2Instruction(long[] v0, long[] v1) {
    v0[0] = v1[0] = 0;
  }

  public void popInstructions() {
    dupX1Instruction();
    dup2X1Instruction();
  }

  // With JDK1.5, this code no longer produces jsr or ret instructions.
  public int jsrAndRetInstructions(int v0) throws Exception {
    int u0 = v0 + 1;
    try {
      u0 = jsrAndRetInstructions(u0);
    } catch (Throwable t) {
      return -1;
    } finally {
      u0++;
    }
    return u0;
  }

  public Object readNullArray() {
    Object[] array = null;
    try {
      return array[0];
    } catch (NullPointerException e) {
      return null;
    }
  }
}
