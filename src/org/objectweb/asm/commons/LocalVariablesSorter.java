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
 */
public class LocalVariablesSorter extends MethodAdapter {

    private final static Type OBJECT_TYPE = Type.getType("Ljava/lang/Object;");
    
    /**
     * Map of <code>Integer</code> representing an old variable index (indexes
     * for long and double types that are using two slots are negated) to
     * <code>Integer</code> value representing variable index after remapping.
     */
    private HashMap locals = new HashMap();

    /**
     * List of <code>Integer</code>s representing indexes of the variables added 
     * using {@link #newLocal(Type) newLocal()}.
     */
    private List newLocals = new ArrayList();
    
    /**
     * Types of the local variables of the method visited by this adapter.
     */
    protected final List localTypes = new ArrayList();
    
    protected final int firstLocal;

    private int nextLocal;

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
        nextLocal = ((Opcodes.ACC_STATIC & access) != 0) ? 0 : 1;
        for (int i = 0; i < args.length; i++) {
            nextLocal += args[i].getSize();
        }
        firstLocal = nextLocal;
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
        mv.visitVarInsn(opcode, remap(var, type));
    }

    public void visitIincInsn(final int var, final int increment) {
        mv.visitIincInsn(remap(var, Type.INT_TYPE), increment);
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
        mv.visitLocalVariable(name, desc, signature, start, end, remap(index));
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        // TODO support packed frames
        if(type!=Opcodes.F_NEW) { // uncompressed frame
            throw new IllegalStateException("ClassReader.accept() should be called with EXPAND_FRAMES flag");
        }
        
        Object l;
        ArrayList result = new ArrayList();
        for (int i = 0; i < nLocal; i++) {
            result.add(l = local[i]);
            if (l == Opcodes.LONG || l == Opcodes.DOUBLE) {
                result.add(Opcodes.TOP);
            }
        }                

        Object[] newLocal = result.toArray(new Object[result.size() + newLocals.size()]);
        
        // add frames for old vars
        for (int i = 0; i < result.size(); i++) {
            l = result.get(i);
            int k;
            if(i < firstLocal || (l != Opcodes.TOP && l != Opcodes.NULL && l != Opcodes.UNINITIALIZED_THIS)) {
                k = remap(i);
            } else {
                Integer value = (Integer) locals.get(new Integer(i));
                if (value != null) {
                    k = value.intValue();
                } else {
                    k = nextLocal;
                    locals.put(new Integer(i), new Integer(k));
                    nextLocal++;
                }
            }
            if(newLocal.length<=(k+1)) {
                Object[] t = newLocal;
                newLocal = new Object[k + 2];
                System.arraycopy(t, 0, newLocal, 0, t.length);
            }
            newLocal[k] = l;
            if(l == Opcodes.LONG || l == Opcodes.DOUBLE) {
                newLocal[k+1] = Opcodes.TOP;
                i++;
            }
        }

        // add frames for vars added using newLocal() method
        for (int i = 0; i < newLocals.size(); i++) {
            int var = ((Integer) newLocals.get(i)).intValue();
            Object frameType;
            Type localType = (Type) localTypes.get(var - firstLocal);
            switch (localType.getSort()) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    frameType = Opcodes.INTEGER;
                    break;
                case Type.FLOAT:
                    frameType = Opcodes.FLOAT;
                    break;
                case Type.LONG:
                    frameType = Opcodes.LONG;
                    break;
                case Type.DOUBLE:
                    frameType = Opcodes.DOUBLE;
                    break;
                // case Type.ARRAY:
                // case Type.OBJECT:
                default:
                    frameType = localType.getDescriptor();
                    break;
            }

            newLocal[var] = frameType;
        }
        
        // TODO strip TOP elements after LONG and DOUBLE frames
        
        mv.visitFrame(type, nLocal + newLocals.size(), newLocal, nStack, stack);
    }
    
    // -------------

    /**
     * Creates a new local variable of the given type.
     * 
     * @param type the type of the local variable to be created.
     * @return the identifier of the newly created local variable.
     */
    public int newLocal(final Type type) {
        int local = nextLocal;
        setLocalType(local, type);
        newLocals.add(new Integer(local));
        nextLocal += type.getSize();
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
        while (localTypes.size() < index + 1)
            localTypes.add(null);
        localTypes.set(index, type);
    }

    private int remap(final int var, final Type type) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(type.getSize() == 2 ? ~var : var);
        Integer value = (Integer) locals.get(key);
        if (value == null) {
            value = new Integer(nextLocal);
            setLocalType(nextLocal, type);
            locals.put(key, value);
            nextLocal += type.getSize();
        }
        return value.intValue();
    }

    private int remap(final int var) {
        if (var < firstLocal) {
            return var;
        }
        Integer key = new Integer(var);
        Integer value = (Integer) locals.get(key);
        if (value == null) {
            key = new Integer(~var);
            value = (Integer) locals.get(key);
            if (value == null) {
                throw new IllegalStateException("Unknown local variable " + var);
            }
        }
        return value.intValue();
    }
}
