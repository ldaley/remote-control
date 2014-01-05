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

package groovyx.remote.result.impl;

import groovyx.remote.RemoteControlException;
import groovyx.remote.SerializationUtil;
import groovyx.remote.result.SerializedResult;

public class DefaultSerializedResult implements SerializedResult {

    private final byte[] bytes;

    public DefaultSerializedResult(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override
    public Object deserialize(ClassLoader classLoader) {
        try {
            return SerializationUtil.deserialize(Object.class, bytes, classLoader);
        } catch (ClassNotFoundException e) {
            throw RemoteControlException.classNotFoundOnClient(e);
        }
    }

}
