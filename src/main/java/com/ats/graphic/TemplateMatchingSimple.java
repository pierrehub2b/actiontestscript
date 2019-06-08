package com.ats.graphic;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.imageio.ImageIO;

public class TemplateMatchingSimple {

	private int targetWidth = 100;
	private int targetHeight = 100;

	private int[][] target;
	private int maxPixelsError = 0;
	private int maxPixelsDiff = 10;

	public TemplateMatchingSimple(final int[][] image) {
		this.target = image;
		if(image != null) {
			this.targetWidth = image.length;
			this.targetHeight = image[0].length;
			this.setPercentError(0.5);
		}
	}

	public TemplateMatchingSimple(final BufferedImage image) {
		this(getVector(image));
	}

	public TemplateMatchingSimple(final byte[] image) {
		this(getVector(getBufferedImage(image)));
	}

	public void setError(int value) {
		this.maxPixelsError = value;
	}

	public void setPercentError(double value) {
		this.maxPixelsError = getMaxError(targetWidth, targetWidth, value);
	}

	public ArrayList<Rectangle> findOccurrences(final byte[] imageData) {
		final BufferedImage image = getBufferedImage(imageData);
		if(image != null) {
			return findOccurrences(image); 
		}
		return new ArrayList<Rectangle>();
	}

	public ArrayList<Rectangle> findOccurrences(final BufferedImage mainImage) {
		return getLocations(mainImage, target, targetWidth, targetHeight, maxPixelsError, maxPixelsDiff);
	}

	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------

	private static int getMaxError(int width, int height, double percent) {
		return (int) (width * height * percent / 100);
	}

	private static BufferedImage getBufferedImage(final byte[] image) {
		final InputStream in = new ByteArrayInputStream(image);
		try {
			return ImageIO.read(in);
		} catch (IOException e) {
			return null;
		}
	}

	public static ArrayList<Rectangle> getLocations(final byte[] mainImageInBytes, final BufferedImage subImage, int maxError, int maxDiff) {
		final InputStream in = new ByteArrayInputStream(mainImageInBytes);
		try {
			final BufferedImage mainImage = ImageIO.read(in);
			return getLocations(mainImage, getVector(subImage), subImage.getWidth(), subImage.getHeight(), maxError, maxDiff);
		} catch (IOException e) {
			return new ArrayList<Rectangle>();
		}
	}

	public static ArrayList<Rectangle> getLocations(final BufferedImage mainImage, final BufferedImage subImage, int maxError, int maxDiff) {
		return getLocations(mainImage, getVector(subImage), subImage.getWidth(), subImage.getHeight(), maxError, maxDiff);
	}

	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------

	private static ArrayList<Rectangle> getLocations(final BufferedImage mainImage, int[][] subVector, int subWidth, int subHeight, int maxError, int maxDiff) {

		final ArrayList<Rectangle> result = new ArrayList<Rectangle>();
		final int[][] mainVector = getVector(mainImage);

		if(mainVector != null && subVector != null) {
			final int xOffsetMax = mainImage.getWidth() - subWidth;
			final int yOffsetMax = mainImage.getHeight() - subHeight;

			final int pixels[] = new int[subWidth * subHeight * 4];
			final WritableRaster raster = mainImage.getRaster();

			Rectangle found = findSubImage(mainVector, raster, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);

			while(found != null) {
				result.add(found);
				raster.setPixels(found.x, found.y, subWidth, subHeight, pixels);
				found = findSubImage(mainVector, raster, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);
			}
		}

		return result;
	}

	private static int[][] getVector(BufferedImage img) {
		if(img != null) {
			final int[][] data = new int[img.getWidth()][img.getHeight()];
			for(int x = 0; x < img.getWidth(); x++){
				for(int y = 0; y < img.getHeight(); y++){
					data[x][y] = pixelGrayed(img.getRGB(x, y));
					//data[x][y] = img.getRGB(x, y);
				}
			}
			return data;
		}
		return null;
	}

	private static Rectangle findSubImage(final int[][] mainVector, final WritableRaster raster, final int xOffsetMax, final int yOffsetMax, final int[][] subImage, final int subWidth, final int subHeight, int maxError, int maxDiff) {

		int[] pix = new int[4];

		for (int x = 0; x < xOffsetMax; x++){
			for (int y = 0; y < yOffsetMax; y++){
				raster.getPixel(x, y, pix);
				if(pix[3] != 0) {
					if (subImageIsAtOffset(subImage, mainVector, x, y, subWidth, subHeight, maxError, maxDiff)) {
						return new Rectangle(x,y,subWidth,subHeight);
					}
				}
			}
		}

		return null;
	}

	private static boolean subImageIsAtOffset(final int[][] subImage, final int[][] image, final int xOffset, final int yOffset, final int width, final int height, int maxError, int maxDiff) {

		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				if (Math.abs(subImage[x][y] - image[xOffset + x][yOffset + y]) > maxDiff) {
					if(maxError > 0) {
						maxError--;
					}else {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static int pixelGrayed(int rgb) {
		final int r = (rgb >> 16) & 0xff;
		final int g = (rgb >>  8) & 0xff;
		final int b =  rgb        & 0xff;

		return (r+g+b)/3;
	}

	/*private static int pixelDiff(int rgb1, int rgb2) {

		int r1 = (rgb1 >> 16) & 0xff;
		int g1 = (rgb1 >>  8) & 0xff;

		int b1 =  rgb1        & 0xff;
		int r2 = (rgb2 >> 16) & 0xff;
		int g2 = (rgb2 >>  8) & 0xff;
		int b2 =  rgb2        & 0xff;

		return Math.abs(r1 - r2 + g1 - g2 + b1 - b2);
		//return Math.abs(r1 - r2) + Math.abs(g1 - g2) + Math.abs(b1 - b2);
	}*/
}