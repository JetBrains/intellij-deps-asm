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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * CheckModuleAdapter tests.
 *
 * @author Eric Bruneton
 */
public class CheckModuleAdapterTest {

  private CheckModuleAdapter checkModuleAdapter = new CheckModuleAdapter(null, /* open = */ false);

  @Test
  public void testConstructor() {
    assertThrows(
        IllegalStateException.class, () -> new CheckModuleAdapter(null, /* open = */ false) {});
  }

  @Test
  public void testNullArraysSupported() {
    checkModuleAdapter.visitExport("package", 0, (String[]) null);
    checkModuleAdapter.visitOpen("package", 0, (String[]) null);
  }

  @Test
  public void testIllegalOpen() {
    checkModuleAdapter = new CheckModuleAdapter(null, /* open = */ true);
    assertThrows(
        RuntimeException.class, () -> checkModuleAdapter.visitOpen("package", 0, (String[]) null));
  }

  @Test
  public void testNameAlreadyDeclared() {
    checkModuleAdapter.visitUse("service");
    assertThrows(RuntimeException.class, () -> checkModuleAdapter.visitUse("service"));
  }

  @Test
  public void testIllegalProvide() {
    assertThrows(RuntimeException.class, () -> checkModuleAdapter.visitProvide("service1"));
    assertThrows(
        RuntimeException.class, () -> checkModuleAdapter.visitProvide("service2", (String[]) null));
  }

  @Test
  public void testIllegalMemberVisitAfterEnd() {
    checkModuleAdapter.visitEnd();
    assertThrows(RuntimeException.class, () -> checkModuleAdapter.visitUse("service"));
  }

  @Test // see issue #317804
  public void testRequireJavaBaseTransitive() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor moduleVisitor = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(
        IllegalArgumentException.class,
        () -> moduleVisitor.visitRequire("java.base", Opcodes.ACC_TRANSITIVE, null));
  }

  @Test // see issue #317804
  public void testRequireJavaBaseStaticPhase() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor moduleVisitor = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(
        IllegalArgumentException.class,
        () -> moduleVisitor.visitRequire("java.base", Opcodes.ACC_STATIC_PHASE, null));
  }

  @Test // see issue #317804
  public void testRequireJavaBaseTransitiveAndStaticPhase() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor moduleVisitor = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(
        IllegalArgumentException.class,
        () ->
            moduleVisitor.visitRequire(
                "java.base", Opcodes.ACC_TRANSITIVE | Opcodes.ACC_STATIC_PHASE, null));
  }

  @Test // see issue #317804
  public void testRequireJavaBaseTransitiveOrStaticPhaseAreIgnoredUnderJVMS9() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V9, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor moduleVisitor = adapter.visitModule("org.objectweb.asm", 0, null);
    moduleVisitor.visitRequire(
        "java.base", Opcodes.ACC_TRANSITIVE | Opcodes.ACC_STATIC_PHASE, null);
    moduleVisitor.visitEnd();
    adapter.visitEnd();
  }
}
