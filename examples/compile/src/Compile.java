/***
 * ASM examples: examples showing how ASM can be used
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;

import java.io.FileOutputStream;

/**
 * @author Eric Bruneton
 */

public class Compile extends ClassLoader {

  public static void main (String[] args) throws Exception {
    // creates the expression tree corresponding to
    //   exp(i) = i > 3 && 6 > i
    Exp exp = new And(new GT(new Var(0), new Cst(3)),
      new GT(new Cst(6), new Var(0)));
    // compiles this expression into an Expression class
    Compile main = new Compile();
    byte[] b = exp.compile("Example");
    FileOutputStream fos = new FileOutputStream("Example.class");
    fos.write(b);
    fos.close();
    Class expClass = main.defineClass("Example", b, 0, b.length);
    // instantiates this compiled expression class...
    Expression iexp = (Expression)expClass.newInstance();
    // ... and uses it to evaluate exp(0) to exp(9)
    for (int i = 0; i < 10; ++i) {
      boolean val = iexp.eval(i, 0) == 1;
      System.out.println(i + " > 3 && " + i + " < 6 = " + val);
    }
  }
}

/**
 * An abstract expression.
 *
 * @author Eric Bruneton
 */

abstract class Exp implements Constants {

  /**
   * Returns the byte code of an Expression class corresponding to this
   * expression.
   */

  byte[] compile (String name) {
    // class header
    String[] itfs = {Expression.class.getName()};
    ClassWriter cw = new ClassWriter(true);
    cw.visit(ACC_PUBLIC, name, "java/lang/Object", itfs, null);

    // default public constructor
    CodeVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(
      INVOKESPECIAL,
      "java/lang/Object", "<init>", "()V");
    mw.visitInsn(RETURN);
    mw.visitMaxs(1, 1);

    // eval method
    mw = cw.visitMethod(ACC_PUBLIC, "eval", "(II)I", null, null);
    compile(mw);
    mw.visitInsn(IRETURN);
    // max stack and max locals automatically computed
    mw.visitMaxs(0, 0);

    return cw.toByteArray();
  }

  /**
   * Compile this expression. This method must append to the given code
   * writer the byte code that evaluates and pushes on the stack the
   * value of this expression.
   */

  abstract void compile (CodeVisitor mw);
}

/**
 * A constant expression.
 */

class Cst extends Exp {

  int value;

  Cst (int value) {
    this.value = value;
  }

  void compile (CodeVisitor mw) {
    // pushes the constant's value onto the stack
    mw.visitLdcInsn(new Integer(value));
  }
}

/**
 * A variable reference expression.
 */

class Var extends Exp {

  int index;

  Var (int index) {
    this.index = index + 1;
  }

  void compile (CodeVisitor mb) {
    // pushes the 'index' local variable onto the stack
    mb.visitVarInsn(ILOAD, index);
  }
}

/**
 * An abstract binary expression.
 */

abstract class BinaryExp extends Exp {

  Exp e1;
  Exp e2;

  BinaryExp (Exp e1, Exp e2) {
    this.e1 = e1;
    this.e2 = e2;
  }
}

/**
 * An addition expression.
 */

class Add extends BinaryExp {

  Add (Exp e1, Exp e2) {
    super(e1, e2);
  }

  void compile (CodeVisitor mw) {
    // compiles e1, e2, and adds an instruction to add the two values
    e1.compile(mw);
    e2.compile(mw);
    mw.visitInsn(IADD);
  }
}

/**
 * A multiplication expression.
 */

class Mul extends BinaryExp {

  Mul (Exp e1, Exp e2) {
    super(e1, e2);
  }

  void compile (CodeVisitor mb) {
    // compiles e1, e2, and adds an instruction to multiply the two values
    e1.compile(mb);
    e2.compile(mb);
    mb.visitInsn(IMUL);
  }
}

/**
 * A "greater than" expression.
 */

class GT extends BinaryExp {

  GT (Exp e1, Exp e2) {
    super(e1, e2);
  }

  void compile (CodeVisitor mw) {
    // compiles e1, e2, and adds the instructions to compare the two values
    e1.compile(mw);
    e2.compile(mw);
    Label iftrue = new Label();
    Label end = new Label();
    mw.visitJumpInsn(IF_ICMPGT, iftrue);
    // case where e1 <= e2 : pushes false and jump to "end"
    mw.visitInsn(ICONST_0);
    mw.visitJumpInsn(GOTO, end);
    // case where e1 > e2 : pushes true
    mw.visitLabel(iftrue);
    mw.visitInsn(ICONST_1);
    mw.visitLabel(end);
  }
}

/**
 * A logical "and" expression.
 */

class And extends BinaryExp {

  And (Exp e1, Exp e2) {
    super(e1, e2);
  }

  void compile (CodeVisitor mw) {
    // compiles e1
    e1.compile(mw);
    // tests if e1 is false
    mw.visitInsn(DUP);
    Label end = new Label();
    mw.visitJumpInsn(IFEQ, end);
    // case where e1 is true : e1 && e2 is equal to e2
    mw.visitInsn(POP);
    e2.compile(mw);
    // if e1 is false, e1 && e2 is equal to e1:
    //   we jump directly to this label, without evaluating e2
    mw.visitLabel(end);
  }
}

/**
 * A logical "or" expression.
 */

class Or extends BinaryExp {

  Or (Exp e1, Exp e2) {
    super(e1, e2);
  }

  void compile (CodeVisitor mw) {
    // compiles e1
    e1.compile(mw);
    // tests if e1 is true
    mw.visitInsn(DUP);
    Label end = new Label();
    mw.visitJumpInsn(IFNE, end);
    // case where e1 is false : e1 || e2 is equal to e2
    mw.visitInsn(POP);
    e2.compile(mw);
    // if e1 is true, e1 || e2 is equal to e1:
    //   we jump directly to this label, without evaluating e2
    mw.visitLabel(end);
  }
}

/**
 * A logical "not" expression.
 */

class Not extends Exp {

  Exp e;

  Not (Exp e) {
    this.e = e;
  }

  void compile (CodeVisitor mw) {
    // computes !e1 by evaluating 1 - e1
    mw.visitInsn(ICONST_1);
    e.compile(mw);
    mw.visitInsn(ISUB);
  }
}
