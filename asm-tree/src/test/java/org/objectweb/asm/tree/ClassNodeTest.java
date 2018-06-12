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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.objectweb.asm.test.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.test.AsmTest;

/**
 * ClassNode tests.
 *
 * @author Eric Bruneton
 */
public class ClassNodeTest extends AsmTest implements Opcodes {

  @Test
  public void testClassNode() {
    ClassNode classNode = new ClassNode();
    assertTrue(classNode.interfaces.isEmpty());
    assertTrue(classNode.innerClasses.isEmpty());
    assertTrue(classNode.fields.isEmpty());
    assertTrue(classNode.methods.isEmpty());

    assertThrows(IllegalStateException.class, () -> new ClassNode() {});
  }

  @Test
  public void testModuleNode() {
    ModuleNode moduleNode = new ModuleNode("module", 123, "1.0");
    assertEquals("module", moduleNode.name);
    assertEquals(123, moduleNode.access);
    assertEquals("1.0", moduleNode.version);

    ModuleNode otherModuleNode =
        new ModuleNode(
            Opcodes.ASM7_EXPERIMENTAL, "otherModule", 456, "2.0", null, null, null, null, null);
    moduleNode.accept(
        new ClassVisitor(Opcodes.ASM7_EXPERIMENTAL) {
          @Override
          public ModuleVisitor visitModule(
              final String name, final int access, final String version) {
            return otherModuleNode;
          }
        });

    assertThrows(IllegalStateException.class, () -> new ModuleNode("module", 123, "1.0") {});
  }

  @Test
  public void testFieldNode() {
    FieldNode fieldNode = new FieldNode(123, "field", "I", null, null);
    assertEquals(123, fieldNode.access);
    assertEquals("field", fieldNode.name);
    assertEquals("I", fieldNode.desc);
    assertThrows(
        IllegalStateException.class, () -> new FieldNode(123, "field", "I", null, null) {});
  }

  @Test
  public void testMethodNode() {
    MethodNode methodNode = new MethodNode(123, "method", "()V", null, null);
    assertEquals(123, methodNode.access);
    assertEquals("method", methodNode.name);
    assertEquals("()V", methodNode.desc);
    assertThrows(IllegalStateException.class, () -> new MethodNode() {});
  }

  @Test
  public void testAnnotationNode() {
    AnnotationNode annotationNode = new AnnotationNode("LI;");
    assertEquals("LI;", annotationNode.desc);
    assertThrows(IllegalStateException.class, () -> new AnnotationNode("LI;") {});

    annotationNode.visit("bytes", new byte[] {0, 1});
    annotationNode.visit("booleans", new boolean[] {false, true});
    annotationNode.visit("shorts", new short[] {0, 1});
    annotationNode.visit("chars", new char[] {'0', '1'});
    annotationNode.visit("ints", new int[] {0, 1});
    annotationNode.visit("longs", new long[] {0L, 1L});
    annotationNode.visit("floats", new float[] {0.0f, 1.0f});
    annotationNode.visit("doubles", new double[] {0.0, 1.0});
    annotationNode.visit("string", "value");
    annotationNode.visitAnnotation("annotation", "Lpkg/Annotation;");

    assertEquals("bytes", annotationNode.values.get(0));
    assertEquals(Arrays.asList(new Byte[] {0, 1}), annotationNode.values.get(1));
    assertEquals("booleans", annotationNode.values.get(2));
    assertEquals(Arrays.asList(new Boolean[] {false, true}), annotationNode.values.get(3));
    assertEquals("shorts", annotationNode.values.get(4));
    assertEquals(Arrays.asList(new Short[] {0, 1}), annotationNode.values.get(5));
    assertEquals("chars", annotationNode.values.get(6));
    assertEquals(Arrays.asList(new Character[] {'0', '1'}), annotationNode.values.get(7));
    assertEquals("ints", annotationNode.values.get(8));
    assertEquals(Arrays.asList(new Integer[] {0, 1}), annotationNode.values.get(9));
    assertEquals("longs", annotationNode.values.get(10));
    assertEquals(Arrays.asList(new Long[] {0L, 1L}), annotationNode.values.get(11));
    assertEquals("floats", annotationNode.values.get(12));
    assertEquals(Arrays.asList(new Float[] {0.0f, 1.0f}), annotationNode.values.get(13));
    assertEquals("doubles", annotationNode.values.get(14));
    assertEquals(Arrays.asList(new Double[] {0.0, 1.0}), annotationNode.values.get(15));
    assertEquals("string", annotationNode.values.get(16));
    assertEquals("value", annotationNode.values.get(17));

    annotationNode.accept(
        new AnnotationVisitor(Opcodes.ASM7_EXPERIMENTAL) {

          @Override
          public AnnotationVisitor visitAnnotation(final String name, final String descriptor) {
            return null;
          }

          @Override
          public AnnotationVisitor visitArray(final String name) {
            return null;
          }
        });
  }

  @Test
  public void testTypeAnnotationNode() {
    TypePath typePath = TypePath.fromString("[");
    TypeAnnotationNode typeAnnotationNode = new TypeAnnotationNode(123, typePath, "LI;");
    assertEquals(123, typeAnnotationNode.typeRef);
    assertEquals(typePath, typeAnnotationNode.typePath);
    assertEquals("LI;", typeAnnotationNode.desc);
    assertThrows(
        IllegalStateException.class, () -> new TypeAnnotationNode(123, typePath, "LI;") {});
  }

  @Test
  public void testFrameNode() {
    Object[] locals = new Object[] {"l"};
    Object[] stack = new Object[] {"s", "t"};
    FrameNode frameNode = new FrameNode(F_FULL, 1, locals, 2, stack);
    assertEquals(AbstractInsnNode.FRAME, frameNode.getType());
    assertEquals(F_FULL, frameNode.type);
    assertEquals(Arrays.asList(locals), frameNode.local);
    assertEquals(Arrays.asList(stack), frameNode.stack);
  }

  @Test
  public void testInsnNode() {
    InsnNode insnNode = new InsnNode(NOP);
    assertEquals(AbstractInsnNode.INSN, insnNode.getType());
    assertEquals(insnNode.getOpcode(), NOP);
  }

  @Test
  public void testIntInsnNode() {
    IntInsnNode intInsnNode = new IntInsnNode(BIPUSH, 0);
    intInsnNode.setOpcode(SIPUSH);
    assertEquals(SIPUSH, intInsnNode.getOpcode());
    assertEquals(AbstractInsnNode.INT_INSN, intInsnNode.getType());
  }

  @Test
  public void testVarInsnNode() {
    VarInsnNode varInsnNode = new VarInsnNode(ALOAD, 123);
    assertEquals(ALOAD, varInsnNode.getOpcode());
    assertEquals(AbstractInsnNode.VAR_INSN, varInsnNode.getType());
    assertEquals(123, varInsnNode.var);

    varInsnNode.setOpcode(ASTORE);
    assertEquals(ASTORE, varInsnNode.getOpcode());
  }

  @Test
  public void testTypeInsnNode() {
    TypeInsnNode typeInsnNode = new TypeInsnNode(NEW, "java/lang/Object");
    assertEquals(NEW, typeInsnNode.getOpcode());
    assertEquals(AbstractInsnNode.TYPE_INSN, typeInsnNode.getType());
    assertEquals("java/lang/Object", typeInsnNode.desc);

    typeInsnNode.setOpcode(CHECKCAST);
    assertEquals(CHECKCAST, typeInsnNode.getOpcode());
  }

  @Test
  public void testFieldInsnNode() {
    FieldInsnNode fieldInsnNode = new FieldInsnNode(GETSTATIC, "owner", "name", "I");
    assertEquals(AbstractInsnNode.FIELD_INSN, fieldInsnNode.getType());
    assertEquals(GETSTATIC, fieldInsnNode.getOpcode());
    assertEquals("owner", fieldInsnNode.owner);
    assertEquals("name", fieldInsnNode.name);
    assertEquals("I", fieldInsnNode.desc);

    fieldInsnNode.setOpcode(PUTSTATIC);
    assertEquals(PUTSTATIC, fieldInsnNode.getOpcode());
  }

  @Test
  public void testMethodInsnNodeDeprecated() {
    MethodInsnNode methodInsnNode = new MethodInsnNode(INVOKESTATIC, "owner", "name", "()I");
    assertEquals(AbstractInsnNode.METHOD_INSN, methodInsnNode.getType());
    assertEquals(INVOKESTATIC, methodInsnNode.getOpcode());
    assertEquals(false, methodInsnNode.itf);

    methodInsnNode = new MethodInsnNode(INVOKEINTERFACE, "owner", "name", "()I");
    assertEquals(AbstractInsnNode.METHOD_INSN, methodInsnNode.getType());
    assertEquals(INVOKEINTERFACE, methodInsnNode.getOpcode());
    assertEquals(true, methodInsnNode.itf);
  }

  @Test
  public void testMethodInsnNode() {
    MethodInsnNode methodInsnNode = new MethodInsnNode(INVOKESTATIC, "owner", "name", "()I", false);
    assertEquals(AbstractInsnNode.METHOD_INSN, methodInsnNode.getType());
    assertEquals(INVOKESTATIC, methodInsnNode.getOpcode());
    assertEquals("owner", methodInsnNode.owner);
    assertEquals("name", methodInsnNode.name);
    assertEquals("()I", methodInsnNode.desc);
    assertEquals(false, methodInsnNode.itf);

    methodInsnNode.setOpcode(INVOKESPECIAL);
    assertEquals(INVOKESPECIAL, methodInsnNode.getOpcode());
  }

  @Test
  public void testInvokeDynamicInsnNode() {
    Handle handle = new Handle(Opcodes.H_INVOKESTATIC, "owner", "name", "()V", false);
    Object[] bootstrapMethodArguments = new Object[] {"s"};
    InvokeDynamicInsnNode invokeDynamicInsnNode =
        new InvokeDynamicInsnNode("name", "()V", handle, bootstrapMethodArguments);

    assertEquals(INVOKEDYNAMIC, invokeDynamicInsnNode.getOpcode());
    assertEquals(AbstractInsnNode.INVOKE_DYNAMIC_INSN, invokeDynamicInsnNode.getType());
    assertEquals("name", invokeDynamicInsnNode.name);
    assertEquals("()V", invokeDynamicInsnNode.desc);
    assertEquals(handle, invokeDynamicInsnNode.bsm);
    assertEquals(bootstrapMethodArguments, invokeDynamicInsnNode.bsmArgs);
  }

  @Test
  public void testJumpInsnNode() {
    LabelNode labelNode = new LabelNode();
    JumpInsnNode jumpInsnNode = new JumpInsnNode(GOTO, labelNode);
    assertEquals(GOTO, jumpInsnNode.getOpcode());
    assertEquals(AbstractInsnNode.JUMP_INSN, jumpInsnNode.getType());
    assertEquals(labelNode, jumpInsnNode.label);

    jumpInsnNode.setOpcode(IFEQ);
    assertEquals(IFEQ, jumpInsnNode.getOpcode());
  }

  @Test
  public void testLabelNode() {
    LabelNode labelNode = new LabelNode();
    assertEquals(AbstractInsnNode.LABEL, labelNode.getType());
    assertNotNull(labelNode.getLabel());

    Label label = new Label();
    labelNode = new LabelNode(label);
    assertEquals(label, labelNode.getLabel());
  }

  @Test
  public void testIincInsnNode() {
    IincInsnNode iincnInsnNode = new IincInsnNode(1, 2);
    assertEquals(AbstractInsnNode.IINC_INSN, iincnInsnNode.getType());
    assertEquals(1, iincnInsnNode.var);
    assertEquals(2, iincnInsnNode.incr);
  }

  @Test
  public void testLdcInsnNode() {
    LdcInsnNode ldcInsnNode = new LdcInsnNode("s");
    assertEquals(AbstractInsnNode.LDC_INSN, ldcInsnNode.getType());
    assertEquals("s", ldcInsnNode.cst);
  }

  @Test
  public void testLookupSwitchInsnNode() {
    LabelNode dflt = new LabelNode();
    int[] keys = new int[] {1};
    LabelNode[] labels = new LabelNode[] {new LabelNode()};
    LookupSwitchInsnNode lookupSwitchInsnNode = new LookupSwitchInsnNode(dflt, keys, labels);
    assertEquals(AbstractInsnNode.LOOKUPSWITCH_INSN, lookupSwitchInsnNode.getType());
    assertEquals(dflt, lookupSwitchInsnNode.dflt);
    assertEquals(Arrays.asList(new Integer[] {1}), lookupSwitchInsnNode.keys);
    assertEquals(Arrays.asList(labels), lookupSwitchInsnNode.labels);
  }

  @Test
  public void testTableSwitchInsnNode() {
    LabelNode dflt = new LabelNode();
    LabelNode[] labels = new LabelNode[] {new LabelNode()};
    TableSwitchInsnNode tableSwitchInsnNode = new TableSwitchInsnNode(0, 1, dflt, labels);
    assertEquals(AbstractInsnNode.TABLESWITCH_INSN, tableSwitchInsnNode.getType());
    assertEquals(0, tableSwitchInsnNode.min);
    assertEquals(1, tableSwitchInsnNode.max);
    assertEquals(dflt, tableSwitchInsnNode.dflt);
    assertEquals(Arrays.asList(labels), tableSwitchInsnNode.labels);
  }

  @Test
  public void testMultiANewArrayInsnNode() {
    MultiANewArrayInsnNode multiANewArrayInsnNode = new MultiANewArrayInsnNode("[[I", 2);
    assertEquals(AbstractInsnNode.MULTIANEWARRAY_INSN, multiANewArrayInsnNode.getType());
    assertEquals("[[I", multiANewArrayInsnNode.desc);
    assertEquals(2, multiANewArrayInsnNode.dims);
  }

  @Test
  public void testLineNumberNode() {
    LabelNode labelNode = new LabelNode();
    LineNumberNode lineNumberNode = new LineNumberNode(42, labelNode);
    assertEquals(42, lineNumberNode.line);
    assertEquals(labelNode, lineNumberNode.start);
    assertEquals(AbstractInsnNode.LINE, lineNumberNode.getType());
  }

  @Test
  public void testCloneMethod() {
    MethodNode methodNode = new MethodNode();
    Label label0 = new Label();
    Label label1 = new Label();
    methodNode.visitCode();
    methodNode.visitLabel(label0);
    methodNode.visitInsn(Opcodes.NOP);
    methodNode.visitLabel(label1);
    methodNode.visitEnd();
    MethodNode methodNode1 = new MethodNode();
    methodNode.accept(methodNode1);
    methodNode.accept(methodNode1);
    assertEquals(6, methodNode1.instructions.size());
  }

  /** Tests that classes are unchanged with a ClassReader->ClassNode->ClassWriter transform. */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testReadAndWrite(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);

    ClassWriter classWriter = new ClassWriter(0);
    classNode.accept(classWriter);
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }

  /**
   * Tests that {@link ClassNode#check} throws an exception for classes that contain elements more
   * recent than the ASM API version.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testCheck(final PrecompiledClass classParameter, final Api apiParameter) {
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassNode classNode = new ClassNode(apiParameter.value());
    classReader.accept(classNode, attributes(), 0);
    assertThat(() -> classNode.check(apiParameter.value()))
        .succeedsOrThrows(RuntimeException.class)
        .when(classParameter.isMoreRecentThan(apiParameter));
  }

  /**
   * Tests that classes are unchanged with a ClassReader->ClassNode->ClassWriter transform, when all
   * instructions are cloned.
   */
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  @SuppressWarnings("serial")
  public void testReadCloneAndWrite(final PrecompiledClass classParameter, final Api apiParameter) {
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
  @ParameterizedTest
  @MethodSource(ALL_CLASSES_AND_ALL_APIS)
  public void testRemoveMembers(final PrecompiledClass classParameter, final Api apiParameter) {
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

    RemoveMembersClassVisitor(final int api, final ClassVisitor classVisitor) {
      super(api, classVisitor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(final String descriptor, final boolean visible) {
      return null;
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(
        final int typeRef,
        final TypePath typePath,
        final String descriptor,
        final boolean visible) {
      return null;
    }

    @Override
    public FieldVisitor visitField(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final Object value) {
      return null;
    }

    @Override
    public MethodVisitor visitMethod(
        final int access,
        final String name,
        final String descriptor,
        final String signature,
        final String[] exceptions) {
      return null;
    }

    @Override
    public ModuleVisitor visitModule(final String name, final int access, final String version) {
      return null;
    }

    @Override
    public void visitNestHostExperimental(final String nestHost) {}

    @Override
    public void visitNestMemberExperimental(final String nestMember) {}

    @Override
    public void visitAttribute(final Attribute attribute) {}
  }
}
