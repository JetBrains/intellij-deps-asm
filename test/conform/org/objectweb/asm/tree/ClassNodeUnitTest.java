/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

import junit.framework.TestCase;

/**
 * ClassNode unit tests.
 * 
 * @author Eric Bruneton
 */
public class ClassNodeUnitTest extends TestCase implements Opcodes {

    public void testFrameNode() {
        FrameNode fn = new FrameNode(F_SAME, 0, null, 0, null);
        assertEquals(fn.getType(), AbstractInsnNode.FRAME);
    }
    
    public void testInsnNode() {
        InsnNode in = InsnNode.getByOpcode(NOP);
        assertEquals(in.getOpcode(), NOP);
        assertEquals(in.getType(), AbstractInsnNode.INSN);
    }

    public void testIntInsnNode() {
        IntInsnNode iin = new IntInsnNode(BIPUSH, 0);
        iin.setOpcode(SIPUSH);
        assertEquals(iin.getOpcode(), SIPUSH);
        assertEquals(iin.getType(), AbstractInsnNode.INT_INSN);
    }

    public void testVarInsnNode() {
        VarInsnNode vn = new VarInsnNode(ALOAD, 0);
        vn.setOpcode(ASTORE);
        assertEquals(vn.getOpcode(), ASTORE);
        assertEquals(vn.getType(), AbstractInsnNode.VAR_INSN);
    }

    public void testTypeInsnNode() {
        TypeInsnNode tin = new TypeInsnNode(NEW, "java/lang/Object");
        tin.setOpcode(CHECKCAST);
        assertEquals(tin.getOpcode(), CHECKCAST);
        assertEquals(tin.getType(), AbstractInsnNode.TYPE_INSN);
    }

    public void testFieldInsnNode() {
        FieldInsnNode fn = new FieldInsnNode(GETSTATIC, "owner", "name", "I");
        fn.setOpcode(PUTSTATIC);
        assertEquals(fn.getOpcode(), PUTSTATIC);
        assertEquals(fn.getType(), AbstractInsnNode.FIELD_INSN);
    }

    public void testMethodInsnNode() {
        MethodInsnNode mn = new MethodInsnNode(INVOKESTATIC,
                "owner",
                "name",
                "I");
        mn.setOpcode(INVOKESPECIAL);
        assertEquals(mn.getOpcode(), INVOKESPECIAL);
        assertEquals(mn.getType(), AbstractInsnNode.METHOD_INSN);
    }

    public void testJumpInsnNode() {
        JumpInsnNode jn = new JumpInsnNode(GOTO, new Label());
        jn.setOpcode(IFEQ);
        assertEquals(jn.getOpcode(), IFEQ);
        assertEquals(jn.getType(), AbstractInsnNode.JUMP_INSN);
    }

    public void testLabelNode() {
        LabelNode ln = new LabelNode(new Label());
        assertEquals(ln.getType(), AbstractInsnNode.LABEL);
    }

    public void testIincInsnNode() {
        IincInsnNode iincn = new IincInsnNode(1, 1);
        assertEquals(iincn.getType(), AbstractInsnNode.IINC_INSN);
    }

    public void testLdcInsnNode() {
        LdcInsnNode ldcn = new LdcInsnNode("s");
        assertEquals(ldcn.getType(), AbstractInsnNode.LDC_INSN);
    }

    public void testLookupSwitchInsnNode() {
        LookupSwitchInsnNode lsn = new LookupSwitchInsnNode(null, null, null);
        assertEquals(lsn.getType(), AbstractInsnNode.LOOKUPSWITCH_INSN);
    }

    public void testTableSwitchInsnNode() {
        TableSwitchInsnNode tsn = new TableSwitchInsnNode(0, 1, null, null);
        assertEquals(tsn.getType(), AbstractInsnNode.TABLESWITCH_INSN);
    }

    public void testMultiANewArrayInsnNode() {
        MultiANewArrayInsnNode manan = new MultiANewArrayInsnNode("[[I", 2);
        assertEquals(manan.getType(), AbstractInsnNode.MULTIANEWARRAY_INSN);
    }
}
