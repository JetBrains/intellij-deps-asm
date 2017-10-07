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
package org.objectweb.asm.tree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassNode tests.
 *
 * @author Eric Bruneton
 */
public class ClassNodeTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with all the apis. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM4, Api.ASM5, Api.ASM6);
  }

  /** Tests that classes are unchanged with a ClassReader->ClassNode->ClassWriter transform. */
  @Test
  public void testReadAndWrite() {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);

    ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(classWriter);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that {@link ClassNode.check()} throws an exception for classes that contain elements more
   * recent than the ASM API version.
   */
  @Test
  public void testCheck() {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);
    if (classParameter.isMoreRecentThan(apiParameter)) {
      thrown.expect(RuntimeException.class);
    }
    classNode.check(apiParameter.value());
  }

  /**
   * Tests that classes are unchanged with a ClassReader->ClassNode->ClassWriter transform, when all
   * instructions are cloned.
   */
  @Test
  public void testReadCloneAndWrite() {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);

    for (MethodNode methodNode : classNode.methods) {
      Map<LabelNode, LabelNode> labelCloneMap =
          new HashMap<LabelNode, LabelNode>() {
            @Override
            public LabelNode get(final Object o) {
              return (LabelNode) o;
            }
          };
      Iterator<AbstractInsnNode> insnIterator = methodNode.instructions.iterator();
      while (insnIterator.hasNext()) {
        AbstractInsnNode insn = insnIterator.next();
        methodNode.instructions.set(insn, insn.clone(labelCloneMap));
      }
    }
    ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(classWriter);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /** Tests that ClassNode accepts visitors that remove class elements. */
  @Test
  public void testRemoveMembers() {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);

    ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(new RemoveMembersClassVisitor(apiParameter.value(), classWriter));
    ClassWriter expectedClassWriter = new ClassWriter(0);
    classReader.accept(new RemoveMembersClassVisitor(apiParameter.value(), expectedClassWriter), 0);
    assertThatClass(classWriter.toByteArray()).isEqualTo(expectedClassWriter.toByteArray());
  }

  private static Attribute[] attributes() {
    return new Attribute[] {new Comment(), new CodeComment()};
  }

  private static class RemoveMembersClassVisitor extends ClassVisitor {

    RemoveMembersClassVisitor(int api, ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
      return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        int typeRef, TypePath typePath, String desc, boolean visible) {
      return null;
    }

    @Override
    public FieldVisitor visitField(
        int access, String name, String desc, String signature, Object value) {
      return null;
    }

    @Override
    public MethodVisitor visitMethod(
        int access, String name, String desc, String signature, String[] exceptions) {
      return null;
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
      return null;
    }

    @Override
    public void visitAttribute(Attribute attr) {}
  }
}
