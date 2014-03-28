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

package io.remotecontrol.groovy.server;

import groovy.lang.Closure;
import io.remotecontrol.result.ResultFactory;
import io.remotecontrol.result.impl.DefaultResultFactory;
import io.remotecontrol.server.MultiTypeReceiver;
import io.remotecontrol.server.Receiver;
import io.remotecontrol.server.StorageContextFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Receives a serialised command chain as an input stream to be unserialised and executed, then writes the serialised result to an output stream.
 */
public class ClosureReceiver implements Receiver {

    private final Receiver delegate;

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see ContextFactory
     */
    public ClosureReceiver(ClassLoader classLoader, ContextFactory contextFactory, ResultFactory resultFactory) {
        delegate = new MultiTypeReceiver(classLoader, new ClosureCommandRunner(classLoader, contextFactory, resultFactory));
    }

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see ContextFactory
     */
    public ClosureReceiver(ClassLoader classLoader, ContextFactory contextFactory) {
        this(classLoader, contextFactory, new DefaultResultFactory());
    }

    /**
     * Creates a receiever that uses the current threads context class loader and the given contextFactory.
     *
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see io.remotecontrol.server.Receiver ( ClassLoader , ContextFactory )
     */
    public ClosureReceiver(ContextFactory contextFactory) {
        this(Thread.currentThread().getContextClassLoader(), contextFactory);
    }

    /**
     * Implicitly creates a StorageContextFactory that uses no initial values.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @see io.remotecontrol.server.StorageContextFactory#withEmptyStorage()
     */
    public ClosureReceiver(ClassLoader classLoader) {
        this(classLoader, StorageContextFactory.withEmptyStorage());
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory that uses no initial values.
     *
     * @see io.remotecontrol.server.Receiver ( ClassLoader )
     */
    public ClosureReceiver() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given map as a seed.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextStorageSeed the seed for the storage
     * @see io.remotecontrol.server.StorageContextFactory#withSeed(java.util.Map)
     */
    public ClosureReceiver(ClassLoader classLoader, Map contextStorageSeed) {
        this(classLoader, StorageContextFactory.withSeed(contextStorageSeed));
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory with the given map as the seed.
     *
     * @param contextStorageSeed the seed for the storage
     * @see io.remotecontrol.server.Receiver ( ClassLoader , Map )
     */
    public ClosureReceiver(Map contextStorageSeed) {
        this(Thread.currentThread().getContextClassLoader(), contextStorageSeed);
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given closure as a generator.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @see io.remotecontrol.server.StorageContextFactory#withGenerator(groovy.lang.Closure)
     */
    public ClosureReceiver(ClassLoader classLoader, Closure contextStorageGenerator) {
        this(classLoader, StorageContextFactory.withGenerator(contextStorageGenerator));
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory with the given closure as the generator.
     *
     * @param contextStorageSeed the seed for the storage
     * @see io.remotecontrol.server.Receiver ( ClassLoader , Closure )
     */
    public ClosureReceiver(Closure contextStorageSeed) {
        this(Thread.currentThread().getContextClassLoader(), contextStorageSeed);
    }

    public void execute(InputStream input, OutputStream output) throws IOException {
        delegate.execute(input, output);
    }

}
