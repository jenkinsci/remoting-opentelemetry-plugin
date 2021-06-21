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
    public static Resource create() {
        // TODO: more attributes,
        // TODO: enable to configure them
        return Resource.create(Attributes.of(
                ResourceAttributes.SERVICE_NAME, "Jenkins Agent"
        ));
    }
}
