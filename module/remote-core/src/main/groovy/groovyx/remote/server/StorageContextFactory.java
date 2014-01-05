package groovyx.remote.server;

import groovy.lang.Closure;
import groovyx.remote.CommandChain;
import groovyx.remote.groovy.server.ContextFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A context factory that returns Storage objects.
 *
 * Note that this class is abstract, but provides static methods to produce context factory instances.
 *
 * @see groovyx.remote.groovy.server.ContextFactory
 * @see groovyx.remote.server.Storage
 * @see groovyx.remote.server.Receiver
 */
public abstract class StorageContextFactory implements ContextFactory {

    /**
     * Creates a factory that generates storage objects with no initial values.
     */
    public static StorageContextFactory withEmptyStorage() {
        return new WithEmptyStorage();
    }

    /**
     * Creates a factory that uses the given map as a seed. That is, the seed will be copied to a new map to be used as the storage for each request for a context.
     */
    public static StorageContextFactory withSeed(Map<String, Object> seed) {
        return new WithSeed(seed);
    }

    /**
     * Creates a factory that calls the given generator closure to produce a map to be used as the storage. The generator is invoked for each request for a context factory. Given generator
     * implementations should take care to implement their own thread safety as they may be invoked by different threads at any given time.
     */
    public static StorageContextFactory withGenerator(Closure generator) {
        return new WithGenerator(generator);
    }

    private static class WithEmptyStorage extends StorageContextFactory {
        public Storage getContext(CommandChain chain) {
            return new Storage(new LinkedHashMap<String, Object>());
        }

    }

    private static class WithSeed extends StorageContextFactory {
        private final Map<String, Object> seed;

        public WithSeed(Map<String, Object> seed) {
            this.seed = new LinkedHashMap<String, Object>(seed);
        }

        public Storage getContext(CommandChain chain) {
            return new Storage(new LinkedHashMap<String, Object>(seed));
        }

        public final Map<String, Object> getSeed() {
            return seed;
        }
    }

    private static class WithGenerator extends StorageContextFactory {

        private final Closure generator;


        public WithGenerator(Closure generator) {
            this.generator = generator;
        }

        public final Closure getGenerator() {
            return generator;
        }


        public Storage getContext(CommandChain chain) {
            Object storage = generator.call(chain);
            if (storage == null) {
                storage = new LinkedHashMap();
            }

            if (storage instanceof Map) {
                @SuppressWarnings("unchecked") Map<String, Object> cast = (Map<String, Object>) storage;
                return new Storage(cast);
            } else {
                throw new IllegalArgumentException("The generator did not return a map");
            }

        }
    }
}
