package groovyx.remote.groovy.server;

import groovyx.remote.CommandChain;
import groovyx.remote.groovy.ClosureCommand;

/**
 * A context factory produces what will be used as the delegate for all commands in a command chain.
 */
public interface ContextFactory {

    /**
     * Produces a context to be used for the entire given chain.
     */
    public abstract Object getContext(CommandChain<ClosureCommand> chain);

}
