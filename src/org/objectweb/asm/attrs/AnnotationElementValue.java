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
 */

package org.objectweb.asm.attrs;

import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

/**
 * The element_value structure is a discriminated union representing the value of a
 * element-value pair. It is used to represent values in all class file attributes
 * that describe annotations ( RuntimeVisibleAnnotations, RuntimeInvisibleAnnotations,
 * RuntimeVisibleParameterAnnotations, and RuntimeInvisibleParameterAnnotations).
 * <p>
 * The element_value structure has the following format:
 * <pre>
 *   element_value {
 *     u1 tag;
 *     union {
 *       u2   const_value_index;
 *       {
 *         u2   type_name_index;
 *         u2   const_name_index;
 *       } enum_const_value;
 *       u2   class_info_index;
 *       annotation annotation_value;
 *       {
 *         u2    num_values;
 *         element_value values[num_values];
 *       } array_value;
 *     } value;
 *   }
 * </pre>
 * The items of the element_value structure are as follows:
 * <dl>
 * <dt>tag</dt>
 * <dd>The tag item indicates the type of this annotation element-value pair. The letters
 *     'B', 'C', 'D', 'F', 'I', 'J', 'S', and 'Z' indicate a primitive type. These
 *     letters are interpreted as BaseType characters (Table 4.2). The other legal
 *     values for tag are listed with their interpretations in this table:
 *     <pre>
 *     tag  value Element Type
 *     's'  String
 *     'e'  enum constant
 *     'c'  class
 *     '@'  annotation type
 *     '['  array
 *   </pre>
 *   </dd>
 * <dt>value</dt>
 * <dd>The value item represents the value of this annotation element. This item is
 *     a union. The tag item, above, determines which item of the union is to be used:
 *   <dl>
 *   <dt>const_value_index</dt>
 *   <dd>The const_value_index item is used if the tag item is one of 'B', 'C', 'D',
 *       'F', 'I', 'J', 'S', 'Z', or 's'. The value of the const_value_index item must
 *       be a valid index into the constant_pool table. The constant_pool entry at
 *       that index must be of the correct entry type for the field type designated by
 *       the tag item, as specified in table 4.6, with one exception: if the tag is
 *       's', the the value of the const_value_index item must be the index of a
 *       CONSTANT_Utf8 structure, rather than a CONSTANT_String.</dd>
 *   <dt>enum_const_value</dt>
 *   <dd>The enum_const_value item is used if the tag item is 'e'. The
 *       enum_const_value item consists of the following two items:
 *     <dl>
 *     <dt>type_name_index</dt>
 *     <dd>The value of the type_name_index item must be a valid index into the
 *         constant_pool table. The constant_pool entry at that index must be a
 *         CONSTANT_Utf8_info structure representing the binary name (JLS 13.1) of the
 *         type of the enum constant represented by this element_value structure.</dd>
 *     <dt>const_name_index</dt>
 *     <dd>The value of the const_name_index item must be a valid index into the
 *         constant_pool table. The constant_pool entry at that index must be a
 *         CONSTANT_Utf8_info structure representing the simple name of the enum
 *         constant represented by this element_value structure.</dd>
 *     </dl>
 *     </dd>
 *   <dt>class_info_index</dt>
 *   <dd>The class_info_index item is used if the tag item is 'c'. The
 *       class_info_index item must be a valid index into the constant_pool table.
 *       The constant_pool entry at that index must be a CONSTANT_Utf8_info 
 *       structure representing the return descriptor of the type that is 
 *       reified by the class represented by this element_value structure.</dd>
 *   <dt>annotation_value</dt>
 *   <dd>The annotation_value item is used if the tag item is '@'. The element_value
 *       structure represents a "nested" {@link org.objectweb.asm.attrs.Annotation annotation}.</dd>
 *   <dt>array_value</dt>
 *   <dd>The array_value item is used if the tag item is '['. The array_value item
 *       consists of the following two items:
 *     <dl>
 *     <dt>num_values</dt>
 *     <dd>The value of the num_values item gives the number of elements in the
 *         array-typed value represented by this element_value structure. Note that a
 *         maximum of 65535 elements are permitted in an array-typed element value.</dd>
 *     <dt>values</dt>
 *     <dd>Each element of the values table gives the value of an element of the
 *         array-typed value represented by this {@link AnnotationElementValue element_value structure}.</dd>
 *     </dl>
 *     </dd>
 *   </dl>
 *   </dd>
 * </dl>
 *
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=175">JSR 175 : A Metadata
 * Facility for the Java Programming Language</a>
 *
 * @author Eugene Kuleshov
 */

public class AnnotationElementValue {

  private Object value;

  public AnnotationElementValue () {
  }

  public AnnotationElementValue (Object value) {
    this.value = value;
  }

  public int getTag () {
    if (value instanceof Byte) {
      return 'B';
    }
    if (value instanceof Character) {
      return 'C';
    }
    if (value instanceof Double) {
      return 'D';
    }
    if (value instanceof Float) {
      return 'F';
    }
    if (value instanceof Integer) {
      return 'I';
    }
    if (value instanceof Long) {
      return 'J';
    }
    if (value instanceof Short) {
      return 'S';
    }
    if (value instanceof Boolean) {
      return 'Z';
    }
    if (value instanceof String) {
      return 's';
    }
    if (value instanceof EnumConstValue) {
      return 'e';
    }
    if (value instanceof Type) {
      return 'c';
    }
    if (value instanceof Annotation) {
      return '@';
    }
    if (value instanceof AnnotationElementValue[]) {
      return '[';
    }
    return -1;    
  }

  public Object getValue () {
    return value;
  }

  /**
   * Reads element_value data structures.
   *
   * @param cr the class that contains the attribute to be read.
   * @param off index of the first byte of the data structure.
   * @param buf buffer to be used to call {@link ClassReader#readUTF8 readUTF8},
   *      {@link ClassReader#readClass(int,char[]) readClass} or {@link
   *      ClassReader#readConst readConst}.
   *
   * @return offset position in bytecode after reading annotation
   */

  public int read (ClassReader cr, int off, char[] buf) {
    int tag = cr.readByte(off++);
    switch (tag) {
      case 'B':  // pointer to CONSTANT_Byte
      case 'C':  // pointer to CONSTANT_Char
      case 'D':  // pointer to CONSTANT_Double
      case 'F':  // pointer to CONSTANT_Float
      case 'I':  // pointer to CONSTANT_Integer
      case 'J':  // pointer to CONSTANT_Long
      case 'S':  // pointer to CONSTANT_Short
      case 'Z':  // pointer to CONSTANT_Boolean
        value = cr.readConst(cr.readUnsignedShort(off), buf);
        off += 2;
        break;

      case 's':  // pointer to CONSTANT_Utf8
        value = cr.readUTF8(off, buf);
        off += 2;
        break;

      case 'e':  // enum_const_value
        // TODO verify the data structures
        value = new EnumConstValue(cr.readUTF8(off, buf), cr.readUTF8(off + 2, buf));
        off += 4;
        break;

      case 'c':  // class_info
        value = Type.getType(cr.readUTF8(off, buf));
        off += 2;
        break;

      case '@':  // annotation_value
        value = new Annotation();
        off = ((Annotation)value).read(cr, off, buf);
        break;

      case '[':  // array_value
        int size = cr.readUnsignedShort(off);
        off += 2;
        AnnotationElementValue[] v = new AnnotationElementValue[size];
        value = v;
        for (int i = 0; i < size; i++) {
          v[i] = new AnnotationElementValue();
          off = v[i].read(cr, off, buf);
        }
        break;
    }
    return off;
  }

  /**
   * Writes element_value data structures.
   *
   * @param bv the byte array form to store data structures.
   * @param cw the class to which this attribute must be added. This parameter
   *      can be used to add to the constant pool of this class the items that
   *      corresponds to this attribute.
   * @return bv.
   */

  public ByteVector write (ByteVector bv, ClassWriter cw) {
    int tag = getTag();
    bv.putByte(tag);
    switch (tag) {
      case 'B':  // pointer to CONSTANT_Byte
      case 'C':  // pointer to CONSTANT_Char
      case 'D':  // pointer to CONSTANT_Double
      case 'F':  // pointer to CONSTANT_Float
      case 'I':  // pointer to CONSTANT_Integer
      case 'J':  // pointer to CONSTANT_Long
      case 'S':  // pointer to CONSTANT_Short
      case 'Z':  // pointer to CONSTANT_Boolean
        bv.putShort(cw.newConst(value));
        break;

      case 's':  // pointer to CONSTANT_Utf8
        bv.putShort(cw.newUTF8((String)value));
        break;

      case 'e':  // enum_const_value
        bv.putShort(cw.newUTF8(((EnumConstValue)value).typeName));
        bv.putShort(cw.newUTF8(((EnumConstValue)value).constName));
        break;

      case 'c':  // class_info
        bv.putShort(cw.newUTF8(((Type)value).getDescriptor()));
        break;

      case '@':  // annotation_value
        ((Annotation)value).write(bv, cw);
        break;

      case '[':  // array_value
        AnnotationElementValue[] v = (AnnotationElementValue[])value;
        bv.putShort(v.length);
        for (int i = 0; i < v.length; i++) {
          v[i].write(bv, cw);
        }
        break;
    }
    return bv;
  }

  /**
   * Returns value in the format described in JSR-175 for Java source code.
   * 
   * @return value in the format described in JSR-175 for Java source code.
   */

  public String toString () {
    StringBuffer sb = new StringBuffer();
    int tag = getTag();
    switch (tag) {
      case 's':  // pointer to CONSTANT_Utf8
        sb.append('"').append(value).append('"');
        break;

      case 'B':  // pointer to CONSTANT_Byte
      case 'C':  // pointer to CONSTANT_Char
      case 'D':  // pointer to CONSTANT_Double
      case 'F':  // pointer to CONSTANT_Float
      case 'I':  // pointer to CONSTANT_Integer
      case 'J':  // pointer to CONSTANT_Long
      case 'S':  // pointer to CONSTANT_Short
      case 'Z':  // pointer to CONSTANT_Boolean
      case 'e':  // enum_const_value
        sb.append(value);
        break;

      case 'c':  // class_info
        // TODO verify if the following is correct
        sb.append(value);
        break;

      case '@':  // annotation_value
        // TODO verify if the following is correct
        sb.append(value);
        break;

      case '[':  // array_value
        AnnotationElementValue[] v = (AnnotationElementValue[])value;
        if (v.length > 0) {
          sb.append("{ ");
          String sep = "";
          for (int i = 0; i < v.length; i++) {
            sb.append(sep).append(v[i].toString());
            sep = ", ";
          }
          sb.append(" }");
        }
        break;
    }
    return sb.toString();
  }

  /**
   * Container class used to store enum_const_value structure.
   */

  public static class EnumConstValue {

    public String typeName;

    public String constName;

    public EnumConstValue (String typeName, String constName) {
      this.typeName = typeName;
      this.constName = constName;
    }

    public String toString () {
      // TODO verify print enum
      return typeName + "." + constName;
    }
  }
}
