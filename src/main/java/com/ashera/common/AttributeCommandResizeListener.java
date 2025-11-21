//start - license
/*
 * Copyright (c) 2025 Ashera Cordova
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
 */
//end - license
package com.ashera.common;

import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import com.ashera.widget.AttributeCommandChain;
import com.ashera.widget.IWidget;

public class AttributeCommandResizeListener implements Listener {
	private IWidget widget;
	private Rectangle rect;
	private String attribute;
	private ValueSetter valueSetter;

	public AttributeCommandResizeListener(IWidget widget, String attribute, ValueSetter valueSetter) {
		super();
		this.valueSetter = valueSetter;
		this.attribute = attribute;
		this.widget = widget;
	}

	@Override
	public void handleEvent(Event event) {
		Control control = (Control) widget.asNativeWidget();

		if (event == null || rect == null || !rect.equals(control.getBounds())) {
			rect = control.getBounds();
			AttributeCommandChain attributeCommandChain = widget.getAttributeCommandChain(this.attribute);
			
			if (attributeCommandChain != null) {
				Object value = attributeCommandChain.getValue(widget, widget.asNativeWidget(), "predraw");
				this.valueSetter.setValueOnControl(control, value);
			}
		}
	}
	
	public interface ValueSetter {
		void setValueOnControl(Control control, Object value);
	}
	
	public static AttributeCommandResizeListener addResizeListenerToControl(IWidget widget, String attribute, ValueSetter valueSetter) {
		Control control = (Control) widget.asNativeWidget();
	    AttributeCommandResizeListener listener = (AttributeCommandResizeListener) control.getData("resizeListener");
	    if (listener == null) {
			listener = new AttributeCommandResizeListener(widget, attribute, valueSetter);
			control.setData("resizeListener", listener);
			control.addListener(org.eclipse.swt.SWT.Resize, listener);
		}	
	    
	    return listener;
	}
 
}