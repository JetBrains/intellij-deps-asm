/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/
package de.loskutov.bco.asm;

import java.util.Iterator;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.Constants;
import org.objectweb.asm.Label;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.DummyASMifierClassVisitor;
import org.objectweb.asm.util.PrintCodeVisitor;
import org.objectweb.asm.util.attrs.ASMifiable;

/**
 * @author Andrei
 */
public class NodePrinter {
    /** copied from ASMifierClassVisitor */
    private static final int ACCESS_FIELD = 524288;
    private static DummyASMifierClassVisitor dummyASMifier;
    
    /**
     * Convert special sequences to "readable" Strings, e.g.
     * from "\n" to "\\n" etc. 
     * @param str as is to be "escaped"
     * @return string for print out in a GUI
     */
    public static String escape(String str){
        if(str == null){
            return "null"; //$NON-NLS-1$
        }
        char [] chars = str.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {            
            switch (chars[i]){
                case '\b': 
                    sb.append("\\b"); //$NON-NLS-1$
                    break;
                case '\t': 
                    sb.append("\\t"); //$NON-NLS-1$
                    break;
                case '\n': 
                    sb.append("\\n"); //$NON-NLS-1$
                    break;
                case '\f': 
                    sb.append("\\f"); //$NON-NLS-1$
                    break;
                case '\r': 
                    sb.append("\\r"); //$NON-NLS-1$
                    break;
                case '\"': 
                    sb.append("\\\""); //$NON-NLS-1$
                    break;
                case '\'': 
                    sb.append("\\'"); //$NON-NLS-1$
                    break;                    
                case '\\': 
                    sb.append("\\\\"); //$NON-NLS-1$
                    break;  
                default: 
                    sb.append(chars[i]);
                    break;
            }
        }
        return sb.toString();
    }
    
    /**
     * @param field
     * @param isRawMode
     * @return text representation of given field
     */
    public static String print(FieldNode field, boolean isRawMode) {
        StringBuffer buf = new StringBuffer();
        final int access = field.access;
        final String name = field.name;
        final String desc = field.desc;
        final Object value = field.value;
        final Attribute attrs = field.attrs;

        if ((access & Constants.ACC_DEPRECATED) != 0) {
            buf.append("  // DEPRECATED\n"); //$NON-NLS-1$
        }
        buf.append("  // access flags ").append(access).append('\n'); //$NON-NLS-1$
        buf.append("  "); //$NON-NLS-1$
        appendAccess(access, buf);
        if ((access & Constants.ACC_ENUM) != 0) {
            buf.append("enum "); //$NON-NLS-1$
        }
        if(isRawMode){
            buf.append(desc);
        } else {
            buf.append(CommentedTraceCodeVisitor.getSimplyBytecodeName(desc));
        }
        buf.append(' ').append(name);
        if (value != null) {
            buf.append(" = "); //$NON-NLS-1$
            if (value instanceof String) {
                buf.append("\"").append(value).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                buf.append(value);
            }
        }
        Attribute attr = attrs;
        if(isRawMode || (attr != null && !attr.isUnknown())){
            while (attr != null) {
                buf.append("  FIELD ATTRIBUTE ").append(attr.type).append(" : ") //$NON-NLS-1$ //$NON-NLS-2$
                    .append(attr.toString()).append('\n');
                attr = attr.next;
            }
        }
        buf.append('\n');
        return buf.toString();
    }
    
    /**
     * @param field
     * @return ASMifier java code representation of given field
     */
    public static String printASMifier(FieldNode field) {
        StringBuffer buf = new StringBuffer();
        final int access = field.access;
        final String name = field.name;
        final String desc = field.desc;
        final Object value = field.value;
        final Attribute attrs = field.attrs;

        int n = 1;
        if (attrs != null) {
            buf.append("{\n"); //$NON-NLS-1$
            buf.append("// FIELD ATTRIBUTES\n"); //$NON-NLS-1$
            Attribute a = attrs;
            while (a != null) {
                if (a instanceof ASMifiable) {
                    ((ASMifiable) a).asmify(buf, "fieldAttrs" + n, null); //$NON-NLS-1$
                    if (n > 1) {
                        buf.append("fieldAttrs" + (n - 1) + " = fieldAttrs" + n //$NON-NLS-1$ //$NON-NLS-2$
                            + ";\n"); //$NON-NLS-1$
                    }
                    n++;
                } else {
                    buf
                        .append("// WARNING! skipped non standard field attribute of type "); //$NON-NLS-1$
                    buf.append(a.type).append('\n');
                }
                a = a.next;
            }
        }

        buf.append("cw.visitField("); //$NON-NLS-1$
        appendASMifiedAccess(access | ACCESS_FIELD, buf);
        buf.append(", "); //$NON-NLS-1$
        DummyASMifierClassVisitor.appendConstant(buf, name);
        buf.append(", "); //$NON-NLS-1$
        DummyASMifierClassVisitor.appendConstant(buf, desc);
        buf.append(", "); //$NON-NLS-1$
        DummyASMifierClassVisitor.appendConstant(buf, value);

        if (n == 1) {
            buf.append(", null);\n\n"); //$NON-NLS-1$
        } else {
            buf.append(", fieldAttrs1);\n"); //$NON-NLS-1$
            buf.append("}\n\n"); //$NON-NLS-1$
        }
        
        return buf.toString();
    }    
    
    /**
     * @param cv
     * @param method
     * @param isRawMode
     * @param isASMifierMode
     */
    public static void visitMethod(PrintCodeVisitor cv, MethodNode method,
        boolean isRawMode,  boolean isASMifierMode) {
        Attribute attr = method.attrs;
        List text = cv.getText();
        if (isASMifierMode) {
            StringBuffer buf = new StringBuffer();  
            buf.append("{\n"); //$NON-NLS-1$
            int n = 1;
            while (attr != null) {
                if (attr instanceof ASMifiable) {
                    ((ASMifiable) attr)
                        .asmify(buf, "methodAttrs" + n, null); //$NON-NLS-1$
                    if (n > 1) {
                        buf.append("methodAttrs" + (n - 1) //$NON-NLS-1$
                            + ".next = methodAttrs" + n + ";\n"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    n++;
                } else {
                    buf
                        .append("// WARNING! skipped non standard method attribute of type "); //$NON-NLS-1$
                    buf.append(attr.type).append('\n');
                }
                attr = attr.next;
            }
            buf.append("cv = cw.visitMethod("); //$NON-NLS-1$
            NodePrinter.appendASMifiedAccess(method.access, buf);
            buf.append(", "); //$NON-NLS-1$
            DummyASMifierClassVisitor.appendConstant(buf, method.name);
            buf.append(", "); //$NON-NLS-1$
            DummyASMifierClassVisitor.appendConstant(buf, method.desc);
            buf.append(", "); //$NON-NLS-1$
            if (method.exceptions != null && method.exceptions.size() > 0) {
              buf.append("new String[] {"); //$NON-NLS-1$
              for (int i = 0; i < method.exceptions.size(); ++i) {
                buf.append(i == 0 ? " " : ", "); //$NON-NLS-1$ //$NON-NLS-2$
                DummyASMifierClassVisitor.appendConstant(buf, method.exceptions.get(i));
              }
              buf.append(" }"); //$NON-NLS-1$
            } else {
              buf.append("null"); //$NON-NLS-1$
            }
            if (n==1) {
              buf.append(", null);\n"); //$NON-NLS-1$
            } else {
              buf.append(", methodAttrs1);\n"); //$NON-NLS-1$
            }                
            text.add(buf.toString());
        } else {
            if (isRawMode || (attr != null && !attr.isUnknown())) {
                StringBuffer buf = new StringBuffer();
                while (attr != null) {
                    buf.setLength(0);
                    buf
                        .append("    METHOD ATTRIBUTE ").append(attr.type).append(" : ") //$NON-NLS-1$ //$NON-NLS-2$
                        .append(attr.toString()).append('\n');
                    text.add(buf.toString());
                    attr = attr.next;
                }
            }
        }
                
        for (int j = 0; j < method.instructions.size(); ++j) {
            Object insn = method.instructions.get(j);
            if (insn instanceof AbstractInsnNode) {
                ((AbstractInsnNode) insn).accept(cv);
            } else {
                cv.visitLabel((Label) insn);
            }
        }

        for (int i = 0; i < method.localVariables.size(); i++) {
            LocalVariableNode lvn = (LocalVariableNode) method.localVariables
                .get(i);
            lvn.accept(cv);
        }
        Attribute attrs = method.codeAttrs;
        while (attrs != null) {
          cv.visitAttribute(attrs);
          attrs = attrs.next;
        }        
        cv.visitMaxs(method.maxStack, method.maxLocals);
        
        if(isASMifierMode){
            text.add("}\n"); //$NON-NLS-1$
        }
    }    
    
    /**
     * @param elementName
     * @param cv
     * @return content of given arguments as string
     */
    public static StringBuffer createTextFromVisitor(String elementName,
        PrintCodeVisitor cv) {
        List text = cv.getText();
        StringBuffer sb = new StringBuffer("// " + elementName + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        for (Iterator iter = text.iterator(); iter.hasNext();) {
            String element = (String) iter.next();
            sb.append(element);
        }
        return sb;
    }    
    
    /**
     * @param access
     * @param buf
     */
    public static void appendASMifiedAccess(int access, StringBuffer buf) {
        if(dummyASMifier == null){
            dummyASMifier = new DummyASMifierClassVisitor();    
        }
        buf.append(dummyASMifier.getAccess(access));        
    }    
    
    /**
     * @param access
     * @param buf
     */
    public static void appendAccess(int access, StringBuffer buf) {
        if ((access & Constants.ACC_PUBLIC) != 0) {
            buf.append("public "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_PRIVATE) != 0) {
            buf.append("private "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_PROTECTED) != 0) {
            buf.append("protected "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_FINAL) != 0) {
            buf.append("final "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_STATIC) != 0) {
            buf.append("static "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_SYNCHRONIZED) != 0) {
            buf.append("synchronized "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_VOLATILE) != 0) {
            buf.append("volatile "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_TRANSIENT) != 0) {
            buf.append("transient "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_NATIVE) != 0) {
            buf.append("native "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_ABSTRACT) != 0) {
            buf.append("abstract "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_STRICT) != 0) {
            buf.append("strictfp "); //$NON-NLS-1$
        }
    }      
}