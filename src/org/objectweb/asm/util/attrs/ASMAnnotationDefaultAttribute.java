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

package org.objectweb.asm.util.attrs;

import java.util.List;
import java.util.Map;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.attrs.Annotation;
import org.objectweb.asm.attrs.AnnotationDefaultAttribute;
import org.objectweb.asm.attrs.AnnotationElementValue;
import org.objectweb.asm.attrs.AnnotationElementValue.EnumConstValue;

/**
 * An {@link ASMifiable} {@link AnnotationDefaultAttribute} sub class.
 *
 * @author Eugene Kuleshov
 */

public class ASMAnnotationDefaultAttribute extends AnnotationDefaultAttribute 
  implements ASMifiable  
{

  protected Attribute read (ClassReader cr, int off,
    int len, char[] buf, int codeOff, Label[] labels) 
  {
    AnnotationDefaultAttribute attr = 
      (AnnotationDefaultAttribute)super.read(
        cr, off, len, buf, codeOff, labels);
    
    ASMAnnotationDefaultAttribute result = new ASMAnnotationDefaultAttribute();
    result.defaultValue = attr.defaultValue;
    return result;
  }
   
  public void asmify (StringBuffer buf, String varName, Map labelNames) {
    buf.append("AnnotationDefaultAttribute ").append(varName)
      .append(" = new AnnotationDefaultAttribute();\n");
    asmify(defaultValue, buf, varName + "Val");
    buf.append(varName).append(".defaultValue = ")
      .append(varName).append("Val;\n");
  }
  
  static void asmifyAnnotations (StringBuffer buf, String varName, List annotations) {
    if (annotations.size() > 0) {
      buf.append("{\n");
      for (int i = 0; i < annotations.size(); i++) {
        String val = varName + "ann" + i;
        asmify((Annotation)annotations.get(i), buf, val);
        buf.append(varName).append(".annotations.add( ").append(val).append(");\n");
      }
      buf.append("}\n");
    }
  }

  static void asmifyParameterAnnotations (StringBuffer buf, String varName, List parameters) {
    if (parameters.size() > 0) {
      buf.append("{\n");
      for (int i = 0; i < parameters.size(); i++) {
        String val = varName + "param" + i;
        buf.append( "List ").append( val).append( " = new ArrayList();\n");
        List annotations = (List)parameters.get(i);
        if (annotations.size() > 0) {
          buf.append("{\n");
          for (int i1 = 0; i1 < annotations.size(); i1++) {
            String val1 = val + "ann" + i1;
            asmify((Annotation)annotations.get(i1), buf, val1);
            buf.append(val).append(".add( ").append(val1).append(");\n");
          }
          buf.append("}\n");
        }
        buf.append(varName).append(".parameters.add( ").append(val).append(");\n\n");
      }
      buf.append("}\n");
    }
  }

  static void asmify (Annotation a, StringBuffer buf, String varName) {
    buf.append("Annotation ").append(varName).append(" = new Annotation();\n");
    buf.append(varName).append(".type = \"").append(a.type).append("\";\n");
    List elementValues = a.elementValues;
    if (elementValues.size() > 0) {
      buf.append("{\n");
      for (int i = 0; i < elementValues.size(); i++) {
        Object[] values = (Object[])elementValues.get(i);
        String val = varName + "val" + i;
        asmify((AnnotationElementValue)values[1], buf, val);
        buf.append(varName).append(".add( \"")
          .append(values[0]).append("\", ").append(val).append(");\n");
      }
      buf.append("}\n");
    }
  }

  static void asmify (AnnotationElementValue val, StringBuffer buf, String valName) {
    int tag = val.getTag();
    Object value = val.getValue();
    String objName = valName.concat("obj");
    switch (tag) {
      case 'B':  // pointer to CONSTANT_Byte
        buf.append("Object ").append(objName)
          .append(" = new Byte(").append(value).append(");\n");
        break;

      case 'C':  // pointer to CONSTANT_Char
        buf.append("Object ").append(objName)
          .append(" = new Character((char)").append(value).append(");\n");
        break;

      case 'D':  // pointer to CONSTANT_Double
        buf.append("Object ").append(objName)
          .append(" = new Double((double)").append(value).append(");\n");
        break;

      case 'F':  // pointer to CONSTANT_Float
        buf.append("Object ").append(objName)
          .append(" = new Float((float)").append(value).append(");\n");
        break;

      case 'I':  // pointer to CONSTANT_Integer
        buf.append("Object ").append(objName)
          .append(" = new Integer((int)").append(value).append(");\n");
        break;

      case 'J':  // pointer to CONSTANT_Long
        buf.append("Object ").append(objName)
          .append(" = new Long((long)").append(value).append(");\n");
        break;

      case 'S':  // pointer to CONSTANT_Short
        buf.append("Object ").append(objName)
          .append(" = new Short((short)").append(value).append(");\n");
        break;

      case 'Z':  // pointer to CONSTANT_Boolean
        buf.append("Object ").append(objName)
          .append(" = new Boolean(").append(value).append(");\n");
        break;

      case 's':  // pointer to CONSTANT_Utf8
        buf.append("Object ").append(objName)
          .append(" = \"").append(value).append("\";\n");
        break;

      case 'e':  // enum_const_value
        EnumConstValue e = (EnumConstValue)value;
        buf.append("Object ").append(objName)
          .append(" = new AnnotationElementValue.EnumConstValue(\"")
          .append(e.typeName).append("\", \"").append(e.constName)
          .append("\"));\n");
        break;

      case 'c':  // class_info
        Type t = (Type)value;
        buf.append("Object ").append(objName).
          append(" = Type.getType(\"" + t.getDescriptor() + "\");\n");
        break;

      case '@':  // annotation_value
        asmify((Annotation)value, buf, objName);
        break;

      case '[':  // array_value
        AnnotationElementValue[] v = (AnnotationElementValue[])value;
        buf.append("AnnotationElementValue[] ").append(objName)
          .append(" = new AnnotationElementValue[")
          .append(v.length).append("]\n;");
        buf.append("{\n");
        buf.append("Object av = null;\n");
        for (int i = 0; i < v.length; i++) {
          asmify(v[i], buf, objName + i);
          buf.append(objName)
            .append("[").append(i).append("] = ").append(objName + i);
        }
        buf.append("};\n");
        break;
    }

    buf.append("AnnotationElementValue ").append(valName);
    buf.append(" = new AnnotationElementValue( ").append(objName).append(");\n");
  }
}
