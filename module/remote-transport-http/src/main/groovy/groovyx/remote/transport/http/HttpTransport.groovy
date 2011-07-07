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
package groovyx.remote.transport.http

import groovyx.remote.*
import groovyx.remote.util.*
import groovyx.remote.client.Transport

/**
 * Transports commands over http to the given receiver address.
 */
class HttpTransport implements Transport {

	final receiverAddress
	final classLoader
	
	/**
	 * @param receiverAddress the full address to the remote receiver
	 * @param classLoader the class loader to use when unserialising the result
	 */
	HttpTransport(String receiverAddress, ClassLoader classLoader) {
		this.classLoader = classLoader
		this.receiverAddress = receiverAddress
	}

	/**
	 * @param receiverAddress the full address to the remote receiver
	 */
	HttpTransport(String receiverAddress) {
		this(receiverAddress, Thread.currentThread().contextClassLoader)
	}

	/**
	 * Serialises the Command and sends it over HTTP, returning the Result.
	 * 
	 * @throws IOException if there is any issue with the receiver.
	 */
	Result send(CommandChain commandChain) throws RemoteControlException {
		try {
			openConnection().with {
				setRequestProperty("Content-Type", ContentType.COMMAND.value)
				setRequestProperty("accept", ContentType.RESULT.value)
				instanceFollowRedirects = true
				doOutput = true

				configureConnection(delegate)

				commandChain.writeTo(outputStream)
				Result.readFrom(inputStream, classLoader)
			}
		} catch (Exception e) {
			throw new RemoteControlException("Error sending command chain to '$receiverAddress'", e)
		}
	}
	
	/**
	 * Subclass hook for configuring the connection object before the request is set.
	 * 
	 * This could be used to implement authentication.
	 */
	protected configureConnection(HttpURLConnection connection) {
		
	}
	
	/**
	 * Creates a HttpURLConnection to the remote receiver at the given receiverAddress.
	 */
	protected HttpURLConnection openConnection() {
		new URL(receiverAddress).openConnection()
	}

}