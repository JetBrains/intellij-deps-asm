/* $Id: LocalVariableTypeTableAttribute.java,v 1.1 2004-03-04 01:34:05 ekuleshov Exp $ */

package org.objectweb.asm.attrs;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;

/**
 * The LocalVariableTypeTable attribute is an optional variable-length attribute of a Code
 * attribute. It may be used by debuggers to determine the value of a given
 * local variable during the execution of a method. If LocalVariableTypeTable attributes
 * are present in the attributes table of a given Code attribute, then they may appear in
 * any order. There may be no more than one LocalVariableTypeTable attribute per local
 * variable in the Code attribute.
 * 
 * The LocalVariableTypeTable attribute differs from the LocalVariableTable attribute in that
 * it provides signature information rather than descriptor information. This difference
 * is only significant for variables whose type is a generic reference type. Such
 * variables will appear in both tables, while variables of other types will appear only
 * in LocalVariableTable.
 * <p>
 * The LocalVariableTypeTable attribute has the following format:
 * <pre>
 *   LocalVariableTypeTable_attribute {
 *     u2 attribute_name_index;
 *     u4 attribute_length;
 *     u2 local_variable_type_table_length;
 *     { 
 *       u2 start_pc;
 *       u2 length;
 *       u2 name_index;
 *       u2 signature_index;
 *       u2 index;
 *     } local_variable_type_table[local_variable_type_table_length];
 *   }
 * </pre>
 * 
 * The items of the LocalVariableTypeTable_attribute structure are as follows:
 * <dl>
 * <dt>attribute_name_index</dt>
 * <dd>The value of the attribute_name_index item must be a valid index
 * into the constant_pool table a121. The constant_pool entry at that
 * index must be a CONSTANT_Utf8_info (4.5.7) a122 structure
 * representing the string "LocalVariableTypeTable" a123.</dd>
 * 
 * <dt>attribute_length</dt>
 * <dd>The value of the attribute_length item indicates the length of the
 * attribute, excluding the initial six bytes.</dd>
 * 
 * <dt>local_variable_table_length</dt>
 * <dd>The value of the local_variable_table_length item indicates the
 * number of entries in the local_variable_table array.</dd>
 * 
 * <dt>local_variable_table[]</dd>
 * <dd>Each entry in the local_variable_table array indicates a range of code
 * array offsets within which a local variable has a value. It also
 * indicates the index into the local variable array of the current
 * frame at which that local variable can be found. Each entry must
 * contain the following five items:</dd>
 *   <dl>
 *   <dt>start_pc, length</dt>
 *   <dd>The given local variable must have a value at indices into the
 *   code array in the interval [start_pc, start_pc+length), that is,
 *   between start_pc and start_pc+length exclusive. The value of
 *   start_pc must be a valid index into the code array of this Code
 *   attribute and must be the index of the opcode of an
 *   instructiona124. The value of start_pc+length must either be a
 *   valid index into the code array of this Code attribute and be
 *   the index of the opcode of an instruction, or it must be the
 *   first index beyond the end of that code array a125.</dd>
 * 
 *   <dt>name_index, signature_index</dt>
 *   <dd>The value of the name_index item must be a valid index into
 *   the constant_pool table a127. The constant_pool entry at that
 *   index must contain a CONSTANT_Utf8_info structure
 *   a128 representing a valid unqualified name denoting a local variablea128. 
 *   Careful here - do we want any restrictions at all?</dd>
 *   <p>
 *   The value of the signature_index item must be a valid index
 *   into the constant_pool table. The constant_pool entry at that index
 *   must contain a CONSTANT_Utf8_info structure
 *   representing a field type signature encoding the type
 *   of a local variable in the source program.</dd>
 * 
 *   <dt>index</dt>
 *   <dd>The given local variable must be at index in the local variable
 *   array of the current frame. If the local variable at index is of
 *   type double or long, it occupies both index and index+1.</dd>
 *   </dl>
 * </dl>  
 * 
 * @author Eugene Kuleshov
 * 
 */
public class LocalVariableTypeTableAttribute extends Attribute {

  protected LocalVariableTypeTableAttribute() {
    super( "LocalVariableTypeTable");
  }

  protected Attribute read( ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
    // TODO Auto-generated method stub
    return null;
  }

  protected ByteVector write( ClassWriter cw, byte[] code, int len, int maxStack, int maxLocals) {
    // TODO Auto-generated method stub
    return null;
  }

}

