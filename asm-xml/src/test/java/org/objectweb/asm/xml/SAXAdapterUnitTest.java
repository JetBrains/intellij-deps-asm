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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAXAdapter unit tests
 *
 * @author Eric Bruneton
 */
public class SAXAdapterUnitTest {

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
}
