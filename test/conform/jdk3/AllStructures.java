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
package jdk3;

import java.io.Serializable;

/**
 * Class which, compiled with the JDK 1.3.0, produces all the class file structures from that
 * version. Must be compiled with "javac -g".
 */
abstract class AllStructures implements Runnable, Cloneable, Serializable {

  private static final String UTF8 = "\u0008\u0080\u0800\u8000";
  private static final long serialVersionUID = 123456L;

  public int f0;
  protected float f1;
  long f2;
  private double f3;
  static AllStructures f4;
  final byte f5 = 1;
  transient char f6;
  volatile short f7;
  boolean f8;

  static {
    f4 = null;
  }

  public int m0() {
    return f0;
  }

  protected float m1() {
    return f1;
  }

  long m2() {
    return f2;
  }

  private double m3() {
    return f3;
  }

  static AllStructures m4() {
    return f4;
  }

  final byte m5() {
    return f5;
  }

  strictfp char m6() {
    return f6;
  }

  short m7() {
    return f7;
  }

  abstract boolean m8();

  public static void main(String[] args) {}

  public void run() {}

  public synchronized Object clone() {
    return this;
  }

  private native void nativeMethod();

  private Runnable anonymousInnerClass() throws Exception {
    if (f0 > 0) {
      throw new Exception();
    }
    return new Runnable() {
      public void run() {
        new InnerClass(new InnerClass(f3 + m3()).f0);
      }
    };
  }

  private class InnerClass {
    private final double f0;

    private InnerClass(double f0) {
      this.f0 = f0;
    }
  }
}
