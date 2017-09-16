/***
 * ASM tests
 * Copyright (c) 2000-2011 INRIA, France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package jdk8;

/**
 * Class which, compiled with the JDK 1.5.0, produces all the stack map frame
 * types. Must be compiled with "javac -g -parameters".
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
    public int m0(boolean b, byte y, char c, short s, int i, float f, long l,
            double d, Object o) {
        return b ? m0(!b, y, c, s, i - 1, f - 1f, l - 1l, d - 1d, o)
                : m0(!b, y, c, s, i + 1, f + 1f, l + 1l, d + 1d, o);
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

    // Frames in methods larger than 32K.
    public AllFrames largeMethod(boolean v0, byte v1, char v2, short v3, int v4,
            long v5, float v6, double v7, Object v8, boolean[] v9, byte[] v10,
            char[] v11, short[] v12, int[] v13, long[] v14, float[] v15,
            double[] v16, Object[] v17) {
        try {
            while (v4++ < v5) {
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
                f = f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                        + f;
            }
        } catch (Throwable t) {
            return null;
        } finally {
            v17[0] = null;
        }
        return new AllFrames(v17[f % v17.length], null);
    }
}
