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

import javax.servlet.*
import javax.servlet.http.*
import groovyx.remote.*
import groovyx.remote.server.*
import javax.servlet.ServletConfig

/**
 * A servlet implementation for receiving commands.
 */
class RemoteControlServlet extends HttpServlet {
	
	Receiver receiver 
	
	void init(ServletConfig config) {
		receiver = createReceiver()
	}
	
	void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.contentType != ContentType.COMMAND.value) {
			response.sendError(415, "Only remote control commands can be sent")
			return
		}

		response.contentType = ContentType.RESULT.value
		
		doExecute(request.inputStream, response.outputStream)
	}

	/**
	 * Hook for subclasses to wrap the actual execution.
	 */
	protected void doExecute(InputStream input, OutputStream output) {
		receiver.execute(input, output)
	}
	
	/**
	 * Hook for subclasses to provide a custom receiver. Will be called during init().
	 * 
	 * This implement returns a receiver created via the default constructor.
	 */
	protected Receiver createReceiver() {
		new Receiver()
	}
}