/* $Id: ClassSignatureDecompilerTest.java,v 1.3 2005-01-18 01:41:24 ekuleshov Exp $ */

package org.objectweb.asm.util;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.util.TraceClassVisitor.ClassSignatureDecompiler;

import junit.framework.TestCase;

/**
 * ClassSignatureDecompilerTest
 *
 * @author Eugene Kuleshov
 */
public class ClassSignatureDecompilerTest extends TestCase {

  public void test1() throws Exception {
    // java.lang.Enum
    String decl = "<E extends Enum<E>>"; 
    String signature = "<E:Ljava/lang/Enum<TE;>;>Ljava/lang/Object;Ljava/lang/Comparable<TE;>;Ljava/io/Serializable;";

    String className = "java/lang/Enum";
    int access = Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access, className);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }

  public void test2() throws Exception {
    // java.lang.reflect.TypeVariable
    String decl = "<D extends java.lang.reflect.GenericDeclaration> extends java.lang.reflect.Type";
    String signature = "<D::Ljava/lang/reflect/GenericDeclaration;>Ljava/lang/Object;Ljava/lang/reflect/Type;";

    String className = "java/lang/reflect/TypeVariable";
    int access = Opcodes.ACC_INTERFACE | Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access, className);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }

  public void test3() throws Exception {
    // java.util.concurrent.ConcurrentHashMap
    String decl = "<K, V> extends java.util.AbstractMap<K, V> implements java.util.concurrent.ConcurrentMap<K, V>, java.io.Serializable";
    String signature = "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;";

    String className = "java/util/concurrent/ConcurrentHashMap";
    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access, className);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }
  
  public void test4() throws Exception {
    // java.util.EnumMap
    String decl = "<K extends java.lang.Enum<K>, V> extends java.util.AbstractMap<K, V> implements java.io.Serializable, java.lang.Cloneable";
    String signature = "<K:Ljava/lang/Enum<TK;>;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/io/Serializable;Ljava/lang/Cloneable;";

    String className = "java/util/EnumMap";
    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access, className);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }
  
}

