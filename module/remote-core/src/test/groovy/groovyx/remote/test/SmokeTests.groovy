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

package groovyx.remote.test

import groovyx.remote.client.RemoteControl
import groovyx.remote.client.RemoteException
import groovyx.remote.client.UnserializableReturnException
import groovyx.remote.server.Receiver
import groovyx.remote.UnserializableException
import groovyx.remote.transport.local.LocalTransport
import groovyx.remote.util.FilteringClassLoader
import groovyx.remote.UnserializableExceptionException

/**
 * This test case shows how to use the remote control and some of it's limitations
 * with regard to serialisation and scope.
 *
 * The remote control object has an exec(Closure) method, and an alias for that as call(Closure).
 * The call(Closure) variant allows the use of the Groovy language feature where you can essentially
 * treat an object like a method, which is how “remote { … }” works below (i.e. it's really “remote.call { … }).
 * This doesn't always work though as you will see (due to Groovy), so sometimes you need to use .exec().
 *
 * Where we are passing a closure to the remote control object, that closure gets executed INSIDE the
 * application we are functionally testing, which may be in a different JVM on a completely different machine.
 * This works by sending the closure over HTTP to the application (which must have the remote-control plugin installed).
 *
 * An example use for this would be creating/deleting domain data inside your remote application for testing purposes.
 */
@SuppressWarnings("MethodCount")
class SmokeTests extends GroovyTestCase {

    // Used in a later test
    def anIvar = 2
    static RemoteControl remote

    void setUp() {
        if (remote == null) {
            // we need to create a classloader for the "server" side that cannot access
            // classes defined in this file.
            def thisClassLoader = getClass().classLoader
            def serverClassLoader = new FilteringClassLoader(thisClassLoader, "groovyx.remote.test")

            def receiver = new Receiver(serverClassLoader)
            def transport = new LocalTransport(receiver, thisClassLoader)

            remote = new RemoteControl(transport, thisClassLoader)
        }
    }

    /**
     * The result of the command run on the server is sent back and is returned
     */
    void testReturingValues() {
        assert remote { def a = 1; a + 1 } == 2
    }

    /**
     * Commands can contain other closures
     */
    void testWithInnerClosures() {
        assert [2, 3, 4] == remote {
            [1, 2, 3].collect { it + 1 }
        }
    }

    /**
     * If the command throwns an exception, we throw a RemoteException
     * client side with the actual exception instance that was thrown server
     * side as the cause
     */
    void testThrowingException() {
        def thrown = null
        try {
            remote { throw new Exception("bang!") }
        } catch (RemoteException e) {
            thrown = e.cause
            assert thrown.class == Exception
            assert thrown.message == "bang!"
        }

        assert thrown
    }

    /**
     * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
     */
    void testUnserialisableReturn() {
        shouldFail(UnserializableReturnException) {
            remote.exec { System.out }
        }
    }

    /**
     * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
     */
    void testNestedUnserialisableReturn() {
        shouldFail(UnserializableReturnException) {
            remote.exec { [m: [out: System.out]] }
        }
    }

    void testUnserializableExceptionsAreWrappedInUnserializableExceptionException() {
        shouldFailWithCause(UnserializableExceptionException) {
            remote.exec { throw new IncorrectClosureArgumentsException({ 1 }, [System.out], OutputStream) }
        }
    }

    void testUnserializableExceptionWithCause() {
        def cause = null
        try {
            remote.exec {
                def e = new UnserializableException()
                e.initCause(new Exception('cause foo'))
                throw e
            }
        } catch (RemoteException e) {
            assert e.cause.class == UnserializableExceptionException // also
            assert e.cause.message == "wrapped unserializable exception: class = groovyx.remote.UnserializableException, message = \"null\""
            assert e.cause.cause.class == UnserializableExceptionException // also
            assert e.cause.cause.message == "wrapped unserializable exception: class = java.lang.Exception, message = \"cause foo\""
        }
    }

    void testCanSpecifyToUseNullIfReturnWasUnserializable() {
        remote.useNullIfResultWasUnserializable = true
        try {
            assert remote.exec { System.out } == null
        } finally {
            remote.useNullIfResultWasUnserializable = false
        }
    }

    void testCanSpecifyToUseStringRepresentationIfReturnWasUnserializable() {
        remote.useStringRepresentationIfResultWasUnserializable = true
        try {
            assert remote.exec { System.out }.contains("Stream")
        } finally {
            remote.useStringRepresentationIfResultWasUnserializable = false
        }
    }

    /**
     * If the command returns an exception but does not throw it, we just return the exception
     */
    void testReturningException() {
        assert (remote { new Exception() }) instanceof Exception
    }

    /**
     * We can access lexical scope (within limits)
     */
    void testAccessingLexicalScope() {
        def a = 1
        assert remote { a + 1 } == 2
    }

    /**
     * Anything in lexical scope we access must be serialisable
     */
    void testAccessingNonSerializableLexicalScope() {
        def a = System.out
        shouldFail(NotSerializableException) {
            remote.exec { a }
        }
    }

    /**
     * Owner ivars can't be accessed because they aren't really lexical
     * so get treated as bean names from the app context
     */
    void testAccessingIvar() {
        def thrown
        try {
            remote { anIvar * 2 }
        } catch (RemoteException e) {
            thrown = e.cause
            assert thrown instanceof MissingPropertyException
        }

        assert thrown
    }

    /**
     * We can pass curried commands
     */
    void testCurryingCommands() {
        def command = { it + 2 }
        assert remote.exec(command.curry(2)) == 4
    }

    /**
     * We can curry a command as many times as we need to
     */
    void testCurryingCommandsMoreThanOnce() {
        def command = { a, b -> a + b }
        def curry1 = command.curry(1)
        def curry2 = curry1.curry(1)

        assert remote.exec(curry2) == 2
    }

    /**
     * Like everything else, currying args must be serialisable
     */
    void testCurryingArgsMustBeSerializable() {
        shouldFail(NotSerializableException) {
            remote.exec({ it }.curry(System.out))
        }
    }

    /**
     * Closures defined outside of the exec closures can be used inside of them if only the closures defined outside are passed as contextClosure option. Useful when creating DSLs.
     */
    void testPassingUsedClosures() {
        def contextClosure = { 1 }
        assert remote.exec(usedClosures: [contextClosure]) { contextClosure() + 1 } == 2
    }

    void testPassingUsedClosuresWithInnerClosures() {
        def contextClosure = { (1..3).inject(0) { sum, value -> sum + value } }
        assert remote.exec(usedClosures: [contextClosure]) { contextClosure() } == 6
    }

    void testPassingUsedClosuresThatAccessADelegate() {
        def contextClosure = { size() }
        assert remote.exec(usedClosures: [contextClosure]) {
            contextClosure.setProperty('delegate', 'some text')
            contextClosure()
        } == 9
    }

    void testPassingWrongTypeInUsedClosures() {
        assert shouldFail(IllegalArgumentException) {
            remote.exec(usedClosures: {}) {}
        } == "'usedClosures' argument must be iterable"
    }

    void testPassingUnknownOption() {
        assert shouldFail(IllegalArgumentException) {
            remote.exec(unknown: {}) {}
        } == "Unknown option 'unknown'"
    }

    /**
     * Any classes referenced have to be available in the remote app,
     * and any classes defined in tests ARE NOT.
     */
    void testCannotReferToClassesNotInTheApp() {
        def a = new GroovyClassLoader().parseClass("class T implements Serializable {}").newInstance()
        shouldFailWithCause(ClassNotFoundException) {
            remote.exec {
                a
            }
        }
    }

    /**
     * Variation of above, but yields a different error.
     */
    static class Inner {}

    void testCannotInstantiateClassesNotInTheApp() {
        shouldFailWithCause(NoClassDefFoundError) {
            remote.exec { new Inner() }
        }
    }

    /**
     * Multiple commands can be sent, the return value of the previous
     * command is passed to the next command as it's single argument
     */
    void testCommandChaining() {
        remote.exec({ 1 }, { it + 1 }) { it + 1 } == 3
    }

    /**
     * The delegate of commands is like a map and can store properties.
     */
    void testCanUseDelegateStorageAlongChain() {
        remote.exec(
                { num = 1 },
                { num = num + 1 }
        ) { num + 1 } == 3
    }

    /**
     * Trying to access a property that is not existing in the delegate
     * causes a MissingPropertyException
     */
    void testAccessingNonExistantPropertyFromDelegateCausesMPE() {
        shouldFailWithCause(MissingPropertyException) {
            remote.exec { iDontExist == true }
        }
    }

    void testCanSetProperties() {
        remote.exec { new GregorianCalendar().time = new Date() }
    }

    void testCanCallMethodsDynamicaly() {
        def methodName = "setTime"
        remote.exec { new GregorianCalendar()."$methodName"(new Date()) } != null
    }

    void testCanUseSpreadOperator() {
        remote.exec { [1, 2, 3]*.toString() } == ["1", "2", "3"]
    }

    void testCanUseSpreadMapOperator() {
        remote.exec { new HashMap(*: [a: 1, b: 2]) }
    }

    void testExternalLibrariesExecutingCodeOnRemote() {
        assert new RemoteCallingClass(remote).multiplyBy2OnRemote(3) == 6
    }

}