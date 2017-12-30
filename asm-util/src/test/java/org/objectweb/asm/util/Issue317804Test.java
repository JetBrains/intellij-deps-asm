package org.objectweb.asm.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Tests for issue #317804.
 *
 * @author Remi Forax
 */
public class Issue317804Test {
  @Test
  public void testTransitive() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor mv = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(IllegalArgumentException.class, () -> mv.visitRequire("java.base", Opcodes.ACC_TRANSITIVE, null));
  }
  
  @Test
  public void testStaticPhase() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor mv = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(IllegalArgumentException.class, () -> mv.visitRequire("java.base", Opcodes.ACC_STATIC_PHASE, null));
  }
  
  @Test
  public void testBoth() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V10, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor mv = adapter.visitModule("org.objectweb.asm", 0, null);
    assertThrows(IllegalArgumentException.class, () -> mv.visitRequire("java.base", Opcodes.ACC_TRANSITIVE | Opcodes.ACC_STATIC_PHASE, null));
  }
  
  @Test
  public void testTransitiveOrStaticPhaseAreIgnoredUnderJVMS9() {
    CheckClassAdapter adapter = new CheckClassAdapter(null, false);
    adapter.visit(Opcodes.V9, Opcodes.ACC_PUBLIC, "module-info", null, null, null);
    ModuleVisitor mv = adapter.visitModule("org.objectweb.asm", 0, null);
    mv.visitRequire("java.base", Opcodes.ACC_TRANSITIVE | Opcodes.ACC_STATIC_PHASE, null);
    mv.visitEnd();
    adapter.visitEnd();
  }
}
