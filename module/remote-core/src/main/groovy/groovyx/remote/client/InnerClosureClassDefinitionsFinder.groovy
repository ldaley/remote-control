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

import java.util.zip.ZipFile

class InnerClosureClassDefinitionsFinder {

    final URLClassLoader classLoader

    InnerClosureClassDefinitionsFinder(ClassLoader classLoader) {
        if (!(classLoader instanceof URLClassLoader)) {
            throw new IllegalArgumentException("Only URLClassLoaders are supported")
        }
        this.classLoader = classLoader
    }

    @SuppressWarnings('NestedBlockDepth')
    List<byte[]> find(Class<? extends Closure> clazz) {
        def classes = []
        def packageDirPath = toPackageDirPath(clazz)
        def innerClassPrefix = toInnerClassPrefix(clazz)
        def innerClassPrefixWithPackage = packageDirPath + "/" + innerClassPrefix

        for (loader in calculateEffectiveClassLoaderHierarchy()) {
            for (url in loader.getURLs()) {
                if (url.protocol != "file") {
                    // we only support searching the filesystem right not
                    continue
                }

                def root = new File(url.toURI())
                if (!root.exists()) {
                    // may have been removed, just skip it
                    continue
                }

                if (root.directory) {
                    def packageDir = packageDirPath ? new File(root, packageDirPath) : root
                    if (packageDir.exists()) {
                        for (classFileName in packageDir.list()) {
                            if (classFileName.startsWith(innerClassPrefix) && classFileName.endsWith(".class")) {
                                classes << new File(packageDir, classFileName).bytes
                            }
                        }
                    }
                } else if (root.name.endsWith(".jar") || root.name.endsWith(".zip")) {
                    def jarFile = new ZipFile(root)
                    def packageDir = packageDirPath ? jarFile.getEntry(packageDirPath) : root

                    if (packageDir != null) {
                        for (entry in jarFile.entries()) {
                            def name = entry.name
                            if (name.startsWith(innerClassPrefixWithPackage) && name.endsWith(".class")) {
                                classes << jarFile.getInputStream(name).bytes
                            }
                        }
                    }
                }
            }
        }

        classes
    }

    protected calculateEffectiveClassLoaderHierarchy() {
        def hierarchy = []
        def current = classLoader
        while (current != null && current instanceof URLClassLoader) {
            hierarchy << current
            current = current.parent
        }
        hierarchy
    }

    protected toPackageDirPath(Class clazz) {
        clazz.package?.name?.replace('.', '/')
    }

    protected toInnerClassPrefix(Class clazz) {
        def packageName = clazz.package?.name
        def fixedName = clazz.name.replace('$_$', '$_') + "_"
        if (packageName) {
            fixedName - "${packageName}."
        } else {
            fixedName
        }
    }

}