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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import net.janino.ClassLoaderIClassLoader;
import net.janino.IClassLoader;
import net.janino.Parser;
import net.janino.Scanner;
import net.janino.Java.CompilationUnit;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.PrintClassVisitor;
import org.objectweb.asm.util.TraceClassVisitor;


/**
 * Roundtrip test suite.
 * 
 * @author Eugene Kuleshov
 */

public class AttrsAsmifierTest extends TestCase {
  public static final Compiller COMPILLER = new JaninoCompiller();
  
  private String className;
  private final URL u;


  public AttrsAsmifierTest( URL u, String className) {
    super( "testAsmifier");
    this.u = u;
    this.className = className;
  }
  
  public static TestSuite suite() throws Exception {
    TestSuite suite = new TestSuite( AttrsAsmifierTest.class.getName());

    Class c = AttrsAsmifierTest.class;
    String s = c.getResource( "/java/lang/String.class").toString();
    int n = s.indexOf( '!');
    URL u = new URL( s.substring( 4, n));
    
    ZipInputStream zis = new ZipInputStream( u.openStream());
    ZipEntry ze = null;    
    while(( ze = zis.getNextEntry())!=null) {
      if( ze.getName().endsWith( ".class")) {
        suite.addTest( new AttrsAsmifierTest( u, "jar:"+u.toString()+"!/"+ze.getName()));
      }
    }
    
    return suite;
  }

  public void testAsmifier() throws Exception {
    URL classUrl = new URL( className);
    byte[] classData = getCode( classUrl.openStream());
    
    StringWriter sw = new StringWriter();
    ASMifierClassVisitor cv = new ASMifierClassVisitor( new PrintWriter( sw));
    
    ClassReader cr = new ClassReader( classData);
    cr.accept(cv, PrintClassVisitor.getDefaultAttributes(), false);

    String name = classUrl.getFile();
    String generated = sw.toString();
    
    String cname = name.substring( name.lastIndexOf( '/')+1, name.length()-".class".length())+"Dump";
    byte[] generatorClassData = COMPILLER.compile( cname, generated);
    
    String nm = className.substring( className.indexOf( "!/")+2, className.length()-".class".length()).replace('/', '.');
    Class c = loadClass("asm."+nm+"Dump", generatorClassData);
    Method m = c.getMethod( "dump", new Class[0]);
    byte[] newBytecode = ( byte[]) m.invoke(null, new Object[ 0]);
    
    try {
      assertTrue( "Different bytecode", Arrays.equals( newBytecode, classData));
    } catch( Throwable ex) {
      // HexDumpEncoder enc = new HexDumpEncoder();
      // assertEquals( "Different bytecode\n"+traceBytecode(classData), enc.encode(classData), enc.encode(newBytecode));
      assertEquals( "Different bytecode:\n"+generated, traceBytecode(classData), traceBytecode(newBytecode));
    }
  }
  
  
  private static String traceBytecode( byte[] classData) {
    StringWriter sw = new StringWriter();
    ClassReader cr = new ClassReader( classData);
    // cr.accept( new TraceClassVisitor( cw, new PrintWriter( System.out)), 
    cr.accept( new TraceClassVisitor( null, new PrintWriter( sw)), Attributes.getDefaultAttributes(), false);
    return sw.toString();
  }

  private Class loadClass(String className, byte[] bytecode) throws ClassNotFoundException {
    return new TestClassLoader( className, bytecode).loadClass( className);
  }

  
  public static byte[] getCode( InputStream is) throws IOException {
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

  
  private static final class TestClassLoader extends ClassLoader {
    private final String className;
    private final byte[] bytecode;

    public TestClassLoader( String className, byte[] bytecode) {
      super();
      this.className = className;
      this.bytecode = bytecode;
    }

    public Class loadClass( String name) throws ClassNotFoundException {
      if( className.equals( name)) {
        return super.defineClass( className, bytecode, 0, bytecode.length);            
      }
      return super.loadClass( name);
    }

  }

  
  private static interface Compiller {
    byte[] compile(String name, String source) throws Exception;
  }

  
  /*
  // require ECLIPSE_HOME\plugins\org.eclipse.jdt.core_3.0.0\jdtcore.jar
  private static class EclipseCompiller implements Compiller {

    public byte[] compile(String name, String source) throws Exception {
      File f = new File( name+".java");
      File cf = new File( name+".class");

      byte[] code;
      
      try {
        FileWriter fw = new FileWriter( f);
        fw.write(source);
        fw.flush();
        fw.close();
        
        StringWriter sw = new StringWriter();
        StringWriter sw2 = new StringWriter();
        if( !org.eclipse.jdt.internal.compiler.batch.Main.compile( "-warn:unusedImport "+f.getName(), new PrintWriter(sw), new PrintWriter(sw2))) {
          throw new RuntimeException( sw2.toString()+"\n-----\n"+sw.toString());
        }
        
        FileInputStream is = new FileInputStream( cf);
        code = getCode( is);
        is.close();
        
      } finally {
        f.delete();
        cf.delete();
        
      }
      return code;
    }
    
  }
  
  // require JDK_HOME/lib/tools.jar
  private static class JavacCompiller implements Compiller {

    public byte[] compile(String name, String source) throws Exception {
      File f = new File( name+".java");
      File cf = new File( name+".class");

      byte[] code;
      
      try {
        FileWriter fw = new FileWriter( f);
        fw.write(source);
        fw.flush();
        fw.close();
        
        String[] args = new String[] { f.getName()};
        StringWriter sw = new StringWriter();
        int rc = com.sun.tools.javac.Main.compile( args, new PrintWriter( sw));
        if( rc!=0) {
          throw new RuntimeException( sw.toString());
        }
        
        FileInputStream is = new FileInputStream( cf);
        code = getCode( is);
        is.close();
        
      } finally {
        f.delete();
        cf.delete();
        
      }
      return code;
    }
    
  }
  */
  
  // require Janino from http://www.janino.net
  private static class JaninoCompiller implements Compiller {

    public byte[] compile( String name, String source) throws Exception {
      // using janino embeddable compiller
      Parser p = new Parser( new Scanner( name, new StringReader( source)));
      CompilationUnit cu = p.parseCompilationUnit();
      IClassLoader cl = new ClassLoaderIClassLoader( new URLClassLoader( new URL[] {}));
      return cu.compile( cl, 0)[0].toByteArray();
    }
  
  }
  
}

