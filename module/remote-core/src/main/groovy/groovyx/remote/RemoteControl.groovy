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
package groovyx.remote

import groovyx.remote.client.*

/**
 * Executes closures in a server, communicating via a Transport.
 */
class RemoteControl {
	
	final Transport transport
	protected final CommandGenerator commandGenerator

	/**
	 * Creates a remote using the given transport and the current thread's contextClassLoader.
	 * 
	 * @see RemoteControl(Transport, ClassLoader)
	 */
	RemoteControl(Transport transport) {
		this(transport, Thread.currentThread().contextClassLoader)
	}

	/**
	 * Creates a remote using the given transport and the given classLoader.
	 */
	RemoteControl(Transport transport, ClassLoader classLoader) {
		this(transport, new CommandGenerator(classLoader))
	}
	
	/**
	 * Hook for subclasses to provide a custom command generator.
	 */
	protected RemoteControl(Transport transport, CommandGenerator commandGenerator) {
		this.transport = transport
		this.commandGenerator = commandGenerator
	}
	
	def exec(Closure[] commands) {
		processResult(sendCommandChain(generateCommandChain(commands)))
	}
	
	def call(Closure[] commands) {
		exec(commands)
	}
	
	protected CommandChain generateCommandChain(Closure[] commands) {
		new CommandChain(commands: commands.collect { commandGenerator.generate(it) })
	}
	
	protected Result sendCommandChain(CommandChain commandChain) {
		transport.send(commandChain)
	}
	
	protected processResult(Result result) {
		if (result.wasNull) {
			null
		} else if (result.wasUnserializable) {
			throw new UnserializableReturnException(result)
		} else if (result.wasThrown) {
			throw new RemoteException(result.value)
		} else {
			result.value
		}
	}
}
