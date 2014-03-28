/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.remotecontrol.util;

import org.codehaus.groovy.runtime.IOGroovyMethods;

import java.io.*;

public abstract class IoUtil {

    private IoUtil() {

    }

    public static byte[] read(InputStream inputStream) throws IOException {
        return IOGroovyMethods.getBytes(inputStream);
    }

    public static byte[] read(File file) throws IOException {
        return read(new FileInputStream(file));
    }
}
