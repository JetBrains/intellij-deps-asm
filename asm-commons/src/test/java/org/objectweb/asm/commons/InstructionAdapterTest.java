package org.objectweb.asm.commons;

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.test.AsmTest;

/**
 * InstructionAdapter tests.
 *
 * @author Eric Bruneton
 */
public class InstructionAdapterTest extends AsmTest {

  /** Tests that classes transformed with an InstructionAdapter are unchanged. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testAdaptInstructions(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    ClassVisitor classVisitor =
        new ClassVisitor(apiParameter.value(), classWriter) {

          @Override
          public MethodVisitor visitMethod(
              final int access,
              final String name,
              final String descriptor,
              final String signature,
              final String[] exceptions) {
            return new InstructionAdapter(
                api, super.visitMethod(access, name, descriptor, signature, exceptions)) {};
          }
        };
    if (classParameter.isMoreRecentThan(apiParameter)) {
      assertThrows(RuntimeException.class, () -> classReader.accept(classVisitor, attributes(), 0));
    } else {
      classReader.accept(classVisitor, attributes(), 0);
      assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
    }
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }
}
