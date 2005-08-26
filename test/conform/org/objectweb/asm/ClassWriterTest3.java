/***
 * ASM tests
 * Copyright (c) 2002-2005 France Telecom
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
package org.objectweb.asm;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.objectweb.asm.AbstractTest.ClassFilter;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.util.TraceClassVisitor;

import junit.framework.TestSuite;

/**
 * ClassWriter tests.
 * 
 * @author Eric Bruneton
 */
public class ClassWriterTest3 extends AbstractTest {

    private final TestClassLoader LOADER = new TestClassLoader();

    public static TestSuite suite() throws Exception {
        return new ClassWriterTest3().getSuite();
    }

    public void test() throws Exception {
        ClassReader cr = new ClassReader(is);
        ClassWriter cw = new ClassWriter(false, true, true);
        cr.accept(cw, false);

        byte[] b = cw.toByteArray();

        // computed frames sometime from original ones
        // assertEquals(cr, new ClassReader(b));

        // check that generated frames can be read by ClassReader
        new ClassReader(b).accept(new EmptyVisitor(), false);

        // check that the new verifier accepts the generated frames
        try {
            /*
             * apparently a class is not verified before it is instantiated for
             * the first time. Hence the testClass method.
             */
            testClass(LOADER.defineClass(n, b));
        } catch (ClassFormatError cfe) {
            fail(cfe.getMessage());
        } catch (VerifyError cfe) {
            StringWriter sw = new StringWriter();
            ClassVisitor cv = new TraceClassVisitor(new PrintWriter(sw));
            new ClassReader(b).accept(new ClassFilter(cv), false);
            assertEquals(sw.toString(), cfe.getMessage());
            //fail(cfe.getMessage() + "\n" + sw);
        } catch (Throwable ignored) {
        }
    }

    // ------------------------------------------------------------------------

    static class TestClassLoader extends ClassLoader {

        public Class defineClass(final String name, final byte[] b) {
            return defineClass(name, b, 0, b.length);
        }
    }

    static void testClass(final Class c) {
        Constructor[] cons = c.getConstructors();
        for (int i = 0; i < cons.length; ++i) {
            try {
                cons[i].newInstance(newInstance(cons[i].getParameterTypes()));
            } catch (InvocationTargetException e) {
                continue;
            } catch (InstantiationException e) {
                continue;
            } catch (IllegalAccessException e) {
                continue;
            }
            break;
        }
    }

    static Object[] newInstance(final Class[] formals) {
        Object[] actuals = new Object[formals.length];
        for (int i = 0; i < actuals.length; ++i) {
            actuals[i] = newInstance(formals[i]);
        }
        return actuals;
    }

    static Object newInstance(final Class c) {
        if (c == Integer.TYPE) {
            return new Integer(0);
        } else if (c == Float.TYPE) {
            return new Float(0);
        } else if (c == Long.TYPE) {
            return new Long(0);
        } else if (c == Double.TYPE) {
            return new Double(0);
        } else if (c == Byte.TYPE) {
            return new Byte((byte) 0);
        } else if (c == Character.TYPE) {
            return new Character((char) 0);
        } else if (c == Short.TYPE) {
            return new Short((short) 0);
        } else if (c == Boolean.TYPE) {
            return new Boolean(false);
        } else {
            return null;
        }
    }
}
