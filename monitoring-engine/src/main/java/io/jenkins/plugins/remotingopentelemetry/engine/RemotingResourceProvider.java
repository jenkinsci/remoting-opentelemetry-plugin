package io.jenkins.plugins.remotingopentelemetry.engine;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

/**
 * Provides {@link Resource}
 */
public class RemotingResourceProvider {
    /**
     * @return configured {@link Resource}
     */
    public static Resource create(EngineConfiguration config) {
        // TODO: more attributes,
        return Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAMESPACE, "jenkins",
                ResourceAttributes.SERVICE_NAME, "jenkins-agent",
                ResourceAttributes.SERVICE_INSTANCE_ID, config.getNodeName()
        ));
    }
}
