// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (classType) 2000-2011 INRIA, France Telecom
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

import gnu.bytecode.Access;
import gnu.bytecode.ClassType;
import gnu.bytecode.CodeAttr;
import gnu.bytecode.Field;
import gnu.bytecode.Method;
import gnu.bytecode.Type;

/**
 * A "Hello World!" class generator using the GNU ByteCode library.
 *
 * @author Eric Bruneton
 */
public class GnuByteCodeGenerator extends Generator {

  private static final Method OBJECT_CONSTRUCTOR = Type.pointer_type.getDeclaredMethod("<init>", 0);
  private static final Field OUT_FIELD = ClassType.make("java.lang.System").getField("out");
  private static final Method PRINTLN_METHOD =
      ClassType.make("java.io.PrintStream")
          .getDeclaredMethod("println", new Type[] {Type.string_type});

  @Override
  public byte[] generateClass() {
    ClassType classType = new ClassType("HelloWorld");
    classType.setSuper("java.lang.Object");
    classType.setModifiers(Access.PUBLIC);
    classType.setSourceFile("HelloWorld.java");

    Method method = classType.addMethod("<init>", "()V", Access.PUBLIC);
    CodeAttr code = method.startCode();
    code.pushScope();
    code.emitPushThis();
    code.emitInvokeSpecial(OBJECT_CONSTRUCTOR);
    code.emitReturn();
    code.popScope();

    method = classType.addMethod("main", "([Ljava/lang/String;)V", Access.PUBLIC | Access.STATIC);
    code = method.startCode();
    code.pushScope();
    code.emitGetStatic(OUT_FIELD);
    code.emitPushString("Hello world!");
    code.emitInvokeVirtual(PRINTLN_METHOD);
    code.emitReturn();
    code.popScope();

    return classType.writeToArray();
  }
}
