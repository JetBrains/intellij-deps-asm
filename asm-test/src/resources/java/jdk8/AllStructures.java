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

import annotations.ICA;
import annotations.IFA;
import annotations.IMA;
import annotations.IPA;
import annotations.ITA;
import annotations.ITPA;
import annotations.ITUA;
import annotations.IVA;
import annotations.VCA;
import annotations.VFA;
import annotations.VMA;
import annotations.VPA;
import annotations.VTA;
import annotations.VTPA;
import annotations.VTUA;
import annotations.VVA;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class which, compiled with the JDK 1.8.0, produces the new class file structures from that
 * version (compared to JDK 1.3.0). Must be compiled with "javac -g -parameters".
 */
@VTA(v = 0)
@ITA(v = 1)
public abstract class AllStructures<
        @VTPA(v = 2) @ITPA(v = 3) U0,
        @VTPA(v = 4) @ITPA(v = 5) U1 extends @VTUA(v = 6) @ITUA(v = 7) List<U0>,
        @VTPA(v = 8) @ITPA(v = 9)
        U2 extends @VTUA(v = 10) @ITUA(v = 11) Collection<@VTUA(v = 12) @ITUA(v = 13) U0>>
    extends @VTUA(v = 14) @ITUA(v = 15) HashMap<
        @VTUA(v = 16) @ITUA(v = 17) U0, @VTUA(v = 18) @ITUA(v = 19) U1>
    implements @VTUA(v = 20) @ITUA(v = 21) Callable<@VTUA(v = 22) @ITUA(v = 23) U0>,
        @VTUA(v = 24) @ITUA(v = 25) Future<@VTUA(v = 26) @ITUA(v = 27) U1> {

  @VFA(v = 28)
  @IFA(v = 29)
  public HashMap<@VTUA(v = 30) @ITUA(v = 31) U0, @VTUA(v = 32) @ITUA(v = 33) U1> f;

  @VCA(v = 34)
  @ICA(v = 35)
  public AllStructures() {}

  @VMA(v = 36)
  @IMA(v = 37)
  public <
          @VTPA(v = 38) @ITPA(v = 39) V0 extends @VTUA(v = 40) @ITUA(v = 41) U0,
          @VTUA(v = 42) @ITUA(v = 43) V1 extends @VTUA(v = 44) @ITUA(v = 45) U1>
      @VTUA(v = 46) @ITUA(v = 47)
      Map<@VTUA(v = 48) @ITUA(v = 49) ? extends V0, @VTUA(v = 50) @ITUA(v = 51) ? extends V1> m(
          @VPA(v = 52) @IPA(v = 53) V0 p0,
          @VPA(v = 54) @IPA(v = 55) V1 p1,
          @VPA(v = 56) @IPA(v = 57)
              Map<
                      @VTUA(v = 58) @ITUA(v = 59) ? extends V0, @VTUA(v = 60) @ITUA(v = 61)
                      ? extends V1>
                  p2)
          throws @VTUA(v = 62) @ITUA(v = 63) IllegalStateException, @VTUA(v = 64) @ITUA(v = 65)
              IllegalArgumentException {
    @VVA(v = 66)
    @IVA(v = 67)
    V1 l1 = p1;
    @VVA(v = 68)
    @IVA(v = 69)
    Map<@VTUA(v = 70) @ITUA(v = 71) ? extends V0, @VTUA(v = 72) @ITUA(v = 73) ? extends V1> l2 = p2;
    @VVA(v = 74)
    @IVA(v = 75)
    ArrayList l3 = (@VTUA(v = 76) @ITUA(v = 77) ArrayList) l1;
    try {
      m(p0, p1, p2);
    } catch (@VTUA(v = 78) @ITUA(v = 79)
        IllegalStateException
        | @VTUA(v = 80) @ITUA(v = 81) IllegalArgumentException e1) {
      // empty catch block
    }
    if (l2 instanceof @VTUA(v = 82) @ITUA(v = 83) HashMap) {
      return l2;
    }
    AllStructures.<@VTUA(v = 84) V0, @ITUA(v = 85) V1>m();
    return new @VTUA(v = 86) @ITUA(v = 87) HashMap<@VTUA(v = 88) V0, @ITUA(v = 89) V1>();
  }
  
  private static <U, V> void m() {}

  private double g;

  private double n() {
    return g;
  }

  private Runnable anonymousInnerClass() throws Exception {
    return new Runnable() {
      public void run() {
        @VTUA(v = 0)
        @ITUA(v = 1)
        double f = new InnerClass(g + n()).f;
        new InnerClass(f);
      }
    };
  }

  private class InnerClass {

    @VTUA(v = 0)
    @ITUA(v = 1)
    private final double f;

    private InnerClass(double f) {
      this.f = f;
    }
  }

  private class 𝔻 {}
}
