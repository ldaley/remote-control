package io.remotecontrol.transport.http;

public enum ContentType {

    COMMAND("application/groovy-remote-control-control-command"),
    RESULT("application/groovy-remote-control-control-result");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

}
