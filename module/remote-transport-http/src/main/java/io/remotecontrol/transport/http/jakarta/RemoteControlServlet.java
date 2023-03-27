package io.remotecontrol.transport.http.jakarta;

import io.remotecontrol.groovy.ContentType;
import io.remotecontrol.server.Receiver;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * A servlet implementation for receiving commands.
 */
public abstract class RemoteControlServlet extends HttpServlet {

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (validateRequest(request, response)) {
            configureSuccessfulResponse(response);
            doExecute(request.getInputStream(), response.getOutputStream());
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
    protected boolean validateRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contentType = request.getContentType();
        if (contentType == null || !contentType.equals(ContentType.COMMAND.getValue())) {
            response.sendError(415, "Only remotecontrol control commands can be sent");
            return false;
        }

        return true;
    }

    /**
     * Called when a request has been validated.
     *
     * Subclasses should call this implementation to set the status code and return content type.
     */
    protected void configureSuccessfulResponse(HttpServletResponse response) {
        response.setContentType(ContentType.RESULT.getValue());
    }

    /**
     * Hook for subclasses to wrap the actual execution.
     */
    protected void doExecute(InputStream input, OutputStream output) throws IOException {
        Receiver receiver = createReceiver();
        receiver.execute(input, output);
    }

    /**
     * Hook for subclasses to provide a custom receiver. Will be called during init().
     *
     * This implement returns a receiver created via the default constructor.
     */
    abstract protected Receiver createReceiver();
}
