package groovyx.remote.client;

import groovy.lang.Closure;
import groovyx.remote.Command;
import groovyx.remote.CommandChain;
import groovyx.remote.result.*;
import groovyx.remote.util.ClosureUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Executes closures in a server, communicating via a Transport.
 */
public class RemoteControl {

    private final Transport transport;
    private final CommandGenerator commandGenerator;
    private final ClassLoader classLoader;

    private boolean useNullIfResultWasUnserializable = false;
    private boolean useStringRepresentationIfResultWasUnserializable = false;

    /**
     * Creates a remote using the given transport and the current thread's contextClassLoader.
     *
     * @see groovyx.remote.client.RemoteControl ( Transport , ClassLoader )
     */
    public RemoteControl(Transport transport) {
        this(transport, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Creates a remote using the given transport and the given classLoader.
     */
    public RemoteControl(Transport transport, ClassLoader classLoader) {
        this(transport, new CommandGenerator(classLoader), classLoader);
    }

    /**
     * Hook for subclasses to provide a custom command generator.
     */
    protected RemoteControl(Transport transport, CommandGenerator commandGenerator, ClassLoader classLoader) {
        this.transport = transport;
        this.commandGenerator = commandGenerator;
        this.classLoader = classLoader;
    }

    /**
     * If set, {@code null} will be used as the return value if the actual return value was not serializable.
     *
     * This prevents a UnserializableReturnException from being thrown.
     */
    public boolean isUseNullIfResultWasUnserializable() {
        return useNullIfResultWasUnserializable;
    }

    public void setUseNullIfResultWasUnserializable(boolean useNullIfResultWasUnserializable) {
        this.useNullIfResultWasUnserializable = useNullIfResultWasUnserializable;
    }

    /**
     * If set, the string representation of the actual return value will be used if the actual return value was not serializable.
     *
     * This prevents a UnserializableReturnException from being thrown.
     */
    public boolean isUseStringRepresentationIfResultWasUnserializable() {
        return useStringRepresentationIfResultWasUnserializable;
    }

    public void setUseStringRepresentationIfResultWasUnserializable(boolean useStringRepresentationIfResultWasUnserializable) {
        this.useStringRepresentationIfResultWasUnserializable = useStringRepresentationIfResultWasUnserializable;
    }

    public Object exec(Closure[] commands) throws IOException {
        return exec(new LinkedHashMap<String, Object>(), commands);
    }

    public Object exec(Map<String, ?> params, Closure[] commands) throws IOException {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(params);
        processExecParams(copy);
        CommandChain commandChain = generateCommandChain(copy, commands);
        Result result = sendCommandChain(commandChain);
        return processResult(result);
    }

    public Object call(Map<String, ?> params, Closure[] commands) throws IOException {
        return exec(params, commands);
    }

    public Object call(Closure[] commands) throws IOException {
        return exec(commands);
    }

    protected void processExecParams(Map<String, Object> params) {
        for (String key : params.keySet()) {
            if (key.equals("usedClosures")) {
                Object usedClosures = params.get(key);
                if (usedClosures instanceof Iterable) {
                    for (Object usedClosure : (Iterable<?>) usedClosures) {
                        if (usedClosure instanceof Closure) {
                            ClosureUtil.nullFields((Closure<?>) usedClosure);
                        } else {
                            throw new IllegalArgumentException("'usedClosures' elements must be closures");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("'usedClosures' argument must be iterable");
                }

            } else {
                throw new IllegalArgumentException("Unknown option '" + key + "'");
            }
        }
    }

    protected CommandChain generateCommandChain(final Map<String, Object> params, Closure<?>[] closures) throws IOException {
        List<Command> commands = new ArrayList<Command>(closures.length);
        for (Closure<?> closure : closures) {
            Command command = commandGenerator.generate(params, closure);
            commands.add(command);
        }
        return new CommandChain(commands);
    }

    protected Result sendCommandChain(CommandChain commandChain) throws IOException {
        return transport.send(commandChain);
    }

    protected Object processResult(Result result) {
        if (result instanceof NullResult) {
            return null;
        } else if (result instanceof ThrownResult) {
            ThrownResult thrownResult = (ThrownResult) result;
            throw new RemoteException(thrownResult.deserialize(classLoader));
        } else if (result instanceof UnserializableThrownResult) {
            UnserializableThrownResult unserializedThrownResult = (UnserializableThrownResult) result;
            throw new RemoteException(unserializedThrownResult.deserializeWrapper(classLoader));
        } else if (result instanceof UnserializableResult) {
            UnserializableResult unserializableResult = (UnserializableResult) result;
            if (useNullIfResultWasUnserializable) {
                return null;
            } else if (useStringRepresentationIfResultWasUnserializable) {
                return unserializableResult.getStringRepresentation();
            } else {
                throw new UnserializableReturnException(unserializableResult);
            }
        } else if (result instanceof SerializedResult) {
            SerializedResult serializedResult = (SerializedResult) result;
            return serializedResult.deserialize(classLoader);
        } else {
            throw new IllegalArgumentException("Unknown result type: " + result);
        }
    }

}
