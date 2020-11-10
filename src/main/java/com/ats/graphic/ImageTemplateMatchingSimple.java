/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
 */

package com.ats.graphic;

import com.ats.driver.AtsManager;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

public class ImageTemplateMatchingSimple {

	private final static double[] GRAYSCALE = new double[] {0.2126, 0.7152, 0.0722};
	private final static double PERCENT_DEFAULT = 0.3;
	private final static int MAX_PIXELS_DIFF = 10;
	
	private int targetWidth = 100;
	private int targetHeight = 100;

	private int[][] target;
	private int maxPixelsError = 0;
	
	public ImageTemplateMatchingSimple(int[][] image) {
		this.target = image;
		if(image != null) {
			this.targetWidth = image.length;
			this.targetHeight = image[0].length;
			this.setPercentError(PERCENT_DEFAULT);
		}
	}

	public ImageTemplateMatchingSimple(final BufferedImage image) {
		this(getVector(image));
	}

	public ImageTemplateMatchingSimple(final byte[] image) {
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
		return getLocations(mainImage, target, targetWidth, targetHeight, maxPixelsError, MAX_PIXELS_DIFF);
	}

	//------------------------------------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------------------------------------

	private static int getMaxError(int width, int height, double percent) {
		return (int) (width * height * percent / 100);
	}

	private static BufferedImage getBufferedImage(final byte[] image) {
		BufferedImage result = null;
		final InputStream in = new ByteArrayInputStream(image);
		try {
			result = ImageIO.read(in);
			in.close();
		} catch (IOException e) {}

		return result;
	}

	public static ArrayList<Rectangle> getLocations(final byte[] mainImageInBytes, final BufferedImage subImage, int maxError, int maxDiff) {
		final InputStream in = new ByteArrayInputStream(mainImageInBytes);
		try {
			return getLocations(ImageIO.read(in), getVector(subImage), subImage.getWidth(), subImage.getHeight(), maxError, maxDiff);
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
		final int[][] burnedPixels = new int[mainImage.getWidth()][ mainImage.getHeight()];

		final int maxTryImageRecognition = AtsManager.getInstance().getMaxTryImageRecognition();

		if(mainVector != null && subVector != null) {

			final int xOffsetMax = mainImage.getWidth() - subWidth;
			final int yOffsetMax = mainImage.getHeight() - subHeight;

			Rectangle found = findSubImage(mainVector, burnedPixels, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);
			while(found != null && result.size() < maxTryImageRecognition) {
				result.add(found);
				found = findSubImage(mainVector, burnedPixels, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);
			}
		}

		return result;
	}

	private static int[][] getVector(BufferedImage img) {
		if(img != null) {
			final int[][] data = new int[img.getWidth()][img.getHeight()];
			IntStream.range(0, data.length).parallel().forEach(x -> Arrays.parallelSetAll(data[x], y -> pixelGrayed(img.getRGB(x, y))));
			return data;
		}
		return null;
	}

	private static Rectangle findSubImage(int[][] mainVector, int[][] burned, int xOffsetMax, int yOffsetMax, int[][] subImage, int subWidth, int subHeight, int maxError, int maxDiff) {
		for (int x = 0; x < xOffsetMax; x++){
			for (int y = 0; y < yOffsetMax; y++){
				if(burned[x][y] == 0) {
					if (subImageIsAtOffset(subImage, mainVector, x, y, subWidth, subHeight, maxError, maxDiff)) {

						final int x0 = x;
						final int y0 = y;
						final int w0 = x0 + subWidth;
						final int h0 = y0 + subHeight;
						
						IntStream.range(x0, w0).parallel().forEach(x1 -> IntStream.range(y0, h0).parallel().forEach(y1 -> burned[x1][y1] = 1));
						
						return new Rectangle(x, y, subWidth, subHeight);
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
					if(maxError <= 0) {
						return false;
					}
					maxError--;
				}
			}
		}
		return true;
	}

	private static int pixelGrayed(int rgb) {
		return (int) (GRAYSCALE[0]*((rgb >> 16)& 0xff) + GRAYSCALE[1]*((rgb >>  8)& 0xff) + GRAYSCALE[2]*(rgb& 0xff));
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