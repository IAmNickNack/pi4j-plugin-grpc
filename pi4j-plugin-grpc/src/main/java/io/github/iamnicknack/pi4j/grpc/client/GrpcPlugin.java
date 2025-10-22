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
import io.grpc.ManagedChannel;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.*;

/**
 * {@link Plugin} which registers providers and manages the gRPC channel lifecycle
 */
public class GrpcPlugin implements Plugin {

    private final Logger logger = getLogger(this.getClass());

    private ManagedChannel channel;

    @Override
    public void initialize(PluginService service) throws InitializeException {

        var channelProvider = new GrpcChannelSupplier(service.context());
        channel = channelProvider.get();

        if (channel != null) {
            var plugins = new Provider<?, ?, ?>[] {
                    new GrpcDigitalInputProvider(channel),
                    new GrpcDigitalOutputProvider(channel),
                    new GrpcI2CProvider(channel),
                    new GrpcSpiProvider(channel),
                    new GrpcPwmProvider(channel)
            };

            service.register(plugins);
        } else {
            logger.warn("Skipping initialisation of gRPC plugin. `pi4j.grpc.host` and/or `pi4j.grpc.port` not provided.");
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
