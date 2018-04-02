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
package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * StaticInitMerger tests.
 *
 * @author Eric Bruneton
 */
public class StaticInitMergerTest {

  @Test
  public void test() throws Exception {
    ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS);
    ClassVisitor classVisitor = new StaticInitMerger("$clinit$", classWriter);
    classVisitor.visit(Opcodes.V1_1, Opcodes.ACC_PUBLIC, "A", null, "java/lang/Object", null);
    classVisitor.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "counter", "I", null, null);
    for (int i = 0; i < 5; ++i) {
      MethodVisitor methodVisitor =
          classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "<clinit>", "()V", null, null);
      methodVisitor.visitFieldInsn(Opcodes.GETSTATIC, "A", "counter", "I");
      methodVisitor.visitInsn(Opcodes.ICONST_1);
      methodVisitor.visitInsn(Opcodes.IADD);
      methodVisitor.visitFieldInsn(Opcodes.PUTSTATIC, "A", "counter", "I");
      methodVisitor.visitInsn(Opcodes.RETURN);
      methodVisitor.visitMaxs(0, 0);
    }
    MethodVisitor methodVisitor =
        classVisitor.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
    methodVisitor.visitMethodInsn(
        Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitInsn(Opcodes.RETURN);
    methodVisitor.visitMaxs(0, 0);
    classVisitor.visitEnd();

    Class<?> aClass =
        new ClassLoader() {
          public Class<?> defineClass(final String name, final byte[] classFile) {
            return defineClass(name, classFile, 0, classFile.length);
          }
        }.defineClass("A", classWriter.toByteArray());
    assertEquals(aClass.getField("counter").getInt(aClass.newInstance()), 5);
  }
}
