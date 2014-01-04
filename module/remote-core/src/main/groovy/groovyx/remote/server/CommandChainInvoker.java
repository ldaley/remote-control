package groovyx.remote.server;

import groovyx.remote.Command;
import groovyx.remote.CommandChain;
import groovyx.remote.result.Result;
import groovyx.remote.result.ResultFactory;
import org.codehaus.groovy.runtime.InvokerInvocationException;

public class CommandChainInvoker {

    private final ClassLoader parentLoader;
    private final CommandChain commandChain;
    private final ResultFactory resultFactory;

    public CommandChainInvoker(ClassLoader parentLoader, CommandChain commandChain, ResultFactory resultFactory) {
        this.resultFactory = resultFactory;
        this.parentLoader = parentLoader;
        this.commandChain = commandChain;
    }

    public Result invokeAgainst(Object delegate, Object firstArg) {
        Object arg = firstArg;

        for (Command command : commandChain.getCommands()) {
            CommandInvoker invoker = createInvoker(parentLoader, command);
            try {
                arg = invoker.invokeAgainst(delegate, arg);
            } catch (InvokerInvocationException e) {
                return resultFactory.forThrown(e.getCause());
            } catch (Throwable throwable) {
                return resultFactory.forThrown(throwable);
            }
        }

        return resultFactory.forValue(arg);
    }

    protected CommandInvoker createInvoker(ClassLoader loader, Command command) {
        return new CommandInvoker(loader, command);
    }

}
