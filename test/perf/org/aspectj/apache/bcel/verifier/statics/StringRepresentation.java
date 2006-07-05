package org.aspectj.apache.bcel.verifier.statics;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache BCEL" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache BCEL", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import org.aspectj.apache.bcel.classfile.*;
import org.aspectj.apache.bcel.classfile.Deprecated;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeInvisibleParameterAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleAnnotations;
import org.aspectj.apache.bcel.classfile.annotation.RuntimeVisibleParameterAnnotations;

/**
 * BCEL's Node classes (those from the classfile API that <B>accept()</B>
 * Visitor instances) have <B>toString()</B> methods that were not designed to
 * be robust, this gap is closed by this class. When performing class file
 * verification, it may be useful to output which entity (e.g. a <B>Code</B>
 * instance) is not satisfying the verifier's constraints, but in this case it
 * could be possible for the <B>toString()</B> method to throw a
 * RuntimeException. A (new StringRepresentation(Node n)).toString() never
 * throws any exception. Note that this class also serves as a placeholder for
 * more sophisticated message handling in future versions of JustIce.
 * 
 * @version $Id: StringRepresentation.java,v 1.1.2.1 2005/09/16 07:19:39
 *          ebruneton Exp $
 * @author <A HREF="http://www.inf.fu-berlin.de/~ehaase"/>Enver Haase</A>
 */
public class StringRepresentation extends
        org.apache.bcel.classfile.EmptyVisitor implements Visitor
{
    /**
     * The string representation, created by a visitXXX() method, output by
     * toString().
     */
    private String tostring;

    /**
     * The node we ask for its string representation. Not really needed; only
     * for debug output.
     */
    // private Node n;
    /**
     * Creates a new StringRepresentation object which is the representation of
     * n.
     * 
     * @param n
     * 
     * @see #toString()
     */
    public StringRepresentation(final Node n) {
        // this.n = n;
        n.accept(this); // assign a string representation to field 'tostring' if
        // we know n's class.
    }

    /**
     * Returns the String representation.
     * 
     * @return TODO
     */
    public String toString() {
        // The run-time check below is needed because we don't want to omit
        // inheritance
        // of "EmptyVisitor" and provide a thousand empty methods.
        // However, in terms of performance this would be a better idea.
        // If some new "Node" is defined in BCEL (such as some concrete
        // "Attribute"), we
        // want to know that this class has also to be adapted.
        if (tostring == null) {
            tostring = ""; // throw new AssertionViolatedException("Please
            // adapt '"+getClass()+"' to deal with objects of
            // class '"+n.getClass()+"'.");
        }
        return tostring;
    }

    /**
     * Returns the String representation of the Node object obj; this is
     * obj.toString() if it does not throw any RuntimeException, or else it is a
     * string derived only from obj's class name.
     * 
     * @param obj
     * @return TODO
     */
    private String toString(final Node obj) {
        String ret;
        try {
            ret = obj.toString();
        } catch (RuntimeException e) {
            String s = obj.getClass().getName();
            s = s.substring(s.lastIndexOf(".") + 1);
            ret = "<<" + s + ">>";
        } catch (ClassFormatError e) { /*
                                         * BCEL can be harsh e.g. trying to
                                         * convert the "signature" of a
                                         * ReturnaddressType LocalVariable
                                         * (shouldn't occur, but people do crazy
                                         * things)
                                         */
            String s = obj.getClass().getName();
            s = s.substring(s.lastIndexOf(".") + 1);
            ret = "<<" + s + ">>";
        }
        return ret;
    }

    // //////////////////////////////
    // Visitor methods start here //
    // //////////////////////////////
    // We don't of course need to call some default implementation:
    // e.g. we could also simply output "Code" instead of a possibly
    // lengthy Code attribute's toString().
    public void visitCode(final Code obj) {
        // tostring = toString(obj);
        tostring = "<CODE>"; // We don't need real code outputs.
    }

    public void visitCodeException(final CodeException obj) {
        tostring = toString(obj);
    }

    public void visitConstantClass(final ConstantClass obj) {
        tostring = toString(obj);
    }

    public void visitConstantDouble(final ConstantDouble obj) {
        tostring = toString(obj);
    }

    public void visitConstantFieldref(final ConstantFieldref obj) {
        tostring = toString(obj);
    }

    public void visitConstantFloat(final ConstantFloat obj) {
        tostring = toString(obj);
    }

    public void visitConstantInteger(final ConstantInteger obj) {
        tostring = toString(obj);
    }

    public void visitConstantInterfaceMethodref(
        final ConstantInterfaceMethodref obj)
    {
        tostring = toString(obj);
    }

    public void visitConstantLong(final ConstantLong obj) {
        tostring = toString(obj);
    }

    public void visitConstantMethodref(final ConstantMethodref obj) {
        tostring = toString(obj);
    }

    public void visitConstantNameAndType(final ConstantNameAndType obj) {
        tostring = toString(obj);
    }

    public void visitConstantPool(final ConstantPool obj) {
        tostring = toString(obj);
    }

    public void visitConstantString(final ConstantString obj) {
        tostring = toString(obj);
    }

    public void visitConstantUtf8(final ConstantUtf8 obj) {
        tostring = toString(obj);
    }

    public void visitConstantValue(final ConstantValue obj) {
        tostring = toString(obj);
    }

    public void visitDeprecated(final Deprecated obj) {
        tostring = toString(obj);
    }

    public void visitExceptionTable(final ExceptionTable obj) {
        tostring = toString(obj);
    }

    public void visitField(final Field obj) {
        tostring = toString(obj);
    }

    public void visitInnerClass(final InnerClass obj) {
        tostring = toString(obj);
    }

    public void visitInnerClasses(final InnerClasses obj) {
        tostring = toString(obj);
    }

    public void visitJavaClass(final JavaClass obj) {
        tostring = toString(obj);
    }

    public void visitLineNumber(final LineNumber obj) {
        tostring = toString(obj);
    }

    public void visitLineNumberTable(final LineNumberTable obj) {
        tostring = "<LineNumberTable: " + toString(obj) + ">";
    }

    public void visitLocalVariable(final LocalVariable obj) {
        tostring = toString(obj);
    }

    public void visitLocalVariableTable(final LocalVariableTable obj) {
        tostring = "<LocalVariableTable: " + toString(obj) + ">";
    }

    public void visitMethod(final Method obj) {
        tostring = toString(obj);
    }

    public void visitSignature(final Signature obj) {
        tostring = toString(obj);
    }

    public void visitSourceFile(final SourceFile obj) {
        tostring = toString(obj);
    }

    public void visitStackMap(final StackMap obj) {
        tostring = toString(obj);
    }

    public void visitSynthetic(final Synthetic obj) {
        tostring = toString(obj);
    }

    public void visitUnknown(final Unknown obj) {
        tostring = toString(obj);
    }

    public void visitStackMapEntry(final StackMapEntry arg0) {
        tostring = "";
    }

    public void visitEnclosingMethod(final EnclosingMethod arg0) {
        tostring = "";
    }

    public void visitRuntimeVisibleAnnotations(
        final RuntimeVisibleAnnotations arg0)
    {
        tostring = "";
    }

    public void visitRuntimeInvisibleAnnotations(
        final RuntimeInvisibleAnnotations arg0)
    {
        tostring = "";
    }

    public void visitRuntimeVisibleParameterAnnotations(
        final RuntimeVisibleParameterAnnotations arg0)
    {
        tostring = "";
    }

    public void visitRuntimeInvisibleParameterAnnotations(
        final RuntimeInvisibleParameterAnnotations arg0)
    {
        tostring = "";
    }

    public void visitAnnotationDefault(final AnnotationDefault arg0) {
        tostring = "";
    }

    public void visitLocalVariableTypeTable(final LocalVariableTypeTable arg0) {
        tostring = "";
    }
}
