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
 */

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.ByteVector;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.FileOutputStream;
import java.io.PrintWriter;

/**
 * @author Eric Bruneton
 */

public class Attributes extends ClassLoader {

  public static void main (final String args[]) throws Exception {
    // loads the original class and adapts it
    ClassReader cr = new ClassReader("CommentAttribute");
    ClassWriter cw = new ClassWriter(false);
    ClassVisitor cv = new AddCommentClassAdapter(cw);
    cr.accept(cv, new Attribute[] { new CommentAttribute("") }, false);
    byte[] b = cw.toByteArray();

    // stores the adapted class on disk
    FileOutputStream fos = new FileOutputStream("CommentAttribute.class.new");
    fos.write(b);
    fos.close();

    // "disassembles" the adapted class
    cr = new ClassReader(b);
    cv = new TraceClassVisitor(null, new PrintWriter(System.out));
    cr.accept(cv, new Attribute[] { new CommentAttribute("") }, false);
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

  protected Attribute read (
    ClassReader cr,
    int off,
    int len,
    char[] buf,
    int codeOff,
    Label[] labels)
  {
    return new CommentAttribute(cr.readUTF8(off, buf));
  }

  protected ByteVector write (
    ClassWriter cw,
    byte[] code,
    int len,
    int maxStack,
    int maxLocals)
  {
    return new ByteVector().putShort(cw.newUTF8(comment));
  }
}
