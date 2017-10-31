// ASM: a very small and fast Java bytecode manipulation framework
// Copyright (c) 2000-2011 INRIA, France Telecom
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions
// are met:
// 1. Redistributions of source code must retain the above copyright
//    notice, this list of conditions and the following disclaimer.
// 2. Redistributions in binary form must reproduce the above copyright
//    notice, this list of conditions and the following disclaimer in the
//    documentation and/or other materials provided with the distribution.
// 3. Neither the name of the copyright holders nor the names of its
//    contributors may be used to endorse or promote products derived from
//    this software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
// THE POSSIBILITY OF SUCH DAMAGE.
package org.objectweb.asm;

/**
 * A {@link ClassVisitor} that generates classes in bytecode form. More precisely this visitor
 * generates a byte array conforming to the Java class file format. It can be used alone, to
 * generate a Java class "from scratch", or with one or more {@link ClassReader ClassReader} and
 * adapter class visitor to generate a modified class from one or more existing Java classes.
 *
 * @author Eric Bruneton
 */
public class ClassWriter extends ClassVisitor {

  /**
   * Flag to automatically compute the maximum stack size and the maximum number of local variables
   * of methods. If this flag is set, then the arguments of the {@link MethodVisitor#visitMaxs
   * visitMaxs} method of the {@link MethodVisitor} returned by the {@link #visitMethod visitMethod}
   * method will be ignored, and computed automatically from the signature and the bytecode of each
   * method.
   *
   * @see #ClassWriter(int)
   */
  public static final int COMPUTE_MAXS = 1;

  /**
   * Flag to automatically compute the stack map frames of methods from scratch. If this flag is
   * set, then the calls to the {@link MethodVisitor#visitFrame} method are ignored, and the stack
   * map frames are recomputed from the methods bytecode. The arguments of the {@link
   * MethodVisitor#visitMaxs visitMaxs} method are also ignored and recomputed from the bytecode. In
   * other words, COMPUTE_FRAMES implies COMPUTE_MAXS.
   *
   * @see #ClassWriter(int)
   */
  public static final int COMPUTE_FRAMES = 2;

  /** The type of instructions without any argument. */
  static final int NOARG_INSN = 0;

  /** The type of instructions with an signed byte argument. */
  static final int SBYTE_INSN = 1;

  /** The type of instructions with an signed short argument. */
  static final int SHORT_INSN = 2;

  /** The type of instructions with a local variable index argument. */
  static final int VAR_INSN = 3;

  /** The type of instructions with an implicit local variable index argument. */
  static final int IMPLVAR_INSN = 4;

  /** The type of instructions with a type descriptor argument. */
  static final int TYPE_INSN = 5;

  /** The type of field and method invocations instructions. */
  static final int FIELDORMETH_INSN = 6;

  /** The type of the INVOKEINTERFACE/INVOKEDYNAMIC instruction. */
  static final int ITFMETH_INSN = 7;

  /** The type of the INVOKEDYNAMIC instruction. */
  static final int INDYMETH_INSN = 8;

  /** The type of instructions with a 2 bytes bytecode offset label. */
  static final int LABEL_INSN = 9;

  /** The type of instructions with a 4 bytes bytecode offset label. */
  static final int LABELW_INSN = 10;

  /** The type of the LDC instruction. */
  static final int LDC_INSN = 11;

  /** The type of the LDC_W and LDC2_W instructions. */
  static final int LDCW_INSN = 12;

  /** The type of the IINC instruction. */
  static final int IINC_INSN = 13;

  /** The type of the TABLESWITCH instruction. */
  static final int TABL_INSN = 14;

  /** The type of the LOOKUPSWITCH instruction. */
  static final int LOOK_INSN = 15;

  /** The type of the MULTIANEWARRAY instruction. */
  static final int MANA_INSN = 16;

  /** The type of the WIDE instruction. */
  static final int WIDE_INSN = 17;

  /**
   * The type of the ASM pseudo instructions with an unsigned 2 bytes offset label (see
   * Label#resolve).
   */
  static final int ASM_LABEL_INSN = 18;

  /** The type of the ASM pseudo instructions with a 4 bytes offset label. */
  static final int ASM_LABELW_INSN = 19;

  /**
   * Represents a frame inserted between already existing frames. This kind of frame can only be
   * used if the frame content can be computed from the previous existing frame and from the
   * instructions between this existing frame and the inserted one, without any knowledge of the
   * type hierarchy. This kind of frame is only used when an unconditional jump is inserted in a
   * method while expanding an ASM pseudo instruction (see ClassReader).
   */
  static final int F_INSERT = 256;

  /** The instruction types of all JVM opcodes. */
  static final byte[] TYPE;

  /** Minor and major version numbers of the class to be generated. */
  int version;

  final SymbolTable symbolTable;

  /** The access flags of this class. */
  private int access;

  /** The constant pool item that contains the internal name of this class. */
  private int name;

  /** The internal name of this class. */
  String thisName;

  /** The constant pool item that contains the signature of this class. */
  private int signature;

  /** The constant pool item that contains the internal name of the super class of this class. */
  private int superName;

  /** Number of interfaces implemented or extended by this class or interface. */
  private int interfaceCount;

  /**
   * The interfaces implemented or extended by this class or interface. More precisely, this array
   * contains the indexes of the constant pool items that contain the internal names of these
   * interfaces.
   */
  private int[] interfaces;

  /**
   * The index of the constant pool item that contains the name of the source file from which this
   * class was compiled.
   */
  private int sourceFile;

  /** The SourceDebug attribute of this class. */
  private ByteVector sourceDebug;

  /** The module attribute of this class. */
  private ModuleWriter moduleWriter;

  /** The constant pool item that contains the name of the enclosing class of this class. */
  private int enclosingMethodOwner;

  /**
   * The constant pool item that contains the name and descriptor of the enclosing method of this
   * class.
   */
  private int enclosingMethod;

  /** The runtime visible annotations of this class. */
  private AnnotationWriter anns;

  /** The runtime invisible annotations of this class. */
  private AnnotationWriter ianns;

  /** The runtime visible type annotations of this class. */
  private AnnotationWriter tanns;

  /** The runtime invisible type annotations of this class. */
  private AnnotationWriter itanns;

  /** The non standard attributes of this class. */
  private Attribute attrs;

  /** The number of entries in the InnerClasses attribute. */
  private int innerClassesCount;

  /** The InnerClasses attribute. */
  private ByteVector innerClasses;

  /**
   * The fields of this class. These fields are stored in a linked list of {@link FieldWriter}
   * objects, linked to each other by their {@link FieldWriter#fv} field. This field stores the
   * first element of this list.
   */
  private FieldWriter firstField;

  /**
   * The fields of this class. These fields are stored in a linked list of {@link FieldWriter}
   * objects, linked to each other by their {@link FieldWriter#fv} field. This field stores the last
   * element of this list.
   */
  private FieldWriter lastField;

  /**
   * The methods of this class. These methods are stored in a linked list of {@link MethodWriter}
   * objects, linked to each other by their {@link MethodWriter#mv} field. This field stores the
   * first element of this list.
   */
  private MethodWriter firstMethod;

  /**
   * The methods of this class. These methods are stored in a linked list of {@link MethodWriter}
   * objects, linked to each other by their {@link MethodWriter#mv} field. This field stores the
   * last element of this list.
   */
  private MethodWriter lastMethod;

  /**
   * Indicates what must be automatically computed.
   *
   * @see MethodWriter#compute
   */
  private int compute;

  // ------------------------------------------------------------------------
  // Static initializer
  // ------------------------------------------------------------------------

  /** Computes the instruction types of JVM opcodes. */
  static {
    int i;
    byte[] b = new byte[221];
    String s =
        "AAAAAAAAAAAAAAAABCLMMDDDDDEEEEEEEEEEEEEEEEEEEEAAAAAAAADD"
            + "DDDEEEEEEEEEEEEEEEEEEEEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
            + "AAAAAAAAAAAAAAAAANAAAAAAAAAAAAAAAAAAAAJJJJJJJJJJJJJJJJDOPAA"
            + "AAAAGGGGGGGHIFBFAAFFAARQJJKKSSSSSSSSSSSSSSSSSST";
    for (i = 0; i < b.length; ++i) {
      b[i] = (byte) (s.charAt(i) - 'A');
    }
    TYPE = b;

    // code to generate the above string
    //
    // // SBYTE_INSN instructions
    // b[Constants.NEWARRAY] = SBYTE_INSN;
    // b[Constants.BIPUSH] = SBYTE_INSN;
    //
    // // SHORT_INSN instructions
    // b[Constants.SIPUSH] = SHORT_INSN;
    //
    // // (IMPL)VAR_INSN instructions
    // b[Constants.RET] = VAR_INSN;
    // for (i = Constants.ILOAD; i <= Constants.ALOAD; ++i) {
    // b[i] = VAR_INSN;
    // }
    // for (i = Constants.ISTORE; i <= Constants.ASTORE; ++i) {
    // b[i] = VAR_INSN;
    // }
    // for (i = 26; i <= 45; ++i) { // ILOAD_0 to ALOAD_3
    // b[i] = IMPLVAR_INSN;
    // }
    // for (i = 59; i <= 78; ++i) { // ISTORE_0 to ASTORE_3
    // b[i] = IMPLVAR_INSN;
    // }
    //
    // // TYPE_INSN instructions
    // b[Constants.NEW] = TYPE_INSN;
    // b[Constants.ANEWARRAY] = TYPE_INSN;
    // b[Constants.CHECKCAST] = TYPE_INSN;
    // b[Constants.INSTANCEOF] = TYPE_INSN;
    //
    // // (Set)FIELDORMETH_INSN instructions
    // for (i = Constants.GETSTATIC; i <= Constants.INVOKESTATIC; ++i) {
    // b[i] = FIELDORMETH_INSN;
    // }
    // b[Constants.INVOKEINTERFACE] = ITFMETH_INSN;
    // b[Constants.INVOKEDYNAMIC] = INDYMETH_INSN;
    //
    // // LABEL(W)_INSN instructions
    // for (i = Constants.IFEQ; i <= Constants.JSR; ++i) {
    // b[i] = LABEL_INSN;
    // }
    // b[Constants.IFNULL] = LABEL_INSN;
    // b[Constants.IFNONNULL] = LABEL_INSN;
    // b[200] = LABELW_INSN; // GOTO_W
    // b[201] = LABELW_INSN; // JSR_W
    // // temporary opcodes used internally by ASM - see Label and
    // MethodWriter
    // for (i = 202; i < 220; ++i) {
    // b[i] = ASM_LABEL_INSN;
    // }
    // b[220] = ASM_LABELW_INSN;
    //
    // // LDC(_W) instructions
    // b[Constants.LDC] = LDC_INSN;
    // b[19] = LDCW_INSN; // LDC_W
    // b[20] = LDCW_INSN; // LDC2_W
    //
    // // special instructions
    // b[Constants.IINC] = IINC_INSN;
    // b[Constants.TABLESWITCH] = TABL_INSN;
    // b[Constants.LOOKUPSWITCH] = LOOK_INSN;
    // b[Constants.MULTIANEWARRAY] = MANA_INSN;
    // b[196] = WIDE_INSN; // WIDE
    //
    // for (i = 0; i < b.length; ++i) {
    // System.err.print((char)('A' + b[i]));
    // }
    // System.err.println();
  }

  // ------------------------------------------------------------------------
  // Constructor
  // ------------------------------------------------------------------------

  /**
   * Constructs a new {@link ClassWriter} object.
   *
   * @param flags option flags that can be used to modify the default behavior of this class. See
   *     {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
   */
  public ClassWriter(final int flags) {
    this(null, flags);
  }

  /**
   * Constructs a new {@link ClassWriter} object and enables optimizations for "mostly add" bytecode
   * transformations. These optimizations are the following:
   *
   * <ul>
   *   <li>The constant pool from the original class is copied as is in the new class, which saves
   *       time. New constant pool entries will be added at the end if necessary, but unused
   *       constant pool entries <i>won't be removed</i>.
   *   <li>Methods that are not transformed are copied as is in the new class, directly from the
   *       original class bytecode (i.e. without emitting visit events for all the method
   *       instructions), which saves a <i>lot</i> of time. Untransformed methods are detected by
   *       the fact that the {@link ClassReader} receives {@link MethodVisitor} objects that come
   *       from a {@link ClassWriter} (and not from any other {@link ClassVisitor} instance).
   * </ul>
   *
   * @param classReader the {@link ClassReader} used to read the original class. It will be used to
   *     copy the entire constant pool from the original class and also to copy other fragments of
   *     original bytecode where applicable.
   * @param flags option flags that can be used to modify the default behavior of this class.
   *     <i>These option flags do not affect methods that are copied as is in the new class. This
   *     means that neither the maximum stack size nor the stack frames will be computed for these
   *     methods</i>. See {@link #COMPUTE_MAXS}, {@link #COMPUTE_FRAMES}.
   */
  public ClassWriter(final ClassReader classReader, final int flags) {
    super(Opcodes.ASM6);
    symbolTable = classReader == null ? new SymbolTable(this) : new SymbolTable(this, classReader);
    this.compute =
        (flags & COMPUTE_FRAMES) != 0
            ? MethodWriter.FRAMES
            : ((flags & COMPUTE_MAXS) != 0 ? MethodWriter.MAXS : MethodWriter.NOTHING);
  }

  // ------------------------------------------------------------------------
  // Implementation of the ClassVisitor abstract class
  // ------------------------------------------------------------------------

  @Override
  public final void visit(
      final int version,
      final int access,
      final String name,
      final String signature,
      final String superName,
      final String[] interfaces) {
    this.version = version;
    this.access = access;
    this.name = symbolTable.addConstantClass(name).index;
    thisName = name;
    if (signature != null) {
      this.signature = symbolTable.addConstantUtf8(signature);
    }
    this.superName = superName == null ? 0 : symbolTable.addConstantClass(superName).index;
    if (interfaces != null && interfaces.length > 0) {
      interfaceCount = interfaces.length;
      this.interfaces = new int[interfaceCount];
      for (int i = 0; i < interfaceCount; ++i) {
        this.interfaces[i] = symbolTable.addConstantClass(interfaces[i]).index;
      }
    }
  }

  @Override
  public final void visitSource(final String file, final String debug) {
    if (file != null) {
      sourceFile = symbolTable.addConstantUtf8(file);
    }
    if (debug != null) {
      sourceDebug = new ByteVector().encodeUTF8(debug, 0, Integer.MAX_VALUE);
    }
  }

  @Override
  public final ModuleVisitor visitModule(
      final String name, final int access, final String version) {
    return moduleWriter =
        new ModuleWriter(
            this,
            symbolTable.addConstantModule(name).index,
            access,
            version == null ? 0 : symbolTable.addConstantUtf8(version));
  }

  @Override
  public final void visitOuterClass(final String owner, final String name, final String desc) {
    enclosingMethodOwner = symbolTable.addConstantClass(owner).index;
    if (name != null && desc != null) {
      enclosingMethod = symbolTable.addConstantNameAndType(name, desc);
    }
  }

  @Override
  public final AnnotationVisitor visitAnnotation(final String desc, final boolean visible) {
    ByteVector bv = new ByteVector();
    // write type, and reserve space for values count
    bv.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return anns = new AnnotationWriter(symbolTable, bv, anns);
    } else {
      return ianns = new AnnotationWriter(symbolTable, bv, ianns);
    }
  }

  @Override
  public final AnnotationVisitor visitTypeAnnotation(
      int typeRef, TypePath typePath, final String desc, final boolean visible) {
    ByteVector bv = new ByteVector();
    // write target_type, target_info, and target_path
    TypeReference.putTarget(typeRef, bv);
    TypePath.put(typePath, bv);
    // write type, and reserve space for values count
    bv.putShort(symbolTable.addConstantUtf8(desc)).putShort(0);
    if (visible) {
      return tanns = new AnnotationWriter(symbolTable, bv, tanns);
    } else {
      return itanns = new AnnotationWriter(symbolTable, bv, itanns);
    }
  }

  @Override
  public final void visitAttribute(final Attribute attr) {
    attr.nextAttribute = attrs;
    attrs = attr;
  }

  @Override
  public final void visitInnerClass(
      final String name, final String outerName, final String innerName, final int access) {
    if (innerClasses == null) {
      innerClasses = new ByteVector();
    }
    // Sec. 4.7.6 of the JVMS states "Every CONSTANT_Class_info entry in the
    // constant_pool table which represents a class or interface C that is
    // not a package member must have exactly one corresponding entry in the
    // classes array". To avoid duplicates we keep track in the info field
    // of the Symbol of each CONSTANT_Class_info entry C whether an inner
    // class entry has already been added for C (this field is unused for
    // class entries, and changing its value does not change the hashcode
    // and equality tests). If so we store the index of this inner class
    // entry (plus one) in intVal. This trick allows duplicate detection in
    // O(1) time.
    Symbol nameItem = symbolTable.addConstantClass(name);
    if (nameItem.info == 0) {
      ++innerClassesCount;
      innerClasses.putShort(nameItem.index);
      innerClasses.putShort(outerName == null ? 0 : symbolTable.addConstantClass(outerName).index);
      innerClasses.putShort(innerName == null ? 0 : symbolTable.addConstantUtf8(innerName));
      innerClasses.putShort(access);
      nameItem.info = innerClassesCount;
    } else {
      // Compare the inner classes entry nameItem.intVal - 1 with the
      // arguments of this method and throw an exception if there is a
      // difference?
    }
  }

  @Override
  public final FieldVisitor visitField(
      final int access,
      final String name,
      final String desc,
      final String signature,
      final Object value) {
    FieldWriter fieldWriter = new FieldWriter(symbolTable, access, name, desc, signature, value);
    if (firstField == null) {
      firstField = fieldWriter;
    } else {
      lastField.fv = fieldWriter;
    }
    return lastField = fieldWriter;
  }

  @Override
  public final MethodVisitor visitMethod(
      final int access,
      final String name,
      final String desc,
      final String signature,
      final String[] exceptions) {
    MethodWriter methodWriter =
        new MethodWriter(symbolTable, access, name, desc, signature, exceptions, compute);
    if (firstMethod == null) {
      firstMethod = methodWriter;
    } else {
      lastMethod.mv = methodWriter;
    }
    return lastMethod = methodWriter;
  }

  @Override
  public final void visitEnd() {}

  // ------------------------------------------------------------------------
  // Other public methods
  // ------------------------------------------------------------------------

  /**
   * Returns the bytecode of the class that was build with this class writer.
   *
   * @return the bytecode of the class that was build with this class writer.
   */
  public byte[] toByteArray() {
    if (symbolTable.getConstantPoolCount() > 0xFFFF) {
      throw new RuntimeException("Class file too large!");
    }
    // computes the real size of the bytecode of this class
    int size = 24 + 2 * interfaceCount;
    int nbFields = 0;
    FieldWriter fb = firstField;
    while (fb != null) {
      ++nbFields;
      size += fb.getSize();
      fb = (FieldWriter) fb.fv;
    }
    int nbMethods = 0;
    MethodWriter mb = firstMethod;
    while (mb != null) {
      ++nbMethods;
      size += mb.getSize();
      mb = (MethodWriter) mb.mv;
    }
    int attributeCount = 0;
    if (symbolTable.getBootstrapMethodsLength() > 0) {
      // we put it as first attribute in order to improve a bit
      // bootstrap methods copying in SymbolTable.
      ++attributeCount;
      size += 8 + symbolTable.getBootstrapMethodsLength();
      symbolTable.addConstantUtf8("BootstrapMethods");
    }
    if (signature != 0) {
      ++attributeCount;
      size += 8;
      symbolTable.addConstantUtf8("Signature");
    }
    if (sourceFile != 0) {
      ++attributeCount;
      size += 8;
      symbolTable.addConstantUtf8("SourceFile");
    }
    if (sourceDebug != null) {
      ++attributeCount;
      size += sourceDebug.length + 6;
      symbolTable.addConstantUtf8("SourceDebugExtension");
    }
    if (enclosingMethodOwner != 0) {
      ++attributeCount;
      size += 10;
      symbolTable.addConstantUtf8("EnclosingMethod");
    }
    if ((access & Opcodes.ACC_DEPRECATED) != 0) {
      ++attributeCount;
      size += 6;
      symbolTable.addConstantUtf8("Deprecated");
    }
    if ((access & Opcodes.ACC_SYNTHETIC) != 0) {
      if ((version & 0xFFFF) < Opcodes.V1_5) {
        ++attributeCount;
        size += 6;
        symbolTable.addConstantUtf8("Synthetic");
      }
    }
    if (innerClasses != null) {
      ++attributeCount;
      size += 8 + innerClasses.length;
      symbolTable.addConstantUtf8("InnerClasses");
    }
    if (anns != null) {
      ++attributeCount;
      size += anns.getAnnotationsSize("RuntimeVisibleAnnotations");
    }
    if (ianns != null) {
      ++attributeCount;
      size += ianns.getAnnotationsSize("RuntimeInvisibleAnnotations");
    }
    if (tanns != null) {
      ++attributeCount;
      size += tanns.getAnnotationsSize("RuntimeVisibleTypeAnnotations");
    }
    if (itanns != null) {
      ++attributeCount;
      size += itanns.getAnnotationsSize("RuntimeInvisibleTypeAnnotations");
    }
    if (moduleWriter != null) {
      attributeCount += 1 + moduleWriter.attributeCount;
      size += 6 + moduleWriter.size + moduleWriter.attributesSize;
      symbolTable.addConstantUtf8("Module");
    }
    if (attrs != null) {
      attributeCount += attrs.getAttributeCount();
      size += attrs.getAttributesSize(symbolTable);
    }
    size += symbolTable.getConstantPoolLength();
    // allocates a byte vector of this size, in order to avoid unnecessary
    // arraycopy operations in the ByteVector.enlarge() method
    ByteVector out = new ByteVector(size);
    out.putInt(0xCAFEBABE).putInt(version);
    symbolTable.putConstantPool(out);
    int mask =
        Opcodes.ACC_DEPRECATED | ((version & 0xFFFF) < Opcodes.V1_5 ? Opcodes.ACC_SYNTHETIC : 0);
    out.putShort(access & ~mask).putShort(name).putShort(superName);
    out.putShort(interfaceCount);
    for (int i = 0; i < interfaceCount; ++i) {
      out.putShort(interfaces[i]);
    }
    out.putShort(nbFields);
    fb = firstField;
    while (fb != null) {
      fb.put(out);
      fb = (FieldWriter) fb.fv;
    }
    boolean hasAsmInsns = false;
    out.putShort(nbMethods);
    mb = firstMethod;
    while (mb != null) {
      mb.put(out);
      hasAsmInsns |= mb.hasAsmInsns;
      mb = (MethodWriter) mb.mv;
    }
    out.putShort(attributeCount);
    symbolTable.putBootstrapMethods(out);
    if (signature != 0) {
      out.putShort(symbolTable.addConstantUtf8("Signature")).putInt(2).putShort(signature);
    }
    if (sourceFile != 0) {
      out.putShort(symbolTable.addConstantUtf8("SourceFile")).putInt(2).putShort(sourceFile);
    }
    if (sourceDebug != null) {
      int len = sourceDebug.length;
      out.putShort(symbolTable.addConstantUtf8("SourceDebugExtension")).putInt(len);
      out.putByteArray(sourceDebug.data, 0, len);
    }
    if (moduleWriter != null) {
      out.putShort(symbolTable.addConstantUtf8("Module"));
      moduleWriter.put(out);
      moduleWriter.putAttributes(out);
    }
    if (enclosingMethodOwner != 0) {
      out.putShort(symbolTable.addConstantUtf8("EnclosingMethod")).putInt(4);
      out.putShort(enclosingMethodOwner).putShort(enclosingMethod);
    }
    if ((access & Opcodes.ACC_DEPRECATED) != 0) {
      out.putShort(symbolTable.addConstantUtf8("Deprecated")).putInt(0);
    }
    if ((access & Opcodes.ACC_SYNTHETIC) != 0 && (version & 0xFFFF) < Opcodes.V1_5) {
      out.putShort(symbolTable.addConstantUtf8("Synthetic")).putInt(0);
    }
    if (innerClasses != null) {
      out.putShort(symbolTable.addConstantUtf8("InnerClasses"));
      out.putInt(innerClasses.length + 2).putShort(innerClassesCount);
      out.putByteArray(innerClasses.data, 0, innerClasses.length);
    }
    if (anns != null) {
      anns.putAnnotations(symbolTable.addConstantUtf8("RuntimeVisibleAnnotations"), out);
    }
    if (ianns != null) {
      ianns.putAnnotations(symbolTable.addConstantUtf8("RuntimeInvisibleAnnotations"), out);
    }
    if (tanns != null) {
      tanns.putAnnotations(symbolTable.addConstantUtf8("RuntimeVisibleTypeAnnotations"), out);
    }
    if (itanns != null) {
      itanns.putAnnotations(symbolTable.addConstantUtf8("RuntimeInvisibleTypeAnnotations"), out);
    }
    if (attrs != null) {
      attrs.putAttributes(symbolTable, out);
    }
    if (hasAsmInsns) {
      boolean hasFrames = false;
      mb = firstMethod;
      while (mb != null) {
        hasFrames |= mb.frameCount > 0;
        mb = (MethodWriter) mb.mv;
      }
      anns = null;
      ianns = null;
      attrs = null;
      moduleWriter = null;
      firstField = null;
      lastField = null;
      firstMethod = null;
      lastMethod = null;
      compute = hasFrames ? MethodWriter.INSERTED_FRAMES : MethodWriter.NOTHING;
      hasAsmInsns = false;
      new ClassReader(out.data)
          .accept(this, (hasFrames ? ClassReader.EXPAND_FRAMES : 0) | ClassReader.EXPAND_ASM_INSNS);
      return toByteArray();
    }
    return out.data;
  }

  // ------------------------------------------------------------------------
  // Utility methods: constant pool management
  // ------------------------------------------------------------------------

  /**
   * Adds a number or string constant to the constant pool of the class being build. Does nothing if
   * the constant pool already contains a similar item. <i>This method is intended for {@link
   * Attribute} sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param cst the value of the constant to be added to the constant pool. This parameter must be
   *     an {@link Integer}, a {@link Float}, a {@link Long}, a {@link Double} or a {@link String}.
   * @return the index of a new or already existing constant item with the given value.
   */
  public int newConst(final Object cst) {
    return symbolTable.addConstant(cst).index;
  }

  /**
   * Adds an UTF8 string to the constant pool of the class being build. Does nothing if the constant
   * pool already contains a similar item. <i>This method is intended for {@link Attribute} sub
   * classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param value the String value.
   * @return the index of a new or already existing UTF8 item.
   */
  public int newUTF8(final String value) {
    return symbolTable.addConstantUtf8(value);
  }

  /**
   * Adds a class reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param value the internal name of the class.
   * @return the index of a new or already existing class reference item.
   */
  public int newClass(final String value) {
    return symbolTable.addConstantClass(value).index;
  }

  /**
   * Adds a method type reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param methodDesc method descriptor of the method type.
   * @return the index of a new or already existing method type reference item.
   */
  public int newMethodType(final String methodDesc) {
    return symbolTable.addConstantMethodType(methodDesc).index;
  }

  /**
   * Adds a module reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param moduleName name of the module.
   * @return the index of a new or already existing module reference item.
   */
  public int newModule(final String moduleName) {
    return symbolTable.addConstantModule(moduleName).index;
  }

  /**
   * Adds a package reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param packageName name of the package in its internal form.
   * @return the index of a new or already existing module reference item.
   */
  public int newPackage(final String packageName) {
    return symbolTable.addConstantPackage(packageName).index;
  }

  /**
   * Adds a handle to the constant pool of the class being build. Does nothing if the constant pool
   * already contains a similar item. <i>This method is intended for {@link Attribute} sub classes,
   * and is normally not needed by class generators or adapters.</i>
   *
   * @param tag the kind of this handle. Must be {@link Opcodes#H_GETFIELD}, {@link
   *     Opcodes#H_GETSTATIC}, {@link Opcodes#H_PUTFIELD}, {@link Opcodes#H_PUTSTATIC}, {@link
   *     Opcodes#H_INVOKEVIRTUAL}, {@link Opcodes#H_INVOKESTATIC}, {@link Opcodes#H_INVOKESPECIAL},
   *     {@link Opcodes#H_NEWINVOKESPECIAL} or {@link Opcodes#H_INVOKEINTERFACE}.
   * @param owner the internal name of the field or method owner class.
   * @param name the name of the field or method.
   * @param desc the descriptor of the field or method.
   * @return the index of a new or already existing method type reference item.
   * @deprecated this method is superseded by {@link #newHandle(int, String, String, String,
   *     boolean)}.
   */
  @Deprecated
  public int newHandle(final int tag, final String owner, final String name, final String desc) {
    return newHandle(tag, owner, name, desc, tag == Opcodes.H_INVOKEINTERFACE);
  }

  /**
   * Adds a handle to the constant pool of the class being build. Does nothing if the constant pool
   * already contains a similar item. <i>This method is intended for {@link Attribute} sub classes,
   * and is normally not needed by class generators or adapters.</i>
   *
   * @param tag the kind of this handle. Must be {@link Opcodes#H_GETFIELD}, {@link
   *     Opcodes#H_GETSTATIC}, {@link Opcodes#H_PUTFIELD}, {@link Opcodes#H_PUTSTATIC}, {@link
   *     Opcodes#H_INVOKEVIRTUAL}, {@link Opcodes#H_INVOKESTATIC}, {@link Opcodes#H_INVOKESPECIAL},
   *     {@link Opcodes#H_NEWINVOKESPECIAL} or {@link Opcodes#H_INVOKEINTERFACE}.
   * @param owner the internal name of the field or method owner class.
   * @param name the name of the field or method.
   * @param desc the descriptor of the field or method.
   * @param itf true if the owner is an interface.
   * @return the index of a new or already existing method type reference item.
   */
  public int newHandle(
      final int tag, final String owner, final String name, final String desc, final boolean itf) {
    return symbolTable.addConstantMethodHandle(tag, owner, name, desc, itf).index;
  }

  /**
   * Adds an invokedynamic reference to the constant pool of the class being build. Does nothing if
   * the constant pool already contains a similar item. <i>This method is intended for {@link
   * Attribute} sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param name name of the invoked method.
   * @param desc descriptor of the invoke method.
   * @param bsm the bootstrap method.
   * @param bsmArgs the bootstrap method constant arguments.
   * @return the index of a new or already existing invokedynamic reference item.
   */
  public int newInvokeDynamic(
      final String name, final String desc, final Handle bsm, final Object... bsmArgs) {
    return symbolTable.addConstantInvokeDynamic(name, desc, bsm, bsmArgs).index;
  }

  /**
   * Adds a field reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param owner the internal name of the field's owner class.
   * @param name the field's name.
   * @param desc the field's descriptor.
   * @return the index of a new or already existing field reference item.
   */
  public int newField(final String owner, final String name, final String desc) {
    return symbolTable.addConstantFieldref(owner, name, desc).index;
  }

  /**
   * Adds a method reference to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param owner the internal name of the method's owner class.
   * @param name the method's name.
   * @param desc the method's descriptor.
   * @param itf <tt>true</tt> if <tt>owner</tt> is an interface.
   * @return the index of a new or already existing method reference item.
   */
  public int newMethod(
      final String owner, final String name, final String desc, final boolean itf) {
    return symbolTable.addConstantMethodref(owner, name, desc, itf).index;
  }

  /**
   * Adds a name and type to the constant pool of the class being build. Does nothing if the
   * constant pool already contains a similar item. <i>This method is intended for {@link Attribute}
   * sub classes, and is normally not needed by class generators or adapters.</i>
   *
   * @param name a name.
   * @param desc a type descriptor.
   * @return the index of a new or already existing name and type item.
   */
  public int newNameType(final String name, final String desc) {
    return symbolTable.addConstantNameAndType(name, desc);
  }

  /**
   * Returns the common super type of the two given types. The default implementation of this method
   * <i>loads</i> the two given classes and uses the java.lang.Class methods to find the common
   * super class. It can be overridden to compute this common super type in other ways, in
   * particular without actually loading any class, or to take into account the class that is
   * currently being generated by this ClassWriter, which can of course not be loaded since it is
   * under construction.
   *
   * @param type1 the internal name of a class.
   * @param type2 the internal name of another class.
   * @return the internal name of the common super class of the two given classes.
   */
  protected String getCommonSuperClass(final String type1, final String type2) {
    Class<?> c, d;
    ClassLoader classLoader = getClass().getClassLoader();
    try {
      c = Class.forName(type1.replace('/', '.'), false, classLoader);
      d = Class.forName(type2.replace('/', '.'), false, classLoader);
    } catch (Exception e) {
      throw new RuntimeException(e.toString());
    }
    if (c.isAssignableFrom(d)) {
      return type1;
    }
    if (d.isAssignableFrom(c)) {
      return type2;
    }
    if (c.isInterface() || d.isInterface()) {
      return "java/lang/Object";
    } else {
      do {
        c = c.getSuperclass();
      } while (!c.isAssignableFrom(d));
      return c.getName().replace('.', '/');
    }
  }
}
