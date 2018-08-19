package com.ats.recorder;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import com.ats.executor.TestBound;

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