/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
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

import org.objectweb.asm.Opcodes;

/**
 * A node that represents a type annotationn.
 * 
 * @author Eric Bruneton
 */
public class TypeAnnotationNode extends AnnotationNode {

    /**
     * The path to the annotated type. The definition of this path depends on
     * the target of this annotation (i.e. whether it is a class, field, method
     * or instruction). See {@link ClassVisitor#visitTypeAnnotation},
     * {@link FieldVisitor#visitTypeAnnotation},
     * {@link MethodVisitor#visitTypeAnnotation}.
     */
    public int target;

    /**
     * The path to the annotated type argument, wildcard bound, array element
     * type, or static outer type within the target type, seen as a tree. For
     * instance, in <tt>@A Map&lt;@B ? extends @C String, @D List&lt;@E
     * Object&gt;&gt;</tt>, A, B, C, D, E have paths (), (0), (0,0), (1), (1,0)
     * respectively. In <tt>@I String @F [] @G [] @H []</tt> F, G, H, I have
     * paths (), (0), (1), (2) respectively. In <tt>@M O1.@L O2.@K O3.@J
     * NestedStatic</tt> J, K, L, M have paths (), (0), (1), (2) respectively.
     * Paths are stored with the same format as in 'target'.
     */
    public long path;

    /**
     * Constructs a new {@link AnnotationNode}. <i>Subclasses must not use this
     * constructor</i>. Instead, they must use the
     * {@link #AnnotationNode(int, String)} version.
     * 
     * @param desc
     *            the class descriptor of the annotation class.
     */
    public TypeAnnotationNode(final int target, final long path,
            final String desc) {
        this(Opcodes.ASM5, target, path, desc);
    }

    /**
     * Constructs a new {@link AnnotationNode}.
     * 
     * @param api
     *            the ASM API version implemented by this visitor. Must be one
     *            of {@link Opcodes#ASM4} or {@link Opcodes#ASM5}.
     * @param desc
     *            the class descriptor of the annotation class.
     */
    public TypeAnnotationNode(final int api, final int target, final long path,
            final String desc) {
        super(api, desc);
        this.target = target;
        this.path = path;
    }
}
