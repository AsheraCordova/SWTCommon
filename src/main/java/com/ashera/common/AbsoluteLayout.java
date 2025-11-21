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

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;

/**
 * A simple layout manager that positions child controls using the bounds
 * provided as their layout data. Each control's layout data should be an
 * instance of {@link Rectangle} representing its desired position and size.
 *
 * This layout is equivalent in effect to using a null layout, but expressed
 * through the standard SWT {@link Layout} API.
 */
public class AbsoluteLayout extends Layout {

    @Override
    protected Point computeSize(Composite parent, int wHint, int hHint, boolean flushCache) {
        int maxRight = 0;
        int maxBottom = 0;

        for (Control child : parent.getChildren()) {
            Object data = child.getLayoutData();
            if (!(data instanceof Rectangle)) {
                continue;
            }
            Rectangle r = (Rectangle) data;
            int right = r.x + r.width;
            int bottom = r.y + r.height;
            if (right > maxRight) maxRight = right;
            if (bottom > maxBottom) maxBottom = bottom;
        }

        return new Point(maxRight, maxBottom);
    }

    @Override
    protected void layout(Composite parent, boolean flushCache) {
        for (Control child : parent.getChildren()) {
            Object data = child.getLayoutData();
            if (data instanceof Rectangle) {
                Rectangle r = (Rectangle) data;
                child.setBounds(r);
            }
        }
    }
}