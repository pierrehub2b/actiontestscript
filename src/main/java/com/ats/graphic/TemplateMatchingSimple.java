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

	public TemplateMatchingSimple(final int[][] image) {
		this.target = image;
		this.targetWidth = image.length;
		this.targetHeight = image[0].length;
	}

	public TemplateMatchingSimple(final BufferedImage image) {
		this(getVector(image));
	}
	
	public TemplateMatchingSimple(final byte[] image) {
		this(getVector(getBufferedImage(image)));
	}
	
	public void setErrorMax(int value) {
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
		return getLocations(mainImage, target, targetWidth, targetHeight, maxPixelsError);
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
	
	public static ArrayList<Rectangle> getLocations(final byte[] mainImageInBytes, final BufferedImage subImage, int maxError) {
		final InputStream in = new ByteArrayInputStream(mainImageInBytes);
		try {
			final BufferedImage mainImage = ImageIO.read(in);
			return getLocations(mainImage, getVector(subImage), subImage.getWidth(), subImage.getHeight(), maxError);
		} catch (IOException e) {
			return new ArrayList<Rectangle>();
		}
	}

	public static ArrayList<Rectangle> getLocations(final BufferedImage mainImage, final BufferedImage subImage, int maxError) {
		return getLocations(mainImage, getVector(subImage), subImage.getWidth(), subImage.getHeight(), maxError);
	}

	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------

	private static ArrayList<Rectangle> getLocations(final BufferedImage mainImage, int[][] subVector, int subWidth, int subHeight, int maxError) {

		final int xOffsetMax = mainImage.getWidth() - subWidth;
		final int yOffsetMax = mainImage.getHeight() - subHeight;

		final int pixels[] = new int[subWidth * subHeight * 4];
		final WritableRaster raster = mainImage.getRaster();

		final int[][] mainVector = getVector(mainImage);

		final ArrayList<Rectangle> result = new ArrayList<Rectangle>();
		
		Rectangle found = findSubImage(mainVector, raster, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError);

		while(found != null) {
			result.add(found);
			raster.setPixels(found.x, found.y, subWidth, subHeight, pixels);
			found = findSubImage(mainVector, raster, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError);
		}

		return result;
	}

	private static int[][] getVector(BufferedImage img) {
		int[][] data = new int[img.getWidth()][img.getHeight()];
		for(int x = 0; x < img.getWidth(); x++){
			for(int y = 0; y < img.getHeight(); y++){
				data[x][y] = img.getRGB(x, y);
			}
		}
		return data;
	}

	private static Rectangle findSubImage(final int[][] mainVector, final WritableRaster raster, final int xOffsetMax, final int yOffsetMax, final int[][] subImage, final int subWidth, final int subHeight, int maxError) {

		int[] pix = new int[4];

		for (int x = 0; x < xOffsetMax; x++){
			for (int y = 0; y < yOffsetMax; y++){
				raster.getPixel(x, y, pix);
				if(pix[3] != 0) {
					if (subImageIsAtOffset(subImage, mainVector, x, y, subWidth, subHeight, maxError)) {
						return new Rectangle(x,y,subWidth,subHeight);
					}
				}
			}
		}

		return null;
	}

	private static boolean subImageIsAtOffset(final int[][] subImage, final int[][] image, final int xOffset, final int yOffset, final int width, final int height, int maxError) {
		for (int x = 0; x < width; x++){
			for (int y = 0; y < height; y++){
				if (subImage[x][y] != image[xOffset + x][yOffset + y]) {
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
}