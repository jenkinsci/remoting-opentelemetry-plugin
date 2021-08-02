package io.jenkins.plugins.remotingopentelemetry.engine.log;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

import javax.annotation.Nullable;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public final class OtlpGrpcLogExporterBuilder {
    private static final String DEFAULT_ENDPOINT_URL = "http://localhost:4317";
    private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
    private static final long DEFAULT_TIMEOUT_SECS = 10;

    private ManagedChannel channel;
    private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
    private URI endpoint = DEFAULT_ENDPOINT;

    @Nullable
    private Metadata metadata;

    public OtlpGrpcLogExporterBuilder setChannel(ManagedChannel channel) {
        this.channel = channel;
        return this;
    }

    public OtlpGrpcLogExporterBuilder setTimeout(long timeout, TimeUnit unit) {
        requireNonNull(unit, "unit");
        checkArgument(timeout >= 0, "timeout must be non-negative");
        timeoutNanos = unit.toNanos(timeout);
        return this;
    }

    public OtlpGrpcLogExporterBuilder setTimeout(Duration timeout) {
        requireNonNull(timeout, "timeout");
        return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
    }

    public OtlpGrpcLogExporterBuilder setEndpoint(String endpoint) {
        requireNonNull(endpoint, "endpoint");
        URI uri;
        try {
            uri = new URI(endpoint);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
        }

        if (uri.getScheme() == null
            || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
            throw new IllegalArgumentException(
                "invalid endpoint, must start with http:// or https://: " + uri);
        }
        this.endpoint = uri;
        return this;
    }

    public OtlpGrpcLogExporterBuilder addHeader(String key, String value) {
        if (metadata == null) {
            metadata = new Metadata();
        }
        metadata.put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value);
        return this;
    }

    public OtlpGrpcLogExporter build() {
        if (channel == null) {
            final ManagedChannelBuilder<?> managedChannelBuilder =
                ManagedChannelBuilder.forTarget(endpoint.getAuthority());

            if (endpoint.getScheme().equals("https")) {
                managedChannelBuilder.useTransportSecurity();
            } else {
                managedChannelBuilder.usePlaintext();
            }

            if (metadata != null) {
                managedChannelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));
            }

            channel = managedChannelBuilder.build();
        }

        return new OtlpGrpcLogExporter(channel, timeoutNanos);
    }
}
