/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm.tree;

import org.objectweb.asm.CodeVisitor;

/**
 * A node that represents a zero operand instruction.
 */

public class InsnNode extends AbstractInsnNode {

  /**
   * Constructs a new {@link InsnNode InsnNode} object.
   *
   * @param opcode the opcode of the instruction to be constructed. This opcode
   *      must be NOP, ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
   *      ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1,
   *      FCONST_2, DCONST_0, DCONST_1,
   *
   *      IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
   *      IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
   *      SASTORE,
   *
   *      POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP,
   *
   *      IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL,
   *      DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG,
   *      FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR,
   *      LOR, IXOR, LXOR,
   *
   *      I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C,
   *      I2S,
   *
   *      LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
   *
   *      IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
   *
   *      ARRAYLENGTH,
   *
   *      ATHROW,
   *
   *      MONITORENTER, or MONITOREXIT.
   */

  public InsnNode (final int opcode) {
    super(opcode);
  }

  /**
   * Sets the opcode of this instruction.
   *
   * @param opcode the new instruction opcode. This opcode must be NOP,
   *      ACONST_NULL, ICONST_M1, ICONST_0, ICONST_1, ICONST_2,
   *      ICONST_3, ICONST_4, ICONST_5, LCONST_0, LCONST_1, FCONST_0, FCONST_1,
   *      FCONST_2, DCONST_0, DCONST_1,
   *
   *      IALOAD, LALOAD, FALOAD, DALOAD, AALOAD, BALOAD, CALOAD, SALOAD,
   *      IASTORE, LASTORE, FASTORE, DASTORE, AASTORE, BASTORE, CASTORE,
   *      SASTORE,
   *
   *      POP, POP2, DUP, DUP_X1, DUP_X2, DUP2, DUP2_X1, DUP2_X2, SWAP,
   *
   *      IADD, LADD, FADD, DADD, ISUB, LSUB, FSUB, DSUB, IMUL, LMUL, FMUL,
   *      DMUL, IDIV, LDIV, FDIV, DDIV, IREM, LREM, FREM, DREM, INEG, LNEG,
   *      FNEG, DNEG, ISHL, LSHL, ISHR, LSHR, IUSHR, LUSHR, IAND, LAND, IOR,
   *      LOR, IXOR, LXOR,
   *
   *      I2L, I2F, I2D, L2I, L2F, L2D, F2I, F2L, F2D, D2I, D2L, D2F, I2B, I2C,
   *      I2S,
   *
   *      LCMP, FCMPL, FCMPG, DCMPL, DCMPG,
   *
   *      IRETURN, LRETURN, FRETURN, DRETURN, ARETURN, RETURN,
   *
   *      ARRAYLENGTH,
   *
   *      ATHROW,
   *
   *      MONITORENTER, or MONITOREXIT.
   */

  public void setOpcode (final int opcode) {
    this.opcode = opcode;
  }

  /**
   * Makes the given code visitor visit this instruction.
   */

  public void accept (final CodeVisitor cv) {
    cv.visitInsn(opcode);
  }
}
