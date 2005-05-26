/**
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
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

package org.objectweb.asm.attrs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * The stack map attribute is used during the process of 
 * verification by typechecking (§4.11.1).
 *
 * A stack map attribute consists of zero or more stack map frames. Each stack map
 * frame specifies (either explicitly or implicitly) a bytecode offset, the verification
 * types (§4.11.1) for the local variables, and the verification types for the operand
 * stack.
 * 
 * The type checker deals with and manipulates the expected types of a method's
 * local variables and operand stack. Throughout this section, a location refers to
 * either a single local variable or to a single operand stack entry.
 * 
 * We will use the terms stack frame map and type state interchangeably to describe
 * a mapping from locations in the operand stack and local variables of a method to
 * verification types. We will usually use the term stack frame map when such a
 * mapping is provided in the class file, and the term type state when the mapping is
 * inferred by the type checker.
 * 
 * If a method's Code attribute does not have a StackMapTable attribute, it has an
 * implicit stack map attribute. This implicit stack map attribute is equivalent to a
 * StackMapTable attribute with number_of_entries equal to zero. A method's Code
 * attribute may have at most one StackMapTable attribute, otherwise a
 * java.lang.ClassFormatError is thrown.
 * 
 * The format of the stack map in the class file is given below. In the following, if the
 * length of the method's byte code is 65535 or less, then uoffset represents the type
 * u2; otherwise uoffset represents the type u4. 
 * If the maximum number of local variables for the method is 65535 or less, 
 * then <code>ulocalvar</code> represents the type u2;
 * otherwise ulocalvar represents the type u4. 
 * If the maximum size of the operand stack is 65535 or less, then <code>ustack</code> 
 * represents the type u2; otherwise ustack represents the type u4.
 * 
 *   stack_map { // attribute StackMapTable
 *     u2 attribute_name_index;
 *     u4 attribute_length
 *     uoffset number_of_entries;
 *     stack_map_frame entries[number_of_entries];
 *   }
 * 
 * Each stack_map_frame structure specifies the type state at a particular byte code
 * offset. Each frame type specifies (explicitly or implicitly) a value, offset_delta, that
 * is used to calulate the actual byte code offset at which it applies. The byte code
 * offset at which the frame applies is given by adding <code>1 + offset_delta</code> 
 * to the <code>offset</code> of the previous frame, unless the previous frame is the 
 * initial frame of the method, in which case the byte code offset is <code>offset_delta</code>.
 * 
 * <i>Note that the length of the byte codes is not the same as the length of the Code
 * attribute. The byte codes are embedded in the Code attribute, along with other
 * information.</i>
 *   
 * By using an offset delta rather than the actual byte code offset we
 * ensure, by definition, that stack map frames are in the correctly sorted
 * order. Furthermore, by consistently using the formula <code>offset_delta + 1</code> for
 * all explicit frames, we guarantee the absence of duplicates.
 * 
 * All frame types, even full_frame, rely on the previous frame for some of
 * their semantics. This raises the question of what is the very first frame?
 * The initial frame is implicit, and computed from the method descriptor.
 * See the Prolog code for methodInitialStacFrame.
 * 
 * The stack_map_frame structure consists of a one-byte tag followed by zero or more
 * bytes, giving more information, depending upon the tag.
 * 
 * A stack map frame may belong to one of several frame types
 * 
 *   union stack_map_frame {
 *     same_frame;
 *     same_locals_1_stack_item_frame;
 *     chop_frame;
 *     same_frame_extended;
 *     append_frame;
 *     full_frame;
 *   }
 * 
 * The frame type same_frame is represented by tags in the range [0-63]. If the frame
 * type is same_frame, it means the frame has exactly the same locals as the previous
 * stack map frame and that the number of stack items is zero. The offset_delta value
 * for the frame is the value of the tag field, frame_type. The form of such a frame is
 * then:
 * 
 *   same_frame {
 *     u1 frame_type = SAME;  // 0-63
 *   }
 * 
 * The frame type same_locals_1_stack_item_frame is represented by tags in the range
 * [64, 127]. If the frame_type is same_locals_1_stack_item_frame, it means the frame
 * has exactly the same locals as the previous stack map frame and that the number
 * of stack items is 1. The offset_delta value for the frame is the value 
 * (frame_type - 64). There is a verification_type_info following the frame_type 
 * for the one stack item. The form of such a frame is then:
 * 
 *   same_locals_1_stack_item_frame {
 *     u1 frame_type = SAME_LOCALS_1_STACK_ITEM;  // 64-127
 *     verification_type_info stack[1];
 *   }
 * 
 * Tags in the range [128-247] are reserved for future use.
 * 
 * The frame type chop_frame is represented by tags in the range [248-250]. If the
 * frame_type is chop_frame, it means that the current locals are the same as the locals
 * in the previous frame, except that the k last locals are absent. The value of k is
 * given by the formula 251-frame_type.
 * 
 * The form of such a frame is then:
 * 
 *   chop_frame {
 *     u1 frame_type=CHOP;  // 248-250
 *     uoffset offset_delta;
 *   }
 * 
 * The frame type same_frame_extended is represented by the tag value 251. If the
 * frame type is same_frame_extended, it means the frame has exactly the same locals
 * as the previous stack map frame and that the number of stack items is zero.
 * The form of such a frame is then:
 * 
 *   same_frame_extended {
 *     u1 frame_type = SAME_FRAME_EXTENDED;  // 251
 *     uoffset offset_delta;
 *   }
 * 
 * The frame type append_frame is represented by tags in the range [252-254]. If the
 * frame_type is append_frame, it means that the current locals are the same as the
 * locals in the previous frame, except that k additional locals are defined. The value
 * of k is given by the formula frame_type-251.
 * 
 * The form of such a frame is then:
 * 
 *   append_frame {
 *     u1 frame_type =APPEND;  // 252-254
 *     uoffset offset_delta;
 *     verification_type_info locals[frame_type -251];
 *   }
 * 
 * The 0th entry in locals represents the type of the first additional local variable. If
 * locals[M] represents local variable N, then locals[M+1] represents local variable N+1
 * if locals[M] is one of Top_variable_info, Integer_variable_info, Float_variable_info,
 * Null_variable_info, UninitializedThis_variable_info, Object_variable_info, or
 * Uninitialized_variable_info, otherwise locals[M+1] represents local variable N+2. It is
 * an error if, for any index i, locals[i] represents a local variable whose index is
 * greater than the maximum number of local variables for the method.
 * 
 * The frame type full_frame is represented by the tag value 255. The form of such a
 * frame is then:
 * 
 * <pre>
 *   full_frame {
 *     u1 frame_type = FULL_FRAME;  // 255
 *     uoffset offset_delta;
 *     ulocalvar number_of_locals;
 *     verification_type_info locals[number_of_locals];
 *     ustack number_of_stack_items;
 *     verification_type_info stack[number_of_stack_items];
 *   }
 * </pre>
 * 
 * The 0th entry in locals represents the type of local variable 0. If locals[M] represents
 * local variable N, then locals[M+1] represents local variable N+1 if locals[M] is one
 * of Top_variable_info, Integer_variable_info, Float_variable_info, Null_variable_info,
 * UninitializedThis_variable_info, Object_variable_info, or Uninitialized_variable_info,
 * otherwise locals[M+1] represents local variable N+2. It is an error if, for any index
 * i, locals[i] represents a local variable whose index is greater than the maximum
 * number of local variables for the method.
 * 
 * The 0th entry in stack represents the type of the bottom of the stack, and
 * subsequent entries represent types of stack elements closer to the top of the
 * operand stack. We shall refer to the bottom element of the stack as stack element
 * 0, and to subsequent elements as stack element 1, 2 etc. If stack[M] represents stack
 * element N, then stack[M+1] represents stack element N+1 if stack[M] is one of
 * Top_variable_info, Integer_variable_info, Float_variable_info, Null_variable_info,
 * UninitializedThis_variable_info, Object_variable_info, or Uninitialized_variable_info,
 * otherwise stack[M+1] represents stack element N+2. It is an error if, for any index i,
 * stack[i] represents a stack entry whose index is greater than the maximum operand
 * stack size for the method.
 * 
 * We say that an instruction in the byte code has a corresponding stack map frame if
 * the offset in the offset field of the stack map frame is the same as the offset of the
 * instruction in the byte codes.
 * 
 * The verification_type_info structure consists of a one-byte tag followed by zero or
 * more bytes, giving more information about the tag. Each verification_type_info
 * structure specifies the verification type of one or two locations.
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
 * </pre>
 * 
 * The Top_variable_info type indicates that the local variable has the verification type
 * top (T.)
 * 
 * <pre>
 *   Top_variable_info {
 *     u1 tag = ITEM_Top; // 0
 *   }
 * </pre>
 * 
 * The Integer_variable_info type indicates that the location contains the verification
 * type int.
 * 
 * <pre>
 *   Integer_variable_info {
 *     u1 tag = ITEM_Integer; // 1
 *   }
 * </pre>
 * 
 * The Float_variable_info type indicates that the location contains the verification type
 * float.
 * 
 * <pre>
 *   Float_variable_info {
 *     u1 tag = ITEM_Float; // 2
 *   }
 * </pre>
 * 
 * The Long_variable_info type indicates that the location contains the verification type
 * long. If the location is a local variable, then:
 * 
 * <ul>
 * <li>It must not be the local variable with the highest index.</li>
 * <li>The next higher numbered local variable contains the verification type T.</li>
 * </ul>
 * 
 * If the location is an operand stack entry, then:
 * 
 * <ul>
 * <li>The current location must not be the topmost location of the operand stack.</li>
 * <li>the next location closer to the top of the operand stack contains the verification type T.</li>
 * </ul>
 * 
 * This structure gives the contents of two locations in the operand stack or in the
 * local variables.
 * 
 * <pre>
 *   Long_variable_info {
 *     u1 tag = ITEM_Long; // 4
 *   }
 * </pre>
 * 
 * The Double_variable_info type indicates that the location contains the verification
 * type double. If the location is a local variable, then:
 * 
 * <ul>
 * <li>It must not be the local variable with the highest index.</li>
 * <li>The next higher numbered local variable contains the verification type T.<li>
 * </ul>
 * 
 * If the location is an operand stack entry, then:
 * 
 * <ul>
 * <li>The current location must not be the topmost location of the operand stack.</li>
 * <li>the next location closer to the top of the operand stack contains the verification type T.</li>
 * </ul>
 * 
 * This structure gives the contents of two locations in in the operand stack or in the
 * local variables.
 * 
 * <pre>
 *   Double_variable_info {
 *     u1 tag = ITEM_Double; // 3
 *   }
 * </pre>
 * 
 * The Null_variable_info type indicates that location contains the verification type null.
 * 
 * <pre>
 *   Null_variable_info {
 *     u1 tag = ITEM_Null; // 5
 *   }
 * </pre>
 * 
 * The UninitializedThis_variable_info type indicates that the location contains the
 * verification type uninitializedThis.
 * 
 * <pre>
 *   UninitializedThis_variable_info {
 *     u1 tag = ITEM_UninitializedThis; // 6
 *   }
 * </pre>
 * 
 * The Object_variable_info type indicates that the location contains an instance of the
 * class referenced by the constant pool entry.
 * 
 * <pre>
 *   Object_variable_info {
 *     u1 tag = ITEM_Object; // 7
 *     u2 cpool_index;
 *   }
 * </pre>
 * 
 * The Uninitialized_variable_info indicates that the location contains the verification
 * type uninitialized(offset). The offset item indicates the offset of the new instruction
 * that created the object being stored in the location.
 * 
 * <pre>
 *   Uninitialized_variable_info {
 *     u1 tag = ITEM_Uninitialized // 8
 *     uoffset offset;
 *   }
 * </pre>
 *
 * @see "ClassFileFormat-Java6.fm Page 138 Friday, April 15, 2005 3:22 PM"
 *
 * @author Eugene Kuleshov
 */

public class StackMapTableAttribute extends Attribute {
  /**
   * Frame has exactly the same locals as the previous stack map frame and
   * number of stack items is zero.
   */
  public static final int SAME_FRAME = 0;  // to 63 (0-3f)
  /**
   * Frame has exactly the same locals as the previous stack map frame and 
   * number of stack items is 1
   */
  public static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64;  // to 127 (40-7f)
  /**
   * Reserved for future use
   */
  public static final int RESERVED = 128;
  /**
   * Frame where current locals are the same as the locals in the previous
   * frame, except that the k last locals are absent. 
   * The value of k is given by the formula 251-frame_type.
   */
  public static final int CHOP_FRAME = 248;  // to 250 (f8-fA)
  /**
   * Frame has exactly the same locals as the previous stack map frame and 
   * number of stack items is zero. Offset is bigger then 63;
   */
  public static final int SAME_FRAME_EXTENDED = 251; // fb
  /**
   * Frame where current locals are the same as the locals in the previous frame, 
   * except that k additional locals are defined. 
   * The value of k is given by the formula frame_type-251.
   */
  public static final int APPEND_FRAME = 252;  // to 254  // fc-fe
  /**
   * Full frame
   */
  public static final int FULL_FRAME = 255;  // ff
  
  
  private static final int MAX_SHORT = 65535;
  
  /**
   * A <code>List</code> of <code>StackMapFrame</code> instances.
   */
  private List frames;

  
  public StackMapTableAttribute() {
    super( "StackMapTable");
  }
  
  public StackMapTableAttribute( List frames) {
    this();
    this.frames = frames;
  }
  
  public List getFrames() {
    return frames;
  }

  public StackMapFrame getFrame (Label label) {
    for (int i = 0; i < frames.size(); i++) {
      StackMapFrame frame = (StackMapFrame)frames.get(i);
      if (frame.label == label) {
        return frame;
      }
    }
    return null;
  }
  
  public boolean isUnknown () {
    return false;
  }
  
  public boolean isCodeAttribute () {
    return true;
  }
  
  protected Attribute read( ClassReader cr, int off, int len, char[] buf, 
      int codeOff, Label[] labels) {
    ArrayList frames = new ArrayList();
    
    // note that this is not the size of Code attribute
    boolean isExtCodeSize = cr.readInt(codeOff + 4) > MAX_SHORT;
    boolean isExtLocals = cr.readUnsignedShort(codeOff + 2) > MAX_SHORT;
    boolean isExtStack = cr.readUnsignedShort(codeOff) > MAX_SHORT;
    
    int offset = 0;
    
    StackMapFrame frame = new StackMapFrame( getLabel(offset, labels), 
//        calculateLocals( cr.readClass( cr.header + 2, buf),  // class name 
//            cr.readUnsignedShort( codeOff),     // method access
//            cr.readUTF8( codeOff + 2, buf),     // method name
//            cr.readUTF8( codeOff + 4, buf)),    // method desc
        // TODO read method access flags, name and desc
        calculateLocals( cr.readClass( cr.header + 2, buf),  // class name 
                         0,     // method access
                         "",     // method name
                         "()V"),    // method desc
        Collections.EMPTY_LIST); 
    frames.add( frame);
    
    int size;
    if (isExtCodeSize) {
      size = cr.readInt(off);  off += 4;
    } else {
      size = cr.readUnsignedShort(off);  off += 2;
    }
    
    for ( ; size>0; size--) {
      int tag = cr.readByte(off); // & 0xff;
      off++;

      List stack;
      List locals;

      int offsetDelta;
      int frameType;
      if( tag<SAME_LOCALS_1_STACK_ITEM_FRAME) {
        frameType = SAME_FRAME;
        offsetDelta = tag;
        
        locals = new ArrayList( frame.locals);
        stack = Collections.EMPTY_LIST;
        
      } else if( tag<RESERVED) {
        frameType = SAME_LOCALS_1_STACK_ITEM_FRAME;
        offsetDelta = tag - SAME_LOCALS_1_STACK_ITEM_FRAME;
        
        locals = new ArrayList( frame.locals);
        stack = new ArrayList();
        // read verification_type_info stack[1];
        off = readType( stack, isExtCodeSize, cr, off, labels, buf);
        
      } else {
        if( isExtCodeSize) {
          offsetDelta = cr.readInt( off);  off += 4;
        } else {
          offsetDelta = cr.readUnsignedShort( off);  off += 2;
        }

        if( tag>=CHOP_FRAME && tag<SAME_FRAME_EXTENDED) {
          frameType = CHOP_FRAME;
          stack = Collections.EMPTY_LIST;

          int k = SAME_FRAME_EXTENDED - tag;
          // copy locals from prev frame and chop last k 
          locals = new ArrayList( frame.locals.subList( 0, frame.locals.size()-k));
        
        } else if( tag==SAME_FRAME_EXTENDED) {
          frameType = SAME_FRAME_EXTENDED;
          stack = Collections.EMPTY_LIST;
          locals = new ArrayList( frame.locals);
          
        } else if( /* tag>=APPEND && */ tag<FULL_FRAME) {
          frameType = APPEND_FRAME;
          stack = Collections.EMPTY_LIST;

          // copy locals from prev frame and append new k
          locals = new ArrayList( frame.locals);
          for( int k = tag - SAME_FRAME_EXTENDED; k>0; k--) {
            off = readType(locals, isExtCodeSize, cr, off, labels, buf);
          }
  
        } else if( tag==FULL_FRAME) {
          frameType = FULL_FRAME;

          // read verification_type_info locals[number_of_locals];
          locals = new ArrayList();
          off = readTypes(locals, isExtLocals, isExtCodeSize, cr, off, labels, buf);
          
          // read verification_type_info stack[number_of_stack_items];
          stack = new ArrayList();
          off = readTypes(stack, isExtStack, isExtCodeSize, cr, off, labels, buf);
          
        } else {
          throw new RuntimeException( "Unknown frame type "+tag+" after offset "+offset);
        
        }
      }

      offset += offsetDelta; 
      
      // System.err.println( offset +" : " + offsetDelta + " : "+  frameType+" : "+ frame);
      
      frame = new StackMapFrame( getLabel( offset, labels), locals, stack);
      frames.add( frame);
      
      offset++;
    }
    
    return new StackMapTableAttribute( frames);
  }

  protected ByteVector write( ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    // TODO implement write
    throw new RuntimeException( "Not yet implemented");
  }
  
  /**
   * Use method signature and access flags to resolve initial locals state.
   * 
   * @return list of <code>StackMapType</code> instances representing locals for an initial frame.
   */  
  public static List calculateLocals( String className, int access, String methodName, String methodDesc) {
    List locals = new ArrayList();

    // TODO
    if( "<init>".equals( methodName) && !className.equals( "java/lang/Object")) {
      StackMapType typeInfo = StackMapType.getTypeInfo( StackMapType.ITEM_UninitializedThis);
      typeInfo.setObject( className);  // this
      locals.add( typeInfo);
    } else if(( access & Opcodes.ACC_STATIC)==0) {
      StackMapType typeInfo = StackMapType.getTypeInfo( StackMapType.ITEM_Object);
      typeInfo.setObject( className);  // this
      locals.add( typeInfo);
    }
      
    Type[] types = Type.getArgumentTypes(methodDesc);
    for( int i = 0; i < types.length; i++) {
      Type t = types[ i];
      StackMapType smt;
      switch( t.getSort()) {
        case Type.LONG:
          smt = StackMapType.getTypeInfo( StackMapType.ITEM_Long);
          break;
        case Type.DOUBLE:
          smt = StackMapType.getTypeInfo( StackMapType.ITEM_Double);
          break;
          
        case Type.FLOAT:
          smt = StackMapType.getTypeInfo( StackMapType.ITEM_Float);
          break;
          
        case Type.ARRAY:
        case Type.OBJECT:
          smt = StackMapType.getTypeInfo( StackMapType.ITEM_Object);
          smt.setObject( t.getDescriptor());  // TODO verify class name
          break;

        default:
          smt = StackMapType.getTypeInfo( StackMapType.ITEM_Integer);
          break;
      }
    }
    
    return locals;
  }

  private int readTypes( List info, boolean isExt, boolean isExtCodeSize, 
      ClassReader cr, int off, Label[] labels, char[] buf) {
    int n = 0;
    if( isExt) {
      n = cr.readInt( off);  off += 4;
    } else {
      n = cr.readUnsignedShort( off);  off += 2;
    }
    
    for( ; n>0; n--) {
      off = readType( info, isExtCodeSize, cr, off, labels, buf);
    }
    return off;
  }

  private int readType( List info, boolean isExtCodeSize, ClassReader cr, int off, Label[] labels, char[] buf) {
    int itemType = cr.readByte( off++);
    StackMapType typeInfo = StackMapType.getTypeInfo( itemType);
    info.add( typeInfo);
    switch( itemType) {
      // case StackMapType.ITEM_Long: //
      // case StackMapType.ITEM_Double: //
      // info.add(StackMapType.getTypeInfo(StackMapType.ITEM_Top));
      // break;

      case StackMapType.ITEM_Object: //
        typeInfo.setObject( cr.readClass( off, buf));
        off += 2;
        break;

      case StackMapType.ITEM_Uninitialized: //
        int offset;
        if( isExtCodeSize) {
          offset = cr.readInt( off);
          off += 4;
        } else {
          offset = cr.readUnsignedShort( off);
          off += 2;
        }
        
        typeInfo.setLabel( getLabel( offset, labels));
        break;
    }
    return off;
  }

  private Label getLabel( int offset, Label[] labels) {
    Label l = labels[ offset];
    if( l!=null) {
      return l;
    }
    return labels[ offset] = new Label();
  }
  
  public String toString () {
    StringBuffer sb = new StringBuffer("StackMapTable[");
    for (int i = 0; i < frames.size(); i++) {
      sb.append('\n').append('[').append(frames.get(i)).append(']');
    }
    sb.append("\n]");
    return sb.toString();
  }
}

