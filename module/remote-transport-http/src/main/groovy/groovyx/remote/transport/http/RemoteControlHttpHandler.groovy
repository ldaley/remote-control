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

import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpExchange
import groovyx.remote.server.Receiver

class RemoteControlHttpHandler implements HttpHandler {

	final Receiver receiver
	
	RemoteControlHttpHandler(Receiver receiver) {
		this.receiver = receiver
	}

	void handle(HttpExchange exchange) {
		if (exchange.requestMethod != "POST") {
			exchange.sendResponseHeaders(415, 0)
			exchange.responseBody.withStream { it << "request must be a POST" }
			return
		}
		
		if (exchange.requestHeaders.getFirst("Content-Type") != ContentType.COMMAND.value) {
			exchange.sendResponseHeaders(415, 0)
			exchange.responseBody.withStream { it << "Content type must be: ${ContentType.COMMAND.value}" }
			return
		}

		exchange.responseHeaders.set("Content-Type", ContentType.RESULT.value)
		exchange.sendResponseHeaders(200, 0)
		
		exchange.responseBody.withStream {
			receiver.execute(exchange.requestBody, it)
		}
	}
}
