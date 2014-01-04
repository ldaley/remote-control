package groovyx.remote.client;

import groovyx.remote.CommandChain;
import groovyx.remote.result.Result;

import java.io.IOException;

/**
 * A transport is used by a remote control for sending a command and receiving the result.
 */
public interface Transport {

    public abstract Result send(CommandChain commandChain) throws IOException;

}
