/* $Id: RuntimeInvisibleParameterAnnotations.java,v 1.2 2003-11-29 06:33:14 ekuleshov Exp $ */

package org.objectweb.asm.attrs;

import java.util.LinkedList;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;


/**
 * The RuntimeInvisibleParameterAnnotations attribute is similar to the
 * RuntimeVisibleParameterAnnotations attribute, except that the annotations
 * represented by a RuntimeInvisibleParameterAnnotations attribute must not be
 * made available for return by reflective APIs, unless the JVM has specifically
 * been instructed to retain these annotations via some implementation-specific
 * mechanism such as a command line flag. In the absence of such instructions, the
 * JVM ignores this attribute.
 * <p>
 * The RuntimeInvisibleParameterAnnotations attribute is a variable length
 * attribute in the attributes table of the method_info structure. The
 * RuntimeInvisibleParameterAnnotations attribute records runtime-invisible Java
 * programming language annotations on the parameters of the corresponding method.
 * Each method_info structure may contain at most one
 * RuntimeInvisibleParameterAnnotations attribute, which records all the
 * runtime-invisible Java programming language annotations on the parameters of
 * the corresponding method.
 * <p>
 * The RuntimeInvisibleParameterAnnotations attribute has the following format: 
 * <pre>
 *   RuntimeInvisibleParameterAnnotations_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u1 num_parameters;
 *     {
 *       u2 num_annotations;
 *       annotation annotations[num_annotations];
 *     } parameter_annotations[num_parameters];
 *   }
 * </pre>
 * The items of the RuntimeInvisibleParameterAnnotations structure are as follows: 
 * <dl>
 * <dt>attribute_name_index</dt>
 * <dd>The value of the attribute_name_index item must be a valid index into the
 *     constant_pool table. The constant_pool entry at that index must be a
 *     CONSTANT_Utf8_info structure representing the string
 *     "RuntimeInvisibleParameterAnnotations".</dd>
 * <dt>attribute_length</dt>
 * <dd>The value of the attribute_length item indicates the length of the attribute,
 *     excluding the initial six bytes. The value of the attribute_length item is
 *     thus dependent on the number of parameters, the number of runtime-invisible
 *     annotations on each parameter, and their values.</dd>
 * <dt>num_parameters</dt>
 * <dd>The value of the num_parameters item gives the number of parameters of the
 *     method represented by the method_info structure on which the annotation
 *     occurs. (This duplicates information that could be extracted from the method
 *     descriptor.)</dd>
 * <dt>parameter_annotations</dt>
 * <dd>Each value of the parameter_annotations table represents all of the
 *     runtime-invisible annotations on a single parameter. The sequence of values
 *     in the table corresponds to the sequence of parameters in the method
 *     signature. Each parameter_annotations entry contains the following two items:
 *     <dl>
 *     <dt>num_annotations</dt>
 *     <dd>The value of the num_annotations item indicates the number of
 *         runtime-invisible annotations on the parameter corresponding to the sequence
 *         number of this parameter_annotations element.</dd>
 *     <dt>annotations</dt>
 *     <dd>Each value of the annotations table represents a single runtime-invisible
 *         {@link Annotation annotation} on the parameter corresponding to the sequence 
 *         number of this parameter_annotations element.</dd>
 *     </dl>
 *     </dd>
 * </dl>
 * 
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=175">JSR 175 : A Metadata Facility for the Java Programming Language</a>
 * 
 * @author Eugene Kuleshov
 */
public class RuntimeInvisibleParameterAnnotations extends Attribute {
  public List parameters = new LinkedList();
  
  public RuntimeInvisibleParameterAnnotations() {
    super( "RuntimeInvisibleParameterAnnotations");
  }

  protected Attribute read(ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
    RuntimeInvisibleParameterAnnotations atr = new RuntimeInvisibleParameterAnnotations();
    Annotation.readParameterAnnotations( atr.parameters, cr, off, buf);
    return atr;
  }

  protected ByteVector write(ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    return Annotation.writeParametersAnnotations( new ByteVector(), parameters, cw);
  }

  public String toString() {
    return Annotation.stringParameterAnnotations( parameters);
  }
  
}

