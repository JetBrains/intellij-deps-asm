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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.test.AsmTest;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.CheckMethodAdapter;

/**
 * ClassRemapper tests.
 *
 * @author Eric Bruneton
 */
public class ClassRemapperTest extends AsmTest {

  @Test
  public void testRenameClass() {
    ClassNode classNode = new ClassNode();
    ClassRemapper classRemapper =
        new ClassRemapper(classNode, new SimpleRemapper("pkg/C", "new/pkg/C"));
    classRemapper.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "pkg/C", null, "java/lang/Object", null);
    assertEquals("new/pkg/C", classNode.name);
  }

  @Test
  public void testRenameModuleHashes() {
    ClassNode classNode = new ClassNode();
    ClassRemapper classRemapper =
        new ClassRemapper(
            classNode,
            new Remapper() {

              @Override
              public String mapModuleName(String name) {
                return "new." + name;
              }
            });
    classRemapper.visit(Opcodes.V1_5, Opcodes.ACC_PUBLIC, "C", null, "java/lang/Object", null);
    classRemapper.visitAttribute(
        new ModuleHashesAttribute("algorithm", Arrays.asList("pkg.C"), Arrays.asList(new byte[0])));
    assertEquals("C", classNode.name);
    assertEquals("new.pkg.C", ((ModuleHashesAttribute) classNode.attrs.get(0)).modules.get(0));
  }

  /** Tests that classes transformed with a ClassRemapper can be loaded and instantiated. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testRemapLoadAndInstantiate(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassReader classReader = new ClassReader(classParameter.getBytes());
    ClassWriter classWriter = new ClassWriter(0);
    UpperCaseRemapper upperCaseRemapper = new UpperCaseRemapper(classParameter.getInternalName());

    ClassRemapper classRemapper =
        new ClassRemapper(apiParameter.value(), classWriter, upperCaseRemapper);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classRemapper, 0));
      return;
    }
    classReader.accept(classRemapper, 0);
    byte[] classFile = classWriter.toByteArray();
    assertThat(() -> loadAndInstantiate(upperCaseRemapper.getRemappedClassName(), classFile))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  /**
   * Tests that classes transformed with a ClassNode and ClassRemapper can be loaded and
   * instantiated.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testRemapLoadAndInstantiateWithTreeApi(
      final PrecompiledClass classParameter, final Api apiParameter) {
    ClassNode classNode = new ClassNode();
    new ClassReader(classParameter.getBytes()).accept(classNode, 0);

    ClassWriter classWriter = new ClassWriter(0);
    UpperCaseRemapper upperCaseRemapper = new UpperCaseRemapper(classParameter.getInternalName());
    ClassRemapper classRemapper =
        new ClassRemapper(apiParameter.value(), classWriter, upperCaseRemapper);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classNode.accept(classRemapper));
      return;
    }
    classNode.accept(classRemapper);
    byte[] classFile = classWriter.toByteArray();
    assertThat(() -> loadAndInstantiate(upperCaseRemapper.getRemappedClassName(), classFile))
        .succeedsOrThrows(UnsupportedClassVersionError.class)
        .when(classParameter.isMoreRecentThanCurrentJdk());
  }

  static class UpperCaseRemapper extends Remapper {

    private final String internalClassName;
    private final String remappedInternalClassName;

    UpperCaseRemapper(final String internalClassName) {
      this.internalClassName = internalClassName;
      this.remappedInternalClassName =
          internalClassName.equals("module-info")
              ? internalClassName
              : internalClassName.toUpperCase();
    }

    String getRemappedClassName() {
      return remappedInternalClassName.replace('/', '.');
    }

    @Override
    public String mapDesc(final String descriptor) {
      checkDescriptor(descriptor);
      return super.mapDesc(descriptor);
    }

    @Override
    public String mapType(final String type) {
      if (type != null && !type.equals("module-info")) {
        checkInternalName(type);
      }
      return super.mapType(type);
    }

    @Override
    public String mapMethodName(final String owner, final String name, final String descriptor) {
      if (name.equals("<init>") || name.equals("<clinit>")) {
        return name;
      }
      return owner.equals(internalClassName) ? name.toUpperCase() : name;
    }

    @Override
    public String mapInvokeDynamicMethodName(final String name, final String descriptor) {
      return name.toUpperCase();
    }

    @Override
    public String mapFieldName(final String owner, final String name, final String descriptor) {
      return owner.equals(internalClassName) ? name.toUpperCase() : name;
    }

    @Override
    public String map(final String typeName) {
      return typeName.equals(internalClassName) ? remappedInternalClassName : typeName;
    }
  }

  private static void checkDescriptor(final String descriptor) {
    CheckMethodAdapter checkMethodAdapter = new CheckMethodAdapter(null);
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitFieldInsn(Opcodes.GETFIELD, "Owner", "name", descriptor);
  }

  private static void checkInternalName(final String internalName) {
    CheckMethodAdapter checkMethodAdapter = new CheckMethodAdapter(null);
    checkMethodAdapter.visitCode();
    checkMethodAdapter.visitFieldInsn(Opcodes.GETFIELD, internalName, "name", "I");
  }
}
