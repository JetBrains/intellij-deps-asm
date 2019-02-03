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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;
import org.objectweb.asm.test.AsmTest;

/**
 * Unit tests for {@link CheckSignatureAdapter}.
 *
 * @author Eric Bruneton
 */
public class CheckSignatureAdapterTest extends AsmTest {

  @Test
  public void testVisitFormalTypeParameter_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    checkSignatureAdapter.visitSuperclass();

    assertThrows(
        IllegalStateException.class, () -> checkSignatureAdapter.visitFormalTypeParameter("T"));
  }

  @Test
  public void testVisitFormalTypeParameter_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(
        IllegalStateException.class, () -> checkSignatureAdapter.visitFormalTypeParameter("T"));
  }

  @Test
  public void testVisitClassBound_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitClassBound());
  }

  @Test
  public void testVisitClassBound_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitClassBound());
  }

  @Test
  public void testVisitInterfaceBound_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitInterfaceBound());
  }

  @Test
  public void testVisitInterfaceBound_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitInterfaceBound());
  }

  @Test
  public void testVisitSuperClass_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    checkSignatureAdapter.visitSuperclass();

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitSuperclass());
  }

  @Test
  public void testVisitSuperClass_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitSuperclass());
  }

  @Test
  public void testVisitInterface_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitInterface());
  }

  @Test
  public void testVisitInterface_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitInterface());
  }

  @Test
  public void testVisitParameterType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitParameterType());
  }

  @Test
  public void testVisitParameterType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);
    checkSignatureAdapter.visitReturnType();

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitParameterType());
  }

  @Test
  public void testVisitReturnType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitReturnType());
  }

  @Test
  public void testVisitReturnType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);
    checkSignatureAdapter.visitReturnType();

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitReturnType());
  }

  @Test
  public void testVisitExceptionType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitExceptionType());
  }

  @Test
  public void testVisitExceptionType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitExceptionType());
  }

  @Test
  public void testVisitBase_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitBaseType('I');

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitBaseType('I'));
  }

  @Test
  public void testVisitBase_typeSignature_illegalVoidArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitBaseType('V'));
  }

  @Test
  public void testVisitBase_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitBaseType('A'));
  }

  @Test
  public void testVisitBase_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitBaseType('I'));
  }

  @Test
  public void testVisitTypeVariable_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitTypeVariable("T"));
  }

  @Test
  public void testVisitTypeVariable_typeSignature_nullArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(
        IllegalArgumentException.class, () -> checkSignatureAdapter.visitTypeVariable(null));
  }

  @Test
  public void testVisitTypeVariable_typeSignature_emptyArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitTypeVariable(""));
  }

  @Test
  public void testVisitTypeVariable_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(
        IllegalArgumentException.class, () -> checkSignatureAdapter.visitTypeVariable("LT;"));
  }

  @Test
  public void testVisitTypeVariable_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitTypeVariable("T");

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitTypeVariable("T"));
  }

  @Test
  public void testVisitArrayType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitArrayType());
  }

  @Test
  public void testVisitArrayType_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitArrayType();

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitArrayType());
  }

  @Test
  public void testVisitClassType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitClassType("A"));
  }

  @Test
  public void testVisitClassType_typeSignature_nullArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitClassType(null));
  }

  @Test
  public void testVisitClassType_typeSignature_emptyArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitClassType(""));
  }

  @Test
  public void testVisitClassType_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalArgumentException.class, () -> checkSignatureAdapter.visitClassType("<A>"));
  }

  @Test
  public void testVisitClassType_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitClassType("A");

    assertThrows(RuntimeException.class, () -> checkSignatureAdapter.visitClassType("A"));
  }

  @Test
  public void testVisitClassType_nonJavaIdentifier() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    SignatureVisitor signatureVisitor = checkSignatureAdapter.visitSuperclass();

    assertDoesNotThrow(() -> signatureVisitor.visitClassType("Foo Bar"));
  }

  @Test
  public void testVisitInnerClassType_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitInnerClassType("A"));
  }

  @Test
  public void testVisitTypeArgument_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitTypeArgument());
  }

  @Test
  public void testVisitTypeArgument_wildcard_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitTypeArgument('+'));
  }

  @Test
  public void testVisitTypeArgument_wildcard_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitClassType("A");

    assertThrows(
        IllegalArgumentException.class, () -> checkSignatureAdapter.visitTypeArgument('*'));
  }

  @Test
  public void testVisitlEnd_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    assertThrows(IllegalStateException.class, () -> checkSignatureAdapter.visitEnd());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#classSignatures"})
  public void testVisitMethods_classSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.accept(
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#classSignatures"})
  public void testVisitMethods_classSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    assertDoesNotThrow(
        () ->
            signatureReader.accept(
                new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null)));
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#methodSignatures"})
  public void testVisitMethods_methodSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.accept(
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#methodSignatures"})
  public void testVisitMethods_methodSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    assertDoesNotThrow(
        () ->
            signatureReader.accept(
                new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null)));
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#fieldSignatures"})
  public void testVisitMethods_typeSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.acceptType(
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#fieldSignatures"})
  public void testVisitMethods_typeSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    assertDoesNotThrow(
        () ->
            signatureReader.acceptType(
                new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null)));
  }
}
