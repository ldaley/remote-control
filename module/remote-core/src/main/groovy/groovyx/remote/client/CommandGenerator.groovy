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
		this(Thread.currentThread().contextClassLoader)
	}
	
	CommandGenerator(ClassLoader classLoader) {
		this.classLoader = classLoader
	}
	
	/**
	 * For the given closure, generate a command object.
	 */
	Command generate(Map params, Closure closure) {
		def cloned = closure.clone()
		def root = getRootClosure(cloned)

		def supportsRoot = getSupportingClassesBytes(root.class)
		def usedClosuresBytes = params.usedClosures.collect { getClassBytes(it.class) }
		def supportsUsedClosures = params.usedClosures.collect { getSupportingClassesBytes(it.class) }.inject([]) { flattened, current -> flattened + current }
		
		new Command(
			instance: serializeInstance(cloned, root),
			root: getClassBytes(root.class),
			supports: supportsRoot + usedClosuresBytes + supportsUsedClosures
		)
	}
	
	/**
	 * Gets the generated closure instance that is underneath the potential layers of currying.
	 * 
	 * If the given closure is the root closure it is returned.
	 */
	protected Closure getRootClosure(Closure closure) {
		def root = closure
		while (root instanceof CurriedClosure) {
			root = root.owner
		}
		root
	}
	
	/**
	 * Gets the class definition bytes of any closures classes that are used by the
	 * given closure class.
	 * 
	 * @see InnerClosureClassDefinitionsFinder
	 */
	protected List<byte[]> getSupportingClassesBytes(Class closureClass) {
		new InnerClosureClassDefinitionsFinder(classLoader).find(closureClass)
	}
	
	/**
	 * Gets the class definition bytes for the given closure class.
	 */
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
	
	/**
	 * Serialises the closure taking care to remove the owner, thisObject and delegate.
	 * 
	 * The given closure may be curried which is why we need the "root" closure because
	 * it has the owner etc. 
	 * 
	 * closure and root will be the same object if closure is not curried.
	 * 
	 * @param closure the target closure to serialise
	 * @param root the actual generated closure that contains the implementation.
	 */
	protected byte[] serializeInstance(Closure closure, Closure root) {
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