/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2005 INRIA, France Telecom
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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.objectweb.asm.ClassAdapter;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.JSRInliner;

/**
 * Removes JSR instructions and inlines the referenced subroutines.
 * 
 * @author Niko Matsakis
 */
public class JSRInlinerVisitor {

    public static void main(String args[])
            throws FileNotFoundException, IOException
    {
        try {
            int i = 0;
            boolean traceclassvisitor = false;

            for (; i < args.length && args[i].startsWith("-"); i++) {
                if (args[i].equals("--trace")) {
                    traceclassvisitor = true;
                } else {
                    throw new UsageException("Invalid option: " + args[i]);
                }
            }

            if (i == args.length) {
                throw new UsageException("No classes listed");
            }

            for (; i < args.length; i++) {
                ClassReader cr;
                if (args[i].endsWith(".class") || args[i].indexOf('\\') > -1
                        || args[i].indexOf('/') > -1)
                {
                    cr = new ClassReader(new FileInputStream(args[i]));
                } else {
                    cr = new ClassReader(args[i]);
                }

                // We always generate a ClassWriter
                ClassWriter cw = new ClassWriter(cr, 0);
                ClassVisitor cv = cw;
                if (traceclassvisitor) {
                    cv = new TraceClassVisitor(cv, new PrintWriter(System.err));
                }
                cv = new ClassAdapter(cv) {
                    public MethodVisitor visitMethod(
                        int access,
                        String name,
                        String desc,
                        String signature,
                        String[] exceptions)
                    {
                        MethodVisitor mv = super.visitMethod(access,
                                name,
                                desc,
                                signature,
                                exceptions);
                        return new JSRInliner(mv,
                                access,
                                name,
                                desc,
                                signature,
                                exceptions);
                    }
                };
                cr.accept(cv, 0);

                if (!traceclassvisitor) {
                    System.out.write(cw.toByteArray());
                }
            }
        } catch (UsageException e) {
            System.err.println("Usage: java org.objectweb.asm.util.JsrInlinerVisitor "
                    + "[--trace] <class files>");
            System.err.println("Error: " + e.toString());
        }
    }

    static class UsageException extends Exception {

        UsageException(String s) {
            super(s);
        }
    }
}
