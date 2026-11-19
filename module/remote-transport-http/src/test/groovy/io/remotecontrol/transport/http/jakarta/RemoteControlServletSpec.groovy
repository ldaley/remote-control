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
package io.remotecontrol.transport.http.jakarta

import io.remotecontrol.groovy.client.RemoteControl
import io.remotecontrol.groovy.server.ClosureReceiver
import io.remotecontrol.server.Receiver
import io.remotecontrol.transport.http.HttpTransport
import io.remotecontrol.util.FilteringClassLoader
import jakarta.servlet.Servlet
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import spock.lang.Shared
import spock.lang.Specification

class RemoteControlServletSpec extends Specification {

    @Shared
        remote
    @Shared
        server
    @Shared
        endpointUrl

    def setupSpec() {
        // we need to create a classloader for the "server" side that cannot access
        // classes defined in this file.
        def thisClassLoader = getClass().classLoader
        def serverClassLoader = new FilteringClassLoader(thisClassLoader, getClass().package.name)

        Servlet servlet = new RemoteControlServlet() {
            @Override
            protected Receiver createReceiver() {
                return new ClosureReceiver(serverClassLoader)
            }
        }

        def contextHandler = new ServletContextHandler()
        contextHandler.addServlet(new ServletHolder(servlet), "/*")
        contextHandler.setContextPath("/")
        server = new Server(0)
        server.setHandler(contextHandler)
        server.start()

        def port = server?.connectors[0].localPort

        endpointUrl = "http://localhost:${port}"
        remote = new RemoteControl(new HttpTransport(endpointUrl))
    }

    def "test the handler"() {
        expect:
        remote.exec { def a = 2; a + 2 } == 4
    }

    def "hit direct"() {
        when:
        HttpURLConnection connection = new URL(endpointUrl).openConnection()
        connection.requestMethod = "POST"

        then:
        connection.responseCode == 415
    }

    def cleanupSpec() {
        server.stop()
    }
}
