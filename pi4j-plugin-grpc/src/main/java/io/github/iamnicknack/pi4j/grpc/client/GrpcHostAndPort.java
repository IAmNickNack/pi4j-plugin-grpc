package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import java.util.Properties;

public record GrpcHostAndPort(
        String host,
        int port
) {

    public static final String GRPC_HOST_PROPERTY = "pi4j.grpc.host";
    public static final String GRPC_PORT_PROPERTY = "pi4j.grpc.port";

    @Nullable
    public static GrpcHostAndPort fromProperties(Properties properties) {
        String host = properties.getProperty(GRPC_HOST_PROPERTY);
        int port = Optional.ofNullable(properties.getProperty(GRPC_PORT_PROPERTY))
                .map(Integer::valueOf)
                .orElse(0);

        if (host == null || port == 0) {
            return null;
        }

        return new GrpcHostAndPort(host, port);
    }

    public static GrpcHostAndPort fromPropertiesWithDefaults(
            Properties properties,
            @Nonnull String defaultHost,
            @Nonnull Integer defaultPort
    ) {
        String host = properties.getProperty(GRPC_HOST_PROPERTY,  defaultHost);
        int port = Optional.ofNullable(properties.getProperty(GRPC_PORT_PROPERTY))
                .map(Integer::valueOf)
                .orElse(defaultPort);

        return new GrpcHostAndPort(host, port);
    }

    @Nullable
    public static GrpcHostAndPort fromContext(Context context) {
        String host = context.properties().get(GRPC_HOST_PROPERTY);
        int port = Optional.ofNullable(context.properties().get(GRPC_PORT_PROPERTY))
                .map(Integer::valueOf)
                .orElse(0);

        if (host == null || port == 0) {
            return null;
        }

        return new GrpcHostAndPort(host, port);
    }

    public static GrpcHostAndPort fromContextWithDefaults(
            Context context,
            @Nonnull String defaultHost,
            @Nonnull Integer defaultPort
    ) {
        String host = context.properties().get(GRPC_HOST_PROPERTY,  defaultHost);
        int port = Optional.of(context.properties().get(GRPC_PORT_PROPERTY))
                .map(Integer::valueOf)
                .orElse(defaultPort);

        return new GrpcHostAndPort(host, port);
    }

}
