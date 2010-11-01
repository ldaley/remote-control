/*
 * Copyright 2010 Luke Daley
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovyx.remote

import groovyx.remote.util.ClassLoaderConfigurableObjectInputStream

class Result implements Serializable {

	/**
	 * If set, indicates that the result was null
	 */
	boolean wasNull = false
	
	/**
	 * If set, indicates that the result was ultimately not able to be serialized.
	 * 
	 * The string representation will be available via stringRepresentation.
	 */
	boolean wasUnserializable = false
	
	/**
	 * The string representation of the return value if it was not able to be serialized.
	 */
	String stringRepresentation = null
	
	/**
	 * If a throwable was thrown, this will be that throwable.
	 */
	transient Throwable thrown = null
	
	/**
	 * The serialized version of what was thrown
	 */
	private byte[] serializedThrown = null
	
	/**
	 * The raw serialized bytes of the value, if it was able to be serialised.
	 */
	private byte[] serializedValue = null
	
	/**
	 * The value, if it was unable to be serialized.
	 */
	transient unserializable = null
	
	/**
	 * Stores the unserialized value when deserializing back at the client
	 */
	transient Serializable value
	
	static forNull() {
		new Result(wasNull: true)
	}
	
	static forValue(value) {
		if (value == null) {
			forNull()
		} else if (value instanceof Serializable) {
			forSerializable(value)
		} else {
			forUnserializable(value)
		}
	}
	
	static forThrown(Throwable thrown) {
		def serializedThrown
		try {
			serializedThrown = serialize(thrown)
		} catch (NotSerializableException) {
			thrown = new UnserializableExceptionException(thrown)
			serializedThrown = serialize(thrown)
		}
		
		new Result(
			thrown: thrown,
			serializedThrown: serializedThrown
		)
	}
	
	private static forUnserializable(unserializable) {
		new Result(
			wasUnserializable: true,
			stringRepresentation: unserializable.toString(),
			unserializable: unserializable
		)
	}
	
	private static forSerializable(Serializable serializable) {
		try {
			new Result(
				value: serializable,
				serializedValue: serialize(serializable),
				stringRepresentation: serializable.toString()
			)
		} catch (NotSerializableException e) {
			forUnserializable(serializable)
		}
	}
	
	private static serialize(Serializable serializable) {
		def baos = new ByteArrayOutputStream()
		def oos = new ObjectOutputStream(baos)
		
		try {
			oos << serializable
		} finally {
			oos.flush()
			oos.close()
		}
		
		baos.toByteArray()
	}
	
	void writeTo(OutputStream output) {
		def oos = new ObjectOutputStream(output)
		oos << this
		oos.flush()
		oos.close()
	}
	
	protected hydrate(ClassLoader classLoader) {
		if (serializedValue) {
			this.value = deserialize(classLoader, serializedValue)
		}
		if (serializedThrown) {
			this.thrown = deserialize(classLoader, serializedThrown)
		}
	}
	
	protected deserialize(ClassLoader classLoader, byte[] bytes) {
		new ClassLoaderConfigurableObjectInputStream(classLoader, new ByteArrayInputStream(bytes)).readObject() 
	}
	
	static Result readFrom(InputStream input, ClassLoader classLoader) {
		def result = new ClassLoaderConfigurableObjectInputStream(classLoader, input).readObject()
		result.hydrate(classLoader)
		result
	}
}