package io.remotecontrol.transport.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.remotecontrol.server.Receiver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A HttpHandler implementation for the com.sun.net.httpserver package.
 */
public class RemoteControlHttpHandler implements HttpHandler {

    private final Receiver receiver;

    public RemoteControlHttpHandler(Receiver receiver) {
        this.receiver = receiver;
    }

    public void handle(final HttpExchange exchange) {
        try {
            if (validateRequest(exchange)) {
                configureSuccessfulResponse(exchange);
                doExecute(exchange.getRequestBody(), exchange.getResponseBody());
            }
        } catch (IOException ignore) {

        } finally {
            exchange.close();
        }

    }

    /**
     * Validate that this request is valid.
     *
     * Subclasses should call this implementation before any custom validation.
     *
     * If the request is invalid, this is the place to send back the appropriate headers/body.
     *
     * @return true if the request is valid and should proceed, false if otherwise.
     */
    protected boolean validateRequest(HttpExchange exchange) throws IOException {
        if (!exchange.getRequestMethod().equals("POST")) {
            exchange.sendResponseHeaders(415, 0);
            exchange.getResponseBody().write("request must be a POST".getBytes("UTF-8"));
            return false;
        }

        if (!exchange.getRequestHeaders().getFirst("Content-Type").equals(ContentType.COMMAND.getValue())) {
            exchange.sendResponseHeaders(415, 0);
            exchange.getResponseBody().write(("Content type must be " + ContentType.COMMAND).getBytes("UTF-8"));
            return false;
        }

        return true;
    }

    /**
     * Called when a request has been validated.
     *
     * Subclasses should call this implementation to set the status code and return content type.
     */
    protected void configureSuccessfulResponse(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", ContentType.RESULT.getValue());
        exchange.sendResponseHeaders(200, 0);
    }

    /**
     * Does the actual command execution.
     *
     * Subclasses can override this method to wrap the execution.
     */
    protected void doExecute(InputStream input, OutputStream output) throws IOException {
        receiver.execute(input, output);
    }

}
