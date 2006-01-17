/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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
package org.objectweb.asm.test.cases;

import java.io.IOException;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates a class that contain all bytecode instruction types (except JSR and
 * RET). Also covers class, field, method, and method parameter visible and
 * invisible annotations, almost all access flags, and unicode characters.
 * 
 * @author Eric Bruneton
 */
public class Insns extends Generator {

    public void generate(final String dir) throws IOException {
        generate(dir, "pkg/Insns.class", dump());
    }

    public byte[] dump() {
        ClassWriter cw = new ClassWriter(0);
        FieldVisitor fv;
        MethodVisitor mv;
        AnnotationVisitor av0, av1;

        cw.visit(V1_5,
                ACC_PUBLIC + ACC_SUPER,
                "pkg/Insns",
                "<E:Ljava/lang/Object;F:Ljava/lang/Exception;>Ljava/util/ArrayList<Ljava/lang/String;>;LInterface<TE;>;",
                "java/util/ArrayList",
                new String[] { "Interface" });

        av0 = cw.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("byteValue", new Byte((byte) 0));
        av0.visit("charValue", new Character((char) 48));
        av0.visit("booleanValue", new Boolean(false));
        av0.visit("intValue", new Integer(0));
        av0.visitEnd();

        av0 = cw.visitAnnotation("Ljava/lang/Deprecated;", true);
        av0.visitEnd();

        fv = cw.visitField(ACC_PRIVATE + ACC_FINAL,
                "z",
                "Z",
                null,
                new Integer(1));
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("shortValue", new Short((short) 0));
        av0.visit("longValue", new Long(0L));
        av0.visit("floatValue", new Float("0.0"));
        av0.visit("doubleValue", new Double("0.0"));
        av0.visitEnd();
        av0 = fv.visitAnnotation("Ljava/lang/Deprecated;", true);
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(ACC_PROTECTED, "b", "B", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("stringValue", "0");
        av0.visitEnum("enumValue", "Lpkg/Enum;", "V0");
        av1 = av0.visitAnnotation("annotationValue",
                "Ljava/lang/annotation/Documented;");
        av1.visitEnd();
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(ACC_PUBLIC, "c", "C", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("classValue", Type.getType("Lpkg/Annotation;"));
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC, "s", "S", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("byteArrayValue", new byte[] { 1, 0 });
        av0.visit("charArrayValue", new char[] { (char) 49, (char) 0 });
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(ACC_PRIVATE + ACC_TRANSIENT, "i", "I", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("booleanArrayValue", new boolean[] { true, false });
        av0.visit("intArrayValue", new int[] { 1, 0 });
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(ACC_PRIVATE + ACC_VOLATILE, "l", "J", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("shortArrayValue", new short[] { (short) 1, (short) 0 });
        av0.visit("longArrayValue", new long[] { 1L, 0L });
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(0, "f", "F", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visit("floatArrayValue", new float[] { 1.0f, 0.0f });
        av0.visit("doubleArrayValue", new double[] { 1.0d, 0.0d });
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(0, "d", "D", null, null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av1 = av0.visitArray("stringArrayValue");
        av1.visit(null, "1");
        av1.visit(null, "0");
        av1.visitEnd();
        av1 = av0.visitArray("enumArrayValue");
        av1.visitEnum(null, "Lpkg/Enum;", "V1");
        av1.visitEnum(null, "Lpkg/Enum;", "V2");
        av1.visitEnd();
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(0, "str", "Ljava/lang/String;", null, "");
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av1 = av0.visitArray("annotationArrayValue");
        av1.visitEnd();
        av1 = av0.visitArray("classArrayValue");
        av1.visitEnd();
        av0.visitEnd();
        fv.visitEnd();

        fv = cw.visitField(0, "e", "Ljava/lang/Object;", "TE;", null);
        av0 = fv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visitEnum("enumValue", "Lpkg/Enum;", "V0");
        av0.visitEnd();
        fv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        av0 = mv.visitAnnotation("Lpkg/Annotation;", false);
        av1 = av0.visitAnnotation("annotationValue",
                "Ljava/lang/annotation/Documented;");
        av1.visitEnd();
        av0.visitEnd();
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/util/ArrayList",
                "<init>",
                "()V");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "z", "Z");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "b", "B");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(BIPUSH, 49);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "c", "C");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(LCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "f", "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "d", "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u1111\u0111\u0011\u0001");
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 1);
        mv.visitEnd();

        mv = cw.visitMethod(0, "i", "()I", null, null);
        av0 = mv.visitAnnotation("Ljava/lang/Deprecated;", true);
        av0.visitEnd();
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(0, "l", "()J", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(LRETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        mv = cw.visitMethod(0, "f", "()F", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "f", "F");
        mv.visitInsn(FRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(0, "d", "()D", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "d", "D");
        mv.visitInsn(DRETURN);
        mv.visitMaxs(2, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_SYNCHRONIZED,
                "m",
                "(ZBCSIFJDLjava/lang/Object;)Ljava/lang/Object;",
                "(ZBCSIFJDTE;)TE;",
                null);
        av0 = mv.visitAnnotation("Lpkg/Annotation;", false);
        av0.visitEnd();
        av0 = mv.visitParameterAnnotation(8, "Lpkg/Annotation;", false);
        av0.visitEnd();
        av0 = mv.visitParameterAnnotation(8, "Ljava/lang/Deprecated;", true);
        av0.visitEnd();
        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, null);
        Label l3 = new Label();
        mv.visitTryCatchBlock(l0, l3, l3, "java/lang/Exception");
        Label l4 = new Label();
        Label l5 = new Label();
        Label l6 = new Label();
        mv.visitTryCatchBlock(l4, l5, l6, null);
        Label l7 = new Label();
        mv.visitTryCatchBlock(l6, l7, l6, null);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 11);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_M1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_0);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_2);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_3);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_4);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_5);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(LCONST_0);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(LCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_0);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "f", "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "f", "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(FCONST_2);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "f", "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DCONST_0);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "d", "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DCONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "d", "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(SIPUSH, 128);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitIntInsn(SIPUSH, 256);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(new Integer(65536));
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(new Long(128L));
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(new Float("128.0"));
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "f", "F");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn(new Double("128.0"));
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "d", "D");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("str");
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        Label l8 = new Label();
        mv.visitJumpInsn(IFEQ, l8);
        mv.visitInsn(ICONST_1);
        Label l9 = new Label();
        mv.visitJumpInsn(GOTO, l9);
        mv.visitLabel(l8);
        mv.visitInsn(ICONST_0);
        mv.visitLabel(l9);
        mv.visitVarInsn(ISTORE, 5);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(BASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(BALOAD);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_CHAR);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitIntInsn(BIPUSH, 48);
        mv.visitInsn(CASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(BIPUSH, 49);
        mv.visitInsn(CASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(CALOAD);
        mv.visitVarInsn(ISTORE, 3);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_SHORT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(SASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(SASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(SALOAD);
        mv.visitVarInsn(ISTORE, 4);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IALOAD);
        mv.visitVarInsn(ISTORE, 5);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_LONG);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(LCONST_0);
        mv.visitInsn(LASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(LALOAD);
        mv.visitVarInsn(LSTORE, 7);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_FLOAT);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(FCONST_0);
        mv.visitInsn(FASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(FALOAD);
        mv.visitVarInsn(FSTORE, 6);
        mv.visitInsn(ICONST_2);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(DCONST_0);
        mv.visitInsn(DASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(DALOAD);
        mv.visitVarInsn(DSTORE, 9);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 11);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_2);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_0);
        mv.visitTypeInsn(NEW, "java/lang/String");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("0");
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/lang/String",
                "<init>",
                "(Ljava/lang/String;)V");
        mv.visitInsn(AASTORE);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(NEW, "java/lang/String");
        mv.visitInsn(DUP);
        mv.visitLdcInsn("1");
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/lang/String",
                "<init>",
                "(Ljava/lang/String;)V");
        mv.visitInsn(AASTORE);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(BASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_BYTE);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitInsn(BASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_CHAR);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 3);
        mv.visitInsn(CASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_SHORT);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 4);
        mv.visitInsn(SASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_INT);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitInsn(IASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_LONG);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(LLOAD, 7);
        mv.visitInsn(LASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_FLOAT);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(FLOAD, 6);
        mv.visitInsn(FASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(DLOAD, 9);
        mv.visitInsn(DASTORE);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "java/lang/String");
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitInsn(AASTORE);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitVarInsn(ASTORE, 12);
        mv.visitTypeInsn(NEW, "java/lang/Float");
        mv.visitInsn(DUP);
        mv.visitInsn(FCONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Float", "<init>", "(F)V");
        mv.visitMethodInsn(INVOKEVIRTUAL,
                "java/lang/Float",
                "floatValue",
                "()F");
        mv.visitInsn(POP);
        mv.visitTypeInsn(NEW, "java/lang/Double");
        mv.visitInsn(DUP);
        mv.visitInsn(DCONST_0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Double", "<init>", "(D)V");
        mv.visitMethodInsn(INVOKEVIRTUAL,
                "java/lang/Double",
                "doubleValue",
                "()D");
        mv.visitInsn(POP2);
        mv.visitInsn(FCONST_0);
        mv.visitVarInsn(FSTORE, 13);
        mv.visitInsn(DCONST_0);
        mv.visitVarInsn(DSTORE, 14);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_FLOAT);
        mv.visitVarInsn(ASTORE, 16);
        mv.visitInsn(ICONST_1);
        mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        mv.visitVarInsn(ASTORE, 17);
        mv.visitVarInsn(ALOAD, 16);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FSUB);
        mv.visitInsn(DUP);
        mv.visitVarInsn(FSTORE, 13);
        mv.visitInsn(DUP_X2);
        mv.visitInsn(FASTORE);
        mv.visitVarInsn(FSTORE, 13);
        mv.visitVarInsn(ALOAD, 17);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DSUB);
        mv.visitInsn(DUP2);
        mv.visitVarInsn(DSTORE, 14);
        mv.visitInsn(DUP2_X2);
        mv.visitInsn(DASTORE);
        mv.visitVarInsn(DSTORE, 14);
        mv.visitTypeInsn(NEW, "java/lang/Integer");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(DUP_X1);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Integer", "<init>", "(I)V");
        mv.visitInsn(POP);
        mv.visitTypeInsn(NEW, "java/lang/Long");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(DUP2_X1);
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LADD);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V");
        mv.visitInsn(POP);
        mv.visitVarInsn(ILOAD, 5);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(IMUL);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_2);
        mv.visitInsn(IREM);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_2);
        mv.visitInsn(IDIV);
        mv.visitInsn(ISUB);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISHL);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ISHR);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(IUSHR);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, 5);
        mv.visitVarInsn(LLOAD, 7);
        mv.visitLdcInsn(new Long(2L));
        mv.visitInsn(LMUL);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitLdcInsn(new Long(2L));
        mv.visitInsn(LREM);
        mv.visitInsn(LADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitLdcInsn(new Long(2L));
        mv.visitInsn(LDIV);
        mv.visitInsn(LSUB);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(LSHL);
        mv.visitInsn(LADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(LSHR);
        mv.visitInsn(LADD);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(ICONST_1);
        mv.visitInsn(LUSHR);
        mv.visitInsn(LADD);
        mv.visitVarInsn(LSTORE, 7);
        mv.visitVarInsn(FLOAD, 6);
        mv.visitInsn(FCONST_2);
        mv.visitInsn(FMUL);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_2);
        mv.visitInsn(FREM);
        mv.visitInsn(FADD);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_2);
        mv.visitInsn(FDIV);
        mv.visitInsn(FSUB);
        mv.visitVarInsn(FSTORE, 6);
        mv.visitVarInsn(DLOAD, 9);
        mv.visitLdcInsn(new Double("2.0"));
        mv.visitInsn(DMUL);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitLdcInsn(new Double("2.0"));
        mv.visitInsn(DREM);
        mv.visitInsn(DADD);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitLdcInsn(new Double("2.0"));
        mv.visitInsn(DDIV);
        mv.visitInsn(DSUB);
        mv.visitVarInsn(DSTORE, 9);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(INEG);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(LNEG);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FNEG);
        mv.visitVarInsn(FSTORE, 13);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(DNEG);
        mv.visitVarInsn(DSTORE, 14);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 18);
        Label l10 = new Label();
        mv.visitLabel(l10);
        mv.visitVarInsn(ILOAD, 18);
        mv.visitInsn(ICONST_3);
        Label l11 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l11);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_M1);
        mv.visitInsn(IXOR);
        mv.visitInsn(IOR);
        mv.visitInsn(IAND);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(IXOR);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitLdcInsn(new Long(-1L));
        mv.visitInsn(LXOR);
        mv.visitInsn(LOR);
        mv.visitInsn(LAND);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(LXOR);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitIincInsn(18, 1);
        mv.visitJumpInsn(GOTO, l10);
        mv.visitLabel(l11);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2B);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "b", "B");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2C);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "c", "C");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2S);
        mv.visitFieldInsn(PUTSTATIC, "pkg/Insns", "s", "S");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(L2I);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(F2I);
        mv.visitInsn(IADD);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(D2I);
        mv.visitInsn(IADD);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2L);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(F2L);
        mv.visitInsn(LADD);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(D2L);
        mv.visitInsn(LADD);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2F);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(L2F);
        mv.visitInsn(FADD);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(D2F);
        mv.visitInsn(FADD);
        mv.visitVarInsn(FSTORE, 13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(I2D);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(L2D);
        mv.visitInsn(DADD);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(F2D);
        mv.visitInsn(DADD);
        mv.visitVarInsn(DSTORE, 14);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        Label l12 = new Label();
        mv.visitJumpInsn(IFNE, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(IFEQ, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(IFLE, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(IFGE, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(IFLT, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(IFGT, l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitLabel(l12);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        Label l13 = new Label();
        mv.visitJumpInsn(IF_ICMPNE, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IF_ICMPEQ, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IF_ICMPLE, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IF_ICMPGE, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IF_ICMPLT, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitInsn(ICONST_1);
        mv.visitJumpInsn(IF_ICMPGT, l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitLabel(l13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LCMP);
        Label l14 = new Label();
        mv.visitJumpInsn(IFNE, l14);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(IFNE, l14);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FCMPL);
        mv.visitJumpInsn(IFLE, l14);
        mv.visitVarInsn(FLOAD, 13);
        mv.visitInsn(FCONST_1);
        mv.visitInsn(FCMPG);
        mv.visitJumpInsn(IFGE, l14);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DCMPL);
        mv.visitJumpInsn(IFNE, l14);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DCMPL);
        mv.visitJumpInsn(IFLE, l14);
        mv.visitVarInsn(DLOAD, 14);
        mv.visitInsn(DCONST_1);
        mv.visitInsn(DCMPG);
        mv.visitJumpInsn(IFGE, l14);
        mv.visitLabel(l14);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitLdcInsn("str");
        Label l15 = new Label();
        mv.visitJumpInsn(IF_ACMPNE, l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitLdcInsn("str");
        mv.visitJumpInsn(IF_ACMPEQ, l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("\n\r\u0009\"\\");
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitLabel(l15);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        Label l16 = new Label();
        mv.visitJumpInsn(IFNONNULL, l16);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitJumpInsn(IFNULL, l16);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLdcInsn("1");
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitLabel(l16);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        Label l17 = new Label();
        Label l18 = new Label();
        Label l19 = new Label();
        Label l20 = new Label();
        mv.visitInsn(NOP);
        mv.visitTableSwitchInsn(0, 2, l20, new Label[] { l17, l18, l19 });
        mv.visitLabel(l17);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l20);
        mv.visitLabel(l18);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_2);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l20);
        mv.visitLabel(l19);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_3);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l20);
        mv.visitLabel(l20);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        Label l21 = new Label();
        Label l22 = new Label();
        Label l23 = new Label();
        mv.visitLookupSwitchInsn(l0,
                new int[] { 0, 10000, 20000 },
                new Label[] { l21, l22, l23 });
        mv.visitLabel(l21);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l22);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_2);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l23);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ICONST_2);
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "i", "I");
        mv.visitJumpInsn(GOTO, l0);
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/lang/RuntimeException",
                "<init>",
                "()V");
        mv.visitInsn(ATHROW);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 19);
        mv.visitLabel(l1);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 20);
        mv.visitVarInsn(ALOAD, 20);
        mv.visitInsn(ICONST_1);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "b", "B");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "c", "C");
        mv.visitFieldInsn(GETSTATIC, "pkg/Insns", "s", "S");
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "i", "I");
        mv.visitVarInsn(FLOAD, 13);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, "pkg/Insns", "l", "J");
        mv.visitVarInsn(DLOAD, 14);
        mv.visitVarInsn(ALOAD, 11);
        mv.visitMethodInsn(INVOKEINTERFACE,
                "Interface",
                "m",
                "(ZBCSIFJDLjava/lang/Object;)Ljava/lang/Object;");
        mv.visitInsn(POP);
        mv.visitVarInsn(ALOAD, 19);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l3);
        mv.visitVarInsn(ASTORE, 18);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitTypeInsn(INSTANCEOF, "java/lang/String");
        Label l24 = new Label();
        mv.visitJumpInsn(IFEQ, l24);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, 19);
        mv.visitInsn(MONITORENTER);
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 12);
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitFieldInsn(PUTFIELD, "pkg/Insns", "str", "Ljava/lang/String;");
        mv.visitVarInsn(ALOAD, 19);
        mv.visitInsn(MONITOREXIT);
        mv.visitLabel(l5);
        mv.visitJumpInsn(GOTO, l24);
        mv.visitLabel(l6);
        mv.visitVarInsn(ASTORE, 21);
        mv.visitVarInsn(ALOAD, 19);
        mv.visitInsn(MONITOREXIT);
        mv.visitLabel(l7);
        mv.visitVarInsn(ALOAD, 21);
        mv.visitInsn(ATHROW);
        mv.visitLabel(l24);
        mv.visitInsn(ICONST_1);
        mv.visitTypeInsn(ANEWARRAY, "[I");
        mv.visitInsn(POP);
        mv.visitInsn(ICONST_1);
        mv.visitInsn(ICONST_2);
        mv.visitInsn(ICONST_3);
        mv.visitMultiANewArrayInsn("[[[I", 3);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(ACONST_NULL);
        mv.visitTypeInsn(CHECKCAST, "[[I");
        mv.visitInsn(AASTORE);
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(12, 22);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_VARARGS + ACC_STRICT,
                "n",
                "([Ljava/lang/Object;)V",
                "([Ljava/lang/Object;)V^TF;",
                new String[] { "java/lang/Exception" });
        mv.visitCode();
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "get",
                "(I)Ljava/lang/String;",
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL,
                "java/util/ArrayList",
                "get",
                "(I)Ljava/lang/Object;");
        mv.visitTypeInsn(CHECKCAST, "java/lang/String");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC + ACC_BRIDGE + ACC_SYNTHETIC,
                "get",
                "(I)Ljava/lang/Object;",
                "(I)TE;",
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "pkg/Insns", "get", "(I)Ljava/lang/String;");
        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PRIVATE + ACC_NATIVE, "o", "()V", null, null);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_1);
        mv.visitFieldInsn(PUTSTATIC, "pkg/Insns", "s", "S");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}
