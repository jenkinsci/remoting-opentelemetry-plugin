package io.jenkins.plugins.remotingopentelemetry.engine.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class ConfigParserTest {
    static final String TEST_ENV1= "REMOTING_OTEL_TEST_ENV1";
    static final String TEST_ENV2 = "REMOTING_OTEL_TEST_ENV2";

    @Test
    public void testConfigParser() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put(TEST_ENV1, "test value");

        TestConfig config = new TestConfig();
        ConfigParser parser = new ConfigParser(config);
        parser.setEnv(env);
        parser.parse();

        Assert.assertEquals("test value", config.value1);
        Assert.assertEquals("default value", config.value2);
    }

    @Test
    public void testInvalidConfigOption() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put(TEST_ENV2, "override value");

        TestConfig config = new TestConfig();
        ConfigParser parser = new ConfigParser(config);
        parser.setEnv(env);

        boolean hasError = false;
        try {
            parser.parse();
        } catch (ConfigurationParseException e) {
            hasError = true;
        }
        Assert.assertTrue(hasError);
    }

    @Configuration
    public static class TestConfig {
        @ConfigOption(env = TEST_ENV1, required = true)
        public String value1;

        @ConfigOption(env = TEST_ENV2)
        public String value2 = "default value";
    }
}
