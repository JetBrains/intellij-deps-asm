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

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;


/**
 * StackMapAttribute is used by CDLC preverifier and also by javac compiller
 * starting from J2SE 1.5. Definition is given in appendix "CLDC Byte Code
 * Typechecker Specification" from CDLC 1.1 specification.
 * 
 * The format of the stack map in the class file is given below. In the following,
 * if the length of the method's byte code1 is 65535 or less, then uoffset
 * represents the type u2; otherwise uoffset represents the type u4. If the
 * maximum number of local variables for the method is 65535 or less, then
 * ulocalvar represents the type u2; otherwise ulocalvar represents the type u4.
 * If the maximum size of the operand stack is 65535 or less, then ustack
 * represents the type u2; otherwise ustack represents the type u4.
 * 
 * <pre>
 *   stack_map { // attribute StackMap
 *     u2 attribute_name_index;
 *     u4 attribute_length
 *     uoffset number_of_entries;
 *     stack_map_frame entries[number_of_entries];
 *   }
 * </pre>
 * 
 * Each stack map frame has the following format:
 * 
 * <pre>
 *   stack_map_frame {
 *     uoffset offset;
 *     ulocalvar number_of_locals;
 *     verification_type_info locals[number_of_locals];
 *     ustack number_of_stack_items;
 *     verification_type_info stack[number_of_stack_items];
 *   }
 * </pre>
 * 
 * The verification_type_info structure consists of a one-byte tag followed by
 * zero or more bytes, giving more information about the tag. Each
 * verification_type_info structure specifies the verification type of one or two
 * locations.
 * 
 * <pre>
 *   union verification_type_info {
 *     Top_variable_info;
 *     Integer_variable_info;
 *     Float_variable_info;
 *     Long_variable_info;
 *     Double_variable_info;
 *     Null_variable_info;
 *     UninitializedThis_variable_info;
 *     Object_variable_info;
 *     Uninitialized_variable_info;
 *   }
 *   
 *   Top_variable_info {
 *     u1 tag = ITEM_Top; // 0
 *   }
 *   
 *   Integer_variable_info {
 *     u1 tag = ITEM_Integer; // 1
 *   }
 *   
 *   Float_variable_info {
 *     u1 tag = ITEM_Float; // 2
 *   }
 *   
 *   Long_variable_info {
 *     u1 tag = ITEM_Long; // 4
 *   }
 *   
 *   Double_variable_info {
 *     u1 tag = ITEM_Double; // 3
 *   }
 *   
 *   Null_variable_info {
 *     u1 tag = ITEM_Null; // 5
 *   }
 *   
 *   UninitializedThis_variable_info {
 *     u1 tag = ITEM_UninitializedThis; // 6
 *   }
 *   
 *   Object_variable_info {
 *     u1 tag = ITEM_Object; // 7
 *     u2 cpool_index;
 *   }
 *   
 *   Uninitialized_variable_info {
 *     u1 tag = ITEM_Uninitialized // 8
 *     uoffset offset;
 *   }
 * </pre>
 * 
 * 
 * @author Eugene Kuleshov
 */
public class StackMapAttribute extends Attribute {
  public List frames = new LinkedList();
  
  public StackMapAttribute() {
    super( "StackMap");
  }

  protected Attribute read( ClassReader cr, int off, int len, char[] buf, 
        Label[] labels, int maxStackk, int maxLocals) {
    StackMapAttribute attr = new StackMapAttribute();
    short size = cr.readShort( off =+ 2);
    for( int i = 0; i<size; i++) {
      int n = cr.readUnsignedShort( off =+ 2);
      if( labels[ n]==null) labels[ n] = new Label();      
      StackMapFrame frame = new StackMapFrame( labels[ n]);
      attr.frames.add( frame);

      n = cr.readUnsignedShort( off =+ 2);
      for( int j = 0; j<n; j++) {
        off = readTypeInfo( cr, off, frame.locals, labels, buf);
      }
      
      n = cr.readUnsignedShort( off =+ 2);
      for( int j = 0; j<n; j++) {
        off = readTypeInfo( cr, off, frame.stack, labels, buf);
      }
    }
    
    return attr;
  }

  protected ByteVector write(ClassWriter cw, byte[] code, int len) {
    ByteVector bv = new ByteVector();
    bv.putShort( frames.size());
    for( int i = 0; i<frames.size(); i++) {
      StackMapFrame frame = ( StackMapFrame) frames.get( i);
      // TODO write frame.label;
      // bv.writeShort( frame.label.getOffset());
      
      bv.putShort( frame.locals.size());
      for( int j = 0; j<frame.locals.size(); j++) {
      	writeTypeInfo( bv, cw, ( StackMapTypeInfo) frame.locals.get(j));
      }
      
      bv.putShort( frame.stack.size());
      for( int j = 0; j<frame.stack.size(); j++) {
        writeTypeInfo( bv, cw, ( StackMapTypeInfo) frame.stack.get(j));
      }
    }
    return bv;
  }
  
  protected void analyze(ClassReader cr, int off, int len, Label[] labels) {
    // TODO implement method StackMapAttribute.analyze
    super.analyze(cr, off, len, labels);
  }
  
  private int readTypeInfo( ClassReader cr, int offset, List locals, Label[] labels, char[] buf) {
    int itemType = cr.readUnsignedShort( offset++) & 0xff;
    StackMapTypeInfo typeInfo = StackMapTypeInfo.getTypeInfo( itemType);
    locals.add( typeInfo);
    switch( itemType) {
      case StackMapTypeInfo.ITEM_Object:  //
        typeInfo.setObject( cr.readClass( offset =+ 2, buf));
        break;        
      
      case StackMapTypeInfo.ITEM_Uninitialized:  //
        int o = cr.readUnsignedShort( offset =+ 2);
        if( labels[ o]==null) labels[ o] = new Label();
        typeInfo.setLabel( labels[ o]);
        break;
    }
    return offset;
  }
  
  private void writeTypeInfo( ByteVector bv, ClassWriter cw, StackMapTypeInfo typeInfo) {
    bv = new ByteVector().putByte( typeInfo.getType());
    switch( typeInfo.getType()) {
      case StackMapTypeInfo.ITEM_Object:  //
        bv.putShort( cw.newClass( typeInfo.getObject()));
        break;        
        
      case StackMapTypeInfo.ITEM_Uninitialized:  //
        // TODO write label from typeInfo.getOffset()
        // bv.putShort( typeInfo.getLabel().getOffset()...);
        break;
    }
  }

  public String toString() {
    StringBuffer sb = new StringBuffer( "StackMap[");
    char pref = '\n';
    for( int i = 0; i<frames.size(); i++) {
      sb.append( pref).append( '[').append( frames.get( i)).append( ']');
      // pref = '\n';
    }
    sb.append( "\n]");
    return sb.toString();
  }
  
}

