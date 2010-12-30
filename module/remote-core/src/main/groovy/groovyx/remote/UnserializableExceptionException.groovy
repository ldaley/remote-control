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

class UnserializableExceptionException extends RemoteControlException {

	static public final long serialVersionUID = 1L
	
	UnserializableExceptionException(Throwable unserializable) {
		super("wrapped unserializable exception: class = ${unserializable.class.name}, message = \"${unserializable.message}\"" as String)
		this.stackTrace = unserializable.stackTrace
		
		if (unserializable.cause) {
			initCause(new UnserializableExceptionException(unserializable.cause))
		}
	}

}