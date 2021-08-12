package io.jenkins.plugins.remotingopentelemetry.engine.config;

/*package*/ final class StringOptionHandler implements OptionHandler<String> {
    @Override
    public String handle(String stringValue) throws ConfigurationParseException {
        return stringValue;
    }
}
