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

import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;


/**
 * StackMapFrame is used by {@link StackMapAttribute} to hold state of the stack and 
 * local variables for a single execution branch. 
 * 
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=139">JSR 139 : Connected Limited Device Configuration 1.1</a>
 * 
 * @author Eugene Kuleshov
 */
public class StackMapFrame {
  public Label label;
  public List locals = new LinkedList();
  public List stack = new LinkedList();

  public int read( ClassReader cr, int off, char[] buf, int codeOff, Label[] labels) {
    int n = cr.readUnsignedShort( off =+ 2);
    if( labels[ n]==null) labels[ n] = new Label();
    label = labels[ n];
    off = readTypeInfo( cr, off, locals, labels, buf, cr.readUnsignedShort( codeOff+2));  //  maxLocals
    off = readTypeInfo( cr, off, stack, labels, buf, cr.readUnsignedShort( codeOff));  // maxStack
    return off;
  }

  public void write( ClassWriter cw, int maxStack, int maxLocals, ByteVector bv) {
    bv.putShort( label.getOffset());
    writeTypeInfo( bv, cw, locals, maxLocals);
    writeTypeInfo( bv, cw, stack, maxStack);
  }

  private int readTypeInfo( ClassReader cr, int off, List info, Label[] labels, char[] buf, int max) {
    int n = max>StackMapAttribute.MAX_SIZE ? cr.readInt( off =+ 4) : cr.readUnsignedShort( off =+ 2);
    for( int j = 0; j<n; j++) {
      int itemType = cr.readUnsignedShort( off++) & 0xff;
      StackMapTypeInfo typeInfo = StackMapTypeInfo.getTypeInfo( itemType);
      info.add( typeInfo);
      switch( itemType) {
        case StackMapTypeInfo.ITEM_Object:  //
          typeInfo.setObject( cr.readClass( off =+ 2, buf));
          break;

         case StackMapTypeInfo.ITEM_Uninitialized:  //
           int o = cr.readUnsignedShort( off =+ 2);
           if( labels[ o]==null) labels[ o] = new Label();
           typeInfo.setLabel( labels[ o]);
           break;
      }
    }
    return off;
  }
  
  private void writeTypeInfo( ByteVector bv, ClassWriter cw, List info, int max) {
    if( max>StackMapAttribute.MAX_SIZE) bv.putInt( info.size());
    else bv.putShort( info.size());
    for( int j = 0; j<info.size(); j++) {
      StackMapTypeInfo typeInfo = ( StackMapTypeInfo) info.get( j);
      bv = new ByteVector().putByte( typeInfo.getType());
      switch( typeInfo.getType()) {
        case StackMapTypeInfo.ITEM_Object:  //
          bv.putShort( cw.newClass( typeInfo.getObject()));
          break;

         case StackMapTypeInfo.ITEM_Uninitialized:  //
           bv.putShort( typeInfo.getLabel().getOffset());
           break;
      }
    }
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

