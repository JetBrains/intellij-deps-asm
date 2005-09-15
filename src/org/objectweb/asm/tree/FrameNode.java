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
package org.objectweb.asm.tree;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.FrameVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class FrameNode extends AbstractInsnNode implements FrameVisitor {

    public final List locals;

    public final List stack;

    private int nLocal;
    
    public FrameNode(final int nLocal, final int nStack) {
        super(-1);
        this.locals = new ArrayList(nLocal);
        this.stack = new ArrayList(nStack);
        this.nLocal = nLocal;
    }

    public void visitPrimitiveType(final int type) {
        if (nLocal > 0) {
            locals.add(new Integer(type));
            --nLocal;
        } else {
            stack.add(new Integer(type));
        }
    }

    public void visitReferenceType(final String type) {
        if (nLocal > 0) {
            locals.add(type);
            --nLocal;
        } else {
            stack.add(type);
        }
    }

    public void visitUninitializedType(final Label newInsn) {
        if (nLocal > 0) {
            locals.add(newInsn);
            --nLocal;
        } else {
            stack.add(newInsn);
        }
    }
    
    public void accept(final MethodVisitor mv) {
        FrameVisitor fv = mv.visitFrame(locals.size(), stack.size());
        for (int i = 0; i < locals.size(); ++i) {
            accept(fv, locals.get(i));
        }
        for (int i = 0; i < stack.size(); ++i) {
            accept(fv, stack.get(i));
        }
    }

    private static void accept(final FrameVisitor fv, final Object type) {
        if (type instanceof Integer) {
            fv.visitPrimitiveType(((Integer) type).intValue());
        } else if (type instanceof String) {
            fv.visitReferenceType((String) type);
        } else {
            fv.visitUninitializedType((Label) type);
        }
    }

    public int getType() {
        return FRAME;
    }
}
