/* $Id: Dumpable.java,v 1.1 2003-12-02 04:55:14 ekuleshov Exp $ */
package org.objectweb.asm.attrs;

import java.util.Map;

/**
 * Dumpable interface has to be implemented by the Attribute class
 * in order to support DumpClassVisitor and DumpCodeVisitor.
 * 
 * Implementation should print the ASM code that generates 
 * attribute data structures for current attribute state.   
 * 
 * @author Eugene Kuleshov
 */
public interface Dumpable {
  
  /**
   * Dump attribute data into ASM code.
   * 
   * @param buf A buffer used for printing java code.
   * @param varName name of the variable in a printed code used to store attribute instance.
   * @param labelNames map of label instances to their names.
   */
  void dump( StringBuffer buf, String varName, Map labelNames);

}

