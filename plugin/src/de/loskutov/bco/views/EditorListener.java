/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.bco.views;

import org.eclipse.core.filebuffers.IFileBuffer;
import org.eclipse.core.filebuffers.IFileBufferListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;

/**
 * @author Andrei
 */
public class EditorListener implements ISelectionListener, IFileBufferListener,
    IPartListener2 {
    protected BytecodeOutlineView view;
    
    EditorListener(BytecodeOutlineView view){
        this.view = view;
    }  
    
    /** 
     * clean view reference
     */
    public void dispose(){
        this.view = null;
    }
    
    /**
     * @param part
     * @param selection
     * 
     */
    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        if(!(selection instanceof ITextSelection)){
            return;
        }
        view.handleSelectionChanged(part, selection);
    }
    
    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#dirtyStateChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
     */
    public void dirtyStateChanged(IFileBuffer buffer, final boolean isDirty) {
        if(!view.isLinkedWithEditor()){
            return;
        }        
        if(isSupportedBuffer(buffer)){ //$NON-NLS-1$
            // first call set only view flag - cause
            view.handleBufferIsDirty(isDirty);
            
            // second call will really refresh view
            if(!isDirty){
                Runnable runnable = new Runnable() {
                    public void run() {                        
                        view.handleBufferIsDirty(isDirty);
                    }
                };
                Display current = Display.getCurrent();
                // let compiler time for generating bytecode
                current.timerExec(1000, runnable);
            }
        }
    }        
    
    private boolean isSupportedBuffer(IFileBuffer buffer) {
        String fileExtension = buffer.getLocation().getFileExtension();
        // TODO export to properties
        return "java".equals(fileExtension);// || "groovy".equals(fileExtension);  //$NON-NLS-1$//$NON-NLS-2$
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partClosed(IWorkbenchPartReference partRef) {
        view.handlePartHidden(partRef.getPart(false));
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partHidden(IWorkbenchPartReference partRef) {
        view.handlePartHidden(partRef.getPart(false));
    }    
    
    /**
     * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partOpened(IWorkbenchPartReference partRef) {    
        view.handlePartVisible(partRef.getPart(false));
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partVisible(IWorkbenchPartReference partRef) {      
        view.handlePartVisible(partRef.getPart(false));
    }    
    
    
    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferDisposed(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void bufferDisposed(IFileBuffer buffer) {
        // is not used here
    }    
    
    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferCreated(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void bufferCreated(IFileBuffer buffer) {
        // is not used here
    }
    
    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentAboutToBeReplaced(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void bufferContentAboutToBeReplaced(IFileBuffer buffer) {
        // is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#bufferContentReplaced(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void bufferContentReplaced(IFileBuffer buffer) {
        // is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChanging(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void stateChanging(IFileBuffer buffer) {
        // is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#stateValidationChanged(org.eclipse.core.filebuffers.IFileBuffer, boolean)
     */
    public void stateValidationChanged(IFileBuffer buffer, boolean isStateValidated) {
        // is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileMoved(org.eclipse.core.filebuffers.IFileBuffer, org.eclipse.core.runtime.IPath)
     */
    public void underlyingFileMoved(IFileBuffer buffer, IPath path) {
        //is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#underlyingFileDeleted(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void underlyingFileDeleted(IFileBuffer buffer) {
        //is not used here
    }

    /**
     * @see org.eclipse.core.filebuffers.IFileBufferListener#stateChangeFailed(org.eclipse.core.filebuffers.IFileBuffer)
     */
    public void stateChangeFailed(IFileBuffer buffer) {
        //is not used here
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partInputChanged(IWorkbenchPartReference partRef) {
        // is not used here
    }
        
    /**
     * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partActivated(IWorkbenchPartReference partRef) {
        // is not used here
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partBroughtToTop(IWorkbenchPartReference partRef) {
        // is not used here
    }

    /**
     * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    public void partDeactivated(IWorkbenchPartReference partRef) {
        // is not used here
    }    





}
