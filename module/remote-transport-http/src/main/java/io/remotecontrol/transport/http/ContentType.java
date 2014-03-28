package io.remotecontrol.transport.http;

public enum ContentType {

    COMMAND("application/groovy-remotecontrol-control-command"),
    RESULT("application/groovy-remotecontrol-control-result");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

}
