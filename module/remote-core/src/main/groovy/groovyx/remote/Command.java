package groovyx.remote;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Command implements Serializable {

    private static final long serialVersionUID = 1L;

    private final byte[] instance;
    private final byte[] root;
    private final Iterable<byte[]> supports;

    public Command(byte[] instance, byte[] root, Iterable<byte[]> supports) {
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

    public Iterable<byte[]> getSupports() {
        return supports;
    }
}
