/*****************************************************************************************
 * ASM: a very small and fast Java bytecode manipulation framework Copyright (c)
 * 2000,2002,2003 INRIA, France Telecom All rights reserved. Redistribution and use in
 * source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain the above
 * copyright notice, this list of conditions and the following disclaimer. 2.
 * Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials
 * provided with the distribution. 3. Neither the name of the copyright holders nor the
 * names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission. THIS SOFTWARE IS PROVIDED BY THE
 * COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/
package de.loskutov.bco.asm;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Constants;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TreeClassAdapter;
import org.objectweb.asm.util.PrintClassVisitor;

/**
 * A {@link PrintClassVisitor PrintClassVisitor}that prints a disassembled view of the
 * classes it visits. This class is written by Eric Bruneton as
 * org.objectweb.asm.util.TraceClassVisitor and rewritten by Andrei Loskutov to meet some
 * special requirements.
 * @author Eric Bruneton - initial version by ASM
 * @author Andrei
 */
public class CommentedTraceClassVisitor extends PrintClassVisitor {

    private List codeVisitors;
    private TreeClassAdapter treeClassAdapter;
    private boolean isRawMode;
    /**
     * The {@link ClassVisitor ClassVisitor}to which this visitor delegates calls. May be
     * <tt>null</tt>.
     */
    protected ClassVisitor cv;

    /**
     * @param cv
     * @param pw
     */
    public CommentedTraceClassVisitor(ClassVisitor cv, PrintWriter pw) {
        super(pw);
        this.cv = cv;
        this.codeVisitors = new ArrayList();
    }

    /**
     * @param access
     * @param name
     * @param desc
     * @param exceptions
     * @param attrs
     * @return TODO update this to new ASM version
     */
    public CodeVisitor visitMethod(int access, String name, String desc,
        String[] exceptions, Attribute attrs) {
        if (!codeVisitors.isEmpty()) {
            CommentedTraceCodeVisitor last = (CommentedTraceCodeVisitor) codeVisitors
                .get(codeVisitors.size() - 1);
            text.addAll(last.getText());
        }

        /**
         * start copy from super class
         */
        buf.setLength(0);
        buf.append('\n');
        if ((access & Constants.ACC_DEPRECATED) != 0) {
            buf.append("  // DEPRECATED\n"); //$NON-NLS-1$
        }
        buf.append("  // access flags ").append(access).append('\n'); //$NON-NLS-1$ //$NON-NLS-2$
        buf.append("  "); //$NON-NLS-1$
        NodePrinter.appendAccess(access, buf);
        if ((access & Constants.ACC_NATIVE) != 0) {
            buf.append("native "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_VARARGS) != 0) {
            buf.append("varargs "); //$NON-NLS-1$
        }
        if ((access & Constants.ACC_BRIDGE) != 0) {
            buf.append("bridge "); //$NON-NLS-1$
        }
        buf.append(name).append(' ');
        if (isRawMode()) {
            buf.append(desc);
        } else {
            buf.append(CommentedTraceCodeVisitor.getSimplySignature(desc));
        }

        if (exceptions != null && exceptions.length > 0) {
            buf.append(" throws "); //$NON-NLS-1$
            for (int i = 0; i < exceptions.length; ++i) {
                buf.append(exceptions[i]).append(' ');
            }
        }
        buf.append('\n'); //$NON-NLS-1$
        text.add(buf.toString());
        Attribute attr = attrs;
        if(isRawMode() || (attr != null && !attr.isUnknown())){
            while (attr != null) {
                buf.setLength(0);
                buf
                    .append("    METHOD ATTRIBUTE ").append(attr.type).append(" : ") //$NON-NLS-1$ //$NON-NLS-2$
                    .append(attr.toString()).append('\n'); //$NON-NLS-1$
                text.add(buf.toString());
                attr = attr.next;
            }
        }
        CodeVisitor cdv;
        if (this.cv != null) {
            cdv = this.cv.visitMethod(access, name, desc, exceptions, attrs);
        } else {
            cdv = null;
        }
        /**
         * end copy from super class
         */

        /**
         * replaced from TraceCodeVisitor to CommentedTraceCodeVisitor
         */
        MethodNode method = findMethod(
            getTreeClassAdapter().classNode.methods, name, desc);
        CommentedTraceCodeVisitor pcv = new CommentedTraceCodeVisitor(
            cdv, method);
        pcv.setRawMode(isRawMode());
        codeVisitors.add(pcv);
        return pcv;
    }

    /**
     * @param version
     * @param access
     * @param name
     * @param superName
     * @param interfaces
     * @param sourceFile TODO update to new ASM
     */
    public void visit(final int version, final int access, final String name,
        final String superName, final String[] interfaces,
        final String sourceFile) {
        int major = version & 0xFFFF;
        int minor = version >>> 16;
        buf.setLength(0);
        buf.append("// class version ").append(major).append('.').append(minor); //$NON-NLS-1$
        buf.append(" [").append(version).append(']'); //$NON-NLS-1$

        // 1.1 is 45, 1.2 is 46 etc.
        int javaVersion = major % 44;
        if (javaVersion > 0 && javaVersion < 10) {
            buf.append(" (Java 1.").append(javaVersion); //$NON-NLS-1$
        }
        buf.append(")\n"); //$NON-NLS-1$

        if ((access & Constants.ACC_DEPRECATED) != 0) {
            buf.append("// DEPRECATED\n"); //$NON-NLS-1$
        }
        if (sourceFile != null) {
            buf.append("// compiled from ").append(sourceFile).append('\n'); //$NON-NLS-1$
        }
        buf.append("// access flags ").append(access).append('\n'); //$NON-NLS-1$
        NodePrinter.appendAccess(access & ~Constants.ACC_SUPER, buf);
        if ((access & Constants.ACC_INTERFACE) != 0) {
            buf.append("interface "); //$NON-NLS-1$
        } else if ((access & Constants.ACC_ENUM) != 0) {
            buf.append("enum "); //$NON-NLS-1$
        } else {
            buf.append("class "); //$NON-NLS-1$
        }
        buf.append(name).append(' ');
        if (superName != null && !superName.equals("java/lang/Object")) { //$NON-NLS-1$
            buf.append("extends ").append(superName).append(' '); //$NON-NLS-1$
        }
        if (interfaces != null && interfaces.length > 0) {
            buf.append("implements "); //$NON-NLS-1$
            for (int i = 0; i < interfaces.length; ++i) {
                buf.append(interfaces[i]).append(' ');
            }
        }
        buf.append("{\n\n"); //$NON-NLS-1$
        text.add(buf.toString());

        if (cv != null) {
            cv.visit(version, access, name, superName, interfaces, sourceFile);
        }
    }

    private String getReferenceName(String owner) {
        if (isRawMode) {
            return owner;
        }
        return CommentedTraceCodeVisitor.getSimplyReferenceTypeName(owner,0);
    }    
    
    /**
     * @param name
     * @param outerName
     * @param innerName
     * @param access TODO update to new ASM
     */
    public void visitInnerClass(final String name, final String outerName,
        final String innerName, final int access) {
        buf.setLength(0);
        buf.append("  INNERCLASS "); //$NON-NLS-1$
        if(isRawMode()){
            buf.append(name);
        } else {
            buf.append(getReferenceName(name));            
        }
        buf.append(' ');
        if(isRawMode()){
            buf.append(outerName);
        } else {
            buf.append(getReferenceName(outerName));            
        }

        
        buf.append(' ').append(innerName).append(' ');
        NodePrinter.appendAccess(access & ~Constants.ACC_SUPER, buf);
        if ((access & Constants.ACC_ENUM) != 0) {
            buf.append("enum "); //$NON-NLS-1$
        }
        buf.append('\n');
        text.add(buf.toString());
        if (cv != null) {
            cv.visitInnerClass(name, outerName, innerName, access);
        }
    }

    /**
     * @param access
     * @param name
     * @param desc
     * @param value
     * @param attrs TODO update to new ASM
     */
    public void visitField(final int access, final String name,
        final String desc, final Object value, final Attribute attrs) {
        buf.setLength(0);
        if ((access & Constants.ACC_DEPRECATED) != 0) {
            buf.append("  // DEPRECATED\n"); //$NON-NLS-1$
        }
        buf.append("  // access flags ").append(access).append('\n'); //$NON-NLS-1$
        buf.append("  "); //$NON-NLS-1$
        NodePrinter.appendAccess(access, buf);
        if ((access & Constants.ACC_ENUM) != 0) {
            buf.append("enum "); //$NON-NLS-1$
        }
        if (isRawMode()) {
            buf.append(desc);
        } else {
            buf.append(CommentedTraceCodeVisitor.getSimplyBytecodeName(desc));
        }
        buf.append(' ').append(name);
        if (value != null) {
            buf.append(" = "); //$NON-NLS-1$
            if (value instanceof String) {
                buf
                    .append("\"").append(NodePrinter.escape((String) value)).append("\""); //$NON-NLS-1$ //$NON-NLS-2$
            } else {
                buf.append(value);
            }
        }
        Attribute attr = attrs;
        if(isRawMode() || (attr != null && !attr.isUnknown())){
            while (attr != null) {
                buf.append("  FIELD ATTRIBUTE ").append(attr.type).append(" : ") //$NON-NLS-1$ //$NON-NLS-2$
                    .append(attr.toString()).append('\n');
                attr = attr.next;
            }
        }
        buf.append('\n');
        text.add(buf.toString());

        if (cv != null) {
            cv.visitField(access, name, desc, value, attrs);
        }
    }

    /**
     * @param attr TODO update to new ASM
     */
    public void visitAttribute(final Attribute attr) {
        buf.setLength(0);
        if(isRawMode() || !attr.isUnknown()){
            buf.append("  CLASS ATTRIBUTE ").append(attr.type).append(" : ") //$NON-NLS-1$ //$NON-NLS-2$
                .append(attr.toString()).append('\n');
            text.add(buf.toString());
        }
        if (cv != null) {
            cv.visitAttribute(attr);
        }
    }

    /**
     *
     */
    public void visitEnd() {
        if (!codeVisitors.isEmpty()) {
            CommentedTraceCodeVisitor last = (CommentedTraceCodeVisitor) codeVisitors
                .get(codeVisitors.size() - 1);
            text.addAll(last.getText());
        }
        text.add("}\n"); //$NON-NLS-1$

        if (cv != null) {
            cv.visitEnd();
        }
        super.visitEnd();
    }

    private static MethodNode findMethod(List methods, String name, String desc) {
        for (int i = 0; i < methods.size(); i++) {
            MethodNode node = (MethodNode) methods.get(i);
            if (node.name.equals(name) && node.desc.equals(desc)) {
                return node;
            }
        }
        return null;
    }

    /**
     * @param treeClassAdapter The treeClassAdapter to set.
     */
    public void setTreeClassAdapter(TreeClassAdapter treeClassAdapter) {
        this.treeClassAdapter = treeClassAdapter;
    }

    /**
     * @return Returns the treeClassAdapter.
     */
    public TreeClassAdapter getTreeClassAdapter() {
        return treeClassAdapter;
    }

    /**
     * @return Returns the isRawMode.
     */
    public boolean isRawMode() {
        return isRawMode;
    }
    /**
     * @param isRawMode The isRawMode to set.
     */
    public void setRawMode(boolean isRawMode) {
        this.isRawMode = isRawMode;
    }

}