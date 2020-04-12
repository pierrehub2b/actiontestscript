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

package com.ats.generator.variables.parameter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

public class ParameterDataFile{

	public static final String CSV_TYPE = "csv";
	public static final String JSON_TYPE = "json";
	public static final String JSON_COMPLEX_TYPE = "jsonComplex";

	private String dataType = CSV_TYPE;
	private ArrayList<ArrayList<ArrayList<String>>> data = new ArrayList<ArrayList<ArrayList<String>>>();
	private ArrayList<String> colsName = new ArrayList<String>();

	private String error = "";
	private boolean editable = true;

	public ParameterDataFile() {}

	public ParameterDataFile(URL url) {

		this.editable = !url.getProtocol().startsWith("http");
		
		String content = null;
		try {
			final InputStreamReader stream = new InputStreamReader(url.openStream(), StandardCharsets.UTF_8);
			content = CharStreams.toString(stream);
			Closeables.closeQuietly(stream);
		} catch (IOException e1) {
			error = e1.getMessage();
			return;
		}

		final JsonElement jsonElement = getJsonElement(content);
		if(jsonElement != null) {
			if(jsonElement.isJsonArray()) {
				this.dataType = JSON_COMPLEX_TYPE;

				final JsonArray jsonArray = jsonElement.getAsJsonArray();

				for (JsonElement line : jsonArray) {

					if(line.isJsonObject()) {
						final ArrayList<ArrayList<String>> newLine = new ArrayList<ArrayList<String>>();
						line.getAsJsonObject().entrySet().forEach(e -> newLine.add(new ArrayList<String>(Arrays.asList(e.getKey(), e.getValue().getAsString()))));
						data.add(newLine);
					}
				}

			}else if(jsonElement.isJsonObject()) {

				this.dataType = JSON_TYPE;

				final JsonObject jsonObject = jsonElement.getAsJsonObject();
				final AtomicInteger colIndex = new AtomicInteger(0);

				jsonObject.keySet().forEach(c -> addLine(data, colsName, jsonObject, c, colIndex.getAndIncrement()));
			}

		}else {
			final CSVReader reader = new CSVReader(new StringReader(content));
			try {
				final List<String[]> csvList = reader.readAll();
				reader.close();
				csvList.forEach(l -> addCsvLine(data, l));

				if(data.size() > 0) {
					for (ArrayList<String> cols : data.get(0)) {
						colsName.add(cols.get(0));
					}
				}

			} catch (IOException | CsvException e) {
				error = e.getMessage();
			}
		}
	}

	public boolean noError() {
		return error == null || error.isEmpty();
	}

	private static void addLine(ArrayList<ArrayList<ArrayList<String>>> list, ArrayList<String> colsName, JsonObject obj, String colName, int colIndex) {

		colsName.add(colName);

		final JsonArray data = obj.get(colName).getAsJsonArray();
		final int dataSize = data.size();
		final AtomicInteger line = new AtomicInteger(0);

		data.forEach(e -> addLine(list, e, line.getAndIncrement(), dataSize, colName, colIndex));
	}

	private static void addLine(ArrayList<ArrayList<ArrayList<String>>> list, JsonElement elem, int line, int dataSize, String colName, int colIndex) {
		if(elem != null && elem.isJsonPrimitive()) {
			if(list.size() < line + 1){
				list.add(new ArrayList<ArrayList<String>>());
			}
			final ArrayList<ArrayList<String>> currentLine = list.get(line);

			if(currentLine.size() < colIndex + 1) {
				currentLine.add(new ArrayList<String>());
			}
			final ArrayList<String> currentCol = currentLine.get(colIndex);

			currentCol.add(colName);
			currentCol.add(elem.getAsString());
		}
	}

	private static void addCsvLine(ArrayList<ArrayList<ArrayList<String>>> result, String[] line) {
		final AtomicInteger col = new AtomicInteger(0);
		result.add(new ArrayList<ArrayList<String>>(Arrays.stream(line).map(l -> new ArrayList<String>(Arrays.asList("p" + col.getAndIncrement(), l))).collect(Collectors.toList())));
	}

	private static JsonElement getJsonElement(String content) {
		try {
			return JsonParser.parseString(content);
		} catch(com.google.gson.JsonSyntaxException ex) { 
			return null;
		}
	}

	public int size() {
		return data.size();
	}

	//-------------------------------------------------------------------------------------------------------

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String type) {
		this.dataType = type;
	}

	public ArrayList<ArrayList<ArrayList<String>>> getData() {
		return data;
	}

	public void setData(ArrayList<ArrayList<ArrayList<String>>> list) {
		this.data = list;
	}

	public ArrayList<String> getColsName() {
		return colsName;
	}

	public void setColsName(ArrayList<String> colsName) {
		this.colsName = colsName;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isEditable() {
		return editable;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}
}