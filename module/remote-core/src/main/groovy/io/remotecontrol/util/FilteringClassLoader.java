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

package io.remotecontrol.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

public class FilteringClassLoader extends ClassLoader {

    private final String[] blockedPackages;

    public FilteringClassLoader(ClassLoader parent, String... blockedPackages) {
        super(parent);
        this.blockedPackages = blockedPackages;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> cl;
        try {
            cl = super.loadClass(name, false);
        } catch (NoClassDefFoundError e) {
            if (classAllowed(name)) {
                throw e;
            }
            // The class isn't visible
            throw new ClassNotFoundException(String.format("%s not found.", name));
        }

        if (!allowed(cl)) {
            throw new ClassNotFoundException(String.format("%s not found.", cl.getName()));
        }
        if (resolve) {
            resolveClass(cl);
        }

        return cl;
    }

    @Override
    protected Package getPackage(String name) {
        Package p = super.getPackage(name);
        if (p == null || !allowed(p)) {
            return null;
        }
        return p;
    }

    @Override
    protected Package[] getPackages() {
        List<Package> packages = new ArrayList<Package>();
        for (Package p : super.getPackages()) {
            if (allowed(p)) {
                packages.add(p);
            }
        }
        return packages.toArray(new Package[packages.size()]);
    }


    private boolean allowed(Package pkg) {
        for (String packagePrefix : blockedPackages) {
            if (pkg.getName().startsWith(packagePrefix)) {
                return false;
            }
        }
        return true;
    }

    private boolean allowed(Class<?> clazz) {
        return classAllowed(clazz.getName());
    }

    private boolean classAllowed(String className) {
        for (String packagePrefix : blockedPackages) {
            if (className.startsWith(packagePrefix)) {
                return false;
            }
        }
        return true;
    }

    private static class EmptyEnumeration<T> implements Enumeration<T> {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public T nextElement() {
            throw new NoSuchElementException();
        }
    }

}
