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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

/**
 * Removes JSR instructions and inlines the referenced subroutines.
 * 
 * 
 * <b>Explanation of how it works</b> TODO
 * 
 * @author Niko Matsakis
 */
public class JSRInlinerAdapter extends MethodNode implements Opcodes {

    /**
     * The visitor to which we will emit a translation of this method without
     * internal subroutines.
     */
    private MethodVisitor mv;

    /**
     * An array of the instructions: used to store the original, uninlined
     * instructions while we are processing.
     */
    private AbstractInsnNode[] insns;

    /**
     * For each label, indicates the index of its LabelNode within the mInsns
     * array. Maps a Label to an Integer.
     */
    private Map labels = new HashMap();

    /**
     * If the method contains at least one JSR instruction.
     */
    private boolean seenJSR;

    /**
     * This counter is used to provide increment ids to the subroutines. Those
     * are really only used for debugging print outs.
     */
    private int subroutineId = 0;

    /**
     * For each label that is jumped to by a JSR, we create a Subroutine
     * instance. Map<Label,Subroutine> is the generic type.
     */
    private Map subroutineHeads = new Hashtable();

    /**
     * This subroutine instance denotes the line of execution that is not
     * contained within any subroutine; i.e., the "subroutine" that is executing
     * when a method first begins.
     */
    private final Subroutine mainSubroutine = new Subroutine(-1);

    /**
     * This BitSet contains the index of every instruction that belongs to more
     * than one subroutine. This should not happen often.
     */
    private BitSet dualCitizens = new BitSet();

    /**
     * Creates a new JSRInliner.
     * 
     * @param mv the <code>MethodVisitor</code> to send the resulting inlined
     *        method code to (use <code>null</code> for none).
     * @param access the method's access flags (see {@link Opcodes}). This
     *        parameter also indicates if the method is synthetic and/or
     *        deprecated.
     * @param name the method's name.
     * @param desc the method's descriptor (see {@link Type}).
     * @param signature the method's signature. May be <tt>null</tt>.
     * @param exceptions the internal names of the method's exception classes
     *        (see {@link Type#getInternalName() getInternalName}). May be
     *        <tt>null</tt>.
     */
    public JSRInlinerAdapter(
        final MethodVisitor mv,
        final int access,
        final String name,
        final String desc,
        final String signature,
        final String[] exceptions)
    {
        super(access, name, desc, signature, exceptions);
        this.mv = mv;
    }

    /**
     * Detects a JSR instruction and sets a flag to indicate we will need to do
     * inlining.
     */
    public void visitJumpInsn(final int opcode, final Label lbl) {
        super.visitJumpInsn(opcode, lbl);
        if (opcode == JSR) {
            seenJSR = true;
        }
    }

    /**
     * If any JSRs were seen, triggers the inlining process. Otherwise, forwards
     * the byte codes untouched.
     */
    public void visitEnd() {
        if (seenJSR) {
            insns = (AbstractInsnNode[]) instructions.toArray(new AbstractInsnNode[instructions.size()]);
            populateLabelMap();
            markSubroutines();
            // logSource();
            emitCode();
        }

        // Forward the translate opcodes on if appropriate:
        if (mv != null) {
            accept(mv);
        }
    }

    /**
     * Find all labels nodes and put their index into mLabels.
     */
    private void populateLabelMap() {
        for (int i = 0; i < insns.length; i++) {
            AbstractInsnNode node = insns[i];
            switch (node.getType()) {
                case AbstractInsnNode.LABEL: {
                    labels.put(((LabelNode) node).label, new Integer(i));
                    break;
                }
                case AbstractInsnNode.JUMP_INSN: {
                    if (node.getOpcode() == JSR) {
                        Label tar = ((JumpInsnNode) node).label;
                        if (!subroutineHeads.containsKey(tar)) {
                            Subroutine subr = new Subroutine(subroutineId++);
                            subroutineHeads.put(tar, subr);
                        }
                    }
                    break;
                }
            }
        }
    }

    /**
     * Returns the index of the LabelNode for this Label in the instructions
     * array.
     * 
     * @param lbl TODO.
     * @return TODO.
     */
    private int derefLabel(final Label lbl) {
        return ((Integer) labels.get(lbl)).intValue();
    }

    /**
     * Walks the method and determines which internal subroutine(s), if any,
     * each instruction is a method of.
     */
    private void markSubroutines() {
        BitSet anyvisited = new BitSet();

        // First walk the main subroutine and find all those instructions which
        // can be reached without invoking any JSR at all
        markSubroutineWalk(mainSubroutine, 0, anyvisited);

        // Go through the head of each subroutine and find any nodes reachable
        // to that subroutine without following any JSR links.
        for (Iterator it = subroutineHeads.entrySet().iterator(); it.hasNext();)
        {
            Map.Entry entry = (Map.Entry) it.next();
            Label lab = (Label) entry.getKey();
            Subroutine sub = (Subroutine) entry.getValue();
            int index = derefLabel(lab);
            markSubroutineWalk(sub, index, anyvisited);
        }
    }

    /**
     * Performs a depth first search walking the normal byte code path starting
     * at <code>index</code>, and adding each instruction encountered into
     * the subroutine <code>sub</code>. After this walk is complete, iterates
     * over the exception handlers to ensure that we also include those byte
     * codes which are reachable through an exception that may be thrown during
     * the execution of the subroutine. Invoked from
     * <code>markSubroutines()</code>.
     * 
     * @param sub TODO.
     * @param index TODO.
     * @param anyvisited TODO.
     */
    private void markSubroutineWalk(
        final Subroutine sub,
        final int index,
        final BitSet anyvisited)
    {
        // log("Mark subroutine walk: sub=" + sub + " index=" + index);

        // First find those instructions reachable via normal execution
        markSubroutineWalkDFS(sub, index, anyvisited);

        // Now, make sure we also include any applicable exception handlers
        boolean loop = true;
        while (loop) {
            loop = false;
            for (Iterator it = tryCatchBlocks.iterator(); it.hasNext();) {
                TryCatchBlockNode trycatch = (TryCatchBlockNode) it.next();

                // log("Scanning try/catch " + trycatch);

                // If the handler has already been processed, skip it.
                int handlerindex = derefLabel(trycatch.handler);
                if (sub.instructions.get(handlerindex)) {
                    continue;
                }

                int startindex = derefLabel(trycatch.start);
                int endindex = derefLabel(trycatch.end);
                int nextbit = sub.instructions.nextSetBit(startindex);
                if (nextbit != -1 && nextbit < endindex) {
                    // log("Adding exception handler: " + startindex + "-" +
                    // endindex + " due to " + nextbit + " handler " +
                    // handlerindex);
                    markSubroutineWalkDFS(sub, handlerindex, anyvisited);
                    loop = true;
                }
            }
        }
    }

    /**
     * Performs a simple DFS of the instructions, assigning each to the
     * subroutine <code>sub</code>. Starts from <code>index</code>.
     * Invoked only by <code>markSubroutineWalk()</code>.
     * 
     * @param sub TODO.
     * @param index TODO.
     * @param anyvisited TODO.
     */
    private void markSubroutineWalkDFS(
        final Subroutine sub,
        int index,
        final BitSet anyvisited)
    {
        while (true) {
            AbstractInsnNode node = insns[index];

            // don't visit a node twice
            if (sub.instructions.get(index)) {
                return;
            }
            sub.instructions.set(index);

            // check for those nodes already visited by another subroutine
            if (anyvisited.get(index)) {
                dualCitizens.set(index);
                // log("Instruction #" + index + " is a dual citizen.");
            }
            anyvisited.set(index);

            if (node.getType() == AbstractInsnNode.JUMP_INSN
                    && node.getOpcode() != JSR)
            {
                // we do not follow recursively called subroutines here; but any
                // other sort of branch we do follow
                JumpInsnNode jnode = (JumpInsnNode) node;
                int destidx = derefLabel(jnode.label);
                markSubroutineWalkDFS(sub, destidx, anyvisited);
            }

            // check to see if this opcode falls through to the next instruction
            // or not; if not, return.
            switch (insns[index].getOpcode()) {
                case ARETURN:
                case LRETURN:
                case DRETURN:
                case FRETURN:
                case IRETURN:
                case GOTO:
                case ATHROW:
                case RETURN:
                case RET:
                    /*
                     * note: this either returns from this subroutine, or a
                     * parent subroutine which invoked it
                     */
                    return;
            }

            // Use tail recursion here in the form of an outer while loop to
            // avoid our stack growing needlessly:
            index++;
        }
    }

    /**
     * Creates the new instructions, inlining each instantiation of each
     * subroutine until the code is fully elaborated.
     */
    private void emitCode() {
        LinkedList worklist = new LinkedList();
        instructions.clear();

        // Create an instantiation of the "root" subroutine, which is just the
        // main routine
        worklist.add(new Instantiation(null, mainSubroutine));

        // Emit instantiations of each subroutine we encounter, including the
        // main subroutine
        List newTryCatchBlocks = new ArrayList();
        while (!worklist.isEmpty()) {
            Instantiation inst = (Instantiation) worklist.removeFirst();
            emitSubroutine(inst, worklist, newTryCatchBlocks);
        }

        tryCatchBlocks = newTryCatchBlocks;
    }

    /**
     * Emits one instantiation of one subroutine, specified by
     * <code>instant</code>. May add new instantiations that are invoked by
     * this one to the <code>worklist</code> parameter, and new try/catch
     * blocks to <code>newTryCatchBlocks</code>.
     * 
     * @param instant TODO.
     * @param workList TODO.
     * @param newTryCatchBlocks TODO.
     */
    private void emitSubroutine(
        final Instantiation instant,
        final List worklist,
        final List newTryCatchBlocks)
    {
        final Map remapped = instant.remapped;
        Label duplbl = null;

        // log("--------------------------------------------------------");
        // log("Emitting instantiation of subroutine " + instant.subroutine);

        // Emit the relevant instructions for this instantiation, translating
        // labels and jump targets as we go:
        for (int i = 0; i < insns.length; i++) {
            AbstractInsnNode insn = insns[i];
            Instantiation owner = instant.findOwner(i);

            // We don't want to emit instructions that were already emitted by a
            // subroutine higher on the stack. Note that it is still possible
            // for a given instruction to be emitted twice because it may belong
            // to two subroutines that do not invoke each other.
            if (owner != instant) {
                continue;
            }

            // log("Emitting instruction #" + i + ":");

            if (insn.getType() == AbstractInsnNode.LABEL) {
                // Translate labels into their renamed equivalents.
                // Avoid adding the same label more than once.
                Label ilbl = ((LabelNode) insn).label;
                Label remap = (Label) remapped.get(ilbl);
                // log("Translating label #" + i + ":" + ilbl + " to " + remap);
                if (remap != duplbl) {
                    instructions.add(new LabelNode(remap));
                    duplbl = remap;
                }
            } else if (insn.getOpcode() == RET) {
                // Translate RET instruction(s) to a jump to the return label
                // for the appropriate instantiation. The problem is that the
                // subroutine may "fall through" to the ret of a parent
                // subroutine; therefore, to find the appropriate ret label we
                // find the highest subroutine on the stack that claims to own
                // this instruction. See the class javadoc comment for an
                // explanation on why this technique is safe (note: it is only
                // safe if the input is verifiable).
                Label retlabel = null;
                for (Instantiation p = instant; p != null; p = p.previous) {
                    if (p.subroutine.ownsInstruction(i)) {
                        retlabel = p.returnLabel;
                    }
                }
                if (retlabel == null) {
                    // This is only possible if the mainSubroutine owns a RET
                    // instruction, which should never happen for verifiable
                    // code.
                    throw new RuntimeException("Instruction #" + i
                            + " is a RET not owned by any subroutine");
                }
                instructions.add(new JumpInsnNode(GOTO, retlabel));
            } else if (insn.getOpcode() == JSR) {
                Label lbl = ((JumpInsnNode) insn).label;
                Subroutine sub = (Subroutine) subroutineHeads.get(lbl);
                Instantiation newinst = new Instantiation(instant, sub);
                Label startlbl = (Label) newinst.remapped.get(lbl);

                // log(" Creating instantiation of subr " + sub);

                // Rather than JSRing, we will jump to the inline version and
                // push NULL for what was once the return value. This hack
                // allows us to avoid doing any sort of data flow analysis to
                // figure out which instructions manipulate the old return value
                // pointer which is now known to be unneeded.
                instructions.add(new InsnNode(ACONST_NULL));
                instructions.add(new JumpInsnNode(GOTO, startlbl));
                instructions.add(new LabelNode(newinst.returnLabel));

                // Insert this new instantiation into the queue to be emitted
                // later.
                worklist.add(newinst);
            } else if (remapped != null
                    && insn.getType() == AbstractInsnNode.JUMP_INSN)
            {
                Label lbl = ((JumpInsnNode) insn).label;
                Label rlbl = (Label) remapped.get(lbl);
                instructions.add(new JumpInsnNode(insn.getOpcode(), rlbl));
            } else {
                instructions.add(insn);
            }
        }

        // Emit try/catch blocks that are relevant to this method.
        for (Iterator it = tryCatchBlocks.iterator(); it.hasNext();) {
            TryCatchBlockNode trycatch = (TryCatchBlockNode) it.next();

            final Label start = (Label) remapped.get(trycatch.start);
            final Label end = (Label) remapped.get(trycatch.end);

            // Ignore empty try/catch regions
            if (start == end) {
                continue;
            }

            // Find the appropriate instantiation of the subroutine that owns
            // the handler code and emit the translated version:
            int handlerindex = derefLabel(trycatch.handler);
            for (Instantiation p = instant; p != null; p = p.previous) {
                if (p.subroutine.ownsInstruction(handlerindex)) {
                    Label handler = p.remappedLabel(trycatch.handler);
                    if (start == null || end == null || handler == null) throw new RuntimeException("Internal error!");
                    newTryCatchBlocks.add(new TryCatchBlockNode(start,
                            end,
                            handler,
                            trycatch.type));
                    break;
                }
            }
        }

        // XXX --- emit visitLocalVariable and visitLineNumbers?
        // How to detect the most relevant one?
    }

    private void logSource() {
        log("--------------------------------------------------------");
        log("Input source");
        StringBuffer desc = new StringBuffer();
        for (int i = 0; i < insns.length; i++) {
            String lnum = Integer.toString(i);
            while (lnum.length() < 3) {
                lnum = "0" + lnum;
            }
            desc.setLength(0);
            AbstractInsnNode insn = insns[i];
            if (insn.getOpcode() >= 0) {
                desc.append(org.objectweb.asm.util.AbstractVisitor.OPCODES[insn.getOpcode()]);
            } else if (insn.getType() == AbstractInsnNode.LABEL) {
                desc.append(((LabelNode) insn).label);
            } else {
                desc.append(insn);
            }
            log(lnum + ": " + desc);
        }
        log(mainSubroutine + ": " + mainSubroutine.instructions);
        for (Iterator it = subroutineHeads.values().iterator(); it.hasNext();) {
            Subroutine sub = (Subroutine) it.next();
            log(sub + ": " + sub.instructions);
        }
    }

    private void log(final String str) {
        System.err.println(str);
    }

    protected static class Subroutine {

        public final int id;

        public final BitSet instructions = new BitSet();

        public Subroutine(final int id) {
            this.id = id;
        }

        public void addInstruction(final int idx) {
            instructions.set(idx);
        }

        public boolean ownsInstruction(final int idx) {
            return instructions.get(idx);
        }

        public String toString() {
            return "[Subroutine #" + id + "]";
        }
    }

    /**
     * A class that represents an instantiation of a subroutine. Each
     * instantiation has an associate "stack" --- which is a listing of those
     * instantiations that were active when this particular instance of this
     * subroutine was invoked. Each instantiation also has a map from the
     * original labels of the program to the labels appropriate for this
     * instantiation, and finally a label to return to.
     */
    private class Instantiation {

        /**
         * Previous instantiations; the stack must be statically predictable to
         * be inlinable.
         */
        final Instantiation previous;

        /**
         * The subroutine this is an instantiation of.
         */
        public final Subroutine subroutine;

        /**
         * Any label local to this subroutine will have to be renamed for each
         * instantiation. Maps a Label to a Label.
         */
        public final Map remapped;

        /**
         * All returns for this instantiation will be mapped to this label
         */
        public final Label returnLabel;

        public Instantiation(final Instantiation prev, final Subroutine sub) {
            previous = prev;
            subroutine = sub;

            for (Instantiation p = prev; p != null; p = p.previous) {
                if (p.subroutine == sub) {
                    throw new RuntimeException("Recursive JSR invocation of "
                            + sub);
                }
            }

            // Determine the label to return to when this subroutine terminates
            // via RET: note that the main subroutine never terminates via RET.
            if (prev != null) {
                returnLabel = new Label();
            } else {
                returnLabel = null;
            }

            // Each instantiation will remap the labels from the code above to
            // refer to its particular copy of its own instructions. Note that
            // we collapse labels which point at the same instruction into one:
            // this is fairly common as we are often ignoring large chunks of
            // instructions, so what were previously distinct labels become
            // duplicates.
            //
            // Also note: if we find a label that already belongs to another
            // instantiation higher up on the stack, then we redirect to their
            // copy of the label so to avoid unnecessary duplication of code.
            remapped = new HashMap();
            Label duplbl = null;
            for (int i = 0, c = insns.length; i < c; i++) {
                AbstractInsnNode insn = insns[i];
                Instantiation owner = findOwner(i);

                // Check for instructions which will never be referenced and
                // ignore them.
                if (owner == null) {
                    continue;
                }

                if (insn.getType() == AbstractInsnNode.LABEL) {
                    Label ilbl = ((LabelNode) insns[i]).label;

                    if (owner != this) {
                        // This label may be jumped to by us, but it points at
                        // code owned by a parent instantiation, so get the
                        // remapped name from them.
                        remapped.put(ilbl, owner.remapped.get(ilbl));
                    } else {
                        // We are the first instantiation on the stack to
                        // generate code for this label, so we'll make our own
                        // name for it.
                        if (duplbl == null) {
                            // if we already have a label pointing at this spot,
                            // don't recreate it.
                            duplbl = new Label();
                        }
                        remapped.put(ilbl, duplbl);
                    }
                } else if (owner == this) {
                    // We will emit this instruction, so clear the 'duplbl' flag
                    // since the next Label will refer to a distinct
                    // instruction.
                    duplbl = null;
                }
            }
        }

        /**
         * Returns the "owner" of a particular instruction relative to this
         * instantiation: the owner referes to the Instantiation which will emit
         * the version of this instruction that we will execute.
         * 
         * Typically, the return value is either <code>this</code> or
         * <code>null</code>. <code>this</code> indicates that this
         * instantiation will generate the version of this instruction that we
         * will execute, and <code>null</code> indicates that this
         * instantiation never executes the given instruction.
         * 
         * Sometimes, however, an instruction can belong to multiple
         * subroutines; this is called a "dual citizen" instruction (though it
         * may belong to more than 2 subroutines), and occurs when multiple
         * subroutines branch to common points of control. In this case, the
         * owner is the subroutine that appears highest on the stack, and which
         * also owns the instruction in question.
         * 
         * @param i TODO.
         * @return the "owner" of a particular instruction relative to this
         *         instantiation.
         */
        public Instantiation findOwner(final int i) {
            if (!subroutine.ownsInstruction(i)) {
                return null;
            }
            if (!dualCitizens.get(i)) {
                return this;
            }
            Instantiation own = this;
            for (Instantiation p = previous; p != null; p = p.previous) {
                if (p.subroutine.ownsInstruction(i)) {
                    own = p;
                }
            }
            return own;
        }

        /**
         * Simply looks up <code>l</code> in our internal hashtable.
         * 
         * @param l TODO.
         * @return TODO.
         */
        public Label remappedLabel(final Label l) {
            return (Label) remapped.get(l);
        }
    }
}
