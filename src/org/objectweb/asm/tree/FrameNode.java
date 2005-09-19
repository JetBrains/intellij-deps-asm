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
import org.objectweb.asm.Opcodes;

/**
 * A node that represents a stack map frame. These nodes are pseudo instruction
 * nodes in order to be inserted in an instruction list. In fact these nodes
 * must(*) be inserted <i>just before</i> any instruction node <b>i</b> that
 * follows an unconditionnal branch instruction such as GOTO or THROW, that is
 * the target of a jump instruction, or that starts an exception handler block.
 * The stack map frame types must describe the values of the local variables and
 * of the operand stack elements <i>just before</i> <b>i</b> is executed. <br>
 * <br> (*) this is mandatory only for classes whose version is greater than or
 * equal to {@link Opcodes#V1_6 V1_6}.
 * 
 * @author Eric Bruneton
 */
public class FrameNode extends AbstractInsnNode implements FrameVisitor {

    /**
     * The types of the local variables of this stack map frame. Elements of
     * this list can be Integer, String or Label objects (for primitive,
     * reference and uninitialized types respectively - see {@link FrameVisitor}).
     */
    public final List locals;

    /**
     * The types of the operand stack elements of this stack map frame. Elements
     * of this list can be Integer, String or Label objects (for primitive,
     * reference and uninitialized types respectively - see {@link FrameVisitor}).
     */
    public final List stack;

    /**
     * Number of remaining local variable types to be visited.
     */
    private int nLocal;

    private final static Integer[] TYPES = {
        new Integer(0),
        new Integer(1),
        new Integer(2),
        new Integer(3),
        new Integer(4),
        new Integer(5),
        new Integer(6)
    };

    /**
     * Constructs a new {@link FrameNode}.
     * 
     * @param nLocal number of local variables of this stack map frame.
     * @param nStack number of operand stack elements of this stack map frame.
     */
    public FrameNode(final int nLocal, final int nStack) {
        super(-1);
        this.locals = new ArrayList(nLocal);
        this.stack = new ArrayList(nStack);
        this.nLocal = nLocal;
    }

    public void visitPrimitiveType(final int type) {
        if (nLocal > 0) {
            locals.add(TYPES[type]);
            --nLocal;
        } else {
            stack.add(TYPES[type]);
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

    /**
     * Makes the given visitor visit this stack map frame.
     * 
     * @param mv a method visitor.
     */
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
