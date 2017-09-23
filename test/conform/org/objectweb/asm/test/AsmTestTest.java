/***
 * ASM: a very small and fast Java bytecode manipulation framework
 * Copyright (c) 2000-2011 INRIA, France Telecom
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
package org.objectweb.asm.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.objectweb.asm.Opcodes;

/**
 * Unit tests for {@link AsmTest}.
 * 
 * @author Eric Bruneton
 */
@RunWith(Parameterized.class)
public class AsmTestTest extends AsmTest {

    /**
     * @return test parameters to test all the precompiled classes with the ASM6
     *         API.
     */
    @Parameters(name = NAME)
    public static Collection<Object[]> data() {
        return data(Opcodes.ASM6);
    }

    /**
     * Tests that {@link #readClass()} can load each precompiled class. Also
     * tests {@link #assertThatClass(byte[])}, and {@link #getInternalName()}.
     * 
     * @throws IOException
     *             if the precompiled test class can't be loaded.
     */
    @Test
    public void testReadClass() throws IOException {
        assertEquals(asmApi, Opcodes.ASM6);
        assertThatClass(readClass()).contains(getInternalName());
    }

    /**
     * Tests that each precompiled class can be loaded successfully with
     * {@link #loadAndInstantiate(String, byte[])}.
     * 
     * @throws IOException
     *             if the precompiled test class can't be loaded.
     */
    @Test
    public void testLoadAndInstantiate() throws IOException {
        if (classIsMoreRecentThanCurrentJdk()) {
            thrown.expect(UnsupportedClassVersionError.class);
        }
        assertTrue(loadAndInstantiate(className, readClass()));
    }

    /**
     * Tests that {@link #loadAndInstantiate(String, byte[])} fails when trying
     * to load an invalid or unverifiable class.
     */
    @Test
    public void testLoadAndInstantiate_invalidClass() throws IOException {
        if (classIsMoreRecentThanCurrentJdk()) {
            return;
        }
        byte[] classContent = readClass();
        if (maybeRemoveAttributes(classContent, "StackMapTable")) {
            // jdk8.AllStructures can't be instantiated because it is abstract.
            // Bytecode verification is not triggered by simply loading the
            // class, so no exception is thrown although the class is invalid.
            if (!className.equals("jdk8.AllStructures")) {
                thrown.expect(VerifyError.class);
            }
        } else if (maybeRemoveAttributes(classContent, "Code")) {
            thrown.expect(ClassFormatError.class);
        }
        loadAndInstantiate(className, classContent);
    }

    /**
     * "Removes" all the attributes of the given type in a class by altering its
     * name in the constant pool of the class, to make it unrecognizable.
     */
    private boolean maybeRemoveAttributes(byte[] classContent,
            String attributeName) {
        // Changes the first letter of the first occurrence of the attribute
        // name (which should be in the constant pool).
        for (int i = 0; i < classContent.length - attributeName.length(); ++i) {
            if (new String(classContent, i, attributeName.length())
                    .equals(attributeName)) {
                classContent[i] += 1;
                return true;
            }
        }
        return false;
    }

}