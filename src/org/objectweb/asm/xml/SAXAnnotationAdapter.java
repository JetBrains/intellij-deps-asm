/***
 * ASM XML Adapter
 * Copyright (c) 2004, Eugene Kuleshov
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

package org.objectweb.asm.xml;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Type;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * SAXAnnotationAdapter
 *
 * @author Eugene Kuleshov
 */
public class SAXAnnotationAdapter implements AnnotationVisitor {
  private final ContentHandler h;
  private final String elementName;
  

  public SAXAnnotationAdapter( ContentHandler h, String elementName, String name, String desc) {
    this(h, elementName, desc, name, -1);
  }

  public SAXAnnotationAdapter( ContentHandler h, String elementName, int parameter, String desc) {
    this(h, elementName, desc, null, parameter);
  }

  private SAXAnnotationAdapter( ContentHandler h, String elementName, String desc, String name, int parameter) {
    this.h = h;
    this.elementName = elementName;
    
    AttributesImpl att = new AttributesImpl();
    if( name!=null) att.addAttribute( "", "name", "name", "", name);
    if( parameter!=-1) att.addAttribute( "", "parameter", "parameter", "", Integer.toString(parameter));
    if( desc!=null) att.addAttribute( "", "desc", "desc", "", desc);
    
    try {
      h.startElement( "", elementName, elementName, att);
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

  
  public void visit( String name, Object value) {
    visitEnum(name, Type.getDescriptor( value.getClass()), value.toString());
  }

  public void visitEnum( String name, String desc, String value) {
    AttributesImpl att = new AttributesImpl();
    if( name!=null) att.addAttribute( "", "name", "name", "", name);
    if( desc!=null) att.addAttribute( "", "desc", "desc", "", desc);
    if( value!=null) att.addAttribute( "", "value", "value", "", SAXClassAdapter.encode( value));
    
    try {
      // TODO finalize element name for annotation values
      h.startElement( "", "value", "value", att);
      h.endElement( "", "value", "value");
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

  public AnnotationVisitor visitAnnotation( String name, String desc) {
    // TODO finalize element name for annotation values
    return new SAXAnnotationAdapter(h, "valueAnnotation", name, desc);
  }

  public AnnotationVisitor visitArray( String name) {
    // TODO Auto-generated method stub
    return new SAXAnnotationAdapter(h, "arrayValue", name, null);
  }

  public void visitEnd() {
    try {
      h.endElement( "", elementName, elementName);
    } catch( SAXException ex) {
      throw new RuntimeException( ex.toString());
    }
  }

}

