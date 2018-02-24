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
import static org.objectweb.asm.commons.GeneratorAdapter.GE;
import static org.objectweb.asm.commons.GeneratorAdapter.LE;

import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

/**
 * GeneratorAdapter tests. TODO: add unit tests for all methods.
 *
 * @author Eric Bruneton
 */
public class GeneratorAdapterTest {

  @Test
  public void testIfCmp() {
    assertEquals("IF_ICMPGE L0", new Generator().ifCmp(Type.INT_TYPE, GE, new Label()));
    assertEquals("LCMP IFGE L0", new Generator().ifCmp(Type.LONG_TYPE, GE, new Label()));
    assertEquals("FCMPL IFGE L0", new Generator().ifCmp(Type.FLOAT_TYPE, GE, new Label()));
    assertEquals("FCMPG IFLE L0", new Generator().ifCmp(Type.FLOAT_TYPE, LE, new Label()));
    assertEquals("DCMPL IFGE L0", new Generator().ifCmp(Type.DOUBLE_TYPE, GE, new Label()));
    assertEquals("DCMPG IFLE L0", new Generator().ifCmp(Type.DOUBLE_TYPE, LE, new Label()));
  }

  private static class Generator {

    private final Textifier textifier;
    private final GeneratorAdapter generatorAdapter;

    public Generator() {
      this(Opcodes.ACC_PUBLIC, "m", "()V");
    }

    public Generator(final int access, final String name, final String descriptor) {
      textifier = new Textifier();
      generatorAdapter =
          new GeneratorAdapter(
              Opcodes.ASM6, new TraceMethodVisitor(textifier), access, name, descriptor);
    }

    public String push(final boolean value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final int value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final long value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final float value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final double value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final String value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final Type value) {
      generatorAdapter.push(value);
      return toString();
    }

    public String push(final Handle handle) {
      generatorAdapter.push(handle);
      return toString();
    }

    public String loadThis() {
      generatorAdapter.loadThis();
      return toString();
    }

    public String loadArg(final int arg) {
      generatorAdapter.loadArg(arg);
      return toString();
    }

    public String loadArgs(final int arg, final int count) {
      generatorAdapter.loadArgs(arg, count);
      return toString();
    }

    public String loadArgs() {
      generatorAdapter.loadArgs();
      return toString();
    }

    public String loadArgArray() {
      generatorAdapter.loadArgArray();
      return toString();
    }

    public String storeArg(final int arg) {
      generatorAdapter.storeArg(arg);
      return toString();
    }

    public Type getLocalType(final int local) {
      return generatorAdapter.getLocalType(local);
    }

    public String loadLocal(final int local) {
      generatorAdapter.loadLocal(local);
      return toString();
    }

    public String loadLocal(final int local, final Type type) {
      generatorAdapter.loadLocal(local, type);
      return toString();
    }

    public String storeLocal(final int local) {
      generatorAdapter.storeLocal(local);
      return toString();
    }

    public String storeLocal(final int local, final Type type) {
      generatorAdapter.storeLocal(local, type);
      return toString();
    }

    public String arrayLoad(final Type type) {
      generatorAdapter.arrayLoad(type);
      return toString();
    }

    public String arrayStore(final Type type) {
      generatorAdapter.arrayStore(type);
      return toString();
    }

    public String pop() {
      generatorAdapter.pop();
      return toString();
    }

    public String pop2() {
      generatorAdapter.pop2();
      return toString();
    }

    public String dup() {
      generatorAdapter.dup();
      return toString();
    }

    public String dup2() {
      generatorAdapter.dup2();
      return toString();
    }

    public String dupX1() {
      generatorAdapter.dupX1();
      return toString();
    }

    public String dupX2() {
      generatorAdapter.dupX2();
      return toString();
    }

    public String dup2X1() {
      generatorAdapter.dup2X1();
      return toString();
    }

    public String dup2X2() {
      generatorAdapter.dup2X2();
      return toString();
    }

    public String swap() {
      generatorAdapter.swap();
      return toString();
    }

    public String swap(final Type prev, final Type type) {
      generatorAdapter.swap(prev, type);
      return toString();
    }

    public String math(final int op, final Type type) {
      generatorAdapter.math(op, type);
      return toString();
    }

    public String not() {
      generatorAdapter.not();
      return toString();
    }

    public String iinc(final int local, final int amount) {
      generatorAdapter.iinc(local, amount);
      return toString();
    }

    public String cast(final Type from, final Type to) {
      generatorAdapter.cast(from, to);
      return toString();
    }

    public String box(final Type type) {
      generatorAdapter.box(type);
      return toString();
    }

    public String valueOf(final Type type) {
      generatorAdapter.valueOf(type);
      return toString();
    }

    public String unbox(final Type type) {
      generatorAdapter.unbox(type);
      return toString();
    }

    public Label newLabel() {
      return generatorAdapter.newLabel();
    }

    public String mark(final Label label) {
      generatorAdapter.mark(label);
      return toString();
    }

    public Label mark() {
      return generatorAdapter.mark();
    }

    public String ifCmp(final Type type, final int mode, final Label label) {
      generatorAdapter.ifCmp(type, mode, label);
      return toString();
    }

    public String ifICmp(final int mode, final Label label) {
      generatorAdapter.ifICmp(mode, label);
      return toString();
    }

    public String ifZCmp(final int mode, final Label label) {
      generatorAdapter.ifZCmp(mode, label);
      return toString();
    }

    public String ifNull(final Label label) {
      generatorAdapter.ifNull(label);
      return toString();
    }

    public String ifNonNull(final Label label) {
      generatorAdapter.ifNonNull(label);
      return toString();
    }

    public String goTo(final Label label) {
      generatorAdapter.goTo(label);
      return toString();
    }

    public String ret(final int local) {
      generatorAdapter.ret(local);
      return toString();
    }

    public String tableSwitch(final int[] keys, final TableSwitchGenerator generator) {
      generatorAdapter.tableSwitch(keys, generator);
      return toString();
    }

    public String tableSwitch(
        final int[] keys, final TableSwitchGenerator generator, final boolean useTable) {
      generatorAdapter.tableSwitch(keys, generator, useTable);
      return toString();
    }

    public String returnValue() {
      generatorAdapter.returnValue();
      return toString();
    }

    public String getStatic(final Type owner, final String name, final Type type) {
      generatorAdapter.getStatic(owner, name, type);
      return toString();
    }

    public String putStatic(final Type owner, final String name, final Type type) {
      generatorAdapter.putStatic(owner, name, type);
      return toString();
    }

    public String getField(final Type owner, final String name, final Type type) {
      generatorAdapter.getField(owner, name, type);
      return toString();
    }

    public String putField(final Type owner, final String name, final Type type) {
      generatorAdapter.putField(owner, name, type);
      return toString();
    }

    public String invokeVirtual(final Type owner, final Method method) {
      generatorAdapter.invokeVirtual(owner, method);
      return toString();
    }

    public String invokeConstructor(final Type type, final Method method) {
      generatorAdapter.invokeConstructor(type, method);
      return toString();
    }

    public String invokeStatic(final Type owner, final Method method) {
      generatorAdapter.invokeStatic(owner, method);
      return toString();
    }

    public String invokeInterface(final Type owner, final Method method) {
      generatorAdapter.invokeInterface(owner, method);
      return toString();
    }

    public String invokeDynamic(
        final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
      generatorAdapter.invokeDynamic(name, desc, bsm, bsmArgs);
      return toString();
    }

    public String newInstance(final Type type) {
      generatorAdapter.newInstance(type);
      return toString();
    }

    public String newArray(final Type type) {
      generatorAdapter.newArray(type);
      return toString();
    }

    public String arrayLength() {
      generatorAdapter.arrayLength();
      return toString();
    }

    public String throwException() {
      generatorAdapter.throwException();
      return toString();
    }

    public String throwException(final Type type, final String msg) {
      generatorAdapter.throwException(type, msg);
      return toString();
    }

    public String checkCast(final Type type) {
      generatorAdapter.checkCast(type);
      return toString();
    }

    public String instanceOf(final Type type) {
      generatorAdapter.instanceOf(type);
      return toString();
    }

    public String monitorEnter() {
      generatorAdapter.monitorEnter();
      return toString();
    }

    public String monitorExit() {
      generatorAdapter.monitorExit();
      return toString();
    }

    public String endMethod() {
      generatorAdapter.endMethod();
      return toString();
    }

    public String catchException(final Label start, final Label end, final Type exception) {
      generatorAdapter.catchException(start, end, exception);
      return toString();
    }

    @Override
    public String toString() {
      return textifier
          .text
          .stream()
          .map(text -> text.toString().trim())
          .collect(Collectors.joining(" "));
    }
  }
}
