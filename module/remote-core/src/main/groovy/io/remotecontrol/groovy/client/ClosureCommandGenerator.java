/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.remotecontrol.groovy.client;

import groovy.lang.Closure;
import io.remotecontrol.SerializationUtil;
import io.remotecontrol.UnserializableCommandException;
import io.remotecontrol.client.CommandGenerator;
import io.remotecontrol.groovy.ClosureCommand;
import io.remotecontrol.groovy.ClosureUtil;
import io.remotecontrol.util.UnexpectedIOException;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.IOGroovyMethods;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

/**
 * Generates command objects from closures.
 */
public class ClosureCommandGenerator implements CommandGenerator<RawClosureCommand, ClosureCommand> {

    private final ClassLoader classLoader;

    public ClosureCommandGenerator() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public ClosureCommandGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Class<ClosureCommand> getCommandType() {
        return ClosureCommand.class;
    }

    /**
     * For the given closure, generate a command object.
     */
    public ClosureCommand generate(RawClosureCommand rawClosureCommand) {
        byte[] bytes;
        byte[] classBytes;
        List<byte[]> supports;
        Closure<?> cloned = (Closure<?>) rawClosureCommand.getRoot().clone();
        Closure<?> root = getRootClosure(cloned);
        bytes = serializeInstance((Closure) cloned, root);
        classBytes = getClassBytes(root.getClass());

        supports = new LinkedList<byte[]>(getSupportingClassesBytes(root.getClass()));

        List<Closure<?>> used = rawClosureCommand.getUsed();
        if (!used.isEmpty()) {
            for (Closure usedClosure : used) {
                supports.add(getClassBytes(usedClosure.getClass()));
                supports.addAll(getSupportingClassesBytes(usedClosure.getClass()));
            }
        }

        return new ClosureCommand(bytes, classBytes, supports);
    }

    /**
     * Gets the generated closure instance that is underneath the potential layers of currying.
     *
     * If the given closure is the root closure it is returned.
     */
    protected Closure getRootClosure(Closure closure) {
        Closure root = closure;
        while (root instanceof CurriedClosure) {
            root = ((Closure) (root.getOwner()));
        }

        return root;
    }

    /**
     * Gets the class definition bytes of any closures classes that are used by the given closure class.
     *
     * @see InnerClosureClassDefinitionsFinder
     */
    protected List<byte[]> getSupportingClassesBytes(Class<? extends Closure> closureClass) {
        try {
            return new InnerClosureClassDefinitionsFinder(classLoader).find(closureClass);
        } catch (IOException e) {
            throw new UnexpectedIOException("cannnot find inner closures of: " + closureClass.getName(), e);
        }
    }

    /**
     * Gets the class definition bytes for the given closure class.
     */
    protected byte[] getClassBytes(final Class<? extends Closure> closureClass) {
        String classFileName = getClassFileName(closureClass);
        URL classFileResource = classLoader.getResource(classFileName);
        if (classFileResource == null) {
            throw new IllegalStateException("Could not find class file for class " + String.valueOf(closureClass));
        }

        try {
            return IOGroovyMethods.getBytes(classFileResource.openStream());
        } catch (IOException e) {
            throw new UnexpectedIOException("reading class files", e);
        }
    }

    protected String getClassFileName(Class closureClass) {
        return closureClass.getName().replace(".", "/") + ".class";
    }

    /**
     * Serialises the closure taking care to remove the owner, thisObject and delegate.
     *
     * The given closure may be curried which is why we need the "root" closure because it has the owner etc.
     *
     * closure and root will be the same object if closure is not curried.
     *
     * @param closure the target closure to serialise
     * @param root the actual generated closure that contains the implementation.
     */
    protected byte[] serializeInstance(Closure closure, Closure root) {
        ClosureUtil.nullFields(root);
        try {
            return SerializationUtil.serialize(closure);
        } catch (NotSerializableException e) {
            throw new UnserializableCommandException("Unable to serialize closure: " + closure, e);
        }
    }

}
