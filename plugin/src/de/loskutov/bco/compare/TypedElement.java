/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/

package de.loskutov.bco.compare;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.swt.graphics.Image;

import de.loskutov.bco.asm.AsmUtils;
import de.loskutov.bco.asm.DecompileResult;
import de.loskutov.bco.ui.JdtUtils;

/**
 * @author Andrei
 */
public class TypedElement extends BufferedContent
    implements
        ITypedElement,
        IStructureComparator {

    private String name;
    private String type;
    private IJavaElement element;
    private boolean isASMifierMode;
    /** used by Eclipse to recognize appropriated viewer */
    public static final String TYPE_BYTECODE = "bytecode"; //$NON-NLS-1$
    /** used by Eclipse to recognize appropriated viewer */
    public static final String TYPE_ASM_IFIER = "java"; //$NON-NLS-1$
    
    /**
     * Constructor for TypedElement.
     */
    public TypedElement() {
        super();
    }    
    
    /**
     * @return Returns the isASMifierMode.
     */
    protected boolean isASMifierMode() {
        return isASMifierMode;
    }
    
    /**
     * @param isASMifierMode The isASMifierMode to set.
     */
    protected void setASMifierMode(boolean isASMifierMode) {
        this.isASMifierMode = isASMifierMode;
    }

    /**
     * Constructor for TypedElement.
     * @param name
     * @param type
     * @param element
     */
    public TypedElement(String name, String type, IJavaElement element) {
        this();
        this.name = name;
        this.type = type;
        this.element = element;        
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getName()
     */
    public String getName() {
        return name;
    }
    
    /**
     * @param name The name to set.
     */
    protected void setName(String name) {
        this.name = name;
    }
    
    /**
     * @param type The type to set.
     */
    protected void setType(String type) {
        this.type = type;
    }
    
    /**
     * @return name
     */
    public String getElementName() {
        return JdtUtils.getElementName(element);
    }    

    /**
     * @see org.eclipse.compare.ITypedElement#getImage()
     */
    public Image getImage() {
        // default image for .class files
        return CompareUI.getImage("class"); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.compare.ITypedElement#getType()
     */
    public String getType() {
        return type;
    }

    /**
     * @see org.eclipse.compare.structuremergeviewer.IStructureComparator#getChildren()
     */
    public Object[] getChildren() {
        return new TypedElement[0];
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.compare.BufferedContent#createStream()
     */
    protected InputStream createStream() throws CoreException {
        InputStream stream = JdtUtils.createInputStream(element); 
        if(stream == null){
            throw new CoreException(new Status(IStatus.ERROR, "de.loskutov.bco", //$NON-NLS-1$
                -1, "cannot get bytecode from class file", null) );
        }
        DecompileResult decompileResult = null;
        try {
            decompileResult = AsmUtils.getFullBytecode(stream, true, isASMifierMode());            
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            throw new CoreException(new Status(IStatus.ERROR, "de.loskutov.bco", //$NON-NLS-1$
                -1, "cannot get bytecode dump", null) );            
        }
        
        String decompiledCode = decompileResult.getDecompiledCode();
        decompiledCode = decompiledCode == null? "" : decompiledCode; //$NON-NLS-1$
        
        StringBufferInputStream sbi = new StringBufferInputStream(decompiledCode);        
        return sbi;
    }

}