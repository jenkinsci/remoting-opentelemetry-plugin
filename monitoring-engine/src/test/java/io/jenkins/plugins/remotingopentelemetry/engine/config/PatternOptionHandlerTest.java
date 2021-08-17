package io.jenkins.plugins.remotingopentelemetry.engine.config;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class PatternOptionHandlerTest {
    static final String TEST_ENV1 = "REMOTING_OTEL_TEST_ENV1";
    static final String TEST_ENV2 = "REMOTING_OTEL_TEST_ENV2";
    static final String TEST_ENV3 = "REMOTING_OTEL_TEST_ENV3";

    @Test
    public void testValidPattern() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put(TEST_ENV1, "^value1$");
        env.put(TEST_ENV2, "^override value$");

        TestConfig config = new TestConfig();
        ConfigParser parser = new ConfigParser(config);
        parser.setEnv(env);
        parser.parse();

        Assert.assertTrue(config.value1.matcher("value1").matches());
        Assert.assertTrue(config.value2.matcher("override value").matches());
        Assert.assertTrue(config.value3.matcher("value3").matches());
    }

    @Test(expected = ConfigurationParseException.class)
    public void testInvalidPattern() throws Exception {
        Map<String, String> env = new HashMap<>();
        env.put(TEST_ENV1, "^value1$");
        env.put(TEST_ENV2, "\\k");

        TestConfig config = new TestConfig();
        ConfigParser parser = new ConfigParser(config);
        parser.setEnv(env);
        parser.parse();
    }

    @Configuration
    public static class TestConfig {
        @ConfigOption(env = TEST_ENV1, required = true)
        public Pattern value1;

        @ConfigOption(env = TEST_ENV2)
        public Pattern value2 = Pattern.compile("^value2$");

        @ConfigOption(env = TEST_ENV3)
        public Pattern value3 = Pattern.compile("^value3$");
    }
}
