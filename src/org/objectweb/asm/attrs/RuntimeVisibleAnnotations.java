/* $Id: RuntimeVisibleAnnotations.java,v 1.3 2003-12-02 05:18:37 ekuleshov Exp $ */

package org.objectweb.asm.attrs;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;


/**
 * The RuntimeVisibleAnnotations attribute is a variable length attribute in the
 * attributes table of the ClassFile, field_info, and method_info structures. The
 * RuntimeVisibleAnnotations attribute records runtime-visible Java programming
 * language annotations on the corresponding class, method, or field. Each
 * ClassFile, field_info, and method_info structure may contain at most one
 * RuntimeVisibleAnnotations attribute, which records all the runtime-visible Java
 * programming language annotations on the corresponding program element. The JVM
 * must make these annotations available so they can be returned by the
 * appropriate reflective APIs.
 * <p>
 * The RuntimeVisibleAnnotations attribute has the following format: 
 * <pre>
 *   RuntimeVisibleAnnotations_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 num_annotations;
 *     annotation annotations[num_annotations];
 *   }
 * </pre>
 * The items of the RuntimeVisibleAnnotations structure are as follows: 
 * <dl>
 * <dt>attribute_name_index</dt>
 * <dd>The value of the attribute_name_index item must be a valid index into the
 *     constant_pool table. The constant_pool entry at that index must be a
 *     CONSTANT_Utf8_info structure representing the string
 *     "RuntimeVisibleAnnotations".</dd>
 * <dt>attribute_length</dt>
 * <dd>The value of the attribute_length item indicates the length of the attribute,
 *     excluding the initial six bytes. The value of the attribute_length item is
 *     thus dependent on the number of runtime-visible annotations represented by
 *     the structure, and their values.</dd>
 * <dt>num_annotations</dt>
 * <dd>The value of the num_annotations item gives the number of runtime-visible
 *     annotations represented by the structure. Note that a maximum of 65535
 *     runtime-visible Java programming language annotations may be directly
 *     attached to a program element.</dd>
 * <dt>annotations</dt>
 * <dd>Each value of the annotations table represents a single runtime-visible
 *     {@link Annotation annotation} on a program element.</dd>
 * </dl>
 * 
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=175">JSR 175 : A Metadata Facility for the Java Programming Language</a>
 * 
 * @author Eugene Kuleshov
 */
public class RuntimeVisibleAnnotations extends Attribute {
  public List annotations = new LinkedList();
  
  public RuntimeVisibleAnnotations() {
    super( "RuntimeVisibleAnnotations");
  }

  protected Attribute read( ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
    RuntimeVisibleAnnotations atr = new RuntimeVisibleAnnotations();
    Annotation.readAnnotations( atr.annotations, cr, off, buf);
    return atr;
  }

  protected ByteVector write( ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    return Annotation.writeAnnotations( new ByteVector(), annotations, cw);
  }
  
  public void dump( StringBuffer buf, String varName, Map labelNames) {
    buf.append( "RuntimeVisibleAnnotations ").append( varName).append( " = new RuntimeVisibleAnnotations();\n");
    Annotation.dumpAnnotations( buf, varName, annotations);
  }
  
  /**
   * Returns value in the format described in JSR-175 for Java source code.
   */
  public String toString() {
    return Annotation.stringAnnotations( annotations);
  }
  
}

