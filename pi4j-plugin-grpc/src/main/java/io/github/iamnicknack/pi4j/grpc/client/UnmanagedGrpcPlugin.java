package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.exception.InitializeException;
import com.pi4j.extension.Plugin;
import com.pi4j.extension.PluginService;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalInputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.i2c.GrpcI2CProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.pwm.GrpcPwmProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.spi.GrpcSpiProvider;
import io.grpc.Channel;

/**
 * A {@link Plugin} which uses an externally managed gRPC {@link Channel}.
 */
public class UnmanagedGrpcPlugin implements Plugin {

    private final Channel channel;

    public UnmanagedGrpcPlugin(Channel channel) {
        this.channel = channel;
    }

    @Override
    public void initialize(PluginService service) throws InitializeException {
        service.register(
                new GrpcDigitalInputProvider(channel),
                new GrpcDigitalOutputProvider(channel),
                new GrpcI2CProvider(channel),
                new GrpcSpiProvider(channel),
                new GrpcPwmProvider(channel)
        );
    }
}
