/* $Id: ClassSignatureDecompilerTest.java,v 1.4 2005-01-19 01:41:07 ekuleshov Exp $ */

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

  public void testClassEnum() throws Exception {
    // java.lang.Enum
    String decl = "<E extends Enum<E>>"; 
    String signature = "<E:Ljava/lang/Enum<TE;>;>Ljava/lang/Object;Ljava/lang/Comparable<TE;>;Ljava/io/Serializable;";

    int access = Opcodes.ACC_ENUM | Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }

  public void testClassTypeVariable() throws Exception {
    // java.lang.reflect.TypeVariable
    String decl = "<D extends java.lang.reflect.GenericDeclaration> extends java.lang.reflect.Type";
    String signature = "<D::Ljava/lang/reflect/GenericDeclaration;>Ljava/lang/Object;Ljava/lang/reflect/Type;";

    int access = Opcodes.ACC_INTERFACE | Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }

  public void testClassConcurrentHashMap() throws Exception {
    // java.util.concurrent.ConcurrentHashMap
    String decl = "<K, V> extends java.util.AbstractMap<K, V> implements java.util.concurrent.ConcurrentMap<K, V>, java.io.Serializable";
    String signature = "<K:Ljava/lang/Object;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/util/concurrent/ConcurrentMap<TK;TV;>;Ljava/io/Serializable;";

    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }
  
  public void testClassEnumMap() throws Exception {
    // java.util.EnumMap
    String decl = "<K extends java.lang.Enum<K>, V> extends java.util.AbstractMap<K, V> implements java.io.Serializable, java.lang.Cloneable";
    String signature = "<K:Ljava/lang/Enum<TK;>;V:Ljava/lang/Object;>Ljava/util/AbstractMap<TK;TV;>;Ljava/io/Serializable;Ljava/lang/Cloneable;";

    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptClass( d);
    
    assertEquals( decl, d.toString());
  }
  
  public void testFieldEnclosingClass() {
    // F java.lang.Class$EnclosingMethodInfo.enclosingClass 
    String decl = "java.lang.Class<?>";
    String signature = "Ljava/lang/Class<*>;";

    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptType( d);
    
    assertEquals( decl, d.toString());
  }
  
  public void testField() {
    // F java.lang.Class.cachedConstructor Ljava/lang/reflect/Constructor<TT;>;
    String decl = "java.lang.reflect.Constructor<T>";
    String signature = "Ljava/lang/reflect/Constructor<TT;>;";

    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptType( d);
    
    assertEquals( decl, d.toString());
  }
  
  public void testFieldEnumConstants() {
    // F java.lang.Class.enumConstants [TT;
    String decl = "T[]";
    String signature = "[TT;";

    int access = Opcodes.ACC_PUBLIC;
    ClassSignatureDecompiler d = new ClassSignatureDecompiler( access);
    SignatureReader r = new SignatureReader( signature);
    r.acceptType( d);
    
    assertEquals( decl, d.toString());
  }
  
}

