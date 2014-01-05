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

package groovyx.remote.server;

import groovyx.remote.Command;
import groovyx.remote.CommandChain;
import groovyx.remote.RemoteControlException;
import groovyx.remote.SerializationUtil;
import groovyx.remote.result.Result;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class MultiTypeReceiver implements Receiver {

    private final ClassLoader classLoader;
    private final List<CommandRunner<?>> runners;

    public MultiTypeReceiver(ClassLoader classLoader, CommandRunner<?>... runners) {
        this.classLoader = classLoader;
        this.runners = Arrays.asList(runners);
    }

    @Override
    public void execute(InputStream commandStream, OutputStream resultStream) throws IOException {
        CommandChain<?> commandChain;
        try {
            commandChain = SerializationUtil.deserialize(CommandChain.class, commandStream, classLoader);
        } catch (ClassNotFoundException e) {
            throw RemoteControlException.classNotFoundOnServer(e);
        }

        for (CommandRunner<?> runner : runners) {
            Result result = maybeInvoke(commandChain, runner);
            if (result != null) {
                SerializationUtil.serialize(result, resultStream);
                return;
            }
        }

        throw new RemoteControlException("Cannot handle commands of type:" + commandChain.getType().getName());
    }

    private <T extends Command> Result maybeInvoke(CommandChain<T> commandChain, CommandRunner<?> runner) {
        if (commandChain.getType().isAssignableFrom(runner.getType())) {
            @SuppressWarnings("unchecked")
            CommandRunner<T> cast = (CommandRunner<T>) runner;
            return cast.run(commandChain);
        } else {
            return null;
        }
    }

}
