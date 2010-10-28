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
package groovyx.remote.http

import javax.servlet.*
import javax.servlet.http.*
import groovyx.remote.*
import groovyx.remote.server.*

class RemoteControlServlet extends HttpServlet {
	
	void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		if (request.contentType != ContentType.COMMAND.value) {
			response.sendError(415, "Only remote control commands can be sent")
			return
		}

		response.contentType = ContentType.RESULT.value
		receiver.execute(request.inputStream, response.outputStream)
	}

	protected Receiver createReceiver() {
		new Receiver()
	}
}