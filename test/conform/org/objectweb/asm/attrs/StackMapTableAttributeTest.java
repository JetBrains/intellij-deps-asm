/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;

import org.objectweb.asm.AbstractTest;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.util.TraceClassVisitor;
import org.objectweb.asm.util.TraceMethodVisitor;


/**
 * StackMapTableAttributeTest
 *
 * @author Eugene Kuleshov
 */
public class StackMapTableAttributeTest extends AbstractTest {

  private static final String TEST_CLASS = "StackMapTableSample.data";

  public StackMapTableAttributeTest() {
    super();
    is = getClass().getResourceAsStream( TEST_CLASS);
  }
  
  public void test() throws Exception {
    Attribute[] attributes = new Attribute[] { new StubStackMapTableAttribute()};
    
    ClassWriter cw = new ClassWriter( false);

//    TraceClassVisitor tv = new TraceClassVisitor( cw, new PrintWriter( System.err)) {
//        protected TraceMethodVisitor createTraceMethodVisitor() {
//          return new TraceMethodVisitor() {
//              protected void appendLabel( Label l) {
//                super.appendLabel(l);
//                buf.append( " "+System.identityHashCode( l));
//              }
//            };
//        }
//      };
    
    ClassReader cr1 = new ClassReader( is);
    cr1.accept( cw, new Attribute[] { new StackMapTableAttribute()}, false);
    
    ClassReader cr2 = new ClassReader( cw.toByteArray());
    
    if(!Arrays.equals(cr1.b, cr2.b)) {          
      StringWriter sw1 = new StringWriter();
      StringWriter sw2 = new StringWriter();
      ClassVisitor cv1 = new TraceClassVisitor(new PrintWriter(sw1));
      ClassVisitor cv2 = new TraceClassVisitor(new PrintWriter(sw2));
      cr1.accept( cv1, attributes, false);
      cr2.accept( cv2, attributes, false);
      assertEquals("different data", sw1.toString(), sw2.toString());
    }
    
  }


  private static final class StubStackMapTableAttribute extends Attribute { 
    private static String s = "0123456789ABCDEF";
    private String data;
    
    public StubStackMapTableAttribute() {
      super( "StackMapTable");
    }
    
    protected Attribute read( ClassReader cr, int off, int len, char[] buf, int codeOff, Label[] labels) {
      StringBuffer sb = new StringBuffer();
      for( int i = 0; i < len; i++) {
        int b = cr.readByte( off + i);
        sb.append( s.charAt( b >> 4))
          .append( s.charAt( b & 0xF))
          .append( " ");
      }
      StubStackMapTableAttribute att = new StubStackMapTableAttribute();
      att.data = sb.toString();
      return att;
    }
    
    public String toString() {
      return data;
    }
    
  }  

}

