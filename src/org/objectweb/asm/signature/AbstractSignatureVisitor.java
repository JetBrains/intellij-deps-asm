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

package org.objectweb.asm.signature;

/**
 * Super interface of {@link ClassSignatureVisitor} and 
 * {@link MethodSignatureVisitor}. The methods of this interface must be called 
 * in the following order: 
 * ( <tt>visitFormalTypeParameter</tt> 
 *   <tt>visitClassBound</tt>? 
 *   <tt>visitInterfaceBound</tt>* )*.
 * 
 * @author Thomas Hallgren
 * @author Eric Bruneton 
 */

public interface AbstractSignatureVisitor {

  /**
   * Visits a formal type parameter.
   * 
   * @param name the name of the formal parameter.
   */
  
  void visitFormalTypeParameter (String name);

  /**
   * Visits the class bound of the last visited formal type parameter.
   * 
   * @return a non null visitor to visit the signature of the class bound.
   */
  
  TypeSignatureVisitor visitClassBound ();

  /**
   * Visits an interface bound of the last visited formal type parameter.
   * 
   * @return a non null visitor to visit the signature of the interface bound.
   */

  TypeSignatureVisitor visitInterfaceBound ();
}
