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

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.IntStream;

import javax.imageio.ImageIO;

public class TemplateMatchingSimple {

	private int targetWidth = 100;
	private int targetHeight = 100;

	private int[][] target;
	private int maxPixelsError = 0;
	private int maxPixelsDiff = 10;

	public TemplateMatchingSimple(int[][] image) {
		this.target = image;
		if(image != null) {
			this.targetWidth = image.length;
			this.targetHeight = image[0].length;
			this.setPercentError(0.3);
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
		final int[][] burned = new int[mainImage.getWidth()][ mainImage.getHeight()];

		if(mainVector != null && subVector != null) {

			final int xOffsetMax = mainImage.getWidth() - subWidth;
			final int yOffsetMax = mainImage.getHeight() - subHeight;

			Rectangle found = findSubImage(mainVector, burned, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);
			while(found != null) {
				result.add(found);
				found = findSubImage(mainVector, burned, xOffsetMax, yOffsetMax, subVector, subWidth, subHeight, maxError, maxDiff);
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
						
						IntStream.range(x0, x0 + subWidth).parallel().forEach(x1 -> IntStream.range(y0, y0 + subHeight).parallel().forEach(y1 -> burned[x1][y1] = 1));
												
						/*for(int x0 = 0; x0 < subWidth; x0++) {
							for(int y0 = 0; y0 < subHeight; y0++) {
								burned[x0+x][y0+y] = 1;
							}
						}*/
						
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

	private static int pixelGrayed(final int rgb) {
		final int r = (rgb >> 16) & 0xff;
		final int g = (rgb >>  8) & 0xff;
		final int b =  rgb        & 0xff;

		//return (r+g+b)/3;
		
		return (int) ((0.299*r) + (0.587*g) + (0.114*b));
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