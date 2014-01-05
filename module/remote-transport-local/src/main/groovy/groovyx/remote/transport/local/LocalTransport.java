package groovyx.remote.transport.local;

import groovyx.remote.CommandChain;
import groovyx.remote.client.Transport;
import groovyx.remote.result.Result;
import groovyx.remote.result.ResultFactory;
import groovyx.remote.result.impl.DefaultResultFactory;
import groovyx.remote.server.Receiver;

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
