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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * A <code>MethodAdapter</code> to dispatch method body instruction
 * <p>
 * The behavior is like this:
 * <ol>
 * 
 * <li>as long as the INVOKESPECIAL for the object initialization has not been
 *     reached, every bytecode instruction is dispatched in the ctor code visitor</li>
 * 
 * <li>when this one is reached, it is only added in the ctor code visitor and
 *     a JP invoke is added</li>
 * <li>after that, only the other code visitor receives the instructions</li>
 * 
 * </ol>
 * 
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public abstract class AdviceAdapter extends MethodAdapter implements Opcodes {
    private static final Object THIS = new Object();
    private static final Object OTHER = new Object();

    protected int methodAccess;
    protected String methodName;
    protected String methodDesc;
    private boolean constructor;
    private boolean superInitialized;
    
    private ArrayList stackFrame;
    private HashMap branches;

    
    public AdviceAdapter(MethodVisitor mv, int access, String name, String desc) {
        super(mv);
        methodAccess = access;
        methodName = name;
        methodDesc = desc;

        constructor = "<init>".equals(name);
        if (!constructor) {
            superInitialized = true;
            onMethodEnter();
        } else {
            stackFrame = new ArrayList();
            branches = new HashMap();
        }
    }

    public void visitLabel(Label label) {
        mv.visitLabel(label);

        if (constructor && branches != null) {
            ArrayList frame = (ArrayList) branches.get(label);
            if (frame != null) {
                stackFrame = frame;
                branches.remove(label);
            }
        }
    }

    public void visitInsn(int opcode) {
        if (constructor) {
            switch (opcode) {
                case RETURN: // empty stack
                    onMethodExit(opcode);
                    break;

                case IRETURN: // 1 before n/a after
                case FRETURN: // 1 before n/a after
                case ARETURN: // 1 before n/a after
                case ATHROW: // 1 before n/a after
                    pop();
                    pop();
                    onMethodExit(opcode);
                    break;

                case LRETURN: // 2 before n/a after
                case DRETURN: // 2 before n/a after
                    pop();
                    pop();
                    onMethodExit(opcode);
                    break;

                case NOP:
                case LALOAD: // remove 2 add 2
                case DALOAD: // remove 2 add 2
                case LNEG:
                case DNEG:
                case FNEG:
                case INEG:
                case L2D:
                case D2L:
                case F2I:
                case I2B:
                case I2C:
                case I2S:
                case I2F:
                case Opcodes.ARRAYLENGTH:
                    break;

                case ACONST_NULL:
                case ICONST_M1:
                case ICONST_0:
                case ICONST_1:
                case ICONST_2:
                case ICONST_3:
                case ICONST_4:
                case ICONST_5:
                case FCONST_0:
                case FCONST_1:
                case FCONST_2:
                case F2L: // 1 before 2 after
                case F2D:
                case I2L:
                case I2D:
                    push(OTHER);
                    break;

                case LCONST_0:
                case LCONST_1:
                case DCONST_0:
                case DCONST_1:
                    push(OTHER);
                    push(OTHER);
                    break;

                case IALOAD: // remove 2 add 1
                case FALOAD: // remove 2 add 1
                case AALOAD: // remove 2 add 1
                case BALOAD: // remove 2 add 1
                case CALOAD: // remove 2 add 1
                case SALOAD: // remove 2 add 1
                case POP:
                case IADD:
                case FADD:
                case ISUB:
                case LSHL: // 3 before 2 after
                case LSHR: // 3 before 2 after
                case LUSHR: // 3 before 2 after
                case L2I: // 2 before 1 after
                case L2F: // 2 before 1 after
                case D2I: // 2 before 1 after
                case D2F: // 2 before 1 after
                case FSUB:
                case FMUL:
                case FDIV:
                case FREM:
                case FCMPL: // 2 before 1 after
                case FCMPG: // 2 before 1 after
                case IMUL:
                case IDIV:
                case IREM:
                case ISHL:
                case ISHR:
                case IUSHR:
                case IAND:
                case IOR:
                case IXOR:
                case MONITORENTER:
                case MONITOREXIT:
                    pop();
                    break;

                case POP2:
                case LSUB:
                case LMUL:
                case LDIV:
                case LREM:
                case LADD:
                case LAND:
                case LOR:
                case LXOR:
                case DADD:
                case DMUL:
                case DSUB:
                case DDIV:
                case DREM:
                    pop();
                    pop();
                    break;

                case IASTORE:
                case FASTORE:
                case AASTORE:
                case BASTORE:
                case CASTORE:
                case SASTORE:
                case LCMP: // 4 before 1 after
                case DCMPL:
                case DCMPG:
                    pop();
                    pop();
                    pop();
                    break;

                case LASTORE:
                case DASTORE:
                    pop();
                    pop();
                    pop();
                    pop();
                    break;

                case DUP:
                    push(peek());
                    break;

                case DUP_X1:
                // TODO optimize this
                {
                    Object o1 = pop();
                    Object o2 = pop();
                    push(o1);
                    push(o2);
                    push(o1);
                }
                    break;

                case DUP_X2:
                // TODO optimize this
                {
                    Object o1 = pop();
                    Object o2 = pop();
                    Object o3 = pop();
                    push(o1);
                    push(o3);
                    push(o2);
                    push(o1);
                }
                    break;

                case DUP2:
                // TODO optimize this
                {
                    Object o1 = pop();
                    Object o2 = pop();
                    push(o2);
                    push(o1);
                    push(o2);
                    push(o1);
                }
                    break;

                case DUP2_X1:
                // TODO optimize this
                {
                    Object o1 = pop();
                    Object o2 = pop();
                    Object o3 = pop();
                    push(o2);
                    push(o1);
                    push(o3);
                    push(o2);
                    push(o1);
                }
                    break;

                case DUP2_X2:
                // TODO optimize this
                {
                    Object o1 = pop();
                    Object o2 = pop();
                    Object o3 = pop();
                    Object o4 = pop();
                    push(o2);
                    push(o1);
                    push(o4);
                    push(o3);
                    push(o2);
                    push(o1);
                }
                    break;

                case SWAP: {
                    Object o1 = pop();
                    Object o2 = pop();
                    push(o1);
                    push(o2);
                }
                    break;
            }
        } else {
            switch (opcode) {
                case RETURN:
                case IRETURN:
                case FRETURN:
                case ARETURN:
                case LRETURN:
                case DRETURN:
                case ATHROW:
                    onMethodExit(opcode);
                    break;
            }
        }
        mv.visitInsn(opcode);
    }

    public void visitVarInsn(int opcode, int var) {
        mv.visitVarInsn(opcode, var);

        if (constructor) {
            switch (opcode) {
                case ILOAD:
                case FLOAD:
                    push(OTHER);
                    break;
                case LLOAD:
                case DLOAD:
                    push(OTHER);
                    push(OTHER);
                    break;
                case ALOAD:
                    push(var == 0 ? THIS : OTHER);
                    break;
                case ASTORE:
                case ISTORE:
                case FSTORE:
                    pop();
                    break;
                case LSTORE:
                case DSTORE:
                    pop();
                    pop();
                    break;
            }
        }
    }

    public void visitFieldInsn(
        int opcode,
        String owner,
        String name,
        String desc)
    {
        mv.visitFieldInsn(opcode, owner, name, desc);

        if (constructor) {
            char c = desc.charAt(0);
            boolean longOrDouble = c == 'J' || c == 'D';
            switch (opcode) {
                case GETSTATIC:
                    push(OTHER);
                    if (longOrDouble) {
                        push(OTHER);
                    }
                    break;
                case PUTSTATIC:
                    pop();
                    if(longOrDouble) {
                        pop();
                    }
                    break;
                case PUTFIELD:
                    pop();
                    if(longOrDouble) {
                        pop();
                        pop();
                    }
                    break;
                // case GETFIELD:
                default:
                    if (longOrDouble) {
                        push(OTHER);
                    }
            }
        }
    }

    public void visitIntInsn(int opcode, int operand) {
        mv.visitIntInsn(opcode, operand);

        if (constructor) {
            switch (opcode) {
                case BIPUSH:
                case SIPUSH:
                    push(OTHER);
            }
        }
    }

    public void visitLdcInsn(Object cst) {
        mv.visitLdcInsn(cst);

        if (constructor) {
            push(OTHER);
            if (cst instanceof Double || cst instanceof Long) {
                push(OTHER);
            }
        }
    }

    public void visitMultiANewArrayInsn(String desc, int dims) {
        mv.visitMultiANewArrayInsn(desc, dims);

        if (constructor) {
            for (int i = 0; i < dims; i++) {
                pop();
            }
            push(OTHER);
        }
    }

    public void visitTypeInsn(int opcode, String name) {
        mv.visitTypeInsn(opcode, name);

        // ANEWARRAY, CHECKCAST or INSTANCEOF don't change stack
        if (constructor && opcode == NEW) {
            push(OTHER);
        }
    }

    public void visitMethodInsn(
        int opcode,
        String owner,
        String name,
        String desc)
    {
        mv.visitMethodInsn(opcode, owner, name, desc);

        if (constructor) {
            Type[] types = Type.getArgumentTypes(desc);
            for (int i = 0; i < types.length; i++) {
                pop();
                if (types[i].getSize() == 2) {
                    pop();
                }
            }
            switch (opcode) {
                // case INVOKESTATIC:
                // break;

                case INVOKEINTERFACE:
                case INVOKEVIRTUAL:
                    pop(); // objectref
                    break;

                case INVOKESPECIAL:
                    Object type = pop(); // objectref
                    if (type == THIS && !superInitialized) {
                        onMethodEnter();
                        superInitialized = true;
                        // once super has been initialized it is no longer 
                        // necessary to keep track of stack state                        
                        constructor = false;
                    }
                    break;
            }

            Type returnType = Type.getReturnType(desc);
            if (returnType != Type.VOID_TYPE) {
                push(OTHER);
                if (returnType.getSize() == 2) {
                    push(OTHER);
                }
            }
        }
    }

    public void visitJumpInsn(int opcode, Label label) {
        mv.visitJumpInsn(opcode, label);

        if (constructor) {
            switch (opcode) {
                case IFEQ:
                case IFNE:
                case IFLT:
                case IFGE:
                case IFGT:
                case IFLE:
                case IFNULL:
                case IFNONNULL:
                    pop();
                    break;

                case IF_ICMPEQ:
                case IF_ICMPNE:
                case IF_ICMPLT:
                case IF_ICMPGE:
                case IF_ICMPGT:
                case IF_ICMPLE:
                case IF_ACMPEQ:
                case IF_ACMPNE:
                    pop();
                    pop();
                    break;

                case JSR:
                    push(OTHER);
                    break;
            }
            addBranch(label);
        }
    }

    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        mv.visitLookupSwitchInsn(dflt, keys, labels);

        if (constructor) {
            pop();
            addBranches(dflt, labels);
        }
    }

    public void visitTableSwitchInsn(
        int min,
        int max,
        Label dflt,
        Label[] labels)
    {
        mv.visitTableSwitchInsn(min, max, dflt, labels);

        if (constructor) {
            pop();
            addBranches(dflt, labels);
        }
    }

    private void addBranches(Label dflt, Label[] labels) {
        addBranch(dflt);
        for (int i = 0; i < labels.length; i++) {
            addBranch(labels[i]);
        }
    }

    private void addBranch(Label label) {
        if (branches.containsKey(label)) {
            return;
        }
        ArrayList frame = new ArrayList();
        frame.addAll(stackFrame);
        branches.put(label, frame);
    }

    private Object pop() {
        return stackFrame.remove(stackFrame.size()-1);
    }

    private Object peek() {
        return stackFrame.get(stackFrame.size()-1);
    }
    
    private void push(Object o) {
        stackFrame.add(o);
    }
    
    abstract void onMethodEnter();

    abstract void onMethodExit(int opcode);

    // TODO onException, onMethodCall
    
}

