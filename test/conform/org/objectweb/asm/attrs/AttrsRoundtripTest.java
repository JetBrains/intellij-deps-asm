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

package org.objectweb.asm.attrs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.attrs.Attributes;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Roundtrip test suite.
 * 
 * @author Eugene Kuleshov
 */

public class AttrsRoundtripTest extends TestCase {
  private String className;


  public AttrsRoundtripTest( String className) {
    super( "testRoundtrip");
    this.className = className;
  }
  
  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite( AttrsRoundtripTest.class.getName());

    Class c = AttrsRoundtripTest.class;
    String s = c.getResource( "/java/lang/String.class").toString();
    int n = s.indexOf( '!');
    URL u = new URL( s.substring( 4, n));
    
    ZipInputStream zis = new ZipInputStream( u.openStream());
    ZipEntry ze = null;    
    while(( ze = zis.getNextEntry())!=null) {
      if( ze.getName().endsWith( ".class")) {
        suite.addTest( new AttrsRoundtripTest( "jar:"+u.toString()+"!/"+ze.getName()));
      }
    }
    
    return suite;
  }

  public void testRoundtrip() throws Exception {
    byte[] classData = getCode( new URL( className).openStream());
    
    ClassReader cr = new ClassReader( classData);
    ClassWriter cw = new ClassWriter( false, false);
    cr.accept(cw, Attributes.getDefaultAttributes(), false);
    byte[] newData = cw.toByteArray();

    try {
	  assertTrue( Arrays.equals( classData, newData));
      
    } catch( Throwable ex) {
      StringWriter sw1 = new StringWriter();
      StringWriter sw2 = new StringWriter();
      traceBytecode( classData, new PrintWriter( sw1));
      traceBytecode( newData, new PrintWriter( sw2));
      assertEquals( "different data: "+ex.toString(), sw1.getBuffer().toString(), sw2.getBuffer().toString());

      // HexDumpEncoder enc = new HexDumpEncoder();
      // assertEquals( "invaid data", enc.encode( classData), enc.encode( newData));

    }
  }
  
  private static void traceBytecode( byte[] classData, PrintWriter pw) {
    ClassReader cr = new ClassReader( classData);
    // cr.accept( new TraceClassVisitor( cw, new PrintWriter( System.out)), 
    cr.accept( new TraceClassVisitor( null, pw), Attributes.getDefaultAttributes(), false);
  }

  private static byte[] getCode( InputStream is) throws IOException {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    byte[] buff = new byte[ 1024];
    int n = -1;
    while(( n = is.read( buff))>-1) bos.write( buff, 0, n);
    return bos.toByteArray();
  }

  // workaround for Ant's JUnit test runner
  public String getName() {
    return super.getName()+" : "+className;
  }
  
}
