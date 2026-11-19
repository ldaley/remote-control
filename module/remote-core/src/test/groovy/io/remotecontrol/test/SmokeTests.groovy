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

package io.remotecontrol.test

import io.remotecontrol.RemoteControlException
import io.remotecontrol.client.UnserializableResultStrategy
import io.remotecontrol.groovy.client.RemoteControl
import io.remotecontrol.client.RemoteException
import io.remotecontrol.client.UnserializableReturnException
import io.remotecontrol.groovy.server.ClosureReceiver
import io.remotecontrol.UnserializableException
import io.remotecontrol.transport.local.LocalTransport
import io.remotecontrol.util.FilteringClassLoader
import io.remotecontrol.UnserializableExceptionException
import io.remotecontrol.UnserializableCommandException
import spock.lang.Specification

/**
 * This test case shows how to use the remotecontrol control and some of it's limitations
 * with regard to serialisation and scope.
 *
 * The remotecontrol control object has an exec(Closure) method, and an alias for that as call(Closure).
 * The call(Closure) variant allows the use of the Groovy language feature where you can essentially
 * treat an object like a method, which is how “remotecontrol { … }” works below (i.e. it's really “remotecontrol.call { … }).
 * This doesn't always work though as you will see (due to Groovy), so sometimes you need to use .exec().
 *
 * Where we are passing a closure to the remotecontrol control object, that closure gets executed INSIDE the
 * application we are functionally testing, which may be in a different JVM on a completely different machine.
 * This works by sending the closure over HTTP to the application (which must have the remotecontrol-control plugin installed).
 *
 * An example use for this would be creating/deleting domain data inside your remotecontrol application for testing purposes.
 */
@SuppressWarnings("MethodCount")
class SmokeTests extends Specification {

    // Used in a later test
    def anIvar = 2

    def remote
    def transport
    def clientClassLoader

    void setup() {
        if (remote == null) {
            // we need to create a classloader for the "server" side that cannot access
            // classes defined in this file.
            clientClassLoader = getClass().classLoader

            def serverClassLoader = new FilteringClassLoader(clientClassLoader, "io.remotecontrol.test")
            def receiver = new ClosureReceiver(serverClassLoader)

            transport = new LocalTransport(receiver, clientClassLoader)
            remote = new RemoteControl(transport, clientClassLoader)
        }
    }

    /**
     * The result of the command run on the server is sent back and is returned
     */
     def "testReturingValues"() {
         expect:
         assert remote { def a = 1; a + 1 } == 2
    }

    /**
     * Commands can contain other closures
     */
    def "testWithInnerClosures"(){
        expect:
        assert [2, 3, 4] == remote {
            [1, 2, 3].collect { [it].collect { it + 1 }[0] }
        }
    }

    /**
     * If the command throwns an exception, we throw a RemoteException
     * client side with the actual exception instance that was thrown server
     * side as the cause
     */
    def "testThrowingException"() {
        expect:
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
    def "testUnserialisableReturn"() {
        when:
        remote.exec { System.out }

        then:
        thrown UnserializableReturnException
    }

    /**
     * If the command returns something that is unserialisable, we thrown an UnserializableReturnException
     */
    def "testNestedUnserialisableReturn"(){
        when:
        remote.exec { [m: [out: System.out]] }

        then:
        thrown UnserializableReturnException
    }

    def "testExceptionsAreWrappedInRemoteControlException"() {
        when:
        remote.exec { throw new IncorrectClosureArgumentsException({ 1 }, [System.out], OutputStream) }

        then:
        Exception x = thrown()
        x instanceof RemoteControlException
        x.getCause() instanceof UnserializableExceptionException
    }

    def "testUnserializableExceptionWithCause"() {
        expect:
        def cause = null
        try {
            remote.exec {
                def e = new UnserializableException()
                e.initCause(new Exception('cause foo'))
                throw e
            }
        } catch (RemoteException e) {
            assert e.cause.class == UnserializableExceptionException // also
            assert e.cause.message == "wrapped unserializable exception: class = io.remotecontrol.UnserializableException, message = \"null\""
            assert e.cause.cause.class == UnserializableExceptionException // also
            assert e.cause.cause.message == "wrapped unserializable exception: class = java.lang.Exception, message = \"cause foo\""
        }
    }

    def "testCanSpecifyToUseNullIfReturnWasUnserializable"() {
        given:
        remote = new RemoteControl(transport, UnserializableResultStrategy.NULL, clientClassLoader)
        expect:
        assert remote.exec { System.out } == null
    }

    def "testCanSpecifyToUseStringRepresentationIfReturnWasUnserializable"() {
        given:
        remote = new RemoteControl(transport, UnserializableResultStrategy.STRING, clientClassLoader)
        expect:
        assert remote.exec { System.out }.contains("Stream")
    }

    /**
     * If the command returns an exception but does not throw it, we just return the exception
     */
    def "testReturningException"() {
        expect:
        assert (remote { new Exception() }) instanceof Exception
    }

    /**
     * We can access lexical scope (within limits)
     */
    def "testAccessingLexicalScope"() {
        given:
        def a = 1
        expect:
        assert remote { a + 1 } == 2
    }

    /**
     * Anything in lexical scope we access must be serialisable
     */
    def "testAccessingNonSerializableLexicalScope"() {
        given:
        def a = System.out
        when:
        remote.exec { a }
        then:
        thrown UnserializableCommandException
    }

    /**
     * Owner ivars can't be accessed because they aren't really lexical
     * so get treated as bean names from the app context
     */
    def "testAccessingIvar"(){
        expect
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
    def "testCurryingCommands"() {
        given:
        def command = { it + 2 }
        expect:
        assert remote.exec(command.curry(2)) == 4
    }

    /**
     * We can curry a command as many times as we need to
     */
    def "testCurryingCommandsMoreThanOnce"() {
        given:
        def command = { a, b -> a + b }
        def curry1 = command.curry(1)
        def curry2 = curry1.curry(1)
        expect:
        assert remote.exec(curry2) == 2
    }

    /**
     * Like everything else, currying args must be serialisable
     */
    def "testCurryingArgsMustBeSerializable"() {
        when:
        remote.exec({ it }.curry(System.out))
        then:
        thrown UnserializableCommandException
    }

    /**
     * Closures defined outside of the exec closures can be used inside of them if only the closures defined outside are passed as contextClosure option. Useful when creating DSLs.
     */
    def "testPassingUsedClosures"(){
        given:
        def contextClosure = { 1 }
        expect:
        assert remote.exec(usedClosures: [contextClosure]) { contextClosure() + 1 } == 2
    }

    def "testPassingUsedClosuresWithInnerClosures"() {
        given:
        def contextClosure = { (1..3).inject(0) { sum, value -> sum + value } }
        expect:
        assert remote.exec(usedClosures: [contextClosure]) { contextClosure() } == 6
    }

    def "testPassingUsedClosuresThatAccessADelegate"() {
        expect:
        def contextClosure = { size() }
        assert remote.exec(usedClosures: [contextClosure]) {
            contextClosure.setProperty('delegate', 'some text')
            contextClosure()
        } == 9
    }

    def "testPassingWrongTypeInUsedClosures"() {
        when:
        remote.exec(usedClosures: {}) {}
        then:
        Exception x = thrown()
        x instanceof IllegalArgumentException
        x.getMessage() == "'usedClosures' argument must be iterable"
    }

    def "testPassingUnknownOption"() {
        when:
        remote.exec(unknown: {}) {}
        then:
        Exception x = thrown()
        x instanceof IllegalArgumentException
        x.getMessage() == "Unknown option 'unknown'"
    }

    /**
     * Any classes referenced have to be available in the remotecontrol app,
     * and any classes defined in tests ARE NOT.
     */
    def "testCannotReferToClassesNotInTheApp"() {
        given:
        def a = new GroovyClassLoader().parseClass("class T implements Serializable {}").newInstance()
        when:
        remote.exec { a }
        then:
        Exception x = thrown()
        x instanceof RemoteControlException
        x.getCause() instanceof RemoteControlException
        x.getCause().getMessage() == "Class not found on server (the command referenced a class that the server does not have)"
    }

    /**
     * Variation of above, but yields a different error.
     */
    @SuppressWarnings("EmptyClass")
    static class Inner {}

    def "testCannotInstantiateClassesNotInTheApp"() {
        when:
        remote.exec { new Inner() }
        then:
        Exception x = thrown()
        x.getCause() instanceof NoClassDefFoundError
    }

    /**
     * Multiple commands can be sent, the return value of the previous
     * command is passed to the next command as it's single argument
     */
    def "testCommandChaining"() {
        expect:
        remote.exec({ 1 }, { it + 1 }) { it + 1 } == 3
    }

    /**
     * The delegate of commands is like a map and can store properties.
     */
    def "testCanUseDelegateStorageAlongChain"() {
        expect:
        remote.exec(
            { num = 1 },
            { num = num + 1 }
        ) { num + 1 } == 3
    }

    /**
     * Trying to access a property that is not existing in the delegate
     * causes a MissingPropertyException
     */
    def "testAccessingNonExistantPropertyFromDelegateCausesMPE"() {
        when:
        remote.exec { iDontExist == true }
        then:
        Exception x = thrown()
        x.getCause() instanceof MissingPropertyException
    }

    def "testCanSetProperties"() {
        expect:
        remote.exec { new GregorianCalendar().time = new Date() }
    }

    def "testCanCallMethodsDynamicaly"() {
        given:
        def methodName = "isLeapYear"
        expect:
        remote.exec { new GregorianCalendar()."$methodName"(2020) } == true
        remote.exec { new GregorianCalendar()."$methodName"(2021) } == false
    }

    def "testCanUseSpreadOperator"() {
        expect:
        remote.exec { [1, 2, 3]*.toString() } == ["1", "2", "3"]
    }

    def "testCanUseSpreadMapOperator"() {
        expect:
        remote.exec { new HashMap(*: [a: 1, b: 2]) }
    }

    def "testExternalLibrariesExecutingCodeOnRemote"(){
        expect:
        assert new RemoteCallingClass(remote).multiplyBy2OnRemote(3) == 6
    }

}
