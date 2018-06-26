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
package org.objectweb.asm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * Constants test.
 *
 * @author Eric Bruneton
 */
public class ConstantsTest {

  /**
   * Tests that the constants in Constants and Opcodes satisfy some basic constraints.
   *
   * @throws IllegalAccessException
   * @throws IllegalArgumentException
   */
  @Test
  public void testBasicConstraints() throws IllegalArgumentException, IllegalAccessException {
    Set<Integer> asmVersions = new HashSet<>();
    Set<Integer> classVersions = new HashSet<>();
    Set<Integer> newArrayTypes = new HashSet<>();
    Set<Integer> referenceKinds = new HashSet<>();
    Set<Integer> frameTypes = new HashSet<>();
    Set<Integer> verificationTypeInfoTags = new HashSet<>();
    Set<Integer> opcodes = new HashSet<>();
    for (Field field : Constants.class.getFields()) {
      switch (field.getName()) {
        case "ASM4":
        case "ASM5":
        case "ASM6":
        case "ASM7_EXPERIMENTAL":
          assertFalse(asmVersions.contains(field.getInt(null)));
          asmVersions.add(field.getInt(null));
          break;
        case "V_PREVIEW_EXPERIMENTAL":
        case "V1_1":
        case "V1_2":
        case "V1_3":
        case "V1_4":
        case "V1_5":
        case "V1_6":
        case "V1_7":
        case "V1_8":
        case "V9":
        case "V10":
        case "V11":
        case "V12":
          assertFalse(classVersions.contains(field.getInt(null)));
          classVersions.add(field.getInt(null));
          break;
        case "ACC_PUBLIC":
        case "ACC_PRIVATE":
        case "ACC_PROTECTED":
        case "ACC_STATIC":
        case "ACC_FINAL":
        case "ACC_SUPER":
        case "ACC_SYNCHRONIZED":
        case "ACC_OPEN":
        case "ACC_TRANSITIVE":
        case "ACC_VOLATILE":
        case "ACC_BRIDGE":
        case "ACC_STATIC_PHASE":
        case "ACC_VARARGS":
        case "ACC_TRANSIENT":
        case "ACC_NATIVE":
        case "ACC_INTERFACE":
        case "ACC_ABSTRACT":
        case "ACC_STRICT":
        case "ACC_SYNTHETIC":
        case "ACC_ANNOTATION":
        case "ACC_ENUM":
        case "ACC_MANDATED":
        case "ACC_MODULE":
          assertEquals(field.getInt(null) & ~0xFFFF, 0);
          assertEquals(Integer.bitCount(field.getInt(null)), 1);
          break;
        case "ACC_DEPRECATED":
        case "ACC_CONSTRUCTOR":
          assertEquals(field.getInt(null) & 0xFFFF, 0);
          assertEquals(Integer.bitCount(field.getInt(null)), 1);
          break;
        case "T_BOOLEAN":
        case "T_CHAR":
        case "T_FLOAT":
        case "T_DOUBLE":
        case "T_BYTE":
        case "T_SHORT":
        case "T_INT":
        case "T_LONG":
          assertEquals(field.getInt(null) & ~0xFF, 0);
          assertFalse(newArrayTypes.contains(field.getInt(null)));
          newArrayTypes.add(field.getInt(null));
          break;
        case "H_GETFIELD":
        case "H_GETSTATIC":
        case "H_PUTFIELD":
        case "H_PUTSTATIC":
        case "H_INVOKEVIRTUAL":
        case "H_INVOKESTATIC":
        case "H_INVOKESPECIAL":
        case "H_NEWINVOKESPECIAL":
        case "H_INVOKEINTERFACE":
          assertEquals(field.getInt(null) & ~0xFF, 0);
          assertFalse(referenceKinds.contains(field.getInt(null)));
          referenceKinds.add(field.getInt(null));
          break;
        case "F_NEW":
        case "F_FULL":
        case "F_APPEND":
        case "F_CHOP":
        case "F_SAME":
        case "F_SAME1":
        case "F_INSERT":
          assertFalse(frameTypes.contains(field.getInt(null)));
          frameTypes.add(field.getInt(null));
          break;
        case "TOP":
        case "INTEGER":
        case "FLOAT":
        case "DOUBLE":
        case "LONG":
        case "NULL":
        case "UNINITIALIZED_THIS":
          int value = ((Integer) field.get(null)).intValue();
          assertEquals(value & ~0xFF, 0);
          assertFalse(verificationTypeInfoTags.contains(value));
          verificationTypeInfoTags.add(value);
          break;
        case "NOP":
        case "ACONST_NULL":
        case "ICONST_M1":
        case "ICONST_0":
        case "ICONST_1":
        case "ICONST_2":
        case "ICONST_3":
        case "ICONST_4":
        case "ICONST_5":
        case "LCONST_0":
        case "LCONST_1":
        case "FCONST_0":
        case "FCONST_1":
        case "FCONST_2":
        case "DCONST_0":
        case "DCONST_1":
        case "BIPUSH":
        case "SIPUSH":
        case "LDC":
        case "LDC_W":
        case "LDC2_W":
        case "ILOAD":
        case "LLOAD":
        case "FLOAD":
        case "DLOAD":
        case "ALOAD":
        case "ILOAD_0":
        case "ILOAD_1":
        case "ILOAD_2":
        case "ILOAD_3":
        case "LLOAD_0":
        case "LLOAD_1":
        case "LLOAD_2":
        case "LLOAD_3":
        case "FLOAD_0":
        case "FLOAD_1":
        case "FLOAD_2":
        case "FLOAD_3":
        case "DLOAD_0":
        case "DLOAD_1":
        case "DLOAD_2":
        case "DLOAD_3":
        case "ALOAD_0":
        case "ALOAD_1":
        case "ALOAD_2":
        case "ALOAD_3":
        case "IALOAD":
        case "LALOAD":
        case "FALOAD":
        case "DALOAD":
        case "AALOAD":
        case "BALOAD":
        case "CALOAD":
        case "SALOAD":
        case "ISTORE":
        case "LSTORE":
        case "FSTORE":
        case "DSTORE":
        case "ASTORE":
        case "ISTORE_0":
        case "ISTORE_1":
        case "ISTORE_2":
        case "ISTORE_3":
        case "LSTORE_0":
        case "LSTORE_1":
        case "LSTORE_2":
        case "LSTORE_3":
        case "FSTORE_0":
        case "FSTORE_1":
        case "FSTORE_2":
        case "FSTORE_3":
        case "DSTORE_0":
        case "DSTORE_1":
        case "DSTORE_2":
        case "DSTORE_3":
        case "ASTORE_0":
        case "ASTORE_1":
        case "ASTORE_2":
        case "ASTORE_3":
        case "IASTORE":
        case "LASTORE":
        case "FASTORE":
        case "DASTORE":
        case "AASTORE":
        case "BASTORE":
        case "CASTORE":
        case "SASTORE":
        case "POP":
        case "POP2":
        case "DUP":
        case "DUP_X1":
        case "DUP_X2":
        case "DUP2":
        case "DUP2_X1":
        case "DUP2_X2":
        case "SWAP":
        case "IADD":
        case "LADD":
        case "FADD":
        case "DADD":
        case "ISUB":
        case "LSUB":
        case "FSUB":
        case "DSUB":
        case "IMUL":
        case "LMUL":
        case "FMUL":
        case "DMUL":
        case "IDIV":
        case "LDIV":
        case "FDIV":
        case "DDIV":
        case "IREM":
        case "LREM":
        case "FREM":
        case "DREM":
        case "INEG":
        case "LNEG":
        case "FNEG":
        case "DNEG":
        case "ISHL":
        case "LSHL":
        case "ISHR":
        case "LSHR":
        case "IUSHR":
        case "LUSHR":
        case "IAND":
        case "LAND":
        case "IOR":
        case "LOR":
        case "IXOR":
        case "LXOR":
        case "IINC":
        case "I2L":
        case "I2F":
        case "I2D":
        case "L2I":
        case "L2F":
        case "L2D":
        case "F2I":
        case "F2L":
        case "F2D":
        case "D2I":
        case "D2L":
        case "D2F":
        case "I2B":
        case "I2C":
        case "I2S":
        case "LCMP":
        case "FCMPL":
        case "FCMPG":
        case "DCMPL":
        case "DCMPG":
        case "IFEQ":
        case "IFNE":
        case "IFLT":
        case "IFGE":
        case "IFGT":
        case "IFLE":
        case "IF_ICMPEQ":
        case "IF_ICMPNE":
        case "IF_ICMPLT":
        case "IF_ICMPGE":
        case "IF_ICMPGT":
        case "IF_ICMPLE":
        case "IF_ACMPEQ":
        case "IF_ACMPNE":
        case "GOTO":
        case "JSR":
        case "RET":
        case "TABLESWITCH":
        case "LOOKUPSWITCH":
        case "IRETURN":
        case "LRETURN":
        case "FRETURN":
        case "DRETURN":
        case "ARETURN":
        case "RETURN":
        case "GETSTATIC":
        case "PUTSTATIC":
        case "GETFIELD":
        case "PUTFIELD":
        case "INVOKEVIRTUAL":
        case "INVOKESPECIAL":
        case "INVOKESTATIC":
        case "INVOKEINTERFACE":
        case "INVOKEDYNAMIC":
        case "NEW":
        case "NEWARRAY":
        case "ANEWARRAY":
        case "ARRAYLENGTH":
        case "ATHROW":
        case "CHECKCAST":
        case "INSTANCEOF":
        case "MONITORENTER":
        case "MONITOREXIT":
        case "WIDE":
        case "MULTIANEWARRAY":
        case "IFNULL":
        case "IFNONNULL":
        case "GOTO_W":
        case "JSR_W":
        case "ASM_IFEQ":
        case "ASM_IFNE":
        case "ASM_IFLT":
        case "ASM_IFGE":
        case "ASM_IFGT":
        case "ASM_IFLE":
        case "ASM_IF_ICMPEQ":
        case "ASM_IF_ICMPNE":
        case "ASM_IF_ICMPLT":
        case "ASM_IF_ICMPGE":
        case "ASM_IF_ICMPGT":
        case "ASM_IF_ICMPLE":
        case "ASM_IF_ACMPEQ":
        case "ASM_IF_ACMPNE":
        case "ASM_GOTO":
        case "ASM_JSR":
        case "ASM_IFNULL":
        case "ASM_IFNONNULL":
        case "ASM_GOTO_W":
          assertEquals(field.getInt(null) & ~0xFF, 0);
          assertFalse(opcodes.contains(field.getInt(null)));
          opcodes.add(field.getInt(null));
          break;
        case "WIDE_JUMP_OPCODE_DELTA":
        case "ASM_OPCODE_DELTA":
        case "ASM_IFNULL_OPCODE_DELTA":
          // Nothing to check.
          break;
        default:
          fail("Unknown constant " + field.getName());
      }
    }
  }
}
