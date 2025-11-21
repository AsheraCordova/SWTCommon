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

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class ColorManager {
    public static Color getColor(String color) {
        try {
        	int alpha = 255;
        	
        	if (color.length() == 9) {
        		alpha = Integer.decode(color.substring(0, 3));
				color = "#" + color.substring(3); 
        	}
            Integer intval = Integer.decode(color);
            int i = intval.intValue();

            Color swtColor = new Color(Display.getCurrent(), (i >> 16) & 0xFF, (i >> 8) & 0xFF, i & 0xFF, alpha);
            return swtColor;
        } catch (NumberFormatException nfe) {
            throw new RuntimeException(nfe);
        }
    }
    public static void dispose() {
    }
}
