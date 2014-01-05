/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovyx.remote.client;

import groovyx.remote.Command;
import groovyx.remote.CommandChain;
import groovyx.remote.result.*;

import java.io.IOException;

public class RemoteControlSupport<T extends Command> {

    private final Transport transport;
    private final UnserializableResultStrategy unserializableResultStrategy;
    private final ClassLoader classLoader;

    public RemoteControlSupport(Transport transport, UnserializableResultStrategy unserializableResultStrategy, ClassLoader classLoader) {
        this.transport = transport;
        this.unserializableResultStrategy = unserializableResultStrategy;
        this.classLoader = classLoader;
    }

    public Object send(CommandChain<T> commandChain) throws IOException {
        Result result = sendCommandChain(commandChain);
        return processResult(result);
    }

    protected Result sendCommandChain(CommandChain commandChain) throws IOException {
        return transport.send(commandChain);
    }

    protected Object processResult(Result result) {
        if (result instanceof NullResult) {
            return null;
        } else if (result instanceof ThrownResult) {
            ThrownResult thrownResult = (ThrownResult) result;
            throw new RemoteException(thrownResult.deserialize(classLoader));
        } else if (result instanceof UnserializableThrownResult) {
            UnserializableThrownResult unserializedThrownResult = (UnserializableThrownResult) result;
            throw new RemoteException(unserializedThrownResult.deserializeWrapper(classLoader));
        } else if (result instanceof UnserializableResult) {
            UnserializableResult unserializableResult = (UnserializableResult) result;
            if (unserializableResultStrategy == UnserializableResultStrategy.NULL) {
                return null;
            } else if (unserializableResultStrategy == UnserializableResultStrategy.STRING) {
                return unserializableResult.getStringRepresentation();
            } else if (unserializableResultStrategy == UnserializableResultStrategy.THROW) {
                throw new UnserializableReturnException(unserializableResult);
            } else {
                throw new IllegalStateException("Unhandled UnserializableResultStrategy: " + unserializableResultStrategy);
            }
        } else if (result instanceof SerializedResult) {
            SerializedResult serializedResult = (SerializedResult) result;
            return serializedResult.deserialize(classLoader);
        } else {
            throw new IllegalArgumentException("Unknown result type: " + result);
        }
    }

}
