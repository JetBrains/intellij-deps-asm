/***
 * ASM examples: examples showing how asm can be used
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

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;

import java.io.FileOutputStream;

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
    CodeVisitor mw = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null);
    mw.visitVarInsn(ALOAD, 0);
    mw.visitMethodInsn(
      INVOKESPECIAL,
      "java/lang/Object", "<init>", "()V");
    mw.visitInsn(RETURN);
    mw.visitMaxs(1, 1);

		// eval method
    mw = cw.visitMethod(ACC_PUBLIC, "eval", "(II)I", null);
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
