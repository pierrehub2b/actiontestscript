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

/* Copyright (c) 2007 Timothy Wall, All Rights Reserved
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.  
 */

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.FromNativeContext;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIFunctionMapper;
import com.sun.jna.win32.W32APITypeMapper;

/** Base type for most W32 API libraries.  Provides standard options
 * for unicode/ASCII mappings.  Set the system property w32.ascii
 * to true to default to the ASCII mappings.
 */
public interface W32API extends StdCallLibrary, W32Errors {
    
    /** Standard options to use the unicode version of a w32 API. */
    @SuppressWarnings({ "rawtypes", "unchecked", "serial" })
	Map UNICODE_OPTIONS = new HashMap() {
        {
            put(OPTION_TYPE_MAPPER, W32APITypeMapper.UNICODE);
            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.UNICODE);
        }
    };
    
    /** Standard options to use the ASCII/MBCS version of a w32 API. */
    @SuppressWarnings({ "rawtypes", "serial", "unchecked" })
	Map ASCII_OPTIONS = new HashMap() {
        {
            put(OPTION_TYPE_MAPPER, W32APITypeMapper.ASCII);
            put(OPTION_FUNCTION_MAPPER, W32APIFunctionMapper.ASCII);
        }
    };

    @SuppressWarnings("rawtypes")
	Map DEFAULT_OPTIONS = Boolean.getBoolean("w32.ascii") ? ASCII_OPTIONS : UNICODE_OPTIONS;
    
    public class HANDLE extends PointerType {
    	@Override
        public Object fromNative(Object nativeValue, FromNativeContext context) {
            Object o = super.fromNative(nativeValue, context);
            if (INVALID_HANDLE_VALUE.equals(o))
                return INVALID_HANDLE_VALUE;
            return o;
        }
    }

    /** Constant value representing an invalid HANDLE. */
    HANDLE INVALID_HANDLE_VALUE = new HANDLE() { 
        { super.setPointer(Pointer.createConstant(-1)); }
        @Override
        public void setPointer(Pointer p) { 
            throw new UnsupportedOperationException("Immutable reference");
        }
    };
}
