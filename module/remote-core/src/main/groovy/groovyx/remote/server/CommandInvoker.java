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

package groovyx.remote.server;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovyx.remote.Command;
import groovyx.remote.SerializationUtil;
import org.codehaus.groovy.runtime.InvokerInvocationException;

import java.io.IOException;

public class CommandInvoker {

    private final ClassLoader parentLoader;
    private final Command command;

    public CommandInvoker(ClassLoader parentLoader, Command command) {
        this.parentLoader = parentLoader;
        this.command = command;
    }

    public Object invokeAgainst(Object delegate, Object argument) throws Throwable {
        try {
            Closure instance = instantiate();

            instance.setResolveStrategy(Closure.DELEGATE_ONLY);
            instance.setDelegate(delegate);

            if (instance.getMaximumNumberOfParameters() < 1) {
                return instance.call();
            } else {
                return instance.call(argument);
            }
        } catch (Throwable thrown) {
            // If the server and client do not share the groovy classes, we get this
            try {
                parentLoader.loadClass(InvokerInvocationException.class.getName()).isAssignableFrom(thrown.getClass());
            } catch (ClassNotFoundException throwable) {
                throw thrown.getCause();
            }

            throw thrown;
        }
    }

    protected Closure<?> instantiate() throws IOException, ClassNotFoundException {
        final GroovyClassLoader classLoader = new GroovyClassLoader(parentLoader);
        SerializationUtil.defineClass(classLoader, command.getRoot());

        for (byte[] bytes : command.getSupports()) {
            SerializationUtil.defineClass(classLoader, bytes);
        }

        return SerializationUtil.deserialize(Closure.class, command.getInstance(), classLoader);
    }

}
