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
package org.objectweb.asm.commons;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@link MethodAdapter} that renumbers local variables in their order of
 * appearance. This adapter allows one to easily add new local variables to a
 * method. {@link ClassWriter#COMPUTE_MAXS} <i>must be set to</i> <tt>true</tt> 
 * <i>when this adapter is used</i>.
 * 
 * @author Chris Nokleberg
 */
public class LocalVariablesSorter extends MethodAdapter {

    private Map locals = new HashMap();

    protected final int firstLocal;

    private int nextLocal;

    public LocalVariablesSorter(
        final int access,
        final String desc,
        final MethodVisitor mv)
    {
        super(mv);
        Type[] args = Type.getArgumentTypes(desc);
        nextLocal = ((Opcodes.ACC_STATIC & access) != 0) ? 0 : 1;
        for (int i = 0; i < args.length; i++) {
            nextLocal += args[i].getSize();
        }
        firstLocal = nextLocal;
    }

    public void visitVarInsn(final int opcode, final int var) {
        int size;
        switch (opcode) {
            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                size = 2;
                break;
            default:
                size = 1;
        }
        mv.visitVarInsn(opcode, remap(var, size));
    }

    public void visitIincInsn(final int var, final int increment) {
        mv.visitIincInsn(remap(var, 1), increment);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        mv.visitMaxs(0, 0);
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        mv.visitLocalVariable(name, desc, signature, start, end, remap(index));
    }

    // -------------

    protected int newLocal(final int size) {
        int var = nextLocal;
        nextLocal += size;
        return var;
    }

    private int remap(final int var, final int size) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(size == 2 ? ~var : var);
        Integer value = (Integer) locals.get(key);
        if (value == null) {
            value = new Integer(newLocal(size));
            locals.put(key, value);
        }
        return value.intValue();
    }

    private int remap(final int var) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(var);
        Integer value = (Integer) locals.get(key);
        if (value == null) {
            key = new Integer(~var);
            value = (Integer) locals.get(key);
            if (value == null) {
                throw new IllegalStateException("Unknown local variable " + var);
            }
        }
        return value.intValue();
    }
}
