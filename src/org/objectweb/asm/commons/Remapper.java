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

package org.objectweb.asm.commons;

import java.util.Collections;
import java.util.Map;

import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

/**
 * A class responsible for remapping types and names.
 * 
 * @author Eugene Kuleshov
 */
public class Remapper {

    private final Map mapping;

    public Remapper(Map mapping) {
        this.mapping = mapping;
    }

    public Remapper(String oldName, String newName) {
        this.mapping = Collections.singletonMap(oldName, newName);
    }

    private Type mapType(Type t) {
        switch (t.getSort()) {
            case Type.ARRAY:
                String s = mapDesc(t.getElementType().getDescriptor());
                for (int i = 0; i < t.getDimensions(); ++i) {
                    s = "[" + s;
                }
                return Type.getType(s);
            case Type.OBJECT:
                s = get(t.getInternalName());
                if(s != null) {
                    return Type.getObjectType(s);
                }
                break;
        }
        return t;
    }

    public String mapType(String type) {
        String newType = get(type);
        return newType == null ? type : newType;
    }

    public String[] mapTypes(String[] types) {
        String[] newTypes = null;
        boolean needMapping = false;
        for (int i = 0; i < types.length; i++) {
            String type = types[i];
            String newType = get(type);
            if (newType != null && newTypes == null) {
                newTypes = new String[types.length];
                if (i > 0) {
                    System.arraycopy(types, 0, newTypes, 0, i);
                }
                needMapping = true;
            }
            if (needMapping) {
                newTypes[i] = newType == null 
                    ? type 
                    : newType;
            }
        }
        return needMapping 
           ? newTypes 
           : types;
    }

    public String mapDesc(String desc) {
        Type t = Type.getType(desc);
        switch (t.getSort()) {
            case Type.OBJECT:
                String newType = get(t.getInternalName());
                if (newType != null) {
                    return "L" + newType + ";";
                }
                break;
            case Type.ARRAY:
                String s = mapDesc(t.getElementType().getDescriptor());
                for (int i = 0; i < t.getDimensions(); ++i) {
                    s = "[" + s;
                }
                return s;
        }
        return desc;
    }

    public String mapMethodDesc(String desc) {
        if("()V".equals(desc)) {
            return desc;
        }
        
        Type[] args = Type.getArgumentTypes(desc);
        String s = "(";
        for (int i = 0; i < args.length; i++) {
            s += mapDesc(args[i].getDescriptor());
        }
        Type returnType = Type.getReturnType(desc);
        if(returnType == Type.VOID_TYPE) {
            return s + ")V";
        }
        return s + ")" + mapDesc(returnType.getDescriptor());
    }

    public Object mapValue(Object value) {
        return value instanceof Type ? mapType((Type) value) : value;
    }

    /**
     * 
     * @param signature
     * @param typeSignature true if signature is a FieldTypeSignature, such as
     *        the signature parameter of the ClassVisitor.visitField or
     *        MethodVisitor.visitLocalVariable methods
     * @return
     */
    public String mapSignature(String signature, boolean typeSignature) {
        if (signature == null) {
            return null;
        }
        SignatureReader r = new SignatureReader(signature);
        SignatureWriter w = new SignatureWriter();
        RemappingSignatureAdapter a = createRemappingSignatureAdapter(w);
        if (typeSignature) {
            r.acceptType(a);
        } else {
            r.accept(a);
        }
        return w.toString();
    }

    public String mapMethodName(String owner, String name, String desc) {
        String s = get(owner + "." + name + desc);
        return s == null ? name : s;
    }

    public String mapFieldName(String owner, String name, String desc) {
        String s = get(owner + "." + name);
        return s == null ? name : s;
    }
    
    protected RemappingSignatureAdapter createRemappingSignatureAdapter(
        SignatureVisitor v)
    {
        return new RemappingSignatureAdapter(v, this);
    }
    
    protected String get(String key) {
        return (String) mapping.get(key);
    }

}
