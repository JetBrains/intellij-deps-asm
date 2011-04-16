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
 * A constant method handle.
 * 
 * @author Remi Forax
 * @author Eric Bruneton
 */
public final class MethodHandle {

    /**
     * Constant method handle tag, should be a value among Opcodes#MH_GETFIELD,
     * Opcodes#MH_GETSTATIC, Opcodes#MH_PUTFIELD, Opcodes#MH_PUTSTATIC,
     * Opcodes#MH_INVOKEVIRTUAL, Opcodes#MH_INVOKESTATIC,
     * Opcodes#MH_INVOKESPECIAL, Opcodes#MH_NEWINVOKESPECIAL and
     * Opcodes#MH_INVOKEINTERFACE.
     */
    final int tag;

    /**
     * Constant method handle owner internal name.
     */
    final String owner;

    /**
     * Constant method handle name. This name is a field name or a method name
     * depending on the tag value.
     */
    final String name;

    /**
     * Constant method handle descriptor. This name is a field descriptor or a
     * method descriptor depending on the tag value.
     */
    final String desc;

    /**
     * Constructs a new constant method handle.
     * 
     * @param tag the kind of this method handle. Must be Opcodes#MH_GETFIELD,
     *        Opcodes#MH_GETSTATIC, Opcodes#MH_PUTFIELD, Opcodes#MH_PUTSTATIC,
     *        Opcodes#MH_INVOKEVIRTUAL, Opcodes#MH_INVOKESTATIC,
     *        Opcodes#MH_INVOKESPECIAL, Opcodes#MH_NEWINVOKESPECIAL or
     *        Opcodes#MH_INVOKEINTERFACE.
     * @param owner the internal name of the field or method owner class.
     * @param name the name of the field or method.
     * @param desc the descriptor of the field or method.
     */
    public MethodHandle(int tag, String owner, String name, String desc) {
        this.tag = tag;
        this.owner = owner;
        this.name = name;
        this.desc = desc;
    }

    /**
     * Returns the kind of this method handle.
     * 
     * @return Opcodes#MH_GETFIELD, Opcodes#MH_GETSTATIC, Opcodes#MH_PUTFIELD,
     *         Opcodes#MH_PUTSTATIC, Opcodes#MH_INVOKEVIRTUAL,
     *         Opcodes#MH_INVOKESTATIC, Opcodes#MH_INVOKESPECIAL,
     *         Opcodes#MH_NEWINVOKESPECIAL or Opcodes#MH_INVOKEINTERFACE.
     */
    public int getTag() {
        return tag;
    }

    /**
     * Returns the internal name of the field or method owner class.
     * 
     * @return the internal name of the field or method owner class.
     */
    public String getOwner() {
        return owner;
    }

    /**
     * Returns the name of the field or method.
     * 
     * @return the name of the field or method.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the descriptor of the field or method.
     * 
     * @return the descriptor of the field or method.
     */
    public String getDesc() {
        return desc;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodHandle)) {
            return false;
        }
        MethodHandle mHandle = (MethodHandle) obj;
        return tag == mHandle.tag && owner.equals(mHandle.owner)
                && name.equals(mHandle.name) && desc.equals(mHandle.desc);
    }

    public int hashCode() {
        return tag + owner.hashCode() * name.hashCode() * desc.hashCode();
    }

    /**
     * Returns the textual representation of this method handle. The textual
     * representation is: <pre>owner '.' name desc ' ' '(' tag ')'</pre>. As
     * this format is fully specifies, it can be parsed if necessary.
     */
    public String toString() {
        return owner + '.' + name + desc + " (" + tag + ')';
    }
}
