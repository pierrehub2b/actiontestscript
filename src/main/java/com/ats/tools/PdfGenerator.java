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

package com.ats.tools;

public class PdfGenerator
{
	public static void main(String[] args)
	{
		for( var value : args ) {
			System.out.println( value );
		}

		String fopBuild = args[0] + "\\build\\fop.jar";
		String fopLib = args[0] + "\\lib\\*";
		final Process p = Runtime.getRuntime().exec(&quot;cmd /c java -cp fopBuil);
	}
}