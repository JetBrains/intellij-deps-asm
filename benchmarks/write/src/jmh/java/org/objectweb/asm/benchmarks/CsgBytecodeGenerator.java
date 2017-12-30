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
package org.objectweb.asm.benchmarks;

import com.claritysys.jvm.builder.CodeBuilder;
import com.claritysys.jvm.classfile.CfMethod;
import com.claritysys.jvm.classfile.ClassFile;
import com.claritysys.jvm.classfile.ConstantPool;
import com.claritysys.jvm.classfile.JVM;

/**
 * A "Hello World!" class generator using the CSG Bytecode library.
 *
 * @author Eric Bruneton
 */
public class CsgBytecodeGenerator extends Generator {

  @Override
  public byte[] generateClass() {
    ClassFile classFile = new ClassFile("HelloWorld", "java/lang/Object", "HelloWorld.java");
    ConstantPool constantPool = classFile.getConstantPool();

    CfMethod method = classFile.addMethod(JVM.ACC_PUBLIC, "<init>", "()V");
    CodeBuilder code = new CodeBuilder(method);
    code.add(JVM.ALOAD_0);
    code.add(
        JVM.INVOKESPECIAL, constantPool.addMethodRef(false, "java/lang/Object", "<init>", "()V"));
    code.add(JVM.RETURN);
    code.flush();

    method = classFile.addMethod(JVM.ACC_PUBLIC + JVM.ACC_STATIC, "main", "([Ljava/lang/String;)V");
    code = new CodeBuilder(method);
    code.add(
        JVM.GETSTATIC,
        constantPool.addFieldRef("java/lang/System", "out", "Ljava/io/PrintStream;"));
    code.add(JVM.LDC, "Hello world!");
    code.add(
        JVM.INVOKEVIRTUAL,
        constantPool.addMethodRef(
            false, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
    code.add(JVM.RETURN);
    code.flush();

    return classFile.writeToArray();
  }
}
