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

/**
 * Class which, compiled with the JDK 1.8.0, produces a large method with stack map frames. Must be
 * compiled with "javac -g -parameters".
 */
public class LargeMethod {

  int f;

  public LargeMethod(Object o, String s) {}

  // Frames in methods larger than 32K.
  public LargeMethod largeMethod(
      boolean v0,
      byte v1,
      char v2,
      short v3,
      int v4,
      long v5,
      float v6,
      double v7,
      Object v8,
      boolean[] v9,
      byte[] v10,
      char[] v11,
      short[] v12,
      int[] v13,
      long[] v14,
      float[] v15,
      double[] v16,
      Object[] v17) {
    try {
      while (v4++ < v5) {
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
        f =
            f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f + f
                + f + f + f + f + f + f + f + f;
      }
    } catch (Throwable t) {
      return null;
    } finally {
      v17[0] = null;
    }
    return new LargeMethod(v17[f % v17.length], null);
  }
}
