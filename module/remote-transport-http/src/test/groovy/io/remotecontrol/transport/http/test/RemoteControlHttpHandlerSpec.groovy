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
package io.remotecontrol.transport.http.test

import com.sun.net.httpserver.HttpServer
import io.remotecontrol.groovy.client.RemoteControl
import io.remotecontrol.groovy.server.ClosureReceiver
import io.remotecontrol.transport.http.HttpTransport
import io.remotecontrol.transport.http.RemoteControlHttpHandler
import io.remotecontrol.util.FilteringClassLoader
import spock.lang.Shared
import spock.lang.Specification

import java.util.concurrent.Executors

class RemoteControlHttpHandlerSpec extends Specification {

    @Shared
        remote
    @Shared
        server

    def setupSpec() {
        // we need to create a classloader for the "server" side that cannot access
        // classes defined in this file.
        def thisClassLoader = getClass().classLoader
        def serverClassLoader = new FilteringClassLoader(thisClassLoader, getClass().getPackage().name)

        def receiver = new ClosureReceiver(serverClassLoader)

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