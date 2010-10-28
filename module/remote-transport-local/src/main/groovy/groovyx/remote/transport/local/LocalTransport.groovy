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
package groovyx.remote.transport.local

import groovyx.remote.*
import groovyx.remote.server.Receiver
import groovyx.remote.client.AbstractTransport

class LocalTransport extends AbstractTransport {

	final serverClassLoader
	final clientClassLoader
	
	LocalTransport(ClassLoader serverClassLoader, ClassLoader clientClassLoader) {
		this.serverClassLoader = serverClassLoader
		this.clientClassLoader = clientClassLoader
	}

	Result send(CommandChain commandChain) throws IOException {
		def commandBytes = new ByteArrayOutputStream()
		writeCommandChain(commandChain, commandBytes)
		def resultBytes = new ByteArrayOutputStream()
		createReceiver().execute(new ByteArrayInputStream(commandBytes.toByteArray()), resultBytes)
		readResult(new ByteArrayInputStream(resultBytes.toByteArray()), clientClassLoader)
	}
	
	protected Receiver createReceiver() {
		new Receiver(serverClassLoader)
	}
	
}