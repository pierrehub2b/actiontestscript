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

import com.google.common.io.CharStreams;
import com.google.common.io.Closeables;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

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

public class ParameterDataFile{

	public static final String CSV_TYPE = "csv";
	public static final String JSON_TYPE = "json";
	public static final String JSON_SIMPLE_TYPE = "jsonSimple";
	public static final String JSON_COMPLEX_TYPE = "jsonComplex";

	private String dataType = CSV_TYPE;
	private ArrayList<ParameterList> data = new ArrayList<ParameterList>();

	private String error = "";
	private boolean editable = true;
	private int maxCols = 0;

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

		if(url.getPath().endsWith(".csv")) {
			readCsvData(content);
		}else {

			final JsonElement jsonElement = getJsonElement(content);
			if(jsonElement != null) {
				if(jsonElement.isJsonArray()) {
					this.dataType = JSON_COMPLEX_TYPE;

					final JsonArray jsonArray = jsonElement.getAsJsonArray();

					int iteration = 0;
					for (JsonElement line : jsonArray) {

						if(line.isJsonObject()) {
							final ParameterList newLine = new ParameterList(iteration);
							final AtomicInteger colIndex = new AtomicInteger(0);
							line.getAsJsonObject().entrySet().forEach(e -> newLine.addParameter(new Parameter(colIndex.getAndIncrement(), e.getKey(), e.getValue().getAsString())));
							data.add(newLine);
						}
						iteration++;
					}

				}else if(jsonElement.isJsonObject()) {

					this.dataType = JSON_TYPE;

					final JsonObject jsonObject = jsonElement.getAsJsonObject();

					if(jsonObject.has("paramNames") && jsonObject.has("paramValues") && jsonObject.size() == 2) {

						try {
							final JsonArray paramNames = jsonObject.get("paramNames").getAsJsonArray();
							final JsonArray paramValues = jsonObject.get("paramValues").getAsJsonArray();

							if(paramNames.size() == paramValues.size()) {
								for (int i = 0; i<paramValues.size(); i++){
									final JsonArray iterations = paramValues.get(i).getAsJsonArray();

									for (int j = 0; j<iterations.size(); j++){

										String paramName = "";
										if(i < paramNames.size()) {
											paramName = paramNames.get(i).getAsString();
										}

										if(data.size() < j+1) {
											data.add(new ParameterList(j));
										}
										final ParameterList row = data.get(j);
										row.addParameter(new Parameter(j, paramName, iterations.get(j).getAsString()));
									}
								}

								return;
							}
						}catch(IllegalStateException e) {}
					}

					this.dataType = JSON_SIMPLE_TYPE;
					parseJsonObject(jsonObject);
				}

			}else {
				readCsvData(content);
			}
		}
	}

	private void readCsvData(String content) {
		final CSVReader reader = new CSVReader(new StringReader(content));
		try {
			final List<String[]> csvList = reader.readAll();
			reader.close();

			setMaxCols(data.size());
			csvList.forEach(l -> addCsvLine(data, l));
		} catch (IOException | CsvException e) {
			error = e.getMessage();
		}
	}

	private void parseJsonObject(JsonObject jsonObject) {
		final AtomicInteger colIndex = new AtomicInteger(0);
		jsonObject.keySet().forEach(c -> addCol(data, jsonObject, c, colIndex.getAndIncrement()));
	}

	public boolean noError() {
		return error == null || error.isEmpty();
	}

	private static void addCol(ArrayList<ParameterList> list, JsonObject obj, String colName, int colIndex) {

		final JsonArray data = obj.get(colName).getAsJsonArray();
		final AtomicInteger line = new AtomicInteger(0);

		data.forEach(e -> addLine(list, e, line.getAndIncrement(), data, colName, colIndex));
	}

	private static void addLine(ArrayList<ParameterList> list, JsonElement elem, int line, JsonArray data, String colName, int colIndex) {
		if(elem != null && elem.isJsonPrimitive()) {
			if(list.size() < line + 1){
				list.add(new ParameterList(line));
			}
			final ParameterList currentLine = list.get(line);
			currentLine.addParameter(new Parameter(colIndex, colName, data.get(line).getAsString()));
		}
	}

	private static void addCsvLine(ArrayList<ParameterList> result, String[] line) {
		final AtomicInteger col = new AtomicInteger(0);
		result.add(new ParameterList(result.size(), Arrays.stream(line).map(l -> new Parameter(col.getAndIncrement(), l)).collect(Collectors.toList())));
	}

	private static JsonElement getJsonElement(String content) {
		try {
			return JsonParser.parseString(content);
		} catch(com.google.gson.JsonSyntaxException ex) { 
			return null;
		}
	}

	public int getSize() {
		return data.size();
	}

	public ParameterList getData(int index) {
		if(getSize() > index) {
			return data.get(index);
		}else if(getSize() > 0) {
			return data.get(0);
		}
		return null;
	}

	//--------------------------------------------------------
	// getters and setters for serialization
	//--------------------------------------------------------

	public String getDataType() {
		return dataType;
	}

	public void setDataType(String type) {
		this.dataType = type;
	}

	public ArrayList<ParameterList> getData() {
		return data;
	}

	public void setData(ArrayList<ParameterList> list) {
		this.data = list;
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

	public int getMaxCols() {
		return maxCols;
	}

	public void setMaxCols(int maxCols) {
		if(maxCols > this.maxCols) {
			this.maxCols = maxCols;
		}
	}
}