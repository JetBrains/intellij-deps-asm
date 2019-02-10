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
package jdk8;

import java.io.FileOutputStream;
import java.io.IOException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * Generates a class with structures, instructions and patterns that cannot be produced by compiling
 * a Java source file with the javac compiler. This includes LDC instructions with MethodType
 * constants.
 *
 * <p>Ideally we should not use ASM to generate this class (which is later used to test ASM), but
 * this would be hard to do.
 *
 * @author Eric Bruneton
 */
public class DumpArtificialStructures implements Opcodes {

  public static void main(String[] args) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream("Artificial$()$Structures.class");
    fileOutputStream.write(dump());
    fileOutputStream.close();
  }

  private static byte[] dump() {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
    MethodVisitor methodVisitor;

    classWriter.visit(
        V1_8,
        ACC_PUBLIC + ACC_SUPER,
        "jdk8/Artificial$()$Structures",
        null,
        "java/lang/Object",
        null);

    classWriter.visitField(ACC_PUBLIC, "value", "I", null, null).visitEnd();

    methodVisitor = classWriter.visitMethod(ACC_PUBLIC, "<init>", "(I)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitVarInsn(ILOAD, 1);
    methodVisitor.visitFieldInsn(PUTFIELD, "jdk8/Artificial$()$Structures", "value", "I");
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor =
        classWriter.visitMethod(
            ACC_PRIVATE, "<init>", "(Ljdk8/Artificial$()$Structures;)V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitVarInsn(ALOAD, 1);
    Label elseLabel = new Label();
    methodVisitor.visitJumpInsn(IFNULL, elseLabel);
    methodVisitor.visitVarInsn(ALOAD, 1);
    methodVisitor.visitFieldInsn(GETFIELD, "jdk8/Artificial$()$Structures", "value", "I");
    Label endIfLabel = new Label();
    methodVisitor.visitJumpInsn(GOTO, endIfLabel);
    methodVisitor.visitLabel(elseLabel);
    methodVisitor.visitInsn(ICONST_0);
    methodVisitor.visitLabel(endIfLabel);
    methodVisitor.visitFieldInsn(PUTFIELD, "jdk8/Artificial$()$Structures", "value", "I");
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor =
        classWriter.visitMethod(
            ACC_PUBLIC, "clone", "()Ljdk8/Artificial$()$Structures;", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitTypeInsn(NEW, "jdk8/Artificial$()$Structures");
    methodVisitor.visitInsn(DUP);
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitMethodInsn(
        INVOKESPECIAL,
        "jdk8/Artificial$()$Structures",
        "<init>",
        "(Ljdk8/Artificial$()$Structures;)V",
        false);
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    methodVisitor =
        classWriter.visitMethod(
            ACC_PUBLIC + ACC_STATIC, "methodType", "()Ljava/lang/invoke/MethodType;", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitLdcInsn(Type.getMethodType("(I)I"));
    methodVisitor.visitInsn(ARETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    classWriter.visitEnd();
    return classWriter.toByteArray();
  }
}
