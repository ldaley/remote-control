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

import groovyx.remote.Command
import org.codehaus.groovy.runtime.CurriedClosure

/**
 * Generates command objects from closures.
 */
class CommandGenerator {

	final ClassLoader classLoader

	CommandGenerator() {
		this(null)
	}
	
	CommandGenerator(ClassLoader classLoader) {
		this.classLoader = classLoader ?: Thread.currentThread().contextClassLoader
	}
	
	Command generate(Closure closure) {
		def cloned = closure.clone()
		def root = getRootClosure(cloned)
		
		new Command(
			instance: serializeInstance(cloned, root),
			root: getClassBytes(root.class),
			supports: getSupportingClassesBytes(root.class)
		)
	}
	
	/**
	 * Gets the generated closure instance that is underneath the potential layers of currying.
	 * 
	 * If the given cls
	 */
	protected Closure getRootClosure(Closure closure) {
		def root = closure
		while (root instanceof CurriedClosure) {
			root = root.owner
		}
		root
	}
	
	protected List<byte[]> getSupportingClassesBytes(Class closureClass) {
		new InnerClosureClassDefinitionsFinder(classLoader).find(closureClass)
	}
	
	protected byte[] getClassBytes(Class closureClass) {
		def classFileResource = classLoader.findResource(getClassFileName(closureClass))
		if (classFileResource == null) {
			throw new IllegalStateException("Could not find class file for class ${closureClass}")
		}
		
		new File(classFileResource.file).bytes
	}
	
	protected String getClassFileName(Class closureClass) {
		closureClass.name.replace('.', '/') + ".class"
	}
	
	protected serializeInstance(Closure closure, Closure root) {
		Closure.metaClass.setAttribute(root, 'owner', null)
		Closure.metaClass.setAttribute(root, 'thisObject', null)
		root.delegate = null
		
		def baos = new ByteArrayOutputStream()
		def oos = new ObjectOutputStream(baos)
		oos.writeObject(closure)
		oos.close()
		
		baos.toByteArray()
	}
	
}