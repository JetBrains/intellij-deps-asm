/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
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

package org.objectweb.asm.tree.analysis;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.VarInsnNode;

/**
 * A semantic bytecode analyzer.
 * 
 * @author Eric Bruneton
 */

public class Analyzer implements Constants {

  private Interpreter interpreter;

  private int n;
  
  private IntMap indexes;

  private List[] handlers;
  
  private Frame[] frames;
  
  private Subroutine[] subroutines;
  
  private boolean[] queued;

  private int[] queue;

  private int top;

  /**
   * Constructs a new {@link Analyzer}.
   * 
   * @param interpreter the interpreter to be used to symbolically interpret 
   *      the bytecode instructions.
   */
  
  public Analyzer (final Interpreter interpreter) {
    this.interpreter = interpreter;
  }
  
  /**
   * Analyzes the given method.
   * 
   * @param c the class to which the method belongs.
   * @param m the method to be analyzed.
   * @return the symbolic state of the execution stack frame at each bytecode
   *     instruction of the method. The size of the returned array is equal to 
   *     the number of instructions (and labels) of the method. A given frame is
   *     <tt>null</tt> if and only if the corresponding instruction cannot be
   *     reached (dead code).  
   */
  
  public Frame[] analyze (final ClassNode c, final MethodNode m) {
    n = m.instructions.size();
    indexes = new IntMap(2*n);
    handlers = new List[n];
    frames = new Frame[n];
    subroutines = new Subroutine[n];
    queued = new boolean[n];
    queue = new int[n];    
    top = 0; 
    
    // computes instruction indexes
    for (int i = 0; i < n; ++i) {
      indexes.put(m.instructions.get(i), i);
    }

    // computes exception handlers for each instruction
    for (int i = 0; i < m.tryCatchBlocks.size(); ++i) {
      TryCatchBlockNode tcb = (TryCatchBlockNode)m.tryCatchBlocks.get(i);
      int begin = indexes.get(tcb.start);
      int end = indexes.get(tcb.end);
      for (int j = begin; j < end; ++j) {
        List insnHandlers = handlers[j];
        if (insnHandlers == null) {
          insnHandlers = new ArrayList();
          handlers[j] = insnHandlers;
        }
        insnHandlers.add(tcb);
      }
    }

    // initializes the data structures for the control flow analysis algorithm
    Frame current = new Frame(m.maxLocals, m.maxStack);
    Frame handler = new Frame(m.maxLocals, m.maxStack);
    Type[] args = Type.getArgumentTypes(m.desc);
    int local = 0;
    if ((m.access & ACC_STATIC) == 0) {
      Type ctype = Type.getType("L" + c.name + ";");
      current.setLocal(local++, interpreter.newValue(ctype));
    }
    for (int i = 0; i < args.length; ++i) {
      current.setLocal(local++, interpreter.newValue(args[i]));
      if (args[i].getSize() == 2) {
        current.setLocal(local++, interpreter.newValue(null));
      }
    }
    while (local < m.maxLocals) {
      current.setLocal(local++, interpreter.newValue(null));
    }
    merge(0, current, null);

    // control flow analysis
    while (top > 0) {
      int insn = queue[--top];
      Frame f = frames[insn];
      Subroutine subroutine = subroutines[insn];
      queued[insn] = false;

      try {
        Object o = m.instructions.get(insn);
        
        if (o instanceof Label) {
          merge(insn + 1, f, subroutine);
        } else {
          AbstractInsnNode insnNode = (AbstractInsnNode)o;
          int insnOpcode = insnNode.getOpcode();
          
          execute(insnNode, current.init(f));
          subroutine = subroutine == null ? null : subroutine.copy();
          
          if (insnNode instanceof JumpInsnNode) {
            JumpInsnNode j = (JumpInsnNode)insnNode;
            if (insnOpcode != GOTO && insnOpcode != JSR) {
              merge(insn + 1, current, subroutine);
            }
            if (insnOpcode == JSR) {
              subroutine = new Subroutine(j.label, m.maxLocals, j);
            }
            merge(indexes.get(j.label), current, subroutine);
          } else if (insnNode instanceof LookupSwitchInsnNode) {
            LookupSwitchInsnNode lsi = (LookupSwitchInsnNode)insnNode;
            merge(indexes.get(lsi.dflt), current, subroutine);
            for (int j = 0; j < lsi.labels.size(); ++j) {
              Label label = (Label)lsi.labels.get(j);
              merge(indexes.get(label), current, subroutine);
            }
          } else if (insnNode instanceof TableSwitchInsnNode) {
            TableSwitchInsnNode tsi = (TableSwitchInsnNode)insnNode;
            merge(indexes.get(tsi.dflt), current, subroutine);
            for (int j = 0; j < tsi.labels.size(); ++j) {
              Label label = (Label)tsi.labels.get(j);
              merge(indexes.get(label), current, subroutine);
            }
          } else if (insnOpcode == RET) {
            if (subroutine == null) {
              throw new RuntimeException(
                "RET instruction outside of a sub routine");
            } else {
              for (int i = 0; i < subroutine.callers.size(); ++i) {
                int caller = indexes.get(subroutine.callers.get(i));
                merge(caller + 1, frames[caller], current, subroutine.access);
              }
            }
          } else if (insnOpcode != ATHROW && (insnOpcode < IRETURN || insnOpcode > RETURN)) {
            if (subroutine != null) {
              if (insnNode instanceof VarInsnNode) {
                int var = ((VarInsnNode)insnNode).var;
                subroutine.access[var] = true;
                if (insnOpcode == LLOAD ||
                    insnOpcode == DLOAD ||
                    insnOpcode == LSTORE ||
                    insnOpcode == DSTORE)
                {
                  subroutine.access[var + 1] = true;
                }
              } else if (insnNode instanceof IincInsnNode) {
                int var = ((IincInsnNode)insnNode).var;
                subroutine.access[var] = true;
              }
            }
            merge(insn + 1, current, subroutine);
          }
        }
        
        List insnHandlers = handlers[insn];
        if (insnHandlers != null) {
          for (int i = 0; i < insnHandlers.size(); ++i) {
            TryCatchBlockNode tcb = (TryCatchBlockNode)insnHandlers.get(i);
            Type type;
            if (tcb.type == null) {
              type = Type.getType("Ljava/lang/Throwable;");
            } else {
              type = Type.getType("L" + tcb.type + ";");
            }
            handler.init(f);
            handler.clearStack();
            handler.push(interpreter.newValue(type));
            merge(indexes.get(tcb.handler), handler, subroutine);
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(
          "Error at instruction " + insn + ": " + e.getMessage());
      }
    }

    return frames;
  }

  /**
   * Returns the index of the given instruction.
   * 
   * @param insn a {@link Label} or {@link AbstractInsnNode} of the last 
   *      recently analyzed method.
   * @return the index of the given instruction of the last recently analyzed 
   *      method. 
   */
  
  public int getIndex (final Object insn) {
    return indexes.get(insn); 
  }
  
  /**
   * Returns the exception handlers for the given instruction.
   *   
   * @param insn the index of an instruction of the last recently analyzed 
   *      method.
   * @return a list of {@link TryCatchBlockNode} objects.
   */
  
  public List getHandlers (final int insn) {
    return handlers[insn];
  }
  
  // -------------------------------------------------------------------------
  
  private void execute (final AbstractInsnNode insn, final Frame f) {
    Value value1, value2, value3, value4;
    List values;
    int var;
    
    switch (insn.getOpcode()) {
      case NOP:
        break;
      case ACONST_NULL:
      case ICONST_M1:
      case ICONST_0:
      case ICONST_1:
      case ICONST_2:
      case ICONST_3:
      case ICONST_4:
      case ICONST_5:
      case LCONST_0:
      case LCONST_1:
      case FCONST_0:
      case FCONST_1:
      case FCONST_2:
      case DCONST_0:
      case DCONST_1:
      case BIPUSH:
      case SIPUSH:
      case LDC:
        f.push(interpreter.newOperation(insn));
        break;
      case ILOAD:
      case LLOAD:
      case FLOAD:
      case DLOAD:
      case ALOAD:
        f.push(interpreter.copyOperation(insn, f.getLocal(((VarInsnNode)insn).var)));
        break;
      case IALOAD:
      case LALOAD:
      case FALOAD:
      case DALOAD:
      case AALOAD:
      case BALOAD:
      case CALOAD:
      case SALOAD:
        value2 = f.pop();
        value1 = f.pop();
        f.push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case ISTORE:
      case LSTORE:
      case FSTORE:
      case DSTORE:
      case ASTORE:
        value1 = interpreter.copyOperation(insn, f.pop());
        var = ((VarInsnNode)insn).var;
        f.setLocal(var, value1);
        if (value1.getSize() == 2) {
          f.setLocal(var + 1, interpreter.newValue(null));
        }
        if (var > 0 && f.getLocal(var - 1).getSize() == 2) {
          f.setLocal(var - 1, interpreter.newValue(null));
        }
        break;
      case IASTORE:
      case LASTORE:
      case FASTORE:
      case DASTORE:
      case AASTORE:
      case BASTORE:
      case CASTORE:
      case SASTORE:
        value3 = f.pop();
        value2 = f.pop();
        value1 = f.pop();
        interpreter.ternaryOperation(insn, value1, value2, value3);
        break;
      case POP:
        if (f.pop().getSize() == 2) {
          throw new RuntimeException("Illegal use of POP");
        }
        break;
      case POP2:
        if (f.pop().getSize() == 1) {
          if (f.pop().getSize() != 1) {
            throw new RuntimeException("Illegal use of POP2");
          }
        }
        break;
      case DUP:
        value1 = f.pop();
        if (value1.getSize() != 1) {
          throw new RuntimeException("Illegal use of DUP");
        }
        f.push(interpreter.copyOperation(insn, value1));
        f.push(interpreter.copyOperation(insn, value1));
        break;
      case DUP_X1:
        value1 = f.pop();
        value2 = f.pop();
        if (value1.getSize() != 1 || value2.getSize() != 1) {
          throw new RuntimeException("Illegal use of DUP_X1");
        }
        f.push(interpreter.copyOperation(insn, value1));
        f.push(interpreter.copyOperation(insn, value2));
        f.push(interpreter.copyOperation(insn, value1));
        break;
      case DUP_X2:
        value1 = f.pop();
        if (value1.getSize() == 1) {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            value3 = f.pop();
            if (value3.getSize() == 1) {
              f.push(interpreter.copyOperation(insn, value1));
              f.push(interpreter.copyOperation(insn, value3));
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              break;
            }
          } else {
            f.push(interpreter.copyOperation(insn, value1));
            f.push(interpreter.copyOperation(insn, value2));
            f.push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new RuntimeException("Illegal use of DUP_X2");
      case DUP2:
        value1 = f.pop();
        if (value1.getSize() == 1) {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            f.push(interpreter.copyOperation(insn, value2));
            f.push(interpreter.copyOperation(insn, value1));
            f.push(interpreter.copyOperation(insn, value2));
            f.push(interpreter.copyOperation(insn, value1));
            break;
          }
        } else {
          f.push(interpreter.copyOperation(insn, value1));
          f.push(interpreter.copyOperation(insn, value1));
          break;
        }
        throw new RuntimeException("Illegal use of DUP2");
      case DUP2_X1:
        value1 = f.pop();
        if (value1.getSize() == 1) {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            value3 = f.pop();
            if (value3.getSize() == 1) {
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              f.push(interpreter.copyOperation(insn, value3));
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              break;
            }
          }
        } else {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            f.push(interpreter.copyOperation(insn, value1));
            f.push(interpreter.copyOperation(insn, value2));
            f.push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new RuntimeException("Illegal use of DUP2_X1");
      case DUP2_X2:
        value1 = f.pop();
        if (value1.getSize() == 1) {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            value3 = f.pop();
            if (value3.getSize() == 1) {
              value4 = f.pop();
              if (value4.getSize() == 1) {
                f.push(interpreter.copyOperation(insn, value2));
                f.push(interpreter.copyOperation(insn, value1));
                f.push(interpreter.copyOperation(insn, value4));
                f.push(interpreter.copyOperation(insn, value3));
                f.push(interpreter.copyOperation(insn, value2));
                f.push(interpreter.copyOperation(insn, value1));
                break;
              }
            } else {
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              f.push(interpreter.copyOperation(insn, value3));
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              break;
            }
          }
        } else {
          value2 = f.pop();
          if (value2.getSize() == 1) {
            value3 = f.pop();
            if (value3.getSize() == 1) {
              f.push(interpreter.copyOperation(insn, value1));
              f.push(interpreter.copyOperation(insn, value3));
              f.push(interpreter.copyOperation(insn, value2));
              f.push(interpreter.copyOperation(insn, value1));
              break;
            }
          } else {
            f.push(interpreter.copyOperation(insn, value1));
            f.push(interpreter.copyOperation(insn, value2));
            f.push(interpreter.copyOperation(insn, value1));
            break;
          }
        }
        throw new RuntimeException("Illegal use of DUP2_X2");
      case SWAP:
        value2 = f.pop();
        value1 = f.pop();
        if (value1.getSize() != 1 || value2.getSize() != 1) {
          throw new RuntimeException("Illegal use of SWAP");
        }
        f.push(interpreter.copyOperation(insn, value2));
        f.push(interpreter.copyOperation(insn, value1));
        break;
      case IADD:
      case LADD:
      case FADD:
      case DADD:
      case ISUB:
      case LSUB:
      case FSUB:
      case DSUB:
      case IMUL:
      case LMUL:
      case FMUL:
      case DMUL:
      case IDIV:
      case LDIV:
      case FDIV:
      case DDIV:
      case IREM:
      case LREM:
      case FREM:
      case DREM:
        value2 = f.pop();
        value1 = f.pop();
        f.push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case INEG:
      case LNEG:
      case FNEG:
      case DNEG:
        f.push(interpreter.unaryOperation(insn, f.pop()));
        break;
      case ISHL:
      case LSHL:
      case ISHR:
      case LSHR:
      case IUSHR:
      case LUSHR:
      case IAND:
      case LAND:
      case IOR:
      case LOR:
      case IXOR:
      case LXOR:
        value2 = f.pop();
        value1 = f.pop();
        f.push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case IINC:
        var = ((IincInsnNode)insn).var;
        f.setLocal(var, interpreter.unaryOperation(insn, f.getLocal(var)));
        break;
      case I2L:
      case I2F:
      case I2D:
      case L2I:
      case L2F:
      case L2D:
      case F2I:
      case F2L:
      case F2D:
      case D2I:
      case D2L:
      case D2F:
      case I2B:
      case I2C:
      case I2S:
        f.push(interpreter.unaryOperation(insn, f.pop()));
        break;
      case LCMP:
      case FCMPL:
      case FCMPG:
      case DCMPL:
      case DCMPG:
        value2 = f.pop();
        value1 = f.pop();
        f.push(interpreter.binaryOperation(insn, value1, value2));
        break;
      case IFEQ:
      case IFNE:
      case IFLT:
      case IFGE:
      case IFGT:
      case IFLE:
        interpreter.unaryOperation(insn, f.pop());
        break;
      case IF_ICMPEQ:
      case IF_ICMPNE:
      case IF_ICMPLT:
      case IF_ICMPGE:
      case IF_ICMPGT:
      case IF_ICMPLE:
      case IF_ACMPEQ:
      case IF_ACMPNE:
        value2 = f.pop();
        value1 = f.pop();
        interpreter.binaryOperation(insn, value1, value2);
        break;
      case GOTO:
        break;
      case JSR:
        f.push(interpreter.newOperation(insn));
        break;
      case RET:
        break;
      case TABLESWITCH:
      case LOOKUPSWITCH:
      case IRETURN:
      case LRETURN:
      case FRETURN:
      case DRETURN:
      case ARETURN:
        interpreter.unaryOperation(insn, f.pop());
        break;
      case RETURN:
        break;
      case GETSTATIC:
        f.push(interpreter.newOperation(insn));
        break;
      case PUTSTATIC:
        interpreter.unaryOperation(insn, f.pop());
        break;
      case GETFIELD:
        f.push(interpreter.unaryOperation(insn, f.pop()));
        break;
      case PUTFIELD:
        value2 = f.pop();
        value1 = f.pop();
        interpreter.binaryOperation(insn, value1, value2);
        break;
      case INVOKEVIRTUAL:
      case INVOKESPECIAL:
      case INVOKESTATIC:
      case INVOKEINTERFACE:
        values = new ArrayList();
        String desc = ((MethodInsnNode)insn).desc;
        for (int i = Type.getArgumentTypes(desc).length; i > 0; --i) {
          values.add(0, f.pop());
        }
        if (insn.getOpcode() != INVOKESTATIC) {
          values.add(0, f.pop());
        }
        if (Type.getReturnType(desc) == Type.VOID_TYPE) {
          interpreter.naryOperation(insn, values);
        } else {
          f.push(interpreter.naryOperation(insn, values));
        }
        break;
      case NEW:
        f.push(interpreter.newOperation(insn));
        break;
      case NEWARRAY:
      case ANEWARRAY:
      case ARRAYLENGTH:
        f.push(interpreter.unaryOperation(insn, f.pop()));
        break;
      case ATHROW:
        interpreter.unaryOperation(insn, f.pop());
        break;
      case CHECKCAST:
      case INSTANCEOF:
        f.push(interpreter.unaryOperation(insn, f.pop()));
        break;
      case MONITORENTER:
      case MONITOREXIT:
        interpreter.unaryOperation(insn, f.pop());
        break;
      case MULTIANEWARRAY:
        values = new ArrayList();
        for (int i = ((MultiANewArrayInsnNode)insn).dims; i > 0; --i) {
          values.add(0, f.pop());
        }
        f.push(interpreter.naryOperation(insn, values));
        break;
      case IFNULL:
      case IFNONNULL:
        interpreter.unaryOperation(insn, f.pop());
        break;
      default:
        throw new RuntimeException("Illegal opcode");
    }
  }
  
  private void merge (
    final int insn, 
    final Frame frame, 
    final Subroutine subroutine) 
  {
    if (insn > n - 1) {
      throw new RuntimeException("Execution can fall off end of the code");
    } else {
      Frame oldFrame = frames[insn];
      Subroutine oldSubroutine = subroutines[insn];
      boolean changes = false;

      if (oldFrame == null) {
        frames[insn] = new Frame(frame);
        changes = true;
      } else {
        changes |= oldFrame.merge(frame);
      }

      if (oldSubroutine == null) {
        if (subroutine != null) {
          subroutines[insn] = subroutine.copy();
          changes = true;
        }
      } else {
        if (subroutine != null) {
          changes |= oldSubroutine.merge(subroutine);
        }
      }
      if (changes && !queued[insn]) {
        queued[insn] = true;
        queue[top++] = insn;
      }
    }
  }

  private void merge (
    final int insn, 
    final Frame beforeJSR,
    final Frame afterRET,
    final boolean[] access)
  {
    if (insn > n - 1) {
      throw new RuntimeException("Execution can fall off end of the code");
    } else {
      Frame oldFrame = frames[insn];
      Subroutine oldSubroutine = subroutines[insn];
      boolean changes = false;

      afterRET.merge(beforeJSR, access);
      
      if (oldFrame == null) {
        frames[insn] = new Frame(afterRET);
        changes = true;
      } else {  
        changes |= oldFrame.merge(afterRET, access);
      }

      if (changes && !queued[insn]) {
        queued[insn] = true;
        queue[top++] = insn;
      }
    }
  }

  private static class IntMap {
    
    private int size;
    
    private Object[] keys;
    
    private int[] values;
    
    private IntMap (final int size) {
      this.size = size;
      this.keys = new Object[size];
      this.values = new int[size];
    }
    
    public int get (final Object key) {
      int n = size;
      int i = (key.hashCode() & 0x7FFFFFFF)%n;
      while (keys[i] != key) {
        i = (i+1)%n;
      }
      return values[i];
    }
    
    public void put (final Object key, final int value) {
      int n = size;
      int i = (key.hashCode() & 0x7FFFFFFF)%n;
      while (keys[i] != null) {
        i = (i+1)%n;
      }
      keys[i] = key;
      values[i] = value;
    }
  }
  
  private static class Subroutine {

    Label start;

    boolean[] access;

    List callers;

    private Subroutine () {
    }

    public Subroutine (final Label start, final int maxLocals, final JumpInsnNode caller) {
      this.start = start;
      this.access = new boolean[maxLocals];
      this.callers = new ArrayList();
      callers.add(caller);
    }

    public Subroutine copy () {
      Subroutine result = new Subroutine();
      result.start = start;
      result.access = new boolean[access.length];
      System.arraycopy(access, 0, result.access, 0, access.length);
      result.callers = new ArrayList(callers);
      return result;
    }

    public boolean merge (final Subroutine subroutine) {
      if (subroutine.start != start) {
        throw new RuntimeException("Overlapping sub routines");
      }
      boolean changes = false;
      for (int i = 0; i < access.length; ++i) {
        if (subroutine.access[i] && !access[i]) {
          access[i] = true;
          changes = true;
        }
      }
      for (int i = 0; i < subroutine.callers.size(); ++i) {
        Object caller = subroutine.callers.get(i);
        if (!callers.contains(caller)) {
          callers.add(caller);
          changes = true;
        }
      }
      return changes;
    }
  }
}
