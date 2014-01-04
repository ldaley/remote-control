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

package groovyx.remote;

import groovyx.remote.util.ClassLoaderConfigurableObjectInputStream;
import groovyx.remote.util.UnexpectedIOException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class SerializationUtil {

    public static byte[] serialize(Serializable serializable) throws NotSerializableException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            serialize(serializable, baos);
        } catch (IOException e) {
            if (e instanceof NotSerializableException) {
                throw (NotSerializableException) e;
            }
            throw new UnexpectedIOException("Unexpected exception while serializing object: " + serializable, e);
        }
        return baos.toByteArray();
    }

    public static void serialize(Serializable serializable, OutputStream outputStream) throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(outputStream);
        try {
            oos.writeObject(serializable);
        } finally {
            try {
                oos.close();
            } catch (IOException ignore) {
                // just suppress
            }
        }
    }

    public static <T> T deserialize(Class<T> type, byte[] bytes, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            return deserialize(type, new ByteArrayInputStream(bytes), classLoader);
        } catch (IOException e) {
            throw new UnexpectedIOException("Unexpected exception while deserializing in memory", e);
        }
    }

    public static <T> T deserialize(Class<T> type, InputStream inputStream, ClassLoader classLoader) throws IOException, ClassNotFoundException {
        Object o = new ClassLoaderConfigurableObjectInputStream(classLoader, inputStream).readObject();
        if (type.isInstance(o)) {
            return type.cast(o);
        } else {
            throw new IllegalArgumentException("Expecting to deserialize instance of " + type.getName() + " but got " + o.getClass().getName() + ": " + o.toString());
        }
    }

    public static Class<?> defineClass(ClassLoader classLoader, byte[] bytes) {
        try {
            Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
            defineClass.setAccessible(true);
            Object clazz = defineClass.invoke(classLoader, null, bytes, 0, bytes.length);
            return (Class<?>) clazz;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
