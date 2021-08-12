package io.jenkins.plugins.remotingopentelemetry.engine.config;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Load configuration from Environment variables, config file, etc...
 * Only environment variables are supported for now.
 */
public final class ConfigParser {
    private final Object bean;
    private Map<String, String> env = System.getenv();

    public ConfigParser(Object bean) {
        this.bean = bean;
    }

    public void setEnv(Map<String, String> env) {
        this.env = env;
    }

    public void parse() throws ConfigurationParseException {
        try {
            doParse();
        } catch (ConfigurationParseException e) {
            throw e;
        } catch (Throwable e) {
            throw new ConfigurationParseException(e);
        }
    }

    private void doParse() throws ConfigurationParseException {
        Class<?> clazz = this.bean.getClass();
        if (clazz.getAnnotation(Configuration.class) == null) {
            throw new ConfigurationParseException("Target object class is not annotated with @Configuration");
        }

        Field[] fields = clazz.getFields();

        for (Field field : fields) {
            ConfigOption configOption = field.getAnnotation(ConfigOption.class);
            if (configOption == null) continue;
            setFieldConfig(field, configOption);
        }
    }

    private void setFieldConfig(Field field, ConfigOption configOption) throws ConfigurationParseException {
        String stringValue = getConfigOptionStringValue(configOption);

        if (stringValue == null && configOption.required()) {
            String msg = String.format("%s is a required configuration option, but was not set", configOption.env());
            throw new ConfigurationParseException(msg);
        }

        if (stringValue == null) return; // use default value

        OptionHandler<?> handler = pickupOptionHandler(field.getType());

        Object optionValue = handler.handle(stringValue);

        try {
            field.set(this.bean, optionValue);
        } catch (Throwable e) {
            throw new ConfigurationParseException(e);
        }
    }

    @Nullable
    private String getConfigOptionStringValue(ConfigOption configOption) {
        return env.get(configOption.env());
    }

    @Nonnull
    private OptionHandler<?> pickupOptionHandler(Class<?> fieldType) throws ConfigurationParseException {
        if (fieldType == String.class) {
            return new StringOptionHandler();
        } else if (fieldType == Pattern.class) {
            return new PatternOptionHandler();
        } else {
            throw new ConfigurationParseException("The type of ConfigOption field must be String or Pattern");
        }
    }
}
