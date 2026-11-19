# Server Side

The server side of the API is about receiving serialised commands, executing them, and sending back the serialised result.

How the command is received is particular to the transport being used. As such, the core library does not provide any endpoint implementations. 

Endpoint implementations create a [`io.remotecontrol.server.Receiver`][receiver-api] object that manages the execution of commands.

It exposes one method...

    void execute(InputStream command, OutputStream result)

The input stream must contain the bytes transmitted by the client. The result of the execution will be written to the output stream.

The receiver is also responsible for managing the execution *context*. See the [section on contexts][contexts] for more information.
