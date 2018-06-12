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

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import java.util.List;

/**
 * Class which, compiled with the JDK 1.5.0, produces the new class file structures from that
 * version (compared to JDK 1.3.0). Must be compiled with "javac -g".
 */
@Deprecated
@AllStructures.InvisibleAnnotation(
  byteValue = 0,
  charValue = 0,
  booleanValue = false,
  intValue = 0,
  shortValue = 0,
  longValue = 0L,
  floatValue = 0f,
  doubleValue = 0d,
  stringValue = "0",
  classValue = AllStructures.class,
  enumValue = AllStructures.EnumClass.VALUE0,
  annotationValue = @Deprecated,
  byteArrayValue = {0},
  charArrayValue = {'0'},
  booleanArrayValue = {false},
  intArrayValue = {0},
  shortArrayValue = {0},
  longArrayValue = {0L},
  floatArrayValue = {0f},
  doubleArrayValue = {0d},
  stringArrayValue = {"0"},
  classArrayValue = {AllStructures.class, int.class, int[].class},
  enumArrayValue = {AllStructures.EnumClass.VALUE0},
  annotationArrayValue = {@Deprecated},
  otherArrayValue = {}
)
class AllStructures<
        U0,
        U1 extends Number,
        U2 extends List<String>,
        U3 extends List<?>,
        U4 extends List<? extends Number>,
        U5 extends List<? super Number>,
        U6 extends Number & Runnable & Cloneable>
    implements Comparator<Integer> {

  @Deprecated
  @InvisibleAnnotation(otherArrayValue = {2})
  public int f;

  private U0 f0;
  private U1 f1;
  private U2 f2;
  private U3 f3;
  private U4 f4;
  private U5 f5;
  private U6 f6;

  @Deprecated
  @InvisibleAnnotation(otherArrayValue = {3})
  public int m() {
    return f;
  }

  public int n(
      int p0,
      @Deprecated @InvisibleAnnotation(otherArrayValue = {4}) float p1,
      @Deprecated float p2,
      long p3,
      @InvisibleAnnotation(otherArrayValue = {5}) double p4) {
    return f;
  }

  public U0 o() {
    return f0;
  }

  <
          U0,
          U1 extends Number,
          U2 extends List<String>,
          U3 extends List<?>,
          U4 extends List<? extends Number>,
          U5 extends List<? super Number>,
          U6 extends Number & Runnable & Cloneable,
          U7 extends Exception,
          U8 extends IOException>
      void genericMethod(
          List<U0> p0,
          List<U1[]> p1,
          List<U2[][]> p2,
          List<U3> p3,
          List<U4> p4,
          List<U5> p5,
          List<U6> p6,
          AllStructures<U0, U1, U2, U3, U4, U5, U6>.InnerClass p7,
          AllStructures<U0, U1, U2, U3, U4, U5, U6>.GenericInnerClass<U1> p8)
          throws U7, U8 {}

  int varArgsAutoBoxingAndForLoop(int... args) {
    int total = 0;
    for (int arg : args) {
      total += arg;
    }
    return total;
  }

  void localClassConstructor(final String name) {
    class LocalClass {
      LocalClass(@Deprecated int value) {
        System.out.println(name + value);
      }
    }
    new LocalClass(42);
  }

  // Generates a bridge method.
  public int compare(Integer a, Integer b) {
    return a < b ? -1 : 1;
  }

  @Retention(RetentionPolicy.CLASS)
  @interface InvisibleAnnotation {
    byte byteValue() default 1;

    char charValue() default '1';

    boolean booleanValue() default true;

    int intValue() default 1;

    short shortValue() default 1;

    long longValue() default 1L;

    float floatValue() default 1f;

    double doubleValue() default 1d;

    String stringValue() default "1";

    Class classValue() default Object.class;

    EnumClass enumValue() default EnumClass.VALUE1;

    Deprecated annotationValue() default @Deprecated;

    byte[] byteArrayValue() default {1};

    char[] charArrayValue() default {'1'};

    boolean[] booleanArrayValue() default {true};

    int[] intArrayValue() default {1};

    short[] shortArrayValue() default {1};

    long[] longArrayValue() default {1L};

    float[] floatArrayValue() default {1f};

    double[] doubleArrayValue() default {1d};

    String[] stringArrayValue() default {"1"};

    Class[] classArrayValue() default {Object.class, int.class, int[].class};

    EnumClass[] enumArrayValue() default {EnumClass.VALUE1};

    Deprecated[] annotationArrayValue() default {@Deprecated};

    int[] otherArrayValue();
  }

  enum EnumClass {
    VALUE0(0),
    VALUE1(1),
    VALUE2(2);

    private int value;

    private EnumClass(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }
  }

  class InnerClass {}

  class GenericInnerClass<T> {}
}
