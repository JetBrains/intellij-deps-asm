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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.AbstractInsnNode;

/**
 * TODO.
 * 
 * @author Eric Bruneton
 */

public class DataflowValue implements Value {

  private int size;
  
  public final AbstractInsnNode insn;
  
  public final List values;
  
  public DataflowValue (final int size, final AbstractInsnNode insn) {
    this(size, insn, Collections.EMPTY_LIST);
  }
  
  public DataflowValue (
    final int size, 
    final AbstractInsnNode insn, 
    final Value value) 
  {
    this(size, insn, new ArrayList());
    values.add(value);
  }
  
  public DataflowValue (
    final int size, 
    final AbstractInsnNode insn, 
    final Value value1,
    final Value value2) 
  {
    this(size, insn, new ArrayList());
    values.add(value1);
    values.add(value2);
  }
  
  public DataflowValue (
    final int size, 
    final AbstractInsnNode insn, 
    final List values) 
  {
    this.size = size;
    this.insn = insn;
    this.values = values;
  }
  
  public int getSize () {
    return size;
  }

  public Value merge (final Value value) {
    if (size != value.getSize()) {
      return new DataflowValue(1, insn, values);
    }
    return this;
  }

  public boolean equals (final Value value) {
    return size == value.getSize();
  }
}