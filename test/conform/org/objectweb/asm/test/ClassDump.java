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

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * A dump of the content of a class file, as a verbose, human "readable" String.
 * As an example, the dump of the HelloWorld class is:
 * 
 * <pre>
 * magic: -889275714
 * minor_version: 0
 * major_version: 49
 * access_flags: 33
 * this_class: ConstantClassInfo HelloWorld
 * super_class: ConstantClassInfo java/lang/Object
 * interfaces_count: 0
 * fields_count: 0
 * methods_count: 2
 * access_flags: 1
 * name_index: <init>
 * descriptor_index: ()V
 * attributes_count: 1
 * attribute_name_index: Code
 * max_stack: 1
 * max_locals: 1
 * 0: 25 0
 * 1: 183 ConstantMethodRefInfo java/lang/Object.<init>()V
 * 2: 177
 * exception_table_length: 0
 * attributes_count: 2
 * attribute_name_index: LineNumberTable
 * line_number_table_length: 1
 * start_pc: <0>
 * line_number: 31
 * attribute_name_index: LocalVariableTable
 * local_variable_table_length: 1
 * start_pc: <0>
 * length: <3>
 * name_index: this
 * descriptor_index: LHelloWorld;
 * index: 0
 * access_flags: 9
 * name_index: main
 * descriptor_index: ([Ljava/lang/String;)V
 * attributes_count: 1
 * attribute_name_index: Code
 * max_stack: 2
 * max_locals: 1
 * 0: 178 ConstantFieldRefInfo java/lang/System.outLjava/io/PrintStream;
 * 1: 18 ConstantStringInfo Hello, world!
 * 2: 182 ConstantMethodRefInfo java/io/PrintStream.println(Ljava/lang/String;)V
 * 3: 177
 * exception_table_length: 0
 * attributes_count: 2
 * attribute_name_index: LineNumberTable
 * line_number_table_length: 2
 * start_pc: <0>
 * line_number: 33
 * start_pc: <3>
 * line_number: 34
 * attribute_name_index: LocalVariableTable
 * local_variable_table_length: 1
 * start_pc: <0>
 * length: <4>
 * name_index: args
 * descriptor_index: [Ljava/lang/String;
 * index: 0
 * attributes_count: 1
 * attribute_name_index: SourceFile
 * sourcefile_index: HelloWorld.java
 * </pre>
 * 
 * This class is used to compare classes in unit tests. Its source code is as
 * close as possible to the Java Virtual Machine specification for ease of
 * reference. The constant pool and bytecode offsets are abstracted away so that
 * two classes which differ only by their constant pool or low level byte code
 * instruction representation (e.g. a ldc vs. a ldc_w) are still considered
 * equal. Likewise, attributes (resp. type annotations) are re-ordered into
 * alphabetical order, so that two classes which differ only via the ordering of
 * their attributes (resp. type annotations) are still considered equal.
 * 
 * @author Eric Bruneton
 */
class ClassDump {

    /** The class to dump, as a data input stream. */
    final DataInput input;

    /**
     * The constant pool of the input class, used to abstract away its internal
     * representation in the output string.
     */
    final ArrayList<CpInfo> constantPool;

    /**
     * An intermediate data structure, used to build the final output string.
     * The final string can't be output fully sequentially, as the input class
     * is parsed, in particular due to the re-ordering of attributes and
     * annotations. Instead, a tree of {@link OutputNode} objects is constructed
     * first, then its nodes are sorted and finally the tree is parsed in Depth
     * First Search order to build the final output string. This stack contains
     * the nodes of the tree along the path from the root to the node which is
     * currently constructed.
     */
    final Stack<OutputNode> pathToCurrentOutputNode;

    /** The final string representation of the input class. */
    final String stringValue;

    /**
     * A map from bytecode offsets to instruction indices for the <i>current</i>
     * method, used to abstract away the low level byte code instruction
     * representation details (e.g. an ldc vs. an ldc_w) in the final output
     * string. A new map is constructed and assigned to this field for each
     * newly encountered method.
     */
    HashMap<Integer, Integer> instructionIndices;

    /**
     * Creates a new ClassDump instance. The input byte array is parsed and
     * converted to a string representation by this constructor. The result can
     * then be obtained with {@link #toString}.
     * 
     * @param bytecode
     *            the content of a class file.
     * @throws IOException
     *             if class can't be parsed.
     */
    ClassDump(byte[] bytecode) throws IOException {
        this.input = new DataInputStream(new ByteArrayInputStream(bytecode));
        this.constantPool = new ArrayList<CpInfo>();
        this.pathToCurrentOutputNode = new Stack<OutputNode>();
        this.pathToCurrentOutputNode.push(new OutputNode());
        this.stringValue = parseClassFile();
    }

    @Override
    public String toString() {
        return stringValue;
    }

    /**
     * Parses the high level structure of the class.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.1
     */
    String parseClassFile() throws IOException {
        parseU4("magic: ");
        parseU2("minor_version: ");
        parseU2("major_version: ");
        int constantPoolCount = parseU2(null);
        constantPool.add(null);
        for (int i = 1; i < constantPoolCount; ++i) {
            CpInfo cpInfo = parseCpInfo();
            constantPool.add(cpInfo);
            if (cpInfo.useTwoEntries()) {
                constantPool.add(null);
                ++i;
            }
        }
        parseU2("access_flags: ");
        parseU2ConstantPoolIndex("this_class: ");
        parseU2ConstantPoolIndex("super_class: ");
        int interfaceCount = parseU2("interfaces_count: ");
        for (int i = 0; i < interfaceCount; ++i) {
            parseU2ConstantPoolIndex("interface: ");
        }
        int fieldCount = parseU2("fields_count: ");
        for (int i = 0; i < fieldCount; ++i) {
            parseFieldInfo();
        }
        int methodCount = parseU2("methods_count: ");
        for (int i = 0; i < methodCount; ++i) {
            parseMethodInfo();
        }
        parseAttributeList();

        StringBuilder stringBuilder = new StringBuilder();
        pathToCurrentOutputNode.peek().toString(stringBuilder);
        return stringBuilder.toString();
    }

    /**
     * Parses a list of attributes.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.1
     */
    private void parseAttributeList() throws IOException {
        int attributeCount = parseU2("attributes_count: ");
        OutputNode attributeListOutputNode = new SortedOutputNode();
        pathToCurrentOutputNode.peek().append(attributeListOutputNode);
        pathToCurrentOutputNode.push(attributeListOutputNode);
        for (int i = 0; i < attributeCount; ++i) {
            parseAttributeInfo();
        }
        pathToCurrentOutputNode.pop();
    }

    /**
     * Parses a constant pool entry.
     * 
     * @return the parsed constant pool entry.
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4
     */
    private CpInfo parseCpInfo() throws IOException {
        int tag = parseU1(null);
        switch (tag) {
        case 7:
            return new ConstantClassInfo();
        case 9:
            return new ConstantFieldRefInfo();
        case 10:
            return new ConstantMethodRefInfo();
        case 11:
            return new ConstantInterfaceMethodRefInfo();
        case 8:
            return new ConstantStringInfo();
        case 3:
            return new ConstantIntegerInfo();
        case 4:
            return new ConstantFloatInfo();
        case 5:
            return new ConstantLongInfo();
        case 6:
            return new ConstantDoubleInfo();
        case 12:
            return new ConstantNameAndTypeInfo();
        case 1:
            return new ConstantUtf8Info();
        case 15:
            return new ConstantMethodHandleInfo();
        case 16:
            return new ConstantMethodTypeInfo();
        case 18:
            return new ConstantInvokeDynamicInfo();
        case 19:
            return new ConstantModuleInfo();
        case 20:
            return new ConstantPackageInfo();
        default:
            throw new IOException("Invalid constant pool entry tag " + tag);
        }
    }

    /** An abstract constant pool entry. */
    static abstract class CpInfo {
        /** The string representation of this constant. */
        protected String value;

        /** Whether this constant uses two entries in the constant pool. */
        protected boolean useTwoEntries() {
            return false;
        }

        /** Computes the string representation of this constant. */
        protected String computeStringValue() {
            return value;
        }

        @Override
        public String toString() {
            if (value == null) {
                value = getClass().getSimpleName() + " " + computeStringValue();
            }
            return value;
        }
    }

    /**
     * A CONSTANT_Class_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.1
     */
    private class ConstantClassInfo extends CpInfo {
        final int nameIndex;

        /** Parses a CONSTANT_Class_info entry. */
        public ConstantClassInfo() throws IOException {
            this.nameIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(nameIndex))
                    .computeStringValue();
        }
    }

    /**
     * A CONSTANT_Fieldref_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2
     */
    private class ConstantFieldRefInfo extends CpInfo {
        final int classIndex;
        final int nameAndTypeIndex;

        /** Parses a CONSTANT_Fieldref_info entry. */
        public ConstantFieldRefInfo() throws IOException {
            this.classIndex = parseU2(null);
            this.nameAndTypeIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantClassInfo) constantPool.get(classIndex))
                    .computeStringValue() + "."
                    + ((ConstantNameAndTypeInfo) constantPool
                            .get(nameAndTypeIndex)).computeStringValue();
        }
    }

    /**
     * A CONSTANT_Methodref_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2
     */
    private class ConstantMethodRefInfo extends CpInfo {
        final int classIndex;
        final int nameAndTypeIndex;

        /** Parses a CONSTANT_Methodref_info entry. */
        public ConstantMethodRefInfo() throws IOException {
            this.classIndex = parseU2(null);
            this.nameAndTypeIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantClassInfo) constantPool.get(classIndex))
                    .computeStringValue() + "."
                    + ((ConstantNameAndTypeInfo) constantPool
                            .get(nameAndTypeIndex)).computeStringValue();
        }
    }

    /**
     * A CONSTANT_InterfaceMethodref_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.2
     */
    private class ConstantInterfaceMethodRefInfo extends CpInfo {
        final int classIndex;
        final int nameAndTypeIndex;

        /** Parses a CONSTANT_InterfaceMethodref_info entry. */
        public ConstantInterfaceMethodRefInfo() throws IOException {
            this.classIndex = parseU2(null);
            this.nameAndTypeIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantClassInfo) constantPool.get(classIndex))
                    .computeStringValue() + "."
                    + ((ConstantNameAndTypeInfo) constantPool
                            .get(nameAndTypeIndex)).computeStringValue();
        }
    }

    /**
     * A CONSTANT_String_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.3
     */
    private class ConstantStringInfo extends CpInfo {
        final int stringIndex;

        /** Parses a CONSTANT_String_info entry. */
        public ConstantStringInfo() throws IOException {
            this.stringIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(stringIndex))
                    .computeStringValue();
        }
    }

    /**
     * A CONSTANT_Integer_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.4
     */
    private class ConstantIntegerInfo extends CpInfo {

        /** Parses a CONSTANT_Integer_info entry. */
        public ConstantIntegerInfo() throws IOException {
            this.value = Integer.toString(parseU4(null));
        }
    }

    /**
     * A CONSTANT_Float_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.4
     */
    private class ConstantFloatInfo extends CpInfo {

        /** Parses a CONSTANT_Float_info entry. */
        public ConstantFloatInfo() throws IOException {
            this.value = Float.toString(Float.intBitsToFloat(parseU4(null)));
        }
    }

    /**
     * A CONSTANT_Long_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.5
     */
    private class ConstantLongInfo extends CpInfo {

        /** Parses a CONSTANT_Long_info entry. */
        public ConstantLongInfo() throws IOException {
            long highBytes = parseU4(null);
            long lowBytes = parseU4(null) & 0xFFFFFFFFL;
            this.value = Long.toString((highBytes << 32) | lowBytes);
        }

        @Override
        protected boolean useTwoEntries() {
            return true;
        }
    }

    /**
     * A CONSTANT_Double_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.5
     */
    private class ConstantDoubleInfo extends CpInfo {

        /** Parses a CONSTANT_Double_info entry. */
        public ConstantDoubleInfo() throws IOException {
            long highBytes = parseU4(null);
            long lowBytes = parseU4(null) & 0xFFFFFFFFL;
            this.value = Double.toString(
                    Double.longBitsToDouble((highBytes << 32) | lowBytes));
        }

        @Override
        protected boolean useTwoEntries() {
            return true;
        }
    }

    /**
     * A CONSTANT_NameAndType_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.6
     */
    private class ConstantNameAndTypeInfo extends CpInfo {
        final int nameIndex;
        final int descriptorIndex;

        /** Parses a CONSTANT_NameAndType_info entry. */
        public ConstantNameAndTypeInfo() throws IOException {
            this.nameIndex = parseU2(null);
            this.descriptorIndex = parseU2(null);
        }

        @Override
        public String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(nameIndex))
                    .computeStringValue()
                    + ((ConstantUtf8Info) constantPool.get(descriptorIndex))
                            .computeStringValue();
        }
    }

    /**
     * A CONSTANT_Utf8_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.7
     */
    private class ConstantUtf8Info extends CpInfo {

        /** Parses a CONSTANT_Utf8_info entry. */
        public ConstantUtf8Info() throws IOException {
            this.value = input.readUTF();
        }
    }

    /**
     * A CONSTANT_MethodHandle_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.8
     */
    private class ConstantMethodHandleInfo extends CpInfo {
        final int referenceKind;
        final int referenceIndex;

        /** Parses a CONSTANT_MethodHandle_info entry. */
        public ConstantMethodHandleInfo() throws IOException {
            this.referenceKind = parseU1(null);
            this.referenceIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return referenceKind + "." + constantPool.get(referenceIndex);
        }
    }

    /**
     * A CONSTANT_MethodType_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.9
     */
    private class ConstantMethodTypeInfo extends CpInfo {
        final int descriptorIndex;

        /** Parses a CONSTANT_MethodType_info entry. */
        public ConstantMethodTypeInfo() throws IOException {
            this.descriptorIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(descriptorIndex))
                    .computeStringValue();
        }
    }

    /**
     * A CONSTANT_InvokeDynamic_info entry.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.4.10
     */
    private class ConstantInvokeDynamicInfo extends CpInfo {
        final int bootstrapMethodAttrIndex;
        final int nameAndTypeIndex;

        /** Parses a CONSTANT_InvokeDynamic_info entry. */
        public ConstantInvokeDynamicInfo() throws IOException {
            this.bootstrapMethodAttrIndex = parseU2(null);
            this.nameAndTypeIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return bootstrapMethodAttrIndex + "."
                    + ((ConstantNameAndTypeInfo) constantPool
                            .get(nameAndTypeIndex)).computeStringValue();
        }
    }

    /**
     * A CONSTANT_Module_info entry.
     * 
     * TODO Add a link to JVMS se9 when it is available.
     */
    private class ConstantModuleInfo extends CpInfo {
        final int descriptorIndex;

        /** Parses a CONSTANT_Module_info entry. */
        public ConstantModuleInfo() throws IOException {
            this.descriptorIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(descriptorIndex))
                    .computeStringValue();
        }
    }

    /**
     * A CONSTANT_Package_info entry.
     * 
     * TODO Add a link to JVMS se9 when it is available.
     */
    private class ConstantPackageInfo extends CpInfo {
        final int descriptorIndex;

        /** Parses a CONSTANT_Package_info entry. */
        public ConstantPackageInfo() throws IOException {
            this.descriptorIndex = parseU2(null);
        }

        @Override
        protected String computeStringValue() {
            return ((ConstantUtf8Info) constantPool.get(descriptorIndex))
                    .computeStringValue();
        }
    }

    /**
     * Parses a field_info structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.5
     */
    private void parseFieldInfo() throws IOException {
        parseU2("access_flags: ");
        parseU2ConstantPoolIndex("name_index: ");
        parseU2ConstantPoolIndex("descriptor_index: ");
        parseAttributeList();
    }

    /**
     * Parses a method_info structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.6
     */
    private void parseMethodInfo() throws IOException {
        instructionIndices = new HashMap<Integer, Integer>();
        parseU2("access_flags: ");
        parseU2ConstantPoolIndex("name_index: ");
        parseU2ConstantPoolIndex("descriptor_index: ");
        parseAttributeList();
    }

    /**
     * Parses an attribute_info structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7
     */
    private void parseAttributeInfo() throws IOException {
        SortableOutputNode attributeOutputNode = new SortableOutputNode();
        pathToCurrentOutputNode.peek().append(attributeOutputNode);
        pathToCurrentOutputNode.push(attributeOutputNode);
        String attributeName = parseU2ConstantPoolIndex(
                "attribute_name_index: ").toString();
        int attributeLength = parseU4(null);
        attributeOutputNode.sortingKey = attributeName;
        if (attributeName.equals("ConstantValue")) {
            parseConstantValueAttribute();
        } else if (attributeName.equals("Code")) {
            parseCodeAttribute();
        } else if (attributeName.equals("CodeComment")) {
            // empty non-standard attribute used for tests.
        } else if (attributeName.equals("Comment")) {
            // empty non-standard attribute used for tests.
        } else if (attributeName.equals("StackMapTable")) {
            parseStackMapTableAttribute();
        } else if (attributeName.equals("Exceptions")) {
            parseExceptionsAttribute();
        } else if (attributeName.equals("InnerClasses")) {
            parseInnerClassesAttribute();
        } else if (attributeName.equals("EnclosingMethod")) {
            parseEnclosingMethodAttribute();
        } else if (attributeName.equals("Synthetic")) {
            parseSyntheticAttribute();
        } else if (attributeName.equals("Signature")) {
            parseSignatureAttribute();
        } else if (attributeName.equals("SourceFile")) {
            parseSourceFileAttribute();
        } else if (attributeName.equals("SourceDebugExtension")) {
            parseSourceDebugAttribute(attributeLength);
        } else if (attributeName.equals("LineNumberTable")) {
            parseLineNumberTableAttribute();
        } else if (attributeName.equals("LocalVariableTable")) {
            parseLocalVariableTableAttribute();
        } else if (attributeName.equals("LocalVariableTypeTable")) {
            parseLocalVariableTypeTableAttribute();
        } else if (attributeName.equals("Deprecated")) {
            parseDeprecatedAttribute();
        } else if (attributeName.equals("RuntimeVisibleAnnotations")) {
            parseRuntimeVisibleAnnotationsAttribute();
        } else if (attributeName.equals("RuntimeInvisibleAnnotations")) {
            parseRuntimeInvisibleAnnotationsAttribute();
        } else if (attributeName.equals("RuntimeVisibleParameterAnnotations")) {
            parseRuntimeVisibleParameterAnnotationsAttribute();
        } else if (attributeName
                .equals("RuntimeInvisibleParameterAnnotations")) {
            parseRuntimeInvisibleParameterAnnotationsAttribute();
        } else if (attributeName.equals("RuntimeVisibleTypeAnnotations")) {
            parseRuntimeVisibleTypeAnnotationsAttribute();
        } else if (attributeName.equals("RuntimeInvisibleTypeAnnotations")) {
            parseRuntimeInvisibleTypeAnnotationsAttribute();
        } else if (attributeName.equals("AnnotationDefault")) {
            parseAnnotationDefaultAttribute();
        } else if (attributeName.equals("BootstrapMethods")) {
            parseBootstrapMethodsAttribute();
        } else if (attributeName.equals("MethodParameters")) {
            parseMethodParametersAttribute();
        } else if (attributeName.equals("Module")) {
            parseModuleAttribute();
        } else if (attributeName.equals("ModuleMainClass")) {
            parseModuleMainClassAttribute();
        } else if (attributeName.equals("ModulePackages")) {
            parseModulePackagesAttribute();
        } else if (attributeName.equals("StackMap")) {
            parseStackMapAttribute();
        } else {
            throw new IOException("Unknown attribute " + attributeName);
        }
        pathToCurrentOutputNode.pop();
    }

    /**
     * Parses a ConstantValue attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.2
     */
    private void parseConstantValueAttribute() throws IOException {
        parseU2ConstantPoolIndex("constantvalue_index: ");
    }

    /**
     * Parses a Code attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.3
     */
    private void parseCodeAttribute() throws IOException {
        parseU2("max_stack: ");
        parseU2("max_locals: ");
        int codeLength = parseU4(null);
        parseInstructions(codeLength);
        int exceptionCount = parseU2("exception_table_length: ");
        for (int i = 0; i < exceptionCount; ++i) {
            parseU2Label("start_pc: ");
            parseU2Label("end_pc: ");
            parseU2Label("handler_pc: ");
            parseU2ConstantPoolIndex("catch_type: ");
        }
        parseAttributeList();
    }

    /**
     * Parses the bytecode instructions of a method.
     * 
     * @param codeLength
     *            the number of bytes to parse.
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html#jvms-6.5
     */
    private void parseInstructions(int codeLength) throws IOException {
        int bytecodeOffset = 0; // Number of bytes parsed so far.
        int instructionIndex = 0; // Number of instructions parsed so far.
        while (bytecodeOffset < codeLength) {
            instructionIndices.put(bytecodeOffset, instructionIndex);
            int opcode = parseU1(null);
            int startOffset = bytecodeOffset++;
            // Instructions are in alphabetical order of their opcode name, as
            // in the specification. This leads to some duplicated code, but is
            // done on purpose for ease of reference.
            switch (opcode) {
            case 0x32: // aaload
            case 0x53: // aastore
            case 0x01: // aconst_null
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x19: // aload
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x2A: // aload_0
            case 0x2B: // aload_1
            case 0x2C: // aload_2
            case 0x2D: // aload_3
                outputInstruction(instructionIndex, 0x19, opcode - 0x2A);
                break;
            case 0xBD: // anewarray
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0xB0: // areturn
            case 0xBE: // arraylength
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x3A: // astore
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x4B: // astore_0
            case 0x4C: // astore_1
            case 0x4D: // astore_2
            case 0x4E: // astore_3
                outputInstruction(instructionIndex, 0x3A, opcode - 0x4B);
                break;
            case 0xBF: // athrow
            case 0x33: // baload
            case 0x54: // bastore
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x10: // bipush
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x34: // caload
            case 0x55: // castore
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xC0: // checkcast
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0x90: // d2f
            case 0x8E: // d2i
            case 0x8F: // d2l
            case 0x63: // dadd
            case 0x31: // daload
            case 0x52: // dastore
            case 0x98: // dcmpg
            case 0x97: // dcmpl
            case 0x0E: // dconst_0
            case 0x0F: // dconst_1
            case 0x6F: // ddiv
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x18: // dload
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x26: // dload_0
            case 0x27: // dload_1
            case 0x28: // dload_2
            case 0x29: // dload_3
                outputInstruction(instructionIndex, 0x18, opcode - 0x26);
                break;
            case 0x6B: // dmul
            case 0x77: // dneg
            case 0x73: // drem
            case 0xAF: // dreturn
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x39: // dstore
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x47: // dstore_0
            case 0x48: // dstore_1
            case 0x49: // dstore_2
            case 0x4A: // dstore_3
                outputInstruction(instructionIndex, 0x39, opcode - 0x47);
                break;
            case 0x67: // dsub
            case 0x59: // dup
            case 0x5A: // dup_x1
            case 0x5B: // dup_x2
            case 0x5C: // dup2
            case 0x5D: // dup2_x1
            case 0x5E: // dup2_x2
            case 0x8D: // f2d
            case 0x8B: // f2i
            case 0x8C: // f2l
            case 0x62: // fadd
            case 0x30: // faload
            case 0x51: // fastore
            case 0x96: // fcmpg
            case 0x95: // fcmpl
            case 0x0B: // fconst_0
            case 0x0C: // fconst_1
            case 0x0D: // fconst_2
            case 0x6E: // fdiv
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x17: // fload
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x22: // fload_0
            case 0x23: // fload_1
            case 0x24: // fload_2
            case 0x25: // fload_3
                outputInstruction(instructionIndex, 0x17, opcode - 0x22);
                break;
            case 0x6A: // fmul
            case 0x76: // fneg
            case 0x72: // frem
            case 0xAE: // freturn
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x38: // fstore
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x43: // fstore_0
            case 0x44: // fstore_1
            case 0x45: // fstore_2
            case 0x46: // fstore_3
                outputInstruction(instructionIndex, 0x38, opcode - 0x43);
                break;
            case 0x66: // fsub
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xB4: // getfield
            case 0xB2: // getstatic
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0xA7: // goto
                outputInstruction(instructionIndex, opcode,
                        newLabel(startOffset + parseS2(null)));
                bytecodeOffset += 2;
                break;
            case 0xC8: // goto_w
                outputInstruction(instructionIndex, 0xA7,
                        newLabel(startOffset + parseU4(null)));
                bytecodeOffset += 4;
                break;
            case 0x91: // i2b
            case 0x92: // i2c
            case 0x87: // i2d
            case 0x86: // i2f
            case 0x85: // i2l
            case 0x93: // i2s
            case 0x60: // iadd
            case 0x2E: // iaload
            case 0x7E: // iand
            case 0x4F: // iastore
            case 0x02: // iconst_m1
            case 0x03: // iconst_0
            case 0x04: // iconst_1
            case 0x05: // iconst_2
            case 0x06: // iconst_3
            case 0x07: // iconst_4
            case 0x08: // iconst_5
            case 0x6C: // idiv
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xA5: // if_acmpeq
            case 0xA6: // if_acmpne
            case 0x9F: // if_icmpeq
            case 0xA0: // if_icmpne
            case 0xA1: // if_icmplt
            case 0xA2: // if_icmpge
            case 0xA3: // if_icmpgt
            case 0xA4: // if_icmple
            case 0x99: // ifeq
            case 0x9A: // ifne
            case 0x9B: // iflt
            case 0x9C: // ifge
            case 0x9D: // ifgt
            case 0x9E: // ifle
            case 0xC7: // ifnonnull
            case 0xC6: // ifnull
                outputInstruction(instructionIndex, opcode,
                        newLabel(startOffset + parseS2(null)));
                bytecodeOffset += 2;
                break;
            case 0x84: // iinc
                outputInstruction(instructionIndex, opcode, parseU1(null),
                        parseS1(null));
                bytecodeOffset += 2;
                break;
            case 0x15: // iload
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x1A: // iload_0
            case 0x1B: // iload_1
            case 0x1C: // iload_2
            case 0x1D: // iload_3
                outputInstruction(instructionIndex, 0x15, opcode - 0x1A);
                break;
            case 0x68: // imul
            case 0x74: // ineg
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xC1: // instanceof
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0xBA: // invokedynamic
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                parseU2(null);
                bytecodeOffset += 4;
                break;
            case 0xB9: // invokeinterface
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null), parseU1(null));
                parseU1(null);
                bytecodeOffset += 4;
                break;
            case 0xB7: // invokespecial
            case 0xB8: // invokestatic
            case 0xB6: // invokevirtual
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0x80: // ior
            case 0x70: // irem
            case 0xAC: // ireturn
            case 0x78: // ishl
            case 0x7A: // ishr
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x36: // istore
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x3B: // istore_0
            case 0x3C: // istore_1
            case 0x3D: // istore_2
            case 0x3E: // istore_3
                outputInstruction(instructionIndex, 0x36, opcode - 0x3B);
                break;
            case 0x64: // isub
            case 0x7C: // iushr
            case 0x82: // ixor
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xA8: // jsr
                outputInstruction(instructionIndex, opcode,
                        newLabel(startOffset + parseS2(null)));
                bytecodeOffset += 2;
                break;
            case 0xC9: // jsr_w
                outputInstruction(instructionIndex, 0xA8,
                        newLabel(startOffset + parseU4(null)));
                bytecodeOffset += 4;
                break;
            case 0x8A: // l2d
            case 0x89: // l2f
            case 0x88: // l2i
            case 0x61: // ladd
            case 0x2F: // laload
            case 0x7F: // land
            case 0x50: // lastore
            case 0x94: // lcmp
            case 0x09: // lconst_0
            case 0x0A: // lconst_1
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x12: // ldc
                outputInstruction(instructionIndex, opcode,
                        parseU1ConstantPoolIndex(null));
                bytecodeOffset += 1;
                break;
            case 0x13: // ldc_w
            case 0x14: // ldc2_w
                outputInstruction(instructionIndex, 0x12,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0x6D: // ldiv
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x16: // lload
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x1E: // lload_0
            case 0x1F: // lload_1
            case 0x20: // lload_2
            case 0x21: // lload_3
                outputInstruction(instructionIndex, 0x16, opcode - 0x1E);
                break;
            case 0x69: // lmul
            case 0x75: // lneg
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xAB: // lookupswitch
                outputInstruction(instructionIndex, opcode);
                while (bytecodeOffset % 4 != 0) {
                    parseU1(null);
                    bytecodeOffset++;
                }
                output("default: ", newLabel(startOffset + parseU4(null)));
                int pairCount = parseU4("npairs: ");
                bytecodeOffset += 8;
                for (int i = 0; i < pairCount; ++i) {
                    output(parseU4(null) + ": ",
                            newLabel(startOffset + parseU4(null)));
                    bytecodeOffset += 8;
                }
                break;
            case 0x81: // lor
            case 0x71: // lrem
            case 0xAD: // lreturn
            case 0x79: // lshl
            case 0x7B: // lshr
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x37: // lstore
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x3F: // lstore_0
            case 0x40: // lstore_1
            case 0x41: // lstore_2
            case 0x42: // lstore_3
                outputInstruction(instructionIndex, 0x37, opcode - 0x3F);
                break;
            case 0x65: // lsub
            case 0x7D: // lushr
            case 0x83: // lxor
            case 0xC2: // monitorenter
            case 0xC3: // monitorexit
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xC5: // multianewarray
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null), parseU1(null));
                bytecodeOffset += 3;
                break;
            case 0xBB: // new
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0xBC: // newarray
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0x00: // nop
            case 0x57: // pop
            case 0x58: // pop2
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xB5: // putfield
            case 0xB3: // putstatic
                outputInstruction(instructionIndex, opcode,
                        parseU2ConstantPoolIndex(null));
                bytecodeOffset += 2;
                break;
            case 0xA9: // ret
                outputInstruction(instructionIndex, opcode, parseU1(null));
                bytecodeOffset += 1;
                break;
            case 0xB1: // return
            case 0x35: // saload
            case 0x56: // sastore
                outputInstruction(instructionIndex, opcode);
                break;
            case 0x11: // sipush
                outputInstruction(instructionIndex, opcode, parseS2(null));
                bytecodeOffset += 2;
                break;
            case 0x5F: // swap
                outputInstruction(instructionIndex, opcode);
                break;
            case 0xAA: // tableswitch
                outputInstruction(instructionIndex, opcode);
                while (bytecodeOffset % 4 != 0) {
                    parseU1(null);
                    bytecodeOffset++;
                }
                output("default: ", newLabel(startOffset + parseU4(null)));
                int low = parseU4("low: ");
                int high = parseU4("high: ");
                bytecodeOffset += 12;
                for (int i = low; i <= high; ++i) {
                    output(i + ": ", newLabel(startOffset + parseU4(null)));
                    bytecodeOffset += 4;
                }
                break;
            case 0xC4: // wide
                opcode = parseU1(null);
                bytecodeOffset += 1;
                switch (opcode) {
                case 0x15: // iload
                case 0x17: // fload
                case 0x19: // aload
                case 0x16: // lload
                case 0x18: // dload
                case 0x36: // istore
                case 0x38: // fstore
                case 0x3A: // astore
                case 0x37: // lstore
                case 0x39: // dstore
                case 0xA9: // ret
                    outputInstruction(instructionIndex, opcode, parseU2(null));
                    bytecodeOffset += 2;
                    break;
                case 0x84: // iinc
                    outputInstruction(instructionIndex, opcode, parseU2(null),
                            parseS2(null));
                    bytecodeOffset += 4;
                    break;
                default:
                    throw new IOException("Unknown wide opcode: " + opcode);
                }
                break;
            default:
                throw new IOException("Unknown opcode: " + opcode);
            }
            instructionIndex++;
        }
        instructionIndices.put(bytecodeOffset, instructionIndex);
    }

    /**
     * Parses a StackMapTable attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.4
     */
    private void parseStackMapTableAttribute() throws IOException {
        int entryCount = parseU2("number_of_entries: ");
        int bytecodeOffset = -1;
        for (int i = 0; i < entryCount; ++i) {
            int frameType = parseU1(null);
            if (frameType < 64) {
                int offsetDelta = frameType;
                bytecodeOffset += offsetDelta + 1;
                output("SAME ", newLabel(bytecodeOffset));
            } else if (frameType < 128) {
                int offsetDelta = frameType - 64;
                bytecodeOffset += offsetDelta + 1;
                output("SAME_LOCALS_1_STACK_ITEM ", newLabel(bytecodeOffset));
                parseVerificationTypeInfo();
            } else if (frameType < 247) {
                throw new IOException("Unknown frame type " + frameType);
            } else if (frameType == 247) {
                int offsetDelta = parseU2(null);
                bytecodeOffset += offsetDelta + 1;
                output("SAME_LOCALS_1_STACK_ITEM ", newLabel(bytecodeOffset));
                parseVerificationTypeInfo();
            } else if (frameType < 251) {
                int offsetDelta = parseU2(null);
                bytecodeOffset += offsetDelta + 1;
                output("CHOP_" + (251 - frameType) + " ",
                        newLabel(bytecodeOffset));
            } else if (frameType == 251) {
                int offsetDelta = parseU2(null);
                bytecodeOffset += offsetDelta + 1;
                output("SAME ", newLabel(bytecodeOffset));
            } else if (frameType < 255) {
                int offsetDelta = parseU2(null);
                bytecodeOffset += offsetDelta + 1;
                output("APPEND_" + (frameType - 251) + " ",
                        newLabel(bytecodeOffset));
                for (int j = 0; j < frameType - 251; ++j) {
                    parseVerificationTypeInfo();
                }
            } else if (frameType == 255) {
                int offsetDelta = parseU2(null);
                bytecodeOffset += offsetDelta + 1;
                output("FULL ", newLabel(bytecodeOffset));
                int numberOfLocals = parseU2("number_of_locals: ");
                for (int j = 0; j < numberOfLocals; ++j) {
                    parseVerificationTypeInfo();
                }
                int numberOfStackItems = parseU2("number_of_stack_items: ");
                for (int j = 0; j < numberOfStackItems; ++j) {
                    parseVerificationTypeInfo();
                }
            } else {
                throw new IOException("Unknown frame_type: " + frameType);
            }
        }
    }

    /**
     * Parses a verification_type_info structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.2
     */
    private void parseVerificationTypeInfo() throws IOException {
        int tag = parseU1("tag: ");
        if (tag > 8) {
            throw new IOException("Unknown verification_type_info tag: " + tag);
        }
        if (tag == 7) {
            parseU2ConstantPoolIndex("cpool_index: ");
        } else if (tag == 8) {
            parseU2Label("offset: ");
        }
    }

    /**
     * Parses an Exception attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.5
     */
    private void parseExceptionsAttribute() throws IOException {
        int exceptionCount = parseU2("number_of_exceptions: ");
        for (int i = 0; i < exceptionCount; ++i) {
            parseU2ConstantPoolIndex("exception_index: ");
        }
    }

    /**
     * Parses an InnerClasses attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.6
     */
    private void parseInnerClassesAttribute() throws IOException {
        int classCount = parseU2("number_of_classes: ");
        for (int i = 0; i < classCount; ++i) {
            parseU2ConstantPoolIndex("inner_class_info_index: ");
            parseU2ConstantPoolIndex("outer_class_info_index: ");
            parseU2ConstantPoolIndex("inner_name_index: ");
            parseU2("inner_class_access_flags: ");
        }
    }

    /**
     * Parses an EnclosingMethod attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.7
     */
    private void parseEnclosingMethodAttribute() throws IOException {
        parseU2ConstantPoolIndex("class_index: ");
        parseU2ConstantPoolIndex("method_index: ");
    }

    /**
     * Parses a Synthetic attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.8
     */
    private void parseSyntheticAttribute() {
        // Nothing to parse.
    }

    /**
     * Parses a Signature attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.9
     */
    private void parseSignatureAttribute() throws IOException {
        parseU2ConstantPoolIndex("signature_index: ");
    }

    /**
     * Parses a SourceFile attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.10
     */
    private void parseSourceFileAttribute() throws IOException {
        parseU2ConstantPoolIndex("sourcefile_index: ");
    }

    /**
     * Parses a SourceDebug attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.11
     */
    private void parseSourceDebugAttribute(int attributeLength)
            throws IOException {
        byte[] attributeData = new byte[attributeLength];
        input.readFully(attributeData);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < attributeData.length; ++i) {
            stringBuilder.append(attributeData[i]).append(',');
        }
        output("debug_extension: ", stringBuilder.toString());
    }

    /**
     * Parses a LineNumberTable attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.12
     */
    private void parseLineNumberTableAttribute() throws IOException {
        int lineNumberCount = parseU2("line_number_table_length: ");
        for (int i = 0; i < lineNumberCount; ++i) {
            parseU2Label("start_pc: ");
            parseU2("line_number: ");
        }
    }

    /**
     * Parses a LocalVariableTable attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.13
     */
    private void parseLocalVariableTableAttribute() throws IOException {
        int localVariableCount = parseU2("local_variable_table_length: ");
        for (int i = 0; i < localVariableCount; ++i) {
            int startPc = parseU2Label("start_pc: ");
            parseU2CodeLength("length: ", startPc);
            parseU2ConstantPoolIndex("name_index: ");
            parseU2ConstantPoolIndex("descriptor_index: ");
            parseU2("index: ");
        }
    }

    /**
     * Parses a LocalVariableTypeTable attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.14
     */
    private void parseLocalVariableTypeTableAttribute() throws IOException {
        int localVariableCount = parseU2("local_variable_type_table_length: ");
        for (int i = 0; i < localVariableCount; ++i) {
            int startPc = parseU2Label("start_pc: ");
            parseU2CodeLength("length: ", startPc);
            parseU2ConstantPoolIndex("name_index: ");
            parseU2ConstantPoolIndex("signature_index: ");
            parseU2("index: ");
        }
    }

    /**
     * Parses a Deprecated attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.15
     */
    private void parseDeprecatedAttribute() {
        // Nothing to parse.
    }

    /**
     * Parses a RuntimeVisibleAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.16
     */
    private void parseRuntimeVisibleAnnotationsAttribute() throws IOException {
        int annotationCount = parseU2("num_annotations: ");
        for (int i = 0; i < annotationCount; ++i) {
            parseAnnotation();
        }
    }

    /**
     * Parses an annotations structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.16
     */
    private void parseAnnotation() throws IOException {
        parseU2ConstantPoolIndex("type_index: ");
        int elementValuePairCount = parseU2("num_element_value_pairs: ");
        for (int i = 0; i < elementValuePairCount; ++i) {
            parseU2ConstantPoolIndex("element_name_index: ");
            parseElementValue();
        }
    }

    /**
     * Parses an element_value structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.16.1
     */
    private void parseElementValue() throws IOException {
        int tag = parseU1(null);
        switch (tag) {
        case 'B':
        case 'C':
        case 'D':
        case 'F':
        case 'I':
        case 'J':
        case 'S':
        case 'Z':
        case 's':
            parseU2ConstantPoolIndex(((char) tag) + ": ");
            return;
        case 'e':
            parseU2ConstantPoolIndex("e: ");
            parseU2ConstantPoolIndex("const_name_index: ");
            return;
        case 'c':
            parseU2ConstantPoolIndex(((char) tag) + ": ");
            return;
        case '@':
            output("@:", " ");
            parseAnnotation();
            return;
        case '[':
            int valueCount = parseU2("[: ");
            for (int i = 0; i < valueCount; ++i) {
                parseElementValue();
            }
            return;
        default:
            throw new IOException("Unknown element_type tag: " + tag);
        }
    }

    /**
     * Parses a RuntimeInvisibleAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.17
     */
    private void parseRuntimeInvisibleAnnotationsAttribute()
            throws IOException {
        parseRuntimeVisibleAnnotationsAttribute();
    }

    /**
     * Parses a RuntimeVisibleParameterAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.18
     */
    private void parseRuntimeVisibleParameterAnnotationsAttribute()
            throws IOException {
        int parameterCount = parseU1("num_parameters: ");
        for (int i = 0; i < parameterCount; ++i) {
            int annotationCount = parseU2("num_annotations: ");
            for (int j = 0; j < annotationCount; ++j) {
                parseAnnotation();
            }
        }
    }

    /**
     * Parses a RuntimeInvisibleParameterAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.19
     */
    private void parseRuntimeInvisibleParameterAnnotationsAttribute()
            throws IOException {
        parseRuntimeVisibleParameterAnnotationsAttribute();
    }

    /**
     * Parses a RuntimeVisibleTypeAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.20
     */
    private void parseRuntimeVisibleTypeAnnotationsAttribute()
            throws IOException {
        int annotationCount = parseU2("num_annotations: ");
        OutputNode typeAnnotationsOutputNode = new SortedOutputNode();
        pathToCurrentOutputNode.peek().append(typeAnnotationsOutputNode);
        pathToCurrentOutputNode.push(typeAnnotationsOutputNode);
        for (int i = 0; i < annotationCount; ++i) {
            parseTypeAnnotation();
        }
        pathToCurrentOutputNode.pop();
    }

    /**
     * Parses a type_annotation structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.20
     */
    private void parseTypeAnnotation() throws IOException {
        SortableOutputNode typeAnnotationOutputNode = new SortableOutputNode();
        pathToCurrentOutputNode.peek().append(typeAnnotationOutputNode);
        pathToCurrentOutputNode.push(typeAnnotationOutputNode);
        int targetType = parseU1("target_type: ");
        typeAnnotationOutputNode.sortingKey = String.valueOf(targetType);
        switch (targetType) {
        case 0x00:
        case 0x01:
            // type_parameter_target
            parseU1("type_parameter_index: ");
            break;
        case 0x10:
            // supertype_target
            parseU2("supertype_index: ");
            break;
        case 0x11:
        case 0x12:
            // type_parameter_bound_target
            parseU1("type_parameter_index: ");
            parseU1("bound_index: ");
            break;
        case 0x13:
        case 0x14:
        case 0x15:
            // empty_target
            // Nothing to parse.
            break;
        case 0x16:
            // formal_parameter_target
            parseU1("formal_parameter_index: ");
            break;
        case 0x17:
            // throws_target
            parseU2("throws_type_index: ");
            break;
        case 0x40:
        case 0x41:
            // localvar_target
            int tableLength = parseU2("table_length: ");
            for (int i = 0; i < tableLength; ++i) {
                int startPc = parseU2Label("start_pc: ");
                parseU2CodeLength("length: ", startPc);
                parseU2("index: ");
            }
            break;
        case 0x42:
            // catch_target
            parseU2("exception_table_index: ");
            break;
        case 0x43:
        case 0x44:
        case 0x45:
        case 0x46:
            // offset_target
            parseU2Label("offset: ");
            break;
        case 0x47:
        case 0x48:
        case 0x49:
        case 0x4A:
        case 0x4B:
            // type_argument_target
            parseU2Label("offset: ");
            parseU1("type_argument_index: ");
            break;
        default:
            throw new IOException("Unknown target_type: " + targetType);
        }
        parseTypePath();
        parseAnnotation();
        pathToCurrentOutputNode.pop();
    }

    /**
     * Parses a type_path structure.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.20.2
     */
    private void parseTypePath() throws IOException {
        int pathLength = parseU1("path_length: ");
        for (int i = 0; i < pathLength; ++i) {
            parseU1("type_path_kind: ");
            parseU1("type_argument_index: ");
        }
    }

    /**
     * Parses a RuntimeInvisibleTypeAnnotations attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.21
     */
    private void parseRuntimeInvisibleTypeAnnotationsAttribute()
            throws IOException {
        parseRuntimeVisibleTypeAnnotationsAttribute();
    }

    /**
     * Parses an AnnotationDefault attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.22
     */
    private void parseAnnotationDefaultAttribute() throws IOException {
        parseElementValue();
    }

    /**
     * Parses a BootstrapMethods attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.23
     */
    private void parseBootstrapMethodsAttribute() throws IOException {
        int bootstrapMethodCount = parseU2("num_bootstrap_methods: ");
        for (int i = 0; i < bootstrapMethodCount; ++i) {
            parseU2ConstantPoolIndex("bootstrap_method_ref: ");
            int bootstrapArgumentCount = parseU2("num_bootstrap_arguments: ");
            for (int j = 0; j < bootstrapArgumentCount; ++j) {
                parseU2ConstantPoolIndex("bootstrap_argument: ");
            }
        }
    }

    /**
     * Parses a MethodParameters attribute.
     * 
     * @see https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html#jvms-4.7.24
     */
    private void parseMethodParametersAttribute() throws IOException {
        int parameterCount = parseU1("parameters_count: ");
        for (int i = 0; i < parameterCount; ++i) {
            parseU2ConstantPoolIndex("name_index: ");
            parseU2("access_flags: ");
        }
    }

    /**
     * Parses a Module attribute.
     * 
     * TODO Add a link to JVMS se9 when it is available.
     */
    private void parseModuleAttribute() throws IOException {
        parseU2ConstantPoolIndex("name: ");
        parseU2("access: ");
        parseU2ConstantPoolIndex("version: ");
        int requireCount = parseU2("require_count: ");
        for (int i = 0; i < requireCount; ++i) {
            parseU2ConstantPoolIndex("name: ");
            parseU2("access: ");
            parseU2ConstantPoolIndex("version: ");
        }
        int exportCount = parseU2("export_count: ");
        for (int i = 0; i < exportCount; ++i) {
            parseU2ConstantPoolIndex("name: ");
            parseU2("access: ");
            int exportToCount = parseU2("export_to_count: ");
            for (int j = 0; j < exportToCount; ++j) {
                parseU2ConstantPoolIndex("to: ");
            }
        }
        int openCount = parseU2("open_count: ");
        for (int i = 0; i < openCount; ++i) {
            parseU2ConstantPoolIndex("name: ");
            parseU2("access: ");
            int openToCount = parseU2("open_to_count: ");
            for (int j = 0; j < openToCount; ++j) {
                parseU2ConstantPoolIndex("to: ");
            }
        }
        int useCount = parseU2("use_count: ");
        for (int i = 0; i < useCount; ++i) {
            parseU2ConstantPoolIndex("use: ");
        }
        int provideCount = parseU2("provide_count: ");
        for (int i = 0; i < provideCount; ++i) {
            parseU2ConstantPoolIndex("provide: ");
            int provideWithCount = parseU2("provide_with_count: ");
            for (int j = 0; j < provideWithCount; ++j) {
                parseU2ConstantPoolIndex("with: ");
            }
        }
    }

    /**
     * Parses a ModuleMainClass attribute.
     *
     * TODO Add a link to JVMS se9 when it is available.
     */
    private void parseModuleMainClassAttribute() throws IOException {
        parseU2ConstantPoolIndex("main_class: ");
    }

    /**
     * Parses a ModulePackages attribute.
     * 
     * TODO Add a link to JVMS se9 when it is available.
     */
    private void parseModulePackagesAttribute() throws IOException {
        int packageCount = parseU2("package_count: ");
        for (int i = 0; i < packageCount; ++i) {
            parseU2ConstantPoolIndex("package: ");
        }
    }

    /**
     * Parses a StackMap attribute.
     *
     * @see http://docs.oracle.com/javame/config/cldc/opt-pkgs/api/cldc/api/Appendix1-verifier.pdf
     */
    private void parseStackMapAttribute() throws IOException {
        int entryCount = parseU2("number_of_entries: ");
        for (int i = 0; i < entryCount; ++i) {
            parseU2Label("offset: ");
            int numberOfLocals = parseU2("number_of_locals: ");
            for (int j = 0; j < numberOfLocals; ++j) {
                parseVerificationTypeInfo();
            }
            int numberOfStackItems = parseU2("number_of_stack_items: ");
            for (int j = 0; j < numberOfStackItems; ++j) {
                parseVerificationTypeInfo();
            }
        }
    }

    /**
     * Parses an unsigned byte.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    int parseU1(String name) throws IOException {
        int value = input.readUnsignedByte();
        if (name != null) {
            output(name, value);
        }
        return value;
    }

    /**
     * Parses a signed byte.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    int parseS1(String name) throws IOException {
        int value = input.readByte();
        if (name != null) {
            output(name, value);
        }
        return value;
    }

    /**
     * Parses an unsigned short.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    int parseU2(String name) throws IOException {
        int value = input.readUnsignedShort();
        if (name != null) {
            output(name, value);
        }
        return value;
    }

    /**
     * Parses a signed short.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    int parseS2(String name) throws IOException {
        int value = input.readShort();
        if (name != null) {
            output(name, value);
        }
        return value;
    }

    /**
     * Parses an integer.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    int parseU4(String name) throws IOException {
        int value = input.readInt();
        if (name != null) {
            output(name, value);
        }
        return value;
    }

    /**
     * Parses an unsigned short containing a bytecode offset (from the start of
     * the method code). Outputs a label corresponding to this offset.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The parsed value.
     */
    private int parseU2Label(String name) throws IOException {
        int bytecodeOffset = input.readUnsignedShort();
        if (name != null) {
            output(name, newLabel(bytecodeOffset));
        }
        return bytecodeOffset;
    }

    /**
     * Parses an unsigned short containing the length of a bytecode sequence, in
     * bytes. Outputs a label corresponding to the end of this sequence.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @param startOffset
     *            The start of the bytecode sequence, in bytes from the start of
     *            the method code.
     * @return The parsed value.
     */
    private int parseU2CodeLength(String name, int startOffset)
            throws IOException {
        int length = input.readUnsignedShort();
        if (name != null) {
            output(name, newLabel(startOffset + length));
        }
        return length;
    }

    /**
     * Parses an unsigned byte containing the index of a constant pool entry.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The constant pool entry at the parsed index.
     */
    private CpInfo parseU1ConstantPoolIndex(String name) throws IOException {
        CpInfo cpInfo = constantPool.get(input.readUnsignedByte());
        if (name != null) {
            output(name, cpInfo);
        }
        return cpInfo;
    }

    /**
     * Parses an unsigned short containing the index of a constant pool entry.
     * 
     * @param name
     *            The symbolic name of this value. Can be null.
     * @return The constant pool entry at the parsed index.
     */
    private CpInfo parseU2ConstantPoolIndex(String name) throws IOException {
        CpInfo cpInfo = constantPool.get(input.readUnsignedShort());
        if (name != null) {
            output(name, cpInfo);
        }
        return cpInfo;
    }

    /**
     * Appends a named value to the current {@link OutputNode}.
     * 
     * @param name
     *            The symbolic name of a value.
     * @param value
     *            A value.
     */
    void output(String name, Object value) {
        OutputNode currentOutputNode = pathToCurrentOutputNode.peek();
        currentOutputNode.append(name);
        currentOutputNode.append(value);
        currentOutputNode.append("\n");
    }

    /**
     * Appends the string representation of a bytecode instruction to the
     * current {@link OutputNode}.
     * 
     * @param index
     *            The index of the bytecode instruction.
     * @param opcode
     *            The opcode of the bytecode instruction.
     * @param arguments
     *            The arguments of the bytecode instruction.
     */
    void outputInstruction(int index, int opcode, Object... arguments) {
        OutputNode currentOutputNode = pathToCurrentOutputNode.peek();
        currentOutputNode.append(index);
        currentOutputNode.append(": ");
        currentOutputNode.append(opcode);
        for (Object argument : arguments) {
            currentOutputNode.append(" ");
            currentOutputNode.append(argument);
        }
        currentOutputNode.append("\n");
    }

    /**
     * A node of the intermediate tree used to build the final dump of the input
     * class (see {@link #pathToCurrentOutputNode}). The children of this node
     * are stored in a list. A child can be an {@link OutputNode} instance.
     */
    static class OutputNode {
        /** The children of this node. */
        final ArrayList<Object> children = new ArrayList<Object>();

        /** Append the given child after the last child of this node. */
        void append(Object child) {
            children.add(child);
        }

        /**
         * Append the string representation of the tree rooted at this node to
         * the given builder.
         */
        void toString(StringBuilder stringBuilder) {
            for (Object child : children) {
                if (child instanceof OutputNode) {
                    ((OutputNode) child).toString(stringBuilder);
                } else {
                    stringBuilder.append(child);
                }
            }
        }
    }

    /**
     * An {@link OutputNode} which sorts its children before computing its
     * string representation. All the children of this node MUST be instances of
     * {@link SortableOutputNode}.
     */
    static class SortedOutputNode extends OutputNode {

        @Override
        void append(Object child) {
            if (child instanceof SortableOutputNode) {
                super.append(child);
            } else {
                throw new IllegalArgumentException(
                        child + " is not a SortableOutputNode");
            }
        }

        @Override
        void toString(StringBuilder stringBuilder) {
            Collections.sort(children, new Comparator<Object>() {

                public int compare(Object child0, Object child1) {
                    return ((SortableOutputNode) child0).sortingKey.compareTo(
                            ((SortableOutputNode) child1).sortingKey);
                }
            });
            super.toString(stringBuilder);
        }
    }

    /**
     * An {@link OutputNode} suitable for use as a child of a
     * {@link SortedOutputNode}.
     */
    static class SortableOutputNode extends OutputNode {
        /**
         * The key used to sort this node and its siblings in their parent
         * {@link SortedOutputNode}.
         */
        String sortingKey;
    }

    /**
     * An {@link OutputNode} containing a bytecode offset. This offset is
     * converted to an instruction index in the string representation of this
     * node.
     */
    static class LabelOutputNode extends OutputNode {
        /**
         * A map from bytecode offsets to instruction indices. This map may not
         * contain the value corresponding to {@link #bytecodeOffset} when this
         * node is constructed, but it must contain it when {@link #toString} is
         * called.
         */
        final Map<Integer, Integer> instructionIndices;
        /** A bytecode offset, from the start of the method. */
        final int bytecodeOffset;

        LabelOutputNode(Map<Integer, Integer> instructionIndices,
                int bytecodeOffset) {
            this.instructionIndices = instructionIndices;
            this.bytecodeOffset = bytecodeOffset;
        }

        @Override
        void toString(StringBuilder stringBuilder) {
            Integer instructionIndex = instructionIndices.get(bytecodeOffset);
            if (instructionIndex == null) {
                throw new RuntimeException(
                        "Invalid bytecode offset:" + bytecodeOffset);
            }
            stringBuilder.append('<').append(instructionIndex).append('>');
        }
    }

    /**
     * Creates and returns a {@link LabelOutputNode} for the given bytecode
     * offset.
     * 
     * @param bytecodeOffset
     *            An offset in the bytecode of a method (from the start of the
     *            method code).
     * @return A {@link LabelOutputNode} for {@link #bytecodeOffset}.
     */
    LabelOutputNode newLabel(int bytecodeOffset) {
        return new LabelOutputNode(instructionIndices, bytecodeOffset);
    }
}
