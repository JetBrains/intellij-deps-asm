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

import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.test.AsmTest;

public class ClassRemapperTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  /** Tests that classes transformed with a ClassRemapper can be loaded and instantiated. */
  @Test
  public void testRemapLoadAndInstantiate() {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter classWriter = new ClassWriter(0);
    Remapper upperCaseRemapper =
        new Remapper() {

          @Override
          public String mapMethodName(String owner, String name, String desc) {
            if (name.equals("<init>") || name.equals("<clinit>")) {
              return name;
            }
            return owner.equals(classParameter.getInternalName()) ? name.toUpperCase() : name;
          }

          @Override
          public String mapInvokeDynamicMethodName(String name, String desc) {
            return name.toUpperCase();
          }

          @Override
          public String mapFieldName(String owner, String name, String desc) {
            return owner.equals(classParameter.getInternalName()) ? name.toUpperCase() : name;
          }

          @Override
          public String map(String typeName) {
            return typeName.equals(classParameter.getInternalName())
                ? typeName.toUpperCase()
                : typeName;
          }
        };
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    } else if (classParameter.isMoreRecentThanCurrentJdk()) {
      thrown.expect(UnsupportedClassVersionError.class);
    }
    classReader.accept(new ClassRemapper(apiParameter.value(), classWriter, upperCaseRemapper), 0);
    assertTrue(
        loadAndInstantiate(classParameter.getName().toUpperCase(), classWriter.toByteArray()));
  }
}