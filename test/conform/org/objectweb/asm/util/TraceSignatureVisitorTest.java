/* $Id: TraceSignatureVisitorTest.java,v 1.2 2005-01-20 11:13:56 ekuleshov Exp $ */

package org.objectweb.asm.util;

import java.util.StringTokenizer;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;

/**
 * ClassSignatureDecompilerTest
 *
 * @author Eugene Kuleshov
 */
public class TraceSignatureVisitorTest extends TestCase {
  private static String[] DATA = {
      "C|E|<E extends java.lang.Enum<E>>|" +
          "<E:Ljava/lang/Enum<TE;>;>Ljava/lang/Object;Ljava/lang/Comparable<TE;>;Ljava/io/Serializable;",
      "C|I|<D extends java.lang.reflect.GenericDeclaration> extends java.lang.reflect.Type|" +
          "<D::Ljava/lang/reflect/GenericDeclaration;>Ljava/lang/Object;Ljava/lang/reflect/Type;",
      "C|C|<K, V> extends java.util.AbstractMap<K, V> implements java.util.concurrent.ConcurrentMap<K, V>, java.io.Serializable|" +
          "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;",
      "C|C|<K extends java.lang.Enum<K>, V> extends java.util.AbstractMap<K, V> implements java.io.Serializable, java.lang.Cloneable|" +
          "<K:Ljava/lang/Enum<TK;>;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/io/Serializable;Ljava/lang/Cloneable;",
      "F|C|java.lang.Class<?>|" +
          "Ljava/lang/Class<*>;",
      "F|C|java.lang.reflect.Constructor<T>|" +
          "Ljava/lang/reflect/Constructor<TT;>;",
      "F|C|T[]|" +
          "[TT;",
      "F|C|java.util.Hashtable<?, ?>|" +
          "Ljava/util/Hashtable<**>;",
      "F|C|java.util.concurrent.atomic.AtomicReferenceFieldUpdater<java.io.BufferedInputStream, byte[]>|" +
          "Ljava/util/concurrent/atomic/AtomicReferenceFieldUpdater<Ljava/io/BufferedInputStream;[B>;",
      "M|C|(java.lang.String, java.lang.Class<?>, java.lang.reflect.Method[], java.lang.reflect.Method, java.lang.reflect.Method) throws IntrospectionException|" +
          "(Ljava/lang/String;Ljava/lang/Class<*>;[Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;Ljava/lang/reflect/Method;)V",
      "F|C|AA<byte[][]>|" +
          "LAA<[[B>;",
      "F|C|AA<java.util.Map<java.lang.String,java.lang.String>[][]>|" +
          "LAA<[[Ljava/util/Map<Ljava/lang/String;Ljava/lang/String;>;>;"
    };
  
  public static TestSuite suite() {
    TestSuite suite = new TestSuite( TraceSignatureVisitorTest.class.getName());
    
    for( int i = 0; i < DATA.length; i++) {
      suite.addTest( new TraceSignatureVisitorTest( new TestData( DATA[ i])));
    }
    
    return suite;
  }
  
  
  private TestData data;
  
  
  private TraceSignatureVisitorTest( TestData data) {
    super( "testSignature");
    this.data = data;
  }

  public void testSignature() {
    TraceSignatureVisitor d = new TraceSignatureVisitor( data.access);
    SignatureReader r = new SignatureReader( data.signature);

    switch( data.type) {
      case 'C':  r.acceptClass( d);   break;
      case 'F':  r.acceptType( d);    break;
      case 'M':  r.acceptMethod( d);  break;
    }
    
    assertEquals( data.declaration, d.getDeclaration());
  }
  
  public String getName() {
    return super.getName()+" "+data.signature;
  }

  
  private static class TestData {
    public final char type;
    public final int access;
    public final String declaration;
    public final String signature;

    private TestData( String data) {
      StringTokenizer st = new StringTokenizer( data, "|");
      this.type = st.nextToken().charAt( 0);
      
      String acc = st.nextToken();
      switch( acc.charAt( 0)) {
        case 'E':  this.access = Opcodes.ACC_ENUM;       break;
        case 'I':  this.access = Opcodes.ACC_INTERFACE;  break;
        case 'A':  this.access = Opcodes.ACC_ANNOTATION; break;
        default:   this.access = 0;
      }
      
      this.declaration = st.nextToken();
      this.signature = st.nextToken();
    }
  }
  
}

