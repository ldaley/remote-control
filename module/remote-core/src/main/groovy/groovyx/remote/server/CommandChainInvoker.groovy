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

import groovyx.remote.*
import groovyx.remote.result.ResultFactory

class CommandChainInvoker {
	
	final ClassLoader parentLoader
	final CommandChain commandChain
    private final ResultFactory resultFactory

    CommandChainInvoker(ClassLoader parentLoader, CommandChain commandChain, ResultFactory resultFactory) {
        this.resultFactory = resultFactory
        this.parentLoader = parentLoader
		this.commandChain = commandChain
	}
	
	groovyx.remote.result.Result invokeAgainst(delegate, firstArg = null) {
		def arg = firstArg
		def lastResult = null
		def lastCommand = commandChain.commands.last()
		
		for (command in commandChain.commands) {
            def invoker = createInvoker(parentLoader, command)
            try {
                lastResult = invoker.invokeAgainst(delegate, arg)
            } catch (Throwable throwable) {
                return resultFactory.forThrown(throwable)
            }
			
			if (command != lastCommand) {
                arg = lastResult
			}
		}
		
		resultFactory.forValue(lastResult)
	}

	protected createInvoker(ClassLoader loader, Command command) {
		new CommandInvoker(parentLoader, command)
	}
}