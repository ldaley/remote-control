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
package groovyx.remote.server

import groovyx.remote.CommandChain
import groovyx.remote.SerializationUtil
import groovyx.remote.result.Result
import groovyx.remote.result.ResultFactory
import groovyx.remote.result.impl.DefaultResultFactory

/**
 * Receives a serialised command chain as an input stream to be unserialised and executed, then
 * writes the serialised result to an output stream.
 */
class Receiver {

    final ClassLoader classLoader
    final ContextFactory contextFactory
    final ResultFactory resultFactory

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     *
     * @see ContextFactory
     */
    Receiver(ClassLoader classLoader, ContextFactory contextFactory, ResultFactory resultFactory) {
        this.classLoader = classLoader
        this.contextFactory = contextFactory
        this.resultFactory = resultFactory
    }

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     *
     * @see ContextFactory
     */
    Receiver(ClassLoader classLoader, ContextFactory contextFactory) {
        this(classLoader, contextFactory, new DefaultResultFactory())
    }

    /**
     * Creates a receiever that uses the current threads context class loader and the given contextFactory.
     *
     * @param contextFactory the context factory to use to create contexts for command chains
     *
     * @see Receiver ( ClassLoader , ContextFactory )
     */
    Receiver(ContextFactory contextFactory) {
        this(Thread.currentThread().contextClassLoader, contextFactory)
    }

    /**
     * Implicitly creates a StorageContextFactory that uses no initial values.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     *
     * @see StorageContextFactory#withEmptyStorage()
     */
    Receiver(ClassLoader classLoader) {
        this(classLoader, StorageContextFactory.withEmptyStorage())
    }

    /**
     * Creates a receiever that uses the current threads context class loader and
     * a StorageContextFactory that uses no initial values.
     *
     * @see Receiver ( ClassLoader )
     */
    Receiver() {
        this(Thread.currentThread().contextClassLoader)
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given map as a seed.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextStorageSeed the seed for the storage
     * @see StorageContextFactory#withSeed(Map)
     */
    Receiver(ClassLoader classLoader, Map contextStorageSeed) {
        this(classLoader, StorageContextFactory.withSeed(contextStorageSeed))
    }

    /**
     * Creates a receiever that uses the current threads context class loader and
     * a StorageContextFactory with the given map as the seed.
     *
     * @param contextStorageSeed the seed for the storage
     *
     * @see Receiver ( ClassLoader , Map )
     */
    Receiver(Map contextStorageSeed) {
        this(Thread.currentThread().contextClassLoader, contextStorageSeed)
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given closure as a generator.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     *
     * @see StorageContextFactory#withGenerator(Closure)
     */
    Receiver(ClassLoader classLoader, Closure contextStorageGenerator) {
        this(classLoader, StorageContextFactory.withGenerator(contextStorageGenerator))
    }

    /**
     * Creates a receiever that uses the current threads context class loader and
     * a StorageContextFactory with the given closure as the generator.
     *
     * @param contextStorageSeed the seed for the storage
     *
     * @see Receiver ( ClassLoader , Closure )
     */
    Receiver(Closure contextStorageGenerator) {
        this(Thread.currentThread().contextClassLoader, contextStorageGenerator)
    }

    /**
     * Executes a serialised command chain.
     *
     * @param input A stream containing a serialised CommandChain object.
     * @param output The stream that the Result object shall be written to.
     */
    void execute(InputStream command, OutputStream result) {
        def resultObject = invokeCommandChain(SerializationUtil.deserialize(CommandChain, command, classLoader))
        SerializationUtil.serialize(resultObject, result)
    }

    protected Result invokeCommandChain(CommandChain commandChain) {
        createInvoker(classLoader, commandChain).invokeAgainst(createContext(commandChain), null)
    }

    protected createInvoker(ClassLoader classLoader, CommandChain commandChain) {
        new CommandChainInvoker(classLoader, commandChain, resultFactory)
    }

    protected createContext(CommandChain commandChain) {
        contextFactory.getContext(commandChain)
    }

}