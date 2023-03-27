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
package io.remotecontrol.transport.http.javax

import io.remotecontrol.groovy.client.RemoteControl
import io.remotecontrol.groovy.server.ClosureReceiver
import io.remotecontrol.server.Receiver
import io.remotecontrol.transport.http.HttpTransport
import io.remotecontrol.util.FilteringClassLoader
import org.mortbay.jetty.Server
import org.mortbay.jetty.servlet.Context
import org.mortbay.jetty.servlet.ServletHolder
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

        server = new Server(0)
        def context = new Context(server, "/")
        def servlet = new RemoteControlServlet() {
            @Override
            protected Receiver createReceiver() {
                return new ClosureReceiver(serverClassLoader)
            }
        }
        context.addServlet(new ServletHolder(servlet), "/*")
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
