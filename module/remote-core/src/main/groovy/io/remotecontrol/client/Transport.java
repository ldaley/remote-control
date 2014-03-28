package io.remotecontrol.client;

import io.remotecontrol.CommandChain;
import io.remotecontrol.result.Result;

import java.io.IOException;

/**
 * A transport is used by a remotecontrol control for sending a command and receiving the result.
 */
public interface Transport {

    public abstract Result send(CommandChain<?> commandChain) throws IOException;

}
