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

package com.ats.recorder;

import com.ats.executor.TestBound;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

public class VisualImage {

	private byte[] data;
	private String type;

	private boolean drawBound = false;
	private int x;
	private int y;
	private int w;
	private int h;

	private File file;

	public VisualImage(Path folder, String name, String type, byte[] data) {
		this.type = type;
		this.data = data;
		this.file = folder.resolve(name).toFile();
	}

	public VisualImage(Path folder, String name, String type, byte[] data, TestBound bound) {
		this(folder, name, type, data);
		this.drawBound = true;
		this.x = bound.getX().intValue() -6;
		this.y = bound.getY().intValue() -7;
		this.w = bound.getWidth().intValue();
		this.h = bound.getHeight().intValue();
	}

	public void save() {
		if(data != null && data.length > 0) {
			try {
				if(drawBound) {

					final BufferedImage buffImage = ImageIO.read(new ByteArrayInputStream(data));
					final Graphics2D g2d = buffImage.createGraphics();

					g2d.setColor(Color.MAGENTA);
					g2d.setStroke(new BasicStroke(3));
					g2d.drawRect(x, y, w, h);
					g2d.dispose();

					ImageIO.write(buffImage, type, file);

				}else {
					FileOutputStream fos = new FileOutputStream(file);
					fos.write(data);
					fos.close();
				}

			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}
}