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

package io.remotecontrol.server;

import groovy.lang.GroovyClassLoader;
import io.remotecontrol.SerializationUtil;
import io.remotecontrol.util.UnexpectedIOException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The class loader that closure classes received from the client are defined in.
 * <p>
 * Groovy 4+ walks from a closure class to its enclosing class via {@link Class#getEnclosingClass()}
 * when dispatching calls and property access inside the closure (see GROOVY-2433 and GROOVY-11128).
 * The enclosing class of a remote closure is a client-side class (e.g. a test class), which by design
 * does not exist on the server, so that walk fails with {@link NoClassDefFoundError}.
 * <p>
 * To keep remote closures working, when a class cannot be found and its name is the enclosing
 * ({@code Outer$...}) prefix of a closure class received from the client, an empty, abstract,
 * synthetic placeholder class is defined in its place. Any other missing class still fails as before.
 * A new instance is created per command, so placeholders never outlive the command.
 */
public class CommandClassLoader extends GroovyClassLoader {

    private static final int ACC_PUBLIC = 0x0001;
    private static final int ACC_SUPER = 0x0020;
    private static final int ACC_ABSTRACT = 0x0400;
    private static final int ACC_SYNTHETIC = 0x1000;

    private final Set<String> commandClassNames = ConcurrentHashMap.newKeySet();

    public CommandClassLoader(ClassLoader parent) {
        super(parent);
    }

    /**
     * Defines a class received from the client.
     */
    public Class<?> defineCommandClass(byte[] bytes) {
        Class<?> clazz = SerializationUtil.defineClass(this, bytes);
        commandClassNames.add(clazz.getName());
        return clazz;
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            return super.loadClass(name, resolve);
        } catch (ClassNotFoundException e) {
            if (!isEnclosingClassOfCommandClass(name)) {
                throw e;
            }
            synchronized (getClassLoadingLock(name)) {
                Class<?> loaded = findLoadedClass(name);
                if (loaded != null) {
                    return loaded;
                }
                byte[] bytes = placeholderClassBytes(name);
                return defineClass(name, bytes, 0, bytes.length);
            }
        }
    }

    private boolean isEnclosingClassOfCommandClass(String name) {
        String prefix = name + "$";
        for (String commandClassName : commandClassNames) {
            if (commandClassName.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Class file bytes for {@code public abstract synthetic class <name> extends java.lang.Object} with no members.
     */
    private static byte[] placeholderClassBytes(String name) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DataOutputStream out = new DataOutputStream(bytes)) {
            out.writeInt(0xCAFEBABE);
            out.writeShort(0);                 // minor version
            out.writeShort(52);                // major version (Java 8)
            out.writeShort(5);                 // constant pool count (entries 1-4)
            out.writeByte(1);                  // #1 Utf8: this class name
            out.writeUTF(name.replace('.', '/'));
            out.writeByte(7);                  // #2 Class #1
            out.writeShort(1);
            out.writeByte(1);                  // #3 Utf8: super class name
            out.writeUTF("java/lang/Object");
            out.writeByte(7);                  // #4 Class #3
            out.writeShort(3);
            out.writeShort(ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT | ACC_SYNTHETIC);
            out.writeShort(2);                 // this class
            out.writeShort(4);                 // super class
            out.writeShort(0);                 // interfaces
            out.writeShort(0);                 // fields
            out.writeShort(0);                 // methods
            out.writeShort(0);                 // attributes
        } catch (IOException e) {
            throw new UnexpectedIOException("generating placeholder class " + name, e);
        }
        return bytes.toByteArray();
    }
}
