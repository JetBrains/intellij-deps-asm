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

import java.lang.reflect.Modifier;
import net.sf.jiapi.reflect.InstructionFactory;
import net.sf.jiapi.reflect.InstructionList;
import net.sf.jiapi.reflect.JiapiClass;
import net.sf.jiapi.reflect.JiapiMethod;
import net.sf.jiapi.reflect.Signature;

/**
 * A "Hello World!" class generator using the Jiapi library.
 *
 * @author Eric Bruneton
 */
public class JiapiGenerator extends Generator {

  private static final Signature EMPTY_SIGNATURE = new Signature("()V");
  private static final Signature MAIN_SIGNATURE = new Signature("([Ljava/lang/String;)V");
  private static final Signature PRINTLN_SIGNATURE = new Signature("(Ljava/lang/String;)V");

  @Override
  public byte[] generateClass() {
    try {
      JiapiClass jiapiClass = JiapiClass.createClass("HelloWorld");

      // No API to set SourceFile!

      JiapiMethod method = jiapiClass.addMethod(Modifier.PUBLIC, "<init>", EMPTY_SIGNATURE);
      InstructionList insnList = method.getInstructionList();
      InstructionFactory insnFactory = insnList.getInstructionFactory();
      insnList.add(insnFactory.aload(0));
      insnList.add(insnFactory.invoke(0, "java/lang/Object", "<init>", EMPTY_SIGNATURE));
      insnList.add(insnFactory.returnMethod(method));

      method = jiapiClass.addMethod(Modifier.PUBLIC | Modifier.STATIC, "main", MAIN_SIGNATURE);
      insnList = method.getInstructionList();
      insnFactory = insnList.getInstructionFactory();
      insnList.add(
          insnFactory.getField(
              Modifier.STATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
      insnList.add(insnFactory.pushConstant("Hello world!"));
      insnList.add(insnFactory.invoke(0, "java/io/PrintStream", "println", PRINTLN_SIGNATURE));
      insnList.add(insnFactory.returnMethod(method));

      return jiapiClass.getByteCode();
    } catch (Exception e) {
      throw new RuntimeException("Class generation failed", e);
    }
  }
}
