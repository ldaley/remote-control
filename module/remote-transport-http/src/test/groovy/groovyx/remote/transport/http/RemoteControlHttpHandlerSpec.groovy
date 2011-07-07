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

import groovyx.remote.client.*
import groovyx.remote.server.*
import com.sun.net.httpserver.HttpServer
import java.util.concurrent.Executors

import spock.lang.*

class RemoteControlHttpHandlerSpec extends Specification {

	@Shared remote
	@Shared server
	
	def setupSpec() {
		// we need to create a classloader for the "server" side that cannot access
		// classes defined in this file.
		def thisClassLoader = getClass().classLoader
		def neededURLsForServer = thisClassLoader.getURLs().findAll { it.path.contains("groovy-all") }
		def serverClassLoader = new URLClassLoader(neededURLsForServer as URL[], thisClassLoader.parent)
		
		def receiver = new Receiver(serverClassLoader)
		
		server = HttpServer.create(new InetSocketAddress(0), 1)
		server.createContext("/", new RemoteControlHttpHandler(receiver))
		server.executor = Executors.newSingleThreadExecutor()
		server.start()
	
		Thread.sleep(2000)
		
		remote = new RemoteControl(new HttpTransport("http://localhost:${server.address.port}" as String))
	}

	def "test the handler"() {
		expect:
		remote.exec { def a = 2; a + 2 } == 4
	}
	
	def cleanupSpec() {
		server.stop(0)
	}
}