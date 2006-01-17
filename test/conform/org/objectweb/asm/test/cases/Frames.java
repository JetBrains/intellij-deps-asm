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

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

/**
 * Generates classes with StackMap and StackMapTable attributes. Covers all
 * frame (FULL, SAME, etc.) and frame element types (TOP, NULL, INTEGER, etc.).
 * Also covers the V1_6 class version.
 * 
 * @author Eric Bruneton
 */
public class Frames extends Generator {

    public void generate(final String dir) throws IOException {
        byte[] b = dump();
        ClassWriter cw = new ClassWriter(0);
        ClassReader cr = new ClassReader(b);
        cr.accept(new RenameAdapter(cw), ClassReader.EXPAND_FRAMES);

        generate(dir, "pkg/FrameTable.class", b);
        generate(dir, "pkg/FrameMap.class", cw.toByteArray());
    }

    public byte[] dump() {
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        FieldVisitor fv;
        Label l0, l1, l2, l3, l4, l5, l6;

        cw.visit(V1_6,
                ACC_PUBLIC + ACC_SUPER,
                "pkg/FrameTable",
                null,
                "java/lang/Object",
                null);

        fv = cw.visitField(ACC_STATIC, "long", "Ljava/lang/Long;", null, null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "double",
                "Ljava/lang/Double;",
                null,
                null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "number",
                "Ljava/lang/Number;",
                null,
                null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "serializable",
                "Ljava/io/Serializable;",
                null,
                null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "comparable",
                "Ljava/lang/Comparable;",
                null,
                null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "longArray",
                "[Ljava/lang/Long;",
                null,
                null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC, "intArray", "[I", null, null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC, "floatArray", "[F", null, null);
        fv.visitEnd();

        fv = cw.visitField(ACC_STATIC,
                "objectArray",
                "[Ljava/lang/Object;",
                null,
                null);
        fv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "<init>",
                "(Ljava/lang/Object;)V",
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "pkg/FrameTable");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, 1);
        l1 = new Label();
        mv.visitJumpInsn(IFNE, l1);
        mv.visitInsn(ACONST_NULL);
        l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitFrame(F_FULL,
                2,
                new Object[] { UNINITIALIZED_THIS, INTEGER },
                3,
                new Object[] { UNINITIALIZED_THIS, l0, l0 });
        mv.visitLabel(l1);
        mv.visitTypeInsn(NEW, "java/lang/Object");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitFrame(F_FULL,
                2,
                new Object[] { UNINITIALIZED_THIS, INTEGER },
                4,
                new Object[] { UNINITIALIZED_THIS, l0, l0, "java/lang/Object" });
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESPECIAL,
                "pkg/FrameTable",
                "<init>",
                "(Ljava/lang/Object;)V");
        mv.visitMethodInsn(INVOKESPECIAL,
                "pkg/FrameTable",
                "<init>",
                "(Ljava/lang/Object;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "nullType",
                "(Ljava/lang/String;Ljava/lang/String;)V",
                null,
                null);
        mv.visitCode();
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ALOAD, 1);
        l0 = new Label();
        mv.visitJumpInsn(IFNONNULL, l0);
        mv.visitVarInsn(ALOAD, 2);
        l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitFrame(F_FULL, 3, new Object[] {
            "pkg/FrameTable",
            "java/lang/String",
            NULL }, 2, new Object[] { "pkg/FrameTable", NULL });
        mv.visitLabel(l0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFrame(F_FULL, 3, new Object[] {
            "pkg/FrameTable",
            "java/lang/String",
            NULL }, 3, new Object[] {
            "pkg/FrameTable",
            NULL,
            "java/lang/String" });
        mv.visitLabel(l1);
        mv.visitMethodInsn(INVOKEVIRTUAL,
                "pkg/FrameTable",
                "nullType",
                "(Ljava/lang/String;Ljava/lang/String;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "appendTopFrame",
                "(ZBCSIFJDLjava/lang/Object;)V",
                null,
                null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 5);
        mv.visitVarInsn(ISTORE, 13);
        mv.visitVarInsn(ILOAD, 1);
        l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitInsn(RETURN);
        mv.visitFrame(F_APPEND, 2, new Object[] { TOP, INTEGER }, 0, null);
        mv.visitLabel(l0);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 14);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "appendAndChopFrame",
                "(I)V",
                null,
                null);
        mv.visitCode();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 2);
        mv.visitFrame(F_APPEND, 1, new Object[] { INTEGER }, 0, null);
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitVarInsn(ILOAD, 2);
        mv.visitVarInsn(ILOAD, 1);
        l1 = new Label();
        mv.visitJumpInsn(IF_ICMPGE, l1);
        mv.visitIincInsn(2, 1);
        mv.visitJumpInsn(GOTO, l0);
        mv.visitFrame(F_CHOP, 1, null, 0, null);
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "sameLocals1stackItemFrame",
                "()I",
                null,
                null);
        mv.visitCode();
        l0 = new Label();
        l1 = new Label();
        mv.visitTryCatchBlock(l0, l1, l0, null);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitFrame(F_SAME1,
                0,
                null,
                1,
                new Object[] { "java/lang/Throwable" });
        mv.visitLabel(l0);
        mv.visitVarInsn(ASTORE, 1);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "sameLocals1stackItemFrame2",
                "()V",
                null,
                null);
        mv.visitCode();
        l0 = new Label();
        l1 = new Label();
        l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, "java/lang/Exception");
        l3 = new Label();
        mv.visitTryCatchBlock(l0, l1, l3, null);
        l4 = new Label();
        mv.visitTryCatchBlock(l2, l4, l3, null);
        l5 = new Label();
        mv.visitTryCatchBlock(l3, l5, l3, null);
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "java/lang/Object");
        mv.visitInsn(DUP);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
        mv.visitVarInsn(ASTORE, 1);
        mv.visitLabel(l1);
        l6 = new Label();
        mv.visitJumpInsn(GOTO, l6);
        mv.visitFrame(F_SAME1,
                0,
                null,
                1,
                new Object[] { "java/lang/Exception" });
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitLabel(l4);
        mv.visitJumpInsn(GOTO, l6);
        mv.visitFrame(F_SAME1,
                0,
                null,
                1,
                new Object[] { "java/lang/Throwable" });
        mv.visitLabel(l3);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitLabel(l5);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitInsn(ATHROW);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l6);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 4);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "sameLocals1stackItemFrameExtended",
                "()I",
                null,
                null);
        mv.visitCode();
        l0 = new Label();
        l1 = new Label();
        l2 = new Label();
        mv.visitTryCatchBlock(l0, l1, l2, null);
        l3 = new Label();
        mv.visitTryCatchBlock(l2, l3, l2, null);
        mv.visitLabel(l0);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 1);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 3);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 5);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 7);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 9);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 11);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 13);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 15);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 17);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 19);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 21);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 23);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 25);
        mv.visitLabel(l1);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitFrame(F_SAME1,
                0,
                null,
                1,
                new Object[] { "java/lang/Throwable" });
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 27);
        mv.visitLabel(l3);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 28);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "sameFrameExtended", "(Z)V", null, null);
        mv.visitCode();
        mv.visitFrame(F_SAME, 0, null, 0, null);
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 2);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 4);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 6);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 8);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 10);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 12);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 14);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 16);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 18);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 20);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 22);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 24);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 26);
        mv.visitVarInsn(ILOAD, 1);
        l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(RETURN);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l1);
        mv.visitJumpInsn(GOTO, l0);
        mv.visitMaxs(2, 28);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC,
                "fullFrame",
                "(Ljava/lang/String;[[Z[B[C[S[I[F[J[D[Ljava/lang/Object;)V",
                null,
                null);
        mv.visitCode();
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 11);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 13);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 15);
        mv.visitLdcInsn(new Long(11L));
        mv.visitVarInsn(LSTORE, 17);
        mv.visitVarInsn(ALOAD, 1);
        l0 = new Label();
        mv.visitJumpInsn(IFNONNULL, l0);
        mv.visitInsn(RETURN);
        mv.visitFrame(F_FULL, 15, new Object[] {
            "pkg/FrameTable",
            "java/lang/String",
            "[[Z",
            "[B",
            "[C",
            "[S",
            "[I",
            "[F",
            "[J",
            "[D",
            "[Ljava/lang/Object;",
            LONG,
            LONG,
            LONG,
            LONG }, 0, new Object[] {});
        mv.visitLabel(l0);
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 19);
        mv.visitEnd();
        
        mv = cw.visitMethod(ACC_PUBLIC,
                "deadCode",
                "(Z)V",
                null,
                null);
        mv.visitCode();
        l0 = new Label();
        l1 = new Label();
        l2 = new Label();
        l3 = new Label();
        mv.visitTryCatchBlock(l0, l1, l1, "java/lang/Exception");
        mv.visitTryCatchBlock(l2, l3, l3, "java/lang/Exception");        
        mv.visitJumpInsn(GOTO, l2);
        mv.visitLabel(l0);
        mv.visitInsn(RETURN);
        mv.visitLabel(l1);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitInsn(RETURN);        
        mv.visitLabel(l2);
        mv.visitInsn(RETURN);
        mv.visitLabel(l3);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 3);
        mv.visitEnd();
        
        mv = cw.visitMethod(ACC_PUBLIC,
                "uninitializedLocal",
                "(Z)V",
                null,
                null);
        mv.visitCode();
        l0 = new Label();
        mv.visitLabel(l0);
        mv.visitTypeInsn(NEW, "java/lang/Long");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ILOAD, 1);
        l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(LCONST_0);
        l2 = new Label();
        mv.visitJumpInsn(GOTO, l2);
        mv.visitFrame(F_FULL,
                3,
                new Object[] { "pkg/FrameTable", INTEGER, l0 },
                1,
                new Object[] { l0 });
        mv.visitLabel(l1);
        mv.visitInsn(LCONST_1);
        mv.visitFrame(F_FULL,
                3,
                new Object[] { "pkg/FrameTable", INTEGER, l0 },
                2,
                new Object[] { l0, LONG });
        mv.visitLabel(l2);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Long", "<init>", "(J)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "merge", "(Z)V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1);
        l0 = new Label();
        mv.visitJumpInsn(IFEQ, l0);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "long",
                "Ljava/lang/Long;");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "number",
                "Ljava/lang/Number;");
        mv.visitVarInsn(ASTORE, 3);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "number",
                "Ljava/lang/Long;");
        mv.visitVarInsn(ASTORE, 4);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "comparable",
                "Ljava/lang/Comparable;");
        mv.visitVarInsn(ASTORE, 5);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 6);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "double",
                "Ljava/lang/Double;");
        mv.visitVarInsn(ASTORE, 7);
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, 8);
        mv.visitFieldInsn(GETSTATIC, "pkg/FrameTable", "intArray", "[I");
        mv.visitVarInsn(ASTORE, 9);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "double",
                "Ljava/lang/Double;");
        mv.visitVarInsn(ASTORE, 10);
        l1 = new Label();
        mv.visitJumpInsn(GOTO, l1);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l0);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "double",
                "Ljava/lang/Double;");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "double",
                "Ljava/lang/Double;");
        mv.visitVarInsn(ASTORE, 3);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "number",
                "Ljava/lang/Number;");
        mv.visitVarInsn(ASTORE, 4);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "serializable",
                "Ljava/io/Serializable;");
        mv.visitVarInsn(ASTORE, 5);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 6);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 7);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 8);
        mv.visitFieldInsn(GETSTATIC, "pkg/FrameTable", "floatArray", "[F");
        mv.visitVarInsn(ASTORE, 9);
        mv.visitFieldInsn(GETSTATIC, "pkg/FrameTable", "intArray", "[I");
        mv.visitVarInsn(ASTORE, 10);
        mv.visitFrame(F_FULL, 11, new Object[] {
            "pkg/FrameTable",
            INTEGER,
            "java/lang/Number",
            "java/lang/Number",
            "java/lang/Number",
            "java/lang/Object",
            NULL,
            "java/lang/Double",
            TOP,
            "java/lang/Object",
            "java/lang/Object" }, 0, null);
        mv.visitLabel(l1);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 11);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "mergeNullArray", "(Z)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1);
        l1 = new Label();
        mv.visitJumpInsn(IFEQ, l1);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 2);
        mv.visitVarInsn(ILOAD, 1);
        l2 = new Label();
        mv.visitJumpInsn(IFEQ, l2);
        mv.visitFieldInsn(GETSTATIC,
                "pkg/FrameTable",
                "longArray",
                "[Ljava/lang/Long;");
        mv.visitVarInsn(ASTORE, 2);
        mv.visitFrame(F_APPEND,
                1,
                new Object[] { "[Ljava/lang/Long;" },
                0,
                null);
        mv.visitLabel(l2);
        mv.visitVarInsn(ALOAD, 2);
        l3 = new Label();
        mv.visitJumpInsn(IFNULL, l3);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ARRAYLENGTH);
        l4 = new Label();
        mv.visitJumpInsn(IFNE, l4);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l3);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 3);
        l5 = new Label();
        mv.visitJumpInsn(GOTO, l5);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l4);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitInsn(ICONST_0);
        mv.visitInsn(AALOAD);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitJumpInsn(IFNE, l5);
        mv.visitInsn(ACONST_NULL);
        mv.visitVarInsn(ASTORE, 3);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitLabel(l5);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IRETURN);
        mv.visitFrame(F_CHOP, 1, null, 0, null);
        mv.visitLabel(l1);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 4);
        mv.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }

    /**
     * Ad hoc class adapter used to rename the FrameTable class and to change
     * its class version.
     * 
     * @author Eric Bruneton
     */
    static class RenameAdapter extends ClassAdapter {

        public RenameAdapter(final ClassVisitor cv) {
            super(cv);
        }

        public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces)
        {
            super.visit(V1_5,
                    access,
                    "pkg/FrameMap",
                    signature,
                    superName,
                    interfaces);
        }

        public MethodVisitor visitMethod(
            int access,
            String name,
            String desc,
            String signature,
            String[] exceptions)
        {
            return new MethodAdapter(super.visitMethod(access,
                    name,
                    desc,
                    signature,
                    exceptions))
            {
                public void visitFrame(
                    int type,
                    int nLocal,
                    Object[] local,
                    int nStack,
                    Object[] stack)
                {
                    Object[] clocal = new Object[local.length];
                    for (int i = 0; i < clocal.length; ++i) {
                        clocal[i] = local[i];
                        if ("pkg/FrameTable".equals(clocal[i])) {
                            clocal[i] = "pkg/FrameMap";
                        }
                    }
                    Object[] cstack = new Object[stack.length];
                    for (int i = 0; i < cstack.length; ++i) {
                        cstack[i] = stack[i];
                        if ("pkg/FrameTable".equals(cstack[i])) {
                            cstack[i] = "pkg/FrameMap";
                        }
                    }
                    super.visitFrame(type, nLocal, clocal, nStack, cstack);
                }

                public void visitTypeInsn(int opcode, String desc) {
                    if (desc.equals("pkg/FrameTable")) {
                        super.visitTypeInsn(opcode, "pkg/FrameMap");
                    } else {
                        super.visitTypeInsn(opcode, desc);
                    }
                }

                public void visitMethodInsn(
                    int opcode,
                    String owner,
                    String name,
                    String desc)
                {
                    if (owner.equals("pkg/FrameTable")) {
                        super.visitMethodInsn(opcode,
                                "pkg/FrameMap",
                                name,
                                desc);
                    } else {
                        super.visitMethodInsn(opcode, owner, name, desc);
                    }
                }
            };
        }
    }
}
