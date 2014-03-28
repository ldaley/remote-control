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
package io.remotecontrol.client

import io.remotecontrol.groovy.client.ClosureCommandGenerator
import io.remotecontrol.groovy.client.RawClosureCommand
import spock.lang.Specification

class CommandGeneratorSpec extends Specification {

	def generator = new ClosureCommandGenerator(this.getClass().classLoader)
	
	def "support size"(Closure<?> command, int size) {
		expect:
		generator.generate(new RawClosureCommand(command, Collections.emptyList())).supports.size() == size
		where:
		command                                                              | size
		{ -> "123" }                                                         | 0
		{ -> def c = { -> "123" } }                                          | 1
		{ -> def c = { -> def a = { -> def b = { -> } } }; def d = { -> } }  | 4
	}

}