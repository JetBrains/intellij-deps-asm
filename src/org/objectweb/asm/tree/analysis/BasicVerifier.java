/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
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

package org.objectweb.asm.tree.analysis;

import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

/**
 * An extended {@link BasicInterpreter} that checks that bytecode instructions 
 * are correctly used. 
 * 
 * @author Eric Bruneton
 */

public class BasicVerifier extends BasicInterpreter {

  public Value copyOperation (final AbstractInsnNode insn, final Value value) {
    Value type;
    switch (insn.getOpcode()) {
      case ILOAD:
      case ISTORE:
        type = BasicValue.INT_VALUE;
        break;
      case FLOAD:
      case FSTORE:
        type = BasicValue.FLOAT_VALUE;
        break;
      case LLOAD:
      case LSTORE:
        type = BasicValue.LONG_VALUE;
        break;
      case DLOAD:
      case DSTORE:
        type = BasicValue.DOUBLE_VALUE;
        break;
      case ALOAD:
        type = BasicValue.REFERENCE_VALUE;
        break;
      case ASTORE:
        if (value != BasicValue.REFERENCE_VALUE && 
            value != BasicValue.RETURNADDRESS_VALUE) 
        {
          throw new RuntimeException("Wrong types on stack.");
        }
        return value;
      default:
        return value;
    }
    if (value != type) {
      throw new RuntimeException("Wrong types on stack.");
    }
    return value;
  }
  
  public Value unaryOperation (final AbstractInsnNode insn, final Value value) {
    Value type;
    switch (insn.getOpcode()) {
      case INEG:
      case IINC:
      case I2F:
      case I2L:
      case I2D:
      case I2B:
      case I2C:
      case I2S:
      case IFEQ:
      case IFNE:
      case IFLT:
      case IFGE:
      case IFGT:
      case IFLE:
      case TABLESWITCH:
      case LOOKUPSWITCH:
      case IRETURN:
      case NEWARRAY:
      case ANEWARRAY:
        type = BasicValue.INT_VALUE;
        break;
      case FNEG:
      case F2I:
      case F2L:
      case F2D:
      case FRETURN:
        type = BasicValue.FLOAT_VALUE;
        break;
      case LNEG:
      case L2I:
      case L2F:
      case L2D:
      case LRETURN:
        type = BasicValue.LONG_VALUE;
        break;
      case DNEG:
      case D2I:
      case D2F:
      case D2L:
      case DRETURN:
        type = BasicValue.DOUBLE_VALUE;
        break;
      case ARETURN:
      case GETFIELD:
      case ARRAYLENGTH:
      case ATHROW:
      case CHECKCAST:
      case INSTANCEOF:
      case MONITORENTER:
      case MONITOREXIT:
      case IFNULL:
      case IFNONNULL:
        type = BasicValue.REFERENCE_VALUE;
        break;
      case PUTSTATIC:
        type = newValue(Type.getType(((FieldInsnNode)insn).desc));
        break;
      default:
        throw new RuntimeException("Internal error.");
    }
    if (value != type) {
      throw new RuntimeException("Wrong types on stack.");
    }
    return super.unaryOperation(insn, value);
  }

  public Value binaryOperation (
    final AbstractInsnNode insn,
    final Value value1,
    final Value value2)
  {
    Value type1;
    Value type2;
    switch (insn.getOpcode()) {
      case IALOAD:
      case LALOAD:
      case FALOAD:
      case DALOAD:
      case AALOAD:
      case BALOAD:
      case CALOAD:
      case SALOAD:
        type1 = BasicValue.REFERENCE_VALUE;
        type2 = BasicValue.INT_VALUE;
        break;
      case IADD:
      case ISUB:
      case IMUL:
      case IDIV:
      case IREM:
      case ISHL:
      case ISHR:
      case IUSHR:
      case IAND:
      case IOR:
      case IXOR:
      case IF_ICMPEQ:
      case IF_ICMPNE:
      case IF_ICMPLT:
      case IF_ICMPGE:
      case IF_ICMPGT:
      case IF_ICMPLE:
        type1 = BasicValue.INT_VALUE;
        type2 = BasicValue.INT_VALUE;
        break;
      case FADD:
      case FSUB:
      case FMUL:
      case FDIV:
      case FREM:
      case FCMPL:
      case FCMPG:
        type1 = BasicValue.FLOAT_VALUE;
        type2 = BasicValue.FLOAT_VALUE;
        break;
      case LADD:
      case LSUB:
      case LMUL:
      case LDIV:
      case LREM:
      case LAND:
      case LOR:
      case LXOR:
      case LCMP:
        type1 = BasicValue.LONG_VALUE;
        type2 = BasicValue.LONG_VALUE;
        break;
      case LSHL:
      case LSHR:
      case LUSHR:
        type1 = BasicValue.LONG_VALUE;
        type2 = BasicValue.INT_VALUE;
        break;
      case DADD:
      case DSUB:
      case DMUL:
      case DDIV:
      case DREM:
      case DCMPL:
      case DCMPG:
        type1 = BasicValue.DOUBLE_VALUE;
        type2 = BasicValue.DOUBLE_VALUE;
        break;
      case IF_ACMPEQ:
      case IF_ACMPNE:
        type1 = BasicValue.REFERENCE_VALUE;
        type2 = BasicValue.REFERENCE_VALUE;
        break;
      case PUTFIELD:
        type1 = BasicValue.REFERENCE_VALUE;
        type2 = newValue(Type.getType(((FieldInsnNode)insn).desc));
        break;
      default:
        throw new RuntimeException("Internal error.");
    }
    if (value1 != type1 || value2 != type2) {
      throw new RuntimeException("Wrong types on stack.");
    }
    return super.binaryOperation(insn, value1, value2);
  }

  public Value ternaryOperation (
    final AbstractInsnNode insn,
    final Value value1,
    final Value value2,
    final Value value3)
  {
    Value type; 
    switch (insn.getOpcode()) {
      case IASTORE: 
      case BASTORE:
      case CASTORE:
      case SASTORE:
        type = BasicValue.INT_VALUE;
        break;
      case LASTORE:
        type = BasicValue.LONG_VALUE;
        break;
      case FASTORE:
        type = BasicValue.FLOAT_VALUE;
        break;
      case DASTORE:
        type = BasicValue.DOUBLE_VALUE;
        break;
      case AASTORE:
        type = BasicValue.REFERENCE_VALUE;
        break;   
      default:
        throw new RuntimeException("Internal error.");
    }
    if (value1 != BasicValue.REFERENCE_VALUE || 
        value2 != BasicValue.INT_VALUE ||
        value3 != type) 
    {
      throw new RuntimeException("Wrong types on stack.");
    }
    return null;
  }

  public Value naryOperation (final AbstractInsnNode insn, final List values) {
    int opcode = insn.getOpcode();
    if (opcode == MULTIANEWARRAY) {
      for (int i = 0; i < values.size(); ++i) {
        if (values.get(i) != BasicValue.INT_VALUE) {
          throw new RuntimeException("Wrong types on stack.");
        }
      }
    } else {
      int i = 0;
      int j = 0;
      if (opcode != INVOKESTATIC) {
        if (values.get(i++) != BasicValue.REFERENCE_VALUE) {
          throw new RuntimeException("Wrong types on stack.");
        }
      }
      Type[] args = Type.getArgumentTypes(((MethodInsnNode)insn).desc);
      while (i < values.size()) {
        if (values.get(i++) != newValue(args[j++])) {
          throw new RuntimeException("Wrong types on stack.");
        }
      }
    }
    return super.naryOperation(insn, values);
  }
}
