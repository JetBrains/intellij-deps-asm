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
package org.objectweb.asm.tree.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * BasicInterpreter tests.
 *
 * @author Eric Bruneton
 */
public class BasicInterpreterTest extends AsmTest {

  @Test
  public void testConstructor() {
    assertThrows(IllegalStateException.class, () -> new BasicInterpreter() {});
  }

  /**
   * Tests that stack map frames are correctly merged when a JSR instruction can be reached from two
   * different control flow paths, with different local variable types (#316204).
   *
   * @throws IOException
   * @throws AnalyzerException
   */
  @Test
  public void testMergeWithJsrReachableFromTwoDifferentPaths()
      throws IOException, AnalyzerException {
    ClassReader classReader =
        new ClassReader(new FileInputStream("src/test/resources/Issue316204.class"));
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode, 0);
    Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
    analyzer.analyze(classNode.name, getMethod(classNode, "basicStopBundles"));
    assertEquals("RIR..... ", analyzer.getFrames()[104].toString());
  }

  /**
   * Tests that the precompiled classes can be successfully analyzed with a BasicInterpreter.
   *
   * @throws AnalyzerException
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_LATEST_API)
  public void testAnalyze(final PrecompiledClass classParameter, final Api apiParameter)
      throws AnalyzerException {
    ClassNode classNode = new ClassNode();
    new ClassReader(classParameter.getBytes()).accept(classNode, 0);
    for (MethodNode methodNode : classNode.methods) {
      Analyzer<BasicValue> analyzer = new Analyzer<BasicValue>(new BasicInterpreter());
      analyzer.analyze(classNode.name, methodNode);
    }
  }

  private static MethodNode getMethod(final ClassNode classNode, final String name) {
    for (MethodNode methodNode : classNode.methods) {
      if (methodNode.name.equals(name)) {
        return methodNode;
      }
    }
    return null;
  }
}
