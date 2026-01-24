package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.util.Optional;
import java.util.Properties;

/**
 * Container for host and port information required to connect to a gRPC server.
 *
 * @param host the host address of the gRPC server
 * @param port the port number of the gRPC server
 */
public record GrpcHostAndPort(
        String host,
        int port
) {

    public static final String GRPC_HOST_PROPERTY = "pi4j.grpc.host";
    public static final String GRPC_PORT_PROPERTY = "pi4j.grpc.port";

    /**
     * Derives host and port information from the system properties.
     * @param properties the system properties
     * @return hosta and port information or null if not found
     */
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

    /**
     * Derives host and port information from the system properties, applying defaults if necessary.
     * @param properties the system properties
     * @param defaultHost the default host address
     * @param defaultPort the default port number
     * @return hosta and port information
     */
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

    /**
     * Derives host and port information from the pi4j context properties.
     * @param context the pi4j context
     * @return hosta and port information or null if not found
     */
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

    /**
     * Derives host and port information from the pi4j context properties, applying defaults if necessary.
     * @param context the pi4j context
     * @param defaultHost the default host address
     * @param defaultPort the default port number
     * @return hosta and port information
     */
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
