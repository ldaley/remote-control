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
package io.remotecontrol;

public class RemoteControlException extends RuntimeException {

	static public final long serialVersionUID = 1L;

	public RemoteControlException(Object message) {
		this(message, null);
	}

	public RemoteControlException(Object message, Throwable cause) {
		super(message.toString());
		if (cause != null) {
			initCause(cause);
		}
	}

    public static RemoteControlException classNotFoundOnServer(ClassNotFoundException e) {
        return new RemoteControlException("Class not found on server (the command referenced a class that the server does not have)", e);
    }

    public static RemoteControlException classNotFoundOnClient(ClassNotFoundException e) {
        return new RemoteControlException("Class not found on client (the result referenced a class that the client does not have)", e);
    }

}