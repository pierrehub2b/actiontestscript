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

package com.ats.tools.performance.external;

import com.ats.executor.ActionStatus;
import com.ats.executor.channels.Channel;
import com.ats.script.actions.performance.octoperf.ActionOctoperfVirtualUser;
import com.ats.tools.logger.MessageCode;
import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.*;
import okhttp3.*;
import okhttp3.Request.Builder;
import okhttp3.internal.Util;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.zip.GZIPOutputStream;

public class OctoperfApi {

	private final static int BUFFER = 2048;

	private final static String WORKSPACES_API = "workspaces";
	private final static String MEMBER_OF_API = WORKSPACES_API + "/member-of";

	private final static String USERS_CURRENT_API = "users/current";

	private final static String DESIGN_API = "design";
	private final static String VIRTUAL_USER_API = DESIGN_API + "/virtual-users";
	private final static String PROJECT_API = DESIGN_API + "/projects";

	private final static String VIRTUAL_USER_PROJECT_API = VIRTUAL_USER_API + "/by-project/";
	private final static String PROJECT_WORKSPACE_API = PROJECT_API + "/by-workspace/";
	private final static String IMPORT_HAR_API = DESIGN_API + "/imports/har/";

	private final String MIME_TYPE = "multipart/form-data";
	private final String JSON_CONTENT_TYPE = "application/json";

	private String host;
	private String apiKey;

	private String userId;
	private String workspaceId;
	private String projectId;

	private boolean serverEnabled = false;

	public OctoperfApi(String host, String apiKey, String workspaceName, String projectName) {

		if(!host.endsWith("/")) {
			host = host + "/";
		}

		if(workspaceName == null) {
			workspaceName = "AtsWorkspace";
		}

		if(projectName == null) {
			projectName = "AtsCaptureProject";
		}

		this.host = host;
		this.apiKey = apiKey;

		checkProjectData(workspaceName, projectName);
	}

	private void checkProjectData(String workspaceName, String projectName) {
		userId = getCurrentUserId();
		if(userId != null) {
			if(checkWorkspaceExists(workspaceName)) {
				serverEnabled = checkProjectExists(projectName);
			}
		}
	}

	public void sendHarFileToUser(Channel channel, ActionOctoperfVirtualUser action, Path currentFile) {

		if(serverEnabled) {
			File gzipFile = null;
			Path tempFolder = null;

			try {
				tempFolder = Files.createTempDirectory("ats-octoperf_");
				tempFolder.toFile().mkdirs();

				gzipFile = createGZipFile(tempFolder, currentFile.toFile());
			} catch (IOException e) {
				action.getStatus().setError(ActionStatus.OCTOPERF_FILE_ERROR, e.getMessage());
			}

			if(gzipFile != null) {

				final String[] tags = action.getTags().split(",");
				final JsonArray tagsArray = new JsonArray();
				for(String tag : tags) {
					tagsArray.add(tag.trim());
				}

				final String virtualUserId = getVirtualUser(action.getUser().getCalculated(), action.getComment().getCalculated(), tagsArray, action.isAppend());
				if(virtualUserId != null) {
					executeRequest(
							getMultiPartRequest(
									channel,
									gzipFile,
									IMPORT_HAR_API, 
									projectId,
									"?containers=PAGE_REF&resources=KEEP_ALL&virtualUserId=",
									virtualUserId
									));
				}

				gzipFile.delete();
			}

			if(tempFolder != null) {
				tempFolder.toFile().delete();
			}
		}
	}

	private String getCurrentUserId() {
		final JsonElement response = executeRequest(getGetRequest(USERS_CURRENT_API));
		if (response != null && response.isJsonObject()) {
			if(response.getAsJsonObject().has("id")) {
				final String id = response.getAsJsonObject().get("id").getAsString();
				if(id != null && id.length() > 0) {
					return id;
				}
			}
		}
		return null; // user doesn't exists
	}

	//----------------------------------------------------------------------------------------------------------
	// Virtual User
	//----------------------------------------------------------------------------------------------------------

	private String getVirtualUser(String name, String comment, JsonArray tags, boolean append) {
		final JsonElement response = executeRequest(getGetRequest(VIRTUAL_USER_PROJECT_API, projectId));
		if(response != null && response.isJsonArray()) {
			final JsonArray virtualUsers = response.getAsJsonArray();
			for (int i=0; i< virtualUsers.size(); i++) {
				final JsonElement virtualUser = virtualUsers.get(i);
				if(virtualUser.isJsonObject() 
						&& virtualUser.getAsJsonObject().has("name") 
						&& virtualUser.getAsJsonObject().get("name").getAsString().equals(name)) {

					final JsonObject virtualUserObject = virtualUser.getAsJsonObject();
					final String virtualUserId = virtualUser.getAsJsonObject().get("id").getAsString();

					virtualUserObject.remove("tags");
					virtualUserObject.add("tags", tags);

					virtualUserObject.remove("description");
					virtualUserObject.addProperty("description", comment);

					if(!append) {
						virtualUserObject.remove("children");
						virtualUserObject.add("children", new JsonArray());
					}

					executeRequest(
							getPutRequest(
									virtualUser,
									VIRTUAL_USER_API,
									"/",
									virtualUserId
									));

					return virtualUserId;
				}
			}
		}
		return createVirtualUser(name, comment, tags);
	}

	private String createVirtualUser(String name, String comment, JsonArray tags) {

		final JsonObject virtualUser = createJsonDataWithTag(userId, name, comment, "JMETER", tags);
		virtualUser.addProperty("projectId", projectId);
		virtualUser.addProperty("@type", "VirtualUser");
		virtualUser.add("children", new JsonArray());

		final JsonElement response = executeRequest(getPostRequest(virtualUser, VIRTUAL_USER_API));
		if(response != null && response.isJsonObject()) {
			final JsonObject createdUser = response.getAsJsonObject();
			if(createdUser.has("id")) {
				return createdUser.get("id").getAsString();
			}
		}
		return null;
	}

	//----------------------------------------------------------------------------------------------------------
	// Workspace data
	//----------------------------------------------------------------------------------------------------------

	private boolean createWorkspace(String name) {

		final JsonObject newWorkspace = createJsonData(userId, name, "New Workspace created by ATS framework");

		final JsonElement createWorskpace = executeRequest(getPostRequest(newWorkspace, WORKSPACES_API));
		if(createWorskpace != null && createWorskpace.isJsonObject()) {
			final JsonObject obj = createWorskpace.getAsJsonObject();
			if(obj.has("id")) {
				workspaceId = obj.get("id").getAsString();
				return true;
			}
		}
		return false;
	}

	private boolean createProject(String name) {

		final JsonArray tags = new JsonArray();
		tags.add("ats");

		final JsonObject newProject = createJsonDataWithTag(userId, name, "New Project created by ATS framework", "DESIGN", tags);
		newProject.addProperty("workspaceId", workspaceId);

		final JsonElement createProject = executeRequest(getPostRequest(newProject, PROJECT_API));
		if(createProject != null && createProject.isJsonObject()) {
			final JsonObject obj = createProject.getAsJsonObject();
			if(obj.has("id")) {
				projectId = obj.get("id").getAsString();
				return true;
			}
		}
		return false;
	}

	private boolean checkWorkspaceExists(String workspaceName) {
		final JsonElement response = executeRequest(getGetRequest(MEMBER_OF_API));
		if(response != null && response.isJsonArray()) {
			final JsonArray workspaces = response.getAsJsonArray();
			for (int i=0; i< workspaces.size(); i++) {
				final JsonElement item = workspaces.get(i);
				if(item.isJsonObject() 
						&& item.getAsJsonObject().has("name") 
						&& item.getAsJsonObject().get("name").getAsString().equals(workspaceName)) {

					workspaceId = item.getAsJsonObject().get("id").getAsString();
					return true;
				}
			}
		}
		return createWorkspace(workspaceName);
	}

	private boolean checkProjectExists(String projectName) {
		final JsonElement response = executeRequest(getGetRequest(PROJECT_WORKSPACE_API, workspaceId, "/DESIGN"));
		if(response != null && response.isJsonArray()) {
			final JsonArray projects = response.getAsJsonArray();
			for (int i=0; i< projects.size(); i++) {
				final JsonElement item = projects.get(i);
				if(item.isJsonObject() 
						&& item.getAsJsonObject().has("name") 
						&& item.getAsJsonObject().get("name").getAsString().equals(projectName)) {

					projectId = item.getAsJsonObject().get("id").getAsString();
					return true;
				}
			}
		}
		return createProject(projectName);
	}

	//----------------------------------------------------------------------------------------------------------
	// Utils
	//----------------------------------------------------------------------------------------------------------

	private static JsonObject createJsonDataWithTag(String userId, String name, String description, String type, JsonArray tags) {

		final JsonObject obj = createJsonData(userId, name, description);
		obj.addProperty("type", type);
		obj.add("tags", tags);

		return obj;
	}

	private static JsonObject createJsonData(String userId, String name, String description) {

		final String currentDate = Instant.now().toString();

		final JsonObject obj = new JsonObject();
		obj.addProperty("created", currentDate);
		obj.addProperty("description", description);
		obj.addProperty("id", "");
		obj.addProperty("lastModified", currentDate);
		obj.addProperty("name", name);
		obj.addProperty("userId", userId);

		return obj;
	}

	private static File createGZipFile(Path tempFolder, File harFile) throws IOException {

		if(harFile.exists()) {
			final String harFileName = harFile.getName();
			final File gzipFile = tempFolder.resolve(harFileName + ".gzip").toFile();

			try {
				FileInputStream fis = new FileInputStream(harFile);
				FileOutputStream fos = new FileOutputStream(gzipFile);
				GZIPOutputStream gzipOS = new GZIPOutputStream(fos);

				byte[] buffer = new byte[1024];
				int len;
				while((len=fis.read(buffer)) != -1){
					gzipOS.write(buffer, 0, len);
				}

				gzipOS.close();
				fos.close();
				fis.close();

			} catch (IOException e) {
				e.printStackTrace();
			}

			return gzipFile;
		}
		return null;
	}

	//----------------------------------------------------------------------------------------------------------
	// Requests
	//----------------------------------------------------------------------------------------------------------

	private Builder getRequest(String ... queryParts) {

		final StringBuilder urlBuilder = new StringBuilder(host);
		for(String str : queryParts) {
			urlBuilder.append(str);
		}

		final Builder request = new Request.Builder()
				.url(urlBuilder.toString())
				.addHeader("User-Agent", "ats")
				.addHeader("Authorization", apiKey);
		return request;
	}

	private Request getPostRequest(JsonElement element, String ... queryParts) {
		final Request request = getRequest(queryParts)
				.addHeader("Content-Type", JSON_CONTENT_TYPE)
				.post(RequestBody.create(null, element.toString()))
				.build();
		return request;
	}

	private Request getPutRequest(JsonElement element, String ... queryParts) {
		final Request request = getRequest(queryParts)
				.addHeader("Content-Type", JSON_CONTENT_TYPE)
				.put(RequestBody.create(null, element.toString()))
				.build();
		return request;
	}

	private Request getGetRequest(String ... queryParts) {
		final Request request = getRequest(queryParts)
				.get()
				.build();
		return request;
	}

	public Request getMultiPartRequest(Channel channel, File f, String... queryParts) {

		String fileType = "";
		if(f.getName().endsWith(".gzip")) {
			fileType = "application/x-gzip";
		}

		final RequestBody requestBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart("file", f.getName(), new RequestBodyWithUploadProgress(f, fileType, channel))
				.build();

		final Request request = getRequest(queryParts)
				.addHeader("Content-Type", JSON_CONTENT_TYPE)
				.addHeader("MimeType", MIME_TYPE)
				.post(requestBody)
				.build();

		return request;
	}

	private JsonElement executeRequest(Request request) {
		final OkHttpClient client = new OkHttpClient.Builder().build();
		try {
			final Response response = client.newCall(request).execute();

			final String responseData = CharStreams.toString(
					new InputStreamReader(
							response
							.body()
							.byteStream(), 
							Charsets.UTF_8));
			response.close();
			return JsonParser.parseString(responseData);
		} catch (JsonSyntaxException | IOException e) {
			return null;
		}
	}

	//----------------------------------------------------------------------------------------------

	public class RequestBodyWithUploadProgress extends RequestBody {

		private final File file;
		private final String contentType;
		private final long fileLength;
		private final Channel channel;

		public RequestBodyWithUploadProgress(File file, String contentType, Channel channel) {
			this.file = file;
			this.contentType = contentType;
			this.fileLength = file.length();
			this.channel = channel;
		}

		@Override
		public long contentLength() {
			return fileLength;
		}

		@Override
		public MediaType contentType() {
			return MediaType.parse(contentType);
		}

		@Override
		public void writeTo(BufferedSink sink) throws IOException {
			Source source = null;
			try {
				source = Okio.source(file);
				long total = 0;
				long read;

				if(fileLength > BUFFER) {
					int threshold = 0;
					while ((read = source.read(sink.buffer(), BUFFER)) != -1) {
						total += read;
						sink.flush();

						final int percent = (int)((float)total/((float)fileLength)*100);
						if(percent > threshold) {
							threshold += 10;
							channel.sendLog(MessageCode.UPLOAD_FILE, "Uploading HAR file, remaining bytes percent to upload", (100-percent) + 1);
						}
					}
					channel.sendLog(MessageCode.UPLOAD_FILE, "Uploading HAR file done", 0);
				}else {
					source.read(sink.buffer(), fileLength);
					sink.flush();
				}
			}
			catch (FileNotFoundException e) {
			}
			finally {
				Util.closeQuietly(source);
			}
		}
	}
}