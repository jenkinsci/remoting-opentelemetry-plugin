/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.jenkins.plugins.remotingopentelemetry.engine.log;

import io.opentelemetry.sdk.resources.Resource;

public final class LogSinkSdkProviderBuilder {
    private Resource resource = Resource.getDefault();

    LogSinkSdkProviderBuilder() {
    }

    public LogSinkSdkProviderBuilder setResource(Resource resource) {
        this.resource = resource;
        return this;
    }

    public LogSinkSdkProvider build() {
        return new LogSinkSdkProvider(resource);
    }

}
