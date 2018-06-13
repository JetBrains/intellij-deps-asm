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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ListIterator;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * InsnList tests.
 *
 * @author Eric Bruneton
 * @author Eugene Kuleshov
 */
public class InsnListTest {

  private InsnList list1;

  private InsnList list2;

  private InsnList list3Unchecked;

  private InsnNode insn1;

  private InsnNode insn2;

  @BeforeEach
  public void setUp() throws Exception {
    list1 = new CheckedInsnList();
    list2 = new CheckedInsnList();
    list3Unchecked = new InsnList();
    insn1 = new InsnNode(0);
    insn2 = new InsnNode(0);
    list2.add(insn1);
    list2.add(insn2);
  }

  void assertEqualInsnArrays(final AbstractInsnNode[] expected, final AbstractInsnNode[] value) {
    assertEquals(expected.length, value.length);
    for (int i = 0; i < value.length; ++i) {
      assertEquals(expected[i], value[i]);
    }
  }

  @Test
  public void testSize() {
    assertEquals(0, list1.size());
  }

  @Test
  public void testGetFirst() {
    assertEquals(null, list1.getFirst());
  }

  @Test
  public void testGetLast() {
    assertEquals(null, list1.getLast());
  }

  @Test
  public void testInvalidGet() {
    assertThrows(IndexOutOfBoundsException.class, () -> list1.get(0));
  }

  @Test
  public void testContains() {
    assertEquals(false, list1.contains(new InsnNode(0)));
  }

  @Test
  public void testIterator() {
    InsnNode insn = new InsnNode(0);

    // Iterate.
    ListIterator<AbstractInsnNode> iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(insn2, iterator.next());
    assertFalse(iterator.hasNext());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn2, iterator.previous());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn1, iterator.previous());
    assertFalse(iterator.hasPrevious());

    list2.add(insn);

    // Remove.
    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(insn2, iterator.next());
    assertTrue(iterator.hasNext());
    iterator.remove(); // Remove insn2.
    assertTrue(iterator.hasPrevious());
    assertTrue(iterator.hasNext());
    assertEquals(insn, iterator.next());
    assertFalse(iterator.hasNext());
    assertTrue(iterator.hasPrevious());

    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(insn, iterator.next());
    assertFalse(iterator.hasNext());

    list2.remove(insn);
    list2.insert(insn1, insn2);

    // Iterate after add.
    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    iterator.add(insn);
    assertEquals(insn2, iterator.next());

    list2.remove(insn);

    // Iterate backward after add.
    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    iterator.add(insn);
    assertEquals(insn, iterator.previous());
    assertEquals(insn, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(insn2, iterator.next());
    assertFalse(iterator.hasNext());

    list2.remove(insn);

    // Iterate backward after set.
    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    iterator.set(insn);
    assertEquals(insn, iterator.previous());
    assertEquals(insn, iterator.next());
    assertTrue(iterator.hasNext());

    list2.remove(insn);
    list2.insertBefore(insn2, insn1);

    // Iterate after add.
    iterator = list2.iterator();
    assertTrue(iterator.hasNext());
    assertEquals(insn1, iterator.next());
    iterator.set(insn);
    assertEquals(insn2, iterator.next());
  }

  @Test
  public void testIterator2() {
    ListIterator<AbstractInsnNode> iterator = list2.iterator(list2.size());

    assertFalse(iterator.hasNext());
    assertTrue(iterator.hasPrevious());
    assertEquals(1, iterator.previousIndex());
    assertEquals(insn2, iterator.previous());
    assertTrue(iterator.hasPrevious());
    assertEquals(0, iterator.previousIndex());
    assertEquals(insn1, iterator.previous());
    assertFalse(iterator.hasPrevious());

    assertEquals(-1, iterator.previousIndex());

    assertTrue(iterator.hasNext());
    assertEquals(0, iterator.nextIndex());
    assertEquals(insn1, iterator.next());
    assertTrue(iterator.hasNext());
    assertEquals(1, iterator.nextIndex());

    InsnNode insn = new InsnNode(0);
    iterator.add(insn);

    assertEquals(2, iterator.nextIndex());
    assertEquals(insn2, iterator.next());
    assertFalse(iterator.hasNext());
    assertEquals(3, iterator.nextIndex());
  }

  @Test
  public void testIterator3() {
    InsnNode insn = new InsnNode(0);
    list2.add(insn);

    // Iterate backward.
    ListIterator<AbstractInsnNode> iterator = list2.iterator(3);
    assertFalse(iterator.hasNext());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn, iterator.previous());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn2, iterator.previous());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn1, iterator.previous());
    assertFalse(iterator.hasPrevious());

    // Iterate backward after remove.
    iterator = list2.iterator(3);
    assertFalse(iterator.hasNext());
    assertTrue(iterator.hasPrevious());
    assertEquals(insn, iterator.previous());
    iterator.remove(); // Remove insn.
    assertTrue(iterator.hasPrevious());
    assertEquals(insn2, iterator.previous());
    iterator.remove(); // Remove insn2.
    assertTrue(iterator.hasPrevious());
    assertEquals(insn1, iterator.previous());
    assertFalse(iterator.hasPrevious());

    assertEquals(1, list2.size());
    assertEquals(insn1, list2.getFirst());
  }

  @Test
  public void testIterator4() {
    assertThrows(NoSuchElementException.class, () -> new InsnList().iterator().next());
  }

  @Test
  public void testIterator5() {
    // Call add on an empty list.
    ListIterator<AbstractInsnNode> it = list1.iterator();
    it.add(new InsnNode(0));
    assertEquals(1, list1.size());

    // Call set at the end of the list.
    it = list1.iterator();
    it.next();
    it.set(new InsnNode(1));
    assertEquals(1, list1.size());
    assertEquals(1, list1.get(0).opcode);

    // Call add at the end of the list.
    it = list2.iterator();
    it.next();
    it.next();
    it.add(new InsnNode(0));
    assertEquals(3, list2.size());

    // Call previous, then set and next.
    it = list2.iterator();
    it.next();
    it.next();
    it.previous();
    it.set(new InsnNode(1));
    assertEquals(1, it.next().opcode);
  }

  @Test
  public void testInvalidIndexOf() {
    assertThrows(IllegalArgumentException.class, () -> list1.indexOf(new InsnNode(0)));
  }

  @Test
  public void testToArray() {
    assertEquals(0, list1.toArray().length);
  }

  @Test
  public void testInvalidSet() {
    assertThrows(IllegalArgumentException.class, () -> list1.set(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testSet() {
    list1.add(new InsnNode(0));
    AbstractInsnNode insn = new InsnNode(0);
    list1.set(list1.getFirst(), insn);
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());

    list1.remove(insn);
    list1.add(new InsnNode(0));

    list1.set(list1.get(0), insn);
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());

    list1.remove(insn);
    list1.add(new InsnNode(0));
    list1.add(new InsnNode(0));

    list1.set(list1.get(1), insn);
    assertEquals(2, list1.size());
    assertEquals(insn, list1.get(1));
  }

  @Test
  public void testSetNodeAssociatedWithAnotherList() {
    InsnNode insnNode = new InsnNode(0);
    list3Unchecked.add(insnNode);
    list3Unchecked.set(insnNode, insn1);
    ListIterator<AbstractInsnNode> iterator = list3Unchecked.iterator();
    iterator.next();
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testInvalidAdd() {
    assertThrows(IllegalArgumentException.class, () -> list1.add(insn1));
  }

  @Test
  public void testAddEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(0, list1.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn}, list1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testAddNonEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.add(insn);
    assertEquals(2, list1.size());
    assertEquals(insn, list1.getLast());
    assertEquals(1, list1.indexOf(insn));
    assertEquals(insn, list1.get(1));
    assertEquals(true, list1.contains(insn));
  }

  @Test
  public void testAddNodeAssociatedWithAnotherList() {
    list3Unchecked.add(insn1);
    ListIterator<AbstractInsnNode> iterator = list3Unchecked.iterator();
    iterator.next();
    assertFalse(iterator.hasNext());
  }

  @Test
  public void testAddEmptyList() {
    list1.add(new InsnList());
    assertEquals(0, list1.size());
    assertEquals(null, list1.getFirst());
    assertEquals(null, list1.getLast());
    assertEqualInsnArrays(new AbstractInsnNode[0], list1.toArray());
  }

  @Test
  public void testInvalidAddAll() {
    assertThrows(IllegalArgumentException.class, () -> list1.add(list1));
  }

  @Test
  public void testAddAllEmpty() {
    list1.add(list2);
    assertEquals(2, list1.size());
    assertEquals(insn1, list1.getFirst());
    assertEquals(insn2, list1.getLast());
    assertEquals(insn1, list1.get(0));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn1));
    assertEquals(1, list1.indexOf(insn2));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn2}, list1.toArray());
  }

  @Test
  public void testAddAllNonEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.add(list2);
    assertEquals(3, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn2, list1.getLast());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn));
    assertEquals(1, list1.indexOf(insn1));
    assertEquals(2, list1.indexOf(insn2));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn, insn1, insn2}, list1.toArray());
  }

  @Test
  public void testInvalidInsert() {
    assertThrows(IllegalArgumentException.class, () -> list1.insert(insn1));
  }

  @Test
  public void testInsertEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.insert(insn);
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(0, list1.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn}, list1.toArray());
  }

  @Test
  public void testInsertNonEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.insert(insn);
    assertEquals(2, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(0, list1.indexOf(insn));
  }

  @Test
  public void testInvalidInsertAll() {
    assertThrows(IllegalArgumentException.class, () -> list1.insert(list1));
  }

  @Test
  public void testInsertAllEmptyList() {
    list1.insert(new InsnList());
    assertEquals(0, list1.size());
    assertEquals(null, list1.getFirst());
    assertEquals(null, list1.getLast());
    assertEqualInsnArrays(new AbstractInsnNode[0], list1.toArray());
  }

  @Test
  public void testInsertAllEmpty() {
    list1.insert(list2);
    assertEquals(2, list1.size(), 2);
    assertEquals(insn1, list1.getFirst());
    assertEquals(insn2, list1.getLast());
    assertEquals(insn1, list1.get(0));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn1));
    assertEquals(1, list1.indexOf(insn2));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn2}, list1.toArray());
  }

  @Test
  public void testInsertAllNonEmpty() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.insert(list2);
    assertEquals(3, list1.size());
    assertEquals(insn1, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEquals(insn1, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn1));
    assertEquals(1, list1.indexOf(insn2));
    assertEquals(2, list1.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn2, insn}, list1.toArray());
  }

  @Test
  public void testInvalidInsert2() {
    assertThrows(
        IllegalArgumentException.class, () -> list1.insert(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testInsert2NotLast() {
    InsnNode insn = new InsnNode(0);
    list2.insert(insn1, insn);
    assertEquals(3, list2.size());
    assertEquals(insn1, list2.getFirst());
    assertEquals(insn2, list2.getLast());
    assertEquals(insn1, list2.get(0));
    assertEquals(true, list2.contains(insn));
    assertEquals(1, list2.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn, insn2}, list2.toArray());
  }

  @Test
  public void testInsert2Last() {
    InsnNode insn = new InsnNode(0);
    list2.insert(insn2, insn);
    assertEquals(3, list2.size());
    assertEquals(insn1, list2.getFirst());
    assertEquals(insn, list2.getLast());
    assertEquals(insn1, list2.get(0));
    assertEquals(true, list2.contains(insn));
    assertEquals(2, list2.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn2, insn}, list2.toArray());
  }

  @Test
  public void testInsertBefore() {
    InsnNode insn = new InsnNode(0);
    list2.insertBefore(insn2, insn);
    assertEquals(3, list2.size());
    assertEquals(insn1, list2.getFirst());
    assertEquals(insn2, list2.getLast());
    assertEquals(insn, list2.get(1));
    assertEquals(true, list2.contains(insn));
    assertEquals(1, list2.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn, insn2}, list2.toArray());
  }

  @Test
  public void testInsertBeforeFirst() {
    InsnNode insn = new InsnNode(0);
    list2.insertBefore(insn1, insn);
    assertEquals(3, list2.size());
    assertEquals(insn, list2.getFirst());
    assertEquals(insn2, list2.getLast());
    assertEquals(insn, list2.get(0));
    assertEquals(true, list2.contains(insn));
    assertEquals(0, list2.indexOf(insn));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn, insn1, insn2}, list2.toArray());
  }

  @Test
  public void testInvalidInsertBefore() {
    assertThrows(
        IllegalArgumentException.class, () -> list1.insertBefore(new InsnNode(0), new InsnNode(0)));
  }

  @Test
  public void testInvalidInsertAll2() {
    assertThrows(
        IllegalArgumentException.class, () -> list1.insert(new InsnNode(0), new InsnList()));
  }

  @Test
  public void testInsertAll2EmptyList() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.insert(insn, new InsnList());
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEqualInsnArrays(new AbstractInsnNode[] {insn}, list1.toArray());
  }

  @Test
  public void testInsertAll2NotLast() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.add(new InsnNode(0));
    list1.insert(insn, list2);
    assertEquals(4, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn));
    assertEquals(1, list1.indexOf(insn1));
    assertEquals(2, list1.indexOf(insn2));
  }

  @Test
  public void testInsertAll2Last() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.insert(insn, list2);
    assertEquals(3, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn2, list1.getLast());
    assertEquals(insn, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(0, list1.indexOf(insn));
    assertEquals(1, list1.indexOf(insn1));
    assertEquals(2, list1.indexOf(insn2));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn, insn1, insn2}, list1.toArray());
  }

  @Test
  public void testInvalidInsertBeforeAll() {
    assertThrows(
        IllegalArgumentException.class, () -> list1.insertBefore(new InsnNode(0), new InsnList()));
  }

  @Test
  public void testInsertBeforeAll2EmptyList() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.insertBefore(insn, new InsnList());
    assertEquals(1, list1.size());
    assertEquals(insn, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEqualInsnArrays(new AbstractInsnNode[] {insn}, list1.toArray());
  }

  @Test
  public void testInsertBeforeAll2NotLast() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.add(insn);
    list1.insertBefore(insn, list2);
    assertEquals(4, list1.size());
    assertEquals(insn1, list1.get(1));
    assertEquals(insn2, list1.get(2));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(3, list1.indexOf(insn));
    assertEquals(1, list1.indexOf(insn1));
    assertEquals(2, list1.indexOf(insn2));
  }

  @Test
  public void testInsertBeforeAll2First() {
    InsnNode insn = new InsnNode(0);
    list1.insert(insn);
    list1.insertBefore(insn, list2);
    assertEquals(3, list1.size());
    assertEquals(insn1, list1.getFirst());
    assertEquals(insn, list1.getLast());
    assertEquals(insn1, list1.get(0));
    assertEquals(true, list1.contains(insn));
    assertEquals(true, list1.contains(insn1));
    assertEquals(true, list1.contains(insn2));
    assertEquals(2, list1.indexOf(insn));
    assertEquals(0, list1.indexOf(insn1));
    assertEquals(1, list1.indexOf(insn2));
    assertEqualInsnArrays(new AbstractInsnNode[] {insn1, insn2, insn}, list1.toArray());
  }

  @Test
  public void testInvalidRemove() {
    assertThrows(IllegalArgumentException.class, () -> list1.remove(new InsnNode(0)));
  }

  @Test
  public void testRemoveSingle() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.remove(insn);
    assertEquals(0, list1.size());
    assertEquals(null, list1.getFirst());
    assertEquals(null, list1.getLast());
    assertEquals(false, list1.contains(insn));
    assertEqualInsnArrays(new AbstractInsnNode[0], list1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveFirst() {
    InsnNode insn = new InsnNode(0);
    list1.add(insn);
    list1.add(new InsnNode(0));
    list1.remove(insn);
    assertEquals(false, list1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveMiddle() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.add(insn);
    list1.add(new InsnNode(0));
    list1.remove(insn);
    assertEquals(false, list1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testRemoveLast() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.add(insn);
    list1.remove(insn);
    assertEquals(false, list1.contains(insn));
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testClear() {
    InsnNode insn = new InsnNode(0);
    list1.add(new InsnNode(0));
    list1.add(insn);
    list1.add(new InsnNode(0));
    list1.clear();
    assertEquals(0, list1.size());
    assertEquals(null, list1.getFirst());
    assertEquals(null, list1.getLast());
    assertEquals(false, list1.contains(insn));
    assertEqualInsnArrays(new AbstractInsnNode[0], list1.toArray());
    assertEquals(null, insn.getPrevious());
    assertEquals(null, insn.getNext());
  }

  @Test
  public void testAcceptor1() {
    list1.add(new InsnNode(55));
    list1.add(new InsnNode(77));

    final InsnList insnList = new InsnList();
    list1.accept(
        new MethodVisitor(Opcodes.ASM7_EXPERIMENTAL) {
          @Override
          public void visitInsn(final int opcode) {
            insnList.add(new InsnNode(opcode));
          }
        });

    assertEquals(55, insnList.get(0).opcode);
    assertEquals(77, insnList.get(1).opcode);
  }

  @Test
  public void testResetLabels() throws Exception {
    LabelNode labelNode = new LabelNode();

    list1.add(new InsnNode(55));
    list1.add(labelNode);
    list1.add(new InsnNode(55));

    Label label = labelNode.getLabel();
    assertNotNull(label);

    list1.resetLabels();

    assertNotSame(label, labelNode.getLabel());
  }

  /** An InsnList which checks that its methods are properly used. */
  static class CheckedInsnList extends InsnList {

    @Override
    public int indexOf(final AbstractInsnNode insnNode) {
      if (!contains(insnNode)) {
        throw new IllegalArgumentException();
      }
      return super.indexOf(insnNode);
    }

    @Override
    public void set(final AbstractInsnNode oldInsnNode, final AbstractInsnNode newInsnNode) {
      if (!(contains(oldInsnNode) && newInsnNode.index == -1)) {
        throw new IllegalArgumentException();
      }
      super.set(oldInsnNode, newInsnNode);
    }

    @Override
    public void add(final AbstractInsnNode insnNode) {
      if (insnNode.index != -1) {
        throw new IllegalArgumentException();
      }
      super.add(insnNode);
    }

    @Override
    public void add(final InsnList insnList) {
      if (insnList == this) {
        throw new IllegalArgumentException();
      }
      super.add(insnList);
    }

    @Override
    public void insert(final AbstractInsnNode insnNode) {
      if (insnNode.index != -1) {
        throw new IllegalArgumentException();
      }
      super.insert(insnNode);
    }

    @Override
    public void insert(final InsnList insnList) {
      if (insnList == this) {
        throw new IllegalArgumentException();
      }
      super.insert(insnList);
    }

    @Override
    public void insert(final AbstractInsnNode previousInsn, final AbstractInsnNode insnNode) {
      if (!(contains(previousInsn) && insnNode.index == -1)) {
        throw new IllegalArgumentException();
      }
      super.insert(previousInsn, insnNode);
    }

    @Override
    public void insert(final AbstractInsnNode previousInsn, final InsnList insnList) {
      if (!(contains(previousInsn) && insnList != this)) {
        throw new IllegalArgumentException();
      }
      super.insert(previousInsn, insnList);
    }

    @Override
    public void insertBefore(final AbstractInsnNode nextInsn, final AbstractInsnNode insnNode) {
      if (!(contains(nextInsn) && insnNode.index == -1)) {
        throw new IllegalArgumentException();
      }
      super.insertBefore(nextInsn, insnNode);
    }

    @Override
    public void insertBefore(final AbstractInsnNode nextInsn, final InsnList insnList) {
      if (!(contains(nextInsn) && insnList != this)) {
        throw new IllegalArgumentException();
      }
      super.insertBefore(nextInsn, insnList);
    }

    @Override
    public void remove(final AbstractInsnNode insnNode) {
      if (!contains(insnNode)) {
        throw new IllegalArgumentException();
      }
      super.remove(insnNode);
    }

    @Override
    public void clear() {
      removeAll(true);
      super.clear();
    }
  }
}
