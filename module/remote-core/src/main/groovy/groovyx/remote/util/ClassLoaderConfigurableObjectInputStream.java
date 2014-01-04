package groovyx.remote.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * Allows us to hydrate objects with a custom classloader.
 */
public class ClassLoaderConfigurableObjectInputStream extends ObjectInputStream {

    private final ClassLoader classLoader;

    public ClassLoaderConfigurableObjectInputStream(ClassLoader classLoader, InputStream input) throws IOException {
        super(input);
        this.classLoader = classLoader;
    }

    public Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
        try {
            return classLoader.loadClass(desc.getName());
        } catch (ClassNotFoundException e) {
            return super.resolveClass(desc);
        }
    }

    public final ClassLoader getClassLoader() {
        return classLoader;
    }

}
