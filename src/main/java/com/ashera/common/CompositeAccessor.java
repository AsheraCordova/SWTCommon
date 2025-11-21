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

import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;

import aurelienribon.tweenengine.TweenAccessor;

public class CompositeAccessor implements TweenAccessor<Control> {
	public static final int POS_X = 1;

	public CompositeAccessor() {
	}

	@Override
	public int getValues(Control panel, int tweenType, float[] returnValues) {
		switch (tweenType) {
		case POS_X:
			returnValues[0] = panel.getBounds().x;
			return 2;

		default:
			assert false;
			return -1;
		}
	}

	@Override
	public void setValues(Control panel, int tweenType, float[] newValues) {
		switch (tweenType) {
		case POS_X: {
			panel.setBounds((int) newValues[0], panel.getBounds().y, panel.getBounds().width, panel.getBounds().height);
			Listener[] listeners = panel.getListeners(AnimationUtils.Animating);
			for (Listener listener : listeners) {
				listener.handleEvent(null);
			}

		}
			break;

		default:
			assert false;
		}
	}
}