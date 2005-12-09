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
package org.objectweb.asm.commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A {@link MethodAdapter} that renumbers local variables in their order of
 * appearance. This adapter allows one to easily add new local variables to a
 * method. The {@link org.objectweb.asm.ClassWriter#COMPUTE_MAXS} flag <i>must
 * be set when this adapter is used</i>.
 * 
 * @author Chris Nokleberg
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public class LocalVariablesSorter extends MethodAdapter {

    private final static Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");

    /**
     * Map of <code>Integer</code> representing an old variable index (indexes
     * for long and double types that are using two slots are negated) to
     * <code>Integer</code> value representing variable index and number after
     * remapping (the variable index is in the lower two bytes, and the variable
     * number in the upper two bytes. Variable indexes are computed by taking
     * variable sizes into account - longs and doubles take two slots, while
     * variable numbers are computed without taking this size into account).
     */
    private HashMap locals = new HashMap();

    /**
     * Types of the local variables of the method visited by this adapter. This
     * array is indexed by local variable indexes (i.e. long and double
     * variables uses two array elements).
     */
    protected final List localTypes = new ArrayList();

    /**
     * Index of the first local variable, after formal parameters.
     */
    protected final int firstLocal;

    /**
     * Index of the next local variable to be created by {@link #newLocal}.
     */
    private int nextLocalIndex;

    /**
     * Number of the next local variable to be created by {@link #newLocal}.
     */
    private int nextLocalNumber;

    /**
     * Number of local variables added by {@link #newLocal}. Long and double
     * variables count for only one variable.
     */
    private int addedLocals;

    /**
     * Array used to store stack map local variable types after sorting. Long
     * and double types use only one array element.
     */
    private Object[] newLocals = new Object[20];

    /**
     * Creates a new {@link LocalVariablesSorter}.
     * 
     * @param access access flags of the adapted method.
     * @param desc the method's descriptor (see {@link Type Type}).
     * @param mv the method visitor to which this adapter delegates calls.
     */
    public LocalVariablesSorter(
        final int access,
        final String desc,
        final MethodVisitor mv)
    {
        super(mv);
        Type[] args = Type.getArgumentTypes(desc);
        nextLocalIndex = ((Opcodes.ACC_STATIC & access) != 0) ? 0 : 1;
        nextLocalNumber = nextLocalIndex;
        for (int i = 0; i < args.length; i++) {
            nextLocalIndex += args[i].getSize();
            nextLocalNumber += 1;
        }
        firstLocal = nextLocalIndex;
    }

    public void visitVarInsn(final int opcode, final int var) {
        Type type;
        switch (opcode) {
            case Opcodes.LLOAD:
            case Opcodes.LSTORE:
                type = Type.LONG_TYPE;
                break;

            case Opcodes.DLOAD:
            case Opcodes.DSTORE:
                type = Type.DOUBLE_TYPE;
                break;

            case Opcodes.FLOAD:
            case Opcodes.FSTORE:
                type = Type.FLOAT_TYPE;
                break;

            case Opcodes.ILOAD:
            case Opcodes.ISTORE:
                type = Type.INT_TYPE;
                break;

            case Opcodes.ALOAD:
            case Opcodes.ASTORE:
                type = OBJECT_TYPE;
                break;

            // case RET:
            default:
                type = Type.VOID_TYPE;
        }
        mv.visitVarInsn(opcode, remap(var, type) & 0xFFFF);
    }

    public void visitIincInsn(final int var, final int increment) {
        mv.visitIincInsn(remap(var, Type.INT_TYPE) & 0xFFFF, increment);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        mv.visitMaxs(0, 0);
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        int newIndex = remap(index, 0) & 0xFFFF;
        mv.visitLocalVariable(name, desc, signature, start, end, newIndex);
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        if (type != Opcodes.F_NEW) { // uncompressed frame
            throw new IllegalStateException("ClassReader.accept() should be called with EXPAND_FRAMES flag");
        }

        int newNLocal = nLocal + addedLocals;

        // copies types from 'local' to 'newLocals'
        // 'newLocals' already contains the variables added with 'newLocal'

        int index = 0; // old local variable index
        int number = 0; // old local variable number
        for (; number < nLocal; ++number) {
            Object t = local[number];
            int size = (t == Opcodes.LONG || t == Opcodes.DOUBLE ? 2 : 1);
            int newNumber; // new local variable number
            if (index < firstLocal) {
                newNumber = number;
            } else {
                newNumber = remap(index, size) >> 16;
            }
            if (newNumber >= newNLocal) {
                newNLocal = newNumber + 1;
            }
            setFrameLocal(newNumber, t);
            index += size;
        }

        // fills in potential gaps
        for (int i = 0; i < newNLocal; ++i) {
            if (newLocals[i] == null) {
                newLocals[i] = Opcodes.TOP;
            }
        }

        mv.visitFrame(type, newNLocal, newLocals, nStack, stack);
    }

    // -------------

    /**
     * Creates a new local variable of the given type.
     * 
     * @param type the type of the local variable to be created.
     * @return the identifier of the newly created local variable.
     */
    public int newLocal(final Type type) {
        Object t;
        switch (type.getSort()) {
            case Type.BOOLEAN:
            case Type.CHAR:
            case Type.BYTE:
            case Type.SHORT:
            case Type.INT:
                t = Opcodes.INTEGER;
                break;
            case Type.FLOAT:
                t = Opcodes.FLOAT;
                break;
            case Type.LONG:
                t = Opcodes.LONG;
                break;
            case Type.DOUBLE:
                t = Opcodes.DOUBLE;
                break;
            case Type.ARRAY:
                t = type.getDescriptor();
                break;
            // case Type.OBJECT:
            default:
                t = type.getInternalName();
                break;
        }
        int local = nextLocalIndex;
        setLocalType(nextLocalIndex, type);
        setFrameLocal(nextLocalNumber, t);
        nextLocalIndex += type.getSize();
        nextLocalNumber += 1;
        ++addedLocals;
        return local;
    }

    /**
     * Returns the type of the given local variable.
     * 
     * @param local a local variable identifier, as returned by {@link #newLocal
     *        newLocal()}.
     * @return the type of the given local variable.
     */
    public Type getLocalType(final int local) {
        return (Type) localTypes.get(local - firstLocal);
    }

    /**
     * Sets the current type of the given local variable.
     * 
     * @param local a local variable identifier, as returned by {@link #newLocal
     *        newLocal()}.
     * @param type the type of the value being stored in the local variable
     */
    protected void setLocalType(final int local, final Type type) {
        int index = local - firstLocal;
        while (localTypes.size() < index + 1) {
            localTypes.add(null);
        }
        localTypes.set(index, type);
    }

    private void setFrameLocal(final int local, final Object type) {
        int l = newLocals.length;
        if (local >= l) {
            Object[] a = new Object[Math.max(2 * l, local + 1)];
            System.arraycopy(newLocals, 0, a, 0, l);
            newLocals = a;
        }
        newLocals[local] = type;
    }

    private int remap(final int var, final Type type) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(type.getSize() == 2 ? ~var : var);
        Integer value = (Integer) locals.get(key);
        if (value == null) {
            value = new Integer(nextLocalIndex | (nextLocalNumber << 16));
            setLocalType(nextLocalIndex, type);
            locals.put(key, value);
            nextLocalIndex += type.getSize();
            nextLocalNumber += 1;
        }
        return value.intValue();
    }

    private int remap(final int var, final int size) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(size == 2 ? ~var : var);
        Integer value = (Integer) locals.get(key);
        if (value == null && size == 0) {
            key = new Integer(~var);
            value = (Integer) locals.get(key);
        }
        if (value == null) {
            throw new IllegalStateException("Unknown local variable " + var);
        }
        return value.intValue();
    }
}
