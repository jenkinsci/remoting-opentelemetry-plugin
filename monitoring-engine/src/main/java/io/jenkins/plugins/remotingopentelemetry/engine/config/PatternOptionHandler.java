package io.jenkins.plugins.remotingopentelemetry.engine.config;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/*package*/ final class PatternOptionHandler implements OptionHandler<Pattern> {

    @Override
    public Pattern handle(String stringValue) throws ConfigurationParseException {
        try {
            return Pattern.compile(stringValue);
        } catch (PatternSyntaxException e) {
            throw new ConfigurationParseException("Failed to compile Pattern option", e);
        }
    }
}
