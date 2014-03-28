/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.remotecontrol.groovy.server;

import io.remotecontrol.CommandChain;
import io.remotecontrol.groovy.ClosureCommand;
import io.remotecontrol.result.Result;
import io.remotecontrol.result.ResultFactory;
import io.remotecontrol.server.CommandChainInvoker;
import io.remotecontrol.server.CommandRunner;

public class ClosureCommandRunner implements CommandRunner<ClosureCommand> {

    private final ClassLoader classLoader;
    private final ContextFactory contextFactory;
    private final ResultFactory resultFactory;

    public ClosureCommandRunner(ClassLoader classLoader, ContextFactory contextFactory, ResultFactory resultFactory) {
        this.classLoader = classLoader;
        this.contextFactory = contextFactory;
        this.resultFactory = resultFactory;
    }

    @Override
    public Class<ClosureCommand> getType() {
        return ClosureCommand.class;
    }

    @Override
    public Result run(CommandChain<ClosureCommand> commandChain) {
        return invokeCommandChain(commandChain);
    }

    protected Result invokeCommandChain(CommandChain<ClosureCommand> commandChain) {
        CommandChainInvoker invoker = createInvoker(classLoader, commandChain);
        return invoker.invokeAgainst(createContext(commandChain), null);
    }

    protected CommandChainInvoker createInvoker(ClassLoader classLoader, CommandChain<ClosureCommand> commandChain) {
        return new CommandChainInvoker(classLoader, commandChain, resultFactory);
    }

    protected Object createContext(CommandChain<ClosureCommand> commandChain) {
        return contextFactory.getContext(commandChain);
    }
}
