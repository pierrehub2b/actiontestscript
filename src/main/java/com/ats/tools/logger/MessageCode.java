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

package com.ats.tools.logger;

public final class MessageCode {

	public static final int TECHNICAL_ERROR = 1;
	public static final int OBJECT_TRY_SEARCH = 50;
	public static final int ACTION_IN_PROGRESS = 51;
	public static final int PROPERTY_TRY_ASSERT = 52;
	public static final int PROPERTY_NOT_FOUND = 53;
	public static final int UPLOAD_FILE = 54;	
	
	public static final int CHANNEL_STATUS = 100;
	
	public static final int ACTION_READY = 110;
	public static final int ACTION_COMPLETE = 111;
	public static final int ACTION_CANCELED = 112;
				
	public static final int NOT_COMPLETE_STATUS = 181;
	public static final int NON_BLOCKING_FAILED = 182;
	public static final int CLOSE_BROWSER = 199;
	
	public static final int STATUS_OK = 200;
	public static final int URL_OPEN = 202;
	
	public static final int OBJECT_SEARCH_DONE = 220;
	
	public static final int ERROR_ACTION = 400;
	public static final int ERROR_JAVASCRIPT = 401;
	public static final int OBJECT_NOT_FOUND = 421;
	public static final int SCRIPT_NOT_FOUND = 422;
	public static final int NO_RUNNING_CHANNEL = 450;
	public static final int CHANNEL_NOT_FOUND = 451;
}