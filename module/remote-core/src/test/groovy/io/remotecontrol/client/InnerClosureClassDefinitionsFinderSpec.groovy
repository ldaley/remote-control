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

import io.remotecontrol.groovy.client.InnerClosureClassDefinitionsFinder
import spock.lang.*

class InnerClosureClassDefinitionsFinderSpec extends Specification {

	protected createFinder(String[] urls) {
		new InnerClosureClassDefinitionsFinder(new URLClassLoader(urls.collect { new URL(it) } as URL[]))
	}
	
	def "non existant class path entries are ignored"() {
		given:
		def finder = createFinder("file:///idontexist.zip", "file:///idontexistdirectory")
		
		when:
		finder.find({ 1 }.class)
		
		then:
		notThrown Exception
	}
	
	def "non file classpath entries are ignored"() {
		given:
		def finder = createFinder("http://google.com")
		
		when:
		finder.find({ 1 }.class)
		
		then:
		notThrown Exception
	}
	
}
