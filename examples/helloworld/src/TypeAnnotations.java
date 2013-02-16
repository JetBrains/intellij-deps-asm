import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.util.TraceClassVisitor;

/***
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c)
 * 2000-2011 INRIA, France Telecom All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of the
 * copyright holders nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written
 * permission.
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
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

public class TypeAnnotations implements Opcodes {

    public static void main(String[] args) {
        ClassWriter cw = new ClassWriter(0);
        StringWriter sw = new StringWriter();
        PrintWriter printer = new PrintWriter(sw);
        TraceClassVisitor cv = new TraceClassVisitor(cw, printer);

        cv.visit(V1_8, ACC_PUBLIC, "TypeAnnotations",
                "<E:LC;F:LD;>LS<TE;TF;>;LI1<TE;>;LI2<TF;>;", "S", new String[] {
                        "I1", "I2" });
        AnnotationVisitor av = cv.visitAnnotation("A1", true);
        av.visit("a", "0");
        av.visit("b", new Integer(1));
        cv.visitAnnotation("A2", false).visitEnd();
        // targets param 0
        av = cv.visitTypeAnnotation(0xFF0000, 0xFF, "A3", true);
        av.visitAnnotation("c", "A4").visitEnd();
        // targets param 1
        cv.visitTypeAnnotation(0xFF0100, 0xFF, "A5", true).visitEnd();
        // targets param 0, bound 0
        cv.visitTypeAnnotation(0xFF000000, 0xFF, "A6", true).visitEnd();
        // targets param 1, bound 0
        cv.visitTypeAnnotation(0xFF000100, 0xFF, "A7", true).visitEnd();
        // targets super class
        cv.visitTypeAnnotation(0xFF01, 0xFF, "A8", true).visitEnd();
        // targets super class, type argument 0
        cv.visitTypeAnnotation(0xFF01, 0xFF00, "A9", true).visitEnd();
        // targets super class, type argument 1
        cv.visitTypeAnnotation(0xFF01, 0xFF11, "A10", true).visitEnd();
        // targets interface 0
        cv.visitTypeAnnotation(0xFF0002, 0xFF, "A11", true).visitEnd();
        // targets interface 0, type argument 0
        cv.visitTypeAnnotation(0xFF0002, 0xFF00, "A12", true).visitEnd();
        // targets interface 1
        cv.visitTypeAnnotation(0xFF0102, 0xFF, "A13", true).visitEnd();
        // targets interface 1, type argument 0
        cv.visitTypeAnnotation(0xFF0102, 0xFF00, "A14", false).visitEnd();

        FieldVisitor fv = cv.visitField(ACC_PUBLIC, "f", "LS;", 
                "LMap<LComparable<[[[LObject;>;LList<LDocument;>;>;", //"LS<TE;TF;>;",
                null);
        av = fv.visitAnnotation("B1", true);
        av.visit("c", "0");
        av.visit("d", new Integer(1));
        fv.visitAnnotation("B2", false).visitEnd();
        // targets type argument 0
        fv.visitTypeAnnotation(0xFF, 0xFF00, "B3", true).visitEnd();
        // targets type argument 1
        fv.visitTypeAnnotation(0xFF, 0xFF01, "B4", false).visitEnd();
        fv.visitEnd();

        String signature = "<E:LX;F:LY;>(TE;TF;LZ<+TE;+TF;>;)LZ<+TE;+TF;>;^LE1<TX;>;^LE2<TY;>;";
        MethodVisitor mv = cv.visitMethod(ACC_PUBLIC + ACC_STATIC, "m",
                "(LX;LY;LZ;)LZ;", signature, new String[] { "E1", "E2" });
        av = mv.visitAnnotation("C1", true);
        av.visit("e", "0");
        av.visit("f", new Integer(1));
        mv.visitAnnotation("C2", false).visitEnd();
        // targets param 0
        av = mv.visitTypeAnnotation(0xFF0000, 0xFF, "C3", true);
        av.visitAnnotation("c", "C4").visitEnd();
        // targets param 1
        mv.visitTypeAnnotation(0xFF0100, 0xFF, "C5", true).visitEnd();
        // targets param 0, bound 0
        mv.visitTypeAnnotation(0xFF000000, 0xFF, "C6", true).visitEnd();
        // targets param 1, bound 0
        mv.visitTypeAnnotation(0xFF000100, 0xFF, "C7", true).visitEnd();
        // targets return type
        mv.visitTypeAnnotation(0xFF01, 0xFF, "C8", true).visitEnd();
        // targets return type, type argument 0
        mv.visitTypeAnnotation(0xFF01, 0xFF00, "C9", true).visitEnd();
        // targets return type, type argument 1
        mv.visitTypeAnnotation(0xFF01, 0xFF11, "C10", true).visitEnd();
        // no receiver type (static method)
        //
        // targets parameter 0
        mv.visitTypeAnnotation(0xFF0003, 0xFF, "C11", true).visitEnd();
        // targets parameter 1
        mv.visitTypeAnnotation(0xFF0103, 0xFF, "C12", true).visitEnd();
        // targets parameter 2
        mv.visitTypeAnnotation(0xFF0203, 0xFF, "C13", true).visitEnd();
        // targets parameter 2, type argument 0
        mv.visitTypeAnnotation(0xFF0203, 0xFF00, "C14", true).visitEnd();
        // targets parameter 2, type argument 1
        mv.visitTypeAnnotation(0xFF0203, 0xFF01, "C15", true).visitEnd();
        // targets exception 0
        mv.visitTypeAnnotation(0xFF0004, 0xFF, "C16", true).visitEnd();
        // targets exception 1
        mv.visitTypeAnnotation(0xFF0104, 0xFF, "C17", true).visitEnd();
        // targets exception 0, type argument 0
        mv.visitTypeAnnotation(0xFF0004, 0xFF00, "C18", true).visitEnd();
        // targets exception 1, type argument 1
        mv.visitTypeAnnotation(0xFF0104, 0xFF01, "C19", false).visitEnd();
        mv.visitCode();
        Label l0 = new Label();
        Label l1 = new Label();
        Label l2 = new Label();
        Label l3 = new Label();
        Label l4 = new Label();
        Label l5 = new Label();
        Label l6 = new Label();
        Label l7 = new Label();
        mv.visitTryCatchBlock(l2, l3, l7, "E1");
        mv.visitTryCatchBlock(l5, l6, l7, "E2");
        // targets try catch block 0
        mv.visitTryCatchAnnotation(0xFF00, 0xFF, "C20", true).visitEnd();
        // targets try catch block 1
        mv.visitTryCatchAnnotation(0xFF00, 0xFF, "C21", false).visitEnd();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLabel(l0);
        mv.visitVarInsn(ASTORE, 4);
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitLabel(l1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitLabel(l2);
        mv.visitVarInsn(ASTORE, 4);
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitLabel(l3);
        mv.visitVarInsn(ALOAD, 0);
        mv.visitLabel(l4);
        mv.visitVarInsn(ASTORE, 4);
        // targets the astore instruction
        av = mv.visitInsnAnnotation(0xFF, 0xFF, "C22", true);
        av.visit("g", "0");
        av.visit("h", new Integer(1));
        av.visitEnd();
        // targets the astore instruction
        mv.visitInsnAnnotation(0xFF, 0xFF, "C23", false).visitEnd();
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitInsn(NOP);
        mv.visitLabel(l5);
        mv.visitInsn(NOP);
        mv.visitLabel(l6);
        mv.visitInsn(NOP);
        mv.visitLabel(l7);
        mv.visitVarInsn(ASTORE, 5);
        // targets the astore instruction
        mv.visitInsnAnnotation(0xFF, 0xFF, "C24", true).visitEnd();
        mv.visitInsn(ACONST_NULL);
        mv.visitInsn(ARETURN);
        mv.visitLocalVariable("a", "LX;", "TE;", l0, l1, 4);
        mv.visitLocalVariable("b", "LZ;", "LZ<+TE;+TF;>;", l2, l3, 4);
        mv.visitLocalVariable("a", "LX;", "TE;", l4, l5, 4);
        mv.visitLocalVariable("a", "LX;", "TE;", l0, l1, 4);
        // targets local variable a
        av = mv.visitLocalVariableAnnotation(0xFF00, 0xFF,
                new Label[] { l0, l4 }, new Label[] { l1, l5 }, new int[] { 4,
                        4 }, "D0", true);
        av.visit("i", "0");
        av.visit("j", new Integer(1));
        av.visitEnd();
        // targets local variable b
        mv.visitLocalVariableAnnotation(0xFF00, 0xFF, new Label[] { l2 },
                new Label[] { l3 }, new int[] { 4 }, "C25", true).visitEnd();
        // targets local variable b, type argument 0
        mv.visitLocalVariableAnnotation(0xFF00, 0xFF00, new Label[] { l2 },
                new Label[] { l3 }, new int[] { 4 }, "C26", true).visitEnd();
        // targets local variable b, type argument 1
        mv.visitLocalVariableAnnotation(0xFF00, 0xFF01, new Label[] { l2 },
                new Label[] { l3 }, new int[] { 4 }, "C27", false).visitEnd();
        mv.visitMaxs(1, 3);
        mv.visitEnd();
        cv.visitEnd();

        System.out.println("REFERENCE");
        System.out.println(sw.toString());
        String ref = sw.toString();

        ClassReader cr = new ClassReader(cw.toByteArray());
        sw = new StringWriter();
        printer = new PrintWriter(sw);
        cr.accept(new TraceClassVisitor(printer), 0);

        System.out.println("VALUE");
        System.out.println(sw.toString());
        String value = sw.toString();
        
        if (!value.equals(ref)) throw new RuntimeException();
    }
}