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

import groovyx.remote.CommandChain
import groovyx.remote.client.Transport
import groovyx.remote.result.Result
import groovyx.remote.result.ResultFactory
import groovyx.remote.result.impl.DefaultResultFactory
import groovyx.remote.server.Receiver

class LocalTransport implements Transport {

    final ClassLoader classLoader
    final Receiver receiver
    private final ResultFactory resultFactory

    LocalTransport(Receiver receiver, ClassLoader classLoader, ResultFactory resultFactory = new DefaultResultFactory()) {
        this.resultFactory = resultFactory
        this.receiver = receiver
        this.classLoader = classLoader
    }

    Result send(CommandChain commandChain) throws IOException {
        def commandBytes = new ByteArrayOutputStream()
        commandChain.writeTo(commandBytes)

        def resultBytes = new ByteArrayOutputStream()
        receiver.execute(new ByteArrayInputStream(commandBytes.toByteArray()), resultBytes)

        resultFactory.deserialize(new ByteArrayInputStream(resultBytes.toByteArray()), classLoader)
    }
}