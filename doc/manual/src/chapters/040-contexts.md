# Command Context

The receiver establishes a “context” for the execution of the command (i.e. the delegate of the closure), which is controlled by the receiver's [ContextFactory][contextfactory-api] instance.

The core library provides the [StorageContextFactory][storagecontextfactory-api] implementation that creates [Storage][storage-api] objects, that can be used for general key/value storage. It allows the setting of arbitrary values, but throws a `MissingPropertyException` when a request is made for a value that has not been set. Storage objects are created with a map that is used for the actual storage, so can be used to “seed” the context.

The [StorageContextFactory][storagecontextfactory-api] is created with either a `Map` that is used as the seed for each storage object, or a closure that produces Map objects to be used as the seed for each storage object.

The [Receiver][receiver-api] class has convenience constructors that take this map or closure and implicitly create a StorageContextFactory to use.