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

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Observable;
import java.util.Observer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;


/**
 * Processor is a command line tool that can be used for
 * bytecode waving using XSL transformation.
 * <p>
 * In order to use a concrete XSLT engine, system property
 * <tt>javax.xml.transform.TransformerFactory</tt> must be set to
 * one of the following values.
 * 
 * <blockquote>
 * <table border="1" cellspacing="0" cellpadding="3">
 * <tr>
 * <td>jd.xslt</td>
 * <td>jd.xml.xslt.trax.TransformerFactoryImpl</td>   
 * </tr>
 *
 * <tr>
 * <td>Saxon</td>
 * <td>net.sf.saxon.TransformerFactoryImpl</td>
 * </tr>
 *
 * <tr>
 * <td>Caucho</td>
 * <td>com.caucho.xsl.Xsl</td>   
 * </tr>
 *
 * <tr>
 * <td>Xalan interpeter</td>   
 * <td>org.apache.xalan.processor.TransformerFactory</td>
 * </tr>
 * 
 * <tr>
 * <td>Xalan xsltc</td>
 * <td>org.apache.xalan.xsltc.trax.TransformerFactoryImpl</td>
 * </tr>
 * </table>
 * </blockquote>
 * 
 * @author Eugene Kuleshov
 */
public class Processor extends Observable {
  private static final String XML2XML = "xml2xml";
  private static final String CODE2CODE = "code2code";
  private static final String XML2CODE = "xml2code";
  private static final String CODE2XML = "code2xml";
  private static final String CODE2ASM = "code2asm";

  private String command = null;
  private InputStream input = null;
  private OutputStream output = null;
  private Source xslt = null;
  private boolean computeMax;
  private boolean singleDocument;
  
  
  public Processor( String command, InputStream input, OutputStream output, Source xslt, boolean computeMax, boolean singleDocument) {
  	this.command = command;
  	this.input = input;
  	this.output = output;
  	this.xslt = xslt;
    this.computeMax = computeMax;
    this.singleDocument = singleDocument;
  }
	
  private void process() throws TransformerException, IOException, SAXException {
    ZipInputStream zis = new ZipInputStream( input);
    ZipOutputStream zos = new ZipOutputStream( new BufferedOutputStream( output));
    OutputStreamWriter osw = new OutputStreamWriter( zos);
    
    /*
    XSLTC xsltc = new XSLTC();
    xsltc.init();
    xsltc.setOutputType( XSLTC.FILE_OUTPUT);
    xsltc.setTemplateInlining(true);
    xsltc.compile( c.getResource( "copy.xsl"));
    System.err.println( xsltc.getClassName());
    */
    
    Thread.currentThread().setContextClassLoader( getClass().getClassLoader());
    
    TransformerFactory tf = TransformerFactory.newInstance();
    if( !tf.getFeature( SAXSource.FEATURE) || !tf.getFeature( SAXResult.FEATURE)) return;
    
    SAXTransformerFactory saxtf = ( SAXTransformerFactory) tf;
    Templates templates = null;
    if( xslt!=null) {
      templates = saxtf.newTemplates( xslt);
    }
    
    ZipEntry outputEntry = null;
    SubdocumentHandler rootHandler = null;
    ContentHandlerFactory handlerFactory = null;
    if( singleDocument) {
      outputEntry = new ZipEntry( "classes.xml");
      zos.putNextEntry( outputEntry);
      
      // rootHandler = new SAXWriter( new OutputStreamWriter( zos));
      rootHandler = new SubdocumentHandler( "class", new SAXWriter( osw, false), getHandlerFactory( zos, saxtf, templates));
      rootHandler.startDocument();
      rootHandler.startElement( "", "classes", "classes", new AttributesImpl());
      osw.flush();
      
      handlerFactory = new SubdocumentHandlerFactory( rootHandler);   

    } else {
      handlerFactory = getHandlerFactory( zos, saxtf, templates);
    
    }
    
    long l1 = System.currentTimeMillis();

    int n = 0;
    ZipEntry ze = null;
    while(( ze = zis.getNextEntry())!=null) {
      if( !singleDocument) {
        outputEntry = new ZipEntry( getName( ze));
        zos.putNextEntry( outputEntry);
      }

      if( isClassEntry( ze)) {
        processEntry( zis, ze, zos, handlerFactory);
      } else {
        copyEntry( zis, zos);
      }
      zos.flush();
      if( !singleDocument) {
        zos.closeEntry();
      }
      
      n++;
      if(( n % 100)==0) {
        setChanged();
        notifyObservers( Integer.toString( n));
      }
    }
    long l2 = System.currentTimeMillis();

    if( singleDocument) {
      rootHandler.endElement( "", "classes", "classes");
      rootHandler.endDocument();
      osw.flush();
      
      zos.closeEntry();
    }
    zos.flush();
    zos.close();

    setChanged();
    notifyObservers( ""+( l2-l1)+"ms "+(1000f*n/( l2-l1))+"files/sec");
  }

  private void copyEntry( InputStream is, OutputStream os) throws IOException {
    if( singleDocument) return;
      
    byte[] buff = new byte[2048];
    int n;
    while ((n = is.read(buff)) != -1) {
      os.write(buff, 0, n);
    }
  }

  private boolean isClassEntry( ZipEntry ze) {
    return ze.getName().endsWith( ".class") || ze.getName().endsWith( ".class.xml");
  }

  private void processEntry( ZipInputStream zis, ZipEntry ze, OutputStream os, ContentHandlerFactory handlerFactory) {
    ContentHandler handler = handlerFactory.createContentHandler();
    try {
      if( CODE2CODE.equals( command) || CODE2XML.equals( command)) {
        ClassReader cr = new ClassReader( readEntry( zis, ze));
        cr.accept( new SAXClassAdapter( handler, cr.getVersion(), singleDocument), false);
      
      } else if( CODE2ASM.equals( command)) {
        ClassReader cr = new ClassReader( readEntry( zis, ze));
        cr.accept( new TraceClassVisitor( null, new PrintWriter( os)), false);
        
      } else {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler( handler);
        if( singleDocument) {
          reader.parse( new InputSource( zis));
        } else {
          reader.parse( new InputSource( new ByteArrayInputStream( readEntry(zis, ze))));
        }      
      }
    } catch( Exception ex) {
      setChanged();
      notifyObservers( ze.getName());
      setChanged();
      notifyObservers( ex);
    }
  }

  private ContentHandlerFactory getHandlerFactory( OutputStream os, SAXTransformerFactory saxtf, Templates templates) {
    ContentHandlerFactory factory = null;
    if( templates==null) {
      // handler = saxtf.newTransformerHandler();
      if( CODE2CODE.equals( command) || XML2CODE.equals( command)) {
        factory = new ASMContentHandlerFactory( os, computeMax);
      } else {
        factory = new SAXWriterFactory( os, true); 
      }      
    } else {
      if( CODE2CODE.equals( command) || XML2CODE.equals( command)) {
        factory = new ASMTransformerHandlerFactory( saxtf, templates, os, computeMax);
      } else {
        factory = new TransformerHandlerFactory( saxtf, templates, os, singleDocument);
      }
    }
    
    return factory;
  }

  private String getName( ZipEntry ze) {
    String name = ze.getName();
    if( isClassEntry( ze)) {
      if( XML2CODE.equals( command)) {
        name = name.substring( 0, name.length()-4);  // .class.xml to .class
      } else if( CODE2XML.equals( command)) {
        name = name.concat( ".xml");  // .class to .class.xml
      } else if( CODE2ASM.equals( command)) {
        name = name.substring( 0, name.length()-6).concat( ".asm");
      }
    } 
    return name;
  }

  private byte[] readEntry(ZipInputStream zis, ZipEntry ze) throws IOException {
    long size = ze.getSize();
    if (size > -1) {
      byte[] buff = new byte[(int) size];
      zis.read(buff);
      return buff;
    } else {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buff = new byte[2048];
      int n;
      while ((n = zis.read(buff)) != -1) {
        bos.write(buff, 0, n);
      }
      return bos.toByteArray();
    }
  }
  
  
  public static void main( String[] args) throws Exception {
    String command = null;
    InputStream is = null;
    OutputStream os = null;
    Source xslt = null;
    boolean computeMax = false;
    boolean singleDocument = false;
    
    for( int i = 0; i<args.length; i++) {
      if( "-in".equals( args[ i])) {
        is = new FileInputStream( args[ ++i]);    
      
      } else if( "-out".equals( args[ i])) {
        os = new FileOutputStream( args[ ++i]);    
      
      } else if( "-xslt".equals( args[ i])) {
        xslt = new StreamSource( new FileInputStream( args[ ++i]));    
      
      } else if( CODE2XML.equals( args[ i])) {
        command = CODE2XML;
      
      } else if( CODE2CODE.equals( args[ i])) {
        command = CODE2CODE;
      
      } else if( CODE2ASM.equals( args[ i])) {
        command = CODE2ASM;
      
      } else if( XML2CODE.equals( args[ i])) {
        command = XML2CODE;
      
      } else if( XML2XML.equals( args[ i])) {
        command = XML2XML;
      
      } else if( "-computemax".equals( args[ i].toLowerCase())) {
        computeMax = true;
      
      } else if( "-singledocument".equals( args[ i].toLowerCase())) {
        singleDocument = true;
      
      } else {
        showUsage();
        return;
        
      }
    }
    
    if( command==null || is==null || os==null) {
      showUsage();
      return;
    }
    
    Processor m = new Processor( command, is, os, xslt, computeMax, singleDocument);
    m.addObserver( new Observer() {
      public void update( Observable o, Object arg) {
        if( arg instanceof Throwable) {
          (( Throwable) arg).printStackTrace();
        } else {
          System.err.println( arg);
        }
      } });
    m.process();
  }

  private static void showUsage() {
    System.err.println( "Usage: Main <command> -in <input jar> -out <output jar> [-xslt <xslt fiel>]");
    System.err.println( "  <command> is one of "+CODE2XML+", "+XML2CODE+", "+XML2XML+", "+CODE2XML+", "+CODE2ASM);
  }

  
  /**
   * SAXWriterFactory
   */
  private static final class SAXWriterFactory implements ContentHandlerFactory {
    private Writer w;
    private boolean optimizeEmptyElements;
  
    public SAXWriterFactory( OutputStream os, boolean optimizeEmptyElements) {
      this.w = new OutputStreamWriter( os);
      this.optimizeEmptyElements = optimizeEmptyElements;
    }
    
    public ContentHandler createContentHandler() {
      return new SAXWriter( w, optimizeEmptyElements);
    }
    
  }
  
  /**
   * ASMContentHandlerFactory
   */
  private static final class ASMContentHandlerFactory implements ContentHandlerFactory {
    private Writer w;
    private boolean computeMax;
  
    public ASMContentHandlerFactory( OutputStream os, boolean computeMax) {
      this.w = new OutputStreamWriter( os);
      this.computeMax = computeMax;
    }
    
    public ContentHandler createContentHandler() {
      return new SAXWriter( w, computeMax);
    }
    
  }
  
  /**
   * ASMTransformerHandlerFactory
   */
  private static final class ASMTransformerHandlerFactory implements ContentHandlerFactory {
    private SAXTransformerFactory saxtf;
    private Templates templates;
    private OutputStream os;
    private boolean computeMax;
  
    public ASMTransformerHandlerFactory(SAXTransformerFactory saxtf, Templates templates, 
          OutputStream os, boolean computeMax) {
      this.saxtf = saxtf;
      this.templates = templates;
      this.os = os;
      this.computeMax = computeMax;
    }
    
    public ContentHandler createContentHandler() {
      try {
        TransformerHandler handler = saxtf.newTransformerHandler( templates);
        handler.setResult( new SAXResult( new ASMContentHandler( os, computeMax)));
        return handler;
      } catch( TransformerConfigurationException ex) {
        throw new RuntimeException( ex.toString());
      }
    }
    
  }
  
  /**
   * TransformerHandlerFactory
   */
  private static final class TransformerHandlerFactory implements ContentHandlerFactory {
    private SAXTransformerFactory saxtf;
    private Templates templates;
    private OutputStream os;
    private boolean singleDocument;
  
    public TransformerHandlerFactory(SAXTransformerFactory saxtf, Templates templates, 
          OutputStream os, boolean singleDocument) {
      this.saxtf = saxtf;
      this.templates = templates;
      this.os = os;
      this.singleDocument = singleDocument;
    }
    
    public ContentHandler createContentHandler() {
      Result result;
      if( singleDocument) {
        SAXWriter saxWriter = new  SAXWriter( new OutputStreamWriter( os), true);
        result = new SAXResult( saxWriter);
        (( SAXResult) result).setLexicalHandler( saxWriter);
      } else {
        result = new StreamResult( os);
      }
  
      try {
        TransformerHandler handler = saxtf.newTransformerHandler( templates);
        handler.setResult( result);
        return handler;
  
      } catch( TransformerConfigurationException ex) {
        throw new RuntimeException( ex.toString());
      }
    }
  }
  
  
  /**
   * SubdocumentHandlerFactory
   */
  private static final class SubdocumentHandlerFactory implements ContentHandlerFactory {
    private ContentHandler subdocumentHandler;

    public SubdocumentHandlerFactory( SubdocumentHandler subdocumentHandler) {
      this.subdocumentHandler = subdocumentHandler;
    }
    
    public ContentHandler createContentHandler() {
      return subdocumentHandler;
    }
    
  }

  
  /**
   * A {@link org.xml.sax.ContentHandler ContentHandler} and
   * {@link org.xml.sax.ext.LexicalHandler LexicalHandler} that serializes
   * XML from SAX 2.0 events into {@link java.io.Writer Writer}. 
   * 
   * <i><blockquote>  
   * This implementation does not support namespaces,
   * entity definitions (uncluding DTD), CDATA and text elements.
   * </blockquote></i>
   */
  private static class SAXWriter extends DefaultHandler implements LexicalHandler {
    private static final char[] OFF = "                                                                                                        ".toCharArray();
    
    private Writer w;
    private boolean optimizeEmptyElements;
    
    private boolean openElement = false;
    private int ident = 0;

    
    /**
     * Creates <code>SAXWriter</code>.
     * 
     * @param w writer
     * @param optimizeEmptyElements if set to <code>true</code>,
     *    short XML syntax will be used for empty elements 
     */
    public SAXWriter( Writer w, boolean optimizeEmptyElements) {
      this.w = w;
      this.optimizeEmptyElements = optimizeEmptyElements;
    }
    
    public void startElement( String ns, String localName, String qName, Attributes atts) throws SAXException {
      try {
        closeElement();

        writeIdent();
        w.write( "<".concat( qName));
        if( atts!=null || atts.getLength()>0) writeAttributes( atts);

        if( !optimizeEmptyElements) {
          w.write( ">\n");
        } else {      
          openElement = true;
        }
        ident += 2;
      
      } catch( IOException ex) {
        throw new SAXException(ex);
      
      }
    }

    public void endElement( String ns, String localName, String qName) throws SAXException {
      ident -= 2;
      try {
        if( openElement) {
          w.write( "/>\n");
          openElement = false;
        } else {
          writeIdent();
          w.write( "</"+qName+">\n");
        }

      } catch( IOException ex) {
        throw new SAXException(ex);
      
      }
    }

    public void endDocument() throws SAXException {
      try {
        w.flush();

      } catch( IOException ex) {
        throw new SAXException(ex);

      }
    }
    
    public void comment( char[] ch, int off, int len) throws SAXException {
      try {
        closeElement();

        writeIdent();
        w.write( "<!-- ");
        w.write( ch, off, len);
        w.write( " -->\n");
        
      } catch( IOException ex) {
        throw new SAXException(ex);
    
      }
    }

    public void startDTD( String arg0, String arg1, String arg2) throws SAXException {
    }

    public void endDTD() throws SAXException {
    }

    public void startEntity( String arg0) throws SAXException {
    }

    public void endEntity( String arg0) throws SAXException {
    }

    public void startCDATA() throws SAXException {
    }

    public void endCDATA() throws SAXException {
    }

    
    
    private void writeAttributes( Attributes atts) throws IOException {
      StringBuffer sb = new StringBuffer();
      int len = atts.getLength();
      for( int i = 0; i<len; i++) {
        sb.append( " ").append( atts.getLocalName( i)).append( "=\"")
          .append( esc( atts.getValue( i))).append( "\"");
      }
      w.write( sb.toString());
    }
    
    /**
     * Encode string with escaping.
     * 
     * @param str string to encode.
     * @return encoded string
     */
    private String esc( String str) {
      StringBuffer sb = new StringBuffer( str.length());
      for( int i = 0; i < str.length(); i++) {
        char ch = str.charAt( i);
        switch( ch) {
          case '&':
            sb.append( "&amp;");
            break;
          
          case '<':
            sb.append( "&lt;");
            break;
          
          case '>':
            sb.append( "&gt;");
            break;
          
          case '\"':
            sb.append( "&quot;");
            break;
            
          default:
            if( ch>0x7f) {
              sb.append( "&#").append( Integer.toString( ch)).append( ';');
            } else {
              sb.append( ch);
            }
          
        }
      }
      return sb.toString();
    }
    
    private void writeIdent() throws IOException {
      int n = ident;
      while( n>0) {
        if( n>OFF.length) {
          w.write( OFF);
          n -= OFF.length;
        } else {
          w.write( OFF, 0, n);
          n = 0;
        }
      }
    }

    private void closeElement() throws IOException {
      if( openElement) {
        w.write( ">\n");
      }
      openElement = false;
    }
    
  }
  
}

