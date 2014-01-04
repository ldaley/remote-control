package groovyx.remote.server;

import groovy.lang.Closure;
import groovyx.remote.CommandChain;
import groovyx.remote.SerializationUtil;
import groovyx.remote.result.Result;
import groovyx.remote.result.ResultFactory;
import groovyx.remote.result.impl.DefaultResultFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * Receives a serialised command chain as an input stream to be unserialised and executed, then writes the serialised result to an output stream.
 */
public class Receiver {

    private final ClassLoader classLoader;
    private final ContextFactory contextFactory;
    private final ResultFactory resultFactory;

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see groovyx.remote.server.ContextFactory
     */
    public Receiver(ClassLoader classLoader, ContextFactory contextFactory, ResultFactory resultFactory) {
        this.classLoader = classLoader;
        this.contextFactory = contextFactory;
        this.resultFactory = resultFactory;
    }

    /**
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see groovyx.remote.server.ContextFactory
     */
    public Receiver(ClassLoader classLoader, ContextFactory contextFactory) {
        this(classLoader, contextFactory, new DefaultResultFactory());
    }

    /**
     * Creates a receiever that uses the current threads context class loader and the given contextFactory.
     *
     * @param contextFactory the context factory to use to create contexts for command chains
     * @see groovyx.remote.server.Receiver ( ClassLoader , ContextFactory )
     */
    public Receiver(ContextFactory contextFactory) {
        this(Thread.currentThread().getContextClassLoader(), contextFactory);
    }

    /**
     * Implicitly creates a StorageContextFactory that uses no initial values.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @see groovyx.remote.server.StorageContextFactory#withEmptyStorage()
     */
    public Receiver(ClassLoader classLoader) {
        this(classLoader, StorageContextFactory.withEmptyStorage());
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory that uses no initial values.
     *
     * @see groovyx.remote.server.Receiver ( ClassLoader )
     */
    public Receiver() {
        this(Thread.currentThread().getContextClassLoader());
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given map as a seed.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @param contextStorageSeed the seed for the storage
     * @see groovyx.remote.server.StorageContextFactory#withSeed(Map)
     */
    public Receiver(ClassLoader classLoader, Map contextStorageSeed) {
        this(classLoader, StorageContextFactory.withSeed(contextStorageSeed));
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory with the given map as the seed.
     *
     * @param contextStorageSeed the seed for the storage
     * @see groovyx.remote.server.Receiver ( ClassLoader , Map )
     */
    public Receiver(Map contextStorageSeed) {
        this(Thread.currentThread().getContextClassLoader(), contextStorageSeed);
    }

    /**
     * Implicitly creates a StorageContextFactory that uses the given closure as a generator.
     *
     * @param classLoader the class loader that will be used when unserialising the command chain
     * @see groovyx.remote.server.StorageContextFactory#withGenerator(Closure)
     */
    public Receiver(ClassLoader classLoader, Closure contextStorageGenerator) {
        this(classLoader, StorageContextFactory.withGenerator(contextStorageGenerator));
    }

    /**
     * Creates a receiever that uses the current threads context class loader and a StorageContextFactory with the given closure as the generator.
     *
     * @param contextStorageSeed the seed for the storage
     * @see groovyx.remote.server.Receiver ( ClassLoader , Closure )
     */
    public Receiver(Closure contextStorageSeed) {
        this(Thread.currentThread().getContextClassLoader(), contextStorageSeed);
    }

    /**
     * Executes a serialised command chain.
     *
     * @param input A stream containing a serialised CommandChain object.
     * @param output The stream that the Result object shall be written to.
     */
    public void execute(InputStream input, OutputStream output) throws IOException, ClassNotFoundException {
        Result resultObject = invokeCommandChain(SerializationUtil.deserialize(CommandChain.class, input, classLoader));
        SerializationUtil.serialize(resultObject, output);
    }

    protected Result invokeCommandChain(CommandChain commandChain) {
        return createInvoker(classLoader, commandChain).invokeAgainst(createContext(commandChain), null);
    }

    protected CommandChainInvoker createInvoker(ClassLoader classLoader, CommandChain commandChain) {
        return new CommandChainInvoker(classLoader, commandChain, resultFactory);
    }

    protected Object createContext(CommandChain commandChain) {
        return contextFactory.getContext(commandChain);
    }

}
