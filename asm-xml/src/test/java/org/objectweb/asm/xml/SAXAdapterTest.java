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
package org.objectweb.asm.xml;

import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.test.AsmTest;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAXAdapter tests.
 *
 * @author Eugene Kuleshov
 */
public class SAXAdapterTest extends AsmTest {

  SAXAdapter sa;

  @BeforeEach
  public void setUp() {
    sa =
        new SAXAdapter(
            new DefaultHandler() {

              @Override
              public void startDocument() throws SAXException {
                throw new SAXException();
              }

              @Override
              public void endDocument() throws SAXException {
                throw new SAXException();
              }

              @Override
              public void startElement(
                  final String arg0, final String arg1, final String arg2, final Attributes arg3)
                  throws SAXException {
                throw new SAXException();
              }

              @Override
              public void endElement(final String arg0, final String arg1, final String arg2)
                  throws SAXException {
                throw new SAXException();
              }
            }) {};
  }

  @Test
  public void testInvalidAddDocumentStart() {
    assertThrows(Exception.class, () -> sa.addDocumentStart());
  }

  @Test
  public void testInvalidAddDocumentEnd() {
    assertThrows(Exception.class, () -> sa.addDocumentEnd());
  }

  @Test
  public void testInvalidAddStart() {
    assertThrows(Exception.class, () -> sa.addStart("name", null));
  }

  @Test
  public void testInvalidAddEnd() {
    assertThrows(Exception.class, () -> sa.addEnd("name"));
  }

  /**
   * Tests that classes are unchanged with a ClassReader->SAXClassAdapter->ClassWriter transform.
   *
   * @throws TransformerFactoryConfigurationError
   * @throws TransformerConfigurationException
   * @throws SAXException
   */
  @ParameterizedTest
  @EnumSource(PrecompiledClass.class)
  public void testSAXAdapter_classUnchanged(PrecompiledClass classParameter)
      throws TransformerConfigurationException, TransformerFactoryConfigurationError, SAXException {
    // Non standard attributes and features introduced in JDK11 or more are not supported.
    if (classParameter == PrecompiledClass.JDK3_ARTIFICIAL_STRUCTURES
        || classParameter.isMoreRecentThan(Api.ASM6)) {
      return;
    }
    byte[] classFile = classParameter.getBytes();
    ClassReader classReader = new ClassReader(classFile);
    ClassWriter classWriter = new ClassWriter(0);
    TransformerHandler transformerHandler =
        ((SAXTransformerFactory) TransformerFactory.newInstance()).newTransformerHandler();
    transformerHandler.setResult(new SAXResult(new ASMContentHandler(classWriter)));
    transformerHandler.startDocument();
    classReader.accept(new SAXClassAdapter(transformerHandler, false), 0);
    transformerHandler.endDocument();
    assertThatClass(classWriter.toByteArray()).isEqualTo(classFile);
  }
}
