package groovyx.remote.client;

import groovy.lang.Closure;
import groovyx.remote.Command;
import groovyx.remote.SerializationUtil;
import groovyx.remote.util.ClosureUtil;
import groovyx.remote.util.UnexpectedIOException;
import org.codehaus.groovy.runtime.CurriedClosure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.IOException;
import java.io.NotSerializableException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generates command objects from closures.
 */
public class CommandGenerator {

    private final ClassLoader classLoader;

    public CommandGenerator() {
        this(Thread.currentThread().getContextClassLoader());
    }

    public CommandGenerator(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }


    public final ClassLoader getClassLoader() {
        return classLoader;
    }

    /**
     * For the given closure, generate a command object.
     */
    public Command generate(Map<String, Object> params, Closure<?> closure) throws NotSerializableException {
        Closure<?> cloned = (Closure<?>) closure.clone();
        Closure<?> root = getRootClosure(cloned);
        byte[] bytes = serializeInstance((Closure) cloned, root);
        byte[] classBytes = getClassBytes(root.getClass());

        List<byte[]> supports = new LinkedList<byte[]>(getSupportingClassesBytes(root.getClass()));
        if (params.containsKey("usedClosures")) {
            @SuppressWarnings("unchecked")
            List<Closure<?>> usedClosures = (List<Closure<?>>) params.get("usedClosures");

            for (Closure usedClosure : usedClosures) {
                supports.add(getClassBytes(usedClosure.getClass()));
                supports.addAll(getSupportingClassesBytes(usedClosure.getClass()));
            }
        }

        return new Command(bytes, classBytes, supports);
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
     * @see groovyx.remote.client.InnerClosureClassDefinitionsFinder
     */
    protected List<byte[]> getSupportingClassesBytes(Class<? extends Closure> closureClass) {
        return new InnerClosureClassDefinitionsFinder(classLoader).find(closureClass);
    }

    /**
     * Gets the class definition bytes for the given closure class.
     */
    protected byte[] getClassBytes(final Class<? extends Closure> closureClass) {
        URL classFileResource = classLoader.getResource(getClassFileName(closureClass));
        if (classFileResource == null) {
            throw new IllegalStateException("Could not find class file for class " + String.valueOf(closureClass));
        }

        try {
            return DefaultGroovyMethods.getBytes(classFileResource);
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
    protected byte[] serializeInstance(Closure closure, Closure root) throws NotSerializableException {
        ClosureUtil.nullFields(root);
        return SerializationUtil.serialize(closure);
    }


}
