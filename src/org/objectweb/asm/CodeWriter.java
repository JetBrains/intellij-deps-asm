/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (C) 2000 INRIA, France Telecom
 * Copyright (C) 2002 France Telecom
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

package org.objectweb.asm;

/**
 * A {@link CodeVisitor CodeVisitor} that generates Java bytecode instructions.
 * Each visit method of this class appends the bytecode corresponding to the
 * visited instruction to a byte vector, in the order these methods are called.
 */

public class CodeWriter implements CodeVisitor {

  /**
   * <tt>true</tt> if preconditions must be checked at runtime or not.
   */

  final static boolean CHECK = false;

  /**
   * Next code writer (see {@link ClassWriter#firstMethod firstMethod}).
   */

  CodeWriter next;

  /**
   * The class writer to which this method must be added.
   */

  private ClassWriter cw;

  /**
   * The constant pool item that contains the name of this method.
   */

  private Item name;

  /**
   * The constant pool item that contains the descriptor of this method.
   */

  private Item desc;

  /**
   * Access flags of this method.
   */

  private int access;

  /**
   * Maximum stack size of this method.
   */

  private int maxStack;

  /**
   * Maximum number of local variables for this method.
   */

  private int maxLocals;

  /**
   * The bytecode of this method.
   */

  private ByteVector code = new ByteVector();

  /**
   * Number of entries in the catch table of this method.
   */

  private int catchCount;

  /**
   * The catch table of this method.
   */

  private ByteVector catchTable;

  /**
   * Number of exceptions that can be thrown by this method.
   */

  private int exceptionCount;

  /**
   * The exceptions that can be thrown by this method. More
   * precisely, this array contains the indexes of the constant pool items
   * that contain the internal names of these exception classes.
   */

  private int[] exceptions;

  /**
   * Number of entries in the LocalVariableTable attribute.
   */

  private int localVarCount;

  /**
   * The LocalVariableTable attribute.
   */

  private ByteVector localVar;

  /**
   * Number of entries in the LineNumberTable attribute.
   */

  private int lineNumberCount;

  /**
   * The LineNumberTable attribute.
   */

  private ByteVector lineNumber;

  // --------------------------------------------------------------------------
  // Fields for the control flow graph analysis algorithm (used to compute the
  // maximum stack size). A control flow graph contains one node per "basic
  // block", and one edge per "jump" from one basic block to another. Each node
  // (i.e., each basic block) is represented by the Label object that
  // corresponds to the first instruction of this basic block. Each node also
  // stores the list of it successors in the graph, as a linked list of Edge
  // objects.
  // --------------------------------------------------------------------------

  /**
   * <tt>true</tt> if the maximum stack size and number of local variables must
   * be automatically computed.
   */

  private final boolean computeMaxs;

  /**
   * The (relative) stack size after the last visited instruction. This size is
   * relative to the beginning of the current basic block, i.e., the true stack
   * size after the last visited instruction is equal to the {@link
   * Label#beginStackSize beginStackSize} of the current basic block plus
   * <tt>stackSize</tt>.
   */

  private int stackSize;

  /**
   * The (relative) maximum stack size after the last visited instruction. This
   * size is relative to the beginning of the current basic block, i.e., the
   * true maximum stack size after the last visited instruction is equal to the
   * {@link Label#beginStackSize beginStackSize} of the current basic block plus
   * <tt>stackSize</tt>.
   */

  private int maxStackSize;

  /**
   * The current basic block. This block is the basic block to which the next
   * instruction to be visited must be added.
   */

  private Label currentBlock;

  /**
   * The basic block stack used by the control flow analysis algorithm. This
   * stack is represented by a linked list of {@link Label Label} objects,
   * linked to each other by their {@link Label#next} field. This stack must
   * not be confused with the JVM stack used to execute the JVM instructions!
   */

  private Label blockStack;

  /**
   * The stack size variation corresponding to each JVM instruction. This stack
   * variation is equal to the size of the values produced by an instruction,
   * minus the size of the values consumed by this instruction.
   */

  private final static int[] SIZE;

  // --------------------------------------------------------------------------
  // Fields to optimize the creation of {@link Edge Edge} objects by using a
  // pool of reusable objects. The (shared) pool is a linked list of Edge
  // objects, linked to each other by their {@link Edge#poolNext} field. Each
  // time a CodeWriter needs to allocate an Edge, it removes the first Edge
  // of the pool and adds it to a private list of Edge objects. After the end
  // of the control flow analysis algorithm, the Edge objects in the private
  // list of the CodeWriter are added back to the pool (by appending this
  // private list to the pool list; in order to do this in constant time, both
  // head and tail of the private list are stored in this CodeWriter).
  // --------------------------------------------------------------------------

  /**
   * The head of the list of {@link Edge Edge} objects used by this {@link
   * CodeWriter CodeWriter}. These objects, linked to each other by their
   * {@link Edge#poolNext} field, are added back to the shared pool at the
   * end of the control flow analysis algorithm.
   */

  private Edge head;

  /**
   * The tail of the list of {@link Edge Edge} objects used by this {@link
   * CodeWriter CodeWriter}. These objects, linked to each other by their
   * {@link Edge#poolNext} field, are added back to the shared pool at the
   * end of the control flow analysis algorithm.
   */

  private Edge tail;

  /**
   * The shared pool of {@link Edge Edge} objects. This pool is a linked list
   * of Edge objects, linked to each other by their {@link Edge#poolNext} field.
   */

  private static Edge pool;

  // --------------------------------------------------------------------------
  // Static initializer
  // --------------------------------------------------------------------------

  /**
   * Computes the stack size variation corresponding to each JVM instruction.
   */

  static {
    int i;
    int[] b = new int[202];
    String s =
      "EFFFFFFFFGGFFFGGFFFEEFGFGFEEEEEEEEEEEEEEEEEEEEDEDEDDDDDCDCDEEEEEEEEE" +
      "EEEEEEEEEEEBABABBBBDCFFFGGGEDCDCDCDCDCDCDCDCDCDCEEEEDDDDDDDCDCDCEFEF" +
      "DDEEFFDEDEEEBDDBBDDDDDDCCCCCCCCEFEDDDCDCDEEEEEEEEEEFEEEEEEDDEEDDEE";
    for (i = 0; i < b.length; ++i) {
      b[i] = s.charAt(i) - 'E';
    }
    SIZE = b;

    /* code to generate the above string

    int NA = 0; // not applicable (unused opcode or variable size opcode)

    b = new int[] {
      0,  //NOP,             // visitInsn
      1,  //ACONST_NULL,     // -
      1,  //ICONST_M1,       // -
      1,  //ICONST_0,        // -
      1,  //ICONST_1,        // -
      1,  //ICONST_2,        // -
      1,  //ICONST_3,        // -
      1,  //ICONST_4,        // -
      1,  //ICONST_5,        // -
      2,  //LCONST_0,        // -
      2,  //LCONST_1,        // -
      1,  //FCONST_0,        // -
      1,  //FCONST_1,        // -
      1,  //FCONST_2,        // -
      2,  //DCONST_0,        // -
      2,  //DCONST_1,        // -
      1,  //BIPUSH,          // visitIntInsn
      1,  //SIPUSH,          // -
      1,  //LDC,             // visitLdcInsn
      NA, //LDC_W,           // -
      NA, //LDC2_W,          // -
      1,  //ILOAD,           // visitVarInsn
      2,  //LLOAD,           // -
      1,  //FLOAD,           // -
      2,  //DLOAD,           // -
      1,  //ALOAD,           // -
      NA, //ILOAD_0,         // -
      NA, //ILOAD_1,         // -
      NA, //ILOAD_2,         // -
      NA, //ILOAD_3,         // -
      NA, //LLOAD_0,         // -
      NA, //LLOAD_1,         // -
      NA, //LLOAD_2,         // -
      NA, //LLOAD_3,         // -
      NA, //FLOAD_0,         // -
      NA, //FLOAD_1,         // -
      NA, //FLOAD_2,         // -
      NA, //FLOAD_3,         // -
      NA, //DLOAD_0,         // -
      NA, //DLOAD_1,         // -
      NA, //DLOAD_2,         // -
      NA, //DLOAD_3,         // -
      NA, //ALOAD_0,         // -
      NA, //ALOAD_1,         // -
      NA, //ALOAD_2,         // -
      NA, //ALOAD_3,         // -
      -1, //IALOAD,          // visitInsn
      0,  //LALOAD,          // -
      -1, //FALOAD,          // -
      0,  //DALOAD,          // -
      -1, //AALOAD,          // -
      -1, //BALOAD,          // -
      -1, //CALOAD,          // -
      -1, //SALOAD,          // -
      -1, //ISTORE,          // visitVarInsn
      -2, //LSTORE,          // -
      -1, //FSTORE,          // -
      -2, //DSTORE,          // -
      -1, //ASTORE,          // -
      NA, //ISTORE_0,        // -
      NA, //ISTORE_1,        // -
      NA, //ISTORE_2,        // -
      NA, //ISTORE_3,        // -
      NA, //LSTORE_0,        // -
      NA, //LSTORE_1,        // -
      NA, //LSTORE_2,        // -
      NA, //LSTORE_3,        // -
      NA, //FSTORE_0,        // -
      NA, //FSTORE_1,        // -
      NA, //FSTORE_2,        // -
      NA, //FSTORE_3,        // -
      NA, //DSTORE_0,        // -
      NA, //DSTORE_1,        // -
      NA, //DSTORE_2,        // -
      NA, //DSTORE_3,        // -
      NA, //ASTORE_0,        // -
      NA, //ASTORE_1,        // -
      NA, //ASTORE_2,        // -
      NA, //ASTORE_3,        // -
      -3, //IASTORE,         // visitInsn
      -4, //LASTORE,         // -
      -3, //FASTORE,         // -
      -4, //DASTORE,         // -
      -3, //AASTORE,         // -
      -3, //BASTORE,         // -
      -3, //CASTORE,         // -
      -3, //SASTORE,         // -
      -1, //POP,             // -
      -2, //POP2,            // -
      1,  //DUP,             // -
      1,  //DUP_X1,          // -
      1,  //DUP_X2,          // -
      2,  //DUP2,            // -
      2,  //DUP2_X1,         // -
      2,  //DUP2_X2,         // -
      0,  //SWAP,            // -
      -1, //IADD,            // -
      -2, //LADD,            // -
      -1, //FADD,            // -
      -2, //DADD,            // -
      -1, //ISUB,            // -
      -2, //LSUB,            // -
      -1, //FSUB,            // -
      -2, //DSUB,            // -
      -1, //IMUL,            // -
      -2, //LMUL,            // -
      -1, //FMUL,            // -
      -2, //DMUL,            // -
      -1, //IDIV,            // -
      -2, //LDIV,            // -
      -1, //FDIV,            // -
      -2, //DDIV,            // -
      -1, //IREM,            // -
      -2, //LREM,            // -
      -1, //FREM,            // -
      -2, //DREM,            // -
      0,  //INEG,            // -
      0,  //LNEG,            // -
      0,  //FNEG,            // -
      0,  //DNEG,            // -
      -1, //ISHL,            // -
      -1, //LSHL,            // -
      -1, //ISHR,            // -
      -1, //LSHR,            // -
      -1, //IUSHR,           // -
      -1, //LUSHR,           // -
      -1, //IAND,            // -
      -2, //LAND,            // -
      -1, //IOR,             // -
      -2, //LOR,             // -
      -1, //IXOR,            // -
      -2, //LXOR,            // -
      0,  //IINC,            // visitIincInsn
      1,  //I2L,             // visitInsn
      0,  //I2F,             // -
      1,  //I2D,             // -
      -1, //L2I,             // -
      -1, //L2F,             // -
      0,  //L2D,             // -
      0,  //F2I,             // -
      1,  //F2L,             // -
      1,  //F2D,             // -
      -1, //D2I,             // -
      0,  //D2L,             // -
      -1, //D2F,             // -
      0,  //I2B,             // -
      0,  //I2C,             // -
      0,  //I2S,             // -
      -3, //LCMP,            // -
      -1, //FCMPL,           // -
      -1, //FCMPG,           // -
      -3, //DCMPL,           // -
      -3, //DCMPG,           // -
      -1, //IFEQ,            // visitJumpInsn
      -1, //IFNE,            // -
      -1, //IFLT,            // -
      -1, //IFGE,            // -
      -1, //IFGT,            // -
      -1, //IFLE,            // -
      -2, //IF_ICMPEQ,       // -
      -2, //IF_ICMPNE,       // -
      -2, //IF_ICMPLT,       // -
      -2, //IF_ICMPGE,       // -
      -2, //IF_ICMPGT,       // -
      -2, //IF_ICMPLE,       // -
      -2, //IF_ACMPEQ,       // -
      -2, //IF_ACMPNE,       // -
      0,  //GOTO,            // -
      1,  //JSR,             // -
      0,  //RET,             // visitVarInsn
      -1, //TABLESWITCH,     // visiTableSwitchInsn
      -1, //LOOKUPSWITCH,    // visitLookupSwitch
      -1, //IRETURN,         // visitInsn
      -2, //LRETURN,         // -
      -1, //FRETURN,         // -
      -2, //DRETURN,         // -
      -1, //ARETURN,         // -
      0,  //RETURN,          // -
      NA, //GETSTATIC,       // visitFieldInsn
      NA, //PUTSTATIC,       // -
      NA, //GETFIELD,        // -
      NA, //PUTFIELD,        // -
      NA, //INVOKEVIRTUAL,   // visitMethodInsn
      NA, //INVOKESPECIAL,   // -
      NA, //INVOKESTATIC,    // -
      NA, //INVOKEINTERFACE, // -
      NA, //UNUSED,          // NOT VISITED
      1,  //NEW,             // visitTypeInsn
      0,  //NEWARRAY,        // visitIntInsn
      0,  //ANEWARRAY,       // visitTypeInsn
      0,  //ARRAYLENGTH,     // visitInsn
      NA, //ATHROW,          // -
      0,  //CHECKCAST,       // visitTypeInsn
      0,  //INSTANCEOF,      // -
      -1, //MONITORENTER,    // visitInsn
      -1, //MONITOREXIT,     // -
      NA, //WIDE,            // NOT VISITED
      NA, //MULTIANEWARRAY,  // visitMultiANewArrayInsn
      -1, //IFNULL,          // visitJumpInsn
      -1, //IFNONNULL,       // -
      NA, //GOTO_W,          // -
      NA, //JSR_W,           // -
    };
    for (i = 0; i < b.length; ++i) {
      System.err.print((char)('E' + b[i]));
    }
    System.err.println();
    */
  }

  // --------------------------------------------------------------------------
  // Constructor
  // --------------------------------------------------------------------------

  /**
   * Constructs a CodeWriter.
   *
   * @param cw the class writer in which the method must be added.
   * @param computeMaxs <tt>true</tt> if the maximum stack size and number of
   *      local variables must be automatically computed.
   */

  protected CodeWriter (final ClassWriter cw, final boolean computeMaxs) {
    if (cw.firstMethod == null) {
      cw.firstMethod = this;
      cw.lastMethod = this;
    } else {
      cw.lastMethod.next = this;
      cw.lastMethod = this;
    }
    this.cw = cw;
    this.computeMaxs = computeMaxs;
    if (computeMaxs) {
      // pushes the first block onto the stack of blocks to be visited
      currentBlock = new Label();
      currentBlock.pushed = true;
      blockStack = currentBlock;
    }
  }

  /**
   * Initializes this CodeWriter to define the bytecode of the specified method.
   *
   * @param access the method's access flags (see {@link Constants}).
   * @param name the method's name.
   * @param desc the method's descriptor (see {@link Type Type}).
   * @param exceptions the internal names of the method's exceptions. May be
   *      <tt>null</tt>.
   */

  protected void init (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions)
  {
    this.access = access;
    this.name = cw.newUTF8(name);
    this.desc = cw.newUTF8(desc);
    if (exceptions != null && exceptions.length > 0) {
      exceptionCount = exceptions.length;
      this.exceptions = new int[exceptionCount];
      for (int i = 0; i < exceptionCount; ++i) {
        this.exceptions[i] = cw.newClass(exceptions[i]).index;
      }
    }
    if (computeMaxs) {
      // updates maxLocals
      int size = getArgumentsAndReturnSizes(desc) >> 2;
      if ((access & Constants.ACC_STATIC) != 0) {
        --size;
      }
      if (size > maxLocals) {
        maxLocals = size;
      }
    }
  }

  // --------------------------------------------------------------------------
  // Implementation of the CodeVisitor interface
  // --------------------------------------------------------------------------

  public void visitInsn (final int opcode) {
    if (computeMaxs) {
      // updates current and max stack sizes
      int size = stackSize + SIZE[opcode];
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
      // if opcode == ATHROW or xRETURN, ends current block (no successor)
      if ((opcode >= Constants.IRETURN && opcode <= Constants.RETURN) ||
          opcode == Constants.ATHROW)
      {
        if (currentBlock != null) {
          currentBlock.maxStackSize = maxStackSize;
          currentBlock = null;
        }
      }
    }
    // adds the instruction to the bytecode of the method
    code.put1(opcode);
  }

  public void visitIntInsn (final int opcode, final int operand) {
    if (computeMaxs && opcode != Constants.NEWARRAY) {
      // updates current and max stack sizes only if opcode == NEWARRAY
      // (stack size variation = 0 for BIPUSH or SIPUSH)
      int size = stackSize + 1;
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
    }
    // adds the instruction to the bytecode of the method
    if (opcode == Constants.SIPUSH) {
      code.put12(opcode, operand);
    } else { // BIPUSH or NEWARRAY
      code.put11(opcode, operand);
    }
  }

  public void visitVarInsn (final int opcode, final int var) {
    if (computeMaxs) {
      // updates current and max stack sizes
      if (opcode == Constants.RET) {
        // no stack change, but end of current block (no successor)
        if (currentBlock != null) {
          currentBlock.maxStackSize = maxStackSize;
          currentBlock = null;
        }
      } else { // xLOAD or xSTORE
        int size = stackSize + SIZE[opcode];
        if (size > maxStackSize) {
          maxStackSize = size;
        }
        stackSize = size;
      }
      // updates max locals
      int n;
      if (opcode == Constants.LLOAD || opcode == Constants.DLOAD ||
          opcode == Constants.LSTORE || opcode == Constants.DSTORE)
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
    if (var < 4 && opcode != Constants.RET) {
      int opt;
      if (opcode < Constants.ISTORE) {
        opt = 26 /*ILOAD_0*/ + ((opcode - Constants.ILOAD) << 2) + var;
      } else {
        opt = 59 /*ISTORE_0*/ + ((opcode - Constants.ISTORE) << 2) + var;
      }
      code.put1(opt);
    } else if (var >= 256) {
      code.put1(196 /*WIDE*/).put12(opcode, var);
    } else {
      code.put11(opcode, var);
    }
  }

  public void visitTypeInsn (final int opcode, final String desc) {
    if (computeMaxs && opcode == Constants.NEW) {
      // updates current and max stack sizes only if opcode == NEW
      // (stack size variation = 0 for ANEWARRAY, CHECKCAST, INSTANCEOF)
      int size = stackSize + 1;
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
    }
    // adds the instruction to the bytecode of the method
    code.put12(opcode, cw.newClass(desc).index);
  }

  public void visitFieldInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    if (computeMaxs) {
      int size;
      // computes the stack size variation
      char c = desc.charAt(0);
      switch (opcode) {
        case Constants.GETSTATIC:
          size = stackSize + (c == 'D' || c == 'J' ? 2 : 1);
          break;
        case Constants.PUTSTATIC:
          size = stackSize + (c == 'D' || c == 'J' ? -2 : -1);
          break;
        case Constants.GETFIELD:
          size = stackSize + (c == 'D' || c == 'J' ? 1 : 0);
          break;
        //case Constants.PUTFIELD:
        default:
          size = stackSize + (c == 'D' || c == 'J' ? -3 : -2);
          break;
      }
      // updates current and max stack sizes
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
    }
    // adds the instruction to the bytecode of the method
    code.put12(opcode, cw.newField(owner, name, desc).index);
  }

  public void visitMethodInsn (
    final int opcode,
    final String owner,
    final String name,
    final String desc)
  {
    Item i;
    if (opcode == Constants.INVOKEINTERFACE) {
      i = cw.newItfMethod(owner, name, desc);
    } else {
      i = cw.newMethod(owner, name, desc);
    }
    int argSize = i.intVal;
    if (computeMaxs) {
      // computes the stack size variation. In order not to recompute several
      // times this variation for the same Item, we use the intVal field of
      // this item to store this variation, once it has been computed. More
      // precisely this intVal field stores the sizes of the arguments and of
      // the return value corresponding to desc.
      if (argSize == 0) {
        // the above sizes have not been computed yet, so we compute them...
        argSize = getArgumentsAndReturnSizes(desc);
        // ... and we save them in order not to recompute them in the future
        i.intVal = argSize;
      }
      int size;
      if (opcode == Constants.INVOKESTATIC) {
        size = stackSize - (argSize >> 2) + (argSize & 0x03) + 1;
      } else {
        size = stackSize - (argSize >> 2) + (argSize & 0x03);
      }
      // updates current and max stack sizes
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
    }
    // adds the instruction to the bytecode of the method
    if (opcode == Constants.INVOKEINTERFACE) {
      if (!computeMaxs) {
        if (argSize == 0) {
          argSize = getArgumentsAndReturnSizes(desc);
          i.intVal = argSize;
        }
      }
      code.put12(Constants.INVOKEINTERFACE, i.index).put11(argSize >> 2, 0);
    } else {
      code.put12(opcode, i.index);
    }
  }

  public void visitJumpInsn (final int opcode, final Label label) {
    if (CHECK) {
      if (label.owner == null) {
        label.owner = this;
      } else if (label.owner != this) {
        throw new IllegalArgumentException();
      }
    }
    if (computeMaxs) {
      if (opcode == Constants.GOTO) {
        // no stack change, but end of current block (with one new successor)
        if (currentBlock != null) {
          currentBlock.maxStackSize = maxStackSize;
          addSuccessor(stackSize, label);
          currentBlock = null;
        }
      } else if (opcode == Constants.JSR) {
        if (currentBlock != null) {
          addSuccessor(stackSize + 1, label);
        }
      } else {
        // updates current stack size (max stack size unchanged because stack
        // size variation always negative in this case)
        stackSize += SIZE[opcode];
        if (currentBlock != null) {
          addSuccessor(stackSize, label);
        }
      }
    }
    // adds the instruction to the bytecode of the method
    code.put1(opcode);
    label.put(
      this,
      code,
      code.length - 1,
      false);
  }

  public void visitLabel (final Label label) {
    if (CHECK) {
      if (label.owner == null) {
        label.owner = this;
      } else if (label.owner != this) {
        throw new IllegalArgumentException();
      }
    }
    if (computeMaxs) {
      if (currentBlock != null) {
        // ends current block (with one new successor)
        currentBlock.maxStackSize = maxStackSize;
        addSuccessor(stackSize, label);
      }
      // begins a new current block,
      // resets the relative current and max stack sizes
      currentBlock = label;
      stackSize = 0;
      maxStackSize = 0;
    }
    // resolves previous forward references to label, if any
    label.resolve(this, code.length, code.data);
  }

  public void visitLdcInsn (final Object cst) {
    Item i = cw.newCst(cst);
    if (computeMaxs) {
      int size;
      // computes the stack size variation
      if (i.type == ClassWriter.LONG || i.type == ClassWriter.DOUBLE) {
        size = stackSize + 2;
      } else {
        size = stackSize + 1;
      }
      // updates current and max stack sizes
      if (size > maxStackSize) {
        maxStackSize = size;
      }
      stackSize = size;
    }
    // adds the instruction to the bytecode of the method
    int index = i.index;
    if (i.type == ClassWriter.LONG || i.type == ClassWriter.DOUBLE) {
      code.put12(20 /*LDC2_W*/, index);
    } else if (index >= 256) {
      code.put12(19 /*LDC_W*/, index);
    } else {
      code.put11(Constants.LDC, index);
    }
  }

  public void visitIincInsn (final int var, final int increment) {
    if (computeMaxs) {
      // updates max locals only (no stack change)
      int n = var + 1;
      if (n > maxLocals) {
        maxLocals = n;
      }
    }
    // adds the instruction to the bytecode of the method
    if ((var > 255) || (increment > 127) || (increment < -128)) {
      code.put1(196 /*WIDE*/).put12(Constants.IINC, var).put2(increment);
    } else {
      code.put1(Constants.IINC).put11(var, increment);
    }
  }

  public void visitTableSwitchInsn (
    final int min,
    final int max,
    final Label dflt,
    final Label labels[])
  {
    if (computeMaxs) {
      // updates current stack size (max stack size unchanged)
      --stackSize;
      // ends current block (with many new successors)
      if (currentBlock != null) {
        currentBlock.maxStackSize = maxStackSize;
        addSuccessor(stackSize, dflt);
        for (int i = 0; i < labels.length; ++i) {
          addSuccessor(stackSize, labels[i]);
        }
        currentBlock = null;
      }
    }
    // adds the instruction to the bytecode of the method
    int source = code.length;
    code.put1(Constants.TABLESWITCH);
    while (code.length % 4 != 0) {
      code.put1(0);
    }
    dflt.put(this, code, source, true);
    code.put4(min).put4(max);
    for (int i = 0; i < labels.length; ++i) {
      labels[i].put(this, code, source, true);
    }
  }

  public void visitLookupSwitchInsn (
    final Label dflt,
    final int keys[],
    final Label labels[])
  {
    if (computeMaxs) {
      // updates current stack size (max stack size unchanged)
      --stackSize;
      // ends current block (with many new successors)
      if (currentBlock != null) {
        currentBlock.maxStackSize = maxStackSize;
        addSuccessor(stackSize, dflt);
        for (int i = 0; i < labels.length; ++i) {
          addSuccessor(stackSize, labels[i]);
        }
        currentBlock = null;
      }
    }
    // adds the instruction to the bytecode of the method
    int source = code.length;
    code.put1(Constants.LOOKUPSWITCH);
    while (code.length % 4 != 0) {
      code.put1(0);
    }
    dflt.put(this, code, source, true);
    code.put4(labels.length);
    for (int i = 0; i < labels.length; ++i) {
      code.put4(keys[i]);
      labels[i].put(this, code, source, true);
    }
  }

  public void visitMultiANewArrayInsn (final String desc, final int dims) {
    if (computeMaxs) {
      // updates current stack size (max stack size unchanged because stack
      // size variation always negative or null)
      stackSize += 1 - dims;
    }
    // adds the instruction to the bytecode of the method
    Item classItem = cw.newClass(desc);
    code.put12(Constants.MULTIANEWARRAY, classItem.index).put1(dims);
  }

  public void visitTryCatchBlock (
    final Label start,
    final Label end,
    final Label handler,
    final String type)
  {
    if (CHECK) {
      if (start.owner != this || end.owner != this || handler.owner != this) {
        throw new IllegalArgumentException();
      }
      if (!start.resolved || !end.resolved || !handler.resolved) {
        throw new IllegalArgumentException();
      }
    }
    if (computeMaxs) {
      // pushes handler block onto the stack of blocks to be visited
      if (!handler.pushed) {
        handler.beginStackSize = 1;
        handler.pushed = true;
        handler.next = blockStack;
        blockStack = handler;
      }
    }
    ++catchCount;
    if (catchTable == null) {
      catchTable = new ByteVector();
    }
    catchTable.put2(start.position);
    catchTable.put2(end.position);
    catchTable.put2(handler.position);
    catchTable.put2(type != null ? cw.newClass(type).index : 0);
  }

  public void visitMaxs (final int maxStack, final int maxLocals) {
    if (computeMaxs) {
      // true (non relative) max stack size
      int max = 0;
      // control flow analysis algorithm: while the block stack is not empty,
      // pop a block from this stack, update the max stack size, compute
      // the true (non relative) begin stack size of the successors of this
      // block, and push these successors onto the stack (unless they have
      // already been pushed onto the stack). Note: by hypothesis, the {@link
      // Label#beginStackSize} of the blocks in the block stack are the true
      // (non relative) beginning stack sizes of these blocks.
      Label stack = blockStack;
      while (stack != null) {
        // pops a block from the stack
        Label l = stack;
        stack = stack.next;
        // computes the true (non relative) max stack size of this block
        int start = l.beginStackSize;
        int blockMax = start + l.maxStackSize;
        // updates the global max stack size
        if (blockMax > max) {
          max = blockMax;
        }
        // analyses the successors of the block
        Edge b = l.successors;
        while (b != null) {
          l = b.successor;
          // if this successor has not already been pushed onto the stack...
          if (!l.pushed) {
            // computes the true beginning stack size of this successor block
            l.beginStackSize = start + b.stackSize;
            // pushes this successor onto the stack
            l.pushed = true;
            l.next = stack;
            stack = l;
          }
          b = b.next;
        }
      }
      this.maxStack = max;
      // releases all the Edge objects used by this CodeWriter
      synchronized (SIZE) {
        // appends the [head ... tail] list at the beginning of the pool list
        if (tail != null) {
          tail.poolNext = pool;
          pool = head;
        }
      }
    } else {
      this.maxStack = maxStack;
      this.maxLocals = maxLocals;
    }
  }

  public void visitLocalVariable (
    final String name,
    final String desc,
    final Label start,
    final Label end,
    final int index)
  {
    if (CHECK) {
      if (start.owner != this || !start.resolved) {
        throw new IllegalArgumentException();
      }
      if (end.owner != this || !end.resolved) {
        throw new IllegalArgumentException();
      }
    }
    if (localVar == null) {
      cw.newUTF8("LocalVariableTable");
      localVar = new ByteVector();
    }
    ++localVarCount;
    localVar.put2(start.position);
    localVar.put2(end.position - start.position);
    localVar.put2(cw.newUTF8(name).index);
    localVar.put2(cw.newUTF8(desc).index);
    localVar.put2(index);
  }

  public void visitLineNumber (final int line, final Label start) {
    if (CHECK) {
      if (start.owner != this || !start.resolved) {
        throw new IllegalArgumentException();
      }
    }
    if (lineNumber == null) {
      cw.newUTF8("LineNumberTable");
      lineNumber = new ByteVector();
    }
    ++lineNumberCount;
    lineNumber.put2(start.position);
    lineNumber.put2(line);
  }

  // --------------------------------------------------------------------------
  // Utility methods: control flow analysis algorithm
  // --------------------------------------------------------------------------

  /**
   * Computes the size of the arguments and of the return value of a method.
   *
   * @param desc the descriptor of a method.
   * @return the size of the arguments of the method (plus one for the implicit
   *      this argument), argSize, and the size of its return value, retSize,
   *      packed into a single int i = <tt>(argSize << 2) | retSize</tt>
   *      (argSize is therefore equal to <tt>i >> 2</tt>, and retSize to
   *      <tt>i & 0x03</tt>).
   */

  private static int getArgumentsAndReturnSizes (final String desc) {
    int n = 1;
    int c = 1;
    while (true) {
      char car = desc.charAt(c++);
      if (car == ')') {
        car = desc.charAt(c);
        return n << 2 | (car == 'V' ? 0 : (car == 'D' || car == 'J' ? 2 : 1));
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
   * @param stackSize the current (relative) stack size in the current block.
   * @param successor the successor block to be added to the current block.
   */

  private void addSuccessor (final int stackSize, final Label successor) {
    Edge b;
    // creates a new Edge object or reuses one from the shared pool
    synchronized (SIZE) {
      if (pool == null) {
        b = new Edge();
      } else {
        b = pool;
        // removes b from the pool
        pool = pool.poolNext;
      }
    }
    // adds the previous Edge to the list of Edges used by this CodeWriter
    if (tail == null) {
      tail = b;
    }
    b.poolNext = head;
    head = b;
    // initializes the previous Edge object...
    b.stackSize = stackSize;
    b.successor = successor;
    // ...and adds it to the successor list of the currentBlock block
    b.next = currentBlock.successors;
    currentBlock.successors = b;
  }

  // --------------------------------------------------------------------------
  // Utility methods: dump bytecode array
  // --------------------------------------------------------------------------

  /**
   * Returns the size of the bytecode of this method.
   *
   * @return the size of the bytecode of this method.
   */

  final int getSize () {
    int size = 8;
    if (code.length > 0) {
      cw.newUTF8("Code");
      size += 18 + code.length + 8 * catchCount;
      if (localVar != null) {
        size += 8 + localVar.length;
      }
      if (lineNumber != null) {
        size += 8 + lineNumber.length;
      }
    }
    if (exceptionCount > 0) {
      cw.newUTF8("Exceptions");
      size += 8 + 2 * exceptionCount;
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      cw.newUTF8("Synthetic");
      size += 6;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      cw.newUTF8("Deprecated");
      size += 6;
    }
    return size;
  }

  /**
   * Puts the bytecode of this method in the given byte vector.
   *
   * @param out the byte vector into which the bytecode of this method must be
   *      copied.
   */

  final void put (final ByteVector out) {
    out.put2(access).put2(name.index).put2(desc.index);
    int attributeCount = 0;
    if (code.length > 0) {
      ++attributeCount;
    }
    if (exceptionCount > 0) {
      ++attributeCount;
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      ++attributeCount;
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      ++attributeCount;
    }
    out.put2(attributeCount);
    if (code.length > 0) {
      int size = 12 + code.length + 8 * catchCount;
      if (localVar != null) {
        size += 8 + localVar.length;
      }
      if (lineNumber != null) {
        size += 8 + lineNumber.length;
      }
      out.put2(cw.newUTF8("Code").index).put4(size);
      out.put2(maxStack).put2(maxLocals);
      out.put4(code.length).putByteArray(code.data, code.length);
      out.put2(catchCount);
      if (catchCount > 0) {
        out.putByteArray(catchTable.data, catchTable.length);
      }
      attributeCount = 0;
      if (localVar != null) {
        ++attributeCount;
      }
      if (lineNumber != null) {
        ++attributeCount;
      }
      out.put2(attributeCount);
      if (localVar != null) {
        out.put2(cw.newUTF8("LocalVariableTable").index);
        out.put4(localVar.length + 2).put2(localVarCount);
        out.putByteArray(localVar.data, localVar.length);
      }
      if (lineNumber != null) {
        out.put2(cw.newUTF8("LineNumberTable").index);
        out.put4(lineNumber.length + 2).put2(lineNumberCount);
        out.putByteArray(lineNumber.data, lineNumber.length);
      }
    }
    if (exceptionCount > 0) {
      out.put2(cw.newUTF8("Exceptions").index).put4(2 * exceptionCount + 2);
      out.put2(exceptionCount);
      for (int i = 0; i < exceptionCount; ++i) {
        out.put2(exceptions[i]);
      }
    }
    if ((access & Constants.ACC_SYNTHETIC) != 0) {
      out.put2(cw.newUTF8("Synthetic").index).put4(0);
    }
    if ((access & Constants.ACC_DEPRECATED) != 0) {
      out.put2(cw.newUTF8("Deprecated").index).put4(0);
    }
  }

  /**
   * Returns the current size of the bytecode of this method. This size just
   * includes the size of the bytecode instructions: it does not include the
   * size of the Exceptions, LocalVariableTable, LineNumberTable, Synthetic
   * and Deprecated attributes, if present.
   *
   * @return the current size of the bytecode of this method.
   */

  protected int getCodeSize () {
    return code.length;
  }

  /**
   * Returns the current bytecode of this method. This bytecode only contains
   * the instructions: it does not include the Exceptions, LocalVariableTable,
   * LineNumberTable, Synthetic and Deprecated attributes, if present.
   *
   * @return the current bytecode of this method. The bytecode is contained
   *      between the index 0 (inclusive) and the index {@link #getCodeSize
   *      getCodeSize} (exclusive).
   */

  protected byte[] getCode () {
    return code.data;
  }
}
