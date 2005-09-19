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
import java.util.Map;

import org.objectweb.asm.FrameVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * 
 * @author Eric Bruneton
 */
public class BasicFramesGenerator extends MethodAdapter implements FrameVisitor
{

    public final List locals;

    public final List stack;

    protected boolean delegate;

    private FrameVisitor fv;

    private int index;

    private List labels;

    private Map uninitializedTypes;

    private final static Integer TOP = new Integer(FrameVisitor.TOP);

    private final static Integer INTEGER = new Integer(FrameVisitor.INTEGER);

    private final static Integer FLOAT = new Integer(FrameVisitor.FLOAT);

    private final static Integer DOUBLE = new Integer(FrameVisitor.DOUBLE);

    private final static Integer LONG = new Integer(FrameVisitor.LONG);

    private final static Integer NULL = new Integer(FrameVisitor.NULL);

    private final static Integer UNINITIALIZED_THIS = new Integer(FrameVisitor.UNINITIALIZED_THIS);

    private final static Integer[] TYPES = {
        TOP,
        INTEGER,
        FLOAT,
        DOUBLE,
        LONG,
        NULL,
        UNINITIALIZED_THIS };

    public BasicFramesGenerator(
        final String owner,
        final int access,
        final String name,
        final String desc,
        final MethodVisitor mv)
    {
        super(mv);
        locals = new ArrayList();
        stack = new ArrayList();
        delegate = true;
        uninitializedTypes = new HashMap();

        if ((access & Opcodes.ACC_STATIC) == 0) {
            if (name.equals("<init>")) {
                locals.add(UNINITIALIZED_THIS);
            } else {
                locals.add("L" + owner + ";");
            }
        }
        Type[] types = Type.getArgumentTypes(desc);
        for (int i = 0; i < types.length; ++i) {
            switch (types[i].getSort()) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    locals.add(INTEGER);
                    break;
                case Type.FLOAT:
                    locals.add(FLOAT);
                    break;
                case Type.LONG:
                    locals.add(LONG);
                    locals.add(TOP);
                    break;
                case Type.DOUBLE:
                    locals.add(DOUBLE);
                    locals.add(TOP);
                    break;
                default:
                    locals.add(types[i].getDescriptor());
            }
        }
    }

    public FrameVisitor visitFrame(final int nLocal, final int nStack) {
        fv = mv.visitFrame(nLocal, nStack);
        index = nLocal;
        locals.clear();
        stack.clear();
        return this;
    }

    public void visitPrimitiveType(final int type) {
        List l = index-- <= 0 ? stack : locals;
        l.add(TYPES[type]);
        if (type == FrameVisitor.LONG || type == FrameVisitor.DOUBLE) {
            l.add(TOP);
        }
        if (fv != null) {
            fv.visitPrimitiveType(type);
        }
    }

    public void visitReferenceType(final String type) {
        (index-- <= 0 ? stack : locals).add(type);
        if (fv != null) {
            fv.visitReferenceType(type);
        }
    }

    public void visitUninitializedType(final Label newInsn) {
        (index-- <= 0 ? stack : locals).add(newInsn);
        if (fv != null) {
            fv.visitUninitializedType(newInsn);
        }
    }

    public void visitInsn(final int opcode) {
        execute(opcode, 0, null);
        if (delegate) {
            mv.visitInsn(opcode);
        }
    }

    public void visitIntInsn(final int opcode, final int operand) {
        execute(opcode, operand, null);
        if (delegate) {
            mv.visitIntInsn(opcode, operand);
        }
    }

    public void visitVarInsn(final int opcode, final int var) {
        execute(opcode, var, null);
        if (delegate) {
            mv.visitVarInsn(opcode, var);
        }
    }

    public void visitTypeInsn(final int opcode, final String desc) {
        execute(opcode, 0, desc);
        if (delegate) {
            mv.visitTypeInsn(opcode, desc);
        }
    }

    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        execute(opcode, 0, desc);
        if (delegate) {
            mv.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        pop(desc);
        if (opcode != Opcodes.INVOKESTATIC) {
            Object t = pop();
            if (opcode == Opcodes.INVOKESPECIAL && name.charAt(0) == '<') {
                Object u;
                if (t == UNINITIALIZED_THIS) {
                    u = owner;
                } else {
                    u = uninitializedTypes.get(t);
                }
                for (int i = 0; i < locals.size(); ++i) {
                    if (locals.get(i) == t) {
                        locals.set(i, u);
                    }
                }
                for (int i = 0; i < stack.size(); ++i) {
                    if (stack.get(i) == t) {
                        stack.set(i, u);
                    }
                }
            }
        }
        pushDesc(desc);
        labels = null;
        if (delegate) {
            mv.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public void visitJumpInsn(final int opcode, final Label label) {
        execute(opcode, 0, null);
        if (delegate) {
            mv.visitJumpInsn(opcode, label);
        }
    }

    public void visitLabel(final Label label) {
        if (labels == null) {
            labels = new ArrayList();
        }
        labels.add(label);
        if (delegate) {
            mv.visitLabel(label);
        }
    }

    public void visitLdcInsn(final Object cst) {
        if (cst instanceof Integer) {
            push(INTEGER);
        } else if (cst instanceof Long) {
            push(LONG);
            push(TOP);
        } else if (cst instanceof Float) {
            push(FLOAT);
        } else if (cst instanceof Double) {
            push(DOUBLE);
            push(TOP);
        } else if (cst instanceof String) {
            pushDesc("Ljava/lang/String;");
        } else if (cst instanceof Type) {
            pushDesc("Ljava/lang/Class;");
        } else {
            throw new IllegalArgumentException();
        }
        labels = null;
        if (delegate) {
            mv.visitLdcInsn(cst);
        }
    }

    public void visitIincInsn(final int var, final int increment) {
        execute(Opcodes.IINC, var, null);
        if (delegate) {
            mv.visitIincInsn(var, increment);
        }
    }

    public void visitTableSwitchInsn(
        final int min,
        final int max,
        final Label dflt,
        final Label labels[])
    {
        execute(Opcodes.TABLESWITCH, 0, null);
        if (delegate) {
            mv.visitTableSwitchInsn(min, max, dflt, labels);
        }
    }

    public void visitLookupSwitchInsn(
        final Label dflt,
        final int keys[],
        final Label labels[])
    {
        execute(Opcodes.LOOKUPSWITCH, 0, null);
        if (delegate) {
            mv.visitLookupSwitchInsn(dflt, keys, labels);
        }
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        execute(Opcodes.MULTIANEWARRAY, dims, desc);
        if (delegate) {
            mv.visitMultiANewArrayInsn(desc, dims);
        }
    }

    // ------------------------------------------------------------------------

    private Object get(final int local) {
        return local < locals.size() ? locals.get(local) : TOP;
    }

    private void set(final int local, final Object type) {
        while (local >= locals.size()) {
            locals.add(TOP);
        }
        locals.set(local, type);
    }

    private void push(final Object type) {
        stack.add(type);
    }

    private void pushDesc(final String desc) {
        int index = desc.charAt(0) == '(' ? desc.indexOf(')') + 1 : 0;
        switch (desc.charAt(index)) {
            case 'V':
                return;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                push(INTEGER);
                return;
            case 'F':
                push(FLOAT);
                return;
            case 'J':
                push(LONG);
                push(TOP);
                return;
            case 'D':
                push(DOUBLE);
                push(TOP);
                return;
            // case 'L':
            // case '[':
            default:
                if (index == 0) {
                    push(desc);
                } else {
                    push(desc.substring(index, desc.length()));
                }
                return;
        }
    }

    private Object pop() {
        return stack.remove(stack.size() - 1);
    }

    private void pop(final int n) {
        int size = stack.size();
        int end = size - n;
        for (int i = size - 1; i >= end; --i) {
            stack.remove(i);
        }
    }

    private void pop(final String desc) {
        char c = desc.charAt(0);
        if (c == '(') {
            int n = 0;
            Type[] types = Type.getArgumentTypes(desc);            
            for (int i = 0; i < types.length; ++i) {
                n += types[i].getSize();
            }
            pop(n);
        } else if (c == 'J' || c == 'D') {
            pop(2);
        } else {
            pop(1);
        }
    }

    private void execute(final int opcode, final int iarg, final String sarg) {
        Object t1, t2, t3, t4;
        switch (opcode) {
            case Opcodes.NOP:
            case Opcodes.INEG:
            case Opcodes.LNEG:
            case Opcodes.FNEG:
            case Opcodes.DNEG:
            case Opcodes.I2B:
            case Opcodes.I2C:
            case Opcodes.I2S:
            case Opcodes.GOTO:
            case Opcodes.RETURN:
                break;
            case Opcodes.ACONST_NULL:
                push(NULL);
                break;
            case Opcodes.ICONST_M1:
            case Opcodes.ICONST_0:
            case Opcodes.ICONST_1:
            case Opcodes.ICONST_2:
            case Opcodes.ICONST_3:
            case Opcodes.ICONST_4:
            case Opcodes.ICONST_5:
            case Opcodes.BIPUSH:
            case Opcodes.SIPUSH:
                push(INTEGER);
                break;
            case Opcodes.LCONST_0:
            case Opcodes.LCONST_1:
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FCONST_0:
            case Opcodes.FCONST_1:
            case Opcodes.FCONST_2:
                push(FLOAT);
                break;
            case Opcodes.DCONST_0:
            case Opcodes.DCONST_1:
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.ALOAD:
                push(get(iarg));
                break;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                push(get(iarg));
                push(TOP);
                break;
            case Opcodes.IALOAD:
            case Opcodes.BALOAD:
            case Opcodes.CALOAD:
            case Opcodes.SALOAD:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LALOAD:
            case Opcodes.D2L:
                pop(2);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FALOAD:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DALOAD:
            case Opcodes.L2D:
                pop(2);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.AALOAD:
                pop(1);
                t1 = pop();
                pushDesc(((String) t1).substring(1));
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                t1 = pop();
                set(iarg, t1);
                if (iarg > 0) {
                    t2 = get(iarg - 1);
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(iarg - 1, TOP);
                    }
                }
                break;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop(1);
                t1 = pop();
                set(iarg, t1);
                set(iarg + 1, TOP);
                if (iarg > 0) {
                    t2 = get(iarg - 1);
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(iarg - 1, TOP);
                    }
                }
                break;
            case Opcodes.IASTORE:
            case Opcodes.BASTORE:
            case Opcodes.CASTORE:
            case Opcodes.SASTORE:
            case Opcodes.FASTORE:
            case Opcodes.AASTORE:
                pop(3);
                break;
            case Opcodes.LASTORE:
            case Opcodes.DASTORE:
                pop(4);
                break;
            case Opcodes.POP:
            case Opcodes.IFEQ:
            case Opcodes.IFNE:
            case Opcodes.IFLT:
            case Opcodes.IFGE:
            case Opcodes.IFGT:
            case Opcodes.IFLE:
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
            case Opcodes.TABLESWITCH:
            case Opcodes.LOOKUPSWITCH:
            case Opcodes.ATHROW:
            case Opcodes.MONITORENTER:
            case Opcodes.MONITOREXIT:
            case Opcodes.IFNULL:
            case Opcodes.IFNONNULL:
                pop(1);
                break;
            case Opcodes.POP2:
            case Opcodes.IF_ICMPEQ:
            case Opcodes.IF_ICMPNE:
            case Opcodes.IF_ICMPLT:
            case Opcodes.IF_ICMPGE:
            case Opcodes.IF_ICMPGT:
            case Opcodes.IF_ICMPLE:
            case Opcodes.IF_ACMPEQ:
            case Opcodes.IF_ACMPNE:
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                pop(2);
                break;
            case Opcodes.DUP:
                t1 = pop();
                push(t1);
                push(t1);
                break;
            case Opcodes.DUP_X1:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2:
                t1 = pop();
                t2 = pop();
                push(t2);
                push(t1);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X1:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                push(t2);
                push(t1);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.DUP2_X2:
                t1 = pop();
                t2 = pop();
                t3 = pop();
                t4 = pop();
                push(t2);
                push(t1);
                push(t4);
                push(t3);
                push(t2);
                push(t1);
                break;
            case Opcodes.SWAP:
                t1 = pop();
                t2 = pop();
                push(t1);
                push(t2);
                break;
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
            case Opcodes.IAND:
            case Opcodes.IOR:
            case Opcodes.IXOR:
            case Opcodes.ISHL:
            case Opcodes.ISHR:
            case Opcodes.IUSHR:
            case Opcodes.L2I:
            case Opcodes.D2I:
            case Opcodes.FCMPL:
            case Opcodes.FCMPG:
                pop(2);
                push(INTEGER);
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
            case Opcodes.LAND:
            case Opcodes.LOR:
            case Opcodes.LXOR:
                pop(4);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
            case Opcodes.L2F:
            case Opcodes.D2F:
                pop(2);
                push(FLOAT);
                break;
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                pop(4);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.LSHL:
            case Opcodes.LSHR:
            case Opcodes.LUSHR:
                pop(3);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.IINC:
                set(iarg, INTEGER);
                break;
            case Opcodes.I2L:
            case Opcodes.F2L:
                pop(1);
                push(LONG);
                push(TOP);
                break;
            case Opcodes.I2F:
                pop(1);
                push(FLOAT);
                break;
            case Opcodes.I2D:
            case Opcodes.F2D:
                pop(1);
                push(DOUBLE);
                push(TOP);
                break;
            case Opcodes.F2I:
            case Opcodes.ARRAYLENGTH:
            case Opcodes.INSTANCEOF:
                pop(1);
                push(INTEGER);
                break;
            case Opcodes.LCMP:
            case Opcodes.DCMPL:
            case Opcodes.DCMPG:
                pop(4);
                push(INTEGER);
                break;
            case Opcodes.JSR:
            case Opcodes.RET:
                throw new RuntimeException("JSR/RET are not supported");
            case Opcodes.GETSTATIC:
                pushDesc(sarg);
                break;
            case Opcodes.PUTSTATIC:
                pop(sarg);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                pushDesc(sarg);
                break;
            case Opcodes.PUTFIELD:
                pop(sarg);
                pop();
                break;
            case Opcodes.NEW:
                Label l;
                if (labels == null) {
                    l = new Label();
                    if (delegate) {
                        mv.visitLabel(l);
                    }
                    labels = new ArrayList();
                    labels.add(l);
                } else {
                    l = (Label) labels.get(0);
                }
                for (int i = 0; i < labels.size(); ++i) {
                    uninitializedTypes.put(labels.get(i), sarg);
                }
                push(l);
                break;
            case Opcodes.NEWARRAY:
                pop();
                switch (iarg) {
                    case Opcodes.T_BOOLEAN:
                        pushDesc("[Z");
                        break;
                    case Opcodes.T_CHAR:
                        pushDesc("[C");
                        break;
                    case Opcodes.T_BYTE:
                        pushDesc("[B");
                        break;
                    case Opcodes.T_SHORT:
                        pushDesc("[S");
                        break;
                    case Opcodes.T_INT:
                        pushDesc("[I");
                        break;
                    case Opcodes.T_FLOAT:
                        pushDesc("[F");
                        break;
                    case Opcodes.T_DOUBLE:
                        pushDesc("[D");
                        break;
                    // case Opcodes.T_LONG:
                    default:
                        pushDesc("[J");
                        break;
                }
                break;
            case Opcodes.ANEWARRAY:
                pop();
                pushDesc("[" + sarg);
                break;
            case Opcodes.CHECKCAST:
                pop();
                pushDesc(sarg);
                break;
            // case Opcodes.MULTIANEWARRAY:
            default:
                pop(iarg);
                pushDesc(sarg);
                break;
        }
        labels = null;
    }
}
