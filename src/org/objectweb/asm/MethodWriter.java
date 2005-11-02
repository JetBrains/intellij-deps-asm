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
package org.objectweb.asm;

/**
 * A {@link MethodVisitor} that generates methods in bytecode form. Each visit
 * method of this class appends the bytecode corresponding to the visited
 * instruction to a byte vector, in the order these methods are called.
 * 
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
class MethodWriter implements MethodVisitor {

    /**
     * Pseudo access flag used to denote constructors.
     */
    final static int ACC_CONSTRUCTOR = 262144;

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is zero.
     */
    static final int SAME_FRAME = 0; // to 63 (0-3f)

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is 1
     */
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME = 64; // to 127 (40-7f)

    /**
     * Reserved for future use
     */
    static final int RESERVED = 128;

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is 1. Offset is bigger then 63;
     */
    static final int SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED = 247; // f7

    /**
     * Frame where current locals are the same as the locals in the previous
     * frame, except that the k last locals are absent. The value of k is given
     * by the formula 251-frame_type.
     */
    static final int CHOP_FRAME = 248; // to 250 (f8-fA)

    /**
     * Frame has exactly the same locals as the previous stack map frame and
     * number of stack items is zero. Offset is bigger then 63;
     */
    static final int SAME_FRAME_EXTENDED = 251; // fb

    /**
     * Frame where current locals are the same as the locals in the previous
     * frame, except that k additional locals are defined. The value of k is
     * given by the formula frame_type-251.
     */
    static final int APPEND_FRAME = 252; // to 254 // fc-fe

    /**
     * Full frame
     */
    static final int FULL_FRAME = 255; // ff

    /**
     * Next method writer (see {@link ClassWriter#firstMethod firstMethod}).
     */
    MethodWriter next;

    /**
     * The class writer to which this method must be added.
     */
    private ClassWriter cw;

    /**
     * Access flags of this method.
     */
    private int access;

    /**
     * The index of the constant pool item that contains the name of this
     * method.
     */
    private int name;

    /**
     * The index of the constant pool item that contains the descriptor of this
     * method.
     */
    private int desc;

    /**
     * The descriptor of this method.
     */
    private String descriptor;

    /**
     * The index of the constant pool item that contains the signature of this
     * method.
     */
    private int signature;

    /**
     * Number of exceptions that can be thrown by this method.
     */
    private int exceptionCount;

    /**
     * The exceptions that can be thrown by this method. More precisely, this
     * array contains the indexes of the constant pool items that contain the
     * internal names of these exception classes.
     */
    private int[] exceptions;

    /**
     * The annotation default attribute of this method. May be <tt>null</tt>.
     */
    private ByteVector annd;

    /**
     * The runtime visible annotations of this method. May be <tt>null</tt>.
     */
    private AnnotationWriter anns;

    /**
     * The runtime invisible annotations of this method. May be <tt>null</tt>.
     */
    private AnnotationWriter ianns;

    /**
     * The runtime visible parameter annotations of this method. May be
     * <tt>null</tt>.
     */
    private AnnotationWriter[] panns;

    /**
     * The runtime invisible parameter annotations of this method. May be
     * <tt>null</tt>.
     */
    private AnnotationWriter[] ipanns;

    /**
     * The non standard attributes of the method.
     */
    private Attribute attrs;

    /**
     * The bytecode of this method.
     */
    private ByteVector code = new ByteVector();

    /**
     * Maximum stack size of this method.
     */
    private int maxStack;

    /**
     * Maximum number of local variables for this method.
     */
    private int maxLocals;

    /**
     * Number of stack map frames in the StackMapTable attribute.
     */
    private int frameCount;

    /**
     * <tt>true</tt> if the frames must be stored from a compressed
     * StackMapTable attribute, or <tt>false</tt> if they must be stored from
     * an uncompressed StackMap attribute.
     */
    private boolean zip;

    /**
     * The StackMapTable attriute.
     */
    private ByteVector stackMap;

    /**
     * The offset of the last frame that was written in the StackMapTable
     * attribute.
     */
    private int previousFrameOffset;

    /**
     * TODO.
     */
    private int frameIndex;
    
    /**
     * The last frame that was written in the StackMapTable attribute.
     * 
     * @see #frame
     */
    private int[] previousFrame;

    /**
     * The current stack map frame. The first element contains the offset of the
     * instruction to which the frame corresponds, the second element is the
     * number of locals and the third one is the number of stack elements. The
     * local variables start at index 3 and are followed by the operand stack
     * values. In summary frame[0] = offset, frame[1] = nLocal, frame[2] =
     * nStack, frame[3] = nLocal. All types are encoded as integers, with the
     * same format as the one used in {@link Label}, but limited to BASE types.
     */
    private int[] frame;

    /**
     * Number of entries in the catch table of this method.
     */
    private int catchCount;

    /**
     * The catch table of this method.
     */
    private Handler catchTable;

    /**
     * The last element in the catchTable handler list.
     */
    private Handler lastHandler;

    /**
     * Number of entries in the LocalVariableTable attribute.
     */
    private int localVarCount;

    /**
     * The LocalVariableTable attribute.
     */
    private ByteVector localVar;

    /**
     * Number of entries in the LocalVariableTypeTable attribute.
     */
    private int localVarTypeCount;

    /**
     * The LocalVariableTypeTable attribute.
     */
    private ByteVector localVarType;

    /**
     * Number of entries in the LineNumberTable attribute.
     */
    private int lineNumberCount;

    /**
     * The LineNumberTable attribute.
     */
    private ByteVector lineNumber;

    /**
     * The non standard attributes of the method's code.
     */
    private Attribute cattrs;

    /**
     * Indicates if some jump instructions are too small and need to be resized.
     */
    private boolean resize;

    // ------------------------------------------------------------------------

    /*
     * Fields for the control flow graph analysis algorithm (used to compute the
     * maximum stack size). A control flow graph contains one node per "basic
     * block", and one edge per "jump" from one basic block to another. Each
     * node (i.e., each basic block) is represented by the Label object that
     * corresponds to the first instruction of this basic block. Each node also
     * stores the list of its successors in the graph, as a linked list of Edge
     * objects.
     */

    /**
     * <tt>true</tt> if the maximum stack size and number of local variables
     * must be automatically computed.
     */
    private final boolean computeMaxs;

    /**
     * <tt>true</tt> if the stack map frames must be recomputed from scratch.
     * In this case the maximum stack size and number of local variables is also
     * recomputed from scratch, i.e. {@link #computeMaxs} is also <tt>true</tt>.
     */
    private final boolean computeFrames;

    /**
     * The first basic block of the method.
     */
    private Label startLabel;

    /**
     * The last visited label.
     */
    private Label lastLabel;

    /**
     * The current basic block.
     */
    private Label currentBlock;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructs a new {@link MethodWriter}.
     * 
     * @param cw the class writer in which the method must be added.
     * @param access the method's access flags (see {@link Opcodes}).
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type}).
     * @param signature the method's signature. May be <tt>null</tt>.
     * @param exceptions the internal names of the method's exceptions. May be
     *        <tt>null</tt>.
     * @param computeMaxs <tt>true</tt> if the maximum stack size and number
     *        of local variables must be automatically computed.
     * @param computeFrames <tt>true</tt> if the stack map tables must be
     *        recomputed from scratch.
     */
    MethodWriter(
        final ClassWriter cw,
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions,
        final boolean computeMaxs,
        final boolean computeFrames)
    {
        if (cw.firstMethod == null) {
            cw.firstMethod = this;
        } else {
            cw.lastMethod.next = this;
        }
        cw.lastMethod = this;
        this.cw = cw;
        this.access = access;
        this.name = cw.newUTF8(name);
        this.desc = cw.newUTF8(desc);
        this.descriptor = desc;
        if (signature != null) {
            this.signature = cw.newUTF8(signature);
        }
        if (exceptions != null && exceptions.length > 0) {
            exceptionCount = exceptions.length;
            this.exceptions = new int[exceptionCount];
            for (int i = 0; i < exceptionCount; ++i) {
                this.exceptions[i] = cw.newClass(exceptions[i]);
            }
        }
        this.computeMaxs = computeMaxs || computeFrames;
        this.computeFrames = computeFrames;
        this.zip = (cw.version & 0xFFFF) >= Opcodes.V1_6;
        if (this.computeMaxs) {
            if (this.computeFrames) {
                if (name.equals("<init>")) {
                    this.access |= ACC_CONSTRUCTOR;
                }
            }
            // updates maxLocals
            int size = getArgumentsAndReturnSizes(descriptor) >> 2;
            if ((access & Opcodes.ACC_STATIC) != 0) {
                --size;
            }
            maxLocals = size;
            // creates and visits the label for the first basic block
            startLabel = new Label();
            visitLabel(startLabel);
        }
    }

    // ------------------------------------------------------------------------
    // Implementation of the MethodVisitor interface
    // ------------------------------------------------------------------------

    public AnnotationVisitor visitAnnotationDefault() {
        annd = new ByteVector();
        return new AnnotationWriter(cw, false, annd, null, 0);
    }

    public AnnotationVisitor visitAnnotation(
        final String desc,
        final boolean visible)
    {
        ByteVector bv = new ByteVector();
        // write type, and reserve space for values count
        bv.putShort(cw.newUTF8(desc)).putShort(0);
        AnnotationWriter aw = new AnnotationWriter(cw, true, bv, bv, 2);
        if (visible) {
            aw.next = anns;
            anns = aw;
        } else {
            aw.next = ianns;
            ianns = aw;
        }
        return aw;
    }

    public AnnotationVisitor visitParameterAnnotation(
        final int parameter,
        final String desc,
        final boolean visible)
    {
        ByteVector bv = new ByteVector();
        // write type, and reserve space for values count
        bv.putShort(cw.newUTF8(desc)).putShort(0);
        AnnotationWriter aw = new AnnotationWriter(cw, true, bv, bv, 2);
        if (visible) {
            if (panns == null) {
                panns = new AnnotationWriter[Type.getArgumentTypes(descriptor).length];
            }
            aw.next = panns[parameter];
            panns[parameter] = aw;
        } else {
            if (ipanns == null) {
                ipanns = new AnnotationWriter[Type.getArgumentTypes(descriptor).length];
            }
            aw.next = ipanns[parameter];
            ipanns[parameter] = aw;
        }
        return aw;
    }

    public void visitAttribute(final Attribute attr) {
        if (attr.isCodeAttribute()) {
            attr.next = cattrs;
            cattrs = attr;
        } else {
            attr.next = attrs;
            attrs = attr;
        }
    }

    public void visitCode() {
    }

    public void visitFrame(
        final int type,
        final int nLocal,
        final Object[] local,
        final int nStack,
        final Object[] stack)
    {
        if (computeFrames) {
            return;
        }

        if (type == Opcodes.F_NEW) {
            startFrame(code.length, nLocal, nStack);
            for (int i = 0; i < nLocal; ++i) {
                if (local[i] instanceof String) {
                    frame[frameIndex++] = Label.OBJECT
                            | cw.addType((String) local[i]);
                } else if (local[i] instanceof Integer) {
                    frame[frameIndex++] = ((Integer) local[i]).intValue();
                } else {
                    frame[frameIndex++] = Label.UNINITIALIZED
                            | cw.addUninitializedType("",
                                    ((Label) local[i]).position);
                }
            }
            for (int i = 0; i < nStack; ++i) {
                if (stack[i] instanceof String) {
                    frame[frameIndex++] = Label.OBJECT
                            | cw.addType((String) stack[i]);
                } else if (stack[i] instanceof Integer) {
                    frame[frameIndex++] = ((Integer) stack[i]).intValue();
                } else {
                    frame[frameIndex++] = Label.UNINITIALIZED
                            | cw.addUninitializedType("",
                                    ((Label) stack[i]).position);
                }
            }
            endFrame();
        } else {
            int delta;
            if (stackMap == null) {
                stackMap = new ByteVector();
                delta = code.length;
            } else {
                delta = code.length - previousFrameOffset - 1;
            }

            switch (type) {
                case Opcodes.F_FULL:
                    stackMap.putByte(FULL_FRAME)
                            .putShort(delta)
                            .putShort(nLocal);
                    for (int i = 0; i < nLocal; ++i) {
                        writeFrameType(local[i]);
                    }
                    stackMap.putShort(nStack);
                    for (int i = 0; i < nStack; ++i) {
                        writeFrameType(stack[i]);
                    }
                    break;
                case Opcodes.F_APPEND:
                    stackMap.putByte(SAME_FRAME_EXTENDED + nLocal)
                            .putShort(delta);
                    for (int i = 0; i < nLocal; ++i) {
                        writeFrameType(local[i]);
                    }
                    break;
                case Opcodes.F_CHOP:
                    stackMap.putByte(SAME_FRAME_EXTENDED - nLocal)
                            .putShort(delta);
                    break;
                case Opcodes.F_SAME:
                    if (delta < 64) {
                        stackMap.putByte(delta);
                    } else {
                        stackMap.putByte(SAME_FRAME_EXTENDED).putShort(delta);
                    }
                    break;
                case Opcodes.F_SAME1:
                    if (delta < 64) {
                        stackMap.putByte(SAME_LOCALS_1_STACK_ITEM_FRAME + delta);
                    } else {
                        stackMap.putByte(SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
                                .putShort(delta);
                    }
                    writeFrameType(stack[0]);
                    break;
            }

            previousFrameOffset = code.length;
            ++frameCount;
        }
    }

    public void visitInsn(final int opcode) {
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, 0, null, null);
            } else {
                // updates current and max stack sizes
                int size = currentBlock.outputStackTop + Label.SIZE[opcode];
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
            // if opcode == ATHROW or xRETURN, ends current block (no successor)
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                    || opcode == Opcodes.ATHROW)
            {
                this.currentBlock = null;
            }
        }
        // adds the instruction to the bytecode of the method
        code.putByte(opcode);
    }

    public void visitIntInsn(final int opcode, final int operand) {
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, operand, null, null);
            } else if (opcode != Opcodes.NEWARRAY) {
                // updates current and max stack sizes
                int size = currentBlock.outputStackTop + 1;
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
        }
        // adds the instruction to the bytecode of the method
        if (opcode == Opcodes.SIPUSH) {
            code.put12(opcode, operand);
        } else { // BIPUSH or NEWARRAY
            code.put11(opcode, operand);
        }
    }

    public void visitVarInsn(final int opcode, final int var) {
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, var, null, null);
            } else {
                // updates current and max stack sizes
                if (opcode == Opcodes.RET) {
                    // no stack change, but end of current block (no successor)
                    this.currentBlock = null;
                } else { // xLOAD or xSTORE
                    int size = currentBlock.outputStackTop + Label.SIZE[opcode];
                    if (size > currentBlock.outputStackMax) {
                        currentBlock.outputStackMax = size;
                    }
                    currentBlock.outputStackTop = size;
                }
            }
            // updates max locals
            int n;
            if (opcode == Opcodes.LLOAD || opcode == Opcodes.DLOAD
                    || opcode == Opcodes.LSTORE || opcode == Opcodes.DSTORE)
            {
                n = var + 2;
            } else {
                n = var + 1;
            }
            if (n > maxLocals) {
                maxLocals = n;
            }
        }
        // adds the instruction to the bytecode of the method
        if (var < 4 && opcode != Opcodes.RET) {
            int opt;
            if (opcode < Opcodes.ISTORE) {
                /* ILOAD_0 */
                opt = 26 + ((opcode - Opcodes.ILOAD) << 2) + var;
            } else {
                /* ISTORE_0 */
                opt = 59 + ((opcode - Opcodes.ISTORE) << 2) + var;
            }
            code.putByte(opt);
        } else if (var >= 256) {
            code.putByte(196 /* WIDE */).put12(opcode, var);
        } else {
            code.put11(opcode, var);
        }
        if (opcode >= Opcodes.ISTORE && computeFrames && catchCount > 0) {
            visitLabel(new Label());
        }
    }

    public void visitTypeInsn(final int opcode, final String desc) {
        Item i = cw.newClassItem(desc);
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, code.length, cw, i);
            } else if (opcode == Opcodes.NEW) {
                // updates current and max stack sizes only if opcode == NEW
                // (no stack change for ANEWARRAY, CHECKCAST, INSTANCEOF)
                int size = currentBlock.outputStackTop + 1;
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
        }
        // adds the instruction to the bytecode of the method
        code.put12(opcode, i.index);
    }

    public void visitFieldInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        Item i = cw.newFieldItem(owner, name, desc);
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, 0, cw, i);
            } else {
                // computes the stack size variation
                int size = currentBlock.outputStackTop;
                char c = desc.charAt(0);
                switch (opcode) {
                    case Opcodes.GETSTATIC:
                        size += (c == 'D' || c == 'J' ? 2 : 1);
                        break;
                    case Opcodes.PUTSTATIC:
                        size += (c == 'D' || c == 'J' ? -2 : -1);
                        break;
                    case Opcodes.GETFIELD:
                        size += (c == 'D' || c == 'J' ? 1 : 0);
                        break;
                    // case Constants.PUTFIELD:
                    default:
                        size += +(c == 'D' || c == 'J' ? -3 : -2);
                        break;
                }
                // updates current and max stack sizes
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
        }
        // adds the instruction to the bytecode of the method
        code.put12(opcode, i.index);
    }

    public void visitMethodInsn(
        final int opcode,
        final String owner,
        final String name,
        final String desc)
    {
        boolean itf = opcode == Opcodes.INVOKEINTERFACE;
        Item i = cw.newMethodItem(owner, name, desc, itf);
        int argSize = i.intVal;
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, 0, cw, i);
            } else {
                /*
                 * computes the stack size variation. In order not to recompute
                 * several times this variation for the same Item, we use the
                 * intVal field of this item to store this variation, once it
                 * has been computed. More precisely this intVal field stores
                 * the sizes of the arguments and of the return value
                 * corresponding to desc.
                 */
                if (argSize == 0) {
                    // the above sizes have not been computed yet, so we compute
                    // them and we save them in order not to recompute them in
                    // the future
                    argSize = getArgumentsAndReturnSizes(desc);
                    i.intVal = argSize;
                }
                int size = currentBlock.outputStackTop;
                if (opcode == Opcodes.INVOKESTATIC) {
                    size += -(argSize >> 2) + (argSize & 0x03) + 1;
                } else {
                    size += -(argSize >> 2) + (argSize & 0x03);
                }
                // updates current and max stack sizes
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
        }
        // adds the instruction to the bytecode of the method
        if (itf) {
            if (argSize == 0) {
                argSize = getArgumentsAndReturnSizes(desc);
                i.intVal = argSize;
            }
            code.put12(Opcodes.INVOKEINTERFACE, i.index).put11(argSize >> 2, 0);
        } else {
            code.put12(opcode, i.index);
        }
    }

    public void visitJumpInsn(final int opcode, final Label label) {
        Label nextInsn = null;
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(opcode, 0, null, null);
                // 'label' is the target of a jump instruction
                label.first.status |= Label.TARGET;
            } else {
                // updates current stack size (max stack size unchanged because
                // stack size variation always negative in this case)
                currentBlock.outputStackTop += Label.SIZE[opcode];
            }
            // adds 'label' as a successor of this basic block
            addSuccessor(label);
            if (opcode == Opcodes.JSR) {
                // marks the edge to 'label' as corresponding to a JSR insn
                currentBlock.successors.kind = Edge.JSR;
            }
            if (opcode == Opcodes.GOTO) {
                // ends the current block with no successor other than 'label'
                this.currentBlock = null;
            } else {
                // creates a Label for the other successor of the current block
                nextInsn = new Label();
            }
        }
        // adds the instruction to the bytecode of the method
        if ((label.status & Label.RESOLVED) != 0
                && label.position - code.length < Short.MIN_VALUE)
        {
            /*
             * case of a backward jump with an offset < -32768. In this case we
             * automatically replace GOTO with GOTO_W, JSR with JSR_W and IFxxx
             * <l> with IFNOTxxx <l'> GOTO_W <l>, where IFNOTxxx is the
             * "opposite" opcode of IFxxx (i.e., IFNE for IFEQ) and where <l'>
             * designates the instruction just after the GOTO_W.
             */
            if (opcode == Opcodes.GOTO) {
                code.putByte(200); // GOTO_W
            } else if (opcode == Opcodes.JSR) {
                code.putByte(201); // JSR_W
            } else {
                // if the IF instruction is transformed into IFNOT GOTO_W the
                // next instruction becomes the target of the IFNOT instruction
                if (nextInsn != null) {
                    nextInsn.status |= Label.TARGET;
                }
                code.putByte(opcode <= 166
                        ? ((opcode + 1) ^ 1) - 1
                        : opcode ^ 1);
                code.putShort(8); // jump offset
                code.putByte(200); // GOTO_W
            }
            label.put(this, code, code.length - 1, true);
        } else {
            /*
             * case of a backward jump with an offset >= -32768, or of a forward
             * jump with, of course, an unknown offset. In these cases we store
             * the offset in 2 bytes (which will be increased in
             * resizeInstructions, if needed).
             */
            code.putByte(opcode);
            label.put(this, code, code.length - 1, false);
        }
        // if the jump instruction is not a GOTO, the next instruction is also
        // a successor of this instruction. Calling visitLabel adds the label
        // of this next instruction as a successor of the current block, and
        // starts a new basic block
        if (nextInsn != null) {
            visitLabel(nextInsn);
        }
    }

    public void visitLabel(final Label label) {
        // resolves previous forward references to label, if any
        resize |= label.resolve(this, code.length, code.data);
        if (computeMaxs && (label.status & Label.DEBUG) == 0) {
            if (currentBlock != null) {
                if (label.position == currentBlock.position) {
                    // successive labels, do not start a new basic block
                    currentBlock.status |= (label.status & Label.TARGET);
                    label.first = currentBlock;
                    return;
                }
                // ends current block (with one new successor)
                addSuccessor(label);
            }
            // begins a new current block
            currentBlock = label;
            // updates the list of the visited labels
            if (lastLabel != null) {
                lastLabel.successor = label;
            }
            lastLabel = label;
        }
    }

    public void visitLdcInsn(final Object cst) {
        Item i = cw.newConstItem(cst);
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(Opcodes.LDC, 0, cw, i);
            } else {
                int size = currentBlock.outputStackTop;
                // computes the stack size variation
                if (i.type == 'J' || i.type == 'D') {
                    size += 2;
                } else {
                    size += 1;
                }
                // updates current and max stack sizes
                if (size > currentBlock.outputStackMax) {
                    currentBlock.outputStackMax = size;
                }
                currentBlock.outputStackTop = size;
            }
        }
        // adds the instruction to the bytecode of the method
        int index = i.index;
        if (i.type == 'J' || i.type == 'D') {
            code.put12(20 /* LDC2_W */, index);
        } else if (index >= 256) {
            code.put12(19 /* LDC_W */, index);
        } else {
            code.put11(Opcodes.LDC, index);
        }
    }

    public void visitIincInsn(final int var, final int increment) {
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(Opcodes.IINC, var, null, null);
            }
            // updates max locals
            int n = var + 1;
            if (n > maxLocals) {
                maxLocals = n;
            }
        }
        // adds the instruction to the bytecode of the method
        if ((var > 255) || (increment > 127) || (increment < -128)) {
            code.putByte(196 /* WIDE */)
                    .put12(Opcodes.IINC, var)
                    .putShort(increment);
        } else {
            code.putByte(Opcodes.IINC).put11(var, increment);
        }
    }

    public void visitTableSwitchInsn(
        final int min,
        final int max,
        final Label dflt,
        final Label labels[])
    {
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(Opcodes.TABLESWITCH, 0, null, null);
            } else {
                // updates current stack size (max stack size unchanged)
                --currentBlock.outputStackTop;
            }
            // ends current block (with many new jump target successors)
            addSuccessor(dflt);
            dflt.first.status |= Label.TARGET;
            for (int i = 0; i < labels.length; ++i) {
                addSuccessor(labels[i]);
                labels[i].first.status |= Label.TARGET;
            }
            this.currentBlock = null;
        }
        // adds the instruction to the bytecode of the method
        int source = code.length;
        code.putByte(Opcodes.TABLESWITCH);
        while (code.length % 4 != 0) {
            code.putByte(0);
        }
        dflt.put(this, code, source, true);
        code.putInt(min).putInt(max);
        for (int i = 0; i < labels.length; ++i) {
            labels[i].put(this, code, source, true);
        }
    }

    public void visitLookupSwitchInsn(
        final Label dflt,
        final int keys[],
        final Label labels[])
    {
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(Opcodes.LOOKUPSWITCH, 0, null, null);
            } else {
                // updates current stack size (max stack size unchanged)
                --currentBlock.outputStackTop;
            }
            // ends current block (with many new jump target successors)
            addSuccessor(dflt);
            dflt.first.status |= Label.TARGET;
            for (int i = 0; i < labels.length; ++i) {
                addSuccessor(labels[i]);
                labels[i].first.status |= Label.TARGET;
            }
            this.currentBlock = null;
        }
        // adds the instruction to the bytecode of the method
        int source = code.length;
        code.putByte(Opcodes.LOOKUPSWITCH);
        while (code.length % 4 != 0) {
            code.putByte(0);
        }
        dflt.put(this, code, source, true);
        code.putInt(labels.length);
        for (int i = 0; i < labels.length; ++i) {
            code.putInt(keys[i]);
            labels[i].put(this, code, source, true);
        }
    }

    public void visitMultiANewArrayInsn(final String desc, final int dims) {
        Item i = cw.newClassItem(desc);
        Label currentBlock = this.currentBlock;
        if (currentBlock != null) {
            if (computeFrames) {
                currentBlock.execute(Opcodes.MULTIANEWARRAY, dims, cw, i);
            } else {
                // updates current stack size (max stack size unchanged because
                // stack size variation always negative or null)
                currentBlock.outputStackTop += 1 - dims;
            }
        }
        // adds the instruction to the bytecode of the method
        code.put12(Opcodes.MULTIANEWARRAY, i.index).putByte(dims);
    }

    public void visitTryCatchBlock(
        final Label start,
        final Label end,
        final Label handler,
        final String type)
    {
        ++catchCount;
        Handler h = new Handler();
        h.start = start;
        h.end = end;
        h.handler = handler;
        h.desc = type;
        h.type = type != null ? cw.newClass(type) : 0;
        if (lastHandler == null) {
            catchTable = h;
        } else {
            lastHandler.next = h;
        }
        lastHandler = h;
    }

    public void visitLocalVariable(
        final String name,
        final String desc,
        final String signature,
        final Label start,
        final Label end,
        final int index)
    {
        if (signature != null) {
            if (localVarType == null) {
                localVarType = new ByteVector();
            }
            ++localVarTypeCount;
            localVarType.putShort(start.position)
                    .putShort(end.position - start.position)
                    .putShort(cw.newUTF8(name))
                    .putShort(cw.newUTF8(signature))
                    .putShort(index);
        }
        if (localVar == null) {
            localVar = new ByteVector();
        }
        ++localVarCount;
        localVar.putShort(start.position)
                .putShort(end.position - start.position)
                .putShort(cw.newUTF8(name))
                .putShort(cw.newUTF8(desc))
                .putShort(index);
    }

    public void visitLineNumber(final int line, final Label start) {
        if (lineNumber == null) {
            lineNumber = new ByteVector();
        }
        ++lineNumberCount;
        lineNumber.putShort(start.position);
        lineNumber.putShort(line);
    }

    public void visitMaxs(final int maxStack, final int maxLocals) {
        if (computeMaxs) {
            if (computeFrames) {
                // creates and visits the first (implicit) frame
                Type[] args = Type.getArgumentTypes(descriptor);
                startLabel.initInputFrame(cw, access, args, this.maxLocals);
                visitFrame(startLabel);
            } else {
                startLabel.inputStackTop = 0 + 1; // see Label.inputStackTop
            }

            Handler handler = catchTable;
            while (handler != null) {
                Label l = handler.start.first;
                Label h = handler.handler.first;
                Label e = handler.end.first;
                // computes the kind of the edges to 'h'
                int kind;
                if (computeFrames) {
                    String t = handler.desc == null
                            ? "java/lang/Throwable"
                            : handler.desc;
                    kind = Label.OBJECT | cw.addType(t);
                } else {
                    // any positive value is ok, the exact value is not used
                    kind = 1;
                }
                // h is an exception handler
                h.status |= Label.TARGET;
                // adds 'h' as a successor of labels between 'start' and 'end'
                while (l != e) {
                    // creates an edge to 'h'
                    Edge b = new Edge();
                    b.kind = kind;
                    b.successor = h;
                    // adds it to the successors of 'l'
                    b.next = l.successors;
                    l.successors = b;
                    // goes to the next label
                    l = l.successor;
                }
                handler = handler.next;
            }

            /*
             * fix point algorithm: mark the first basic block as 'changed'
             * (i.e. put it in the 'changed' list) and, while there are changed
             * basic blocks, choose one, mark it as unchanged, and update its
             * successors (which can be changed in the process).
             */
            int max = 0;
            Label changed = startLabel;
            while (changed != null) {
                // removes a basic block from the list of changed basic blocks
                Label l = changed;
                changed = changed.next;
                l.next = null;
                // a reacheable jump target must be stored in the stack map
                if ((l.status & Label.TARGET) != 0) {
                    l.status |= Label.STORE;
                }
                // updates the (absolute) maximum stack size
                int blockMax;
                if (computeFrames) {
                    blockMax = l.inputStack.length + l.outputStackMax;
                } else {
                    blockMax = l.inputStackTop - 1 + l.outputStackMax;
                }
                if (blockMax > max) {
                    max = blockMax;
                }
                // updates the successors of the current basic block
                Edge e = l.successors;
                while (e != null) {
                    Label n = e.successor.first;
                    boolean change;
                    if (computeFrames) {
                        change = l.merge(cw, n, e.kind);
                    } else {
                        change = l.merge(n, e.kind);
                    }
                    if (change && n.next == null) {
                        // if n has changed and is not already in the 'changed'
                        // list, adds it to this list
                        n.next = changed;
                        changed = n;
                    }
                    e = e.next;
                }
            }
            this.maxStack = max;

            if (computeFrames) {
                // visits all the frames that must be stored in the stack map
                Label l = startLabel;
                while (l != null) {
                    if ((l.status & Label.STORE) != 0) {
                        visitFrame(l);
                    }
                    l = l.successor;
                }
            }
        } else {
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
        }
    }

    public void visitEnd() {
    }

    // ------------------------------------------------------------------------
    // Utility methods: control flow analysis algorithm
    // ------------------------------------------------------------------------

    /**
     * Computes the size of the arguments and of the return value of a method.
     * 
     * @param desc the descriptor of a method.
     * @return the size of the arguments of the method (plus one for the
     *         implicit this argument), argSize, and the size of its return
     *         value, retSize, packed into a single int i =
     *         <tt>(argSize << 2) | retSize</tt> (argSize is therefore equal
     *         to <tt>i >> 2</tt>, and retSize to <tt>i & 0x03</tt>).
     */
    static int getArgumentsAndReturnSizes(final String desc) {
        int n = 1;
        int c = 1;
        while (true) {
            char car = desc.charAt(c++);
            if (car == ')') {
                car = desc.charAt(c);
                return n << 2
                        | (car == 'V' ? 0 : (car == 'D' || car == 'J' ? 2 : 1));
            } else if (car == 'L') {
                while (desc.charAt(c++) != ';') {
                }
                n += 1;
            } else if (car == '[') {
                while ((car = desc.charAt(c)) == '[') {
                    ++c;
                }
                if (car == 'D' || car == 'J') {
                    n -= 1;
                }
            } else if (car == 'D' || car == 'J') {
                n += 2;
            } else {
                n += 1;
            }
        }
    }

    /**
     * Adds a successor to the {@link #currentBlock currentBlock} block.
     * 
     * @param successor the successor block to be added to the current block.
     */
    private void addSuccessor(final Label successor) {
        Edge b = new Edge();
        b.successor = successor;
        b.next = currentBlock.successors;
        currentBlock.successors = b;
    }

    // ------------------------------------------------------------------------
    // Utility methods: stack map frames
    // ------------------------------------------------------------------------

    /**
     * Visits a frame that has been computed from scratch.
     * 
     * @param block the basic block whose input frame must be visited.
     */
    private void visitFrame(final Label block) {
        int i, t;
        int nTop = 0;
        int nLocal = 0;
        int nStack = 0;
        int[] locals = block.inputLocals;
        int[] stacks = block.inputStack;
        // computes the number of locals (ignores TOP types that are just after
        // a LONG or a DOUBLE, and all trailing TOP types)
        for (i = 0; i < locals.length; ++i) {
            t = locals[i];
            if (t == Label.TOP) {
                ++nTop;
            } else {
                nLocal += nTop + 1;
                nTop = 0;
            }
            if (t == Label.LONG || t == Label.DOUBLE) {
                ++i;
            }
        }
        // computes the stack size (ignores TOP types that are just after
        // a LONG or a DOUBLE)
        for (i = 0; i < stacks.length; ++i) {
            t = stacks[i];
            ++nStack;
            if (t == Label.LONG || t == Label.DOUBLE) {
                ++i;
            }
        }
        // visits the frame and its content
        startFrame(block.position, nLocal, nStack);
        for (i = 0; nLocal > 0; ++i, --nLocal) {
            t = locals[i];
            frame[frameIndex++] = t;
            if (t == Label.LONG || t == Label.DOUBLE) {
                ++i;
            }
        }
        for (i = 0; i < stacks.length; ++i) {
            t = stacks[i];
            frame[frameIndex++] = t;
            if (t == Label.LONG || t == Label.DOUBLE) {
                ++i;
            }
        }
        endFrame();
    }

    /**
     * Starts the visit of a stack map frame.
     * 
     * @param offset the offset of the instruction to which the frame
     *        corresponds.
     * @param nLocal the number of local variables in the frame.
     * @param nStack the number of stack elements in the frame.
     */
    private void startFrame(final int offset, final int nLocal, final int nStack)
    {
        int n = 3 + nLocal + nStack;
        if (frame == null || frame.length < n) {
            frame = new int[n];
        }
        frame[0] = offset;
        frame[1] = nLocal;
        frame[2] = nStack;
        frameIndex = 3;
    }

    /**
     * Checks if the visit of the current frame {@link #frame} is finished, and
     * if yes, write it in the StackMapTable attribute.
     */
    private void endFrame() {
        if (previousFrame != null) { // do not write the first frame
            if (stackMap == null) {
                stackMap = new ByteVector();
            }
            writeFrame();
            ++frameCount;
        }
        previousFrame = frame;
        frame = null;
    }

    /**
     * Compress and writes the current frame {@link #frame} in the StackMapTable
     * attribute.
     */
    private void writeFrame() {
        int clocalsSize = frame[1];
        int cstackSize = frame[2];
        if (!zip) {
            stackMap.putShort(frame[0]).putShort(clocalsSize);
            writeFrameTypes(3, 3 + clocalsSize);
            stackMap.putShort(cstackSize);
            writeFrameTypes(3 + clocalsSize, 3 + clocalsSize + cstackSize);
            return;
        }
        int localsSize = previousFrame[1];
        int type = FULL_FRAME;
        int k = 0;
        int delta;
        if (frameCount == 0) {
            delta = frame[0];
        } else {
            delta = frame[0] - previousFrame[0] - 1;
        }
        if (cstackSize == 0) {
            k = clocalsSize - localsSize;
            switch (k) {
                case -3:
                case -2:
                case -1:
                    type = CHOP_FRAME;
                    localsSize = clocalsSize;
                    break;
                case 0:
                    type = delta < 64 ? SAME_FRAME : SAME_FRAME_EXTENDED;
                    break;
                case 1:
                case 2:
                case 3:
                    type = APPEND_FRAME;
                    break;
            }
        } else if (clocalsSize == localsSize && cstackSize == 1) {
            type = delta < 63
                    ? SAME_LOCALS_1_STACK_ITEM_FRAME
                    : SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED;
        }
        if (type != FULL_FRAME) {
            // verify if locals are the same
            int l = 3;
            for (int j = 0; j < localsSize; j++) {
                if (frame[l] != previousFrame[l]) {
                    type = FULL_FRAME;
                    break;
                }
                l++;
            }
        }
        switch (type) {
            case SAME_FRAME:
                stackMap.putByte(delta);
                break;
            case SAME_LOCALS_1_STACK_ITEM_FRAME:
                stackMap.putByte(SAME_LOCALS_1_STACK_ITEM_FRAME + delta);
                writeFrameTypes(3 + clocalsSize, 4 + clocalsSize);
                break;
            case SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED:
                stackMap.putByte(SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
                        .putShort(delta);
                writeFrameTypes(3 + clocalsSize, 4 + clocalsSize);
                break;
            case SAME_FRAME_EXTENDED:
                stackMap.putByte(SAME_FRAME_EXTENDED).putShort(delta);
                break;
            case CHOP_FRAME:
                stackMap.putByte(SAME_FRAME_EXTENDED + k).putShort(delta);
                break;
            case APPEND_FRAME:
                stackMap.putByte(SAME_FRAME_EXTENDED + k).putShort(delta);
                writeFrameTypes(3 + localsSize, 3 + clocalsSize);
                break;
            // case FULL_FRAME:
            default:
                stackMap.putByte(FULL_FRAME)
                        .putShort(delta)
                        .putShort(clocalsSize);
                writeFrameTypes(3, 3 + clocalsSize);
                stackMap.putShort(cstackSize);
                writeFrameTypes(3 + clocalsSize, 3 + clocalsSize + cstackSize);
        }
    }

    /**
     * Writes some types of the current frame {@link #frame} into the
     * StackMapTableAttribute. This method converts types from the format used
     * in {@link Label} to the format used in StackMapTable attributes. In
     * particular, it converts type table indexes to constant pool indexes.
     * 
     * @param start index of the first type in {@link #frame} to write.
     * @param end index of last type in {@link #frame} to write (exclusive).
     */
    private void writeFrameTypes(final int start, final int end) {
        for (int i = start; i < end; ++i) {
            int t = frame[i];
            int d = t & Label.DIM;
            if (d == 0) {
                int v = t & Label.BASE_VALUE;
                switch (t & Label.BASE_KIND) {
                    case Label.OBJECT:
                        stackMap.putByte(7)
                                .putShort(cw.newClass(cw.typeTable[v].strVal1));
                        break;
                    case Label.UNINITIALIZED:
                        stackMap.putByte(8).putShort(cw.typeTable[v].intVal);
                        break;
                    default:
                        stackMap.putByte(v);
                }
            } else {
                StringBuffer buf = new StringBuffer();
                d >>= 28;
                while (d-- > 0) {
                    buf.append('[');
                }
                if ((t & Label.BASE_KIND) == Label.OBJECT) {
                    buf.append('L');
                    buf.append(cw.typeTable[t & Label.BASE_VALUE].strVal1);
                    buf.append(';');
                } else {
                    switch (t & 0xF) {
                        case 1:
                            buf.append('I');
                            break;
                        case 2:
                            buf.append('F');
                            break;
                        case 3:
                            buf.append('D');
                            break;
                        case 9:
                            buf.append('Z');
                            break;
                        case 10:
                            buf.append('B');
                            break;
                        case 11:
                            buf.append('C');
                            break;
                        case 12:
                            buf.append('S');
                            break;
                        default:
                            buf.append('J');
                    }
                }
                stackMap.putByte(7).putShort(cw.newClass(buf.toString()));
            }
        }
    }

    private void writeFrameType(final Object type) {
        if (type instanceof String) {
            stackMap.putByte(7).putShort(cw.newClass((String) type));
        } else if (type instanceof Integer) {
            stackMap.putByte(((Integer) type).intValue());
        } else {
            stackMap.putByte(8).putShort(((Label) type).position);
        }
    }

    // ------------------------------------------------------------------------
    // Utility methods: dump bytecode array
    // ------------------------------------------------------------------------

    /**
     * Returns the size of the bytecode of this method.
     * 
     * @return the size of the bytecode of this method.
     */
    final int getSize() {
        if (resize) {
            // replaces the temporary jump opcodes introduced by Label.resolve.
            resizeInstructions(new int[0], new int[0], 0);
        }
        int size = 8;
        if (code.length > 0) {
            cw.newUTF8("Code");
            size += 18 + code.length + 8 * catchCount;
            if (localVar != null) {
                cw.newUTF8("LocalVariableTable");
                size += 8 + localVar.length;
            }
            if (localVarType != null) {
                cw.newUTF8("LocalVariableTypeTable");
                size += 8 + localVarType.length;
            }
            if (lineNumber != null) {
                cw.newUTF8("LineNumberTable");
                size += 8 + lineNumber.length;
            }
            if (stackMap != null) {
                cw.newUTF8(zip ? "StackMapTable" : "StackMap");
                size += 8 + stackMap.length;
            }
            if (cattrs != null) {
                size += cattrs.getSize(cw,
                        code.data,
                        code.length,
                        maxStack,
                        maxLocals);
            }
        }
        if (exceptionCount > 0) {
            cw.newUTF8("Exceptions");
            size += 8 + 2 * exceptionCount;
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && (cw.version & 0xffff) < Opcodes.V1_5)
        {
            cw.newUTF8("Synthetic");
            size += 6;
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            cw.newUTF8("Deprecated");
            size += 6;
        }
        if (cw.version == Opcodes.V1_4) {
            if ((access & Opcodes.ACC_VARARGS) != 0) {
                cw.newUTF8("Varargs");
                size += 6;
            }
            if ((access & Opcodes.ACC_BRIDGE) != 0) {
                cw.newUTF8("Bridge");
                size += 6;
            }
        }
        if (signature != 0) {
            cw.newUTF8("Signature");
            size += 8;
        }
        if (annd != null) {
            cw.newUTF8("AnnotationDefault");
            size += 6 + annd.length;
        }
        if (anns != null) {
            cw.newUTF8("RuntimeVisibleAnnotations");
            size += 8 + anns.getSize();
        }
        if (ianns != null) {
            cw.newUTF8("RuntimeInvisibleAnnotations");
            size += 8 + ianns.getSize();
        }
        if (panns != null) {
            cw.newUTF8("RuntimeVisibleParameterAnnotations");
            size += 7 + 2 * panns.length;
            for (int i = panns.length - 1; i >= 0; --i) {
                size += panns[i] == null ? 0 : panns[i].getSize();
            }
        }
        if (ipanns != null) {
            cw.newUTF8("RuntimeInvisibleParameterAnnotations");
            size += 7 + 2 * ipanns.length;
            for (int i = ipanns.length - 1; i >= 0; --i) {
                size += ipanns[i] == null ? 0 : ipanns[i].getSize();
            }
        }
        if (attrs != null) {
            size += attrs.getSize(cw, null, 0, -1, -1);
        }
        return size;
    }

    /**
     * Puts the bytecode of this method in the given byte vector.
     * 
     * @param out the byte vector into which the bytecode of this method must be
     *        copied.
     */
    final void put(final ByteVector out) {
        out.putShort(access).putShort(name).putShort(desc);
        int attributeCount = 0;
        if (code.length > 0) {
            ++attributeCount;
        }
        if (exceptionCount > 0) {
            ++attributeCount;
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && (cw.version & 0xffff) < Opcodes.V1_5)
        {
            ++attributeCount;
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            ++attributeCount;
        }
        if (cw.version == Opcodes.V1_4) {
            if ((access & Opcodes.ACC_VARARGS) != 0) {
                ++attributeCount;
            }
            if ((access & Opcodes.ACC_BRIDGE) != 0) {
                ++attributeCount;
            }
        }
        if (signature != 0) {
            ++attributeCount;
        }
        if (annd != null) {
            ++attributeCount;
        }
        if (anns != null) {
            ++attributeCount;
        }
        if (ianns != null) {
            ++attributeCount;
        }
        if (panns != null) {
            ++attributeCount;
        }
        if (ipanns != null) {
            ++attributeCount;
        }
        if (attrs != null) {
            attributeCount += attrs.getCount();
        }
        out.putShort(attributeCount);
        if (code.length > 0) {
            int size = 12 + code.length + 8 * catchCount;
            if (localVar != null) {
                size += 8 + localVar.length;
            }
            if (localVarType != null) {
                size += 8 + localVarType.length;
            }
            if (lineNumber != null) {
                size += 8 + lineNumber.length;
            }
            if (stackMap != null) {
                size += 8 + stackMap.length;
            }
            if (cattrs != null) {
                size += cattrs.getSize(cw,
                        code.data,
                        code.length,
                        maxStack,
                        maxLocals);
            }
            out.putShort(cw.newUTF8("Code")).putInt(size);
            out.putShort(maxStack).putShort(maxLocals);
            out.putInt(code.length).putByteArray(code.data, 0, code.length);
            out.putShort(catchCount);
            if (catchCount > 0) {
                Handler h = catchTable;
                while (h != null) {
                    out.putShort(h.start.position)
                            .putShort(h.end.position)
                            .putShort(h.handler.position)
                            .putShort(h.type);
                    h = h.next;
                }
            }
            attributeCount = 0;
            if (localVar != null) {
                ++attributeCount;
            }
            if (localVarType != null) {
                ++attributeCount;
            }
            if (lineNumber != null) {
                ++attributeCount;
            }
            if (stackMap != null) {
                ++attributeCount;
            }
            if (cattrs != null) {
                attributeCount += cattrs.getCount();
            }
            out.putShort(attributeCount);
            if (localVar != null) {
                out.putShort(cw.newUTF8("LocalVariableTable"));
                out.putInt(localVar.length + 2).putShort(localVarCount);
                out.putByteArray(localVar.data, 0, localVar.length);
            }
            if (localVarType != null) {
                out.putShort(cw.newUTF8("LocalVariableTypeTable"));
                out.putInt(localVarType.length + 2).putShort(localVarTypeCount);
                out.putByteArray(localVarType.data, 0, localVarType.length);
            }
            if (lineNumber != null) {
                out.putShort(cw.newUTF8("LineNumberTable"));
                out.putInt(lineNumber.length + 2).putShort(lineNumberCount);
                out.putByteArray(lineNumber.data, 0, lineNumber.length);
            }
            if (stackMap != null) {
                out.putShort(cw.newUTF8(zip ? "StackMapTable" : "StackMap"));
                out.putInt(stackMap.length + 2).putShort(frameCount);
                out.putByteArray(stackMap.data, 0, stackMap.length);
            }
            if (cattrs != null) {
                cattrs.put(cw, code.data, code.length, maxLocals, maxStack, out);
            }
        }
        if (exceptionCount > 0) {
            out.putShort(cw.newUTF8("Exceptions"))
                    .putInt(2 * exceptionCount + 2);
            out.putShort(exceptionCount);
            for (int i = 0; i < exceptionCount; ++i) {
                out.putShort(exceptions[i]);
            }
        }
        if ((access & Opcodes.ACC_SYNTHETIC) != 0
                && (cw.version & 0xffff) < Opcodes.V1_5)
        {
            out.putShort(cw.newUTF8("Synthetic")).putInt(0);
        }
        if ((access & Opcodes.ACC_DEPRECATED) != 0) {
            out.putShort(cw.newUTF8("Deprecated")).putInt(0);
        }
        if (cw.version == Opcodes.V1_4) {
            if ((access & Opcodes.ACC_VARARGS) != 0) {
                out.putShort(cw.newUTF8("Varargs")).putInt(0);
            }
            if ((access & Opcodes.ACC_BRIDGE) != 0) {
                out.putShort(cw.newUTF8("Bridge")).putInt(0);
            }
        }
        if (signature != 0) {
            out.putShort(cw.newUTF8("Signature")).putInt(2).putShort(signature);
        }
        if (annd != null) {
            out.putShort(cw.newUTF8("AnnotationDefault"));
            out.putInt(annd.length);
            out.putByteArray(annd.data, 0, annd.length);
        }
        if (anns != null) {
            out.putShort(cw.newUTF8("RuntimeVisibleAnnotations"));
            anns.put(out);
        }
        if (ianns != null) {
            out.putShort(cw.newUTF8("RuntimeInvisibleAnnotations"));
            ianns.put(out);
        }
        if (panns != null) {
            out.putShort(cw.newUTF8("RuntimeVisibleParameterAnnotations"));
            AnnotationWriter.put(panns, out);
        }
        if (ipanns != null) {
            out.putShort(cw.newUTF8("RuntimeInvisibleParameterAnnotations"));
            AnnotationWriter.put(ipanns, out);
        }
        if (attrs != null) {
            attrs.put(cw, null, 0, -1, -1, out);
        }
    }

    // ------------------------------------------------------------------------
    // Utility methods: instruction resizing (used to handle GOTO_W and JSR_W)
    // ------------------------------------------------------------------------

    /**
     * Resizes the designated instructions, while keeping jump offsets and
     * instruction addresses consistent. This may require to resize other
     * existing instructions, or even to introduce new instructions: for
     * example, increasing the size of an instruction by 2 at the middle of a
     * method can increases the offset of an IFEQ instruction from 32766 to
     * 32768, in which case IFEQ 32766 must be replaced with IFNEQ 8 GOTO_W
     * 32765. This, in turn, may require to increase the size of another jump
     * instruction, and so on... All these operations are handled automatically
     * by this method. <p> <i>This method must be called after all the method
     * that is being built has been visited</i>. In particular, the
     * {@link Label Label} objects used to construct the method are no longer
     * valid after this method has been called.
     * 
     * @param indexes current positions of the instructions to be resized. Each
     *        instruction must be designated by the index of its <i>last</i>
     *        byte, plus one (or, in other words, by the index of the <i>first</i>
     *        byte of the <i>next</i> instruction).
     * @param sizes the number of bytes to be <i>added</i> to the above
     *        instructions. More precisely, for each i &lt; <tt>len</tt>,
     *        <tt>sizes</tt>[i] bytes will be added at the end of the
     *        instruction designated by <tt>indexes</tt>[i] or, if
     *        <tt>sizes</tt>[i] is negative, the <i>last</i> |<tt>sizes[i]</tt>|
     *        bytes of the instruction will be removed (the instruction size
     *        <i>must not</i> become negative or null). The gaps introduced by
     *        this method must be filled in "manually" in {@link #code code}
     *        method.
     * @param len the number of instruction to be resized. Must be smaller than
     *        or equal to <tt>indexes</tt>.length and <tt>sizes</tt>.length.
     * @return the <tt>indexes</tt> array, which now contains the new
     *         positions of the resized instructions (designated as above).
     */
    private int[] resizeInstructions(
        final int[] indexes,
        final int[] sizes,
        final int len)
    {
        byte[] b = code.data; // bytecode of the method
        int u, v, label; // indexes in b
        int i, j; // loop indexes
        /*
         * 1st step: As explained above, resizing an instruction may require to
         * resize another one, which may require to resize yet another one, and
         * so on. The first step of the algorithm consists in finding all the
         * instructions that need to be resized, without modifying the code.
         * This is done by the following "fix point" algorithm:
         * 
         * Parse the code to find the jump instructions whose offset will need
         * more than 2 bytes to be stored (the future offset is computed from
         * the current offset and from the number of bytes that will be inserted
         * or removed between the source and target instructions). For each such
         * instruction, adds an entry in (a copy of) the indexes and sizes
         * arrays (if this has not already been done in a previous iteration!).
         * 
         * If at least one entry has been added during the previous step, go
         * back to the beginning, otherwise stop.
         * 
         * In fact the real algorithm is complicated by the fact that the size
         * of TABLESWITCH and LOOKUPSWITCH instructions depends on their
         * position in the bytecode (because of padding). In order to ensure the
         * convergence of the algorithm, the number of bytes to be added or
         * removed from these instructions is over estimated during the previous
         * loop, and computed exactly only after the loop is finished (this
         * requires another pass to parse the bytecode of the method).
         */
        int[] allIndexes = new int[len]; // copy of indexes
        int[] allSizes = new int[len]; // copy of sizes
        boolean[] resize; // instructions to be resized
        int newOffset; // future offset of a jump instruction

        System.arraycopy(indexes, 0, allIndexes, 0, len);
        System.arraycopy(sizes, 0, allSizes, 0, len);
        resize = new boolean[code.length];

        // 3 = loop again, 2 = loop ended, 1 = last pass, 0 = done
        int state = 3;
        do {
            if (state == 3) {
                state = 2;
            }
            u = 0;
            while (u < b.length) {
                int opcode = b[u] & 0xFF; // opcode of current instruction
                int insert = 0; // bytes to be added after this instruction

                switch (ClassWriter.TYPE[opcode]) {
                    case ClassWriter.NOARG_INSN:
                    case ClassWriter.IMPLVAR_INSN:
                        u += 1;
                        break;
                    case ClassWriter.LABEL_INSN:
                        if (opcode > 201) {
                            // converts temporary opcodes 202 to 217, 218 and
                            // 219 to IFEQ ... JSR (inclusive), IFNULL and
                            // IFNONNULL
                            opcode = opcode < 218 ? opcode - 49 : opcode - 20;
                            label = u + readUnsignedShort(b, u + 1);
                        } else {
                            label = u + readShort(b, u + 1);
                        }
                        newOffset = getNewOffset(allIndexes, allSizes, u, label);
                        if (newOffset < Short.MIN_VALUE
                                || newOffset > Short.MAX_VALUE)
                        {
                            if (!resize[u]) {
                                if (opcode == Opcodes.GOTO
                                        || opcode == Opcodes.JSR)
                                {
                                    // two additional bytes will be required to
                                    // replace this GOTO or JSR instruction with
                                    // a GOTO_W or a JSR_W
                                    insert = 2;
                                } else {
                                    // five additional bytes will be required to
                                    // replace this IFxxx <l> instruction with
                                    // IFNOTxxx <l'> GOTO_W <l>, where IFNOTxxx
                                    // is the "opposite" opcode of IFxxx (i.e.,
                                    // IFNE for IFEQ) and where <l'> designates
                                    // the instruction just after the GOTO_W.
                                    insert = 5;
                                }
                                resize[u] = true;
                            }
                        }
                        u += 3;
                        break;
                    case ClassWriter.LABELW_INSN:
                        u += 5;
                        break;
                    case ClassWriter.TABL_INSN:
                        if (state == 1) {
                            // true number of bytes to be added (or removed)
                            // from this instruction = (future number of padding
                            // bytes - current number of padding byte) -
                            // previously over estimated variation =
                            // = ((3 - newOffset%4) - (3 - u%4)) - u%4
                            // = (-newOffset%4 + u%4) - u%4
                            // = -(newOffset & 3)
                            newOffset = getNewOffset(allIndexes, allSizes, 0, u);
                            insert = -(newOffset & 3);
                        } else if (!resize[u]) {
                            // over estimation of the number of bytes to be
                            // added to this instruction = 3 - current number
                            // of padding bytes = 3 - (3 - u%4) = u%4 = u & 3
                            insert = u & 3;
                            resize[u] = true;
                        }
                        // skips instruction
                        u = u + 4 - (u & 3);
                        u += 4 * (readInt(b, u + 8) - readInt(b, u + 4) + 1) + 12;
                        break;
                    case ClassWriter.LOOK_INSN:
                        if (state == 1) {
                            // like TABL_INSN
                            newOffset = getNewOffset(allIndexes, allSizes, 0, u);
                            insert = -(newOffset & 3);
                        } else if (!resize[u]) {
                            // like TABL_INSN
                            insert = u & 3;
                            resize[u] = true;
                        }
                        // skips instruction
                        u = u + 4 - (u & 3);
                        u += 8 * readInt(b, u + 4) + 8;
                        break;
                    case ClassWriter.WIDE_INSN:
                        opcode = b[u + 1] & 0xFF;
                        if (opcode == Opcodes.IINC) {
                            u += 6;
                        } else {
                            u += 4;
                        }
                        break;
                    case ClassWriter.VAR_INSN:
                    case ClassWriter.SBYTE_INSN:
                    case ClassWriter.LDC_INSN:
                        u += 2;
                        break;
                    case ClassWriter.SHORT_INSN:
                    case ClassWriter.LDCW_INSN:
                    case ClassWriter.FIELDORMETH_INSN:
                    case ClassWriter.TYPE_INSN:
                    case ClassWriter.IINC_INSN:
                        u += 3;
                        break;
                    case ClassWriter.ITFMETH_INSN:
                        u += 5;
                        break;
                    // case ClassWriter.MANA_INSN:
                    default:
                        u += 4;
                        break;
                }
                if (insert != 0) {
                    // adds a new (u, insert) entry in the allIndexes and
                    // allSizes arrays
                    int[] newIndexes = new int[allIndexes.length + 1];
                    int[] newSizes = new int[allSizes.length + 1];
                    System.arraycopy(allIndexes,
                            0,
                            newIndexes,
                            0,
                            allIndexes.length);
                    System.arraycopy(allSizes, 0, newSizes, 0, allSizes.length);
                    newIndexes[allIndexes.length] = u;
                    newSizes[allSizes.length] = insert;
                    allIndexes = newIndexes;
                    allSizes = newSizes;
                    if (insert > 0) {
                        state = 3;
                    }
                }
            }
            if (state < 3) {
                --state;
            }
        } while (state != 0);

        // 2nd step:
        // copies the bytecode of the method into a new bytevector, updates the
        // offsets, and inserts (or removes) bytes as requested.

        ByteVector newCode = new ByteVector(code.length);

        u = 0;
        while (u < code.length) {
            for (i = allIndexes.length - 1; i >= 0; --i) {
                if (allIndexes[i] == u) {
                    if (i < len) {
                        if (sizes[i] > 0) {
                            newCode.putByteArray(null, 0, sizes[i]);
                        } else {
                            newCode.length += sizes[i];
                        }
                        indexes[i] = newCode.length;
                    }
                }
            }
            int opcode = b[u] & 0xFF;
            switch (ClassWriter.TYPE[opcode]) {
                case ClassWriter.NOARG_INSN:
                case ClassWriter.IMPLVAR_INSN:
                    newCode.putByte(opcode);
                    u += 1;
                    break;
                case ClassWriter.LABEL_INSN:
                    if (opcode > 201) {
                        // changes temporary opcodes 202 to 217 (inclusive), 218
                        // and 219 to IFEQ ... JSR (inclusive), IFNULL and
                        // IFNONNULL
                        opcode = opcode < 218 ? opcode - 49 : opcode - 20;
                        label = u + readUnsignedShort(b, u + 1);
                    } else {
                        label = u + readShort(b, u + 1);
                    }
                    newOffset = getNewOffset(allIndexes, allSizes, u, label);
                    if (resize[u]) {
                        // replaces GOTO with GOTO_W, JSR with JSR_W and IFxxx
                        // <l> with IFNOTxxx <l'> GOTO_W <l>, where IFNOTxxx is
                        // the "opposite" opcode of IFxxx (i.e., IFNE for IFEQ)
                        // and where <l'> designates the instruction just after
                        // the GOTO_W.
                        if (opcode == Opcodes.GOTO) {
                            newCode.putByte(200); // GOTO_W
                        } else if (opcode == Opcodes.JSR) {
                            newCode.putByte(201); // JSR_W
                        } else {
                            newCode.putByte(opcode <= 166
                                    ? ((opcode + 1) ^ 1) - 1
                                    : opcode ^ 1);
                            newCode.putShort(8); // jump offset
                            newCode.putByte(200); // GOTO_W
                            // newOffset now computed from start of GOTO_W
                            newOffset -= 3;
                        }
                        newCode.putInt(newOffset);
                    } else {
                        newCode.putByte(opcode);
                        newCode.putShort(newOffset);
                    }
                    u += 3;
                    break;
                case ClassWriter.LABELW_INSN:
                    label = u + readInt(b, u + 1);
                    newOffset = getNewOffset(allIndexes, allSizes, u, label);
                    newCode.putByte(opcode);
                    newCode.putInt(newOffset);
                    u += 5;
                    break;
                case ClassWriter.TABL_INSN:
                    // skips 0 to 3 padding bytes
                    v = u;
                    u = u + 4 - (v & 3);
                    // reads and copies instruction
                    newCode.putByte(Opcodes.TABLESWITCH);
                    while (newCode.length % 4 != 0) {
                        newCode.putByte(0);
                    }
                    label = v + readInt(b, u);
                    u += 4;
                    newOffset = getNewOffset(allIndexes, allSizes, v, label);
                    newCode.putInt(newOffset);
                    j = readInt(b, u);
                    u += 4;
                    newCode.putInt(j);
                    j = readInt(b, u) - j + 1;
                    u += 4;
                    newCode.putInt(readInt(b, u - 4));
                    for (; j > 0; --j) {
                        label = v + readInt(b, u);
                        u += 4;
                        newOffset = getNewOffset(allIndexes, allSizes, v, label);
                        newCode.putInt(newOffset);
                    }
                    break;
                case ClassWriter.LOOK_INSN:
                    // skips 0 to 3 padding bytes
                    v = u;
                    u = u + 4 - (v & 3);
                    // reads and copies instruction
                    newCode.putByte(Opcodes.LOOKUPSWITCH);
                    while (newCode.length % 4 != 0) {
                        newCode.putByte(0);
                    }
                    label = v + readInt(b, u);
                    u += 4;
                    newOffset = getNewOffset(allIndexes, allSizes, v, label);
                    newCode.putInt(newOffset);
                    j = readInt(b, u);
                    u += 4;
                    newCode.putInt(j);
                    for (; j > 0; --j) {
                        newCode.putInt(readInt(b, u));
                        u += 4;
                        label = v + readInt(b, u);
                        u += 4;
                        newOffset = getNewOffset(allIndexes, allSizes, v, label);
                        newCode.putInt(newOffset);
                    }
                    break;
                case ClassWriter.WIDE_INSN:
                    opcode = b[u + 1] & 0xFF;
                    if (opcode == Opcodes.IINC) {
                        newCode.putByteArray(b, u, 6);
                        u += 6;
                    } else {
                        newCode.putByteArray(b, u, 4);
                        u += 4;
                    }
                    break;
                case ClassWriter.VAR_INSN:
                case ClassWriter.SBYTE_INSN:
                case ClassWriter.LDC_INSN:
                    newCode.putByteArray(b, u, 2);
                    u += 2;
                    break;
                case ClassWriter.SHORT_INSN:
                case ClassWriter.LDCW_INSN:
                case ClassWriter.FIELDORMETH_INSN:
                case ClassWriter.TYPE_INSN:
                case ClassWriter.IINC_INSN:
                    newCode.putByteArray(b, u, 3);
                    u += 3;
                    break;
                case ClassWriter.ITFMETH_INSN:
                    newCode.putByteArray(b, u, 5);
                    u += 5;
                    break;
                // case MANA_INSN:
                default:
                    newCode.putByteArray(b, u, 4);
                    u += 4;
                    break;
            }
        }

        // updates the exception handler block labels
        Handler h = catchTable;
        while (h != null) {
            getNewOffset(allIndexes, allSizes, h.start);
            getNewOffset(allIndexes, allSizes, h.end);
            getNewOffset(allIndexes, allSizes, h.handler);
            h = h.next;
        }
        // updates the instructions addresses in the
        // local var and line number tables
        for (i = 0; i < 2; ++i) {
            ByteVector bv = i == 0 ? localVar : localVarType;
            if (bv != null) {
                b = bv.data;
                u = 0;
                while (u < bv.length) {
                    label = readUnsignedShort(b, u);
                    newOffset = getNewOffset(allIndexes, allSizes, 0, label);
                    writeShort(b, u, newOffset);
                    label += readUnsignedShort(b, u + 2);
                    newOffset = getNewOffset(allIndexes, allSizes, 0, label)
                            - newOffset;
                    writeShort(b, u + 2, newOffset);
                    u += 10;
                }
            }
        }
        if (lineNumber != null) {
            b = lineNumber.data;
            u = 0;
            while (u < lineNumber.length) {
                writeShort(b, u, getNewOffset(allIndexes,
                        allSizes,
                        0,
                        readUnsignedShort(b, u)));
                u += 4;
            }
        }
        // recomputes the stack map frames
        if (frameCount > 0) {
            frameCount = 0;
            stackMap = null;
            previousFrame = null;
            frame = null;
            Label l = new Label();
            Type[] args = Type.getArgumentTypes(descriptor);
            l.initInputFrame(cw, access, args, maxLocals);
            visitFrame(l);
            l = startLabel;
            while (l != null) {
                u = l.position - 3;
                if ((l.status & Label.STORE) != 0 || (u >= 0 && resize[u])) {
                    getNewOffset(allIndexes, allSizes, l);
                    // TODO update offsets in UNINITIALIZED values
                    visitFrame(l);
                }
                l = l.successor;
            }
        }
        // updates the labels of the other attributes
        while (cattrs != null) {
            Label[] labels = cattrs.getLabels();
            if (labels != null) {
                for (i = labels.length - 1; i >= 0; --i) {
                    getNewOffset(allIndexes, allSizes, labels[i]);
                }
            }
        }

        // replaces old bytecodes with new ones
        code = newCode;

        // returns the positions of the resized instructions
        return indexes;
    }

    /**
     * Reads an unsigned short value in the given byte array.
     * 
     * @param b a byte array.
     * @param index the start index of the value to be read.
     * @return the read value.
     */
    static int readUnsignedShort(final byte[] b, final int index) {
        return ((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF);
    }

    /**
     * Reads a signed short value in the given byte array.
     * 
     * @param b a byte array.
     * @param index the start index of the value to be read.
     * @return the read value.
     */
    static short readShort(final byte[] b, final int index) {
        return (short) (((b[index] & 0xFF) << 8) | (b[index + 1] & 0xFF));
    }

    /**
     * Reads a signed int value in the given byte array.
     * 
     * @param b a byte array.
     * @param index the start index of the value to be read.
     * @return the read value.
     */
    static int readInt(final byte[] b, final int index) {
        return ((b[index] & 0xFF) << 24) | ((b[index + 1] & 0xFF) << 16)
                | ((b[index + 2] & 0xFF) << 8) | (b[index + 3] & 0xFF);
    }

    /**
     * Writes a short value in the given byte array.
     * 
     * @param b a byte array.
     * @param index where the first byte of the short value must be written.
     * @param s the value to be written in the given byte array.
     */
    static void writeShort(final byte[] b, final int index, final int s) {
        b[index] = (byte) (s >>> 8);
        b[index + 1] = (byte) s;
    }

    /**
     * Computes the future value of a bytecode offset. <p> Note: it is possible
     * to have several entries for the same instruction in the <tt>indexes</tt>
     * and <tt>sizes</tt>: two entries (index=a,size=b) and (index=a,size=b')
     * are equivalent to a single entry (index=a,size=b+b').
     * 
     * @param indexes current positions of the instructions to be resized. Each
     *        instruction must be designated by the index of its <i>last</i>
     *        byte, plus one (or, in other words, by the index of the <i>first</i>
     *        byte of the <i>next</i> instruction).
     * @param sizes the number of bytes to be <i>added</i> to the above
     *        instructions. More precisely, for each i < <tt>len</tt>,
     *        <tt>sizes</tt>[i] bytes will be added at the end of the
     *        instruction designated by <tt>indexes</tt>[i] or, if
     *        <tt>sizes</tt>[i] is negative, the <i>last</i> |<tt>sizes[i]</tt>|
     *        bytes of the instruction will be removed (the instruction size
     *        <i>must not</i> become negative or null).
     * @param begin index of the first byte of the source instruction.
     * @param end index of the first byte of the target instruction.
     * @return the future value of the given bytecode offset.
     */
    static int getNewOffset(
        final int[] indexes,
        final int[] sizes,
        final int begin,
        final int end)
    {
        int offset = end - begin;
        for (int i = 0; i < indexes.length; ++i) {
            if (begin < indexes[i] && indexes[i] <= end) {
                // forward jump
                offset += sizes[i];
            } else if (end < indexes[i] && indexes[i] <= begin) {
                // backward jump
                offset -= sizes[i];
            }
        }
        return offset;
    }

    /**
     * Updates the offset of the given label.
     * 
     * @param indexes current positions of the instructions to be resized. Each
     *        instruction must be designated by the index of its <i>last</i>
     *        byte, plus one (or, in other words, by the index of the <i>first</i>
     *        byte of the <i>next</i> instruction).
     * @param sizes the number of bytes to be <i>added</i> to the above
     *        instructions. More precisely, for each i < <tt>len</tt>,
     *        <tt>sizes</tt>[i] bytes will be added at the end of the
     *        instruction designated by <tt>indexes</tt>[i] or, if
     *        <tt>sizes</tt>[i] is negative, the <i>last</i> |<tt>sizes[i]</tt>|
     *        bytes of the instruction will be removed (the instruction size
     *        <i>must not</i> become negative or null).
     * @param label the label whose offset must be updated.
     */
    static void getNewOffset(
        final int[] indexes,
        final int[] sizes,
        final Label label)
    {
        if ((label.status & Label.RESIZED) == 0) {
            label.position = getNewOffset(indexes, sizes, 0, label.position);
            label.status |= Label.RESIZED;
        }
    }
}
