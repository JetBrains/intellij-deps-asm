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
 * A symbolic execution stack frame. A stack frame contains a set of local 
 * variable slots, and an operand stack. Warning: long and double values are 
 * represented by <i>two</i> slots in local variables, and by <i>one</i> slot 
 * in the operand stack.
 * 
 * @author Eric Bruneton
 */

public class Frame {
  
  /**
   * The local variables of this frame.
   */
  
  private Value[] locals;
  
  /**
   * The operand stack of this frame.
   */
  
  private Value[] stack;

  /**
   * The number of elements in the operand stack.
   */
  
  private int top;
  
  /**
   * Constructs a new frame with the given size.
   *  
   * @param nLocals the maximum number of local variables of the frame.
   * @param nStack the maximum stack size of the frame.
   */
  
  public Frame (final int nLocals, final int nStack) {
    this.locals = new Value[nLocals];
    this.stack = new Value[nStack];
  }
  
  /**
   * Constructs a new frame that is identical to the given frame.
   * 
   * @param src a frame. 
   */
  
  public Frame (final Frame src) {
    this(src.locals.length, src.stack.length);
    init(src);
  }
  
  /**
   * Copies the state of the given frame into this frame.
   * 
   * @param src a frame.
   * @return this frame.
   */
  
  public Frame init (final Frame src) {
    System.arraycopy(src.locals, 0, locals, 0, locals.length);
    System.arraycopy(src.stack, 0, stack, 0, src.top);
    top = src.top;
    return this;
  }
  
  /**
   * Returns the maximum number of local variables of this frame.
   * 
   * @return the maximum number of local variables of this frame.
   */
  
  public int getLocals () {
    return locals.length;
  }
  
  /**
   * Returns the value of the given local variable.
   * 
   * @param i a local variable index.
   * @return the value of the given local variable.
   */
  
  public Value getLocal (final int i) {
    if (i >= locals.length) {
      throw new RuntimeException("Trying to access an inexistant local variable");
    }
    return locals[i];
  }
  
  /**
   * Sets the value of the given local variable.
   * 
   * @param i a local variable index.
   * @param value the new value of this local variable.
   */
  
  public void setLocal (final int i, final Value value) {
    if (i >= locals.length) {
      throw new RuntimeException("Trying to access an inexistant local variable");
    }
    locals[i] = value;
  }

  /**
   * Returns the number of values in the operand stack of this frame. Long and
   * double values are treated as single values.
   * 
   * @return the number of values in the operand stack of this frame.
   */
  
  public int getStackSize () {
    return top;
  }
  
  /**
   * Returns the value of the given operand stack slot.
   * 
   * @param i the index of an operand stack slot.
   * @return the value of the given operand stack slot.
   */
  
  public Value getStack (final int i) {
    if (i >= top) {
      throw new RuntimeException("Trying to access an inexistant stack element");
    }
    return stack[i];
  }
  
  /**
   * Clears the operand stack of this frame.
   */
  
  public void clearStack () {
    top = 0;
  }

  /**
   * Pops a value from the operand stack of this frame.
   * 
   * @return the value that has been popped from the stack.
   */
  
  public Value pop () {
    if (top == 0) {
      throw new RuntimeException("Cannot pop operand off an empty stack.");
    }
    return stack[--top];
  }
  
  /**
   * Pushes a value into the operand stack of this frame.
   * 
   * @param value the value that must be pushed into the stack.
   */
  
  public void push (final Value value) {
    if (top >= stack.length) {
      throw new RuntimeException("Insufficient maximum stack size.");
    }
    stack[top++] = value;
  }
  
  /**
   * Merges this frame with the given frame.
   *  
   * @param frame a frame.
   * @return <tt>true</tt> if this frame has been changed as a result of the
   *      merge operation, or <tt>false</tt> otherwise.
   */
  
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
  
  /**
   * Merges this frame with the given frame (case of a RET instruction).

   * @param frame a frame
   * @param access the local variables that have been accessed by the 
   *     subroutine to which the RET instruction corresponds.
   * @return <tt>true</tt> if this frame has been changed as a result of the
   *      merge operation, or <tt>false</tt> otherwise.
   */
  
  boolean merge (final Frame frame, final boolean[] access) {
    boolean changes = false;
    for (int i = 0; i < locals.length; ++i) {
      if (!access[i] && !locals[i].equals(frame.locals[i])) {
        locals[i] = frame.locals[i];
        changes = true;
      }
    }
    return changes;
  }
  
  /**
   * Returns a string representation of this frame.
   * 
   * @return a string representation of this frame.
   */
  
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
