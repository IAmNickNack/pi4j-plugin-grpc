package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

/**
 * Naive utility to configure a gRPC channel
 */
public class GrpcChannelSupplier implements Supplier<ManagedChannel> {

    private final Logger logger = LoggerFactory.getLogger(GrpcChannelSupplier.class);

    public static final String GRPC_HOST_PROPERTY = "pi4j.grpc.host";
    public static final String GRPC_PORT_PROPERTY = "pi4j.grpc.port";

    private final String host;
    private final Integer port;

    private ManagedChannel channel;

    /**
     * Derive configuration from system properties
     */
    public GrpcChannelSupplier() {
        this(System.getProperties());
    }

    /**
     * Derive configuration from specific properties
     */
    public GrpcChannelSupplier(Properties props) {
        this(Pi4J.newContextBuilder()
                .add(props)
                .build());
    }

    /**
     * Derive configuration from a Pi4J context
     */
    public GrpcChannelSupplier(Context context) {
        this(
                context.properties().get(GRPC_HOST_PROPERTY, null),
                Optional.ofNullable(context.properties().get(GRPC_PORT_PROPERTY, null))
                        .map(Integer::valueOf)
                        .orElse(null)
        );
    }

    /**
     * Explicitly state the remote grpc server host and port
     */
    public GrpcChannelSupplier(String host, Integer port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Create a {@link ManagedChannel} from the derived configuration
     */
    @Override
    public ManagedChannel get() {
        if (channel == null && (host != null && port != null)) {
            logger.info("Creating gRPC channel to {}:{}", host, port);
            channel = Grpc
                    .newChannelBuilderForAddress(host, port, InsecureChannelCredentials.create())
                    .build();
        } else if (channel == null) {
            logger.warn("Unable to create gRPC channel. Could not read {} and/or {}", GRPC_HOST_PROPERTY, GRPC_PORT_PROPERTY);
        }
        return this.channel;
    }
}
