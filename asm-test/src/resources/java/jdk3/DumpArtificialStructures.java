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
    FileOutputStream fos = new FileOutputStream("ArtificialStructures.class");
    fos.write(dump());
    fos.close();
  }

  private static byte[] dump() {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    FieldVisitor fv;
    MethodVisitor mv;

    cw.visit(
        V1_3, ACC_PUBLIC + ACC_SUPER, "jdk3/ArtificialStructures", null, "java/lang/Object", null);

    cw.visitSource("ArtificialStructures.java", "source-debug");

    cw.visitAttribute(new Comment());

    fv = cw.visitField(ACC_PUBLIC + ACC_SYNTHETIC, "f", "I", null, null);
    fv.visitAttribute(new Comment());
    fv.visitEnd();

    mv = cw.visitMethod(ACC_PUBLIC + ACC_SYNTHETIC, "<init>", "(Ljava/lang/String;)V", null, null);
    mv.visitAttribute(new Comment());
    mv.visitCode();
    mv.visitVarInsn(ALOAD, 0);
    mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(NOP);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitAttribute(new CodeComment());
    mv.visitEnd();

    mv = cw.visitMethod(0, "<init>", "(Z)V", null, null);
    mv.visitCode();
    mv.visitVarInsn(ILOAD, 1);
    mv.visitVarInsn(ALOAD, 0);
    mv.visitInsn(SWAP);
    Label elseLabel = new Label();
    mv.visitJumpInsn(IFEQ, elseLabel);
    mv.visitLdcInsn("1");
    Label endIfLabel = new Label();
    mv.visitJumpInsn(GOTO, endIfLabel);
    mv.visitLabel(elseLabel);
    mv.visitLineNumber(1, elseLabel);
    mv.visitLineNumber(3, elseLabel);
    mv.visitLdcInsn("0");
    mv.visitLabel(endIfLabel);
    mv.visitLineNumber(5, endIfLabel);
    mv.visitLineNumber(7, endIfLabel);
    mv.visitLineNumber(11, endIfLabel);
    mv.visitLineNumber(13, endIfLabel);
    mv.visitLineNumber(17, endIfLabel);
    mv.visitMethodInsn(
        INVOKESPECIAL, "jdk3/ArtificialStructures", "<init>", "(Ljava/lang/String;)V", false);
    mv.visitInsn(RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();
    return cw.toByteArray();
  }
}
