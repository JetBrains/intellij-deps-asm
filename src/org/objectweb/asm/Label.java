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
 * A label represents a position in the bytecode of a method. Labels are used
 * for jump, goto, and switch instructions, and for try catch blocks.
 * 
 * @author Eric Bruneton
 */
public class Label {

    // TODO merge debug, resolved and resize flags in a single 'status' field?

    /**
     * Indicates if this label is only used for debug attributes.
     */
    boolean debug;

    /**
     * The line number corresponding to this label, if known.
     */
    int line;

    /**
     * Indicates if the position of this label is known.
     */
    boolean resolved;

    /**
     * The position of this label in the code, if known.
     */
    int position;

    /**
     * If the label position has been updated, after instruction resizing.
     */
    boolean resized;

    /**
     * Number of forward references to this label, times two.
     */
    private int referenceCount;

    /**
     * Informations about forward references. Each forward reference is
     * described by two consecutive integers in this array: the first one is the
     * position of the first byte of the bytecode instruction that contains the
     * forward reference, while the second is the position of the first byte of
     * the forward reference itself. In fact the sign of the first integer
     * indicates if this reference uses 2 or 4 bytes, and its absolute value
     * gives the position of the bytecode instruction.
     */
    private int[] srcAndRefPositions;

    /*
     * Fields for the control flow graph analysis algorithm (used to compute the
     * maximum stack size). A control flow graph contains one node per "basic
     * block", and one edge per "jump" from one basic block to another. Each
     * node (i.e., each basic block) is represented by the Label object that
     * corresponds to the first instruction of this basic block. Each node also
     * stores the list of it successors in the graph, as a linked list of Edge
     * objects.
     */

    /**
     * The stack size at the beginning of this basic block. This size is
     * initially unknown. It is computed by the control flow analysis algorithm
     * (see {@link MethodWriter#visitMaxs visitMaxs}).
     */
    int beginStackSize;

    /**
     * The (relative) maximum stack size corresponding to this basic block. This
     * size is relative to the stack size at the beginning of the basic block,
     * i.e., the true maximum stack size is equal to {@link #beginStackSize
     * beginStackSize} + {@link #maxStackSize maxStackSize}.
     */
    int maxStackSize;

    /**
     * The successors of this node in the control flow graph. These successors
     * are stored in a linked list of {@link Edge Edge} objects, linked to each
     * other by their {@link Edge#next} field.
     */
    Edge successors;

    /**
     * The next basic block in the basic block stack. See
     * {@link MethodWriter#visitMaxs visitMaxs}.
     */
    Label next;

    /**
     * <tt>true</tt> if this basic block has been pushed in the basic block
     * stack. See {@link MethodWriter#visitMaxs visitMaxs}.
     */
    boolean pushed;

    /**
     * In case of several successive labels, points to the first label of the
     * series. Several successive labels represent the same basic block; it
     * would be a waste of space and time to have one basic block per label in
     * this case. This field allow the detection of such cases, which are
     * optimized by storing a basic block only in the first label in a series.
     */
    Label first;

    /**
     * The successor of this label, in the order they are visited.
     */
    Label successor;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructs a new label.
     */
    public Label() {
    }

    /**
     * Constructs a new label.
     * 
     * @param debug if this label is only used for debug attributes.
     */
    Label(final boolean debug) {
        this.debug = debug;
    }

    // ------------------------------------------------------------------------
    // Methods to compute offsets and to manage forward references
    // ------------------------------------------------------------------------

    /**
     * Returns the offset corresponding to this label. This offset is computed
     * from the start of the method's bytecode. <i>This method is intended for
     * {@link Attribute} sub classes, and is normally not needed by class
     * generators or adapters.</i>
     * 
     * @return the offset corresponding to this label.
     * @throws IllegalStateException if this label is not resolved yet.
     */
    public int getOffset() {
        if (!resolved) {
            throw new IllegalStateException("Label offset position has not been resolved yet");
        }
        return position;
    }

    /**
     * Puts a reference to this label in the bytecode of a method. If the
     * position of the label is known, the offset is computed and written
     * directly. Otherwise, a null offset is written and a new forward reference
     * is declared for this label.
     * 
     * @param owner the code writer that calls this method.
     * @param out the bytecode of the method.
     * @param source the position of first byte of the bytecode instruction that
     *        contains this label.
     * @param wideOffset <tt>true</tt> if the reference must be stored in 4
     *        bytes, or <tt>false</tt> if it must be stored with 2 bytes.
     * @throws IllegalArgumentException if this label has not been created by
     *         the given code writer.
     */
    void put(
        final MethodWriter owner,
        final ByteVector out,
        final int source,
        final boolean wideOffset)
    {
        if (resolved) {
            if (wideOffset) {
                out.putInt(position - source);
            } else {
                out.putShort(position - source);
            }
        } else {
            if (wideOffset) {
                addReference(-1 - source, out.length);
                out.putInt(-1);
            } else {
                addReference(source, out.length);
                out.putShort(-1);
            }
        }
    }

    /**
     * Adds a forward reference to this label. This method must be called only
     * for a true forward reference, i.e. only if this label is not resolved
     * yet. For backward references, the offset of the reference can be, and
     * must be, computed and stored directly.
     * 
     * @param sourcePosition the position of the referencing instruction. This
     *        position will be used to compute the offset of this forward
     *        reference.
     * @param referencePosition the position where the offset for this forward
     *        reference must be stored.
     */
    private void addReference(
        final int sourcePosition,
        final int referencePosition)
    {
        if (srcAndRefPositions == null) {
            srcAndRefPositions = new int[6];
        }
        if (referenceCount >= srcAndRefPositions.length) {
            int[] a = new int[srcAndRefPositions.length + 6];
            System.arraycopy(srcAndRefPositions,
                    0,
                    a,
                    0,
                    srcAndRefPositions.length);
            srcAndRefPositions = a;
        }
        srcAndRefPositions[referenceCount++] = sourcePosition;
        srcAndRefPositions[referenceCount++] = referencePosition;
    }

    /**
     * Resolves all forward references to this label. This method must be called
     * when this label is added to the bytecode of the method, i.e. when its
     * position becomes known. This method fills in the blanks that where left
     * in the bytecode by each forward reference previously added to this label.
     * 
     * @param owner the code writer that calls this method.
     * @param position the position of this label in the bytecode.
     * @param data the bytecode of the method.
     * @return <tt>true</tt> if a blank that was left for this label was to
     *         small to store the offset. In such a case the corresponding jump
     *         instruction is replaced with a pseudo instruction (using unused
     *         opcodes) using an unsigned two bytes offset. These pseudo
     *         instructions will need to be replaced with true instructions with
     *         wider offsets (4 bytes instead of 2). This is done in
     *         {@link MethodWriter#resizeInstructions}.
     * @throws IllegalArgumentException if this label has already been resolved,
     *         or if it has not been created by the given code writer.
     */
    boolean resolve(
        final MethodWriter owner,
        final int position,
        final byte[] data)
    {
        boolean needUpdate = false;
        this.resolved = true;
        this.position = position;
        int i = 0;
        while (i < referenceCount) {
            int source = srcAndRefPositions[i++];
            int reference = srcAndRefPositions[i++];
            int offset;
            if (source >= 0) {
                offset = position - source;
                if (offset < Short.MIN_VALUE || offset > Short.MAX_VALUE) {
                    /*
                     * changes the opcode of the jump instruction, in order to
                     * be able to find it later (see resizeInstructions in
                     * MethodWriter). These temporary opcodes are similar to
                     * jump instruction opcodes, except that the 2 bytes offset
                     * is unsigned (and can therefore represent values from 0 to
                     * 65535, which is sufficient since the size of a method is
                     * limited to 65535 bytes).
                     */
                    int opcode = data[reference - 1] & 0xFF;
                    if (opcode <= Opcodes.JSR) {
                        // changes IFEQ ... JSR to opcodes 202 to 217
                        data[reference - 1] = (byte) (opcode + 49);
                    } else {
                        // changes IFNULL and IFNONNULL to opcodes 218 and 219
                        data[reference - 1] = (byte) (opcode + 20);
                    }
                    needUpdate = true;
                }
                data[reference++] = (byte) (offset >>> 8);
                data[reference] = (byte) offset;
            } else {
                offset = position + source + 1;
                data[reference++] = (byte) (offset >>> 24);
                data[reference++] = (byte) (offset >>> 16);
                data[reference++] = (byte) (offset >>> 8);
                data[reference] = (byte) offset;
            }
        }
        return needUpdate;
    }

    // ------------------------------------------------------------------------
    // Overriden Object methods
    // ------------------------------------------------------------------------

    /**
     * Returns a string representation of this label.
     * 
     * @return a string representation of this label.
     */
    public String toString() {
        return "L" + System.identityHashCode(this);
    }

    // ------------------------------------------------------------------------

    /*
     * Fields for the computation of stack map frames. Frames are computed in a
     * two steps process: during the visit of each instruction, the state of the
     * frame at the end of current basic block is updated by simulating the
     * action of the instruction on the previous state of this so called "output
     * frame". In visitMaxs, a fix point algorithm is used to compute the "input
     * frame" of each basic block, i.e. the stack map frame at the begining of
     * the basic block, starting from the input frame of the first basic block
     * (which is computed from the method descriptor), and by using the
     * previously computed output frames to compute the input state of the other
     * blocks.
     * 
     * All output and input frames are stored as arrays of integers. Reference
     * and array types are represented by an index into a type table (which is
     * not the same as the constant pool of the class, in order to avoid adding
     * unnecessary constants in the pool - not all computed frames will end up
     * being stored in the stack map table). This allows very fast type
     * comparisons.
     * 
     * Output stack map frames are computed relatively to the input frame of the
     * basic block, which is not yet known when output frames are computed. It
     * is therefore necessary to be able to represent abstract types such as
     * "the type at position x in the input frame locals" or "the type at
     * position x from the top of the input frame stack" or even "the type at
     * position x in the input frame, with y more (or less) array dimensions".
     * This explains the rather complicated type format used in output frames.
     * 
     * This format is the following: DIM KIND VALUE (4, 4 and 24 bits) - DIM is
     * a signed number of array dimensions (from -8 to 7) - KIND is either BASE,
     * LOCAL or STACK. BASE is used for types that are not relative to the input
     * frame. LOCAL is used for types that are relative to the input local
     * variable types. STACK is used for types that are relative to the input
     * stack types. - VALUE depends on KIND. For LOCAL types, it is an index in
     * the input local variable types. For STACK types, it is a position
     * relatively to the top of input frame stack. For BASE types, it is either
     * one of the constants defined in FrameVisitor, or for OBJECT and
     * UNINITIALIZED types, a tag and an index in the type table.
     * 
     * Output frames can contain types of any kind and with a positive or
     * negative dimension (and even unassigned types, represented by 0 - which
     * does not correspond to any valid type value). Input frames can only
     * contain BASE types of positive or null dimension. In all cases the type
     * table contains only internal type names (array type descriptor are
     * forbidden - dimensions must be represented through the DIM field).
     * 
     * The LONG and DOUBLE types are always represented by using two slots (LONG +
     * TOP or DOUBLE + TOP), for local variable types as well as in the operand
     * stack. This is necessary to be able to simulate DUPx_y instructions,
     * whose effect would be dependent on the actual type values if types were
     * always represented by a single slot in the stack (and this is not
     * possible, since actual type values are not always known - cf LOCAL and
     * STACK type kinds).
     */

    /**
     * Mask to get the dimension of a frame type. This dimension is a signed
     * integer between -8 and 7.
     */
    final static int TYPE_DIM = 0xF0000000;

    /**
     * Constant to be added to a type to get a type with one more dimension.
     */
    final static int ARRAY_OF = 0x10000000;

    /**
     * Constant to be added to a type to get a type with one less dimension.
     */
    final static int ELEMENT_TYPE_OF = 0xF0000000;

    /**
     * Mask to get the kind of a frame type.
     * 
     * @see #BASE_TYPE
     * @see #LOCAL_TYPE
     * @see #STACK_TYPE
     */
    final static int TYPE_KIND = 0xF000000;

    /**
     * Mask to get the value of a frame type.
     */
    final static int TYPE_VALUE = 0xFFFFFF;

    /**
     * Mask to get the kind of base types.
     */
    final static int BASE_TYPE_KIND = 0xFF00000;

    /**
     * Mask to get the value of OBJECT and UNINITIALIZED base types.
     */
    final static int BASE_TYPE_VALUE = 0xFFFFF;

    /**
     * Kind of the types that are not relative to an input stack map frame.
     */
    final static int BASE_TYPE = 0x1000000;

    /**
     * Base kind of the base reference types. The BASE_TYPE_VALUE of such types
     * is an index into the type table.
     */
    final static int BASE_OBJECT_TYPE = BASE_TYPE | 0x700000;

    /**
     * Base kind of the uninitialized base types. The BASE_TYPE_VALUE of such
     * types in an index into the type table (the Item at that index contains
     * both an instruction offset and an internal class name).
     */
    final static int BASE_UNINITIALIZED_TYPE = BASE_TYPE | 0x800000;

    /**
     * Kind of the types that are relative to the local variable types of an
     * input stack map frame. The value of such types is a local variable index.
     */
    private final static int LOCAL_TYPE = 0x2000000;

    /**
     * Kind of the the types that are relative to the stack of an input stack
     * map frame. The value of such types is a position relatively to the top of
     * this stack.
     */
    private final static int STACK_TYPE = 0x3000000;

    /**
     * The TOP type. This is a BASE_TYPE type.
     */
    final static int TOP = BASE_TYPE | FrameVisitor.TOP;

    final static int BOOLEAN = BASE_TYPE | 9;
    final static int BYTE = BASE_TYPE | 10;
    final static int CHAR = BASE_TYPE | 11;
    final static int SHORT = BASE_TYPE | 12;

    /**
     * The INTEGER type. This is a BASE_TYPE type.
     */
    final static int INTEGER = BASE_TYPE | FrameVisitor.INTEGER;

    /**
     * The FLOAT type. This is a BASE_TYPE type.
     */
    final static int FLOAT = BASE_TYPE | FrameVisitor.FLOAT;

    /**
     * The DOUBLE type. This is a BASE_TYPE type.
     */
    final static int DOUBLE = BASE_TYPE | FrameVisitor.DOUBLE;

    /**
     * The LONG type. This is a BASE_TYPE type.
     */
    final static int LONG = BASE_TYPE | FrameVisitor.LONG;

    /**
     * The NULL type. This is a BASE_TYPE type.
     */
    final static int NULL = BASE_TYPE | FrameVisitor.NULL;

    /**
     * The UNINITIALIZED_THIS type. This is a BASE_TYPE type.
     */
    final static int UNINITIALIZED_THIS = BASE_TYPE
            | FrameVisitor.UNINITIALIZED_THIS;

    /**
     * The output stack map frame locals. May be <tt>null</tt> if no locals
     * are used in the basic block.
     */
    private int[] outputLocals;

    /**
     * The output stack map stack. May be <tt>null</tt> if the stack is not
     * used in the basic block.
     */
    private int[] outputStack;

    /**
     * Offset of the first output stack element relatively to the top of the
     * input stack. This offset is always negative or null. A null offset means
     * that the output stack must be appended to the input stack. A -n offset
     * means that the first n output stack elements must replace the top n input
     * stack elements, and that the other elements must be appended to the input
     * stack.
     */
    private int outputStackOffset;

    /**
     * Actual number of types in {@link #outputStack}.
     */
    private int outputStackTop;

    /**
     * Maximum height reached by the output stack, relatively to the top of the
     * input stack. This maximum is always positive or null.
     */
    int outputStackMax;

    // TODO comment this
    private boolean initThis;
    private int initializationCount;
    private int[] initializations;

    /**
     * The input stack map frame locals.
     */
    int[] inputLocals;

    /**
     * The output stack map stack.
     */
    int[] inputStack;

    /**
     * If the stack map frame of this basic block must be store in the stack map
     * table. 0 means "do not store", 1 means "store if the block is
     * reacheable", 2 = "store".
     */
    int store;

    /**
     * Returns the output frame local variable type at the given index.
     * 
     * @param local the index of the local that must be returned.
     * @return the output frame local variable type at the given index.
     */
    private int get(final int local) {
        if (outputLocals == null || local >= outputLocals.length) {
            // this local has never been assigned in this basic block,
            // so it is still equal to its value in the input frame
            return LOCAL_TYPE | local;
        } else {
            int type = outputLocals[local];
            if (type == 0) {
                // this local has never been assigned in this basic block,
                // so it is still equal to its value in the input frame
                type = outputLocals[local] = LOCAL_TYPE | local;
            }
            return type;
        }
    }

    /**
     * Sets the output frame local variable type at the given index.
     * 
     * @param local the index of the local that must be set.
     * @param type the value of the local that must be set.
     */
    private void set(final int local, final int type) {
        if (outputLocals == null) {
            outputLocals = new int[10];
        }
        if (local >= outputLocals.length) {
            int[] t = new int[Math.max(local + 1, 2 * outputLocals.length)];
            System.arraycopy(outputLocals, 0, t, 0, outputLocals.length);
            outputLocals = t;
        }
        outputLocals[local] = type;
    }

    /**
     * Pushes a new type onto the output frame stack.
     * 
     * @param type the type that must be pushed.
     */
    private void push(final int type) {
        if (outputStack == null) {
            outputStack = new int[10];
        }
        if (outputStackTop >= outputStack.length) {
            int[] t = new int[Math.max(outputStackTop + 1,
                    2 * outputStack.length)];
            System.arraycopy(outputStack, 0, t, 0, outputStack.length);
            outputStack = t;
        }
        outputStack[outputStackTop++] = type;
        // updates the maximun height reached by the output stack, if needed
        if (outputStackTop + outputStackOffset > outputStackMax) {
            outputStackMax = outputStackTop + outputStackOffset;
        }
    }

    /**
     * Pushes a new type onto the output frame stack.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param desc the descriptor of the type to be pushed. Can also be a method
     *        descriptor (in this case this method pushes its return type onto
     *        the output frame stack).
     */
    private void push(final ClassWriter cw, final String desc) {
        int index = desc.charAt(0) == '(' ? desc.indexOf(')') + 1 : 0;
        switch (desc.charAt(index)) {
            case 'V':
                // void type: nothing to push
                break;
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
                push(INTEGER);
                break;
            case 'F':
                push(FLOAT);
                break;
            case 'J':
                push(LONG);
                push(TOP);
                break;
            case 'D':
                push(DOUBLE);
                push(TOP);
                break;
            case 'L':
                // stores the internal name in the type table, not the
                // descriptor
                push(BASE_OBJECT_TYPE
                        | cw.addType(desc.substring(index + 1,
                                desc.length() - 1)));
                break;
            // case '[':
            default:
                // extracts the dimensions and the element type
                int data;
                int dims = index + 1;
                while (desc.charAt(dims) == '[') {
                    ++dims;
                }
                switch (desc.charAt(dims)) {
                    case 'Z':
                        data = BOOLEAN;
                        break;
                    case 'C':
                        data = CHAR;
                        break;
                    case 'B':
                        data = BYTE;
                        break;
                    case 'S':
                        data = SHORT;
                        break;
                    case 'I':
                        data = INTEGER;
                        break;
                    case 'F':
                        data = FLOAT;
                        break;
                    case 'J':
                        data = LONG;
                        break;
                    case 'D':
                        data = DOUBLE;
                        break;
                    // case 'L':
                    default:
                        // stores the internal name, not the descriptor
                        data = BASE_OBJECT_TYPE
                                | cw.addType(desc.substring(dims + 1,
                                        desc.length() - 1));
                }
                push((dims - index) << 28 | data);
                break;
        }
    }

    /**
     * Pops a type from the output frame stack and returns its value.
     * 
     * @return the type that has been popped from the output frame stack.
     */
    int pop() {
        if (outputStackTop > 0) {
            return outputStack[--outputStackTop];
        } else {
            // if the output frame stack is empty, pops from the input stack
            return STACK_TYPE | -(--outputStackOffset);
        }
    }

    /**
     * Pop a type from the output frame stack.
     * 
     * @param desc the descriptor of the type to be popped. Can also be a method
     *        descriptor (in this case this method pops the types corresponding
     *        to the method arguments).
     */
    private void pop(final String desc) {
        switch (desc.charAt(0)) { // TODO if elseif else? inline?
            case 'Z':
            case 'C':
            case 'B':
            case 'S':
            case 'I':
            case 'F':
            case '[':
            case 'L':
                pop(1);
                break;
            case 'J':
            case 'D':
                pop(2);
                break;
            case '(': // TODO optimize?
                int argSize = MethodWriter.getArgumentsAndReturnSizes(desc);
                pop((argSize >> 2) - 1);
        }
    }

    /**
     * Pops the given number of types from the output frame stack.
     * 
     * @param elements the number of types that must be popped.
     */
    void pop(final int elements) {
        if (outputStackTop >= elements) {
            outputStackTop -= elements;
        } else {
            // if the number of elements to be popped is greater than the number
            // of elements in the output stack, clear it, and pops the remaining
            // elements from the input stack.
            outputStackOffset -= elements - outputStackTop;
            outputStackTop = 0;
        }
    }

    // TODO comment this
    void init(final int var) {
        if (initializations == null) {
            initializations = new int[10];
        }
        if (initializationCount >= initializations.length) {
            int[] t = new int[Math.max(initializationCount + 1,
                    2 * initializations.length)];
            System.arraycopy(initializations, 0, t, 0, initializations.length);
            initializations = t;
        }
        initializations[initializationCount++] = var;
    }

    /**
     * TODO
     * 
     * @param constructor
     */
    void initOutputFrame(final boolean constructor) {
        if (constructor) {
            set(0, UNINITIALIZED_THIS);
        }
    }

    void initInputFrame(
        final ClassWriter cw,
        final int access,
        final Type[] args,
        final int maxLocals)
    {
        int[] oldOutputStack = outputStack;
        int oldOutputStackTop = outputStackTop;
        int oldOutputStackMax = outputStackMax;

        outputStack = new int[maxLocals];
        for (int i = 0; i < maxLocals; ++i) {
            outputStack[i] = TOP;
        }
        outputStackTop = 0;
        if ((access & Opcodes.ACC_STATIC) == 0) {
            if ((access & MethodWriter.ACC_CONSTRUCTOR) == 0) {
                push(BASE_OBJECT_TYPE | cw.addType(cw.thisName));
            } else {
                push(UNINITIALIZED_THIS);
            }
        }
        for (int i = 0; i < args.length; ++i) {
            push(cw, args[i].getDescriptor());
        }
        inputLocals = outputStack;
        inputStack = new int[0];

        outputStack = oldOutputStack;
        outputStackTop = oldOutputStackTop;
        outputStackMax = oldOutputStackMax;
    }

    /**
     * Simulates the action of the given instruction on the output stack frame.
     * 
     * @param opcode the opcode of the instruction.
     * @param arg the operand of the instruction, if any.
     * @param cw the class writer to which this label belongs.
     * @param item the operand of the instructions, if any.
     */
    void execute(
        final int opcode,
        final int arg,
        final ClassWriter cw,
        final Item item)
    {
        int t1, t2, t3, t4;
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
            case Opcodes.LDC:
                switch (item.type) {
                    case 'I':
                        push(INTEGER);
                        break;
                    case 'J':
                        push(LONG);
                        push(TOP);
                        break;
                    case 'F':
                        push(FLOAT);
                        break;
                    case 'D':
                        push(DOUBLE);
                        push(TOP);
                        break;
                    case 'C':
                        push(BASE_OBJECT_TYPE | cw.addType("java/lang/Class"));
                        break;
                    // case 'S':
                    default:
                        push(BASE_OBJECT_TYPE | cw.addType("java/lang/String"));
                }
                break;
            case Opcodes.ILOAD:
            case Opcodes.FLOAD:
            case Opcodes.ALOAD:
                push(get(arg));
                break;
            case Opcodes.LLOAD:
            case Opcodes.DLOAD:
                push(get(arg));
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
                push(ELEMENT_TYPE_OF + t1);
                break;
            case Opcodes.ISTORE:
            case Opcodes.FSTORE:
            case Opcodes.ASTORE:
                t1 = pop();
                set(arg, t1);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
                    }
                }
                break;
            case Opcodes.LSTORE:
            case Opcodes.DSTORE:
                pop(1);
                t1 = pop();
                set(arg, t1);
                set(arg + 1, TOP);
                if (arg > 0) {
                    t2 = get(arg - 1);
                    // if t2 is of kind STACK or LOCAL we cannot know its size!
                    if (t2 == LONG || t2 == DOUBLE) {
                        set(arg - 1, TOP);
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
                set(arg, INTEGER);
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
                throw new RuntimeException("JSR/RET are not supported with computeFrames option");
            case Opcodes.GETSTATIC:
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTSTATIC:
                pop(item.strVal3);
                break;
            case Opcodes.GETFIELD:
                pop(1);
                push(cw, item.strVal3);
                break;
            case Opcodes.PUTFIELD:
                pop(item.strVal3);
                pop();
                break;
            case Opcodes.INVOKEVIRTUAL:
            case Opcodes.INVOKESPECIAL:
            case Opcodes.INVOKESTATIC:
            case Opcodes.INVOKEINTERFACE:
                pop(item.strVal3);
                if (opcode != Opcodes.INVOKESTATIC) {
                    t1 = pop();
                    // TODO and what if t1 is a LOCAL or STACK type???
                    if (opcode == Opcodes.INVOKESPECIAL) {
                        if (t1 == UNINITIALIZED_THIS) {
                            initThis = true;
                        } else if ((t1 & (TYPE_DIM | BASE_TYPE_KIND)) == BASE_UNINITIALIZED_TYPE)
                        {
                            init(t1);
                        }
                    }
                }
                push(cw, item.strVal3);
                break;
            case Opcodes.NEW:
                push(BASE_UNINITIALIZED_TYPE
                        | cw.addUninitializedType(item.strVal1, arg));
                break;
            case Opcodes.NEWARRAY:
                pop();
                switch (arg) {
                    case Opcodes.T_BOOLEAN:
                        push(ARRAY_OF | BOOLEAN);
                        break;
                    case Opcodes.T_CHAR:
                        push(ARRAY_OF | CHAR);
                        break;
                    case Opcodes.T_BYTE:
                        push(ARRAY_OF | BYTE);
                        break;
                    case Opcodes.T_SHORT:
                        push(ARRAY_OF | SHORT);
                        break;
                    case Opcodes.T_INT:
                        push(ARRAY_OF | INTEGER);
                        break;
                    case Opcodes.T_FLOAT:
                        push(ARRAY_OF | FLOAT);
                        break;
                    case Opcodes.T_DOUBLE:
                        push(ARRAY_OF | DOUBLE);
                        break;
                    // case Opcodes.T_LONG:
                    default:
                        push(ARRAY_OF | LONG);
                        break;
                }
                break;
            case Opcodes.ANEWARRAY:
                String s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, "[" + s);
                } else {
                    push(ARRAY_OF | BASE_OBJECT_TYPE | cw.addType(s));
                }
                break;
            case Opcodes.CHECKCAST:
                s = item.strVal1;
                pop();
                if (s.charAt(0) == '[') {
                    push(cw, s);
                } else {
                    push(BASE_OBJECT_TYPE | cw.addType(s));
                }
                break;
            // case Opcodes.MULTIANEWARRAY:
            default:
                pop(arg);
                push(cw, item.strVal1);
                break;
        }
    }

    /**
     * Merges the input frame of the given basic block with the input and output
     * frames of this basic block. Returns <tt>true</tt> if the input frame of
     * the given label has been changed by this operation.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param label the basic block whose input frame must be updated.
     * @return <tt>true</tt> if the input frame of the given label has been
     *         changed by this operation.
     */
    // TODO refactor to remove duplicated code, comment code
    boolean merge(final ClassWriter cw, final Label label, final int exception)
    {
        boolean changed = false;
        int i, s, dim, kind, t;

        int nLocal = inputLocals.length;
        if (label.inputLocals == null) {
            label.inputLocals = new int[nLocal];
            changed = true;
        }

        for (i = 0; i < nLocal; ++i) {
            if (outputLocals != null && i < outputLocals.length) {
                s = outputLocals[i];
                if (s == 0) {
                    t = inputLocals[i];
                } else {
                    dim = s & TYPE_DIM;
                    kind = s & TYPE_KIND;
                    if (kind == LOCAL_TYPE) {
                        t = dim + inputLocals[s & TYPE_VALUE];
                    } else if (kind == STACK_TYPE) {
                        t = dim
                                + inputStack[inputStack.length
                                        - (s & TYPE_VALUE)];
                    } else {
                        t = s;
                    }
                }
            } else {
                t = inputLocals[i];
            }
            if (t == UNINITIALIZED_THIS && initThis) {
                t = BASE_OBJECT_TYPE | cw.addType(cw.thisName);
            }
            if (initializations != null
                    && (t & (TYPE_DIM | BASE_TYPE_KIND)) == BASE_UNINITIALIZED_TYPE)
            {
                s = BASE_OBJECT_TYPE
                        | cw.addType(cw.typeTable[t & BASE_TYPE_VALUE].strVal1);
                for (int j = 0; j < initializationCount; ++j) {
                    if (t == initializations[j]) {
                        t = s;
                        break;
                    }
                }
            }
            if (t == 0) {
                t = TOP;
            }
            changed |= merge(cw, t, label.inputLocals, i);
        }

        if (exception != 0) {
            for (i = 0; i < nLocal; ++i) {
                t = inputLocals[i];
                if (t == 0) {
                    t = TOP;
                }
                changed |= merge(cw, t, label.inputLocals, i);
            }
            if (label.inputStack == null) {
                label.inputStack = new int[1];
                changed = true;
            }
            changed |= merge(cw, exception, label.inputStack, 0);
            return changed;
        }

        int nInputStack = inputStack.length + outputStackOffset;
        int nStack = nInputStack + outputStackTop;
        if (label.inputStack == null) {
            label.inputStack = new int[nStack];
            changed = true;
        }

        for (i = 0; i < nInputStack; ++i) {
            t = inputStack[i];
            if (t == UNINITIALIZED_THIS && initThis) {
                t = BASE_OBJECT_TYPE | cw.addType(cw.thisName);
            }
            if (initializations != null
                    && (t & (TYPE_DIM | BASE_TYPE_KIND)) == BASE_UNINITIALIZED_TYPE)
            {
                s = BASE_OBJECT_TYPE
                        | cw.addType(cw.typeTable[t & BASE_TYPE_VALUE].strVal1);
                for (int j = 0; j < initializationCount; ++j) {
                    if (t == initializations[j]) {
                        t = s;
                        break;
                    }
                }
            }
            changed |= merge(cw, t, label.inputStack, i);
        }
        for (i = 0; i < outputStackTop; ++i) {
            s = outputStack[i];
            dim = s & TYPE_DIM;
            kind = s & TYPE_KIND;
            if (kind == LOCAL_TYPE) {
                t = dim + inputLocals[s & TYPE_VALUE];
            } else if (kind == STACK_TYPE) {
                t = dim + inputStack[inputStack.length - (s & TYPE_VALUE)];
            } else {
                t = s;
            }
            if (t == UNINITIALIZED_THIS && initThis) {
                t = BASE_OBJECT_TYPE | cw.addType(cw.thisName);
            }
            if (initializations != null
                    && (t & (TYPE_DIM | BASE_TYPE_KIND)) == BASE_UNINITIALIZED_TYPE)
            {
                s = BASE_OBJECT_TYPE
                        | cw.addType(cw.typeTable[t & BASE_TYPE_VALUE].strVal1);
                for (int j = 0; j < initializationCount; ++j) {
                    if (t == initializations[j]) {
                        t = s;
                        break;
                    }
                }
            }
            changed |= merge(cw, t, label.inputStack, nInputStack + i);
        }
        return changed;
    }

    /**
     * Merges the type at the given index in the given type array with the given
     * type. Returns <tt>true</tt> if the type array has been modified by this
     * operation.
     * 
     * @param cw the ClassWriter to which this label belongs.
     * @param t the type with which the type array element must be merged.
     * @param types an array of types.
     * @param index the index of the type that must be merged in 'types'.
     * @return <tt>true</tt> if the type array has been modified by this
     *         operation.
     */
    private boolean merge(
        final ClassWriter cw,
        int t,
        final int[] types,
        final int index)
    {
        int u = types[index];
        if (u == t) { // if the types are equal, merge(u,t)=u, so there is no
            // change
            return false;
        }
        if (t == BOOLEAN || t == BYTE || t == CHAR || t == SHORT)
            t = INTEGER;
        if (u == 0) { // if types[index] has never been assigned, merge(u,t)=t
            types[index] = t;
            return true;
        }
        int v;
        if ((u & BASE_TYPE_KIND) == BASE_OBJECT_TYPE || (u & TYPE_DIM) != 0) {
            // if u is a reference type of any dimension
            if (t == NULL) {
                // if t is the NULL type, merge(u,t)=u, so there is no change
                return false;
            } else if ((t & (TYPE_DIM | BASE_TYPE_KIND)) == (u & (TYPE_DIM | BASE_TYPE_KIND)))
            {
                if ((u & BASE_TYPE_KIND) == BASE_OBJECT_TYPE) {
                    // if t is also a reference type, and if u and t have the
                    // same dimension
                    // merge(u,t) = dim(t) | common parent of the element types
                    // of u and t
                    v = (t & TYPE_DIM)
                            | BASE_OBJECT_TYPE
                            | cw.getMergedType(t & BASE_TYPE_VALUE, u
                                    & BASE_TYPE_VALUE);
                } else {
                    v = TOP;
                }
            } else {
                // if t is any other type, merge(u,t)=TOP
                v = TOP;
            }
        } else if (u == NULL) {
            // if u is the NULL type, merge(u,t)=t, or TOP if t is not a
            // reference type
            v = (t & BASE_TYPE_KIND) == BASE_OBJECT_TYPE || (t & TYPE_DIM) != 0
                    ? t
                    : TOP;
        } else {
            // if u is any other type, merge(u,t)=TOP whatever t
            v = TOP;
        }
        if (u != v) {
            types[index] = v;
            return true;
        }
        return false;
    }
}
