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

/**
 * A {@link MethodVisitor} that generates a corresponding 'method_info' structure, as defined in the
 * Java Virtual Machine Specification (JVMS).
 *
 * @see <a href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.6">JVMS
 *     4.6</a>
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
final class MethodWriter extends MethodVisitor {

  // ASM specific access flags

  // WARNING: the 16 least significant bits must NOT be used, to avoid conflicts with standard
  // access flags, and also to make sure that these flags are automatically filtered out when
  // written in class files (because access flags are stored using 16 bits only).

  // Keep in sync with Opcodes.java (central place collecting all access flags, to make sure there
  // are no duplicates).

  /** ASM specific access flag used to denote constructors. */
  static final int ACC_CONSTRUCTOR = 0x40000;

  /**
   * Indicates that all the stack map frames must be recomputed from scratch. In this case the
   * maximum stack size and number of local variables is also recomputed from scratch.
   *
   * @see #compute
   */
  static final int COMPUTE_ALL_FRAMES = 3;

  /**
   * Indicates that the stack map frames of type F_INSERT must be computed. The other frames are not
   * (re)computed. They should all be of type F_NEW and should be sufficient to compute the content
   * of the F_INSERT frames, together with the bytecode instructions between a F_NEW and a F_INSERT
   * frame - and without any knowledge of the type hierarchy (by definition of F_INSERT).
   *
   * @see #compute
   */
  static final int COMPUTE_INSERTED_FRAMES = 2;

  /**
   * Indicates that the maximum stack size and number of local variables must be automatically
   * computed.
   *
   * @see #compute
   */
  static final int COMPUTE_MAX_STACK_AND_LOCAL = 1;

  /**
   * Indicates that nothing must be automatically computed.
   *
   * @see #compute
   */
  static final int COMPUTE_NOTHING = 0;

  /** Where the constants used in this MethodWriter must be stored. */
  private final SymbolTable symbolTable;

  // Note: fields are ordered as in the method_info structure, and those related to attributes are
  // ordered as in Section 4.7 of the JVMS.

  /**
   * The access_flags field of the method_info JVMS structure. This field can contain ASM specific
   * access flags, such as {@link Opcodes#ACC_DEPRECATED}, which are removed when generating the
   * ClassFile structure.
   */
  private final int accessFlags;

  /** The name_index field of the method_info JVMS structure. */
  private final int nameIndex;

  /** The descriptor_index field of the method_info JVMS structure. */
  private final int descriptorIndex;

  /** The descriptor of this method. */
  private final String descriptor;

  // Code attribute fields and sub attributes:

  /** The max_stack field of the Code attribute. */
  private int maxStack;

  /** The max_locals field of the Code attribute. */
  private int maxLocals;

  /** The 'code' field of the Code attribute. */
  private final ByteVector code = new ByteVector();

  /**
   * The first element in the exception handler list (used to generate the exception_table of the
   * Code attribute). The next ones can be accessed with the {@link Handler#nextHandler} field. May
   * be <tt>null</tt>.
   */
  private Handler firstHandler;

  /**
   * The last element in the exception handler list (used to generate the exception_table of the
   * Code attribute). The next ones can be accessed with the {@link Handler#nextHandler} field. May
   * be <tt>null</tt>.
   */
  private Handler lastHandler;

  /** The line_number_table_length field of the LineNumberTable code attribute. */
  private int lineNumberTableLength;

  /** The line_number_table array of the LineNumberTable code attribute, or <tt>null</tt>. */
  private ByteVector lineNumberTable;

  /** The local_variable_table_length field of the LocalVariableTable code attribute. */
  private int localVariableTableLength;

  /** The local_variable_table array of the LocalVariableTable code attribute, or <tt>null</tt>. */
  private ByteVector localVariableTable;

  /** The local_variable_type_table_length field of the LocalVariableTypeTable code attribute. */
  private int localVariableTypeTableLength;

  /**
   * The local_variable_type_table array of the LocalVariableTypeTable code attribute, or
   * <tt>null</tt>.
   */
  private ByteVector localVariableTypeTable;

  /** The number_of_entries field of the StackMapTable code attribute. */
  private int stackMapTableNumberOfEntries;

  /** The 'entries' array of the StackMapTable code attribute. */
  private ByteVector stackMapTableEntries;

  /**
   * The last runtime visible type annotation of the Code attribute. The previous ones can be
   * accessed with the {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastCodeRuntimeVisibleTypeAnnotation;

  /**
   * The last runtime invisible type annotation of the Code attribute. The previous ones can be
   * accessed with the {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastCodeRuntimeInvisibleTypeAnnotation;

  /**
   * The first non standard attribute of the Code attribute. The next ones can be accessed with the
   * {@link Attribute#nextAttribute} field. May be <tt>null</tt>.
   *
   * <p><b>WARNING</b>: this list stores the attributes in the <i>reverse</i> order of their visit.
   * firstAttribute is actually the last attribute visited in {@link #visitAttribute}. The {@link
   * #putMethodInfo} method writes the attributes in the order defined by this list, i.e. in the
   * reverse order specified by the user.
   */
  private Attribute firstCodeAttribute;

  // Other method_info attributes:

  /** The number_of_exceptions field of the Exceptions attribute. */
  final int numberOfExceptions;

  /** The exception_index_table array of the Exceptions attribute, or <tt>null</tt>. */
  final int[] exceptionIndexTable;

  /** The signature_index field of the Signature attribute. */
  final int signatureIndex;

  /**
   * The last runtime visible annotation of this method. The previous ones can be accessed with the
   * {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastRuntimeVisibleAnnotation;

  /**
   * The last runtime invisible annotation of this method. The previous ones can be accessed with
   * the {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastRuntimeInvisibleAnnotation;

  /** The number of method parameters that can have runtime visible annotations, or 0. */
  private int visibleAnnotableParameterCount;

  /**
   * The runtime visible parameter annotations of this method. Each array element contains the last
   * annotation of a parameter (which can be <tt>null</tt> - the previous ones can be accessed with
   * the {@link AnnotationWriter#previousAnnotation} field). May be <tt>null</tt>.
   */
  private AnnotationWriter[] lastRuntimeVisibleParameterAnnotations;

  /** The number of method parameters that can have runtime visible annotations, or 0. */
  private int invisibleAnnotableParameterCount;

  /**
   * The runtime invisible parameter annotations of this method. Each array element contains the
   * last annotation of a parameter (which can be <tt>null</tt> - the previous ones can be accessed
   * with the {@link AnnotationWriter#previousAnnotation} field). May be <tt>null</tt>.
   */
  private AnnotationWriter[] lastRuntimeInvisibleParameterAnnotations;

  /**
   * The last runtime visible type annotation of this method. The previous ones can be accessed with
   * the {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastRuntimeVisibleTypeAnnotation;

  /**
   * The last runtime invisible type annotation of this method. The previous ones can be accessed
   * with the {@link AnnotationWriter#previousAnnotation} field. May be <tt>null</tt>.
   */
  private AnnotationWriter lastRuntimeInvisibleTypeAnnotation;

  /** The default_value field of the AnnotationDefault attribute, or <tt>null</tt>. */
  private ByteVector defaultValue;

  /** The parameters_count field of the MethodParameters attribute. */
  private int parametersCount;

  /** The 'parameters' array of the MethodParameters attribute, or <tt>null</tt>. */
  private ByteVector parameters;

  /**
   * The first non standard attribute of this method. The next ones can be accessed with the {@link
   * Attribute#nextAttribute} field. May be <tt>null</tt>.
   *
   * <p><b>WARNING</b>: this list stores the attributes in the <i>reverse</i> order of their visit.
   * firstAttribute is actually the last attribute visited in {@link #visitAttribute}. The {@link
   * #putMethodInfo} method writes the attributes in the order defined by this list, i.e. in the
   * reverse order specified by the user.
   */
  private Attribute firstAttribute;

  // -----------------------------------------------------------------------------------------------
  // Fields used to compute the maximum stack size and number of locals, and the stack map frames
  // -----------------------------------------------------------------------------------------------

  /**
   * Indicates what must be automatically computed. Must be one of {@link #COMPUTE_ALL_FRAMES},
   * {@link #COMPUTE_INSERTED_FRAMES}, {@link #COMPUTE_MAX_STACK_AND_LOCAL} or {@link
   * #COMPUTE_NOTHING}.
   */
  private final int compute;

  /**
   * The first basic block of the method. The next ones (in bytecode offset order) can be accessed
   * with the {@link Label#nextBasicBlock} field).
   */
  private Label firstBasicBlock;

  /**
   * The last basic block of the method (in bytecode offset order). This field is updated each time
   * a basic block is encountered, and is used to append it at the end of the basic block list.
   */
  private Label lastBasicBlock;

  /**
   * The current basic block, i.e. the basic block of the last visited instruction. When {@link
   * #compute} is equal to {@link #COMPUTE_ALL_FRAMES}, this field is <tt>null</tt> for unreachable
   * code. When {@link #compute} is equal to {@link #COMPUTE_INSERTED_FRAMES}, this field stays
   * unchanged throughout the whole method (i.e. the whole code is seen as a single basic block;
   * indeed, the existing frames are sufficient by hypothesis to compute any intermediate frame
   * without using any control flow graph).
   */
  private Label currentBasicBlock;

  /**
   * The (relative) stack size after the last visited instruction. This size is relative to the
   * beginning of {@link #currentBasicBlock}, i.e. the true stack size after the last visited
   * instruction is equal to the {@link Label#inputStackTop} of the current basic block plus {@link
   * #relativeStackSize}.
   */
  private int relativeStackSize;

  /**
   * The (relative) maximum stack size after the last visited instruction. This size is relative to
   * the beginning of {@link #currentBasicBlock}, i.e. the true maximum stack size after the last
   * visited instruction is equal to the {@link Label#inputStackTop} of the current basic block plus
   * {@link #maxRelativeStackSize}.
   */
  private int maxRelativeStackSize;

  /** The number of local variables in the last visited stack map frame. */
  private int currentLocals;

  /** The bytecode offset of the last frame that was written in {@link #stackMapTableEntries}. */
  private int previousFrameOffset;

  /**
   * The last frame that was written in {@link #stackMapTableEntries}. This field has the same
   * format as {@link #currentFrame}.
   */
  private int[] previousFrame;

  /**
   * The current stack map frame. The first element contains the bytecode offset of the instruction
   * to which the frame corresponds, the second element is the number of locals and the third one is
   * the number of stack elements. The local variables start at index 3 and are followed by the
   * operand stack values. In summary frame[0] = offset, frame[1] = nLocal, frame[2] = nStack,
   * frame[3] = nLocal. All types are encoded as integers, with the same format as the one used in
   * {@link Frame}, but limited to {@link Frame#BASE} types.
   */
  private int[] currentFrame;

  /** The number of subroutines in this method. */
  private int numSubroutines;

  // -----------------------------------------------------------------------------------------------
  // Other miscellaneous status fields
  // -----------------------------------------------------------------------------------------------

  /** Whether the bytecode of this method contains ASM specific instructions. */
  private boolean hasAsmInstructions;

  /**
   * The start offset of the last visited instruction. Used to set the offset field of type
   * annotations of type 'offset_target' (see <a
   * href="https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.1">JVMS
   * 4.7.20.1</a>).
   */
  private int lastBytecodeOffset;

  /**
   * The offset in bytes in {@link #getSource} from which the method_info for this method (excluding
   * its first 6 bytes) must be copied, or 0.
   */
  int sourceOffset;

  /**
   * The length in bytes in {@link #getSource} which must be copied to get the method_info for this
   * method (excluding its first 6 bytes for access_flags, name_index and descriptor_index).
   */
  int sourceLength;

  // -----------------------------------------------------------------------------------------------
  // Constructor and accessors
  // -----------------------------------------------------------------------------------------------

  /**
   * Constructs a new {@link MethodWriter}.
   *
   * @param symbolTable where the constants used in this AnnotationWriter must be stored.
   * @param access the method's access flags (see {@link Opcodes}).
   * @param name the method's name.
   * @param descriptor the method's descriptor (see {@link Type}).
   * @param signature the method's signature. May be <tt>null</tt>.
   * @param exceptions the internal names of the method's exceptions. May be <tt>null</tt>.
   * @param compute Indicates what must be automatically computed (see #compute).
   */
  MethodWriter(
      final SymbolTable symbolTable,
      final int access,
      final String name,
      final String descriptor,
      final String signature,
      final String[] exceptions,
      final int compute) {
    super(Opcodes.ASM6);
    this.symbolTable = symbolTable;
    this.accessFlags = "<init>".equals(name) ? access | ACC_CONSTRUCTOR : access;
    this.nameIndex = symbolTable.addConstantUtf8(name);
    this.descriptorIndex = symbolTable.addConstantUtf8(descriptor);
    this.descriptor = descriptor;
    this.signatureIndex = signature == null ? 0 : symbolTable.addConstantUtf8(signature);
    if (exceptions != null && exceptions.length > 0) {
      numberOfExceptions = exceptions.length;
      this.exceptionIndexTable = new int[numberOfExceptions];
      for (int i = 0; i < numberOfExceptions; ++i) {
        this.exceptionIndexTable[i] = symbolTable.addConstantClass(exceptions[i]).index;
      }
    } else {
      numberOfExceptions = 0;
      this.exceptionIndexTable = null;
    }
    this.compute = compute;
    if (compute != COMPUTE_NOTHING) {
      // Update maxLocals and currentLocals
      int argumentsSize = Type.getArgumentsAndReturnSizes(descriptor) >> 2;
      if ((access & Opcodes.ACC_STATIC) != 0) {
        --argumentsSize;
      }
      maxLocals = argumentsSize;
      currentLocals = argumentsSize;
      // Create and visit the label for the first basic block
      firstBasicBlock = new Label();
      visitLabel(firstBasicBlock);
    }
  }

  ClassReader getSource() {
    return symbolTable.getSource();
  }

  boolean hasFrames() {
    return stackMapTableNumberOfEntries > 0;
  }

  boolean hasAsmInstructions() {
    return hasAsmInstructions;
  }

  // -----------------------------------------------------------------------------------------------
  // Implementation of the MethodVisitor abstract class
  // -----------------------------------------------------------------------------------------------

  @Override
  public void visitParameter(String name, int access) {
    if (parameters == null) {
      parameters = new ByteVector();
    }
    ++parametersCount;
    parameters.putShort((name == null) ? 0 : symbolTable.addConstantUtf8(name)).putShort(access);
  }

  @Override
  public AnnotationVisitor visitAnnotationDefault() {
    defaultValue = new ByteVector();
    return new AnnotationWriter(symbolTable, /* useNamedValues = */ false, defaultValue, null);
  }

  @Override
  public AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    // Create a ByteVector to hold an 'annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.16.
    ByteVector annotation = new ByteVector();
    // Write type_index and reserve space for num_element_value_pairs.
    annotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return lastRuntimeVisibleAnnotation =
          new AnnotationWriter(symbolTable, annotation, lastRuntimeVisibleAnnotation);
    } else {
      return lastRuntimeInvisibleAnnotation =
          new AnnotationWriter(symbolTable, annotation, lastRuntimeInvisibleAnnotation);
    }
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(
      final int typeRef, final TypePath typePath, final String desc, final boolean visible) {
    // Create a ByteVector to hold a 'type_annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.
    ByteVector typeAnnotation = new ByteVector();
    // Write target_type, target_info, and target_path.
    TypeReference.putTarget(typeRef, typeAnnotation);
    TypePath.put(typePath, typeAnnotation);
    // Write type_index and reserve space for num_element_value_pairs.
    typeAnnotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return lastRuntimeVisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastRuntimeVisibleTypeAnnotation);
    } else {
      return lastRuntimeInvisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastRuntimeInvisibleTypeAnnotation);
    }
  }

  @Override
  public void visitAnnotableParameterCount(int parameterCount, boolean visible) {
    if (visible) {
      visibleAnnotableParameterCount = parameterCount;
    } else {
      invisibleAnnotableParameterCount = parameterCount;
    }
  }

  @Override
  public AnnotationVisitor visitParameterAnnotation(
      final int parameter, final String desc, final boolean visible) {
    // Create a ByteVector to hold an 'annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.16.
    ByteVector annotation = new ByteVector();
    // Write type_index and reserve space for num_element_value_pairs.
    annotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      if (lastRuntimeVisibleParameterAnnotations == null) {
        lastRuntimeVisibleParameterAnnotations =
            new AnnotationWriter[Type.getArgumentTypes(descriptor).length];
      }
      return lastRuntimeVisibleParameterAnnotations[parameter] =
          new AnnotationWriter(
              symbolTable, annotation, lastRuntimeVisibleParameterAnnotations[parameter]);
    } else {
      if (lastRuntimeInvisibleParameterAnnotations == null) {
        lastRuntimeInvisibleParameterAnnotations =
            new AnnotationWriter[Type.getArgumentTypes(descriptor).length];
      }
      return lastRuntimeInvisibleParameterAnnotations[parameter] =
          new AnnotationWriter(
              symbolTable, annotation, lastRuntimeInvisibleParameterAnnotations[parameter]);
    }
  }

  @Override
  public void visitAttribute(final Attribute attribute) {
    // Store the attributes in the <i>reverse</i> order of their visit by this method.
    if (attribute.isCodeAttribute()) {
      attribute.nextAttribute = firstCodeAttribute;
      firstCodeAttribute = attribute;
    } else {
      attribute.nextAttribute = firstAttribute;
      firstAttribute = attribute;
    }
  }

  @Override
  public void visitCode() {}

  @Override
  public void visitFrame(
      final int type,
      final int nLocal,
      final Object[] local,
      final int nStack,
      final Object[] stack) {
    if (compute == COMPUTE_ALL_FRAMES) {
      return;
    }

    if (compute == COMPUTE_INSERTED_FRAMES) {
      if (currentBasicBlock.frame == null) {
        // This should happen only once, for the implicit first frame (which is explicitly visited
        // in ClassReader if the EXPAND_ASM_INSNS option is used).
        currentBasicBlock.frame = new CurrentFrame();
        currentBasicBlock.frame.owner = currentBasicBlock;
        currentBasicBlock.frame.initInputFrame(
            symbolTable, accessFlags, Type.getArgumentTypes(descriptor), nLocal);
        visitImplicitFirstFrame();
      } else {
        if (type == Opcodes.F_NEW) {
          currentBasicBlock.frame.set(symbolTable, nLocal, local, nStack, stack);
        } else {
          // In this case type is equal to F_INSERT by hypothesis, and currentBlock.frame contains
          // the stack map frame at the current instruction, computed from the last F_NEW frame
          // and the bytecode instructions in between (via calls to CurrentFrame#execute).
        }
        visitFrame(currentBasicBlock.frame);
      }
    } else if (type == Opcodes.F_NEW) {
      if (previousFrame == null) {
        visitImplicitFirstFrame();
      }
      currentLocals = nLocal;
      int frameIndex = startFrame(code.length, nLocal, nStack);
      for (int i = 0; i < nLocal; ++i) {
        if (local[i] instanceof String) {
          String descriptor = Type.getObjectType((String) local[i]).getDescriptor();
          currentFrame[frameIndex++] = Frame.type(symbolTable, descriptor);
        } else if (local[i] instanceof Integer) {
          currentFrame[frameIndex++] = Frame.BASE | ((Integer) local[i]).intValue();
        } else {
          currentFrame[frameIndex++] =
              Frame.UNINITIALIZED
                  | symbolTable.addUninitializedType("", ((Label) local[i]).position);
        }
      }
      for (int i = 0; i < nStack; ++i) {
        if (stack[i] instanceof String) {
          String descriptor = Type.getObjectType((String) stack[i]).getDescriptor();
          currentFrame[frameIndex++] = Frame.type(symbolTable, descriptor);
        } else if (stack[i] instanceof Integer) {
          currentFrame[frameIndex++] = Frame.BASE | ((Integer) stack[i]).intValue();
        } else {
          currentFrame[frameIndex++] =
              Frame.UNINITIALIZED
                  | symbolTable.addUninitializedType("", ((Label) stack[i]).position);
        }
      }
      endFrame();
    } else {
      int offsetDelta;
      if (stackMapTableEntries == null) {
        stackMapTableEntries = new ByteVector();
        offsetDelta = code.length;
      } else {
        offsetDelta = code.length - previousFrameOffset - 1;
        if (offsetDelta < 0) {
          if (type == Opcodes.F_SAME) {
            return;
          } else {
            throw new IllegalStateException();
          }
        }
      }

      switch (type) {
        case Opcodes.F_FULL:
          currentLocals = nLocal;
          stackMapTableEntries.putByte(Frame.FULL_FRAME).putShort(offsetDelta).putShort(nLocal);
          for (int i = 0; i < nLocal; ++i) {
            writeFrameType(local[i]);
          }
          stackMapTableEntries.putShort(nStack);
          for (int i = 0; i < nStack; ++i) {
            writeFrameType(stack[i]);
          }
          break;
        case Opcodes.F_APPEND:
          currentLocals += nLocal;
          stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED + nLocal).putShort(offsetDelta);
          for (int i = 0; i < nLocal; ++i) {
            writeFrameType(local[i]);
          }
          break;
        case Opcodes.F_CHOP:
          currentLocals -= nLocal;
          stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED - nLocal).putShort(offsetDelta);
          break;
        case Opcodes.F_SAME:
          if (offsetDelta < 64) {
            stackMapTableEntries.putByte(offsetDelta);
          } else {
            stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED).putShort(offsetDelta);
          }
          break;
        case Opcodes.F_SAME1:
          if (offsetDelta < 64) {
            stackMapTableEntries.putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME + offsetDelta);
          } else {
            stackMapTableEntries
                .putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
                .putShort(offsetDelta);
          }
          writeFrameType(stack[0]);
          break;
      }

      previousFrameOffset = code.length;
      ++stackMapTableNumberOfEntries;
    }

    maxStack = Math.max(maxStack, nStack);
    maxLocals = Math.max(maxLocals, currentLocals);
  }

  @Override
  public void visitInsn(final int opcode) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    code.putByte(opcode);
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, 0, null, null);
      } else {
        int size = relativeStackSize + Frame.SIZE[opcode];
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
      if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) || opcode == Opcodes.ATHROW) {
        endCurrentBasicBlockWithNoSuccessor();
      }
    }
  }

  @Override
  public void visitIntInsn(final int opcode, final int operand) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    if (opcode == Opcodes.SIPUSH) {
      code.put12(opcode, operand);
    } else { // BIPUSH or NEWARRAY
      code.put11(opcode, operand);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, operand, null, null);
      } else if (opcode != Opcodes.NEWARRAY) {
        // The stack size delta is 1 for BIPUSH or SIPUSH, and 0 for NEWARRAY.
        int size = relativeStackSize + 1;
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    if (var < 4 && opcode != Opcodes.RET) {
      int optimizedOpcode;
      if (opcode < Opcodes.ISTORE) {
        optimizedOpcode = 26 /* ILOAD_0 */ + ((opcode - Opcodes.ILOAD) << 2) + var;
      } else {
        optimizedOpcode = 59 /* ISTORE_0 */ + ((opcode - Opcodes.ISTORE) << 2) + var;
      }
      code.putByte(optimizedOpcode);
    } else if (var >= 256) {
      code.putByte(196 /* WIDE */).put12(opcode, var);
    } else {
      code.put11(opcode, var);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, var, null, null);
      } else {
        if (opcode == Opcodes.RET) {
          // No stack size delta.
          currentBasicBlock.status |= Label.BASIC_BLOCK_ENDS_WITH_RET;
          // Save 'relativeStackSize' for future use in {@link Label#visitSubroutine}.
          currentBasicBlock.inputStackTop = relativeStackSize;
          endCurrentBasicBlockWithNoSuccessor();
        } else { // xLOAD or xSTORE
          int size = relativeStackSize + Frame.SIZE[opcode];
          if (size > maxRelativeStackSize) {
            maxRelativeStackSize = size;
          }
          relativeStackSize = size;
        }
      }
    }
    if (compute != COMPUTE_NOTHING) {
      int currentLocals;
      if (opcode == Opcodes.LLOAD
          || opcode == Opcodes.DLOAD
          || opcode == Opcodes.LSTORE
          || opcode == Opcodes.DSTORE) {
        currentLocals = var + 2;
      } else {
        currentLocals = var + 1;
      }
      if (currentLocals > maxLocals) {
        maxLocals = currentLocals;
      }
    }
    if (opcode >= Opcodes.ISTORE && compute == COMPUTE_ALL_FRAMES && firstHandler != null) {
      // If there are exception handler blocks, each instruction within a handler range is, in
      // theory, a basic block (since execution can jump from this instruction to the exception
      // handler). As a consequence, the local variable types at the beginning of the handler
      // block should be the merge of the local variable types at all the instructions within the
      // handler range. However, instead of creating a basic block for each instruction, we can
      // get the same result in a more efficient way. Namely, by starting a new basic block after
      // each xSTORE instruction, which is what we do here.
      visitLabel(new Label());
    }
  }

  @Override
  public void visitTypeInsn(final int opcode, final String type) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol typeSymbol = symbolTable.addConstantClass(type);
    code.put12(opcode, typeSymbol.index);
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, lastBytecodeOffset, symbolTable, typeSymbol);
      } else if (opcode == Opcodes.NEW) {
        // The stack size delta is 1 for NEW, and 0 for ANEWARRAY, CHECKCAST, or INSTANCEOF.
        int size = relativeStackSize + 1;
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitFieldInsn(
      final int opcode, final String owner, final String name, final String desc) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol fieldrefSymbol = symbolTable.addConstantFieldref(owner, name, desc);
    code.put12(opcode, fieldrefSymbol.index);
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, 0, symbolTable, fieldrefSymbol);
      } else {
        int size;
        char firstDescChar = desc.charAt(0);
        switch (opcode) {
          case Opcodes.GETSTATIC:
            size = relativeStackSize + (firstDescChar == 'D' || firstDescChar == 'J' ? 2 : 1);
            break;
          case Opcodes.PUTSTATIC:
            size = relativeStackSize + (firstDescChar == 'D' || firstDescChar == 'J' ? -2 : -1);
            break;
          case Opcodes.GETFIELD:
            size = relativeStackSize + (firstDescChar == 'D' || firstDescChar == 'J' ? 1 : 0);
            break;
          case Opcodes.PUTFIELD:
          default:
            size = relativeStackSize + (firstDescChar == 'D' || firstDescChar == 'J' ? -3 : -2);
            break;
        }
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitMethodInsn(
      final int opcode,
      final String owner,
      final String name,
      final String desc,
      final boolean itf) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol methodrefSymbol = symbolTable.addConstantMethodref(owner, name, desc, itf);
    if (opcode == Opcodes.INVOKEINTERFACE) {
      code.put12(Opcodes.INVOKEINTERFACE, methodrefSymbol.index)
          .put11(methodrefSymbol.getArgumentsAndReturnSizes() >> 2, 0);
    } else {
      code.put12(opcode, methodrefSymbol.index);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(opcode, 0, symbolTable, methodrefSymbol);
      } else {
        int argumentsAndReturnSize = methodrefSymbol.getArgumentsAndReturnSizes();
        int stackSizeDelta = (argumentsAndReturnSize & 3) - (argumentsAndReturnSize >> 2);
        int size;
        if (opcode == Opcodes.INVOKESTATIC) {
          size = relativeStackSize + stackSizeDelta + 1;
        } else {
          size = relativeStackSize + stackSizeDelta;
        }
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitInvokeDynamicInsn(
      final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol invokeDynamicSymbol = symbolTable.addConstantInvokeDynamic(name, desc, bsm, bsmArgs);
    code.put12(Opcodes.INVOKEDYNAMIC, invokeDynamicSymbol.index);
    code.putShort(0);
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(Opcodes.INVOKEDYNAMIC, 0, symbolTable, invokeDynamicSymbol);
      } else {
        int argumentsAndReturnSize = invokeDynamicSymbol.getArgumentsAndReturnSizes();
        int stackSizeDelta = (argumentsAndReturnSize & 3) - (argumentsAndReturnSize >> 2) + 1;
        int size = relativeStackSize + stackSizeDelta;
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    // Compute the 'base' opcode, i.e. GOTO or JSR if opcode is GOTO_W or JSR_W, otherwise opcode.
    int baseOpcode = opcode >= 200 /* GOTO_W */ ? opcode - 33 : opcode;
    boolean nextInsnIsJumpTarget = false;
    if ((label.status & Label.RESOLVED) != 0 && label.position - code.length < Short.MIN_VALUE) {
      // Case of a backward jump with an offset < -32768. In this case we automatically replace GOTO
      // with GOTO_W, JSR with JSR_W and IFxxx <l> with IFNOTxxx <L> GOTO_W <l> L:..., where
      // IFNOTxxx is the "opposite" opcode of IFxxx (e.g. IFNE for IFEQ) and where <L> designates
      // the instruction just after the GOTO_W.
      if (baseOpcode == Opcodes.GOTO) {
        code.putByte(200 /* GOTO_W */);
      } else if (baseOpcode == Opcodes.JSR) {
        code.putByte(201 /* JSR_W */);
      } else {
        // Put the "opposite" opcode of baseOpcode. This can be done by flipping the least
        // significant bit for IFNULL and IFNONNULL, and similarly for IFEQ ... IF_ACMPEQ (with a
        // pre and post offset by 1). The jump offset is 8 bytes (3 for IFNOTxxx, 5 for GOTO_W).
        code.putByte(baseOpcode >= Opcodes.IFNULL ? baseOpcode ^ 1 : ((baseOpcode + 1) ^ 1) - 1);
        code.putShort(8);
        // Here we could put a GOTO_W in theory, but if ASM specific instructions are used in this
        // method or another one, and if the class has frames, we will need to insert a frame after
        // this GOTO_W during the additional ClassReader -> ClassWriter round trip to remove the ASM
        // specific instructions. To not miss this additional frame, we need to use an ASM_GOTO_W
        // here, which has the unfortunate effect of forcing this additional round trip (which in
        // some case would not have been really necessary, but we can't know this at this point).
        code.putByte(220 /* ASM_GOTO_W */);
        hasAsmInstructions = true;
        // The instruction after the GOTO_W becomes the target of the IFNOT instruction.
        nextInsnIsJumpTarget = true;
      }
      label.put(this, code, code.length - 1, true);
    } else if (baseOpcode != opcode) {
      // Case of a GOTO_W or JSR_W specified by the user (normally ClassReader when used to remove
      // ASM specific instructions). In this case we keep the original instruction.
      code.putByte(opcode);
      label.put(this, code, code.length - 1, true);
    } else {
      // Case of a jump with an offset >= -32768, or of a jump with an unknown offset. In these
      // cases we store the offset in 2 bytes (which will be increased via a ClassReader ->
      // ClassWriter round trip if it turns out that 2 bytes are not sufficient).
      code.putByte(baseOpcode);
      label.put(this, code, code.length - 1, false);
    }

    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      Label nextBasicBlock = null;
      if (compute == COMPUTE_ALL_FRAMES) {
        currentBasicBlock.frame.execute(baseOpcode, 0, null, null);
        // Record the fact that 'label' is the target of a jump instruction.
        label.getFirst().status |= Label.TARGET;
        // Add 'label' as a successor of the current basic block.
        addSuccessorToCurrentBasicBlock(Edge.JUMP, label);
        if (baseOpcode != Opcodes.GOTO) {
          // The next instruction starts a new basic block (except for GOTO: by default the code
          // following a goto is unreachable - unless there is an explicit label for it - and we
          // should not compute stack frame types for its instructions).
          nextBasicBlock = new Label();
        }
      } else if (compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(baseOpcode, 0, null, null);
      } else {
        if (baseOpcode == Opcodes.JSR) {
          // Record the fact that 'label' designates a subroutine, if not already done.
          if ((label.status & Label.SUBROUTINE) == 0) {
            label.status |= Label.SUBROUTINE;
            ++numSubroutines;
          }
          currentBasicBlock.status |= Label.BASIC_BLOCK_ENDS_WITH_JSR;
          // Note that, by construction in this method, a JSR block has at least two successors in
          // the control flow graph: the first one (added below) leads to the instruction after the
          // JSR, while the second one (added here) leads to the JSR target.
          addSuccessorToCurrentBasicBlock(relativeStackSize + 1, label);
          // The instruction after the JSR starts a new basic block.
          nextBasicBlock = new Label();
        } else {
          // No need to update maxRelativeStackSize (the stack size delta is always negative).
          relativeStackSize += Frame.SIZE[baseOpcode];
          addSuccessorToCurrentBasicBlock(relativeStackSize, label);
        }
      }
      // If the next instruction starts a new basic block, call visitLabel to add the label of this
      // instruction as a successor of the current block, and to start a new basic block.
      if (nextBasicBlock != null) {
        if (nextInsnIsJumpTarget) {
          nextBasicBlock.status |= Label.TARGET;
        }
        visitLabel(nextBasicBlock);
      }
      if (baseOpcode == Opcodes.GOTO) {
        endCurrentBasicBlockWithNoSuccessor();
      }
    }
  }

  @Override
  public void visitLabel(final Label label) {
    // Resolve the forward references to this label, if any.
    hasAsmInstructions |= label.resolve(this, code.length, code.data);
    // visitLabel starts a new basic block (except for debug only labels), so we need to update the
    // previous and current block references and list of successors.
    if ((label.status & Label.DEBUG_ONLY) != 0) {
      return;
    }
    if (compute == COMPUTE_ALL_FRAMES) {
      if (currentBasicBlock != null) {
        if (label.position == currentBasicBlock.position) {
          // If 'label' has the same offset as the current basic block, don't start a new one
          // (instead merge their flags and make them share the same stack frame).
          currentBasicBlock.status |= (label.status & Label.TARGET);
          label.frame = currentBasicBlock.frame;
          return;
        }
        // End the current basic block (with one new successor).
        addSuccessorToCurrentBasicBlock(Edge.JUMP, label);
      }
      // Start a new current basic block.
      currentBasicBlock = label;
      if (label.frame == null) {
        label.frame = new Frame();
        label.frame.owner = label;
      }
      // Append it at the end of the basic block list.
      if (lastBasicBlock != null) {
        if (label.position == lastBasicBlock.position) {
          lastBasicBlock.status |= (label.status & Label.TARGET);
          label.frame = lastBasicBlock.frame;
          currentBasicBlock = lastBasicBlock;
          return;
        }
        lastBasicBlock.nextBasicBlock = label;
      }
      lastBasicBlock = label;
    } else if (compute == COMPUTE_INSERTED_FRAMES) {
      if (currentBasicBlock == null) {
        // This case should happen only once, for the visitLabel call in the constructor. Indeed, if
        // compute is equal to COMPUTE_INSERTED_FRAMES, currentBasicBlock stays unchanged.
        currentBasicBlock = label;
      } else {
        // Update the frame owner so that a correct frame offset is computed in visitFrame(Frame).
        currentBasicBlock.frame.owner = label;
      }
    } else if (compute == COMPUTE_MAX_STACK_AND_LOCAL) {
      if (currentBasicBlock != null) {
        // End the current basic block (with one new successor).
        currentBasicBlock.outputStackMax = maxRelativeStackSize;
        addSuccessorToCurrentBasicBlock(relativeStackSize, label);
      }
      // Start a new current basic block, and reset the current and maximum relative stack sizes.
      currentBasicBlock = label;
      relativeStackSize = 0;
      maxRelativeStackSize = 0;
      // Append the new basic block at the end of the basic block list.
      if (lastBasicBlock != null) {
        lastBasicBlock.nextBasicBlock = label;
      }
      lastBasicBlock = label;
    }
  }

  @Override
  public void visitLdcInsn(final Object cst) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol constantSymbol = symbolTable.addConstant(cst);
    int constantIndex = constantSymbol.index;
    boolean isLongOrDouble =
        constantSymbol.tag == Symbol.CONSTANT_LONG_TAG
            || constantSymbol.tag == Symbol.CONSTANT_DOUBLE_TAG;
    if (isLongOrDouble) {
      code.put12(20 /* LDC2_W */, constantIndex);
    } else if (constantIndex >= 256) {
      code.put12(19 /* LDC_W */, constantIndex);
    } else {
      code.put11(Opcodes.LDC, constantIndex);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(Opcodes.LDC, 0, symbolTable, constantSymbol);
      } else {
        int size = relativeStackSize + (isLongOrDouble ? 2 : 1);
        if (size > maxRelativeStackSize) {
          maxRelativeStackSize = size;
        }
        relativeStackSize = size;
      }
    }
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    if ((var > 255) || (increment > 127) || (increment < -128)) {
      code.putByte(196 /* WIDE */).put12(Opcodes.IINC, var).putShort(increment);
    } else {
      code.putByte(Opcodes.IINC).put11(var, increment);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(Opcodes.IINC, var, null, null);
      }
    }
    if (compute != COMPUTE_NOTHING) {
      int currentLocals = var + 1;
      if (currentLocals > maxLocals) {
        maxLocals = currentLocals;
      }
    }
  }

  @Override
  public void visitTableSwitchInsn(
      final int min, final int max, final Label dflt, final Label... labels) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    code.putByte(Opcodes.TABLESWITCH).putByteArray(null, 0, (4 - code.length % 4) % 4);
    dflt.put(this, code, lastBytecodeOffset, true);
    code.putInt(min).putInt(max);
    for (Label label : labels) {
      label.put(this, code, lastBytecodeOffset, true);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    visitSwitchInsn(dflt, labels);
  }

  @Override
  public void visitLookupSwitchInsn(final Label dflt, final int[] keys, final Label[] labels) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    code.putByte(Opcodes.LOOKUPSWITCH).putByteArray(null, 0, (4 - code.length % 4) % 4);
    dflt.put(this, code, lastBytecodeOffset, true);
    code.putInt(labels.length);
    for (int i = 0; i < labels.length; ++i) {
      code.putInt(keys[i]);
      labels[i].put(this, code, lastBytecodeOffset, true);
    }
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    visitSwitchInsn(dflt, labels);
  }

  private void visitSwitchInsn(final Label dflt, final Label[] labels) {
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES) {
        currentBasicBlock.frame.execute(Opcodes.LOOKUPSWITCH, 0, null, null);
        // Add all the labels as successors of the current basic block.
        addSuccessorToCurrentBasicBlock(Edge.JUMP, dflt);
        dflt.getFirst().status |= Label.TARGET;
        for (Label label : labels) {
          addSuccessorToCurrentBasicBlock(Edge.JUMP, label);
          label.getFirst().status |= Label.TARGET;
        }
      } else {
        // No need to update maxRelativeStackSize (the stack size delta is always negative).
        --relativeStackSize;
        // Add all the labels as successors of the current basic block.
        addSuccessorToCurrentBasicBlock(relativeStackSize, dflt);
        for (Label label : labels) {
          addSuccessorToCurrentBasicBlock(relativeStackSize, label);
        }
      }
      // End the current basic block.
      endCurrentBasicBlockWithNoSuccessor();
    }
  }

  @Override
  public void visitMultiANewArrayInsn(final String desc, final int dims) {
    lastBytecodeOffset = code.length;
    // Add the instruction to the bytecode of the method.
    Symbol descSymbol = symbolTable.addConstantClass(desc);
    code.put12(Opcodes.MULTIANEWARRAY, descSymbol.index).putByte(dims);
    // If needed, update the maximum stack size and number of locals, and stack map frames.
    if (currentBasicBlock != null) {
      if (compute == COMPUTE_ALL_FRAMES || compute == COMPUTE_INSERTED_FRAMES) {
        currentBasicBlock.frame.execute(Opcodes.MULTIANEWARRAY, dims, symbolTable, descSymbol);
      } else {
        // No need to update maxRelativeStackSize (the stack size delta is always negative).
        relativeStackSize += 1 - dims;
      }
    }
  }

  @Override
  public AnnotationVisitor visitInsnAnnotation(
      int typeRef, TypePath typePath, String desc, boolean visible) {
    // Create a ByteVector to hold a 'type_annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.
    ByteVector typeAnnotation = new ByteVector();
    // Write target_type, target_info, and target_path.
    TypeReference.putTarget((typeRef & 0xFF0000FF) | (lastBytecodeOffset << 8), typeAnnotation);
    TypePath.put(typePath, typeAnnotation);
    // Write type_index and reserve space for num_element_value_pairs.
    typeAnnotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return lastCodeRuntimeVisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeVisibleTypeAnnotation);
    } else {
      return lastCodeRuntimeInvisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeInvisibleTypeAnnotation);
    }
  }

  @Override
  public void visitTryCatchBlock(
      final Label start, final Label end, final Label handler, final String type) {
    Handler newHandler =
        new Handler(
            start, end, handler, type != null ? symbolTable.addConstantClass(type).index : 0, type);
    if (firstHandler == null) {
      firstHandler = newHandler;
    } else {
      lastHandler.nextHandler = newHandler;
    }
    lastHandler = newHandler;
  }

  @Override
  public AnnotationVisitor visitTryCatchAnnotation(
      int typeRef, TypePath typePath, String desc, boolean visible) {
    // Create a ByteVector to hold a 'type_annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.
    ByteVector typeAnnotation = new ByteVector();
    // Write target_type, target_info, and target_path.
    TypeReference.putTarget(typeRef, typeAnnotation);
    TypePath.put(typePath, typeAnnotation);
    // Write type_index and reserve space for num_element_value_pairs.
    typeAnnotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return lastCodeRuntimeVisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeVisibleTypeAnnotation);
    } else {
      return lastCodeRuntimeInvisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeInvisibleTypeAnnotation);
    }
  }

  @Override
  public void visitLocalVariable(
      final String name,
      final String desc,
      final String signature,
      final Label start,
      final Label end,
      final int index) {
    if (signature != null) {
      if (localVariableTypeTable == null) {
        localVariableTypeTable = new ByteVector();
      }
      ++localVariableTypeTableLength;
      localVariableTypeTable
          .putShort(start.position)
          .putShort(end.position - start.position)
          .putShort(symbolTable.addConstantUtf8(name))
          .putShort(symbolTable.addConstantUtf8(signature))
          .putShort(index);
    }
    if (localVariableTable == null) {
      localVariableTable = new ByteVector();
    }
    ++localVariableTableLength;
    localVariableTable
        .putShort(start.position)
        .putShort(end.position - start.position)
        .putShort(symbolTable.addConstantUtf8(name))
        .putShort(symbolTable.addConstantUtf8(desc))
        .putShort(index);
    if (compute != COMPUTE_NOTHING) {
      char firstDescChar = desc.charAt(0);
      int currentLocals = index + (firstDescChar == 'J' || firstDescChar == 'D' ? 2 : 1);
      if (currentLocals > maxLocals) {
        maxLocals = currentLocals;
      }
    }
  }

  @Override
  public AnnotationVisitor visitLocalVariableAnnotation(
      int typeRef,
      TypePath typePath,
      Label[] start,
      Label[] end,
      int[] index,
      String desc,
      boolean visible) {
    // Create a ByteVector to hold a 'type_annotation' JVMS structure.
    // See https://docs.oracle.com/javase/specs/jvms/se9/html/jvms-4.html#jvms-4.7.20.
    ByteVector typeAnnotation = new ByteVector();
    // Write target_type, target_info, and target_path.
    typeAnnotation.putByte(typeRef >>> 24).putShort(start.length);
    for (int i = 0; i < start.length; ++i) {
      typeAnnotation
          .putShort(start[i].position)
          .putShort(end[i].position - start[i].position)
          .putShort(index[i]);
    }
    TypePath.put(typePath, typeAnnotation);
    // Write type_index and reserve space for num_element_value_pairs.
    typeAnnotation.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return lastCodeRuntimeVisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeVisibleTypeAnnotation);
    } else {
      return lastCodeRuntimeInvisibleTypeAnnotation =
          new AnnotationWriter(symbolTable, typeAnnotation, lastCodeRuntimeInvisibleTypeAnnotation);
    }
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    if (lineNumberTable == null) {
      lineNumberTable = new ByteVector();
    }
    ++lineNumberTableLength;
    lineNumberTable.putShort(start.position);
    lineNumberTable.putShort(line);
  }

  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {
    if (compute == COMPUTE_ALL_FRAMES) {
      // Complete the control flow graph with exception handler blocks.
      Handler handler = firstHandler;
      while (handler != null) {
        String catchTypeDescriptor =
            handler.catchTypeDescriptor == null
                ? "java/lang/Throwable"
                : handler.catchTypeDescriptor;
        int catchType = Frame.OBJECT | symbolTable.addType(catchTypeDescriptor);
        // Mark handlerBlock as an exception handler.
        Label handlerBlock = handler.handlerPc.getFirst();
        handlerBlock.status |= Label.TARGET;
        // Add handlerBlock as a successor of all the basic blocks in the exception handler range.
        Label handlerRangeBlock = handler.startPc.getFirst();
        Label handlerRangeEnd = handler.endPc.getFirst();
        while (handlerRangeBlock != handlerRangeEnd) {
          handlerRangeBlock.outgoingEdges =
              new Edge(catchType, handlerBlock, handlerRangeBlock.outgoingEdges);
          handlerRangeBlock = handlerRangeBlock.nextBasicBlock;
        }
        handler = handler.nextHandler;
      }

      // Create and visit the first (implicit) frame.
      Frame firstFrame = firstBasicBlock.frame;
      firstFrame.initInputFrame(
          symbolTable, accessFlags, Type.getArgumentTypes(descriptor), this.maxLocals);
      visitFrame(firstFrame);

      // Fix point algorithm: put the first basic block in a list of "changed" blocks (i.e. blocks
      // whose stack map frame has changed) and, while there are changed blocks, remove one from the
      // list and update the stack map frames of its successor blocks in the control flow graph
      // (which might change them, in which case these blocks are added to the changedBlocks list).
      // Also compute the maximum stack size of the method, as a by-product.
      firstBasicBlock.status |= Label.BASIC_BLOCK_CHANGED;
      Label changedBlocks = firstBasicBlock;
      int maxStackSize = 0;
      while (changedBlocks != null) {
        // Get the first basic block from the changedBlocks list and remove it from this list.
        Label basicBlock = changedBlocks;
        changedBlocks = changedBlocks.nextChangedBlock;
        basicBlock.status &= ~Label.BASIC_BLOCK_CHANGED;
        // By definition, basicBlock is reachable.
        basicBlock.status |= Label.REACHABLE;
        // If it is also a jump target, it must be stored in the StackMapTable attribute.
        if ((basicBlock.status & Label.TARGET) != 0) {
          basicBlock.status |= Label.STORE;
        }
        // Update the (absolute) maximum stack size.
        int maxBlockStackSize = basicBlock.frame.inputStack.length + basicBlock.outputStackMax;
        if (maxBlockStackSize > maxStackSize) {
          maxStackSize = maxBlockStackSize;
        }
        // Update the successor blocks of basicBlock in the control flow graph.
        Edge outgoingEdge = basicBlock.outgoingEdges;
        while (outgoingEdge != null) {
          Label successorBlock = outgoingEdge.successor.getFirst();
          boolean successorBlockChanged =
              basicBlock.frame.merge(symbolTable, successorBlock.frame, outgoingEdge.info);
          if (successorBlockChanged && (successorBlock.status & Label.BASIC_BLOCK_CHANGED) == 0) {
            // If successorBlock has changed and is not in changedBlocks, add it to this list.
            successorBlock.status |= Label.BASIC_BLOCK_CHANGED;
            successorBlock.nextChangedBlock = changedBlocks;
            changedBlocks = successorBlock;
          }
          outgoingEdge = outgoingEdge.nextEdge;
        }
      }

      // Loop over all the basic blocks and visit the stack map frames that must be stored in the
      // StackMapTable attribute. Also replace unreachable code with NOP* ATHROW, and remove it from
      // exception handler ranges.
      Label basicBlock = firstBasicBlock;
      while (basicBlock != null) {
        if ((basicBlock.status & Label.STORE) != 0) {
          visitFrame(basicBlock.frame);
        }
        if ((basicBlock.status & Label.REACHABLE) == 0) {
          // Find the start and end bytecode offsets of this unreachable block.
          Label nextBasicBlock = basicBlock.nextBasicBlock;
          int startOffset = basicBlock.position;
          int endOffset = (nextBasicBlock == null ? code.length : nextBasicBlock.position) - 1;
          if (endOffset >= startOffset) {
            // Replace its instructions with NOP ... NOP ATHROW.
            for (int i = startOffset; i < endOffset; ++i) {
              code.data[i] = Opcodes.NOP;
            }
            code.data[endOffset] = (byte) Opcodes.ATHROW;
            // Emit a frame for this unreachable block, with no local and a Throwable on the stack
            // (so that the ATHROW could consume this Throwable if it were reachable).
            int frameIndex = startFrame(startOffset, /* nLocal = */ 0, /* nStack = */ 1);
            currentFrame[frameIndex] = Frame.OBJECT | symbolTable.addType("java/lang/Throwable");
            endFrame();
            // Removes this unreachable basic block from the exception handler ranges.
            firstHandler = Handler.removeRange(firstHandler, basicBlock, nextBasicBlock);
            // The maximum stack size is now at least one, because of the Throwable declared above.
            maxStackSize = Math.max(maxStackSize, 1);
          }
        }
        basicBlock = basicBlock.nextBasicBlock;
      }

      this.maxStack = maxStackSize;
    } else if (compute == COMPUTE_MAX_STACK_AND_LOCAL) {
      // Complete the control flow graph with exception handler blocks.
      Handler handler = firstHandler;
      while (handler != null) {
        Label handlerBlock = handler.handlerPc;
        Label handlerRangeBlock = handler.startPc;
        Label handlerRangeEnd = handler.endPc;
        // Add handlerBlock as a successor of all the basic blocks in the exception handler range.
        while (handlerRangeBlock != handlerRangeEnd) {
          if ((handlerRangeBlock.status & Label.BASIC_BLOCK_ENDS_WITH_JSR) == 0) {
            handlerRangeBlock.outgoingEdges =
                new Edge(Edge.EXCEPTION, handlerBlock, handlerRangeBlock.outgoingEdges);
          } else {
            // If handlerRangeBlock is a JSR block, add handlerBlock after the first two outgoing
            // edges to preserve the hypothesis about JSR block successors order
            // (see {@link #visitJumpInsn}).
            handlerRangeBlock.outgoingEdges.nextEdge.nextEdge =
                new Edge(
                    Edge.EXCEPTION,
                    handlerBlock,
                    handlerRangeBlock.outgoingEdges.nextEdge.nextEdge);
          }
          handlerRangeBlock = handlerRangeBlock.nextBasicBlock;
        }
        handler = handler.nextHandler;
      }

      // Complete the control flow graph with the successor blocks of subroutines, if needed.
      if (numSubroutines > 0) {
        // First step: find the subroutines. This step determines, for each basic block, to which
        // subroutine(s) it belongs. Start with the main "subroutine":
        firstBasicBlock.visitSubroutine(null, 1, numSubroutines);
        // Then, loop over all the basic blocks to find those that belong to real subroutines.
        int subroutineId = 0;
        Label basicBlock = firstBasicBlock;
        while (basicBlock != null) {
          if ((basicBlock.status & Label.SUBROUTINE) != 0
              && (basicBlock.status & Label.VISITED) == 0) {
            // If this subroutine has not been visited yet, find its basic blocks.
            subroutineId += 1;
            basicBlock.visitSubroutine(
                null, (subroutineId / 32L) << 32 | (1L << (subroutineId % 32)), numSubroutines);
          }
          basicBlock = basicBlock.nextBasicBlock;
        }
        // Second step: find the successor blocks of the subroutines.
        basicBlock = firstBasicBlock;
        while (basicBlock != null) {
          if ((basicBlock.status & Label.BASIC_BLOCK_ENDS_WITH_JSR) != 0) {
            Label L = firstBasicBlock;
            while (L != null) {
              L.status &= ~Label.VISITED2;
              L = L.nextBasicBlock;
            }
            // the subroutine is defined by l's TARGET, not by l
            Label subroutine = basicBlock.outgoingEdges.nextEdge.successor;
            subroutine.visitSubroutine(basicBlock, 0, numSubroutines);
          }
          basicBlock = basicBlock.nextBasicBlock;
        }
      }

      // Data flow algorithm: put the first basic block in a list of "changed" blocks (i.e. blocks
      // whose input stack size has changed) and, while there are changed blocks, remove one from
      // the list, update the input stack size of its successor blocks in the control flow graph,
      // and put these blocks in the changedBlocks list (if not already done).
      firstBasicBlock.status |= Label.BASIC_BLOCK_CHANGED;
      Label changedBlocks = firstBasicBlock;
      int maxStackSize = 0;
      while (changedBlocks != null) {
        // Get the first basic block from the changedBlocks list and remove it from this list.
        Label basicBlock = changedBlocks;
        changedBlocks = changedBlocks.nextChangedBlock;
        // Don't remove the BASIC_BLOCK_CHANGED flag: if the code is valid, the maximum stack size
        // can be computed with at most one visit per basic block (which is enforced here by not
        // clearing this flag). Note that property does not hold when computing stack map frames.
        // Compute the (absolute) input stack size and maximum stack size of this block.
        int inputStackTop = basicBlock.inputStackTop;
        int maxBlockStackSize = inputStackTop + basicBlock.outputStackMax;
        // updates the global max stack size
        if (maxBlockStackSize > maxStackSize) {
          maxStackSize = maxBlockStackSize;
        }
        // Update the input stack size of the successor blocks of basicBlock in the control flow
        // graph, and put these blocks in changedBlocks if not already done.
        Edge outgoingEdge = basicBlock.outgoingEdges;
        if ((basicBlock.status & Label.BASIC_BLOCK_ENDS_WITH_JSR) != 0) {
          // Ignore the first outgoing edge of JSR blocks (virtual successor).
          outgoingEdge = outgoingEdge.nextEdge;
        }
        while (outgoingEdge != null) {
          Label successorBlock = outgoingEdge.successor;
          if ((successorBlock.status & Label.BASIC_BLOCK_CHANGED) == 0) {
            successorBlock.inputStackTop =
                outgoingEdge.info == Edge.EXCEPTION ? 1 : inputStackTop + outgoingEdge.info;
            successorBlock.status |= Label.BASIC_BLOCK_CHANGED;
            successorBlock.nextChangedBlock = changedBlocks;
            changedBlocks = successorBlock;
          }
          outgoingEdge = outgoingEdge.nextEdge;
        }
      }
      this.maxStack = Math.max(maxStack, maxStackSize);
    } else {
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
    }
  }

  @Override
  public void visitEnd() {}

  // -----------------------------------------------------------------------------------------------
  // Utility methods: control flow analysis algorithm
  // -----------------------------------------------------------------------------------------------

  /**
   * Adds a successor to {@link #currentBasicBlock} in the control flow graph.
   *
   * @param info information about the control flow edge to be added.
   * @param successor the successor block to be added to the current basic block.
   */
  private void addSuccessorToCurrentBasicBlock(final int info, final Label successor) {
    currentBasicBlock.outgoingEdges = new Edge(info, successor, currentBasicBlock.outgoingEdges);
  }

  /**
   * Ends the current basic block. This method must be used in the case where the current basic
   * block does not have any successor.
   *
   * <p>WARNING: this method must be called after the currently visited instruction has been put in
   * {@link #code} (if frames are computed, this method inserts a new Label to start a new basic
   * block after the current instruction).
   */
  private void endCurrentBasicBlockWithNoSuccessor() {
    if (compute == COMPUTE_ALL_FRAMES) {
      Label nextBasicBlock = new Label();
      nextBasicBlock.frame = new Frame();
      nextBasicBlock.frame.owner = nextBasicBlock;
      nextBasicBlock.resolve(this, code.length, code.data);
      lastBasicBlock.nextBasicBlock = nextBasicBlock;
      lastBasicBlock = nextBasicBlock;
    } else {
      currentBasicBlock.outputStackMax = maxRelativeStackSize;
    }
    if (compute != COMPUTE_INSERTED_FRAMES) {
      currentBasicBlock = null;
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Utility methods: stack map frames
  // -----------------------------------------------------------------------------------------------

  /**
   * Visits a frame that has been computed from scratch.
   *
   * @param frame the frame that must be visited.
   */
  private void visitFrame(final Frame frame) {
    int nLocal = 0;
    int nStack = 0;
    int[] localTypes = frame.inputLocals;
    int[] stackTypes = frame.inputStack;
    // Compute the number of locals, ignoring TOP types that are just after a LONG or a DOUBLE, and
    // all trailing TOP types.
    for (int i = 0, nTrailingTop = 0; i < localTypes.length; ++i) {
      int localType = localTypes[i];
      if (localType == Frame.TOP) {
        ++nTrailingTop;
      } else {
        nLocal += nTrailingTop + 1;
        nTrailingTop = 0;
      }
      if (localType == Frame.LONG || localType == Frame.DOUBLE) {
        ++i;
      }
    }
    // Compute the stack size, ignoring TOP types that are just after a LONG or a DOUBLE.
    for (int i = 0; i < stackTypes.length; ++i) {
      ++nStack;
      int stackType = stackTypes[i];
      if (stackType == Frame.LONG || stackType == Frame.DOUBLE) {
        ++i;
      }
    }
    // Visit the frame and its content.
    int frameIndex = startFrame(frame.owner.position, nLocal, nStack);
    for (int i = 0; nLocal > 0; ++i, --nLocal) {
      int localType = localTypes[i];
      currentFrame[frameIndex++] = localType;
      if (localType == Frame.LONG || localType == Frame.DOUBLE) {
        ++i;
      }
    }
    for (int i = 0; i < stackTypes.length; ++i) {
      int stackType = stackTypes[i];
      currentFrame[frameIndex++] = stackType;
      if (stackType == Frame.LONG || stackType == Frame.DOUBLE) {
        ++i;
      }
    }
    endFrame();
  }

  /** Visit the implicit first frame of this method. */
  private void visitImplicitFirstFrame() {
    // There can be at most descriptor.length() + 1 locals.
    int frameIndex =
        startFrame(/* offset = */ 0, /* nLocal = */ descriptor.length() + 1, /* nStack = */ 0);
    if ((accessFlags & Opcodes.ACC_STATIC) == 0) {
      if ((accessFlags & ACC_CONSTRUCTOR) == 0) {
        currentFrame[frameIndex++] = Frame.OBJECT | symbolTable.addType(symbolTable.getClassName());
      } else {
        currentFrame[frameIndex++] = Frame.UNINITIALIZED_THIS;
      }
    }
    // Parse the descriptor to get the local variable types, one char at a time. descriptorOffset
    // is the offset of the currently parsed char. We skip the first char, always equal to '('.
    int descriptorOffset = 1;
    while (true) {
      // Parse one local variable type descriptor at each loop iteration. First, record the start
      // offset of the current method argument descriptor.
      int startOffsetOfCurrentArgumentDescriptor = descriptorOffset;
      switch (descriptor.charAt(descriptorOffset++)) {
        case 'Z':
        case 'C':
        case 'B':
        case 'S':
        case 'I':
          currentFrame[frameIndex++] = Frame.INTEGER;
          break;
        case 'F':
          currentFrame[frameIndex++] = Frame.FLOAT;
          break;
        case 'J':
          currentFrame[frameIndex++] = Frame.LONG;
          break;
        case 'D':
          currentFrame[frameIndex++] = Frame.DOUBLE;
          break;
        case '[':
          while (descriptor.charAt(descriptorOffset) == '[') {
            ++descriptorOffset;
          }
          if (descriptor.charAt(descriptorOffset) == 'L') {
            ++descriptorOffset;
            while (descriptor.charAt(descriptorOffset) != ';') {
              ++descriptorOffset;
            }
          }
          currentFrame[frameIndex++] =
              Frame.type(
                  symbolTable,
                  descriptor.substring(startOffsetOfCurrentArgumentDescriptor, ++descriptorOffset));
          break;
        case 'L':
          while (descriptor.charAt(descriptorOffset) != ';') {
            ++descriptorOffset;
          }
          currentFrame[frameIndex++] =
              Frame.OBJECT
                  | symbolTable.addType(
                      descriptor.substring(
                          startOffsetOfCurrentArgumentDescriptor + 1, descriptorOffset++));
          break;
        default:
          // End of the descriptor, store the number of local variables in the frame and return.
          currentFrame[1] = frameIndex - 3;
          endFrame();
          return;
      }
    }
  }

  /**
   * Starts the visit of a new stack map frame.
   *
   * @param offset the bytecode offset of the instruction to which the frame corresponds.
   * @param nLocal the number of local variables in the frame.
   * @param nStack the number of stack elements in the frame.
   * @return the index of the next element to be written in this frame.
   */
  private int startFrame(final int offset, final int nLocal, final int nStack) {
    int frameLength = 3 + nLocal + nStack;
    if (currentFrame == null || currentFrame.length < frameLength) {
      currentFrame = new int[frameLength];
    }
    currentFrame[0] = offset;
    currentFrame[1] = nLocal;
    currentFrame[2] = nStack;
    return 3;
  }

  /**
   * Ends the visit of {@link #currentFrame} by writing it in the StackMapTable entries and by
   * updating the StackMapTable number_of_entries (except if the current frame is the first one,
   * which is implicit in StackMapTable). Then resets {@link #currentFrame} to <tt>null</tt>.
   */
  private void endFrame() {
    if (previousFrame != null) {
      if (stackMapTableEntries == null) {
        stackMapTableEntries = new ByteVector();
      }
      writeFrame();
      ++stackMapTableNumberOfEntries;
    }
    previousFrame = currentFrame;
    currentFrame = null;
  }

  /** Compresses and writes {@link #currentFrame} in a new StackMapTable entry. */
  private void writeFrame() {
    final int nLocal = currentFrame[1];
    final int nStack = currentFrame[2];
    if (symbolTable.getMajorVersion() < Opcodes.V1_6) {
      // Generate a StackMap attribute entry, which are always uncompressed.
      stackMapTableEntries.putShort(currentFrame[0]).putShort(nLocal);
      writeFrameTypes(3, 3 + nLocal);
      stackMapTableEntries.putShort(nStack);
      writeFrameTypes(3 + nLocal, 3 + nLocal + nStack);
      return;
    }
    final int offsetDelta =
        stackMapTableNumberOfEntries == 0
            ? currentFrame[0]
            : currentFrame[0] - previousFrame[0] - 1;
    final int previousNlocal = previousFrame[1];
    final int nLocalDelta = nLocal - previousNlocal;
    int type = Frame.FULL_FRAME;
    if (nStack == 0) {
      switch (nLocalDelta) {
        case -3:
        case -2:
        case -1:
          type = Frame.CHOP_FRAME;
          break;
        case 0:
          type = offsetDelta < 64 ? Frame.SAME_FRAME : Frame.SAME_FRAME_EXTENDED;
          break;
        case 1:
        case 2:
        case 3:
          type = Frame.APPEND_FRAME;
          break;
      }
    } else if (nLocalDelta == 0 && nStack == 1) {
      type =
          offsetDelta < 63
              ? Frame.SAME_LOCALS_1_STACK_ITEM_FRAME
              : Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED;
    }
    if (type != Frame.FULL_FRAME) {
      // Verify if locals are the same as in the previous frame.
      int frameIndex = 3;
      for (int i = 0; i < previousNlocal && i < nLocal; i++) {
        if (currentFrame[frameIndex] != previousFrame[frameIndex]) {
          type = Frame.FULL_FRAME;
          break;
        }
        frameIndex++;
      }
    }
    switch (type) {
      case Frame.SAME_FRAME:
        stackMapTableEntries.putByte(offsetDelta);
        break;
      case Frame.SAME_LOCALS_1_STACK_ITEM_FRAME:
        stackMapTableEntries.putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME + offsetDelta);
        writeFrameTypes(3 + nLocal, 4 + nLocal);
        break;
      case Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED:
        stackMapTableEntries
            .putByte(Frame.SAME_LOCALS_1_STACK_ITEM_FRAME_EXTENDED)
            .putShort(offsetDelta);
        writeFrameTypes(3 + nLocal, 4 + nLocal);
        break;
      case Frame.SAME_FRAME_EXTENDED:
        stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED).putShort(offsetDelta);
        break;
      case Frame.CHOP_FRAME:
        stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED + nLocalDelta).putShort(offsetDelta);
        break;
      case Frame.APPEND_FRAME:
        stackMapTableEntries.putByte(Frame.SAME_FRAME_EXTENDED + nLocalDelta).putShort(offsetDelta);
        writeFrameTypes(3 + previousNlocal, 3 + nLocal);
        break;
      case Frame.FULL_FRAME:
      default:
        stackMapTableEntries.putByte(Frame.FULL_FRAME).putShort(offsetDelta).putShort(nLocal);
        writeFrameTypes(3, 3 + nLocal);
        stackMapTableEntries.putShort(nStack);
        writeFrameTypes(3 + nLocal, 3 + nLocal + nStack);
    }
  }

  /**
   * Writes some types of {@link #currentFrame} into the StackMapTableAttribute. This method
   * converts types from the format used in {@link Frame} to the format used in StackMapTable
   * attributes. In particular, it converts type table indices to constant pool indices.
   *
   * @param start index of the first type in {@link #currentFrame} to write.
   * @param end index of last type in {@link #currentFrame} to write (exclusive).
   */
  private void writeFrameTypes(final int start, final int end) {
    for (int i = start; i < end; ++i) {
      int frameType = currentFrame[i];
      int arrayDimensions = (frameType & Frame.DIM) >> 28;
      if (arrayDimensions == 0) {
        int baseType = frameType & Frame.BASE_VALUE;
        switch (frameType & Frame.BASE_KIND) {
          case Frame.OBJECT:
            stackMapTableEntries
                .putByte(Frame.ITEM_OBJECT)
                .putShort(symbolTable.addConstantClass(symbolTable.getType(baseType).value).index);
            break;
          case Frame.UNINITIALIZED:
            stackMapTableEntries
                .putByte(Frame.ITEM_UNINITIALIZED)
                .putShort((int) symbolTable.getType(baseType).data);
            break;
          default:
            stackMapTableEntries.putByte(baseType);
        }
      } else {
        // Case of an array type, we need to build its descriptor first.
        StringBuilder typeDescriptor = new StringBuilder();
        while (arrayDimensions-- > 0) {
          typeDescriptor.append('[');
        }
        if ((frameType & Frame.BASE_KIND) == Frame.OBJECT) {
          typeDescriptor.append('L');
          typeDescriptor.append(symbolTable.getType(frameType & Frame.BASE_VALUE).value);
          typeDescriptor.append(';');
        } else {
          switch (frameType & 0xF) {
            case Frame.ITEM_INTEGER:
              typeDescriptor.append('I');
              break;
            case Frame.ITEM_FLOAT:
              typeDescriptor.append('F');
              break;
            case Frame.ITEM_DOUBLE:
              typeDescriptor.append('D');
              break;
            case Frame.ITEM_LONG:
              typeDescriptor.append('J');
              break;
            case Frame.ITEM_ASM_BOOLEAN:
              typeDescriptor.append('Z');
              break;
            case Frame.ITEM_ASM_BYTE:
              typeDescriptor.append('B');
              break;
            case Frame.ITEM_ASM_CHAR:
              typeDescriptor.append('C');
              break;
            case Frame.ITEM_ASM_SHORT:
              typeDescriptor.append('S');
              break;
            default:
          }
        }
        stackMapTableEntries
            .putByte(Frame.ITEM_OBJECT)
            .putShort(symbolTable.addConstantClass(typeDescriptor.toString()).index);
      }
    }
  }

  private void writeFrameType(final Object type) {
    if (type instanceof String) {
      stackMapTableEntries
          .putByte(Frame.ITEM_OBJECT)
          .putShort(symbolTable.addConstantClass((String) type).index);
    } else if (type instanceof Integer) {
      stackMapTableEntries.putByte(((Integer) type).intValue());
    } else {
      stackMapTableEntries.putByte(Frame.ITEM_UNINITIALIZED).putShort(((Label) type).position);
    }
  }

  // -----------------------------------------------------------------------------------------------
  // Utility methods
  // -----------------------------------------------------------------------------------------------

  /**
   * Returns the size of the method_info JVMS structure generated by this MethodWriter. Also add the
   * names of the attributes of this method in the constant pool.
   *
   * @return the size in bytes of the method_info JVMS structure.
   */
  final int computeMethodInfoSize() {
    // If this method_info must be copied from an existing one, the size computation is trivial.
    if (sourceOffset != 0) {
      // sourceLength excludes the first 6 bytes for access_flags, name_index and descriptor_index.
      return 6 + sourceLength;
    }
    // 2 bytes each for access_flags, name_index, descriptor_index and attributes_count.
    int size = 8;
    // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
    if (code.length > 0) {
      if (code.length > 65535) {
        throw new RuntimeException("Method code too large!");
      }
      symbolTable.addConstantUtf8("Code");
      // The Code attribute has 6 header bytes, plus 2, 2, 4 and 2 bytes respectively for max_stack,
      // max_locals, code_length and attributes_count, plus the bytecode and the exception table.
      size += 16 + code.length + Handler.getExceptionTableSize(firstHandler);
      if (stackMapTableEntries != null) {
        boolean useStackMapTable = symbolTable.getMajorVersion() >= Opcodes.V1_6;
        symbolTable.addConstantUtf8(useStackMapTable ? "StackMapTable" : "StackMap");
        // 6 header bytes and 2 bytes for number_of_entries.
        size += 8 + stackMapTableEntries.length;
      }
      if (lineNumberTable != null) {
        symbolTable.addConstantUtf8("LineNumberTable");
        // 6 header bytes and 2 bytes for line_number_table_length.
        size += 8 + lineNumberTable.length;
      }
      if (localVariableTable != null) {
        symbolTable.addConstantUtf8("LocalVariableTable");
        // 6 header bytes and 2 bytes for local_variable_table_length.
        size += 8 + localVariableTable.length;
      }
      if (localVariableTypeTable != null) {
        symbolTable.addConstantUtf8("LocalVariableTypeTable");
        // 6 header bytes and 2 bytes for local_variable_type_table_length.
        size += 8 + localVariableTypeTable.length;
      }
      if (lastCodeRuntimeVisibleTypeAnnotation != null) {
        size +=
            lastCodeRuntimeVisibleTypeAnnotation.computeAnnotationsSize(
                "RuntimeVisibleTypeAnnotations");
      }
      if (lastCodeRuntimeInvisibleTypeAnnotation != null) {
        size +=
            lastCodeRuntimeInvisibleTypeAnnotation.computeAnnotationsSize(
                "RuntimeInvisibleTypeAnnotations");
      }
      if (firstCodeAttribute != null) {
        size +=
            firstCodeAttribute.getAttributesSize(
                symbolTable, code.data, code.length, maxStack, maxLocals);
      }
    }
    if (numberOfExceptions > 0) {
      symbolTable.addConstantUtf8("Exceptions");
      size += 8 + 2 * numberOfExceptions;
    }
    boolean useSyntheticAttribute = symbolTable.getMajorVersion() < Opcodes.V1_5;
    if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0 && useSyntheticAttribute) {
      symbolTable.addConstantUtf8("Synthetic");
      size += 6;
    }
    if (signatureIndex != 0) {
      symbolTable.addConstantUtf8("Signature");
      size += 8;
    }
    if ((accessFlags & Opcodes.ACC_DEPRECATED) != 0) {
      symbolTable.addConstantUtf8("Deprecated");
      size += 6;
    }
    if (lastRuntimeVisibleAnnotation != null) {
      size += lastRuntimeVisibleAnnotation.computeAnnotationsSize("RuntimeVisibleAnnotations");
    }
    if (lastRuntimeInvisibleAnnotation != null) {
      size += lastRuntimeInvisibleAnnotation.computeAnnotationsSize("RuntimeInvisibleAnnotations");
    }
    if (lastRuntimeVisibleParameterAnnotations != null) {
      size +=
          AnnotationWriter.computeParameterAnnotationsSize(
              "RuntimeVisibleParameterAnnotations",
              lastRuntimeVisibleParameterAnnotations,
              visibleAnnotableParameterCount == 0
                  ? lastRuntimeVisibleParameterAnnotations.length
                  : visibleAnnotableParameterCount);
    }
    if (lastRuntimeInvisibleParameterAnnotations != null) {
      size +=
          AnnotationWriter.computeParameterAnnotationsSize(
              "RuntimeInvisibleParameterAnnotations",
              lastRuntimeInvisibleParameterAnnotations,
              invisibleAnnotableParameterCount == 0
                  ? lastRuntimeInvisibleParameterAnnotations.length
                  : invisibleAnnotableParameterCount);
    }
    if (lastRuntimeVisibleTypeAnnotation != null) {
      size +=
          lastRuntimeVisibleTypeAnnotation.computeAnnotationsSize("RuntimeVisibleTypeAnnotations");
    }
    if (lastRuntimeInvisibleTypeAnnotation != null) {
      size +=
          lastRuntimeInvisibleTypeAnnotation.computeAnnotationsSize(
              "RuntimeInvisibleTypeAnnotations");
    }
    if (defaultValue != null) {
      symbolTable.addConstantUtf8("AnnotationDefault");
      size += 6 + defaultValue.length;
    }
    if (parameters != null) {
      symbolTable.addConstantUtf8("MethodParameters");
      // 6 header bytes and 1 byte for parameters_count.
      size += 7 + parameters.length;
    }
    if (firstAttribute != null) {
      size += firstAttribute.getAttributesSize(symbolTable);
    }
    return size;
  }

  /**
   * Puts the content of the method_info JVMS structure generated by this MethodWriter into the
   * given ByteVector.
   *
   * @param output where the method_info structure must be put.
   */
  final void putMethodInfo(final ByteVector output) {
    boolean useSyntheticAttribute = symbolTable.getMajorVersion() < Opcodes.V1_5;
    int mask = useSyntheticAttribute ? Opcodes.ACC_SYNTHETIC : 0;
    output.putShort(accessFlags & ~mask).putShort(nameIndex).putShort(descriptorIndex);
    // If this method_info must be copied from an existing one, copy it now and return early.
    if (sourceOffset != 0) {
      output.putByteArray(getSource().b, sourceOffset, sourceLength);
      return;
    }
    // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
    int attributeCount = 0;
    if (code.length > 0) {
      ++attributeCount;
    }
    if (numberOfExceptions > 0) {
      ++attributeCount;
    }
    if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0 && useSyntheticAttribute) {
      ++attributeCount;
    }
    if (signatureIndex != 0) {
      ++attributeCount;
    }
    if ((accessFlags & Opcodes.ACC_DEPRECATED) != 0) {
      ++attributeCount;
    }
    if (lastRuntimeVisibleAnnotation != null) {
      ++attributeCount;
    }
    if (lastRuntimeInvisibleAnnotation != null) {
      ++attributeCount;
    }
    if (lastRuntimeVisibleParameterAnnotations != null) {
      ++attributeCount;
    }
    if (lastRuntimeInvisibleParameterAnnotations != null) {
      ++attributeCount;
    }
    if (lastRuntimeVisibleTypeAnnotation != null) {
      ++attributeCount;
    }
    if (lastRuntimeInvisibleTypeAnnotation != null) {
      ++attributeCount;
    }
    if (defaultValue != null) {
      ++attributeCount;
    }
    if (parameters != null) {
      ++attributeCount;
    }
    if (firstAttribute != null) {
      attributeCount += firstAttribute.getAttributeCount();
    }
    // For ease of reference, we use here the same attribute order as in Section 4.7 of the JVMS.
    output.putShort(attributeCount);
    if (code.length > 0) {
      // 2, 2, 4 and 2 bytes respectively for max_stack, max_locals, code_length and
      // attributes_count, plus the bytecode and the exception table.
      int size = 10 + code.length + Handler.getExceptionTableSize(firstHandler);
      int codeAttributeCount = 0;
      if (stackMapTableEntries != null) {
        // 6 header bytes and 2 bytes for number_of_entries.
        size += 8 + stackMapTableEntries.length;
        ++codeAttributeCount;
      }
      if (lineNumberTable != null) {
        // 6 header bytes and 2 bytes for line_number_table_length.
        size += 8 + lineNumberTable.length;
        ++codeAttributeCount;
      }
      if (localVariableTable != null) {
        // 6 header bytes and 2 bytes for local_variable_table_length.
        size += 8 + localVariableTable.length;
        ++codeAttributeCount;
      }
      if (localVariableTypeTable != null) {
        // 6 header bytes and 2 bytes for local_variable_type_table_length.
        size += 8 + localVariableTypeTable.length;
        ++codeAttributeCount;
      }
      if (lastCodeRuntimeVisibleTypeAnnotation != null) {
        size +=
            lastCodeRuntimeVisibleTypeAnnotation.computeAnnotationsSize(
                "RuntimeVisibleTypeAnnotations");
        ++codeAttributeCount;
      }
      if (lastCodeRuntimeInvisibleTypeAnnotation != null) {
        size +=
            lastCodeRuntimeInvisibleTypeAnnotation.computeAnnotationsSize(
                "RuntimeInvisibleTypeAnnotations");
        ++codeAttributeCount;
      }
      if (firstCodeAttribute != null) {
        size +=
            firstCodeAttribute.getAttributesSize(
                symbolTable, code.data, code.length, maxStack, maxLocals);
        codeAttributeCount += firstCodeAttribute.getAttributeCount();
      }
      output
          .putShort(symbolTable.addConstantUtf8("Code"))
          .putInt(size)
          .putShort(maxStack)
          .putShort(maxLocals)
          .putInt(code.length)
          .putByteArray(code.data, 0, code.length);
      Handler.putExceptionTable(firstHandler, output);
      output.putShort(codeAttributeCount);
      if (stackMapTableEntries != null) {
        boolean useStackMapTable = symbolTable.getMajorVersion() >= Opcodes.V1_6;
        output
            .putShort(symbolTable.addConstantUtf8(useStackMapTable ? "StackMapTable" : "StackMap"))
            .putInt(2 + stackMapTableEntries.length)
            .putShort(stackMapTableNumberOfEntries)
            .putByteArray(stackMapTableEntries.data, 0, stackMapTableEntries.length);
      }
      if (lineNumberTable != null) {
        output
            .putShort(symbolTable.addConstantUtf8("LineNumberTable"))
            .putInt(2 + lineNumberTable.length)
            .putShort(lineNumberTableLength)
            .putByteArray(lineNumberTable.data, 0, lineNumberTable.length);
      }
      if (localVariableTable != null) {
        output
            .putShort(symbolTable.addConstantUtf8("LocalVariableTable"))
            .putInt(2 + localVariableTable.length)
            .putShort(localVariableTableLength)
            .putByteArray(localVariableTable.data, 0, localVariableTable.length);
      }
      if (localVariableTypeTable != null) {
        output
            .putShort(symbolTable.addConstantUtf8("LocalVariableTypeTable"))
            .putInt(2 + localVariableTypeTable.length)
            .putShort(localVariableTypeTableLength)
            .putByteArray(localVariableTypeTable.data, 0, localVariableTypeTable.length);
      }
      if (lastCodeRuntimeVisibleTypeAnnotation != null) {
        lastCodeRuntimeVisibleTypeAnnotation.putAnnotations(
            symbolTable.addConstantUtf8("RuntimeVisibleTypeAnnotations"), output);
      }
      if (lastCodeRuntimeInvisibleTypeAnnotation != null) {
        lastCodeRuntimeInvisibleTypeAnnotation.putAnnotations(
            symbolTable.addConstantUtf8("RuntimeInvisibleTypeAnnotations"), output);
      }
      if (firstCodeAttribute != null) {
        firstCodeAttribute.putAttributes(
            symbolTable, code.data, code.length, maxStack, maxLocals, output);
      }
    }
    if (numberOfExceptions > 0) {
      output
          .putShort(symbolTable.addConstantUtf8("Exceptions"))
          .putInt(2 + 2 * numberOfExceptions)
          .putShort(numberOfExceptions);
      for (int exceptionIndex : exceptionIndexTable) {
        output.putShort(exceptionIndex);
      }
    }
    if ((accessFlags & Opcodes.ACC_SYNTHETIC) != 0 && useSyntheticAttribute) {
      output.putShort(symbolTable.addConstantUtf8("Synthetic")).putInt(0);
    }
    if (signatureIndex != 0) {
      output.putShort(symbolTable.addConstantUtf8("Signature")).putInt(2).putShort(signatureIndex);
    }
    if ((accessFlags & Opcodes.ACC_DEPRECATED) != 0) {
      output.putShort(symbolTable.addConstantUtf8("Deprecated")).putInt(0);
    }
    if (lastRuntimeVisibleAnnotation != null) {
      lastRuntimeVisibleAnnotation.putAnnotations(
          symbolTable.addConstantUtf8("RuntimeVisibleAnnotations"), output);
    }
    if (lastRuntimeInvisibleAnnotation != null) {
      lastRuntimeInvisibleAnnotation.putAnnotations(
          symbolTable.addConstantUtf8("RuntimeInvisibleAnnotations"), output);
    }
    if (lastRuntimeVisibleParameterAnnotations != null) {
      AnnotationWriter.putParameterAnnotations(
          symbolTable.addConstantUtf8("RuntimeVisibleParameterAnnotations"),
          lastRuntimeVisibleParameterAnnotations,
          visibleAnnotableParameterCount == 0
              ? lastRuntimeVisibleParameterAnnotations.length
              : visibleAnnotableParameterCount,
          output);
    }
    if (lastRuntimeInvisibleParameterAnnotations != null) {
      AnnotationWriter.putParameterAnnotations(
          symbolTable.addConstantUtf8("RuntimeInvisibleParameterAnnotations"),
          lastRuntimeInvisibleParameterAnnotations,
          invisibleAnnotableParameterCount == 0
              ? lastRuntimeInvisibleParameterAnnotations.length
              : invisibleAnnotableParameterCount,
          output);
    }
    if (lastRuntimeVisibleTypeAnnotation != null) {
      lastRuntimeVisibleTypeAnnotation.putAnnotations(
          symbolTable.addConstantUtf8("RuntimeVisibleTypeAnnotations"), output);
    }
    if (lastRuntimeInvisibleTypeAnnotation != null) {
      lastRuntimeInvisibleTypeAnnotation.putAnnotations(
          symbolTable.addConstantUtf8("RuntimeInvisibleTypeAnnotations"), output);
    }
    if (defaultValue != null) {
      output
          .putShort(symbolTable.addConstantUtf8("AnnotationDefault"))
          .putInt(defaultValue.length)
          .putByteArray(defaultValue.data, 0, defaultValue.length);
    }
    if (parameters != null) {
      output
          .putShort(symbolTable.addConstantUtf8("MethodParameters"))
          .putInt(1 + parameters.length)
          .putByte(parametersCount)
          .putByteArray(parameters.data, 0, parameters.length);
    }
    if (firstAttribute != null) {
      firstAttribute.putAttributes(symbolTable, output);
    }
  }
}
