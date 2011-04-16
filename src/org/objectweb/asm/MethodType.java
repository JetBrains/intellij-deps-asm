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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * A method type.
 * 
 * @author Remi Forax
 * @author Eric Bruneton
 */
public final class MethodType {

    /**
     * The descriptor of methods of this type.
     */
    final String desc;

    /**
     * Creates a method type from a method descriptor.
     * 
     * @param desc a method descriptor
     */
    public MethodType(String desc) {
        this.desc = desc;
    }

    /**
     * Creates a method type from argument and return types.
     * 
     * @param returnType the return type of the method.
     * @param argumentTypes the argument types of the method.
     */
    public MethodType(final Type returnType, final Type[] argumentTypes) {
        this.desc = Type.getMethodDescriptor(returnType, argumentTypes);
    }

    /**
     * Creates a method type from the given constructor.
     * 
     * @param c a {@link Constructor Constructor} object.
     */
    public MethodType(final Constructor<?> c) {
        this.desc = Type.getConstructorDescriptor(c);
    }

    /**
     * Creates a method type from the given method.
     * 
     * @param m a {@link Method Method} object.
     */
    public MethodType(final Method m) {
        this.desc = Type.getMethodDescriptor(m);
    }

    /**
     * Returns the descriptor of methods of this type.
     * 
     * @return the descriptor of methods of this type.
     */
    public String getDescriptor() {
        return desc;
    }

    /**
     * Returns the argument types of methods of this type.
     * 
     * @return the argument types of methods of this type.
     */
    public Type[] getArgumentTypes() {
        return Type.getArgumentTypes(desc);
    }

    /**
     * Returns the return type of methods of this type.
     * 
     * @return the return type of methods of this type.
     */
    public Type getReturnType() {
        return Type.getReturnType(desc);
    }

    /**
     * Returns the size of the arguments and of the return value of methods of
     * this type.
     * 
     * @return the size of the arguments (plus one for the implicit this
     *         argument), argSize, and the size of the return value, retSize,
     *         packed into a single int i = <tt>(argSize << 2) | retSize</tt>
     *         (argSize is therefore equal to <tt>i >> 2</tt>, and retSize to
     *         <tt>i & 0x03</tt>).
     */
    public int getArgumentsAndReturnSizes() {
        return Type.getArgumentsAndReturnSizes(desc);
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodType)) {
            return false;
        }
        return desc.equals(((MethodType) obj).desc);
    }

    public int hashCode() {
        return ~desc.hashCode();
    }

    public String toString() {
        return desc;
    }
}
