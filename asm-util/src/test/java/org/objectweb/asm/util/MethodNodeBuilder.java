// ASM: a very sm14all and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm.util;

import java.util.Arrays;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * A builder of {@link MethodNode}, to construct test cases for unit tests.
 *
 * @author Eric Bruneton
 */
final class MethodNodeBuilder {

  private final MethodNode methodNode;

  MethodNodeBuilder() {
    this(/* maxStack = */ 10, /* maxLocals = */ 10);
  }

  MethodNodeBuilder(final int maxStack, final int maxLocals) {
    methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "m", "()V", null, null);
    methodNode.maxStack = maxStack;
    methodNode.maxLocals = maxLocals;
    methodNode.visitCode();
  }

  MethodNodeBuilder nop() {
    methodNode.visitInsn(Opcodes.NOP);
    return this;
  }

  MethodNodeBuilder iconst_0() {
    methodNode.visitInsn(Opcodes.ICONST_0);
    return this;
  }

  MethodNodeBuilder vreturn() {
    methodNode.visitInsn(Opcodes.RETURN);
    return this;
  }

  MethodNodeBuilder label(final Label label) {
    methodNode.visitLabel(label);
    return this;
  }

  MethodNodeBuilder go(final Label label) {
    methodNode.visitJumpInsn(Opcodes.GOTO, label);
    return this;
  }

  MethodNodeBuilder jsr(final Label label) {
    methodNode.visitJumpInsn(Opcodes.JSR, label);
    return this;
  }

  MethodNodeBuilder ret(final int varIndex) {
    methodNode.visitVarInsn(Opcodes.RET, varIndex);
    return this;
  }

  MethodNodeBuilder ifne(final Label label) {
    methodNode.visitJumpInsn(Opcodes.IFNE, label);
    return this;
  }

  MethodNodeBuilder frame(final int type, final Object[] local, final Object[] stack) {
    FrameNode frameNode = new FrameNode(Opcodes.F_NEW, 0, null, 0, null);
    frameNode.type = type;
    frameNode.local = local == null ? null : Arrays.asList(local);
    frameNode.stack = stack == null ? null : Arrays.asList(stack);
    methodNode.instructions.add(frameNode);
    return this;
  }

  MethodNode build() {
    methodNode.visitEnd();
    return methodNode;
  }
}
