package io.jenkins.plugins.remotingopentelemetry;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;
import org.kohsuke.stapler.WebMethod;

import java.io.IOException;

@Extension
public class MonitoringEngineDownloadAction implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "remoting-opentelemetry";
    }

    @WebMethod(name="monitoring-engine.jar")
    public void doMonitoringEngineJar(StaplerRequest req, StaplerResponse res) {
        try {
            res.getWriter().printf("Monitoring engine will be able to download from this endpoint.").close();
        } catch (IOException e) {
            // don't care
        }
    }
}
