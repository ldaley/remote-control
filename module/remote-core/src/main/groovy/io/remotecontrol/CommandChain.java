package io.remotecontrol;

import io.remotecontrol.util.UnexpectedIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class CommandChain<T extends Command> implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Class<T> type;
    private final List<? extends T> commands;

    public CommandChain(Class<T> type, List<? extends T> commands) {
        this.type = type;
        this.commands = commands;
    }

    public Class<T> getType() {
        return type;
    }

    public List<? extends T> getCommands() {
        return commands;
    }

    public void writeTo(OutputStream outputStream) {
        try {
            SerializationUtil.serialize(this, outputStream);
        } catch (IOException e) {
            throw new UnexpectedIOException("command chain should be serializable", e);
        }
    }

    public static <T extends Command> CommandChain<T> of(Class<T> type, T... commands) {
        return new CommandChain<T>(type, Arrays.asList(commands));
    }

    public static <T extends Command> CommandChain<T> of(Class<T> type, List<? extends T> commands) {
        return new CommandChain<T>(type, commands);
    }

}
