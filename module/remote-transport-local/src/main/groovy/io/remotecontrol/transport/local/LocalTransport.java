package io.remotecontrol.transport.local;

import io.remotecontrol.CommandChain;
import io.remotecontrol.client.Transport;
import io.remotecontrol.result.Result;
import io.remotecontrol.result.ResultFactory;
import io.remotecontrol.result.impl.DefaultResultFactory;
import io.remotecontrol.server.Receiver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class LocalTransport implements Transport {

    private final ClassLoader classLoader;
    private final Receiver receiver;
    private final ResultFactory resultFactory;

    public LocalTransport(Receiver receiver, ClassLoader classLoader, ResultFactory resultFactory) {
        this.resultFactory = resultFactory;
        this.receiver = receiver;
        this.classLoader = classLoader;
    }

    public LocalTransport(Receiver receiver, ClassLoader classLoader) {
        this(receiver, classLoader, new DefaultResultFactory());
    }

    public Result send(CommandChain commandChain) throws IOException {
        ByteArrayOutputStream commandBytes = new ByteArrayOutputStream();
        commandChain.writeTo(commandBytes);

        ByteArrayOutputStream resultBytes = new ByteArrayOutputStream();

        receiver.execute(new ByteArrayInputStream(commandBytes.toByteArray()), resultBytes);

        return resultFactory.deserialize(new ByteArrayInputStream(resultBytes.toByteArray()), classLoader);
    }

}
