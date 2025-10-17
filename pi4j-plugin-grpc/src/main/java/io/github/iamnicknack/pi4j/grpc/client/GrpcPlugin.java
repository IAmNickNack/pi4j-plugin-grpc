package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.extension.Plugin;
import com.pi4j.extension.PluginService;
import com.pi4j.provider.Provider;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalInputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.i2c.GrpcI2CProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.pwm.GrpcPwmProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.spi.GrpcSpiProvider;
import io.grpc.Grpc;
import io.grpc.InsecureChannelCredentials;
import io.grpc.ManagedChannel;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.*;

/**
 * This currently doesn't work with the Pi4J plugin architecture due to issues providing module information
 * for the protobuf generated classes.
 */
public class GrpcPlugin implements Plugin {

    private final Logger logger = getLogger(this.getClass());

    private ManagedChannel channel;

    @Override
    public void initialize(PluginService service) throws InitializeException {
        if (service.context().properties().has("pi4j.grpc.host") &&
                service.context().properties().has("pi4j.grpc.port")) {
            var host = service.context().properties().get("pi4j.grpc.host");
            var port = service.context().properties().getInteger("pi4j.grpc.port");

            logger.info("Initializing gRPC plugin for {}:{}", host, port);

            channel = Grpc
                    .newChannelBuilder(host + ":" + port, InsecureChannelCredentials.create())
                    .build();

            var plugins = new Provider<?, ?, ?>[] {
                    new GrpcDigitalInputProvider(channel),
                    new GrpcDigitalOutputProvider(channel),
                    new GrpcI2CProvider(channel),
                    new GrpcSpiProvider(channel),
                    new GrpcPwmProvider(channel)
            };

            service.register(plugins);
        }
    }

    @Override
    public void shutdown(Context context) throws ShutdownException {
        if (channel != null) {
            channel.shutdown();
        }
        Plugin.super.shutdown(context);
    }
}
