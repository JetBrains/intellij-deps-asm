// ASM: a very small and fast Java bytecode manipulation framework
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
package org.objectweb.asm.tree;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;

/**
 * ClassNode unit tests.
 *
 * @author Eric Bruneton
 */
public class ClassNodeUnitTest implements Opcodes {

  @Test
  public void testFrameNode() {
    FrameNode fn = new FrameNode(F_SAME, 0, null, 0, null);
    assertEquals(AbstractInsnNode.FRAME, fn.getType());
  }

  @Test
  public void testInsnNode() {
    InsnNode in = new InsnNode(NOP);
    assertEquals(in.getOpcode(), NOP);
    assertEquals(AbstractInsnNode.INSN, in.getType());
  }

  @Test
  public void testIntInsnNode() {
    IntInsnNode iin = new IntInsnNode(BIPUSH, 0);
    iin.setOpcode(SIPUSH);
    assertEquals(SIPUSH, iin.getOpcode());
    assertEquals(AbstractInsnNode.INT_INSN, iin.getType());
  }

  @Test
  public void testVarInsnNode() {
    VarInsnNode vn = new VarInsnNode(ALOAD, 0);
    vn.setOpcode(ASTORE);
    assertEquals(ASTORE, vn.getOpcode());
    assertEquals(AbstractInsnNode.VAR_INSN, vn.getType());
  }

  @Test
  public void testTypeInsnNode() {
    TypeInsnNode tin = new TypeInsnNode(NEW, "java/lang/Object");
    tin.setOpcode(CHECKCAST);
    assertEquals(CHECKCAST, tin.getOpcode());
    assertEquals(AbstractInsnNode.TYPE_INSN, tin.getType());
  }

  @Test
  public void testFieldInsnNode() {
    FieldInsnNode fn = new FieldInsnNode(GETSTATIC, "owner", "name", "I");
    fn.setOpcode(PUTSTATIC);
    assertEquals(PUTSTATIC, fn.getOpcode());
    assertEquals(AbstractInsnNode.FIELD_INSN, fn.getType());
  }

  @Test
  public void testMethodInsnNode() {
    MethodInsnNode mn = new MethodInsnNode(INVOKESTATIC, "owner", "name", "I", false);
    mn.setOpcode(INVOKESPECIAL);
    assertEquals(INVOKESPECIAL, mn.getOpcode());
    assertEquals(AbstractInsnNode.METHOD_INSN, mn.getType());
  }

  @Test
  public void testInvokeDynamicInsnNode() {
    Handle bsm = new Handle(Opcodes.H_INVOKESTATIC, "owner", "name", "()V", false);
    InvokeDynamicInsnNode mn = new InvokeDynamicInsnNode("name", "()V", bsm, new Object[0]);

    assertEquals(INVOKEDYNAMIC, mn.getOpcode());
    assertEquals(AbstractInsnNode.INVOKE_DYNAMIC_INSN, mn.getType());
  }

  @Test
  public void testJumpInsnNode() {
    JumpInsnNode jn = new JumpInsnNode(GOTO, new LabelNode());
    jn.setOpcode(IFEQ);
    assertEquals(IFEQ, jn.getOpcode());
    assertEquals(AbstractInsnNode.JUMP_INSN, jn.getType());
  }

  @Test
  public void testLabelNode() {
    LabelNode ln = new LabelNode();
    assertEquals(AbstractInsnNode.LABEL, ln.getType());
    assertNotNull(ln.getLabel());
    // dummy assignment to instruct FindBugs that Label.info can
    // reference other objects than LabelNode instances
    ln.getLabel().info = new Object();
  }

  @Test
  public void testIincInsnNode() {
    IincInsnNode iincn = new IincInsnNode(1, 1);
    assertEquals(AbstractInsnNode.IINC_INSN, iincn.getType());
  }

  @Test
  public void testLdcInsnNode() {
    LdcInsnNode ldcn = new LdcInsnNode("s");
    assertEquals(AbstractInsnNode.LDC_INSN, ldcn.getType());
  }

  @Test
  public void testLookupSwitchInsnNode() {
    LookupSwitchInsnNode lsn = new LookupSwitchInsnNode(null, null, null);
    assertEquals(AbstractInsnNode.LOOKUPSWITCH_INSN, lsn.getType());
  }

  @Test
  public void testTableSwitchInsnNode() {
    TableSwitchInsnNode tsn = new TableSwitchInsnNode(0, 1, null, (LabelNode[]) null);
    assertEquals(AbstractInsnNode.TABLESWITCH_INSN, tsn.getType());
  }

  @Test
  public void testMultiANewArrayInsnNode() {
    MultiANewArrayInsnNode manan = new MultiANewArrayInsnNode("[[I", 2);
    assertEquals(AbstractInsnNode.MULTIANEWARRAY_INSN, manan.getType());
  }

  @Test
  public void testCloneMethod() {
    MethodNode n = new MethodNode();
    Label l0 = new Label();
    Label l1 = new Label();
    n.visitCode();
    n.visitLabel(l0);
    n.visitInsn(Opcodes.NOP);
    n.visitLabel(l1);
    n.visitEnd();
    MethodNode n1 = new MethodNode();
    n.accept(n1);
    n.accept(n1);
  }
}
