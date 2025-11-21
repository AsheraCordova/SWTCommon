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
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * Instances of this class are controls located on the top of a shell. They
 * display a blurred version of the content of the shell
 */
public class BlurredPanel {
	private final Composite parent;
	private int radius;
	private Composite panel;
	private Image blurImage;
	private Canvas canvas;
	private ResizeListener listener;


	public Composite getPanel() {
		return panel;
	}

	public void setPanel(Composite panel) {
		this.panel = panel;
	}
	private class ResizeListener implements Listener {
		@Override
		public void handleEvent(Event event) {
			panel.setBounds(0, 0, parent.getBounds().width, parent.getBounds().height);
		}
	}

	public Canvas getCanvas() {
		return canvas;
	}

	/**
	 * Constructs a new instance of this class given its parent.
	 *
	 * @param shell a shell that will be the parent of the new instance (cannot
	 *            be null)
	 * @exception IllegalArgumentException
	 *                <ul>
	 *                <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
	 *                </ul>
	 * @exception SWTException
	 *                <ul>
	 *                <li>ERROR_WIDGET_DISPOSED - if the parent has been
	 *                disposed</li>
	 *                <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
	 *                thread that created the parent</li>
	 *                </ul>
	 */
	public BlurredPanel(final Composite shell) {
		if (shell == null) {
			SWT.error(SWT.ERROR_NULL_ARGUMENT);
		}

		if (shell.isDisposed()) {
			SWT.error(SWT.ERROR_INVALID_ARGUMENT);
		}

		parent = shell;
		radius = 2;
		
		if (parent.isDisposed()) {
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		}

		panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new FillLayout());
		panel.setEnabled(true);

		panel.addListener(SWT.KeyUp, event -> {
			event.doit = false;
		});

		canvas = new Canvas(panel, SWT.NO_BACKGROUND | SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(event -> {
			paintCanvas(event);
		});
		panel.setBounds(0, 0, parent.getBounds().width, parent.getBounds().height);
		this.listener = new ResizeListener();
		parent.addListener(SWT.Resize, listener);
	}
	
	/**
	 * Paint the canvas that holds the panel
	 *
	 * @param e {@link PaintEvent}
	 */
	private void paintCanvas(final PaintEvent e) {
		if (blurImage == null) {
			// Paint the panel
			blurImage = createBlurredImage();
		}
		e.gc.drawImage(blurImage, 0, 0);
	}
	
	public void dispose() {
		if (blurImage != null) {
			blurImage.dispose();
		}
		parent.removeListener(SWT.Resize, this.listener);
		canvas.dispose();
		panel.dispose();
	}

	private Image createBlurredImage() {
		final GC gc = new GC(parent.getDisplay());
		final Image image = new Image(parent.getDisplay(), parent.getSize().x, parent.getSize().y);
		Point point = parent.getShell().toDisplay(0, 0);
		gc.copyArea(image,point.x,point.y);
		gc.dispose();

		Image image2 = new Image(parent.getDisplay(), blur(image.getImageData(), radius));
		image.dispose();
		return image2;

	}

	/**
	 * Hide the panel
	 */
	public void hide() {
		if (parent.isDisposed()) {
			SWT.error(SWT.ERROR_WIDGET_DISPOSED);
		}

		if (panel == null || panel.isDisposed()) {
			return;
		}

		panel.dispose();
	}

	/**
	 * @return the radius of the blur effect
	 */
	public int getRadius() {
		return radius;
	}

	/**
	 * @param radius the radius to set
	 */
	public void setRadius(final int radius) {
		this.radius = radius;
	}

	public static ImageData blur(final ImageData originalImageData, int radius) {

		if (radius < 1) {
			return originalImageData;
		}

		// prepare new image data with 24-bit direct palette to hold blurred
		// copy of image
		final ImageData newImageData = new ImageData(originalImageData.width, originalImageData.height, 24, new PaletteData(0xFF, 0xFF00, 0xFF0000));
		if (radius >= newImageData.height || radius >= newImageData.width) {
			radius = Math.min(newImageData.height, newImageData.width) - 1;
		}
		// initialize cache
		final ArrayList<RGB[]> rowCache = new ArrayList<>();
		// number of rows of imageData we cache
		final int cacheSize = radius * 2 + 1 > newImageData.height ? newImageData.height : radius * 2 + 1;
		int cacheStartIndex = 0; // which row of imageData the cache begins with
		for (int row = 0; row < cacheSize; row++) {
			// row data is horizontally blurred before caching
			rowCache.add(rowCache.size(), blurRow(originalImageData, row, radius));
		}

		// sum red, green, and blue values separately for averaging
		final RGB[] rowRGBSums = new RGB[newImageData.width];
		final int[] rowRGBAverages = new int[newImageData.width];
		int topSumBoundary = 0; // current top row of summed values scope
		int targetRow = 0; // row with RGB averages to be determined
		int bottomSumBoundary = 0; // current bottom row of summed values scope
		int numRows = 0; // number of rows included in current summing scope
		for (int i = 0; i < newImageData.width; i++) {
			rowRGBSums[i] = new RGB(0, 0, 0);
		}

		while (targetRow < newImageData.height) {
			if (bottomSumBoundary < newImageData.height) {
				do {
					// sum pixel RGB values for each column in our radius scope
					for (int col = 0; col < newImageData.width; col++) {
						rowRGBSums[col].red += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].red;
						rowRGBSums[col].green += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].green;
						rowRGBSums[col].blue += rowCache.get(bottomSumBoundary - cacheStartIndex)[col].blue;
					}
					numRows++;
					bottomSumBoundary++; // move bottom scope boundary lower
					if (bottomSumBoundary < newImageData.height && bottomSumBoundary - cacheStartIndex > radius * 2) {
						// grow cache
						rowCache.add(rowCache.size(), blurRow(originalImageData, bottomSumBoundary, radius));
					}
				} while (bottomSumBoundary <= radius); // to initialize
				// rowRGBSums at start
			}

			if (targetRow - topSumBoundary > radius) {
				// subtract values of top row from sums as scope of summed
				// values moves down
				for (int col = 0; col < newImageData.width; col++) {
					rowRGBSums[col].red -= rowCache.get(topSumBoundary - cacheStartIndex)[col].red;
					rowRGBSums[col].green -= rowCache.get(topSumBoundary - cacheStartIndex)[col].green;
					rowRGBSums[col].blue -= rowCache.get(topSumBoundary - cacheStartIndex)[col].blue;
				}
				numRows--;
				topSumBoundary++; // move top scope boundary lower
				rowCache.remove(0); // remove top row which is out of summing
				// scope
				cacheStartIndex++;
			}

			// calculate each column's RGB-averaged pixel
			for (int col = 0; col < newImageData.width; col++) {
				rowRGBAverages[col] = newImageData.palette.getPixel(new RGB(rowRGBSums[col].red / numRows, rowRGBSums[col].green / numRows, rowRGBSums[col].blue / numRows));
			}

			// replace original pixels
			newImageData.setPixels(0, targetRow, newImageData.width, rowRGBAverages, 0);
			targetRow++;
		}
		return newImageData;
	}

	/**
	 * Average blurs a given row of image data. Returns the blurred row as a matrix
	 * of separated RGB values.
	 */
	private static RGB[] blurRow(final ImageData originalImageData, final int row, final int radius) {
		final RGB[] rowRGBAverages = new RGB[originalImageData.width];
		final int[] lineData = new int[originalImageData.width];
		originalImageData.getPixels(0, row, originalImageData.width, lineData, 0);
		int r = 0, g = 0, b = 0; // sum red, green, and blue values separately
		// for averaging
		int leftSumBoundary = 0; // beginning index of summed values scope
		int targetColumn = 0; // column of RGB average to be determined
		int rightSumBoundary = 0; // ending index of summed values scope
		int numCols = 0; // number of columns included in current summing scope
		RGB rgb;
		while (targetColumn < lineData.length) {
			if (rightSumBoundary < lineData.length) {
				// sum RGB values for each pixel in our radius scope
				do {
					rgb = originalImageData.palette.getRGB(lineData[rightSumBoundary]);
					r += rgb.red;
					g += rgb.green;
					b += rgb.blue;
					numCols++;
					rightSumBoundary++;
				} while (rightSumBoundary <= radius); // to initialize summing
				// scope at start
			}

			// subtract sum of left pixel as summing scope moves right
			if (targetColumn - leftSumBoundary > radius) {
				rgb = originalImageData.palette.getRGB(lineData[leftSumBoundary]);
				r -= rgb.red;
				g -= rgb.green;
				b -= rgb.blue;
				numCols--;
				leftSumBoundary++;
			}

			// calculate RGB averages
			rowRGBAverages[targetColumn] = new RGB(r / numCols, g / numCols, b / numCols);
			targetColumn++;
		}
		return rowRGBAverages;
	}
}