/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
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

package org.objectweb.asm.tree.analysis;

/**
 * A {@link Value} that is represented by its type in a seven types type sytem.
 * This type system distinguishes the UNINITIALZED, INT, FLOAT, LONG, DOUBLE,
 * REFERENCE and RETURNADDRESS types.
 * 
 * @author Eric Bruneton
 */

public class BasicValue implements Value {
  
  private final static int UNINITIALIZED = 0;
  
  private final static int INT = 1;
  
  private final static int FLOAT = 2;
  
  private final static int LONG = 3;
  
  private final static int DOUBLE = 4;
  
  private final static int REFERENCE = 5;
  
  private final static int RETURNADDRESS = 6;
  
  public final static Value UNINITIALIZED_VALUE = new BasicValue(UNINITIALIZED);
  
  public final static Value INT_VALUE = new BasicValue(INT);
  
  public final static Value FLOAT_VALUE = new BasicValue(FLOAT);
  
  public final static Value LONG_VALUE = new BasicValue(LONG);
  
  public final static Value DOUBLE_VALUE = new BasicValue(DOUBLE);
  
  public final static Value REFERENCE_VALUE = new BasicValue(REFERENCE);
  
  public final static Value RETURNADDRESS_VALUE = new BasicValue(RETURNADDRESS);
  
  private int type;
  
  private BasicValue (final int type) {
    this.type = type;  
  }

  public int getType () {
    return type;
  }
  
  public int getSize () {
    return type == LONG || type == DOUBLE ? 2 : 1;
  }
  
  public Value merge (final Value value) {
    if (type != ((BasicValue)value).type) {
      return UNINITIALIZED_VALUE;
    }
    return this;
  }

  public boolean equals (final Value value) {
    return value == this;
  }
  
  public String toString () {
    switch (type) {
      case UNINITIALIZED: return "U";
      case INT: return "I";
      case FLOAT: return "F";
      case LONG: return "L";
      case DOUBLE: return "D";
      case REFERENCE: return "R";
      case RETURNADDRESS: return "A";
      default: throw new RuntimeException("Internal error.");
    }
  }
}