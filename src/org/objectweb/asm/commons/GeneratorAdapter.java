/*
 * Copyright 2003,2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.objectweb.asm.commons;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * TODO.
 * 
 * @author Juozas Baliuka
 * @author Chris Nokleberg
 * @author Eric Bruneton
 */

public class GeneratorAdapter extends LocalVariablesSorter {

  private final static Type BYTE_TYPE = Type.getType("Ljava/lang/Byte;");
  private final static Type BOOLEAN_TYPE = Type.getType("Ljava/lang/Boolean;");
  private final static Type SHORT_TYPE = Type.getType("Ljava/lang/Short;");
  private final static Type CHARACTER_TYPE = Type.getType("Ljava/lang/Character;");
  private final static Type INTEGER_TYPE = Type.getType("Ljava/lang/Integer;");
  private final static Type FLOAT_TYPE = Type.getType("Ljava/lang/Float;");
  private final static Type LONG_TYPE = Type.getType("Ljava/lang/Long;");
  private final static Type DOUBLE_TYPE = Type.getType("Ljava/lang/Double;");
  private final static Type NUMBER_TYPE = Type.getType("Ljava/lang/Number;");
  private final static Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");
  
  private final static Method BOOLEAN_VALUE = Method.getMethod("boolean booleanValue()");
  private final static Method CHAR_VALUE = Method.getMethod("char charValue()");
  private final static Method INT_VALUE = Method.getMethod("int intValue()");
  private final static Method FLOAT_VALUE = Method.getMethod("float floatValue()");
  private final static Method LONG_VALUE = Method.getMethod("long longValue()");
  private final static Method DOUBLE_VALUE = Method.getMethod("double doubleValue()");

  public final static int ADD = Opcodes.IADD;

  public final static int MUL = Opcodes.IMUL;

  public final static int XOR = Opcodes.IXOR;

  public final static int USHR = Opcodes.IUSHR;

  public final static int SUB = Opcodes.ISUB;

  public final static int DIV = Opcodes.IDIV;

  public final static int NEG = Opcodes.INEG;

  public final static int REM = Opcodes.IREM;

  public final static int AND = Opcodes.IAND;

  public final static int OR = Opcodes.IOR;


  public final static int GT = Opcodes.IFGT;

  public final static int LT = Opcodes.IFLT;

  public final static int GE = Opcodes.IFGE;

  public final static int LE = Opcodes.IFLE;

  public final static int NE = Opcodes.IFNE;

  public final static int EQ = Opcodes.IFEQ;

  
  private final int access;

  private final Type returnType;
  
  private final Type[] argumentTypes;

  private final Type[] exceptionTypes;

  private final List localTypes;
  
  public GeneratorAdapter (
    final int access, 
    final Method method, 
    final Type[] exceptions, 
    final MethodVisitor mv) 
  {
    super(access, method.getDescriptor(), mv);
    this.access = access;
    this.returnType = method.getReturnType();
    this.argumentTypes = method.getArgumentTypes();
    this.exceptionTypes = exceptions;
    this.localTypes = new ArrayList();
  }
  
  // --------------------------------------------------------------------------
  // Instructions to push constants on the stack
  // --------------------------------------------------------------------------
  
  public void push (final boolean value) {
    push(value ? 1 : 0);
  }

  public void push (final int i) {
    if (i >= -1 && i <= 5) {
      mv.visitInsn(Opcodes.ICONST_0 + i);
    } else if (i >= Byte.MIN_VALUE && i <= Byte.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.BIPUSH, i);
    } else if (i >= Short.MIN_VALUE && i <= Short.MAX_VALUE) {
      mv.visitIntInsn(Opcodes.SIPUSH, i);
    } else {
      mv.visitLdcInsn(new Integer(i));
    }
  }

  public void push (final long value) {
    if (value == 0L || value == 1L) {
      mv.visitInsn(Opcodes.LCONST_0 + (int)value);
    } else {
      mv.visitLdcInsn(new Long(value));
    }
  }

  public void push (final float value) {
    if (value == 0f || value == 1f || value == 2f) {
      mv.visitInsn(Opcodes.FCONST_0 + (int)value);
    } else {
      mv.visitLdcInsn(new Float(value));
    }
  }

  public void push (final double value) {
    if (value == 0d || value == 1d) {
      mv.visitInsn(Opcodes.DCONST_0 + (int)value);
    } else {
      mv.visitLdcInsn(new Double(value));
    }
  }

  public void push (final String value) {
    if (value == null) {
      mv.visitInsn(Opcodes.ACONST_NULL);
    } else {
      mv.visitLdcInsn(value);
    }
  }

  // --------------------------------------------------------------------------
  // Instructions to load and store method arguments
  // --------------------------------------------------------------------------

  private int getArgIndex (final int arg) {
    int index = ((access & Opcodes.ACC_STATIC) == 0 ? 1 : 0);
    for (int i = 0; i < arg; i++) {
      index += argumentTypes[i].getSize();
    }
    return index;
  }

  private void loadInsn (final Type type, final int index) {
    mv.visitVarInsn(type.getOpcode(Opcodes.ILOAD), index);
  }

  private void storeInsn (final Type type, final int index) {
    mv.visitVarInsn(type.getOpcode(Opcodes.ISTORE), index);
  }

  public void loadThis () {
    if ((access & Opcodes.ACC_STATIC) != 0) {
      throw new IllegalStateException("no 'this' pointer within static method");
    }
    mv.visitVarInsn(Opcodes.ALOAD, 0);
  }

  public void loadArg (final int arg) {
    loadInsn(argumentTypes[arg], getArgIndex(arg));
  }

  public void loadArgs (final int arg, final int count) {
    int index = getArgIndex(arg);
    for (int i = 0; i < count; ++i) {
      Type t = argumentTypes[arg + i];
      loadInsn(t, index);
      index += t.getSize();
    }
  }
  
  public void loadArgs () {
    loadArgs(0, argumentTypes.length);
  }

  public void loadArgArray () {
    push(argumentTypes.length);
    newArray(OBJECT_TYPE);
    for (int i = 0; i < argumentTypes.length; i++) {
      dup();
      push(i);
      loadArg(i);
      box(argumentTypes[i]);
      arrayStore(OBJECT_TYPE);
    }
  }

  public void storeArg (final int arg) {
    storeInsn(argumentTypes[arg], getArgIndex(arg));
  }

  // --------------------------------------------------------------------------
  // Instructions to load and store local variables
  // --------------------------------------------------------------------------

  public int newLocal (final Type type) {
    localTypes.add(type);
    return super.newLocal(type.getSize());
  }

  public Type getLocalType (final int local) {
    return (Type)localTypes.get(local);
  }
  
  public int getLocalIndex (final int local) {
    int index = firstLocal;
    for (int i = 0; i < local; ++i) {
      index += getLocalType(local).getSize();
    }
    return index;
  }

  public void loadLocal (final int local) {
    loadInsn(getLocalType(local), getLocalIndex(local));
  }

  public void storeLocal (final int local) {
    storeInsn(getLocalType(local), getLocalIndex(local));
  }

  public void arrayLoad (final Type type) {
    mv.visitInsn(type.getOpcode(Opcodes.IALOAD));
  }

  public void arrayStore (final Type type) {
    mv.visitInsn(type.getOpcode(Opcodes.IASTORE));
  }

  // --------------------------------------------------------------------------
  // Instructions to manage the stack
  // --------------------------------------------------------------------------

  public void pop () {
    mv.visitInsn(Opcodes.POP);
  }

  public void pop2 () {
    mv.visitInsn(Opcodes.POP2);
  }

  public void dup () {
    mv.visitInsn(Opcodes.DUP);
  }

  public void dup2 () {
    mv.visitInsn(Opcodes.DUP2);
  }

  public void dupX1 () {
    mv.visitInsn(Opcodes.DUP_X1);
  }

  public void dupX2 () {
    mv.visitInsn(Opcodes.DUP_X2);
  }

  public void dup2X1 () {
    mv.visitInsn(Opcodes.DUP2_X1);
  }

  public void dup2X2 () {
    mv.visitInsn(Opcodes.DUP2_X2);
  }

  public void swap () {
    mv.visitInsn(Opcodes.SWAP);
  }

  public void swap (final Type prev, final Type type) {
    if (type.getSize() == 1) {
      if (prev.getSize() == 1) {
        swap(); // same as dupX1(), pop();
      } else {
        dupX2();
        pop();
      }
    } else {
      if (prev.getSize() == 1) {
        dup2X1();
        pop2();
      } else {
        dup2X2();
        pop2();
      }
    }
  }

  // --------------------------------------------------------------------------
  // Instructions to do mathematical and logical operations
  // --------------------------------------------------------------------------
  
  public void math (final int op, final Type type) {
    mv.visitInsn(type.getOpcode(op));
  }

  public void not () {
    push(1);
    math(XOR, Type.INT_TYPE);
  }

  public void iinc (final int local, final int amount) {
    mv.visitIincInsn(getLocalIndex(local), amount);
  }

  public void cast (final Type from, final Type to) {
    if (from != to) {
      if (from == Type.DOUBLE_TYPE) {
        if (to == Type.FLOAT_TYPE) {
          mv.visitInsn(Opcodes.D2F);
        } else if (to == Type.LONG_TYPE) {
          mv.visitInsn(Opcodes.D2L);
        } else {
          mv.visitInsn(Opcodes.D2I);
          cast(Type.INT_TYPE, to);
        }
      } else if (from == Type.FLOAT_TYPE) {
        if (to == Type.DOUBLE_TYPE) {
          mv.visitInsn(Opcodes.F2D);
        } else if (to == Type.LONG_TYPE) {
          mv.visitInsn(Opcodes.F2L);
        } else {
          mv.visitInsn(Opcodes.F2I);
          cast(Type.INT_TYPE, to);
        }
      } else if (from == Type.LONG_TYPE) {
        if (to == Type.DOUBLE_TYPE) {
          mv.visitInsn(Opcodes.L2D);
        } else if (to == Type.FLOAT_TYPE) {
          mv.visitInsn(Opcodes.L2F);
        } else {
          mv.visitInsn(Opcodes.L2I);
          cast(Type.INT_TYPE, to);
        }
      } else {
        if (to == Type.BYTE_TYPE) {
          mv.visitInsn(Opcodes.I2B);
        } else if (to == Type.CHAR_TYPE) {
          mv.visitInsn(Opcodes.I2C);
        } else if (to == Type.DOUBLE_TYPE) {
          mv.visitInsn(Opcodes.I2D);
        } else if (to == Type.FLOAT_TYPE) {
          mv.visitInsn(Opcodes.I2F);
        } else if (to == Type.LONG_TYPE) {
          mv.visitInsn(Opcodes.I2L);
        } else if (to == Type.SHORT_TYPE) {
          mv.visitInsn(Opcodes.I2S);
        }
      }
    }
  }

  // --------------------------------------------------------------------------
  // Instructions to do boxing and unboxing operations
  // --------------------------------------------------------------------------

  public void box (final Type type) {
    if (type.getSort() == Type.OBJECT || type.getSort() == Type.ARRAY) {
      return;
    }
    if (type == Type.VOID_TYPE) {
      push(null);
    } else {
      Type boxed = type;
      switch (type.getSort()) {
        case Type.BYTE:
          boxed = BYTE_TYPE;
          break;
        case Type.BOOLEAN:
          boxed = BOOLEAN_TYPE;
          break;
        case Type.SHORT:
          boxed = SHORT_TYPE;
          break;
        case Type.CHAR:
          boxed = CHARACTER_TYPE;
          break;
        case Type.INT:
          boxed = INTEGER_TYPE;
          break;
        case Type.FLOAT:
          boxed = FLOAT_TYPE;
          break;
        case Type.LONG:
          boxed = LONG_TYPE;
          break;
        case Type.DOUBLE:
          boxed = DOUBLE_TYPE;
          break;
      }
      newInstance(boxed);
      if (type.getSize() == 2) {
        // Pp -> Ppo -> oPpo -> ooPpo -> ooPp -> o
        dupX2();
        dupX2();
        pop();
      } else {
        // p -> po -> opo -> oop -> o
        dupX1();
        swap();
      }
      invokeConstructor(
          boxed, new Method("<init>", Type.VOID_TYPE, new Type[] { type }));
    }
  }

  public void unbox (final Type type) {
    Type t = NUMBER_TYPE;
    Method sig = null;
    switch (type.getSort()) {
      case Type.VOID:
        return;
      case Type.CHAR:
        t = CHARACTER_TYPE;
        sig = CHAR_VALUE;
        break;
      case Type.BOOLEAN:
        t = BOOLEAN_TYPE;
        sig = BOOLEAN_VALUE;
        break;
      case Type.DOUBLE:
        sig = DOUBLE_VALUE;
        break;
      case Type.FLOAT:
        sig = FLOAT_VALUE;
        break;
      case Type.LONG:
        sig = LONG_VALUE;
        break;
      case Type.INT:
      case Type.SHORT:
      case Type.BYTE:
        sig = INT_VALUE;
    }
    if (sig == null) {
      checkCast(type);
    } else {
      checkCast(t);
      invokeVirtual(t, sig);
    }
  }

  // --------------------------------------------------------------------------
  // Instructions to jump to other instructions
  // --------------------------------------------------------------------------

  public Label newLabel () {
    return new Label();
  }

  public void mark (final Label label) {
    mv.visitLabel(label);
  }

  public Label mark () {
    Label label = new Label();
    mv.visitLabel(label);
    return label;
  }

  public void ifJump (final int mode, final Label label) {
    mv.visitJumpInsn(mode, label);
  }

  public void ifCmp (final Type type, final int mode, final Label label) {
    int intOp = -1;
    int jumpmode = mode;
    switch (mode) {
      case GE:
        jumpmode = LT;
        break;
      case LE:
        jumpmode = GT;
        break;
    }
    switch (type.getSort()) {
      case Type.LONG:
        mv.visitInsn(Opcodes.LCMP);
        break;
      case Type.DOUBLE:
        mv.visitInsn(Opcodes.DCMPG);
        break;
      case Type.FLOAT:
        mv.visitInsn(Opcodes.FCMPG);
        break;
      case Type.ARRAY:
      case Type.OBJECT:
        switch (mode) {
          case EQ:
            mv.visitJumpInsn(Opcodes.IF_ACMPEQ, label);
            return;
          case NE:
            mv.visitJumpInsn(Opcodes.IF_ACMPNE, label);
            return;
        }
        throw new IllegalArgumentException("Bad comparison for type " + type);
      default:
        switch (mode) {
          case EQ:
            intOp = Opcodes.IF_ICMPEQ;
            break;
          case NE:
            intOp = Opcodes.IF_ICMPNE;
            break;
          case GE:
            swap(); /* fall through */
          case LT:
            intOp = Opcodes.IF_ICMPLT;
            break;
          case LE:
            swap(); /* fall through */
          case GT:
            intOp = Opcodes.IF_ICMPGT;
            break;
        }
        mv.visitJumpInsn(intOp, label);
        return;
    }
    ifJump(jumpmode, label);
  }

  public void ifICmp (final int mode, final Label label) {
    ifCmp(Type.INT_TYPE, mode, label);
  }

  public void ifNull (final Label label) {
    mv.visitJumpInsn(Opcodes.IFNULL, label);
  }

  public void ifNonNull (final Label label) {
    mv.visitJumpInsn(Opcodes.IFNONNULL, label);
  }
  
  public void goTo (final Label label) {
    mv.visitJumpInsn(Opcodes.GOTO, label);
  }

  // TODO switchs
  /*public void process_switch(int[] keys, ProcessSwitchCallback callback) {
  float density;
  if (keys.length == 0) {
  density = 0;
  } else {
  density = (float)keys.length / (keys[keys.length - 1] - keys[0] + 1);
  }
  process_switch(keys, callback, density >= 0.5f);
  }

  public void process_switch(int[] keys, ProcessSwitchCallback callback, boolean useTable) {
  if (!isSorted(keys))
  throw new IllegalArgumentException("keys to switch must be sorted ascending");
  Label def = make_label();
  Label end = make_label();

  try {
  if (keys.length > 0) {
  int len = keys.length;
  int min = keys[0];
  int max = keys[len - 1];
  int range = max - min + 1;

  if (useTable) {
  Label[] labels = new Label[range];
  Arrays.fill(labels, def);
  for (int i = 0; i < len; i++) {
  labels[keys[i] - min] = make_label();
  }
  mv.visitTableSwitchInsn(min, max, def, labels);
  for (int i = 0; i < range; i++) {
  Label label = labels[i];
  if (label != def) {
  mark(label);
  callback.processCase(i + min, end);
  }
  }
  } else {
  Label[] labels = new Label[len];
  for (int i = 0; i < len; i++) {
  labels[i] = make_label();
  }
  mv.visitLookupSwitchInsn(def, keys, labels);
  for (int i = 0; i < len; i++) {
  mark(labels[i]);
  callback.processCase(keys[i], end);
  }
  }
  }

  mark(def);
  callback.processDefault();
  mark(end);

  } catch (RuntimeException e) {
  throw e;
  } catch (Error e) {
  throw e;
  } catch (Exception e) {
  throw new CodeGenerationException(e);
  }
  }

  private static boolean isSorted(int[] keys) {
  for (int i = 1; i < keys.length; i++) {
  if (keys[i] < keys[i - 1])
  return false;
  }
  return true;
  }*/

  public void returnValue () {
    mv.visitInsn(returnType.getOpcode(Opcodes.IRETURN));
  }
  
  // --------------------------------------------------------------------------
  // Instructions to load and store fields
  // --------------------------------------------------------------------------

  private void fieldInsn (
    final int opcode,
    final Type ownerType, 
    final String name, 
    final Type fieldType) 
  {
    mv.visitFieldInsn(
      opcode, ownerType.getInternalName(), name, fieldType.getDescriptor());
  }

  public void getStatic (final Type owner, final String name, final Type type) {
    fieldInsn(Opcodes.GETSTATIC, owner, name, type);
  }

  public void putStatic (final Type owner, final String name, final Type type) {
    fieldInsn(Opcodes.PUTSTATIC, owner, name, type);
  }

  public void getField (final Type owner, final String name, final Type type) {
    fieldInsn(Opcodes.GETFIELD, owner, name, type);
  }

  public void putField (final Type owner, final String name, final Type type) {
    fieldInsn(Opcodes.PUTFIELD, owner, name, type);
  }

  // --------------------------------------------------------------------------
  // Instructions to invoke methods
  // --------------------------------------------------------------------------

  private void invokeInsn (
    final int opcode, 
    final Type type, 
    final Method method) 
  {
    mv.visitMethodInsn(
      opcode, type.getInternalName(), method.getName(), method.getDescriptor());
  }

  public void invokeVirtual (final Type owner, final Method method) {
    invokeInsn(Opcodes.INVOKEVIRTUAL, owner, method);
  }

  public void invokeConstructor (final Type type, final Method method) {
    invokeInsn(Opcodes.INVOKESPECIAL, type, method);
  }

  public void invokeStatic (final Type owner, final Method method) {
    invokeInsn(Opcodes.INVOKESTATIC, owner, method);
  }

  public void invokeInterface (final Type owner, final Method method) {
    invokeInsn(Opcodes.INVOKEINTERFACE, owner, method);
  }

  // --------------------------------------------------------------------------
  // Instructions to create objects and arrays
  // --------------------------------------------------------------------------

  private void typeInsn (final int opcode, final Type type) {
    String desc;
    if (type.getSort() == Type.ARRAY) {
      desc = type.getDescriptor();
    } else {
      desc = type.getInternalName();
    }
    mv.visitTypeInsn(opcode, desc);
  }

  public void newInstance (final Type type) {
    typeInsn(Opcodes.NEW, type);
  }

  public void newArray (final Type type) {
    int typ;
    switch (type.getSort()) {
      case Type.BOOLEAN:
        typ = Opcodes.T_BOOLEAN;
        break;
      case Type.CHAR:
        typ = Opcodes.T_CHAR;
        break;
      case Type.BYTE:
        typ = Opcodes.T_BYTE;
        break;
      case Type.SHORT:
        typ = Opcodes.T_SHORT;
        break;
      case Type.INT:
        typ = Opcodes.T_INT;
        break;
      case Type.FLOAT:
        typ = Opcodes.T_FLOAT;
        break;
      case Type.LONG:
        typ = Opcodes.T_LONG;
        break;
      case Type.DOUBLE:
        typ = Opcodes.T_DOUBLE;
        break;
      default:
        typeInsn(Opcodes.ANEWARRAY, type);
        return;
    }
    mv.visitIntInsn(Opcodes.NEWARRAY, typ);
  }

  // --------------------------------------------------------------------------
  // Miscelaneous instructions
  // --------------------------------------------------------------------------

  public void arrayLength () {
    mv.visitInsn(Opcodes.ARRAYLENGTH);
  }

  public void throwException () {
    mv.visitInsn(Opcodes.ATHROW);
  }

  public void throwException (final Type type, final String msg) {
    newInstance(type);
    dup();
    push(msg);
    invokeConstructor(type, Method.getMethod("void <init> (String)"));
    throwException();
  }

  public void checkCast (final Type type) {
    if (!type.equals(OBJECT_TYPE)) {
      typeInsn(Opcodes.CHECKCAST, type);
    }
  }
  
  public void instanceOf (final Type type) {
    typeInsn(Opcodes.INSTANCEOF, type);
  }

  public void monitorEnter () {
    mv.visitInsn(Opcodes.MONITORENTER);
  }

  public void monitorExit () {
    mv.visitInsn(Opcodes.MONITOREXIT);
  }
  
  // --------------------------------------------------------------------------
  // TODO
  // --------------------------------------------------------------------------
  
  public void endMethod () {
    if ((access & Opcodes.ACC_ABSTRACT) != 0) {
      mv.visitMaxs(0, 0);
    }
  }

  public void catchException (
    final Label start, 
    final Label end, 
    final Type exception) 
  {
    mv.visitTryCatchBlock(start, end, mark(), exception.getInternalName());
  }
}