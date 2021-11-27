/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2021 DBeaver Corp and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jkiss.dbeaver.ui.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * Color cell editor
 */
public class CustomColorCellEditor extends CellEditor {

    private DefaultColorSelector colorSelector;
    private Object defaultValue; 
    
    public CustomColorCellEditor(Composite parent) {
        super(parent);
    }

    @Override
    protected Control createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(1, true);
        gridLayout.marginWidth = 0;
        gridLayout.marginHeight = 0;
        container.setLayout(gridLayout);
    
        // implicit commit handler
        FocusAdapter buttonFocusLostListener = new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
            	fireApplyEditorValue();
            }
        };
        
        colorSelector = new DefaultColorSelector(container, true) {
        	@Override
        	public void open() {
        		// don't need to commit by focus handler during dialog operations because focus will be back 
        		this.getButton().removeFocusListener(buttonFocusLostListener); 
        		super.open();
    			this.getButton().addFocusListener(buttonFocusLostListener);
        	}
        };
        
        Button button = colorSelector.getButton();
        button.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, true));

        button.addFocusListener(buttonFocusLostListener); // we only need this handler by default
        button.addTraverseListener(e -> {
			if (e.detail == SWT.TRAVERSE_ESCAPE || e.detail == SWT.TRAVERSE_RETURN) {
				e.doit = false;
			}
		});
        button.addKeyListener(new KeyListener() {
            public void keyPressed(KeyEvent e) {
                switch (e.character) {
                    case SWT.ESC:
                    	if ((e.stateMask & SWT.SHIFT) == SWT.SHIFT && defaultValue != null) {
                    		colorSelector.setColorValue(parseValue(defaultValue));
                    	} else {
                    		colorSelector.setColorValue(colorSelector.getDefaultColorValue());
                    	}
                    	applyEditorValue();
                        // fall through
                    case SWT.CR:
                    	button.removeFocusListener(buttonFocusLostListener); // don't need due to explicit commit 
                        fireApplyEditorValue();
                        break;
                }
            }
            public void keyReleased(KeyEvent e) {
            }
        });
        colorSelector.addListener(e -> {
            applyEditorValue();
		});
        
        return container;
    }

    
    @Override
    protected Object doGetValue() {
        RGB color = colorSelector.getColorValue();
        String colorHexString = String.format("#%02X%02X%02X", color.red, color.green, color.blue);
        return colorHexString;
    }

    @Override
    protected void doSetFocus() {
    	colorSelector.getButton().setFocus();
    }
    
    @Override
    protected void doSetValue(Object value) {
    	Assert.isTrue(colorSelector != null && (value instanceof String));
    	RGB rgbValue = parseValue(value);
        colorSelector.setColorValue(rgbValue);
        colorSelector.setDefaultColorValue(rgbValue);
    }
    
    private RGB parseValue(Object value) {
    	String colorHexString = (String)value;
        java.awt.Color c = java.awt.Color.decode(colorHexString);
        RGB rgbValue = new RGB(c.getRed(), c.getGreen(), c.getBlue());
        return rgbValue;
    }

    @Override
    public LayoutData getLayoutData() {
        LayoutData layoutData = super.getLayoutData();
        layoutData.grabHorizontal = true;
        layoutData.horizontalAlignment = SWT.CENTER;
        return layoutData;
    }

    private void applyEditorValue() {
        // must set the selection before getting value
        Object newValue = doGetValue();
        markDirty();
        boolean isValid = isCorrect(newValue);
        setValueValid(isValid);
    }

    protected int getDoubleClickTimeout() {
        return 0;
    }
    
    public void setDefaultValue(Object value) {
    	defaultValue = value;
    }

}
