/***
 * ASM: a very small and fast Java bytecode manipulation framework
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
/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.bco.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.CodeVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.PrintCodeVisitor;
import org.objectweb.asm.util.TraceCodeVisitor;

/**
 * A {@link PrintCodeVisitor PrintCodeVisitor}that prints a disassembled view of the code
 * it visits. This class is written by Eric Bruneton as
 * org.objectweb.asm.util.TraceCodeVisitor and rewritten by Andrei Loskutov to meet some
 * special requirements.
 * @author Eric Bruneton - initial version by ASM
 * @author Andrei
 */
public class CommentedTraceCodeVisitor extends PrintCodeVisitor {

    private static final String PADDING_START = "    "; //$NON-NLS-1$

//    private static final char[] PRIMITIVE_TYPES = {'B', 'C', 'D', 'F', 'I',
//        'J', 'S', 'V', 'Z'};

    private static final char TYPE_REFERENCE = 'L';

    private static final char TYPE_ARRAY = '[';

    protected MethodNode method;
    /**
     * keys are Labels, and values are LineNumberNode instances
     */
    protected HashMap labelsToLineNodes;

    protected Label currentLabel;

    protected List labels;

    protected boolean isRawMode;

    /**
     * The {@link CodeVisitor CodeVisitor}to which this visitor delegates calls. May be
     * <tt>null</tt>.
     */

    protected final CodeVisitor cv;

    /**
     * The label names. This map associate String values to Label keys.
     */
    protected final HashMap labelsNames;
    protected boolean duplicatedLocals;

    /**
     * Constructs a new {@link TraceCodeVisitor TraceCodeVisitor}object.
     * @param cv the code visitor to which this adapter must delegate calls. May be
     * <tt>null</tt>.
     * @param method corresponding method node
     */
    public CommentedTraceCodeVisitor(CodeVisitor cv, MethodNode method) {
        this.cv = cv;
        this.method = method;
        labelsNames = new HashMap();
        labelsToLineNodes = new HashMap(method.lineNumbers.size());
        labels = new ArrayList();
        for (int i = 0; i < method.lineNumbers.size(); i++) {
            LineNumberNode lnn = (LineNumberNode) method.lineNumbers.get(i);
            labelsToLineNodes.put(lnn.start, lnn);
        }
        List variables = new ArrayList();
        for (int i = 0; i < method.localVariables.size(); i++) {
            LocalVariableNode lvn = (LocalVariableNode) method.localVariables.get(i);
            Integer idx = new Integer(lvn.index);
            if(variables.contains(idx)){
                duplicatedLocals = true;
                break;
            }
            variables.add(idx);
        }
        for (int i = 0; i < method.instructions.size(); i++) {
            Object inst = method.instructions.get(i);
            if(inst instanceof Label){
                labels.add(inst);
            }
        }
    }

    private static final int count(String s, char c, int startOffset) {
        int count = 0;
        for (int i = startOffset; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    /**
     *
     * @param line
     * @param c
     * @param startOffset
     * @return index of character block start in given string from given position
     * backward; -1, if given character not found at 'startOffset' position
     */
    private static final int blockIndex(String line, char c, int startOffset){
        int idx = startOffset;
        while (idx >= 0 && line.charAt(idx) == c) {
            idx--;
        }
        if(idx == startOffset){
            return -1;
        }
        return ++idx;
    }

    /**
     * Converts valide bytecode qualified names to simplier version without full
     * qualified, i.e. from Ljava/lang/String;Ljava/lang/Integer; to String,Integer
     * @param classNames
     * @return simplified class name(s) without package information
     */
    private static final String getOnlyClassNames(String classNames) {
        // split I[I[Ljava/lang/Integer;I to I[I[Ljava/lang/Integer and I
        StringTokenizer st = new StringTokenizer(classNames, ";"); //$NON-NLS-1$
        StringBuffer sb = new StringBuffer();
        String token;
        while (st.hasMoreTokens()) {
            token = st.nextToken();

            int startReference = token.indexOf(TYPE_REFERENCE);
            if (startReference < 0) {
                sb.append(getPrimitiveNames(token));
            } else {
                // check for array dimensions for reference type
                // like in: "I[I[[[Ljava/lang/Integer"
                int arrayStartIdx = blockIndex(
                    token, TYPE_ARRAY, startReference - 1);
                int newReferenceIdx = 0;
                if (arrayStartIdx >= 0) {
                    // update reference type ofset for [[ case in: "I[[Ljava/lang/Object"
                    newReferenceIdx = startReference - arrayStartIdx; 
                    startReference = arrayStartIdx;                    
                }
                if (startReference > 0) {
                    // we still have primitives
                    sb.append(getPrimitiveNames(token.substring(
                        0, startReference)));
                    sb.append(',');
                }
                sb.append(getSimplyReferenceTypeName(token
                    .substring(startReference), newReferenceIdx));
            }

            if (st.hasMoreTokens()) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * Converts valide bytecode method signature with full qualified names to simplier
     * version without full qualified, i.e. from (Ljava/lang/String;)Ljava/lang/Integer;
     * to (String):Integer
     * @param signature
     * @return simplified signature without package information
     */
    public static final String getSimplySignature(String signature) {
        StringBuffer sb = new StringBuffer();
        int idx = signature.indexOf('(');
        // all before first "(", inclusive "("
        sb.append(signature.substring(0, idx + 1));

        // all after first "("
        signature = signature.substring(idx + 1);

        int idx2 = signature.indexOf(')');
        if (idx2 > 0) {
            // all before ")", exclusive
            sb.append(getOnlyClassNames(signature.substring(0, idx2)));
        }
        sb.append("): "); //$NON-NLS-1$

        sb.append(getOnlyClassNames(signature.substring(idx2 + 1, signature
            .length())));
        return sb.toString();
    }

    /**
     * Converts valide bytecode qualified name to simplier version without full qualified,
     * i.e. from Ljava/lang/String; to String, J to long and [ to []
     * @param fullName
     * @return simplified class name without package information
     */
    public static final String getSimplyBytecodeName(String fullName) { 
        if(fullName.length() == 0){
            return fullName;
        }
        int startReference = fullName.indexOf(TYPE_REFERENCE);
        if (startReference < 0) {
            return getPrimitiveNames(fullName);
        }
        return getSimplyReferenceTypeName(fullName, startReference);               
    }

    /**
     * 
     * @param fullName like "Ljava/lang/String;" or "java/lang/String" - only for reference types!
     * @param startReference index of "L" character, if any, or 0
     * @return simply name like "String" or "Object"
     */
    public static final String getSimplyReferenceTypeName(String fullName, int startReference) {        
        if(fullName == null){
            return "null";             //$NON-NLS-1$
        }        
        StringBuffer sb = new StringBuffer();
        if(startReference > 0){
            int arrayDim = count(fullName, '[', 0);            
            for (int i = 0; i < arrayDim; i++) {
                sb.append("[]"); //$NON-NLS-1$
            }
        }

        int lastSeparator = fullName.lastIndexOf('/') + 1;
        // for classes from default packages, like [LMyClass
        if(startReference > lastSeparator){
            lastSeparator = startReference + 1;
        }
        if (fullName.endsWith(";")) { //$NON-NLS-1$
            sb.append(fullName.substring(lastSeparator, fullName
                .length() - 1));
        } else {
            sb.append(fullName.substring(lastSeparator, fullName
                .length()));
        }
        return sb.toString();
    }

    /**
     * Converts bytecode primitive names to sourcecode names, i.e. J to long or [ to []
     * I[JJ to int,[]long,long etc
     * @param fullNames can be multiple names <b>without</b> separator between
     * @return translated names, separated througth ','
     */
    private static final String getPrimitiveNames(String fullNames) {
        char[] chars = fullNames.toCharArray();
        // reserve at least place for one name
        StringBuffer sb = new StringBuffer(fullNames.length() * 4);
        for (int i = 0; i < chars.length; i++) {
            switch (chars[i]) {
                case 'B' :
                    sb.append("byte"); //$NON-NLS-1$
                    break;
                case 'C' :
                    sb.append("char"); //$NON-NLS-1$
                    break;
                case 'D' :
                    sb.append("double"); //$NON-NLS-1$
                    break;
                case 'F' :
                    sb.append("float"); //$NON-NLS-1$
                    break;
                case 'I' :
                    sb.append("int"); //$NON-NLS-1$
                    break;
                case 'J' :
                    sb.append("long"); //$NON-NLS-1$
                    break;
                case 'S' :
                    sb.append("short"); //$NON-NLS-1$
                    break;
                case 'V' :
                    sb.append("void"); //$NON-NLS-1$
                    break;
                case 'Z' :
                    sb.append("boolean"); //$NON-NLS-1$
                    break;
                case '[' :
                    sb.append("[]"); //$NON-NLS-1$
                    break;
                default :
                    sb.append(chars[i]);
                    break;
            }
            if (i < chars.length - 1 && chars[i] != TYPE_ARRAY) {
                sb.append(',');
            }
        }
        return sb.toString();
    }

    /**
     * Prints a zero operand instruction.
     * @param opcode
     * @see PrintCodeVisitor
     */
    public void printInsn(final int opcode) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append('\n');

        if (cv != null) {
            cv.visitInsn(opcode);
        }
    }

    /**
     * Prints an instruction with a single int operand.
     *
     * @param opcode the opcode of the instruction to be printed. This opcode is
     *      either BIPUSH, SIPUSH or NEWARRAY.
     * @param operand the operand of the instruction to be printed.
     */
    public void printIntInsn(final int opcode, final int operand) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ').append(
            operand).append('\n');

        if (cv != null) {
            cv.visitIntInsn(opcode, operand);
        }
    }

    /**
     * Prints a local variable instruction. A local variable instruction is an
     * instruction that loads or stores the value of a local variable.
     *
     * @param opcode the opcode of the local variable instruction to be printed.
     *      This opcode is either ILOAD, LLOAD, FLOAD, DLOAD, ALOAD, ISTORE,
     *      LSTORE, FSTORE, DSTORE, ASTORE or RET.
     * @param var the operand of the instruction to be printed. This operand is
     *      the index of a local variable.
     */
    public void printVarInsn(final int opcode, final int var) {
        String localName = getLocalName(var);
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ');
        buf.append(var);
        if (!isRawMode && localName != null) {
            buf.append(": ").append(localName); //$NON-NLS-1$
        }
        buf.append('\n');

        if (cv != null) {
            cv.visitVarInsn(opcode, var);
        }
    }

    /**
     * Prints a type instruction. A type instruction is an instruction that
     * takes a type descriptor as parameter.
     *
     * @param opcode the opcode of the type instruction to be printed. This opcode
     *      is either NEW, ANEWARRAY, CHECKCAST or INSTANCEOF.
     * @param desc the operand of the instruction to be printed. This operand is
     *      must be a fully qualified class name in internal form, or a the type
     *      descriptor of an array type (see {@link org.objectweb.asm.Type Type}).
     */
    public void printTypeInsn(final int opcode, final String desc) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ');
        buf.append(getReferenceName(desc));
        buf.append('\n');

        if (cv != null) {
            cv.visitTypeInsn(opcode, desc);
        }
    }

    private String getReferenceName(String owner) {
        if (isRawMode) {
            return owner;
        }
        return getSimplyReferenceTypeName(owner,0);
    }

    private String getBytecodeDescription(String desc) {
        if (isRawMode) {
            return desc;
        }
        return getSimplyBytecodeName(desc); // getSimplyReferenceTypeName(desc,0);
    }
    
    /**
     * Prints a field instruction. A field instruction is an instruction that
     * loads or stores the value of a field of an object.
     *
     * @param opcode the opcode of the type instruction to be printed. This opcode
     *      is either GETSTATIC, PUTSTATIC, GETFIELD or PUTFIELD.
     * @param owner the internal name of the field's owner class (see {@link
     *      org.objectweb.asm.Type#getInternalName() getInternalName}).
     * @param name the field's name.
     * @param desc the field's descriptor (see {@link org.objectweb.asm.Type
     *      Type}).
     */
    public void printFieldInsn(final int opcode, final String owner,
        final String name, final String desc) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ');
        buf.append(getReferenceName(owner));

        buf.append('.').append(name).append(": "); //$NON-NLS-1$

        buf.append(getBytecodeDescription(desc));
        buf.append('\n');

        if (cv != null) {
            cv.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    /**
     * Prints a method instruction. A method instruction is an instruction that
     * invokes a method.
     *
     * @param opcode the opcode of the type instruction to be printed. This opcode
     *      is either INVOKEVIRTUAL, INVOKESPECIAL, INVOKESTATIC or
     *      INVOKEINTERFACE.
     * @param owner the internal name of the method's owner class (see {@link
     *      org.objectweb.asm.Type#getInternalName() getInternalName}).
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link org.objectweb.asm.Type
     *      Type}).
     */
    public void printMethodInsn(final int opcode, final String owner,
        final String name, final String desc) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ');
        buf.append(getReferenceName(owner));
        buf.append('.').append(name);//.append(' ');

        if (isRawMode) {
            buf.append(desc);
        } else {
            buf.append(getSimplySignature(desc));
        }

        buf.append('\n');

        if (cv != null) {
            cv.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    /**
     * Prints a jump instruction. A jump instruction is an instruction that may
     * jump to another instruction.
     *
     * @param opcode the opcode of the type instruction to be printed. This opcode
     *      is either IFEQ, IFNE, IFLT, IFGE, IFGT, IFLE, IF_ICMPEQ, IF_ICMPNE,
     *      IF_ICMPLT, IF_ICMPGE, IF_ICMPGT, IF_ICMPLE, IF_ACMPEQ, IF_ACMPNE,
     *      GOTO, JSR, IFNULL or IFNONNULL.
     * @param label the operand of the instruction to be printed. This operand is
     *      a label that designates the instruction to which the jump instruction
     *      may jump.
     */
    public void printJumpInsn(final int opcode, final Label label) {
        buf.append(PADDING_START).append(OPCODES[opcode]).append(' ');
        appendLabel(label);
        buf.append('\n');

        if (cv != null) {
            cv.visitJumpInsn(opcode, label);
        }
    }

    /**
     * Prints a label. A label designates the instruction that will be visited
     * just after it.
     *
     * @param label a {@link Label Label} object.
     */
    public void printLabel(final Label label) {
        buf.append("   "); //$NON-NLS-1$
        appendLabel(label);

        currentLabel = label;
        LineNumberNode lnn = (LineNumberNode) labelsToLineNodes.get(label);
        if (lnn != null) {
            buf.append(DecompileResult.SOURCE_LINE_PREFIX);
            buf.append(lnn.line);
            buf.append(DecompileResult.SOURCE_LINE_SUFFIX);
        } else {
            buf.append('\n');
        }

        if (cv != null) {
            cv.visitLabel(label);
        }
    }

    /**
     * Prints a LDC instruction.
     *
     * @param cst the constant to be loaded on the stack. This parameter must be
     *      a non null {@link java.lang.Integer Integer}, a {@link java.lang.Float
     *      Float}, a {@link java.lang.Long Long}, a {@link java.lang.Double
     *      Double} or a {@link String String}.
     */
    public void printLdcInsn(final Object cst) {
        buf.append(PADDING_START).append("LDC "); //$NON-NLS-1$
        if (cst instanceof String) {
            buf.append("\"").append(NodePrinter.escape((String)cst)).append("\""); //$NON-NLS-1$//$NON-NLS-2$
        } else if (cst instanceof Type) {
            buf.append(((Type) cst).getDescriptor() + ".class"); //$NON-NLS-1$
        } else {
            buf.append(cst);
        }
        buf.append('\n');

        if (cv != null) {
            cv.visitLdcInsn(cst);
        }
    }

    /**
     * Prints an IINC instruction.
     *
     * @param var index of the local variable to be incremented.
     * @param increment amount to increment the local variable by.
     */
    public void printIincInsn(final int var, final int increment) {
        buf.append(PADDING_START).append("IINC "); //$NON-NLS-1$
        String localName = getLocalName(var);
        buf.append(var);
        if (localName != null) {
            buf.append(": ").append(localName); //$NON-NLS-1$
        }
        buf.append(' ').append(increment).append('\n');
        if (cv != null) {
            cv.visitIincInsn(var, increment);
        }
    }

    /**
     * Prints a TABLESWITCH instruction.
     *
     * @param min the minimum key value.
     * @param max the maximum key value.
     * @param dflt beginning of the default handler block.
     * @param alabels beginnings of the handler blocks. <tt>labels[i]</tt> is the
     *      beginning of the handler block for the <tt>min + i</tt> key.
     */
    public void printTableSwitchInsn(final int min, final int max,
        final Label dflt, final Label alabels[]) {
        buf.append(PADDING_START).append("TABLESWITCH\n"); //$NON-NLS-1$
        for (int i = 0; i < alabels.length; ++i) {
            buf.append("      ").append(min + i).append(": "); //$NON-NLS-1$//$NON-NLS-2$
            appendLabel(alabels[i]);
            buf.append('\n');
        }
        buf.append("      default: "); //$NON-NLS-1$
        appendLabel(dflt);
        buf.append('\n');

        if (cv != null) {
            cv.visitTableSwitchInsn(min, max, dflt, alabels);
        }
    }

    /**
     * Prints a LOOKUPSWITCH instruction.
     *
     * @param dflt beginning of the default handler block.
     * @param keys the values of the keys.
     * @param alabels beginnings of the handler blocks. <tt>labels[i]</tt> is the
     *      beginning of the handler block for the <tt>keys[i]</tt> key.
     */
    public void printLookupSwitchInsn(final Label dflt, final int keys[],
        final Label alabels[]) {
        buf.append(PADDING_START).append("LOOKUPSWITCH\n"); //$NON-NLS-1$
        for (int i = 0; i < alabels.length; ++i) {
            buf.append("      ").append(keys[i]).append(": "); //$NON-NLS-1$//$NON-NLS-2$
            appendLabel(alabels[i]);
            buf.append('\n');
        }
        buf.append("      default: "); //$NON-NLS-1$
        appendLabel(dflt);
        buf.append('\n');

        if (cv != null) {
            cv.visitLookupSwitchInsn(dflt, keys, alabels);
        }
    }

    /**
     * Prints a MULTIANEWARRAY instruction.
     *
     * @param desc an array type descriptor (see {@link org.objectweb.asm.Type
     *      Type}).
     * @param dims number of dimensions of the array to allocate.
     */
    public void printMultiANewArrayInsn(final String desc, final int dims) {
        buf.append(PADDING_START).append("MULTIANEWARRAY "); //$NON-NLS-1$
        buf.append(getBytecodeDescription(desc));
        buf.append(' ').append(dims).append('\n');

        if (cv != null) {
            cv.visitMultiANewArrayInsn(desc, dims);
        }
    }

    /**
     * Prints a try catch block.
     *
     * @param start beginning of the exception handler's scope (inclusive).
     * @param end end of the exception handler's scope (exclusive).
     * @param handler beginning of the exception handler's code.
     * @param type internal name of the type of exceptions handled by the handler,
     *      or <tt>null</tt> to catch any exceptions (for "finally" blocks).
     */
    public void printTryCatchBlock(final Label start, final Label end,
        final Label handler, final String type) {
        if (isRawMode) {
            buf.append(PADDING_START).append("TRYCATCHBLOCK "); //$NON-NLS-1$
            appendLabel(start);
            buf.append(' ');
            appendLabel(end);
            buf.append(' ');
            appendLabel(handler);
            buf.append(' ').append(type).append('\n');
        }

        if (cv != null) {
            cv.visitTryCatchBlock(start, end, handler, type);
        }
    }

    /**
     * Prints the maximum stack size and the maximum number of local variables of
     * the method.
     *
     * @param maxStack maximum stack size of the method.
     * @param maxLocals maximum number of local variables for the method.
     */
    public void printMaxs(final int maxStack, final int maxLocals) {
        if (isRawMode) {
            buf.append(PADDING_START).append("MAXSTACK = ").append(maxStack); //$NON-NLS-1$
            buf.append('\n').append(PADDING_START);
            buf.append("MAXLOCALS = ").append(maxLocals).append('\n'); //$NON-NLS-1$
        }

        if (cv != null) {
            cv.visitMaxs(maxStack, maxLocals);
        }
    }

    /**
     * Prints a local variable declaration.
     *
     * @param name the name of a local variable.
     * @param desc the type descriptor of this local variable.
     * @param start the first instruction corresponding to the scope of this
     *      local variable (inclusive).
     * @param end the last instruction corresponding to the scope of this
     *      local variable (exclusive).
     * @param index the local variable's index.
     */
    public void printLocalVariable(final String name, final String desc,
        final Label start, final Label end, final int index) {
        if (isRawMode) {
            buf.append(PADDING_START)
                .append("LOCALVARIABLE ").append(name).append(' '); //$NON-NLS-1$

            buf.append(getBytecodeDescription(desc));

            buf.append(' ');
            appendLabel(start);
            buf.append(' ');
            appendLabel(end);
            buf.append(' ').append(index).append('\n');
        }

        if (cv != null) {
            cv.visitLocalVariable(name, desc, start, end, index);
        }
    }

    /**
     * Prints a line number declaration.
     *
     * @param line a line number. This number refers to the source file
     *      from which the class was compiled.
     * @param start the first instruction corresponding to this line number.
     */
    public void printLineNumber(final int line, final Label start) {
        if (isRawMode) {
            buf.append(PADDING_START)
                .append("LINENUMBER ").append(line).append(' '); //$NON-NLS-1$
            appendLabel(start);
            buf.append('\n');
        }

        if (cv != null) {
            cv.visitLineNumber(line, start);
        }
    }

    /**
     * Prints a non standard code attribute.
     * @param attr a non standard code attribute.
     */
    public void printAttribute(final Attribute attr) {
        buf.append(PADDING_START).append("CODE ATTRIBUTE "); //$NON-NLS-1$
        buf.append(attr.type).append(" : ") //$NON-NLS-1$
            .append(attr.toString()).append('\n');

        if (cv != null) {
            cv.visitAttribute(attr);
        }
    }

    /**
     * Appends the name of the given label to {@link #buf buf}. Creates a new label name
     * if the given label does not yet have one.
     * @param l a label.
     */
    private void appendLabel(final Label l) {
        String name = (String) labelsNames.get(l);
        if (name == null) {
            name = "L" + labelsNames.size(); //$NON-NLS-1$
            labelsNames.put(l, name);
        }
        buf.append(name);
    }

    private String getLocalName(int varIdx) {
        for (int i = 0; i < method.localVariables.size(); i++) {
            LocalVariableNode lvn = (LocalVariableNode) method.localVariables
                .get(i);
            if (varIdx != lvn.index) {
                continue;
            } else if(!duplicatedLocals){
                return lvn.name;
            }
            // here we assume, that labels are sorted ascending in their number order
            int firstOcc = labels.indexOf(lvn.start);
            int lastOcc = labels.indexOf(lvn.end);
            if(firstOcc < 0 && lastOcc < 0){
                continue;
            }
            boolean openEnd = lastOcc == -1 || firstOcc == -1;

            // int result = labelComparator.compare(lvn.start, lvn.end);
            // sometimes the first/last labels are in wrong order - asm bug or feature?
            // seems to be asm bug? last idx is smaller as first idx but
            // always 1 lesser as real  first occurence

            int min = firstOcc;
            int max = lastOcc;
            if(openEnd && firstOcc < 0){
                min = lastOcc;
            }
            if(openEnd && lastOcc < 0){
                max = firstOcc;
            }
            if(min > 0){
                min--;
            }

            int currentIdx = labels.indexOf(currentLabel);
            if (min > currentIdx) {
                // this variable will come later, we check another one
                continue;
            }

            if (openEnd || max > currentIdx) {
                // either end of variable scope is not reached now, or is greater
                return lvn.name;
            }
        }
        return null;
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