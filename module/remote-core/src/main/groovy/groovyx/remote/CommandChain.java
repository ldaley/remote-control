package groovyx.remote;

import groovyx.remote.util.UnexpectedIOException;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;

public class CommandChain implements Serializable {

    private static final long serialVersionUID = 1L;

    private final List<Command> commands;

    public CommandChain(List<Command> commands) {
        this.commands = commands;
    }

    public List<Command> getCommands() {
        return commands;
    }

    public void writeTo(OutputStream outputStream) {
        try {
            SerializationUtil.serialize(this, outputStream);
        } catch (IOException e) {
            throw new UnexpectedIOException("command chain should be serializable", e);
        }
    }
}
