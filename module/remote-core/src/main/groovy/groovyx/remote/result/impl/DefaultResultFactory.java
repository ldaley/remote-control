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
import groovyx.remote.UnserializableExceptionException;
import groovyx.remote.result.Result;
import groovyx.remote.result.ResultFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.NotSerializableException;
import java.io.Serializable;

public class DefaultResultFactory implements ResultFactory {

    @Override
    public Result forValue(Object value) {
        if (value == null) {
            return new DefaultNullResult();
        } else if (value instanceof Serializable) {
            return forSerializable((Serializable) value);
        } else {
            return forUnserializable(value);
        }
    }

    protected Result forSerializable(Serializable serializable) {
        try {
            byte[] bytes = SerializationUtil.serialize(serializable);
            return new DefaultSerializedResult(bytes);
        } catch (NotSerializableException e) {
            return forUnserializable(serializable);
        }
    }

    protected Result forUnserializable(Object unserializable) {
        return new DefaultUnserializableResult(unserializable.toString());
    }

    @Override
    public Result forThrown(Throwable throwable) {
        try {
            byte[] bytes = SerializationUtil.serialize(throwable);
            return new DefaultThrownResult(bytes);
        } catch (NotSerializableException e) {
            try {
                return new DefaultUnserializableThrownResult(throwable.toString(), SerializationUtil.serialize(new UnserializableExceptionException(throwable)));
            } catch (NotSerializableException e1) {
                throw new IllegalStateException("Can't serialize NotSerializableException", e1);
            }
        }
    }

    @Override
    public Result deserialize(InputStream inputStream, ClassLoader classLoader) throws IOException {
        try {
            return SerializationUtil.deserialize(Result.class, inputStream, classLoader);
        } catch (ClassNotFoundException e) {
            throw RemoteControlException.classNotFoundOnClient(e);
        }
    }

}
