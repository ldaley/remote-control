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
 * A context factory produces what will be used as the delegate for
 * all commands in a command chain.
 */
interface ContextFactory {

	/**
	 * Produces a context to be used for the entire given chain.
	 */
	def getContext(CommandChain chain)

}