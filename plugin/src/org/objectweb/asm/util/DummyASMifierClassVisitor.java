/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package org.objectweb.asm.util;


/**
 * Dummy class to access some protected / private methods/fields from
 * ASMifierClassVisitor 
 * @author Andrei
 */
public class DummyASMifierClassVisitor extends ASMifierClassVisitor {

    /**
     * 
     */
    public DummyASMifierClassVisitor() {
        super(null);
    }

    /**
     * @return current buffer
     */
    public StringBuffer getBuffer(){
        return this.buf;
    }
    
    
    /**
     * @param access
     * @return
     * @see org.objectweb.asm.util.ASMifierClassVisitor#appendAccess(int)
     */
    public String getAccess(int access) {
        getBuffer().setLength(0);
        super.appendAccess(access);
        return getBuffer().toString();
    }
    
    /**
     * Appends a string representation of the given constant to the given buffer.
     * @param buf a string buffer.
     * @param cst an {@link java.lang.Integer Integer},{@link java.lang.Float Float},
     * {@link java.lang.Long Long},{@link java.lang.Double Double}or
     * {@link String String}object. May be <tt>null</tt>.
     */
    public static void appendConstant(final StringBuffer buf,
        final Object cst) {
        ASMifierClassVisitor.appendConstant(buf, cst);
    }    
}
