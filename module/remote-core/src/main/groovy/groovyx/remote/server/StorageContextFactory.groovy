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

/**
 * A context factory that returns Storage objects.
 * 
 * Note that this class is abstract, but provides static methods to
 * produce context factory instances.
 * 
 * @see ContextFactory
 * @see Storage
 * @see Receiver
 */
abstract class StorageContextFactory implements ContextFactory {
	
	/**
	 * Creates a factory that generates storage objects with no initial values.
	 */
	static StorageContextFactory withEmptyStorage() {
		new WithEmptyStorage()
	}

	static private class WithEmptyStorage extends StorageContextFactory {
		def getContext(CommandChain chain) {
			new Storage([:])
		}
	}
	
	/**
	 * Creates a factory that uses the given map as a seed. That is,
	 * the seed will be copied to a new map to be used as the storage
	 * for each request for a context.
	 */
	static StorageContextFactory withSeed(Map seed) {
		new WithSeed(seed)
	}
	
	static private class WithSeed extends StorageContextFactory {
		WithSeed(Map seed) {
			this.seed = seed
		}
		
		def getContext(CommandChain chain) {
			new Storage(new LinkedHashMap(seed))
		}
	}
	
	/**
	 * Creates a factory that calls the given generator closure to produce
	 * a map to be used as the storage. The generator is invoked for each 
	 * request for a context factory. Given generator implementations should
	 * take care to implement their own thread safety as they may be invoked
	 * by different threads at any given time.
	 */
	static StorageContextFactory withGenerator(Closure generator) {
		new WithGenerator(generator)
	}

	static private class WithGenerator extends StorageContextFactory {
		WithGenerator(Closure generator) {
			this.generator = generator
		}
		
		def getContext(CommandChain chain) {
			def storage = generator(chain)
			if (storage == null) {
				storage = [:]
			}
			
			if (storage instanceof Map) {
				new Storage(storage)
			} else {
				throw new IllegalArgumentException("The generator did not return a map")
			}
		}
	}
	
}