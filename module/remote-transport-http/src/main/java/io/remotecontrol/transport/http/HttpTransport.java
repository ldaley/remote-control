package io.remotecontrol.transport.http;

import io.remotecontrol.CommandChain;
import io.remotecontrol.RemoteControlException;
import io.remotecontrol.client.Transport;
import io.remotecontrol.groovy.ContentType;
import io.remotecontrol.result.Result;
import io.remotecontrol.result.ResultFactory;
import io.remotecontrol.result.impl.DefaultResultFactory;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Transports commands over http to the given receiver address.
 */
public class HttpTransport implements Transport {

    private final String receiverAddress;
    private final ClassLoader classLoader;
    private final ResultFactory resultFactory;

    /**
     * @param receiverAddress the full address to the remotecontrol receiver
     * @param classLoader the class loader to use when unserialising the result
     */
    public HttpTransport(String receiverAddress, ClassLoader classLoader, ResultFactory resultFactory) {
        this.classLoader = classLoader;
        this.receiverAddress = receiverAddress;
        this.resultFactory = resultFactory;
    }

    /**
     * @param receiverAddress the full address to the remotecontrol receiver
     * @param classLoader the class loader to use when unserialising the result
     */
    public HttpTransport(String receiverAddress, ClassLoader classLoader) {
        this(receiverAddress, classLoader, new DefaultResultFactory());
    }

    /**
     * @param receiverAddress the full address to the remotecontrol receiver
     */
    public HttpTransport(String receiverAddress) {
        this(receiverAddress, Thread.currentThread().getContextClassLoader());
    }

    /**
     * Serialises the Command and sends it over HTTP, returning the Result.
     *
     * @throws RemoteControlException if there is any issue with the receiver.
     */
    public Result send(final CommandChain commandChain) throws RemoteControlException {
        OutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            HttpURLConnection urlConnection = openConnection();
            urlConnection.setRequestProperty("Content-Type", ContentType.COMMAND.getValue());
            urlConnection.setRequestProperty("Accept", ContentType.RESULT.getValue());
            urlConnection.setInstanceFollowRedirects(true);
            urlConnection.setDoOutput(true);

            configureConnection(urlConnection);

            outputStream = urlConnection.getOutputStream();
            commandChain.writeTo(outputStream);

            inputStream = urlConnection.getInputStream();
            return resultFactory.deserialize(inputStream, classLoader);
        } catch (Exception e) {
            throw new RemoteControlException("Error sending command chain to \'" + String.valueOf(receiverAddress) + "\'", e);
        } finally {
            for (Closeable closeable : new Closeable[]{inputStream, outputStream}) {
                if (closeable != null) {
                    try {
                        closeable.close();
                    } catch (IOException ignore) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * Subclass hook for configuring the connection object before the request is set.
     *
     * This could be used to implement authentication.
     */
    @SuppressWarnings({"EmptyMethod", "UnusedParameters"})
    protected void configureConnection(HttpURLConnection connection) {

    }

    /**
     * Creates a HttpURLConnection to the remotecontrol receiver at the given receiverAddress.
     */
    protected HttpURLConnection openConnection() throws IOException {
        return ((HttpURLConnection) (new URL(receiverAddress).openConnection()));
    }

}
