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

import gnu.bytecode.Access;
import gnu.bytecode.ClassType;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Field;
import gnu.bytecode.Method;

import com.claritysys.jvm.builder.CodeBuilder;
import com.claritysys.jvm.classfile.CfMethod;
import com.claritysys.jvm.classfile.ClassFile;
import com.claritysys.jvm.classfile.ConstantPool;
import com.claritysys.jvm.classfile.JVM;

/**
 * Performance tests for frameworks that can only do bytecode generation.
 * 
 * @author Eric Bruneton
 */
public class GenPerfTest {

    final static int N = 100000;
    
    public static void main(String[] args) {
        for (int i = 0; i < 5; ++i) {
            asmTest();
        }
        for (int i = 0; i < 5; ++i) {
            gnuByteCodeTest();
        }
        for (int i = 0; i < 5; ++i) {
            csgBytecodeTest();
        }
    }
    
    static void asmTest() {
        long t = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            asmHelloWorld();
        }
        t = System.currentTimeMillis() - t;
        System.out.println("ASM generation time: " + ((float) t) / N + " ms/class");
    }
    
    static void gnuByteCodeTest() {
        long t = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            gnuByteCodeHelloWorld();
        }
        t = System.currentTimeMillis() - t;
        System.out.println("gnu.bytecode generation time: " + ((float) t) / N + " ms/class");
    }

    static void csgBytecodeTest() {
        long t = System.currentTimeMillis();
        for (int i = 0; i < N; ++i) {
            csgBytecodeHelloWorld();
        }
        t = System.currentTimeMillis() - t;
        System.out.println("CSG bytecode generation time: " + ((float) t) / N + " ms/class");
    }

    static byte[] asmHelloWorld() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);

        cw.visit(Opcodes.V1_1,
                Opcodes.ACC_PUBLIC,
                "HelloWorld",
                null,
                "java/lang/Object",
                null);
        cw.visitSource("HelloWorld.java", null);

        MethodVisitor mw = cw.visitMethod(Opcodes.ACC_PUBLIC,
                "<init>",
                "()V",
                null,
                null);
        mw.visitVarInsn(Opcodes.ALOAD, 0);
        mw.visitMethodInsn(Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                "<init>",
                "()V");
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(0, 0);
        mw.visitEnd();

        mw = cw.visitMethod(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V",
                null,
                null);
        mw.visitFieldInsn(Opcodes.GETSTATIC,
                "java/lang/System",
                "out",
                "Ljava/io/PrintStream;");
        mw.visitLdcInsn("Hello world!");
        mw.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V");
        mw.visitInsn(Opcodes.RETURN);
        mw.visitMaxs(0, 0);
        mw.visitEnd();

        return cw.toByteArray();
    }

    static Method objectCtor = gnu.bytecode.Type.pointer_type.getDeclaredMethod("<init>",
            0);

    static Field outField = ClassType.make("java.lang.System").getField("out");

    static Method printlnMethod = ClassType.make("java.io.PrintStream")
            .getDeclaredMethod("println",
                    new gnu.bytecode.Type[] { gnu.bytecode.Type.string_type });

    static byte[] gnuByteCodeHelloWorld() {
        ClassType c = new ClassType("HelloWorld");
        c.setSuper("java.lang.Object");
        c.setModifiers(Access.PUBLIC);
        c.setSourceFile("HelloWorld.java");

        Method m = c.addMethod("<init>", "()V", Access.PUBLIC);
        CodeAttr code = m.startCode();
        code.pushScope();
        code.emitPushThis();
        code.emitInvokeSpecial(objectCtor);
        code.emitReturn();
        code.popScope();

        m = c.addMethod("main", "([Ljava/lang/String;)V", Access.PUBLIC
                | Access.STATIC);
        code = m.startCode();
        code.pushScope();
        code.emitGetStatic(outField);
        code.emitPushString("Hello world!");
        code.emitInvokeVirtual(printlnMethod);
        code.emitReturn();
        code.popScope();

        return c.writeToArray();
    }
    
    static byte[] csgBytecodeHelloWorld() {
        ClassFile cf = new ClassFile("HelloWorld",
                "java/lang/Object",
                "HelloWorld.java");
        ConstantPool cp = cf.getConstantPool();

        CfMethod method = cf.addMethod(JVM.ACC_PUBLIC, "<init>", "()V");
        CodeBuilder code = new CodeBuilder(method);
        code.add(JVM.ALOAD_0);
        code.add(JVM.INVOKESPECIAL, cp.addMethodRef(false,
                "java/lang/Object",
                "<init>",
                "()V"));
        code.add(JVM.RETURN);
        code.flush();

        method = cf.addMethod(JVM.ACC_PUBLIC + JVM.ACC_STATIC,
                "main",
                "([Ljava/lang/String;)V");
        code = new CodeBuilder(method);
        code.add(JVM.GETSTATIC, cp.addFieldRef("java/lang/System",
                "out",
                "Ljava/io/PrintStream;"));
        code.add(JVM.LDC, "Hello world!");
        code.add(JVM.INVOKEVIRTUAL, cp.addMethodRef(false,
                "java/io/PrintStream",
                "println",
                "(Ljava/lang/String;)V"));
        code.add(JVM.RETURN);
        code.flush();

        return cf.writeToArray();
    }
}
