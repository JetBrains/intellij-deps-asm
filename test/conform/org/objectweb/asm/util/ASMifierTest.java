/***
 * ASM tests
 * Copyright (c) 2002,2003 France Telecom
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.objectweb.asm.util;

import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import junit.framework.TestSuite;

import net.janino.ClassLoaderIClassLoader;
import net.janino.IClassLoader;
import net.janino.Parser;
import net.janino.Scanner;

import org.objectweb.asm.AbstractTest;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;

/**
 * ASMifier tests.
 * 
 * @author Eugene Kuleshov
 * @author Eric Bruneton
 */

public class ASMifierTest extends AbstractTest {
  
  public static final Compiler COMPILER = new Compiler();
  
  private final static TestClassLoader LOADER = new TestClassLoader();

  public static TestSuite suite () throws Exception {
    return new ASMifierTest().getSuite();
  }

  public void test () throws Exception {
    ClassReader cr = new ClassReader(is);
    
    if (cr.b.length > 20000) {
      return;
    }
    
    StringWriter sw = new StringWriter();
    ASMifierClassVisitor cv = new ASMifierClassVisitor(new PrintWriter(sw));
    cr.accept(cv, false);

    String generated = sw.toString();
    
    byte[] generatorClassData = COMPILER.compile(n, generated);
    
    Class c = LOADER.defineClass("asm." + n + "Dump", generatorClassData);
    Method m = c.getMethod("dump", new Class[0]);
    byte[] b = (byte[])m.invoke(null, new Object[ 0]);
    
    assertEquals(cr, new ClassReader(b));
  }
  
  private static class TestClassLoader extends ClassLoader {
    
    public Class defineClass (final String name, final byte[] b) {
      return defineClass(name, b, 0, b.length);
    }
  }
  
  private static class Compiler  {
    
    final static IClassLoader CL = 
      new ClassLoaderIClassLoader(new URLClassLoader(new URL[0]));

    public byte[] compile(String name, String source) throws Exception {
      Parser p = new Parser(new Scanner(name, new StringReader(source)));
      return p.parseCompilationUnit().compile(CL, 0)[0].toByteArray();
    }
  }
}

