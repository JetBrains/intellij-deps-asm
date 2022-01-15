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
import org.junit.jupiter.api.function.Executable;
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
class CheckSignatureAdapterTest extends AsmTest {

  @Test
  void testVisitFormalTypeParameter_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    checkSignatureAdapter.visitSuperclass();

    Executable visitFormalTypeParameter = () -> checkSignatureAdapter.visitFormalTypeParameter("T");

    assertThrows(IllegalStateException.class, visitFormalTypeParameter);
  }

  @Test
  void testVisitFormalTypeParameter_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitFormalTypeParameter = () -> checkSignatureAdapter.visitFormalTypeParameter("T");

    assertThrows(IllegalStateException.class, visitFormalTypeParameter);
  }

  @Test
  void testVisitClassBound_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitClassBound = () -> checkSignatureAdapter.visitClassBound();

    assertThrows(IllegalStateException.class, visitClassBound);
  }

  @Test
  void testVisitClassBound_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitClassBound = () -> checkSignatureAdapter.visitClassBound();

    assertThrows(IllegalStateException.class, visitClassBound);
  }

  @Test
  void testVisitInterfaceBound_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitInterfaceBound = () -> checkSignatureAdapter.visitInterfaceBound();

    assertThrows(IllegalStateException.class, visitInterfaceBound);
  }

  @Test
  void testVisitInterfaceBound_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitInterfaceBound = () -> checkSignatureAdapter.visitInterfaceBound();

    assertThrows(IllegalStateException.class, visitInterfaceBound);
  }

  @Test
  void testVisitSuperClass_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    checkSignatureAdapter.visitSuperclass();

    Executable visitSuperClass = () -> checkSignatureAdapter.visitSuperclass();

    assertThrows(IllegalStateException.class, visitSuperClass);
  }

  @Test
  void testVisitSuperClass_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    Executable visitSuperClass = () -> checkSignatureAdapter.visitSuperclass();

    assertThrows(IllegalStateException.class, visitSuperClass);
  }

  @Test
  void testVisitInterface_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitInterface = () -> checkSignatureAdapter.visitInterface();

    assertThrows(IllegalStateException.class, visitInterface);
  }

  @Test
  void testVisitInterface_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    Executable visitInterface = () -> checkSignatureAdapter.visitInterface();

    assertThrows(IllegalStateException.class, visitInterface);
  }

  @Test
  void testVisitParameterType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitParameterType = () -> checkSignatureAdapter.visitParameterType();

    assertThrows(IllegalStateException.class, visitParameterType);
  }

  @Test
  void testVisitParameterType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);
    checkSignatureAdapter.visitReturnType();

    Executable visitParameterType = () -> checkSignatureAdapter.visitParameterType();

    assertThrows(IllegalStateException.class, visitParameterType);
  }

  @Test
  void testVisitReturnType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitReturnType = () -> checkSignatureAdapter.visitReturnType();

    assertThrows(IllegalStateException.class, visitReturnType);
  }

  @Test
  void testVisitReturnType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);
    checkSignatureAdapter.visitReturnType();

    Executable visitReturnType = () -> checkSignatureAdapter.visitReturnType();

    assertThrows(IllegalStateException.class, visitReturnType);
  }

  @Test
  void testVisitExceptionType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitExceptionType = () -> checkSignatureAdapter.visitExceptionType();

    assertThrows(IllegalStateException.class, visitExceptionType);
  }

  @Test
  void testVisitExceptionType_methodSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null);

    Executable visitExceptionType = () -> checkSignatureAdapter.visitExceptionType();

    assertThrows(IllegalStateException.class, visitExceptionType);
  }

  @Test
  void testVisitBaseType_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitBaseType('I');

    Executable visitBaseType = () -> checkSignatureAdapter.visitBaseType('I');

    assertThrows(IllegalStateException.class, visitBaseType);
  }

  @Test
  void testVisitBaseType_typeSignature_illegalVoidArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitBaseType = () -> checkSignatureAdapter.visitBaseType('V');

    Exception exception = assertThrows(IllegalArgumentException.class, visitBaseType);
    assertEquals("Base type descriptor can't be V", exception.getMessage());
  }

  @Test
  void testVisitBaseType_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitBaseType = () -> checkSignatureAdapter.visitBaseType('A');

    Exception exception = assertThrows(IllegalArgumentException.class, visitBaseType);
    assertEquals("Base type descriptor must be one of ZCBSIFJD", exception.getMessage());
  }

  @Test
  void testVisitBaseType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitBaseType = () -> checkSignatureAdapter.visitBaseType('I');

    assertThrows(IllegalStateException.class, visitBaseType);
  }

  @Test
  void testVisitTypeVariable_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitTypeVariable = () -> checkSignatureAdapter.visitTypeVariable("T");

    assertThrows(IllegalStateException.class, visitTypeVariable);
  }

  @Test
  void testVisitTypeVariable_typeSignature_nullArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitTypeVariable = () -> checkSignatureAdapter.visitTypeVariable(null);

    Exception exception = assertThrows(IllegalArgumentException.class, visitTypeVariable);
    assertEquals("Invalid type variable (must not be null or empty)", exception.getMessage());
  }

  @Test
  void testVisitTypeVariable_typeSignature_emptyArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitTypeVariable = () -> checkSignatureAdapter.visitTypeVariable("");

    Exception exception = assertThrows(IllegalArgumentException.class, visitTypeVariable);
    assertEquals("Invalid type variable (must not be null or empty)", exception.getMessage());
  }

  @Test
  void testVisitTypeVariable_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitTypeVariable = () -> checkSignatureAdapter.visitTypeVariable("LT;");

    Exception exception = assertThrows(IllegalArgumentException.class, visitTypeVariable);
    assertEquals(
        "Invalid type variable (must not contain . ; [ / < > or :): LT;", exception.getMessage());
  }

  @Test
  void testVisitTypeVariable_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitTypeVariable("T");

    Executable visitTypeVariable = () -> checkSignatureAdapter.visitTypeVariable("T");

    assertThrows(IllegalStateException.class, visitTypeVariable);
  }

  @Test
  void testVisitArrayType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitArrayType = () -> checkSignatureAdapter.visitArrayType();

    assertThrows(IllegalStateException.class, visitArrayType);
  }

  @Test
  void testVisitArrayType_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitArrayType();

    Executable visitArrayType = () -> checkSignatureAdapter.visitArrayType();

    assertThrows(IllegalStateException.class, visitArrayType);
  }

  @Test
  void testVisitClassType_classSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);

    Executable visitClassType = () -> checkSignatureAdapter.visitClassType("A");

    assertThrows(IllegalStateException.class, visitClassType);
  }

  @Test
  void testVisitClassType_typeSignature_nullArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitClassType = () -> checkSignatureAdapter.visitClassType(null);

    Exception exception = assertThrows(IllegalArgumentException.class, visitClassType);
    assertEquals("Invalid class name (must not be null or empty)", exception.getMessage());
  }

  @Test
  void testVisitClassType_typeSignature_emptyArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitClassType = () -> checkSignatureAdapter.visitClassType("");

    Exception exception = assertThrows(IllegalArgumentException.class, visitClassType);
    assertEquals("Invalid class name (must not be null or empty)", exception.getMessage());
  }

  @Test
  void testVisitClassType_typeSignature_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitClassType = () -> checkSignatureAdapter.visitClassType("<A>");

    Exception exception = assertThrows(IllegalArgumentException.class, visitClassType);
    assertEquals(
        "Invalid class name (must not contain . ; [ < > or :): <A>", exception.getMessage());
  }

  @Test
  void testVisitClassType_typeSignature_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitClassType("A");

    Executable visitClassType = () -> checkSignatureAdapter.visitClassType("A");

    assertThrows(IllegalStateException.class, visitClassType);
  }

  @Test
  void testVisitClassType_nonJavaIdentifier() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null);
    SignatureVisitor signatureVisitor = checkSignatureAdapter.visitSuperclass();

    Executable visitClassType = () -> signatureVisitor.visitClassType("Foo Bar");

    assertDoesNotThrow(visitClassType);
  }

  @Test
  void testVisitInnerClassType_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitInnerClassType = () -> checkSignatureAdapter.visitInnerClassType("A");

    assertThrows(IllegalStateException.class, visitInnerClassType);
  }

  @Test
  void testVisitTypeArgument_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitTypeArgument = () -> checkSignatureAdapter.visitTypeArgument();

    assertThrows(IllegalStateException.class, visitTypeArgument);
  }

  @Test
  void testVisitTypeArgument_wildcard_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitTypeArgument = () -> checkSignatureAdapter.visitTypeArgument('+');

    assertThrows(IllegalStateException.class, visitTypeArgument);
  }

  @Test
  void testVisitTypeArgument_wildcard_illegalArgument() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);
    checkSignatureAdapter.visitClassType("A");

    Executable visitTypeArgument = () -> checkSignatureAdapter.visitTypeArgument('*');

    Exception exception = assertThrows(IllegalArgumentException.class, visitTypeArgument);
    assertEquals("Wildcard must be one of +-=", exception.getMessage());
  }

  @Test
  void testVisitEnd_illegalState() {
    CheckSignatureAdapter checkSignatureAdapter =
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null);

    Executable visitEnd = () -> checkSignatureAdapter.visitEnd();

    assertThrows(IllegalStateException.class, visitEnd);
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#classSignatures"})
  void testVisitMethods_classSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.accept(
        new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#classSignatures"})
  void testVisitMethods_classSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    Executable visitMethods =
        () ->
            signatureReader.accept(
                new CheckSignatureAdapter(CheckSignatureAdapter.CLASS_SIGNATURE, null));

    assertDoesNotThrow(visitMethods);
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#methodSignatures"})
  void testVisitMethods_methodSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.accept(
        new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#methodSignatures"})
  void testVisitMethods_methodSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    Executable visitMethods =
        () ->
            signatureReader.accept(
                new CheckSignatureAdapter(CheckSignatureAdapter.METHOD_SIGNATURE, null));

    assertDoesNotThrow(visitMethods);
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#fieldSignatures"})
  void testVisitMethods_typeSignature(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);
    SignatureWriter signatureWriter = new SignatureWriter();

    signatureReader.acceptType(
        new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, signatureWriter));

    assertEquals(signature, signatureWriter.toString());
  }

  @ParameterizedTest
  @MethodSource({"org.objectweb.asm.util.SignaturesProviders#fieldSignatures"})
  void testVisitMethods_typeSignature_noDelegate(final String signature) {
    SignatureReader signatureReader = new SignatureReader(signature);

    Executable visitMethods =
        () ->
            signatureReader.acceptType(
                new CheckSignatureAdapter(CheckSignatureAdapter.TYPE_SIGNATURE, null));

    assertDoesNotThrow(visitMethods);
  }
}
