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

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;

/**
 * AdviceAdapter tests.
 *
 * @author Eugene Kuleshov
 */
public class AdviceAdapterTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  @Test
  public void testEmptyAdviceAdapter() throws Exception {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter expectedClassWriter = new ClassWriter(0);
    ClassWriter actualClassWriter = new ClassWriter(0);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classReader.accept(
        new ReferenceClassAdapter(apiParameter.value(), expectedClassWriter),
        ClassReader.EXPAND_FRAMES);
    classReader.accept(
        new AdviceClassAdapter(apiParameter.value(), actualClassWriter), ClassReader.EXPAND_FRAMES);
    assertThatClass(actualClassWriter.toByteArray()).isEqualTo(expectedClassWriter.toByteArray());
  }

  private static class ReferenceClassAdapter extends ClassVisitor {

    ReferenceClassAdapter(final int api, final ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      if (mv == null || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) > 0) {
        return mv;
      }
      return new LocalVariablesSorter(access, desc, mv);
    }
  }

  private static class AdviceClassAdapter extends ClassVisitor {

    AdviceClassAdapter(final int api, final ClassVisitor cv) {
      super(api, cv);
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions) {
      MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
      if (mv == null || (access & (Opcodes.ACC_ABSTRACT | Opcodes.ACC_NATIVE)) > 0) {
        return mv;
      }
      return new AdviceAdapter(api, mv, access, name, desc) {};
    }
  }
}
