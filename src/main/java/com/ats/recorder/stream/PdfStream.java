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

package com.ats.recorder.stream;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

import com.ats.executor.TestBound;
import com.ats.recorder.VisualAction;
import com.ats.script.ScriptHeader;
import com.ats.tools.ResourceContent;
import com.itextpdf.io.font.FontConstants;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.color.Color;
import com.itextpdf.kernel.color.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.Style;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

public class PdfStream {

	private Document pdfDoc;

	private Style titleStyle = new Style();
	private Style statusStyle = new Style();
	private Style warningStyle = new Style();

	private PdfFont helvetica;
	private PdfFont helveticaBold;
	private PdfFont helveticaItalic;
	private PdfFont helveticaBoldItalic;

	public PdfStream(Path videoFolderPath, ScriptHeader header) {

		File pdfFile = videoFolderPath.resolve(header.getQualifiedName() + ".pdf").toFile();

		PdfDocument pdfDocFile = null;
		try {
			pdfDocFile = new PdfDocument(new PdfWriter(pdfFile.getAbsolutePath()));
			pdfDoc = new Document(pdfDocFile, new PageSize(PageSize.A4));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {

			helvetica = PdfFontFactory.createFont(FontConstants.HELVETICA);
			helveticaBold = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLD);
			helveticaItalic = PdfFontFactory.createFont(FontConstants.HELVETICA_OBLIQUE);
			helveticaBoldItalic = PdfFontFactory.createFont(FontConstants.HELVETICA_BOLDOBLIQUE);

		} catch (IOException e) {
			e.printStackTrace();
		}

		titleStyle.setFont(helveticaBold).setFontSize(14);
		statusStyle.setFont(helveticaItalic).setFontSize(11);
		warningStyle.setFont(helveticaItalic).setFontSize(12).setFontColor(Color.ORANGE);


		Paragraph paragraphTitle = new Paragraph("Action Test Script - Visual Report");
		paragraphTitle.setWidthPercent(100).setTextAlignment(TextAlignment.CENTER);
		paragraphTitle.setFont(helveticaBold).setFontSize(24);

		//Paragraph paragraphElement = new Paragraph("Test unit : " + header.getQualifiedName());
		//paragraphElement.setWidthPercent(100).setTextAlignment(TextAlignment.CENTER);
		//paragraphElement.setFont(helveticaItalic).setFontSize(18);

		pdfDoc.add(paragraphTitle);
		//pdfDoc.add(paragraphElement);


		Table table = new Table(4);
		table.setWidthPercent(100);
		Cell cell;

		cell = new Cell(1, 4).add("Test case : " + header.getQualifiedName());
		cell.setFont(helveticaBold).setFontSize(18);
		cell.setTextAlignment(TextAlignment.CENTER);
		cell.setBackgroundColor(Color.convertRgbToCmyk(new DeviceRgb(175, 183, 188)));
		table.addCell(cell);

		cell = new Cell(1, 1).add("Author");
		cell.setFont(helveticaBoldItalic).setFontSize(11);
		table.addCell(cell);

		cell = new Cell(1, 3).add(header.getAuthor());
		cell.setFont(helvetica).setFontSize(14);
		table.addCell(cell);

		cell = new Cell(1, 1).add("Execution date");
		cell.setFont(helveticaBoldItalic).setFontSize(11);
		table.addCell(cell);

		cell = new Cell(1, 3).add("21/12/1862 at 10:56:00");
		cell.setFont(helvetica).setFontSize(14);
		table.addCell(cell);

		cell = new Cell(1, 1).add("Description");
		cell.setFont(helveticaBoldItalic).setFontSize(11);
		table.addCell(cell);

		cell = new Cell(1, 3).add(header.getDescription());
		cell.setFont(helvetica).setFontSize(14);
		table.addCell(cell);

		cell = new Cell(1, 1).add("Prerequisite");
		cell.setFont(helveticaBoldItalic).setFontSize(11);
		table.addCell(cell);

		cell = new Cell(1, 3).add(header.getPrerequisite());
		cell.setFont(helvetica).setFontSize(14);
		table.addCell(cell);

		cell = new Cell(1, 1).add("Groups");
		cell.setFont(helveticaBoldItalic).setFontSize(11);
		table.addCell(cell);

		cell = new Cell(1, 3).add(header.getGroups().toString());
		cell.setFont(helvetica).setFontSize(14);
		table.addCell(cell);


		pdfDoc.add(table);
		pdfDoc.add(new AreaBreak());

	}

	public Image getWatermarkedImage(PdfDocument pdfDocument, Image img, TestBound elementBound) {
		float width = img.getImageScaledWidth();
		float height = img.getImageScaledHeight();
		PdfFormXObject template = new PdfFormXObject(new Rectangle(width, height));
		new Canvas(template, pdfDocument).add(img);
		new PdfCanvas(template, pdfDocument)
		.saveState()
		.setStrokeColor(Color.CYAN)
		.setLineWidth(5)
		.moveTo(elementBound.getX().doubleValue(), elementBound.getY().doubleValue())
		.lineTo(elementBound.getX().doubleValue(), elementBound.getY().doubleValue() + elementBound.getHeight().doubleValue())
		.lineTo(elementBound.getX().doubleValue() + elementBound.getWidth().doubleValue(), elementBound.getY().doubleValue() + elementBound.getHeight().doubleValue())
		.lineTo(elementBound.getX().doubleValue() + elementBound.getWidth().doubleValue(), elementBound.getY().doubleValue())
		.lineTo(elementBound.getX().doubleValue(), elementBound.getY().doubleValue())
		.stroke()
		.restoreState();
		return new Image(template);
	}

	public void terminate() {
		pdfDoc.close();
		pdfDoc = null;

		titleStyle = null;
		statusStyle = null;
		warningStyle = null;

		helvetica = null;
		helveticaBold = null;
		helveticaItalic = null;
		helveticaBoldItalic = null;
	}

	private byte[] imgBytes;
	private ImageData imgData;
	private Image img;

	public void flush(VisualAction currentVisual) {

		Table table = new Table(new float[]{1, 2});
		table.setWidthPercent(100);

		Paragraph paragraphElement = new Paragraph();
		paragraphElement.setHeight(100);
		paragraphElement.setTextAlignment(TextAlignment.RIGHT);

		try {
			if(currentVisual.getImages() != null) {
				ArrayList<byte[]> images = currentVisual.getImages();

				imgBytes = images.get(images.size()-1);
				imgData = ImageDataFactory.create(imgBytes);
				img = new Image(imgData);

				if(currentVisual.getNumElements() > -1) {

					if(currentVisual.getNumElements() > 0) {
						img = getWatermarkedImage(pdfDoc.getPdfDocument(), img, currentVisual.getElementBound());
						paragraphElement.add("Element found in " + currentVisual.getTotalSearchDuration() + " ms");

						paragraphElement.setFont(helveticaItalic).setFontSize(12).setFontColor(Color.GRAY);

					}else {
						paragraphElement.addStyle(warningStyle);
						paragraphElement.add("Element not found after " + currentVisual.getTotalSearchDuration() + " ms !");
					}
				}else {

				}

				Cell cellImage = new Cell().add(img.scaleToFit(320, 240));
				cellImage.setMarginTop(40);

				table.addCell(cellImage);
			}
		}catch(OutOfMemoryError err) {
			System.err.println("out of memory");
		}

		Cell cellStatus = new Cell();
		Paragraph p1 = new Paragraph(currentVisual.getType());
		p1.addStyle(titleStyle);
		p1.setTextAlignment(TextAlignment.RIGHT);
		cellStatus.add(p1);


		String data = currentVisual.getValue() + currentVisual.getData();
		if(data.length() > 50) {
			data = data.substring(0, 50);
		}

		Paragraph p2 = new Paragraph(data);
		p2.setTextAlignment(TextAlignment.RIGHT);
		cellStatus.add(p2);

		Paragraph p3 = new Paragraph();
		p3.setHeight(12);

		cellStatus.add(paragraphElement);

		Paragraph p4 = new Paragraph();
		p4.addStyle(statusStyle);
		p4.setTextAlignment(TextAlignment.RIGHT);

		Image icon = new Image(ImageDataFactory.create(ResourceContent.getTick24Icon()));
		p4.add(icon);
		p4.add("(passed)");
		p4.setVerticalAlignment(VerticalAlignment.MIDDLE);

		cellStatus.add(p4);

		cellStatus.setMarginTop(40);
		cellStatus.setVerticalAlignment(VerticalAlignment.BOTTOM);
		//cellStatus.setBorder(Border.NO_BORDER);

		//table.addCell(img.setAutoScale(true));

		table.addCell(cellStatus);

		pdfDoc.add(table);

	}
}