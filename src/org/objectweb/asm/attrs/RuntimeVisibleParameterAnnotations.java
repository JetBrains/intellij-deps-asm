/* $Id: RuntimeVisibleParameterAnnotations.java,v 1.3 2003-12-02 05:18:37 ekuleshov Exp $ */

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
 * The RuntimeVisibleParameterAnnotations attribute is a variable length attribute
 * in the attributes table of the method_info structure. The
 * RuntimeVisibleParameterAnnotations attribute records runtime-visible Java
 * programming language annotations on the parameters of the corresponding method.
 * Each method_info structure may contain at most one
 * RuntimeVisibleParameterAnnotations attribute, which records all the
 * runtime-visible Java programming language annotations on the parameters of the
 * corresponding method. The JVM must make these annotations available so they can
 * be returned by the appropriate reflective APIs.
 * <p>
 * The RuntimeVisibleParameterAnnotations attribute has the following format: 
 * <pre>
 *   RuntimeVisibleParameterAnnotations_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u1 num_parameters;
 *     {
 *       u2 num_annotations;
 *       annotation annotations[num_annotations];
 *     } parameter_annotations[num_parameters];
 *   }
 * <pre>
 * The items of the RuntimeVisibleParameterAnnotations structure are as follows: 
 * <dl>
 * <dt>attribute_name_index</dt>
 * <dd>The value of the attribute_name_index item must be a valid index into the
 *     constant_pool table. The constant_pool entry at that index must be a
 *     CONSTANT_Utf8_info structure representing the string
 *     "RuntimeVisibleParameterAnnotations".</dd>
 * <dt>attribute_length</dt>
 * <dd>The value of the attribute_length item indicates the length of the attribute,
 *     excluding the initial six bytes. The value of the attribute_length item is
 *     thus dependent on the number of parameters, the number of runtime-visible
 *     annotations on each parameter, and their values.</dd>
 * <dt>num_parameters</dt>
 * <dd>The value of the num_parameters item gives the number of parameters of the
 *     method represented by the method_info structure on which the annotation
 *     occurs. (This duplicates information that could be extracted from the method
 *     descriptor.)</dd>
 * <dt>parameter_annotations</dt>
 * <dd>Each value of the parameter_annotations table represents all of the
 *     runtime-visible annotations on a single parameter. The sequence of values in
 *     the table corresponds to the sequence of parameters in the method signature.
 *     Each parameter_annotations entry contains the following two items:</dd>
 *     <dl>
 *     <dt>num_annotations</dt>
 *     <dd>The value of the num_annotations item indicates the number of runtime-visible
 *         annotations on the parameter corresponding to the sequence number of this
 *         parameter_annotations element.</dd>
 *     <dt>annotations</dt>
 *     <dd>Each value of the annotations table represents a single runtime-visible
 *         {@link org.objectweb.asm.attrs.Annotation annotation} on the parameter 
 *         corresponding to the sequence number of this parameter_annotations element.</dd>
 *     </dl>
 *     </dd>
 * </dl>
 * 
 * @see <a href="http://www.jcp.org/en/jsr/detail?id=175">JSR 175 : A Metadata Facility for the Java Programming Language</a>
 * 
 * @author Eugene Kuleshov
 */
public class RuntimeVisibleParameterAnnotations extends Attribute implements Dumpable {
  public List parameters = new LinkedList();
  
  public RuntimeVisibleParameterAnnotations() {
    super( "RuntimeVisibleParameterAnnotations");
  }

  protected Attribute read( ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
    RuntimeInvisibleParameterAnnotations atr = new RuntimeInvisibleParameterAnnotations();
    Annotation.readParameterAnnotations( atr.parameters, cr, off, buf);
    return atr;
  }

  protected ByteVector write( ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    return Annotation.writeParametersAnnotations( new ByteVector(), parameters, cw);
  }

  public void dump( StringBuffer buf, String varName, Map labelNames) {
    buf.append( "RuntimeVisibleParameterAnnotations ").append( varName).append( " = new RuntimeVisibleParameterAnnotations();\n");
    Annotation.dumpParameterAnnotations( buf, varName, parameters);
  }
  
  public String toString() {
    return Annotation.stringParameterAnnotations( parameters);
  }
  
}

