/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
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

package org.objectweb.asm;

/**
 * A visitor to visit a stack map frame. The methods of this interface must be
 * called for each local variable and for each operand stack element in the
 * visited stack map frame, in sequential order (local variables must be visited
 * first, before operand stack elements). Local variables <i>and</i> operand
 * stack elements of type long and double must be visited with a single call to
 * {@link #visitPrimitiveType}; the next slot, of type TOP, is implicit and
 * must not be visited.
 * 
 * @author Eric Bruneton
 */
public interface FrameVisitor {

    /**
     * The type of uninitialized or invalid values.
     */
    int TOP = 0;

    /**
     * The type of boolean, byte, char, short, and int values.
     */
    int INTEGER = 1;

    /**
     * The type of float values.
     */
    int FLOAT = 2;

    /**
     * The type of double values.
     */
    int DOUBLE = 3;

    /**
     * The type of long values.
     */
    int LONG = 4;

    /**
     * The type of the <tt>null</tt> value.
     */
    int NULL = 5;

    /**
     * The type of <tt>this</tt> in constructors, until the super constructor
     * is called.
     */
    int UNINITIALIZED_THIS = 6;

    /**
     * Visits a local variable or operand stack element whose type is
     * "primitive".
     * 
     * @param type the type of the visited local variable or operand stack
     *        element. This type must be one of the constants defined in this
     *        interface.
     */
    void visitPrimitiveType(int type);

    /**
     * Visits a local variable or operand stack element whose type is a
     * reference type.
     * 
     * @param type the internal name (or, for array types, the type descriptor)
     *        of the class of the visited local variable or operand stack
     *        element.
     */
    void visitReferenceType(String type);

    /**
     * Visits a local variable or operand stack element whose value is an
     * uninitialized, newly created object.
     * 
     * @param newInsn a label that designates the NEW instruction that created
     *        this uninitialized object.
     */
    void visitUninitializedType(Label newInsn);
}
