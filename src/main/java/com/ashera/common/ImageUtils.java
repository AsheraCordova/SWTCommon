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

import java.awt.Graphics2D;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.imgscalr.Scalr;

import com.ashera.converter.CommonConverters;
import com.ashera.model.RectM;
import com.ashera.widget.IWidget;

public class ImageUtils {
	private static final Pattern COLOR_SMOOTHEN_REGEX = Pattern
			.compile("r\\s*(>|<)\\s*([0-9]*)\\s*&&\\s*g\\s*(>|<)\\s*([0-9]*)\\s*&&\\s*b\\s*(>|<)\\s*([0-9]*)\\s*");

	private static List<String> evalRegEx(String expression, Pattern regEx, String message) {
		Matcher m = regEx.matcher(expression);
		boolean b = m.matches();
		List<String> groups = new ArrayList<>();
		if (b) {
			for (int i = 1; i <= m.groupCount(); i++) {
				groups.add(m.group(i));

			}
		} else {
			throw new RuntimeException(message + " : " + expression);
		}
		return groups;
	}

	private static void applyTintWithPorterDuff(ImageData imageData, String typeOfImage, RGB tint, int tintAlpha, String compositingMode) {
		int width = imageData.width;
		int height = imageData.height;

		// Get the existing palette
		PaletteData palette = imageData.palette;
		boolean isDirect = palette.isDirect;

		if (isDirect) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					int pixel = imageData.getPixel(x, y);
					int alpha = imageData.getAlpha(x, y);
					// Extract RGB components from the pixel
					RGB pixelRGB = extractRGBDirect(imageData, pixel);
	
					// Apply the chosen compositing mode
					RGB blendedRGB = blendRGB(pixelRGB, tint, tintAlpha, alpha, compositingMode);
	
					// Set the new pixel color
					int newPixel = packRGBDirect(imageData, blendedRGB.red, blendedRGB.green, blendedRGB.blue);
					imageData.setPixel(x, y, newPixel);
					
					// background
					if (typeOfImage != null) {
						switch (typeOfImage) {
						case "png":
							switch (compositingMode) {
							case "src_over":
							case "screen":
							case "add":
								blendedRGB = blendRGBWithAlpha(tint, blendedRGB, alpha);
								newPixel = palette.getPixel(blendedRGB);
								imageData.setPixel(x, y, newPixel);
								imageData.setAlpha(x, y, 255);
								break;
	
							default:
								break;
							}	
							break;
	
						default:
							break;
						}
					}
					
				}
			}
		} else {
			RGB [] rgbs = palette.getRGBs();
			for (int i=0; i<rgbs.length; i++) {
				if (imageData.transparentPixel != i) {
					RGB color = rgbs [i];
					RGB blendedRGB = blendRGB(color, tint, tintAlpha, 0, compositingMode);
					color.red = blendedRGB.red;
					color.blue = blendedRGB.blue;
					color.green = blendedRGB.green;
				}
			}
			imageData.palette = new PaletteData(rgbs);
		}
	}
	

    private static RGB blendRGBWithAlpha(RGB backgroundRGB, RGB foregroundRGB, int alpha) {
        int red = (foregroundRGB.red * alpha + backgroundRGB.red * (255 - alpha)) / 255;
        int green = (foregroundRGB.green * alpha + backgroundRGB.green * (255 - alpha)) / 255;
        int blue = (foregroundRGB.blue * alpha + backgroundRGB.blue * (255 - alpha)) / 255;

        // Clamp RGB values to [0, 255]
        red = Math.max(0, Math.min(255, red));
        green = Math.max(0, Math.min(255, green));
        blue = Math.max(0, Math.min(255, blue));

        return new RGB(red, green, blue);
    }

	private static RGB blendRGB(RGB pixelRGB, RGB tint, int tintAlpha,  int pixelAlpha, String compositingMode) {
		int srcAlpha = tintAlpha;
		 int destAlpha = (pixelAlpha * (255 - srcAlpha)) / 255;

		int red, green, blue;

		switch (compositingMode) {
		case "src_over":
			red = (tint.red * srcAlpha + pixelRGB.red * destAlpha) / 255;
			green = (tint.green * srcAlpha + pixelRGB.green * destAlpha) / 255;
			blue = (tint.blue * srcAlpha + pixelRGB.blue * destAlpha) / 255;
			break;
		case "src_in":
			red = (tint.red * srcAlpha) / (pixelRGB.red + srcAlpha * (1 - pixelRGB.red));
			green = (tint.green * srcAlpha) / (pixelRGB.green + srcAlpha * (1 - pixelRGB.green));
			blue = (tint.blue * srcAlpha) / (pixelRGB.blue + srcAlpha * (1 - pixelRGB.blue));
			break;
		case "screen":
            red = 255 - (((255 - pixelRGB.red) * (255 - tint.red)) / 255);
            green = 255 - (((255 - pixelRGB.green) * (255 - tint.green)) / 255);
            blue = 255 - (((255 - pixelRGB.blue) * (255 - tint.blue)) / 255);
            break;			
		case "add":
            red = Math.min(255, pixelRGB.red + tint.red);
            green = Math.min(255, pixelRGB.green + tint.green);
            blue = Math.min(255, pixelRGB.blue + tint.blue);
            break;

        case "multiply":
            red = (pixelRGB.red * tint.red) / 255;
            green = (pixelRGB.green * tint.green) / 255;
            blue = (pixelRGB.blue * tint.blue) / 255;
            break;

        case "src_atop":
            red = (tint.red * srcAlpha + pixelRGB.red * (255 - srcAlpha)) / 255;
            green = (tint.green * srcAlpha + pixelRGB.green * (255 - srcAlpha)) / 255;
            blue = (tint.blue * srcAlpha + pixelRGB.blue * (255 - srcAlpha)) / 255;
            break;
		default:
			// Default to no blending
			red = pixelRGB.red;
			green = pixelRGB.green;
			blue = pixelRGB.blue;
			break;
		}

		// Clamp RGB values to [0, 255]
		red = Math.max(0, Math.min(255, red));
		green = Math.max(0, Math.min(255, green));
		blue = Math.max(0, Math.min(255, blue));

		return new RGB(red, green, blue);
	}

	private static RGB extractRGBDirect(ImageData imageData, int pixel) {
		PaletteData palette = imageData.palette;
		int redMask = palette.redMask;
		int greenMask = palette.greenMask;
		int blueMask = palette.blueMask;
		int redShift = palette.redShift;
		int greenShift = palette.greenShift;
		int blueShift = palette.blueShift;

		int red = pixel & redMask;
		red = (redShift < 0) ? red >>> -redShift : red << redShift;
		int green = pixel & greenMask;
		green = (greenShift < 0) ? green >>> -greenShift : green << greenShift;
		int blue = pixel & blueMask;
		blue = (blueShift < 0) ? blue >>> -blueShift : blue << blueShift;
		return new RGB(red, green, blue);
	}

	private static int packRGBDirect(ImageData imageData, int red, int green, int blue) {
		int pixel = (red << imageData.palette.redShift * -1) | (green << imageData.palette.greenShift * -1)
				| (blue << imageData.palette.blueShift * -1);
		return pixel;
	}

	public static Image tintImage(Image srcImage, Color c, String compositingMode) {
		if (compositingMode == null) {
			compositingMode = "src_atop";
		}
		// Get image data
		ImageData imageData = srcImage.getImageData();
		String typeOfImage = (String) ((Display) srcImage.getDevice()).getData(srcImage.hashCode() + "");
		// Define the tint color and alpha
		RGB tint = new RGB(c.getRed(), c.getGreen(), c.getBlue());
		int tintAlpha = c.getAlpha(); // 50% transparency (0-255 range)
		// Apply the tint using the chosen compositing mode
		applyTintWithPorterDuff(imageData, typeOfImage, tint, tintAlpha, compositingMode);

		// Create a new image with the tinted image data
		Image tintedImage = new Image(Display.getDefault(), imageData);
		return tintedImage;
	}

	private static Image retainTransparency(Image scaled, ResizeOptions resizeOptions) {
		ImageData imageData = scaled.getImageData();
		imageData.transparentPixel = imageData.getPixel(0, 0);

		if (resizeOptions.getColorSmoothenGcFilter() != null) {
			List<String> groups = evalRegEx(resizeOptions.getColorSmoothenGcFilter(), COLOR_SMOOTHEN_REGEX,
					"Invalid expression. e.g. r > 100 && g > 100 && b > 100");
			PaletteData palette = imageData.palette;
			int redShift = palette.redShift;
			int greenShift = palette.greenShift;
			int blueShift = palette.blueShift;
			int redMask = palette.redMask;
			int greenMask = palette.greenMask;
			int blueMask = palette.blueMask;
			int[] lineData = new int[imageData.width];
			for (int y = 0; y < imageData.height; y++) {
				imageData.getPixels(0, y, imageData.width, lineData, 0);
				// Analyze each pixel value in the line
				for (int x = 0; x < lineData.length; x++) {
					// Extract the red, green and blue component
					int pixel = lineData[x];
					int red = pixel & redMask;
					red = (redShift < 0) ? red >>> -redShift : red << redShift;
					int green = pixel & greenMask;
					green = (greenShift < 0) ? green >>> -greenShift : green << greenShift;
					int blue = pixel & blueMask;
					blue = (blueShift < 0) ? blue >>> -blueShift : blue << blueShift;
					if (checkThreshold(groups, 0, red) && checkThreshold(groups, 2, blue)
							&& checkThreshold(groups, 4, red)) {
						imageData.setPixel(x, y, imageData.getPixel(0, 0));
					} else {
						System.out.println(red + " " + green + " " + blue);
					}
				}
			}
		}
		// Final scaled transparent image
		Image finalImage = new Image(Display.getDefault(), imageData);
		return finalImage;
	}

	private static boolean checkThreshold(List<String> groups, int index, int color) {
		return (groups.get(index).equals("<") && color < Integer.parseInt(groups.get(index + 1)))
				|| (groups.get(index).equals(">") && color > Integer.parseInt(groups.get(index + 1)));
	}

	public static Image crop(Image image, RectM bounds) {
		try {
//			long t0 = System.currentTimeMillis();
			org.eclipse.swt.graphics.ImageLoader imageLoader = new org.eclipse.swt.graphics.ImageLoader();
			imageLoader.data = new ImageData[] { image.getImageData() };
			java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
			imageLoader.save(bos, SWT.IMAGE_PNG);

			java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
			BufferedImage bufferedImage = ImageIO.read(bis);
			bufferedImage = bufferedImage.getSubimage(bounds.x, bounds.y, bounds.width, bounds.height);
			java.io.ByteArrayOutputStream bos1 = new java.io.ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", bos1);
			java.io.ByteArrayInputStream bis1 = new java.io.ByteArrayInputStream(bos1.toByteArray());

			image = new Image(null, new ImageData(bis1));

			bos.close();
			bos1.close();
			bis.close();
			bis1.close();
//			System.out.println("resize" + (System.currentTimeMillis() - t0));

			return image;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static Image scaleImageUsingBufferedImage(Image image, int width, int height, RectM bounds, RectM clip,
			ResizeOptions resizeOptions) throws IOException {
		org.eclipse.swt.graphics.ImageLoader imageLoader = new org.eclipse.swt.graphics.ImageLoader();
		imageLoader.data = new ImageData[] { image.getImageData() };
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		imageLoader.save(bos, SWT.IMAGE_PNG);

		java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
		BufferedImage bufferedImage = ImageIO.read(bis);
		bufferedImage = Scalr.resize(bufferedImage, resizeOptions.getBufferedImageScalingMethod(), Scalr.Mode.FIT_EXACT,
				bounds.width, bounds.height);

		bufferedImage = scaleImage(bufferedImage, width, height, bounds, clip, resizeOptions);
		java.io.ByteArrayOutputStream bos1 = new java.io.ByteArrayOutputStream();
		ImageIO.write(bufferedImage, "png", bos1);
		java.io.ByteArrayInputStream bis1 = new java.io.ByteArrayInputStream(bos1.toByteArray());

		image = new Image(null, new ImageData(bis1));

		bos.close();
		bos1.close();
		bis.close();
		bis1.close();
		return image;
	}

	private static Image scaleImageUsingGC(Image image, int width, int height, RectM bounds, RectM clip,
			ResizeOptions resizeOptions) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled, SWT.LEFT_TO_RIGHT);
		gc.setAntialias(org.eclipse.swt.SWT.ON);
		gc.setInterpolation(SWT.HIGH);

		Color backgroundHint = null;
		if (resizeOptions.useBackgroundHint) {
			backgroundHint = resizeOptions.getBackgroundHint();
		}

		if (backgroundHint != null) {
			gc.setBackground(backgroundHint);
			gc.fillRectangle(0, 0, width, height);
		}

		if (clip != null) {
			gc.setClipping(clip.x, clip.y, clip.width, clip.height);
		}

		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, bounds.x, bounds.y, bounds.width,
				bounds.height);

		if (resizeOptions.isRetainGCTransparency() && image.getImageData().alphaData != null) {
			image = retainTransparency(scaled, resizeOptions);
			scaled.dispose();
		} else {
			image = scaled;
		}
		gc.dispose();
		return image;
	}

	protected static BufferedImage scaleImage(BufferedImage src, int width, int height, RectM bounds, RectM clip,
			ResizeOptions resizeOptions) {
		// Setup the rendering resources to match the source image's
		BufferedImage result = createOptimalImage(src, width, height);
		Graphics2D resultGraphics = result.createGraphics();

		if (clip != null) {
			resultGraphics.setClip(clip.x, clip.y, clip.width, clip.height);
		}

		Color backgroundHint = null;
		if (resizeOptions.useBackgroundHint) {
			backgroundHint = resizeOptions.getBackgroundHint();
		}

		if (backgroundHint != null) {
			resultGraphics.setColor(
					new java.awt.Color(backgroundHint.getRed(), backgroundHint.getBlue(), backgroundHint.getGreen()));
			resultGraphics.fillRect(0, 0, width, height);
		}

		// Scale the image to the new buffer using the specified rendering hint.
		resultGraphics.drawImage(src, bounds.x, bounds.y, bounds.width, bounds.height, null);

		// Just to be clean, explicitly dispose our temporary graphics object
		resultGraphics.dispose();

		// Return the scaled image to the caller.
		return result;
	}

	protected static BufferedImage createOptimalImage(BufferedImage src, int width, int height)
			throws IllegalArgumentException {
		if (width < 0 || height < 0)
			throw new IllegalArgumentException("width [" + width + "] and height [" + height + "] must be >= 0");

		return new BufferedImage(width, height,
				(src.getTransparency() == Transparency.OPAQUE ? BufferedImage.TYPE_INT_ARGB
						: BufferedImage.TYPE_INT_ARGB));
	}

	public static Image resize(Image image, int width, int height, ResizeOptions options) {
		return resize(image, width, height, new RectM(0, 0, width, height), null, options);
	}

	public static Image resize(Image image, int width, int height, RectM bounds, RectM clip, ResizeOptions options) {
		ImageData imageData = image.getImageData();

		if (clip == null && bounds.x == 0 && bounds.y == 0 && imageData.width == bounds.width
				&& imageData.height == bounds.height) {
			// no need of resize in this case
			// return image;
		}

		try {
//			long t0 = System.currentTimeMillis();
			if (options.useBufferedImage) {
				image = scaleImageUsingBufferedImage(image, width, height, bounds, clip, options);
			} else {
				image = scaleImageUsingGC(image, width, height, bounds, clip, options);
			}
//			System.out.println("resize" + (System.currentTimeMillis() - t0));

			return image;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static class ResizeOptions {
		private ResizeOptions() {

		}

		private boolean useBufferedImage = false;
		private org.imgscalr.Scalr.Method bufferedImageScalingMethod = Scalr.Method.QUALITY;
		private Color backgroundHint = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
		private boolean useBackgroundHint = false;
		private boolean retainGCTransparency = true;
		private String colorSmoothenGcFilter;// = "g > 100 && r > 100 && b > 100";

		public boolean isUseBufferedImage() {
			return useBufferedImage;
		}

		public org.imgscalr.Scalr.Method getBufferedImageScalingMethod() {
			return bufferedImageScalingMethod;
		}

		public Color getBackgroundHint() {
			return backgroundHint;
		}

		public boolean isUseBackgroundHint() {
			return useBackgroundHint;
		}

		public boolean isRetainGCTransparency() {
			return retainGCTransparency;
		}

		public String getColorSmoothenGcFilter() {
			return colorSmoothenGcFilter;
		}

		public static class Builder {
			ResizeOptions resizeOptions = new ResizeOptions();

			public Builder withColorSmoothenGcFilter(String regex) {
				resizeOptions.colorSmoothenGcFilter = regex;
				return this;
			}

			public Builder useBufferedImage(boolean flag) {
				resizeOptions.useBufferedImage = flag;
				return this;
			}

			public Builder withBufferedImageScalingMethod(org.imgscalr.Scalr.Method method) {
				resizeOptions.bufferedImageScalingMethod = method;
				return this;
			}

			public Builder withRetainGCTransparency(boolean flag) {
				resizeOptions.retainGCTransparency = flag;
				return this;
			}

			public Builder withUseBackgroundHint(boolean flag) {
				resizeOptions.useBackgroundHint = flag;
				return this;
			}

			public Builder withBackgroundHint(Color color) {
				resizeOptions.backgroundHint = color;
				return this;
			}

			public Builder withBufferedImage(org.imgscalr.Scalr.Method method) {
				resizeOptions.bufferedImageScalingMethod = method;
				return this;
			}

			public ResizeOptions build() {
				return resizeOptions;
			}

			public Builder initFromAttr(IWidget widget, String attributeName) {
				Map<String, Object> elements = (Map<String, Object>) widget.getFromTempCache("swtResizeOptions");
				Map<String, Object> options = null;

				if (elements != null) {
					options = (Map<String, Object>) elements.get(attributeName);
				}
				if (options != null) {
					for (String key : options.keySet()) {
						Object value = options.get(key);
						switch (key) {
						case "useBufferedImage":
							resizeOptions.useBufferedImage = value.equals("true");
							break;
						case "useBackgroundHint":
							resizeOptions.useBackgroundHint = value.equals("true");
							break;
						case "retainGCTransparency":
							resizeOptions.retainGCTransparency = value.equals("true");
							break;
						case "colorSmoothenGcFilter":
							resizeOptions.colorSmoothenGcFilter = (String) value;
							break;
						case "bufferedImageScalingMethod":
							resizeOptions.bufferedImageScalingMethod = Scalr.Method.valueOf((String) value);
							break;
						case "backgroundHint":
							if (widget != null) {
								resizeOptions.backgroundHint = (Color) widget.quickConvert(value, "color");
							}
							break;
						case "useParentBackground":
							resizeOptions.backgroundHint = ((Control) widget.asNativeWidget()).getParent()
									.getBackground();
							break;
						default:
							break;
						}
					}
				}
				return this;
			}
		}
	}

	public static Object getResizeOptionsAsMap(IWidget w, Object objValue) {
		Map<String, Object> elementsMap = com.ashera.model.ModelExpressionParser
				.parseSimpleCssExpression((String) objValue);
		for (String key : elementsMap.keySet()) {
			String expressionForResource = (String) w.quickConvert(elementsMap.get(key),
					CommonConverters.resourcestring);
			elementsMap.put(key,
					com.ashera.model.ModelExpressionParser.parseSimpleCssExpression(expressionForResource));
		}
		return elementsMap;
	}
}