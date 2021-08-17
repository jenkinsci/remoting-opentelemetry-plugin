package io.jenkins.plugins.remotingopentelemetry.engine;

import io.jenkins.plugins.remotingopentelemetry.engine.config.ConfigParser;
import io.jenkins.plugins.remotingopentelemetry.engine.config.ConfigurationParseException;

import java.lang.instrument.Instrumentation;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreMain {
    static final Logger LOGGER = Logger.getLogger(PreMain.class.getName());

    public static void premain(String agentArgs, Instrumentation inst) {

        DefaultEngineConfiguration config = new DefaultEngineConfiguration();
        ConfigParser configParser = new ConfigParser(config);

        try {
            configParser.parse();
        } catch (ConfigurationParseException e) {
            LOGGER.log(Level.WARNING, "Failed to load configuration", e);
            return;
        }

        MonitoringEngine engine = new MonitoringEngine(config);
        engine.start();
    }
}
