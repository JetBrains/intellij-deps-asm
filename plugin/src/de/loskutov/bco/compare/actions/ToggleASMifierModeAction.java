/*******************************************************************************
 * Copyright (c) 2004 Andrei Loskutov.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the BSD License
 * which accompanies this distribution, and is available at
 * http://www.opensource.org/licenses/bsd-license.php
 * Contributor:  Andrei Loskutov - initial API and implementation
 *******************************************************************************/
package de.loskutov.bco.compare.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.loskutov.bco.BytecodeOutlinePlugin;

/**
 * Action used in compare view/bytecode view to toggle ASMifier formatter on/off.
 * To use it, register IPropertyChangeListener and check for IAction.CHECKED
 * event name.
 * @author Andrei
 */
public class ToggleASMifierModeAction extends Action {
    private static final String TASM_ID = "tasm"; //$NON-NLS-1$
    private boolean isASMifierMode;
    
    /**
     * Init action with image/text/tooltip
     */
    public ToggleASMifierModeAction() {
        super();
        setImageDescriptor(AbstractUIPlugin.imageDescriptorFromPlugin(
            BytecodeOutlinePlugin.getDefault().getBundle().getSymbolicName(),
            BytecodeOutlinePlugin.IMG_ASM));

        setText(BytecodeOutlinePlugin
            .getResourceString("ToggleASMifierModeAction.toggleASMifierMode_text")); //$NON-NLS-1$
        setToolTipText(BytecodeOutlinePlugin
            .getResourceString("ToggleASMifierModeAction.toggleASMifierMode_tooltip")); //$NON-NLS-1$
    }

    /**
     * @see org.eclipse.jface.action.IAction#getId()
     */
    public String getId() {
        return TASM_ID;
    }
        
    /**
     * @see org.eclipse.jface.action.IAction#run()
     */
    public void run() {
        isASMifierMode = !isASMifierMode;
        setChecked(isASMifierMode);
    }
}