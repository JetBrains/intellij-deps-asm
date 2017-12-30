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

import org.mozilla.classfile.ByteCode;
import org.mozilla.classfile.ClassFileWriter;

/**
 * A "Hello World!" class generator using the Mozilla ClassFile library.
 *
 * @author Eric Bruneton
 */
public class MozillaClassFileGenerator extends Generator {

  @Override
  public byte[] generateClass() {
    ClassFileWriter classFileWriter =
        new ClassFileWriter("HelloWorld", "java/lang/Object", "HelloWorld.java");

    classFileWriter.startMethod("<init>", "()V", ClassFileWriter.ACC_PUBLIC);
    classFileWriter.addLoadThis();
    classFileWriter.addInvoke(ByteCode.INVOKESPECIAL, "java/lang/Object", "<init>", "()V");
    classFileWriter.add(ByteCode.RETURN);
    classFileWriter.stopMethod((short) 1);

    classFileWriter.startMethod("main", "()V", ClassFileWriter.ACC_PUBLIC);
    classFileWriter.add(ByteCode.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    classFileWriter.addPush("Hello world!");
    classFileWriter.addInvoke(
        ByteCode.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V");
    classFileWriter.add(ByteCode.RETURN);
    classFileWriter.stopMethod((short) 1);

    return classFileWriter.toByteArray();
  }
}
