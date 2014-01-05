package groovyx.remote.transport.http;

public enum ContentType {

    COMMAND("application/groovy-remote-control-command"),
    RESULT("application/groovy-remote-control-result");

    private final String value;

    ContentType(String value) {
        this.value = value;
    }

    public final String getValue() {
        return value;
    }

}
