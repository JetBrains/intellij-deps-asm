/**
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
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm.attrs;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Label;


/**
 * Frame instance used by StackMapAttribute to hold state of the stack and 
 * local variables for a single execution branch 
 * 
 * @author Eugene Kuleshov
 */
public class StackMapFrame {
  public Label label;
  public List locals = new LinkedList();
  public List stack = new LinkedList();

  public StackMapFrame( Label label) {
    this.label = label;
  }
  
  public String toString() {
    StringBuffer sb = new StringBuffer( "Frame:L"+System.identityHashCode( label));
    sb.append( " locals[");
    String pref = "";
    for( int i = 0; i<locals.size(); i++) {
      sb.append( pref).append( locals.get( i));
      pref = ":";
    }
    sb.append( "]");
    sb.append( " stack[");
    pref = "";
    for( int i = 0; i<stack.size(); i++) {
      sb.append( pref).append( stack.get( i));
      pref = ":";
     }
    sb.append( "]");
    return sb.toString();
  }

}

