/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2004 INRIA, France Telecom
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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.signature.ClassSignatureVisitor;
import org.objectweb.asm.signature.MethodSignatureVisitor;
import org.objectweb.asm.signature.TypeSignatureVisitor;

public class TraceSignatureVisitor implements
  ClassSignatureVisitor,
  MethodSignatureVisitor,
  TypeSignatureVisitor
{

  private StringBuffer buf = new StringBuffer();
  private StringBuffer returnBuf = null;

  private boolean isInterface;
  private boolean seenFormalParameter = false;
  private boolean seenInterfaceBound = false;
  private boolean seenParameter = false;
  private boolean seenInterface = false;
  private boolean seenException = false;
  private boolean seenArray = false;

  /**
   * Stack used to keep track of class types that have arguments. Each element
   * of this stack is a boolean encoded in one bit. The top of the stack is the
   * lowest order bit. Pushing false = *2, pushing true = *2+1, popping = /2.
   */

  private int argumentStack;

  private String separator = "";


  public TraceSignatureVisitor( int access) {
    isInterface = ( access & Opcodes.ACC_INTERFACE)!=0;
    this.buf = new StringBuffer();
  }
  
  public TraceSignatureVisitor( int access, StringBuffer buf) {
    isInterface = ( access & Opcodes.ACC_INTERFACE)!=0;
    this.buf = buf;
  }
  

  // AbstractSignatureVisitor
  
  public void visitFormalTypeParameter (String name) {
    buf.append(seenFormalParameter ? ", " : "<").append(name);
    seenFormalParameter = true;
    seenInterfaceBound = false;
  }

  public TypeSignatureVisitor visitClassBound () {
    separator = " extends ";
    return this;
  }

  public TypeSignatureVisitor visitInterfaceBound () {
    separator = seenInterfaceBound ? ", " : (isInterface ? " extends " : " implements ");
    seenInterfaceBound = true;
    return this;
  }

  
  // ClassSignatureVisitor
  
  public TypeSignatureVisitor visitSuperclass () {
    endFormals();
    separator = " extends ";
    return this;
  }

  public TypeSignatureVisitor visitInterface () {
    separator = seenInterface ? ", " : (isInterface ? " extends " : " implements ");
    seenInterface = true;
    return this;
  }

  
  // MethodsignatureVisitor
  
  public TypeSignatureVisitor visitParameterType () {
    endFormals();
    if (!seenParameter) {
      seenParameter = true;
      buf.append('(');
    } else {
      buf.append(", ");
    }
    return this;
  }

  public TypeSignatureVisitor visitReturnType () {
    endFormals();
    if (!seenParameter) {
      buf.append('(');
    }
    buf.append(')');
    
    returnBuf = new StringBuffer();
    return new TraceSignatureVisitor( 0, returnBuf);
  }

  public TypeSignatureVisitor visitExceptionType () {
    buf.append(seenException ? ", " : " throws ");
    seenException = true;
    return this;
  }

  
  // TypeSignatureVisitor
  
  public void visitBaseType (char descriptor) {
    switch( descriptor) {
      case 'V':
        buf.append( "void");  break;
      case 'B':
        buf.append( "byte");  break;
      case 'J':
        buf.append( "long");  break;
      case 'Z':
        buf.append( "boolean");  break;
      case 'I':
        buf.append( "int");  break;
      case 'S':
        buf.append( "short");  break;
      case 'C':
        buf.append( "char");  break;
      case 'F':
        buf.append( "float");  break;
      case 'D':
        buf.append( "double");  break;
      default:
        throw new IllegalArgumentException( "Invalid descriptor "+descriptor);
    }
  }

  public void visitTypeVariable (String name) {
    buf.append(name);
  }

  public TypeSignatureVisitor visitArrayType () {
    // TODO where [] must be added? requires to return a new TraceSignatureVisitor instance?
    seenArray = true;
    return this;
  }

  public void visitClassType (String name) {
    if (!"java/lang/Object".equals(name)) {
      buf.append(separator).append(name.replace('/', '.'));
    }
    separator = "";
    argumentStack *= 2;
  }

  public void visitInnerClassType (String name) {
    // TODO
  }

  public void visitTypeArgument () {
    if (argumentStack%2 == 0) {
      ++argumentStack;
      buf.append("<");
    } else {
      buf.append(", ");
    }
    buf.append( "?");
  }

  public TypeSignatureVisitor visitTypeArgument (char tag) {
    if (argumentStack%2 == 0) {
      ++argumentStack;
      buf.append("<");
    } else {
      buf.append(", ");
    }

    if( tag==TypeSignatureVisitor.EXTENDS) {
      buf.append( "? extends ");
    } else if( tag==TypeSignatureVisitor.SUPER) {
      buf.append( "? super ");
    }
    
    return this;
  }

  public void visitEnd () {
    // TODO add an array stack?
    if( seenArray) {
      buf.append( "[]");
      seenArray = false;
    } else {
      if (argumentStack%2 == 1) {
        buf.append(">");
      }
      argumentStack /= 2;
    }
  }

  public String getDeclaration () {
    return buf.toString();
  }

  public String getReturnType () {
    return returnBuf.toString();
  }

  // -----------------------------------------------

  private void endFormals () {
    if (seenFormalParameter) {
      buf.append(">");
      seenFormalParameter = false;
    }
  }
}
