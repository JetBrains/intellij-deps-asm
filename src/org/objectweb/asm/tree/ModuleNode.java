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
package org.objectweb.asm.tree;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A node that represents a module declaration.
 * 
 * @author Remi Forax
 */
public class ModuleNode extends ModuleVisitor {
    private int requireCount;
    public String[] requires;
    public int[] requireAccess;
    
    private int exportCount;
    public String[] exports;
    public String[][] exportTos;
    
    private int useCount;
    public String[] uses;
   
    private int provideCount;
    public String[] provides;
    public String[] provideWiths;

    public ModuleNode() {
        super(Opcodes.ASM6);
    }
    
    public ModuleNode(final int api,
            final String[] requires, final int[] requireAccess,
            final String[] exports, final String[][] exportTos,
            final String[] uses,
            final String[] provides, final String[] provideWiths) {
        super(Opcodes.ASM6);
        this.requires = requires;
        this.requireCount = requires.length;
        this.requireAccess = requireAccess;
        this.exports = exports;
        this.exportCount = exports.length;
        this.exportTos = exportTos;
        this.uses = uses;
        this.useCount = uses.length;
        this.provides = provides;
        this.provideCount = provides.length;
        this.provideWiths = provideWiths;
        if (getClass() != ModuleNode.class) {
            throw new IllegalStateException();
        }
    }

    private static String[] copyOf(String[] array, int newSize) {
        String[] newArray = new String[newSize];
        System.arraycopy(array, 0, newArray, 0, Math.min(newSize, array.length));
        return newArray;
    }
    private static String[][] copyOf(String[][] array, int newSize) {
        String[][] newArray = new String[newSize][];
        System.arraycopy(array, 0, newArray, 0, Math.min(newSize, array.length));
        return newArray;
    }
    private static int[] copyOf(int[] array, int newSize) {
        int[] newArray = new int[newSize];
        System.arraycopy(array, 0, newArray, 0, Math.min(newSize, array.length));
        return newArray;
    }
    
    private static String[] trim(String[] array, int size) {
        return (array.length == size)? array: copyOf(array, size);
    }
    private static String[][] trim(String[][] array, int size) {
        return (array.length == size)? array: copyOf(array, size);
    }
    private static int[] trim(int[] array, int size) {
        return (array.length == size)? array: copyOf(array, size);
    }
    
    @Override
    public void visitRequire(String module, int access) {
        if (requires == null) {
            requires = new String[8];
            requireAccess = new int[8];
        }
        if (requireCount == requires.length) {
            requires = copyOf(requires, requireCount << 1);
            requireAccess = copyOf(requireAccess, requireCount << 1);
        }
        requires[requireCount] = module;
        requireAccess[requireCount++] = access;
    }
    
    @Override
    public void visitExport(String packaze, String... modules) {
        if (exports == null) {
            exports = new String[8];
            exportTos = new String[8][];
        }
        if (exportCount == exports.length) {
            exports = copyOf(exports, exportCount << 1);
            exportTos = copyOf(exportTos, exportCount << 1);
        }
        exports[exportCount] = packaze;
        exportTos[exportCount++] = modules;
    }
    
    @Override
    public void visitUse(String service) {
        if (uses == null) {
            uses = new String[8];
        }
        if (useCount == uses.length) {
            uses = copyOf(uses, useCount << 1);
        }
        uses[useCount++] = service;
    }
    
    @Override
    public void visitProvide(String service, String impl) {
        if (provides == null) {
            provides = new String[8];
            provideWiths = new String[8];
        }
        if (provideCount == provides.length) {
            provides = copyOf(provides, provideCount << 1);
            provideWiths = copyOf(provideWiths, provideCount << 1);
        }
        provides[provideCount] = service;
        provideWiths[provideCount++] = impl;
    }
    
    @Override
    public void visitEnd() {
        if (requires != null) {
            requires = trim(requires, requireCount);
            requireAccess = trim(requireAccess, requireCount);
        }
        if (exports != null) {
            exports = trim(exports, exportCount);
            exportTos = trim(exportTos, exportCount);
        }
        if (uses != null) {
            uses = trim(uses, useCount);
        }
        if (provides != null) {
            provides = trim(provides, provideCount);
            provideWiths = trim(provideWiths, provideCount);
        }
    }
    
    public void accept(final ClassVisitor cv) {
        ModuleVisitor mv = cv.visitModule();
        if (mv == null) {
            return;
        }
        if (requires != null) {
            for(int i = 0; i < requires.length; i++) {
                mv.visitRequire(requires[i], requireAccess[i]);
            }
        }
        if (exports != null) {
            for(int i = 0; i < exports.length; i++) {
                mv.visitExport(exports[i], exportTos[i]);
            }
        }
        if (uses != null) {
            for(String use: uses) {
                mv.visitUse(use);
            }
        }
        if (provides != null) {
            for(int i = 0; i < provides.length; i++) {
                mv.visitExport(provides[i], provideWiths[i]);
            }
        }
    }
}
