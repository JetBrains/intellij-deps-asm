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
 * TODO.
 * 
 * @author Eric Bruneton
 */

public class Frame {
  
  private Value[] locals;
  
  private Value[] stack;

  private int top;
  
  public Frame (final int nLocals, final int nStack) {
    this.locals = new Value[nLocals];
    this.stack = new Value[nStack];
  }
    
  public Frame (final Frame src) {
    this(src.locals.length, src.stack.length);
    init(src);
  }
  
  public Frame init (final Frame src) {
    System.arraycopy(src.locals, 0, locals, 0, locals.length);
    System.arraycopy(src.stack, 0, stack, 0, src.top);
    top = src.top;
    return this;
  }
  
  public int getLocals () {
    return locals.length;
  }
  
  public Value getLocal (int i) {
    return locals[i];
  }
  
  public void setLocal (int i, Value value) {
    locals[i] = value;
  }
  
  public int getStackSize () {
    return top;
  }
  
  public Value getStack (int i) {
    return stack[i];
  }
  
  public void clearStack () {
    top = 0;
  }
  
  public Value pop () {
    if (top == 0) {
      throw new RuntimeException("Cannot pop operand off an empty stack.");
    }
    return stack[--top];
  }
  
  public void push (final Value value) {
    if (top >= stack.length) {
      throw new RuntimeException("Insufficient maximum stack size.");
    }
    stack[top++] = value;
  }
  
  boolean merge (final Frame frame) {
    if (top != frame.top) {
      throw new RuntimeException("Incompatible stack heights");
    }
    boolean changes = false;
    for (int i = 0; i < locals.length; ++i) {
      Value v = locals[i].merge(frame.locals[i]);
      if (v != locals[i]) {
        locals[i] = v;
        changes |= true;
      }
    }
    for (int i = 0; i < top; ++i) {
      Value v = stack[i].merge(frame.stack[i]);
      if (v != stack[i]) {
        stack[i] = v;
        changes |= true;
      }
    }
    return changes;
  }
  
  boolean merge (final Frame beforeJSR, final boolean[] access) {
    boolean changes = false;
    for (int i = 0; i < locals.length; ++i) {
      if (!access[i] && !locals[i].equals(beforeJSR.locals[i])) {
        locals[i] = beforeJSR.locals[i];
        changes = true;
      }
    }
    return changes;
  }
  
  public String toString () {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < locals.length; ++i) {
      b.append(locals[i]);
    }
    b.append(' ');
    for (int i = 0; i < top; ++i) {
      b.append(stack[i].toString());
    }
    return b.toString();
  }
}
