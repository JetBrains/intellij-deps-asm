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

import java.util.Collection;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import org.junit.Test;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.test.AsmTest;
import org.xml.sax.SAXException;

/**
 * SAXAdapter tests.
 *
 * @author Eugene Kuleshov
 */
public class SAXAdapterTest extends AsmTest {

  /** @return test parameters to test all the precompiled classes with ASM6. */
  @Parameters(name = NAME)
  public static Collection<Object[]> data() {
    return data(Api.ASM6);
  }

  /**
   * Tests that classes are unchanged with a ClassReader->SAXClassAdapter->ClassWriter transform.
   *
   * @throws TransformerFactoryConfigurationError
   * @throws TransformerConfigurationException
   * @throws SAXException
   */
  @Test
  public void testSAXAdapter_classUnchanged()
      throws TransformerConfigurationException, TransformerFactoryConfigurationError, SAXException {
    // Non standard attributes are not supported by the XML API.
    if (classParameter == PrecompiledClass.JDK3_ATTRIBUTE) return;
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
