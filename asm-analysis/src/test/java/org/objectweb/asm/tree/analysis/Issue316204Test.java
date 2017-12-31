package org.objectweb.asm.tree.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * Non-regression test for issue #316204.
 *
 * @author Eric Bruneton
 */
public class Issue316204Test {

  @Test
  public void testNonRegression() throws IOException, AnalyzerException {
    ClassReader classReader =
        new ClassReader(new FileInputStream("src/test/resources/Issue316204.class"));
    ClassNode classNode = new ClassNode();
    classReader.accept(classNode, 0);
    Analyzer<BasicValue> analyzer = new Analyzer<>(new BasicInterpreter());
    analyzer.analyze(classNode.name, getMethod(classNode, "basicStopBundles"));
    assertEquals("RIR..... ", analyzer.getFrames()[104].toString());
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
