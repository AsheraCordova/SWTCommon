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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.imgscalr.Scalr;

import com.ashera.converter.CommonConverters;
import com.ashera.model.RectM;
import com.ashera.widget.IWidget;

public class ImageUtils {
	private static final Pattern COLOR_SMOOTHEN_REGEX = Pattern.compile(
            "r\\s*(>|<)\\s*([0-9]*)\\s*&&\\s*g\\s*(>|<)\\s*([0-9]*)\\s*&&\\s*b\\s*(>|<)\\s*([0-9]*)\\s*");
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


	private static Image retainTransparency(Image scaled, ResizeOptions resizeOptions) {
		ImageData imageData = scaled.getImageData();
		imageData.transparentPixel = imageData.getPixel(0, 0);

		if (resizeOptions.getColorSmoothenGcFilter() != null) {
			List<String> groups = evalRegEx(resizeOptions.getColorSmoothenGcFilter(), COLOR_SMOOTHEN_REGEX, "Invalid expression. e.g. r > 100 && g > 100 && b > 100");
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
					if (checkThreshold(groups, 0, red) && checkThreshold(groups, 2, blue) && checkThreshold(groups, 4, red)) {
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
		return (groups.get(index).equals("<") && color < Integer.parseInt(groups.get(index + 1))) || 
				(groups.get(index).equals(">") && color > Integer.parseInt(groups.get(index + 1)));
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
	
	private static Image scaleImageUsingBufferedImage(Image image, int width, int height, RectM bounds, RectM clip, ResizeOptions resizeOptions) throws IOException {
		org.eclipse.swt.graphics.ImageLoader imageLoader = new org.eclipse.swt.graphics.ImageLoader();
		imageLoader.data = new ImageData[] { image.getImageData() };
		java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
		imageLoader.save(bos, SWT.IMAGE_PNG);

		java.io.ByteArrayInputStream bis = new java.io.ByteArrayInputStream(bos.toByteArray());
		BufferedImage bufferedImage = ImageIO.read(bis);
		bufferedImage = Scalr.resize(bufferedImage, resizeOptions.getBufferedImageScalingMethod(), Scalr.Mode.FIT_EXACT, bounds.width,
				bounds.height);

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

	private static Image scaleImageUsingGC(Image image, int width, int height, RectM bounds, RectM clip, ResizeOptions resizeOptions) {
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

		gc.drawImage(image, 0, 0, image.getBounds().width, image.getBounds().height, bounds.x, bounds.y, bounds.width, bounds.height);

		if (resizeOptions.isRetainGCTransparency() && image.getImageData().alphaData != null) {
			image = retainTransparency(scaled, resizeOptions);
			scaled.dispose();
		} else {
			image = scaled;
		}
		gc.dispose();
		return image;
	}

	protected static BufferedImage scaleImage(BufferedImage src, int width, int height, RectM bounds, RectM clip, ResizeOptions resizeOptions) {
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
			resultGraphics.setColor(new java.awt.Color(backgroundHint.getRed(), backgroundHint.getBlue(), backgroundHint.getGreen()));
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
		
		if (clip == null && bounds.x == 0 && bounds.y == 0 && imageData.width == bounds.width && imageData.height == bounds.height) {
			// no need of resize in this case
			//return image;
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
								resizeOptions.backgroundHint =  (Color) widget.quickConvert(value, "color");
							}
							break;
						case "useParentBackground":
							resizeOptions.backgroundHint = ((Control) widget.asNativeWidget()).getParent().getBackground();
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
		Map<String, Object> elementsMap = com.ashera.model.ModelExpressionParser.parseSimpleCssExpression((String) objValue);
		for (String key : elementsMap.keySet()) {
			String expressionForResource = (String) w.quickConvert(elementsMap.get(key), CommonConverters.resourcestring);
			elementsMap.put(key, com.ashera.model.ModelExpressionParser.parseSimpleCssExpression(expressionForResource));
		}		
		return elementsMap;
	}
}