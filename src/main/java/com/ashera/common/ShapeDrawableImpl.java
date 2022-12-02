package com.ashera.common;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import r.android.graphics.Rect;

public class ShapeDrawableImpl extends r.android.graphics.drawable.ShapeDrawable{
	private Image image;
	@Override
	public java.lang.Object getDrawable() {
		Rect bounds = getBounds();
		Display display = Display.getDefault();
		if (bounds.isEmpty()) {
			return new Image(display, 1, 1);
		}

		if (image == null) {
			int width = bounds.width();
			int height = bounds.height();

			image = new Image(display, width, height);

			GC gc = new GC(image);
			switch (getType()) {
			case "line":
				if (getStrokeColor() != null) {
					gc.setForeground((Color) getStrokeColor());
				}
				if (getStrokeWidth() != -1) {
					gc.setLineWidth(getStrokeWidth());
				}
				if (getStrokeDashWidth() != -1 && getStrokeDashGap() != -1) {
					gc.setLineDash(new int[] { getStrokeDashWidth(), getStrokeDashGap() });
				}
				gc.drawLine(0, height/2, width, (height/2));
				    
				break;

			default:
				break;
			}
			
			gc.dispose();
		}
		return image;
	}
	
	@Override
	protected void onBoundsChange(r.android.graphics.Rect bounds) {
		super.onBoundsChange(bounds);
		if (image != null) {
			image.dispose();
			image = null;
		}
	}
}
