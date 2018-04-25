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

package com.ats.tools.win32;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef.DWORD;
/* https://jna.dev.java.net/ */
public interface Kernel32 extends W32API {
    Kernel32 INSTANCE = (Kernel32) Native.loadLibrary("kernel32", Kernel32.class, DEFAULT_OPTIONS);
    /* http://msdn.microsoft.com/en-us/library/ms683179(VS.85).aspx */
    HANDLE GetCurrentProcess();
    DWORD GetCurrentProcessId();
    /* http://msdn.microsoft.com/en-us/library/ms683215.aspx */
    int GetProcessId(HANDLE Process);
}