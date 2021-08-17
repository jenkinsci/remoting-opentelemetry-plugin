package io.jenkins.plugins.remotingopentelemetry.engine.config;

/*package*/ interface OptionHandler<T> {
     T handle(String stringValue) throws ConfigurationParseException;
}
