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
