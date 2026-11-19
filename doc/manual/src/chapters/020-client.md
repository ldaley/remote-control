# Client Side

The following is an overview of the client side API.

## Creating a remote control

Instances of the `io.remotecontrol.client.RemoteControl` class are used by clients to send closures to servers. This class has the following constructors…

    RemoteControl(io.remotecontrol.client.Transport transport)
    RemoteControl(io.remotecontrol.client.Transport transport, ClassLoader classLoader)

The `Transport` object is responsible for providing the means of communication with the server. The different transport jars (such as `remote-transport-http`) provide implementations of this interface. 

The classloader given to the constructor is used to find the `.class` files of the closures to be remotely executed. This means that the classloader must have access to the closure classes. The constructor variant with no classloader parameter implicitly uses the current thread's context class loader.

> The remote execution mechanism works by sending the definition of the closure class to the server. It does this by finding the corresponding `.class` file for the closure on the class path. This means that there must be a `.class` file for the closure on the class path for the closure to be able to be remotely executed. Closure's whose class has been generated dynamically at runtime are currently not supported.

## Execution

Remote control objects provide the following method for executing closures remotely…

    def exec(Closure[] commands)

It takes multiple closures to facilitate command *chaining* which will be discussed later.

Given a `RemoteControl` object named `remote`, the following would fetch the date/time on the server.

    def serverDate = remote.exec { new Date() }

This will cause the given closure to be sent to the server (via the remote's `Transport` object) and executed there. The result of the closure is then sent back to the client and returned as the return of the `exec()` method call.

### Closure delegate, owner and thisObject

When the closure is sent to the server, its `delegate`, `owner` and `thisObject` are all set to `null`. This is done to minimise what needs to be serialised and to reduce issues any of these objects being unserialisable or having references to objects that are unserialisable.

However, when the closure is executed on the server side its delegate is set to what is called the command *context*. See the section on [contexts][contexts] for more information.

### Chaining

Muliple commands can be *chained* together. They are executed serially in order, with the result of the previous command being passed as the only argument to the next command in the chain. The only exception to this is when the next closure does not take any arguments. In this case the result of the previous command is simply discarded.

    assert remote.exec({ 1 }, { it + 1 }, { it + 1 }) == 3

### Currying

It's also possible to send curried closures.

    assert remote.exec({ it + 2 }.curry(2)) == 4

You can also used curried closures in chains.

    assert remote.exec({ 2 }, { num, previous -> num + previous }.curry(10)) == 12

Curry parameters must be serializable. If they are not, a `java.io.NotSerializableException` will be thrown on the client side.

### Inner Closures

You can use closures inside the command closure.

    remote.exec {
        def total = 0
        [1, 2, 3, 4].each {
            total += it
        }
    }

### Passing additional closures to the server side

When using the library you have to be aware that only the closure defined in `exec` call and closures defined within that closure are serialized and passed to the server side. Sometimes you might want to define closures outside of the `exec` method call and then execute them on the server side. This is especially useful when creating DSL-like calls that get executed on the remote. You can use `usedClosures` option in the `exec` call for that.

Given a delegate class and a dsl entry-point:

    class DslDelegate {
        int value

        void startingWith(int value) {
            this.value = value
        }

        void add(int added) {
            value += added
        }

        void subtract(int subtracted) {
            value -= subtracted
        }

        void divideBy(int dividedBy) {
            value /= dividedBy
        }
    }

    void dsl(Closure closure) {
        remote.exec(usedClosures: [closure]) {
            def dslDelegate = new DslDelegate()
            closure.setDelegate(dslDelegate)
            closure.call()
            dslDelegate.value
        }
    }

The following will pass:

    assert dsl {
        startingWith 8
        add 1
        subtract 3
        divideBy 3
    } == 2



### Remote Exceptions

Exceptions thrown on the server are captured and returned to the client where they are wrapped in a `io.remotecontrol.client.RemoteException` and thrown.
    
    try {
        remote.exec {
            throw new IllegalStateException("bang!")
        }
    } catch (io.remotecontrol.client.RemoteException e) {
        def remoteException = e.cause
        assert remoteException instanceof IllegalStateException
        assert remoteException.message == "bang!"
    }

If the remote exception was not able to be serialized, it will be *wrapped* in a `io.remotecontrol.UnserializableExceptionException`. This exception assumes the stack trace of the real exception and constructs a message based on the class and message of the real exception.

### Unserializable Return Values

If the return value of the last command in the chain is not `Serializable`, a `io.remotecontrol.client.UnserializableReturnException` is thrown. The `toString()` representation of the unserializable object is available as the `stringRepresentation` property on the exception.

There are two flags that can be set on remote control objects to augment this behaviour (both default to false)

#### useNullIfResultWasUnserializable

If this `boolean` property is set to `true`, `null` will be returned to the client if the result of the command on the server was a value that could not be serialized.

#### useStringRepresentationIfResultWasUnserializable

If this `boolean` property is set to `true`, the `toString()` value of the result of the command on the server will be returned to the client if it could not be serialized.

### Accessing Lexical Scope

Command closures can access lexically scoped variables, as long as they are serializable.

    def i = 10
    assert remote.exec { i + 5 } == 15

If something is accessed from lexical scoped that is not serializable, a `java.io.NotSerializableException` will be thrown on the client side.

### All Classes Must Be Available To The Server

With the exception of the command closure and any closures defined inside it, any classes used inside the command must be available to the server. If a command closure does use a class that is not available to the server, either a `java.lang.ClassNotFoundException` or `java.lang.NoClassDefFoundError` (depending on how the class is used) will be thrown on the server (which results in a `io.remotecontrol.client.RemoteException` being thrown on the client).

This means that closures defined outside of the command closure (that are not available to the server) cannot be used even if they are in lexical scope. This means the following will not work.

    def double = { it + 2 }
    remote.exec { double(10) }

### Caveats

Apart from the caveats mentioned above such as serializability and scoping, there are also certain things that do not work due to the current Groovy implementation. These may change in later versions of Groovy.

#### Setting Properties

Property assignment on objects does not currently work consistently. That is, the following may not work...

    def a = [:]
    remote.exec { a.num = 10 }

Use of property assignment creates a reference to the object that instantiated the closure, which is likely to not be serializable, resulting in a `java.io.NotSerializableException` when the command is serialized.

The workaround is either to use a method (such as a setter), or to use the metaclass set property method.

    def a = [:]
    remote.exec { a.getMetaClass().setProperty('num', 10) }

#### Invoking Methods Dynamically

Using the dynamic method invocation syntax has the same problem as property assigment. Which means the following will not work...

    def method = "newInstance"
    remote.exec { Object."$method"() }

The workaround is to use the Groovy `invokeMethod()` method.

    def method = "newInstance"
    remote.exec { Object.invokeMethod(method) }
