/*****************************************************************************************
 * Copyright (c) 2004 Andrei Loskutov. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the BSD License which
 * accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php Contributor: Andrei Loskutov -
 * initial API and implementation
 ****************************************************************************************/
package de.loskutov.bco.views;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.actions.AbstractToggleLinkingAction;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.part.ViewPart;

import de.loskutov.bco.BytecodeOutlinePlugin;
import de.loskutov.bco.asm.AsmUtils;
import de.loskutov.bco.asm.DecompileResult;
import de.loskutov.bco.compare.actions.ToggleASMifierModeAction;
import de.loskutov.bco.ui.EclipseUtils;
import de.loskutov.bco.ui.JdtUtils;

/**
 * This view shows decompiled java bytecode
 * @author Andrei
 */
public class BytecodeOutlineView extends ViewPart {

    protected boolean doLinkWithEditor;
    protected boolean selectedOnly;
    protected boolean showQualifiedNames;
    protected boolean bytecodeChanged;
    protected boolean selectionScopeChanged;
    protected boolean bufferIsDirty;
    protected boolean isASMifierMode;
    private boolean isEnabled;
    private boolean isActive;
    private boolean isVisible;

    protected StyledText textControl;
    protected JavaEditor javaEditor;
    protected IJavaElement javaInput;
    protected IJavaElement lastChildElement;
    protected IJavaElement lastDecompiledElement;
    protected ITextSelection currentSelection;
    protected EditorListener editorListener;

    protected Action linkWithEditorAction;
    protected Action selectionChangedAction;
    protected Action showSelectedOnlyAction;
    protected Action setRawModeAction;
    protected Action toggleASMifierModeAction;

    private ViewInitializer viewInitializer;
    private DecompileResult lastDecompiledResult;


    /**
     * The constructor.
     */
    public BytecodeOutlineView() {
        super();
    }

    /**
     * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite)
     */
    public void init(IViewSite site) {
        super.setSite(site);
        if (editorListener == null) {
            editorListener = new EditorListener(this);
            getSite().getWorkbenchWindow().getPartService().addPartListener(
                editorListener);
        }
    }

    /**
     * This is a callback that will allow us to create the viewer and initialize it.
     * @param parent
     */
    public void createPartControl(Composite parent) {
        viewInitializer = new ViewInitializer();
        textControl = new StyledText(parent, SWT.READ_ONLY | SWT.H_SCROLL
            | SWT.V_SCROLL);
        textControl.setEditable(false);

        viewInitializer.makeActions();
        viewInitializer.hookSelection();
        IActionBars bars = getViewSite().getActionBars();
        viewInitializer.fillLocalPullDown(bars.getMenuManager());
        viewInitializer.fillLocalToolBar(bars.getToolBarManager());
        setEnabled(false);
    }

    /**
     * Passing the focus request to the viewer's control.
     */
    public void setFocus() {
        if (textControl != null) {
            textControl.setFocus();
        }
    }

    protected void handleBufferIsDirty(boolean isDirty) {
        if(!isLinkedWithEditor() || !isActive()){
            return;
        }
        if(isDirty){
            setBufferIsDirty(isDirty);
        } else {
            if(!bufferIsDirty){
                // second time calling with same argument -
                // cause new bytecode should be written now
                setBytecodeChanged(true);
                refreshView();
            } else {
                // first time - set the flag only - cause
                // bytecode is not yet written
                setBufferIsDirty(false);
            }
        }
    }

    protected void handlePartHidden(IWorkbenchPart part){
        if(!isLinkedWithEditor()){
           return;
        }
        if(this == part){
            isVisible = false;
            deActivateView();
        } else if(isActive() && (part instanceof IEditorPart)){
            // check if at least one editor is open
            checkOpenEditors(false);
        }
    }

    /**
     * check if at least one java editor is open - if not, deactivate me
     */
    protected void checkOpenEditors(boolean checkNewSelection) {
        IEditorReference[] editorReferences = getSite().getPage().getEditorReferences();
        if(editorReferences == null || editorReferences.length == 0){
            deActivateView();
        } else {
            if(checkNewSelection){
                IEditorPart activeEditor = EclipseUtils.getActiveEditor();
                if(activeEditor instanceof JavaEditor){
                    ITextSelection selection = EclipseUtils
                        .getSelection(((JavaEditor) activeEditor)
                            .getSelectionProvider());
                    handleSelectionChanged(activeEditor, selection);
                    return;
                }
            }
            for (int i = 0; i < editorReferences.length; i++) {
                IEditorPart editor = editorReferences[i].getEditor(false);
                if(editor instanceof JavaEditor){
                    return;
                }
            }
            // here are all editors checked and no one is java editor
            deActivateView();
        }
    }

    protected void handlePartVisible(IWorkbenchPart part){
        if(!isLinkedWithEditor()){
            return;
        }
        if (this == part) {
            if(isVisible){
                return;
            }
            isVisible = true;
            // check if java editor is already open
            IEditorPart activeEditor = EclipseUtils.getActiveEditor();
            if (!(activeEditor instanceof JavaEditor)) {
                // start monitoring again, even if current editor is not
                // supported - but we at front now
                activateView();
                return;
            }
            part = activeEditor;
            // continue with setting input
        }
        if(isVisible && part instanceof JavaEditor){
            if(isActive() && part == javaEditor){
                return;
            }
            activateView();
            setEnabled(true);
            setInput((JavaEditor) part);
            refreshView();
        } else if(part instanceof IEditorPart){
            if(isActive()){
                deActivateView();
            }
        }
    }

    protected void handleSelectionChanged(IWorkbenchPart part,
        ISelection selection) {
        if (!isLinkedWithEditor() || !isActive() || !(part instanceof IEditorPart)) {
            return;
        }

        if (!(part instanceof JavaEditor)) {
            deActivateView();
            return;
        }

        if(!isEnabled()){
            setEnabled(true);
        }

        if (part != javaEditor) {
            setInput((JavaEditor) part);
        } else {
            updateSelection((ITextSelection) selection);
        }
        refreshView();
    }

    /**
     * Does nothing if view is already deactivated
     */
    private void deActivateView() {
        if(!isActive()){
            return;
        }
        setEnabled(false);
        if (editorListener != null) {
            ISelectionService service = getSite().getWorkbenchWindow()
                .getSelectionService();
            service.removePostSelectionListener(editorListener);
            FileBuffers.getTextFileBufferManager().removeFileBufferListener(
                editorListener);

        }
        if (textControl != null && !textControl.isDisposed()) {
            textControl.setText(""); //$NON-NLS-1$
        }
        currentSelection = null;
        lastDecompiledResult = null;
        lastDecompiledElement = null;
        javaEditor = null;
        javaInput = null;
        lastChildElement = null;
        setBufferIsDirty(false);
        isActive = false;
    }

    /**
     * Does nothing if view is already active
     *
     */
    private void activateView(){
        if(isActive()){
            return;
        }
        isActive = true;
        getSite().getWorkbenchWindow().getSelectionService()
            .addPostSelectionListener(editorListener);
        FileBuffers.getTextFileBufferManager().addFileBufferListener(
            editorListener);
    }

    private void setEnabled(boolean on){
        this.isEnabled = on;
        if (textControl != null && !textControl.isDisposed()) {
            textControl.setEnabled(on);
        }
        showSelectedOnlyAction.setEnabled(on);
        linkWithEditorAction.setEnabled(on);
        selectionChangedAction.setEnabled(on);
        setRawModeAction.setEnabled(on);
        toggleASMifierModeAction.setEnabled(on);
    }

    protected void refreshView() {
        if(!isActive() || !isLinkedWithEditor()){
            return;
        }
        boolean scopeChanged = selectionScopeChanged;
        selectionScopeChanged = false;
        if (javaInput == null || currentSelection == null) {
            deActivateView();
            return;
        }
        IJavaElement childEl = null;
        try {
            childEl = getSelectedJavaElement(currentSelection);
        } catch (JavaModelException e) {
            BytecodeOutlinePlugin.error(null, e);
        }
        if (isJavaStructureChanged(childEl) || scopeChanged) {
            bytecodeChanged = false;
            lastChildElement = childEl;
            DecompileResult result = decompileBytecode(childEl);
            if(result != null){
                textControl.setText(result.getDecompiledCode());
            } else {
                textControl.setText(""); //$NON-NLS-1$
            }
            lastDecompiledResult = result;
        }
        setSelectionInBytecodeView();
    }

    /**
     *
     */
    private void setSelectionInBytecodeView() {
        if (lastDecompiledResult == null
            || lastDecompiledResult.getDecompiledCode() == null) {
            return;
        }
        int startLine = currentSelection.getStartLine() + 1;
        String linePattern = DecompileResult.SOURCE_LINE_PREFIX
            + startLine + DecompileResult.SOURCE_LINE_SUFFIX;
        int idx = lastDecompiledResult.getDecompiledCode().indexOf(linePattern);
        if (idx > 0) {
            try {
                int offsetAtLine = idx;
                int offsetEnd = lastDecompiledResult.getDecompiledCode().indexOf('\n', idx);
                textControl.setSelection(offsetAtLine, offsetEnd);
            } catch (IllegalArgumentException e) {
                BytecodeOutlinePlugin.error(null, e);
            }
        }
    }

    /**
     * @param childEl
     * @return true if either bytecode was rewritten or selection was changed
     */
    private boolean isJavaStructureChanged(IJavaElement childEl) {
        if(bytecodeChanged
            || lastDecompiledElement == null){
            return true;
        }
        // here is lastDecompiledElement != null

        if(lastChildElement == null && childEl == null){
            // no selected child - we stay by entire class bytecode
            return false;
        } else if (lastChildElement == null || !lastChildElement.equals(childEl)) {
            return true;
        }

        return true;
    }

    /**
     * @return java element for current selection
     */
    private IJavaElement getSelectedJavaElement(ITextSelection selection)
        throws JavaModelException {
        IJavaElement childEl = JdtUtils.getElementAtOffset(javaInput, selection);
        if(childEl != null){
            switch(childEl.getElementType()){
                case IJavaElement.METHOD : {
                    break;
                }
                case IJavaElement.FIELD : {
                    break;
                }
                case IJavaElement.INITIALIZER : {
                    break;
                }
                case IJavaElement.LOCAL_VARIABLE : {
                    childEl = childEl.getAncestor(IJavaElement.METHOD);
                    break;
                }
                default : {
                    childEl = null;
                    break;
                }
            }
        }
        return childEl;
    }

    /**
     * @param childEl
     * @return
     * @throws JavaModelException
     */
    private DecompileResult decompileBytecode(IJavaElement childEl) {
        // check here for inner classes too
        IJavaElement type = JdtUtils.getEnclosingType(childEl);
        if(type == null){
            type = javaInput;
        }
        InputStream is = JdtUtils.createInputStream(type);
        lastDecompiledElement = type;
        if(is == null){
            return null;
        }
        DecompileResult decompileResult = null;
        try {
            if(selectedOnly && childEl != null){
                decompileResult = AsmUtils.getBytecode(
                    is, childEl, showQualifiedNames, isASMifierMode);
            } else {
                decompileResult = AsmUtils.getFullBytecode(
                    is, showQualifiedNames, isASMifierMode);
            }
        } catch (IOException e) {
            try {
                // check if compilation unit is ok - then this is user problem
                if(type != null && type.isStructureKnown()){
                    BytecodeOutlinePlugin.error(null, e);
                }
            } catch (JavaModelException e1) {
                // this is compilation problem - don't show message
                BytecodeOutlinePlugin.logError(e);
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                BytecodeOutlinePlugin.logError(e);
            }
        }
        return decompileResult;
    }

    private void setInput(JavaEditor editor) {
        javaEditor = null;
        javaInput = null;

        if (editor != null) {
            IJavaElement javaElem = EclipseUtils.getJavaInput(editor);
            if (javaElem == null) {
                return;
            }
            javaInput = javaElem;
            javaEditor = editor;
            updateSelection(EclipseUtils.getSelection(javaEditor
                .getSelectionProvider()));
            setBufferIsDirty(editor.isDirty());
        }
        setBytecodeChanged(true);
    }

    private void updateSelection(ITextSelection sel) {
        if(sel != null && sel.equals(currentSelection)){
            return;
        }
        currentSelection = sel;
    }

    /**
     * @see org.eclipse.ui.IWorkbenchPart#dispose()
     */
    public void dispose() {
        deActivateView();
        if(editorListener != null){
            getSite().getWorkbenchWindow().getPartService().removePartListener(editorListener);
            editorListener.dispose();
            editorListener = null;
        }
        if (textControl != null) {
            textControl.dispose();
            textControl = null;
        }
        currentSelection = null;
        javaEditor = null;
        javaInput = null;
        lastChildElement = null;
        linkWithEditorAction = null;
        selectionChangedAction = null;
        showSelectedOnlyAction = null;
        setRawModeAction = null;
        toggleASMifierModeAction = null;
        viewInitializer = null;
        lastDecompiledResult = null;
        super.dispose();
    }

    /**
     * @param bufferIsDirty The bufferIsDirty to set.
     */
    private void setBufferIsDirty(boolean bufferIsDirty) {
        this.bufferIsDirty = bufferIsDirty;
    }

    /**
     * @param bytecodeChanged The bytecodeChanged to set.
     */
    private void setBytecodeChanged(boolean bytecodeChanged) {
        this.bytecodeChanged = bytecodeChanged;
    }

    protected void setSelectionInJavaEditor(Point selection) {
        if(javaEditor != null && javaEditor.getViewer() == null){
            // editor was closed - we should clean the reference
            javaEditor = null;
            javaInput = null;
        }
        if(javaEditor == null || lastDecompiledResult == null){
            deActivateView();
            return;
        }

        int startLine = lastDecompiledResult.getSourceLine(selection.x);

        try {
            if (startLine > 0) {
                IRegion lineInformation1 = javaEditor.getViewer().getDocument()
                    .getLineInformation(startLine - 1);

                EclipseUtils.selectInEditor(javaEditor, lineInformation1
                    .getOffset(), lineInformation1.getLength());
            }
        } catch (Exception e) {
            BytecodeOutlinePlugin.logError(e);
        }
    }

    /**
     * Whole scrap about to init this view
     */
    private class ViewInitializer {

        /**
         *
         */
        protected void hookSelection() {
            textControl.addMouseListener(new MouseAdapter() {
                public void mouseDown(MouseEvent e){
                    if(isLinkedWithEditor() && !isASMifierMode){
                        selectionChangedAction.run();
                    }
                }
            });
        }

        protected void fillLocalPullDown(IMenuManager manager) {
            manager.add(linkWithEditorAction);
            manager.add(showSelectedOnlyAction);
            manager.add(setRawModeAction);
            manager.add(toggleASMifierModeAction);
        }

        protected void fillLocalToolBar(IToolBarManager manager) {
            manager.add(linkWithEditorAction);
            manager.add(showSelectedOnlyAction);
            manager.add(setRawModeAction);
            manager.add(toggleASMifierModeAction);
        }

        protected void makeActions() {

            selectionChangedAction = new Action(){
                public void run() {
                    Point selection = textControl.getSelection();
                    setSelectionInJavaEditor(selection);
                }
            };

            linkWithEditorAction = new AbstractToggleLinkingAction() {
                public void run() {
                    doLinkWithEditor = linkWithEditorAction.isChecked();
                    if(doLinkWithEditor){
                        showSelectedOnlyAction.setEnabled(true);
                        setRawModeAction.setEnabled(true);
                        toggleASMifierModeAction.setEnabled(true);
                        checkOpenEditors(true);
                        //refreshView();
                    } else {
                        showSelectedOnlyAction.setEnabled(false);
                        setRawModeAction.setEnabled(false);
                        toggleASMifierModeAction.setEnabled(false);
                    }
                }
            };
            {
                // TODO get preference from store
                linkWithEditorAction.setChecked(true);
                doLinkWithEditor = true;
            }
            linkWithEditorAction.setText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.linkWithEditor_text"));  //$NON-NLS-1$
            linkWithEditorAction.setToolTipText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.linkWithEditorText_tooltip")); //$NON-NLS-1$

            showSelectedOnlyAction = new Action() {
                public void run() {
                    selectedOnly = showSelectedOnlyAction.isChecked();
                    selectionScopeChanged = true;
                    refreshView();
                }
            };

            //ImageDescriptor id = JavaPluginImages.DESC_TOOL_SHOW_SEGMENTS;
            //showSelectedOnlyAction.setImageDescriptor(id);
            JavaPluginImages.setToolImageDescriptors(showSelectedOnlyAction, "segment_edit.gif"); //$NON-NLS-1$

            {
                // TODO get preference from store
                showSelectedOnlyAction.setChecked(true);
                selectedOnly = true;
            }
            showSelectedOnlyAction.setText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.showOnlySelection_text"));  //$NON-NLS-1$
            showSelectedOnlyAction.setToolTipText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.showOnlySelection_tooltip")); //$NON-NLS-1$


            setRawModeAction = new Action() {
                public void run() {
                    showQualifiedNames = setRawModeAction.isChecked();
                    selectionScopeChanged = true;
                    refreshView();
                }
            };
            ImageDescriptor idesc = JavaPluginImages.DESC_OBJS_PACKAGE;
            setRawModeAction.setImageDescriptor(idesc);
            {
                // TODO get preference from store
                setRawModeAction.setChecked(false);
                showQualifiedNames = false;
            }
            setRawModeAction.setText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.enableRawMode_text"));  //$NON-NLS-1$
            setRawModeAction.setToolTipText(BytecodeOutlinePlugin.getResourceString("BytecodeOutlineView.enableRawMode_tooltip")); //$NON-NLS-1$


            toggleASMifierModeAction = new ToggleASMifierModeAction();
            toggleASMifierModeAction.addPropertyChangeListener(new IPropertyChangeListener(){
                public void propertyChange(PropertyChangeEvent event) {
                    if(IAction.CHECKED.equals(event.getProperty())){
                        toggleASMifierMode(Boolean.TRUE == event.getNewValue());
                    }
                }
            });
            {
                // TODO get preference from store
                toggleASMifierModeAction.setChecked(false);
                isASMifierMode = false;
            }
        }
    }

    protected void toggleASMifierMode(boolean asmEnabled) {
        isASMifierMode = asmEnabled;
        selectionScopeChanged = true;
        setRawModeAction.setEnabled(!asmEnabled);        
        refreshView();
    }

    /**
     * Are actions on toolbar active?
     * @return Returns the isEnabled.
     */
    private boolean isEnabled() {
        return isEnabled;
    }

    /**
     * Is this view monitoring workspace changes?
     * @return Returns the isActive.
     */
    private boolean isActive() {
        return isActive;
    }

    /**
     * Is this view state changes depending on editor changes?
     * @return true if linked with editor
     */
    protected boolean isLinkedWithEditor(){
        return doLinkWithEditor;
    }
}