/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/
package de.loskutov.bco.asm;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IInitializer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.core.BinaryMember;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TreeClassAdapter;
import org.objectweb.asm.util.ASMifierClassVisitor;
import org.objectweb.asm.util.ASMifierCodeVisitor;
import org.objectweb.asm.util.PrintClassVisitor;
import org.objectweb.asm.util.PrintCodeVisitor;

import de.loskutov.bco.BytecodeOutlinePlugin;
import de.loskutov.bco.ui.JdtUtils;

/**
 * @author Andrei
 */
public class AsmUtils {

    /**
     * Decompiles entire given input stream (with class file) to java bytecode
     * @param is java .class file
     * @param isRawMode true for "not beautified" output, false for better
     * readable output without package information but with resolved local
     * variables
     * @param isASMifierMode true if we should return not bytecode instructions,
     * but ASM instructions that generate bytecode
     * @return decompiled bytecode as text or ASMfier java code 
     * @throws IOException
     */
    public static DecompileResult getFullBytecode(InputStream is,
        boolean isRawMode, boolean isASMifierMode) throws IOException {
        ClassReader cr = new ClassReader(is);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        
        if(isASMifierMode){
            ASMifierClassVisitor acv = new ASMifierClassVisitor(pw); 
            cr.accept(acv, PrintClassVisitor.getDefaultAttributes(), false);
        } else {
            CommentedTraceClassVisitor cv = new CommentedTraceClassVisitor(null, pw);            
            cv.setRawMode(isRawMode);
            TreeClassAdapter treeClassAdapter = new TreeClassAdapter(cv);
            cv.setTreeClassAdapter(treeClassAdapter);
            cr.accept(treeClassAdapter, PrintClassVisitor.getDefaultAttributes(), false);
        }
        pw.flush();
        return new DecompileResult(sw.toString());
    }
    
    /**
     * Decompiles a part of given input stream (with class file) to java bytecode
     * @param is
     * @param javaElement java element to decompile
     * @param isRawMode true for "not beautified" output, false for better
     * readable output without package information but with resolved local
     * variables
     * @param isASMifierMode true if we should return not bytecode instructions,
     * but ASM instructions that generate bytecode
     * @return decompiled bytecode as text or ASMfier java code 
     * @throws IOException
     */
    public static DecompileResult getBytecode(InputStream is,
        IJavaElement javaElement,
        boolean isRawMode, boolean isASMifierMode) throws IOException {

        ClassNode cnode = createClassNode(is);
        MethodNode methodToVisit = null;

        // some fields doesn't included in <cinit>/<init> blocks...
        if (javaElement.getElementType() == IJavaElement.FIELD) {
            // try to get single field
            List fields = cnode.fields;
            for (int i = 0; i < fields.size(); i++) {
                FieldNode fn = (FieldNode) fields.get(i);
                if (javaElement.getElementName().equals(fn.name)) {
                    if (fn.value != null) {
                        if(!isASMifierMode) {
                            return new DecompileResult(NodePrinter.print(
                                fn, isRawMode));
                        }
                        return new DecompileResult(NodePrinter
                            .printASMifier(fn));
                    }
                    methodToVisit = findInitializerNode(
                        cnode.methods, (IField) javaElement);
                    // no initializer - print empty field
                    if (methodToVisit == null) {
                        if(!isASMifierMode) {
                            return new DecompileResult(NodePrinter.print(
                                fn, isRawMode));
                        }
                        return new DecompileResult(NodePrinter
                            .printASMifier(fn));                        
                    }
                    break;
                }
            }
        }

        if (methodToVisit == null) {
            methodToVisit = getMethodNode(javaElement, cnode.methods);
            if (methodToVisit == null) {
                return null;
            }
        }
        StringBuffer sb;
        String elementName = createBytecodeSignature(methodToVisit, isRawMode);
        PrintCodeVisitor cv;
        if(!isASMifierMode){
            cv = new CommentedTraceCodeVisitor(
                null, methodToVisit);
            ((CommentedTraceCodeVisitor)cv).setRawMode(isRawMode);
        } else {
            cv = new ASMifierCodeVisitor();
        }
        NodePrinter.visitMethod(cv, methodToVisit, isRawMode, isASMifierMode);
        sb = NodePrinter.createTextFromVisitor(elementName, cv);
        return new DecompileResult(sb.toString());
    }

    /**
     * @param javaElement
     * @param methods
     * @return enclosing bytecode method node for given element from given list
     */
    private static MethodNode getMethodNode(IJavaElement javaElement,
        List methods) {
        MethodNode methodToVisit = null;
        switch (javaElement.getElementType()) {
            case IJavaElement.FIELD :
                IField iField = (IField) javaElement;
                methodToVisit = findInitializerNode(methods, iField);
                break;
            case IJavaElement.INITIALIZER :
                IInitializer ini = (IInitializer) javaElement;
                methodToVisit = findInitializerNode(methods, ini);
                break;
            case IJavaElement.METHOD :
                IMethod iMethod = (IMethod) javaElement;
                methodToVisit = findMethodNode(methods, iMethod);
                break;
            default :
                break;
        }
        return methodToVisit;
    }

    /**
     * @param is
     * @return
     * @throws IOException
     */
    private static ClassNode createClassNode(InputStream is) throws IOException {
        ClassReader cr = new ClassReader(is);
        TreeClassAdapter treeClassAdapter = new TreeClassAdapter(null);
        cr.accept(treeClassAdapter, PrintClassVisitor.getDefaultAttributes(), false);
        return treeClassAdapter.classNode;
    }

    /**
     * @param methods
     * @param iMethod
     * @return enclosing bytecode method node for given element from given list
     */
    private static MethodNode findMethodNode(List methods, IMethod iMethod) {
        List similarMethods = null;
        try {
            similarMethods = findSimilarBytecodeMethods(iMethod, methods);
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.error(null, e);
        }
        if (similarMethods == null || similarMethods.isEmpty()) {
            return null;
        }
        MethodNode methodToVisit = null;
        if (similarMethods.size() == 1) {
            methodToVisit = (MethodNode) similarMethods.get(0);
        } else {
            String resolvedSignature = null;
            try {
                resolvedSignature = createMethodSignature(iMethod);
            } catch (JavaModelException e) {
                BytecodeOutlinePlugin.error(null, e);
            }
            for (int i = 0; i < similarMethods.size(); i++) {
                MethodNode method = (MethodNode) similarMethods.get(i);
                String bytecodeSign = createBytecodeSignature(method, true);
                if (bytecodeSign.equals(resolvedSignature)) {
                    methodToVisit = method;
                    break;
                }
            }
        }
        return methodToVisit;
    }

    /**
     * @param methods
     * @param ini
     * @return enclosing bytecode method node for given element from given list
     */
    private static MethodNode findInitializerNode(List methods, IInitializer ini) {
        MethodNode methodToVisit = null;
        try {
            if (Flags.isStatic(ini.getFlags())) {
                methodToVisit = getStaticInit(methods);
            } else {
                methodToVisit = getDefaultInit(methods);
            }
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.error(null, e);
        }
        return methodToVisit;
    }

    /**
     * @param methods
     * @param iField
     * @return enclosing bytecode method node for given element from given list
     */
    private static MethodNode findInitializerNode(List methods, IField iField) {
        MethodNode methodToVisit = null;
        try {
            IType enclosingType = JdtUtils.getEnclosingType(iField);
            if (Flags.isStatic(iField.getFlags())
                || enclosingType.isInterface()) {
                methodToVisit = getStaticInit(methods);
            } else {
                methodToVisit = getDefaultInit(methods);
            }
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.error(null, e);
        }
        return methodToVisit;
    }



    /**
     * @throws JavaModelException
     */
    private static List findSimilarBytecodeMethods(IMethod iMethod, List methods)
        throws JavaModelException {
        List result = new ArrayList();
        String name = iMethod.getElementName();
        // Eclipse put class name as constructor name - we change it!
        if (iMethod.isConstructor()) {
            name = "<init>"; //$NON-NLS-1$
        }

        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if (name.equals(method.name)) {
                result.add(method);
            }
        }
        return result;
    }

    private static MethodNode getDefaultInit(List methods) {
        MethodNode classInit = null;
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if ("<init>".equals(method.name) && "()V".equals(method.desc)) { //$NON-NLS-1$//$NON-NLS-2$
                classInit = method;
                break;
            }
        }
        return classInit;
    }

    private static MethodNode getStaticInit(List methods) {
        MethodNode classInit = null;
        for (Iterator iter = methods.iterator(); iter.hasNext();) {
            MethodNode method = (MethodNode) iter.next();
            if ("<clinit>".equals(method.name)) { //$NON-NLS-1$
                classInit = method;
                break;
            }
        }
        return classInit;
    }

    /**
     * @param method
     * @param useFullQualifiedNames
     * @return bytecode signature for given method
     */
    private static String createBytecodeSignature(MethodNode method,
        boolean useFullQualifiedNames) {
        String bytecodeSign = method.name + method.desc;
        if (!useFullQualifiedNames) {
            return CommentedTraceCodeVisitor.getSimplySignature(bytecodeSign);
        }
        return bytecodeSign;
    }

    private static String createMethodSignature(IMethod iMethod)
        throws JavaModelException {
        StringBuffer sb = new StringBuffer();

        // Eclipse put class name as constructor name - we change it!
        if (iMethod.isConstructor()) {
            sb.append("<init>"); //$NON-NLS-1$
        } else {
            sb.append(iMethod.getElementName());
        }
        
        if (iMethod instanceof BinaryMember) {
            // binary info should be full qualified
            return sb.append(iMethod.getSignature()).toString();
        }        
        
        sb.append('(');

        // XXX add here 1.5 generic code - iMethod.getTypeParameterSignatures();

        IType declaringType = iMethod.getDeclaringType();
        String[] parameterTypes = iMethod.getParameterTypes();
        for (int i = 0; i < parameterTypes.length; i++) {
            sb.append(getResolvedType(parameterTypes[i], declaringType));
        }
        sb.append(')');
        // continue here with adding resolved return type
        String returnType = iMethod.getReturnType();
        sb.append(getResolvedType(returnType, declaringType));

        return sb.toString();
    }

    /**
     * @param typeToResolve
     * @param declaringType
     * @return full qualified "bytecode formatted" type
     * @throws JavaModelException
     */
    private static String getResolvedType(String typeToResolve,
        IType declaringType) throws JavaModelException {
        StringBuffer sb = new StringBuffer();
        int arrayCount = Signature.getArrayCount(typeToResolve);
        // test which letter is following - Q or L are for reference types
        boolean isPrimitive = isPrimitiveType(typeToResolve.charAt(arrayCount));
        if (isPrimitive) {
            // simply add whole string (probably with array chars like [[I etc.)
            sb.append(typeToResolve);
        } else {
            // we need resolved types
            String resolved = getResolvedTypeName(typeToResolve, declaringType);
            
            while (arrayCount > 0) {
                sb.append(Signature.C_ARRAY);
                arrayCount--;
            }
            sb.append(Signature.C_RESOLVED);
            sb.append(resolved);
            sb.append(Signature.C_SEMICOLON);
        }
        return sb.toString();
    }
    
    /**
     * Copied and modified from JavaModelUtil. Resolves a type name in the context of the
     * declaring type.
     * @param refTypeSig the type name in signature notation (for example 'QVector') this
     * can also be an array type, but dimensions will be ignored.
     * @param declaringType the context for resolving (type where the reference was made
     * in)
     * @return returns the fully qualified <b>bytecode</b> type name or build-in-type name. if a
     * unresoved type couldn't be resolved null is returned
     */
    private static String getResolvedTypeName(String refTypeSig,
        IType declaringType) throws JavaModelException {
        int arrayCount = Signature.getArrayCount(refTypeSig);
        char type = refTypeSig.charAt(arrayCount);
        if (type == Signature.C_UNRESOLVED) {
            int semi = refTypeSig
                .indexOf(Signature.C_SEMICOLON, arrayCount + 1);
            if (semi == -1) {
                throw new IllegalArgumentException();
            }
            String name = refTypeSig.substring(arrayCount + 1, semi);

            String[][] resolvedNames = declaringType.resolveType(name);
            if (resolvedNames != null && resolvedNames.length > 0) { 
                char innerPrefix = '$';//JdtUtils.getInnerPrefix(declaringType);
                return concatenateName(
                    resolvedNames[0][0], resolvedNames[0][1], innerPrefix);
            }
            return null;
        }
        return Signature.toString(refTypeSig.substring(arrayCount));
    }    
    
    /**
     * Concatenates package and Class name
     * Both strings can be empty or <code>null</code>.
     */
    private static String concatenateName(String packageName, String className,
        char innerPrefix) {
        StringBuffer buf= new StringBuffer();
        if (packageName != null && packageName.length() > 0) {
            packageName = packageName.replace(Signature.C_DOT, '/');  
            buf.append(packageName);
        }
        if (className != null && className.length() > 0) {
            if (buf.length() > 0) {
                buf.append('/');
            }
            className = className.replace(Signature.C_DOT, innerPrefix); 
            buf.append(className);
        }       
        return buf.toString();
    }    

    /**
     * Test which letter is following - Q or L are for reference types
     * @param first
     * @return true, if character is not a simbol for reference types
     */
    private static boolean isPrimitiveType(char first) {
        return (first != Signature.C_RESOLVED && first != Signature.C_UNRESOLVED);
    }
}