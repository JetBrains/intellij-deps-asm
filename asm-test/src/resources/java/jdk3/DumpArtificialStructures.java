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
package jdk3;

import java.io.FileOutputStream;
import java.io.IOException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeComment;
import org.objectweb.asm.Comment;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Generates a class with structures, instructions and patterns that cannot be produced by compiling
 * a Java source file with the javac compiler. This includes:
 *
 * <ul>
 *   <li>the class, field and method Synthetic attribute (now replaced with an access flag),
 *   <li>the StackMap attribute (which was used for pre-verification in J2ME CLDC 1.1),
 *   <li>non standard class, field, method and code attributes,
 *   <li>the nop and swap instructions,
 *   <li>some variants of the dup_x2 and dup2_x2 instructions,
 *   <li>several line numbers per bytecode offset.
 * </ul>
 *
 * Ideally we should not use ASM to generate this class (which is later used to test ASM), but this
 * would be hard to do.
 *
 * @author Eric Bruneton
 */
public class DumpArtificialStructures implements Opcodes {

  public static void main(String[] args) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream("ArtificialStructures.class");
    fileOutputStream.write(dump());
    fileOutputStream.close();
  }

  private static byte[] dump() {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    FieldVisitor fieldVisitor;
    MethodVisitor methodVisitor;

    classWriter.visit(
        V1_3, ACC_PUBLIC + ACC_SUPER, "jdk3/ArtificialStructures", null, "java/lang/Object", null);

    classWriter.visitSource("ArtificialStructures.java", "source-debug");

    classWriter.visitAttribute(new Comment());

    fieldVisitor = classWriter.visitField(ACC_PUBLIC + ACC_SYNTHETIC, "f", "I", null, null);
    fieldVisitor.visitAttribute(new Comment());
    fieldVisitor.visitEnd();

    methodVisitor =
        classWriter.visitMethod(
            ACC_PUBLIC + ACC_SYNTHETIC, "<init>", "(Ljava/lang/String;)V", null, null);
    methodVisitor.visitAttribute(new Comment());
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitInsn(NOP);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitAttribute(new CodeComment());
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(0, "<init>", "(Z)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ILOAD, 1);
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitInsn(SWAP);
    Label elseLabel = new Label();
    methodVisitor.visitJumpInsn(IFEQ, elseLabel);
    methodVisitor.visitLdcInsn("1");
    Label endIfLabel = new Label();
    methodVisitor.visitJumpInsn(GOTO, endIfLabel);
    methodVisitor.visitLabel(elseLabel);
    methodVisitor.visitLineNumber(1, elseLabel);
    methodVisitor.visitLineNumber(3, elseLabel);
    methodVisitor.visitLdcInsn("0");
    methodVisitor.visitLabel(endIfLabel);
    methodVisitor.visitLineNumber(5, endIfLabel);
    methodVisitor.visitLineNumber(7, endIfLabel);
    methodVisitor.visitLineNumber(11, endIfLabel);
    methodVisitor.visitLineNumber(13, endIfLabel);
    methodVisitor.visitLineNumber(17, endIfLabel);
    methodVisitor.visitMethodInsn(
        INVOKESPECIAL, "jdk3/ArtificialStructures", "<init>", "(Ljava/lang/String;)V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_STATIC, "dup_x2", "(IJ)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(LLOAD, 1);
    methodVisitor.visitVarInsn(ILOAD, 0);
    methodVisitor.visitInsn(DUP_X2);
    methodVisitor.visitInsn(I2L);
    methodVisitor.visitInsn(LADD);
    methodVisitor.visitMethodInsn(
        INVOKESTATIC, "jdk3/ArtificialStructures", "dup_x2", "(IJ)V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_STATIC, "dup2_x2", "(IIII)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ILOAD, 3);
    methodVisitor.visitVarInsn(ILOAD, 2);
    methodVisitor.visitVarInsn(ILOAD, 1);
    methodVisitor.visitVarInsn(ILOAD, 0);
    methodVisitor.visitInsn(DUP2_X2);
    methodVisitor.visitInsn(IADD);
    methodVisitor.visitInsn(IADD);
    methodVisitor.visitMethodInsn(
        INVOKESTATIC, "jdk3/ArtificialStructures", "dup2_x2", "(IIII)V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_STATIC, "dup2_x2", "(IIJ)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(LLOAD, 2);
    methodVisitor.visitVarInsn(ILOAD, 1);
    methodVisitor.visitVarInsn(ILOAD, 0);
    methodVisitor.visitInsn(DUP2_X2);
    methodVisitor.visitInsn(IADD);
    methodVisitor.visitInsn(I2L);
    methodVisitor.visitInsn(LADD);
    methodVisitor.visitMethodInsn(
        INVOKESTATIC, "jdk3/ArtificialStructures", "dup2_x2", "(IIJ)V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_STATIC, "dup2_x2", "(JD)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(DLOAD, 2);
    methodVisitor.visitVarInsn(LLOAD, 0);
    methodVisitor.visitInsn(DUP2_X2);
    methodVisitor.visitInsn(L2D);
    methodVisitor.visitInsn(DADD);
    methodVisitor.visitMethodInsn(
        INVOKESTATIC, "jdk3/ArtificialStructures", "dup2_x2", "(JD)V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    classWriter.visitEnd();
    return classWriter.toByteArray();
  }
}
