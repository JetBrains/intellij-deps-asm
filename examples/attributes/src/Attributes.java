/***
 * ASM examples: examples showing how ASM can be used
 * Copyright (c) 2000,2002,2003 INRIA, France Telecom
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
 *
 * Contact: Eric.Bruneton@rd.francetelecom.com
 *
 * Author: Eric Bruneton
 */

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.IOException;

public class Attributes extends ClassLoader {

  public static void main (final String args[]) throws Exception {
    // loads the original class and adapts it
    ClassReader cr = new MyClassReader("CommentAttribute");
    ClassWriter cw = new MyClassWriter(false);
    ClassVisitor cv = new AddCommentClassAdapter(cw);
    cr.accept(cv, false);
    byte[] b = cw.toByteArray();

    // stores the adapted class on disk
    FileOutputStream fos = new FileOutputStream("CommentAttribute.class.new");
    fos.write(b);
    fos.close();

    // "disassembles" the adapted class
    cr = new MyClassReader(b);
    cv = new TraceClassVisitor(null, new PrintWriter(System.out));
    cr.accept(cv, false);
  }
}

class AddCommentClassAdapter extends ClassAdapter implements Constants {

  public AddCommentClassAdapter (final ClassVisitor cv) {
    super(cv);
  }

  public void visit (
    final int access,
    final String name,
    final String superName,
    final String[] interfaces,
    final String sourceFile)
  {
    super.visit(access, name, superName, interfaces, sourceFile);
    visitAttribute(new CommentAttribute("this is a class comment"));
  }

  public void visitField (
    final int access,
    final String name,
    final String desc,
    final Object value,
    final Attribute attrs)
  {
    super.visitField(
      access,
      name,
      desc,
      value,
      new CommentAttribute("this is a field comment", attrs));
  }

  public CodeVisitor visitMethod (
    final int access,
    final String name,
    final String desc,
    final String[] exceptions,
    final Attribute attrs)
  {
    CodeVisitor mv = cv.visitMethod(
      access,
      name,
      desc,
      exceptions,
      new CommentAttribute("this is a method comment", attrs));
    if (mv != null) {
      mv.visitAttribute(new CommentAttribute("this is a code comment"));
    }
    return mv;
  }
}

class CommentAttribute extends Attribute {

  private String comment;

  public CommentAttribute (final String comment) {
    super("Comment");
    this.comment = comment;
  }

  public CommentAttribute (final String comment, final Attribute next) {
    super("Comment");
    this.comment = comment;
    this.next = next;
  }

  public String getComment () {
    return comment;
  }
}

class MyClassReader extends ClassReader {

  public MyClassReader (final byte[] b) {
    super(b);
  }

  public MyClassReader (final InputStream is) throws IOException {
    super(is);
  }

  public MyClassReader (final String name) throws IOException {
    super(name);
  }

  protected Attribute readAttribute (
    final String type,
    final int off,
    final int len,
    final char[] buf)
  {
    if (type.equals("Comment")) {
      return new CommentAttribute(readUTF8(off, buf));
    } else {
      return super.readAttribute(type, off, len, buf);
    }
  }
}

class MyClassWriter extends ClassWriter {

  public MyClassWriter (final boolean computeMaxs) {
    super(computeMaxs);
  }

  protected byte[] writeAttribute (final Attribute attr) {
    if (attr instanceof CommentAttribute) {
      int item = newUTF8(((CommentAttribute)attr).getComment());
      return new byte[] { (byte)(item >>> 8), (byte)item };
    } else {
      return super.writeAttribute(attr);
    }
  }
}
