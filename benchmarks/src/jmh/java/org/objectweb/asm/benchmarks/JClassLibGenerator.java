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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.util.Arrays;
import org.gjt.jclasslib.bytecode.ImmediateByteInstruction;
import org.gjt.jclasslib.bytecode.ImmediateShortInstruction;
import org.gjt.jclasslib.bytecode.Opcodes;
import org.gjt.jclasslib.bytecode.SimpleInstruction;
import org.gjt.jclasslib.io.ByteCodeWriter;
import org.gjt.jclasslib.structures.AccessFlags;
import org.gjt.jclasslib.structures.AttributeInfo;
import org.gjt.jclasslib.structures.CPInfo;
import org.gjt.jclasslib.structures.ClassFile;
import org.gjt.jclasslib.structures.ConstantPoolUtil;
import org.gjt.jclasslib.structures.MethodInfo;
import org.gjt.jclasslib.structures.attributes.CodeAttribute;
import org.gjt.jclasslib.structures.attributes.SourceFileAttribute;
import org.gjt.jclasslib.structures.constants.ConstantStringInfo;

/**
 * A "Hello World!" class generator using the JClassLib library.
 *
 * @author Eric Bruneton
 */
public class JClassLibGenerator extends Generator {

  @Override
  public byte[] generateClass() {
    try {
      ClassFile classFile = new ClassFile();
      classFile.setConstantPool(new CPInfo[0]);
      ConstantPoolUtil.addConstantUTF8Info(classFile, "", 0); // dummy constant
      classFile.setMajorVersion(45);
      classFile.setMinorVersion(3);
      classFile.setAccessFlags(AccessFlags.ACC_PUBLIC);
      classFile.setThisClass(ConstantPoolUtil.addConstantClassInfo(classFile, "HelloWorld", 0));
      classFile.setSuperClass(
          ConstantPoolUtil.addConstantClassInfo(classFile, "java/lang/Object", 0));

      SourceFileAttribute sourceFileAttribute = new SourceFileAttribute();
      sourceFileAttribute.setAttributeNameIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, SourceFileAttribute.ATTRIBUTE_NAME, 0));
      sourceFileAttribute.setSourcefileIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, "HelloWorld.java", 0));

      MethodInfo methodInfo1 = new MethodInfo();
      methodInfo1.setAccessFlags(AccessFlags.ACC_PUBLIC);
      methodInfo1.setNameIndex(ConstantPoolUtil.addConstantUTF8Info(classFile, "<init>", 0));
      methodInfo1.setDescriptorIndex(ConstantPoolUtil.addConstantUTF8Info(classFile, "()V", 0));
      CodeAttribute codeAttribute1 = new CodeAttribute();
      codeAttribute1.setAttributeNameIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, CodeAttribute.ATTRIBUTE_NAME, 0));
      codeAttribute1.setCode(
          ByteCodeWriter.writeByteCode(
              Arrays.asList(
                  new SimpleInstruction(Opcodes.OPCODE_ALOAD_0),
                  new ImmediateShortInstruction(
                      Opcodes.OPCODE_INVOKESPECIAL,
                      ConstantPoolUtil.addConstantMethodrefInfo(
                          classFile, "java/lang/Object", "<init>", "()V", 0)),
                  new SimpleInstruction(Opcodes.OPCODE_RETURN))));
      codeAttribute1.setMaxStack(1);
      codeAttribute1.setMaxLocals(1);
      methodInfo1.setAttributes(new AttributeInfo[] {codeAttribute1});

      ConstantStringInfo constantStringInfo = new ConstantStringInfo();
      constantStringInfo.setStringIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, "Hello world!", 0));

      MethodInfo methodInfo2 = new MethodInfo();
      methodInfo2.setAccessFlags(AccessFlags.ACC_PUBLIC | AccessFlags.ACC_STATIC);
      methodInfo2.setNameIndex(ConstantPoolUtil.addConstantUTF8Info(classFile, "main", 0));
      methodInfo2.setDescriptorIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, "([Ljava/lang/String;)V", 0));
      CodeAttribute codeAttribute2 = new CodeAttribute();
      codeAttribute2.setAttributeNameIndex(
          ConstantPoolUtil.addConstantUTF8Info(classFile, CodeAttribute.ATTRIBUTE_NAME, 0));
      codeAttribute2.setCode(
          ByteCodeWriter.writeByteCode(
              Arrays.asList(
                  new ImmediateShortInstruction(
                      Opcodes.OPCODE_GETSTATIC,
                      ConstantPoolUtil.addConstantFieldrefInfo(
                          classFile, "java/lang/System", "out", "Ljava/io/PrintStream;", 0)),
                  new ImmediateByteInstruction(
                      Opcodes.OPCODE_LDC,
                      false,
                      ConstantPoolUtil.addConstantPoolEntry(classFile, constantStringInfo, 0)),
                  new ImmediateShortInstruction(
                      Opcodes.OPCODE_INVOKEVIRTUAL,
                      ConstantPoolUtil.addConstantMethodrefInfo(
                          classFile,
                          "java/io/PrintStream",
                          "println",
                          "(Ljava/lang/String;)V",
                          0)))));
      codeAttribute2.setMaxStack(2);
      codeAttribute2.setMaxLocals(1);
      methodInfo2.setAttributes(new AttributeInfo[] {codeAttribute2});

      classFile.setMethods(new MethodInfo[] {methodInfo1, methodInfo2});
      classFile.setAttributes(new AttributeInfo[] {sourceFileAttribute});

      ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
      DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
      classFile.write(dataOutputStream);
      dataOutputStream.close();
      return byteArrayOutputStream.toByteArray();
    } catch (Exception e) {
      throw new RuntimeException("Class generation failed", e);
    }
  }
}
