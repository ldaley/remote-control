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
package groovyx.remote.client

import groovyx.remote.*

/**
 * Executes closures in a server, communicating via a Transport.
 */
class RemoteControl {

	final Transport transport
	protected final CommandGenerator commandGenerator

	/**
	 * If set, {@code null} will be used as the return value if the actual return value was not serializable.
	 *
	 * This prevents a UnserializableReturnException from being thrown.
	 */
	boolean useNullIfResultWasUnserializable = false

	/**
	 * If set, the string representation of the actual return value will be used if the actual return value was not serializable.
	 *
	 * This prevents a UnserializableReturnException from being thrown.
	 */
	boolean useStringRepresentationIfResultWasUnserializable = false

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
		exec([:], commands)
	}

	def exec(Map params, Closure[] commands) {
		processExecParams(params)
		processResult(sendCommandChain(generateCommandChain(params, commands)))
	}

	def call(Map params, Closure[] commands) {
		exec(params, commands)
	}

	def call(Closure[] commands) {
		exec(commands)
	}

	protected void processExecParams(Map params) {
		params.usedClosures.each {
            Closure.metaClass.setAttribute(it, 'owner', null)
            Closure.metaClass.setAttribute(it, 'thisObject', null)
            it.delegate = null
            it.resolveStrategy = Closure.DELEGATE_ONLY
        }
	}

	protected CommandChain generateCommandChain(Map params, Closure[] commands) {
		new CommandChain(commands: commands.collect { commandGenerator.generate(params, it) })
	}

	protected Result sendCommandChain(CommandChain commandChain) {
		transport.send(commandChain)
	}

	protected processResult(Result result) {
		if (result.wasNull) {
			null
		} else if (result.wasUnserializable) {
			if (useNullIfResultWasUnserializable) {
				null
			} else if (useStringRepresentationIfResultWasUnserializable) {
				result.stringRepresentation
			} else {
				throw new UnserializableReturnException(result)
			}
		} else if (result.thrown) {
			throw new RemoteException(result.thrown)
		} else {
			result.value
		}
	}
}
