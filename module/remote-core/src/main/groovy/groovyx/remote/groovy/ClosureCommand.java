package groovyx.remote.groovy;

import groovyx.remote.Command;

import java.util.Collection;

public class ClosureCommand implements Command {

    private static final long serialVersionUID = 1L;

    private final byte[] instance;
    private final byte[] root;
    private final Collection<byte[]> supports;

    public ClosureCommand(byte[] instance, byte[] root, Collection<byte[]> supports) {
        this.instance = instance;
        this.root = root;
        this.supports = supports;
    }

    public byte[] getInstance() {
        return instance;
    }

    public byte[] getRoot() {
        return root;
    }

    public Collection<byte[]> getSupports() {
        return supports;
    }
}
