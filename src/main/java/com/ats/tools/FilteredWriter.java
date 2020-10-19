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

import java.io.FilterWriter;
import java.io.IOException;
import java.io.Writer;

public class FilteredWriter extends FilterWriter
{
    protected char[] filter = "\t".toCharArray();

    protected FilteredWriter(Writer out) {
        super(out);
    }

    public void write(String str, int off, int len) throws IOException
    {
        write(str.toCharArray(), off, len);
    }

    public void write(char[] cbuf, int off, int len) throws IOException
    {
        for (int i = off; i < off + len; i++)
            write(cbuf[i]);
    }

    public void write(int c) throws IOException
    {
        for (char f : filter)
        {
            if (f == (char)c)
                return;
        }
        out.write(c);
    }
}
