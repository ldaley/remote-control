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
import spock.lang.Shared
import spock.lang.Specification

class CommandGeneratorSpec extends Specification {

	@Shared
	Closure closureSizeZero = { -> "123" }
	@Shared
	Closure closureSizeOne  = { -> def c = { -> "123" } }
	@Shared
	Closure closureSizeFour = { -> def c = { -> def a = { -> def b = { -> } } }; def d = { -> } }

	def generator = new ClosureCommandGenerator(this.getClass().classLoader)

	def "support size"() {
		expect:
		generator.generate(new RawClosureCommand(command, Collections.emptyList())).supports.size() == size
		where:
		command          | size
		closureSizeZero  | 0
		closureSizeOne   | 1
		closureSizeFour  | 4
	}

}
