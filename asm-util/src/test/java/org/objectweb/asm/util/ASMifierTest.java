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
package org.objectweb.asm.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import org.codehaus.janino.ClassLoaderIClassLoader;
import org.codehaus.janino.IClassLoader;
import org.codehaus.janino.Parser;
import org.codehaus.janino.Scanner;
import org.codehaus.janino.UnitCompiler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * ASMifier tests.
 *
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */
public class ASMifierTest extends AsmTest {

  private static final IClassLoader ICLASS_LOADER =
      new ClassLoaderIClassLoader(new URLClassLoader(new URL[0]));

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new ASMifier() {});
  }

  @Test
  public void testMain() throws IOException {
    PrintStream err = System.err;
    PrintStream out = System.out;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    System.setOut(new PrintStream(new ByteArrayOutputStream()));
    try {
      String thisClassName = getClass().getName();
      String thisClassFilePath =
          ClassLoader.getSystemResource(thisClassName.replace('.', '/') + ".class").getPath();
      ASMifier.main(new String[0]);
      ASMifier.main(new String[] {"-debug"});
      ASMifier.main(new String[] {thisClassName});
      ASMifier.main(new String[] {thisClassFilePath});
      ASMifier.main(new String[] {"-debug", thisClassName});
      ASMifier.main(new String[] {"java.lang.Object"});
      ASMifier.main(new String[] {"-debug", thisClassName, "extraArgument"});
      assertThrows(IOException.class, () -> ASMifier.main(new String[] {"DoNotExist.class"}));
      assertThrows(IOException.class, () -> ASMifier.main(new String[] {"do\\not\\exist"}));
    } finally {
      System.setErr(err);
      System.setOut(out);
    }
  }

  @Test
  public void testBackwardCompatibility() {
    ASMifier asmifier = new ASMifier();
    asmifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V");
    assertEquals(
        "classWriter.visitMethodInsn(INVOKESPECIAL, \"owner\", \"name\", \"()V\", false);\n",
        asmifier.getText().get(0));
  }

  @Test
  public void testBackwardCompatibilityAsm4() {
    ASMifier asmifier = new ASMifier(Opcodes.ASM4, "classWriter", 0) {};
    asmifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V");
    asmifier.visitMethodInsn(Opcodes.INVOKESPECIAL, "owner", "name", "()V", false);
    String expectedText =
        "classWriter.visitMethodInsn(INVOKESPECIAL, \"owner\", \"name\", \"()V\", false);\n";
    assertEquals(expectedText, asmifier.getText().get(0));
    assertEquals(expectedText, asmifier.getText().get(1));
  }

  /**
   * Tests that the code produced with an ASMifier compiles and generates the original class.
   *
   * @throws Exception
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testAsmifyCompileAndExecute(
      final PrecompiledClass classParameter, final Api apiParameter) throws Exception {
    byte[] classFile = classParameter.getBytes();
    if (classFile.length > Short.MAX_VALUE) return;

    // Produce the ASMified Java source code corresponding to classParameter.
    StringWriter stringWriter = new StringWriter();
    TraceClassVisitor classVisitor =
        new TraceClassVisitor(
            null,
            new ASMifier(apiParameter.value(), "classWriter", 0) {},
            new PrintWriter(stringWriter));
    new ClassReader(classFile)
        .accept(classVisitor, new Attribute[] {new Comment(), new CodeComment()}, 0);
    String asmifiedSource = stringWriter.toString();

    // Compile and execute this Java source code (skip JDK9 modules, Janino can't compile them).
    if (classParameter == PrecompiledClass.JDK9_MODULE) return;
    byte[] asmifiedClassFile = compile(classParameter.getName(), asmifiedSource);
    String asmifiedClassName = classParameter.getName() + "Dump";
    if (asmifiedClassName.indexOf('.') != -1) {
      asmifiedClassName = "asm." + asmifiedClassName;
    }
    Class<?> asmifiedClass =
        new TestClassLoader().defineClass(asmifiedClassName, asmifiedClassFile);
    Method dumpMethod = asmifiedClass.getMethod("dump");
    byte[] dumpClassFile = (byte[]) dumpMethod.invoke(null);

    assertThatClass(dumpClassFile).isEqualTo(classFile);
  }

  private static byte[] compile(final String name, final String source) throws Exception {
    Parser parser = new Parser(new Scanner(name, new StringReader(source)));
    UnitCompiler unitCompiler = new UnitCompiler(parser.parseCompilationUnit(), ICLASS_LOADER);
    return unitCompiler.compileUnit(true, true, true)[0].toByteArray();
  }

  private static class TestClassLoader extends ClassLoader {

    TestClassLoader() {}

    public Class<?> defineClass(final String name, final byte[] classFile) {
      return defineClass(name, classFile, 0, classFile.length);
    }
  }
}
