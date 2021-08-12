package io.jenkins.plugins.remotingopentelemetry.engine.config;

public class ConfigurationParseException extends Exception {

    public ConfigurationParseException(String message) {
        super(message);
    }

    public ConfigurationParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ConfigurationParseException(Throwable cause) {
        super(cause);
    }
}
