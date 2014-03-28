package io.remotecontrol.groovy.client;

import groovy.lang.Closure;
import io.remotecontrol.CommandChain;
import io.remotecontrol.client.CommandGenerator;
import io.remotecontrol.client.RemoteControlSupport;
import io.remotecontrol.client.Transport;
import io.remotecontrol.client.UnserializableResultStrategy;
import io.remotecontrol.groovy.ClosureCommand;
import io.remotecontrol.groovy.ClosureUtil;

import java.io.IOException;
import java.util.*;

public class RemoteControl {

    private final CommandGenerator<RawClosureCommand, ClosureCommand> commandGenerator;
    private final RemoteControlSupport<ClosureCommand> support;

    public RemoteControl(Transport transport) {
        this(transport, UnserializableResultStrategy.THROW, Thread.currentThread().getContextClassLoader());
    }

    public RemoteControl(Transport transport, ClassLoader classLoader) {
        this(transport, UnserializableResultStrategy.THROW, classLoader);
    }

    protected RemoteControl(Transport transport, UnserializableResultStrategy unserializableResultStrategy, ClassLoader classLoader) {
        this.support = new RemoteControlSupport<ClosureCommand>(transport, unserializableResultStrategy, classLoader);
        this.commandGenerator = new ClosureCommandGenerator(classLoader);
    }

    public Object exec(Closure[] commands) throws IOException {
        return exec(new LinkedHashMap<String, Object>(), commands);
    }

    public Object exec(Map<String, ?> params, Closure[] commands) throws IOException {
        Map<String, Object> copy = new LinkedHashMap<String, Object>(params);
        processExecParams(copy);
        CommandChain<ClosureCommand> commandChain = generateCommandChain(copy, commands);
        return support.send(commandChain);
    }

    public Object call(Closure[] commands) throws IOException {
        return exec(commands);
    }

    public Object call(Map<String, ?> params, Closure[] commands) throws IOException {
        return exec(params, commands);
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

    protected CommandChain<ClosureCommand> generateCommandChain(final Map<String, Object> params, Closure<?>[] closures) throws IOException {
        List<ClosureCommand> commands = new ArrayList<ClosureCommand>(closures.length);
        for (Closure<?> closure : closures) {
            List<Closure<?>> uses = Collections.emptyList();
            if (params.containsKey("usedClosures")) {
                @SuppressWarnings("unchecked")
                List<Closure<?>> usedClosures = (List<Closure<?>>) params.get("usedClosures");
                uses = usedClosures;
            }
            ClosureCommand command = commandGenerator.generate(new RawClosureCommand(closure, uses));
            commands.add(command);
        }
        return CommandChain.of(ClosureCommand.class, commands);
    }

}
