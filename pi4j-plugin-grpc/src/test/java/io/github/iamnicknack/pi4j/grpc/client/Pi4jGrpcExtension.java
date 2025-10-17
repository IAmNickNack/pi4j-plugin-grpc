package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl;
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl;
import com.pi4j.plugin.mock.provider.pwm.MockPwmProviderImpl;
import com.pi4j.plugin.mock.provider.spi.MockSpiProviderImpl;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalInputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.i2c.GrpcI2CProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.pwm.GrpcPwmProvider;
import io.github.iamnicknack.pi4j.grpc.client.provider.spi.GrpcSpiProvider;
import io.github.iamnicknack.pi4j.grpc.server.service.*;
import io.grpc.Channel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.extension.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * JUnit 5 extension for running tests with a Pi4J gRPC client.
 */
public class Pi4jGrpcExtension implements BeforeEachCallback, AfterEachCallback, ParameterResolver {

    private final Channel channel = InProcessChannelBuilder.forName("test").build();

    private Context localPi4j;

    private Context remotePi4j;

    private Server server;

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        localPi4j = Pi4J.newContextBuilder()
                .add(new GrpcDigitalInputProvider(channel))
                .add(new GrpcDigitalOutputProvider(channel))
                .add(new GrpcI2CProvider(channel))
                .add(new GrpcSpiProvider(channel))
                .add(new GrpcPwmProvider(channel))
                .build();
        remotePi4j = Pi4J.newContextBuilder()
                .add(new MockDigitalInputProviderImpl())
                .add(new MockDigitalOutputProviderImpl())
                .add(new MockI2CProviderImpl())
                .add(new MockSpiProviderImpl())
                .add(new MockPwmProviderImpl())
                .build();
        server = InProcessServerBuilder.forName("test").directExecutor()
                .addService(new DeviceConfigService(remotePi4j))
                .addService(new DigitalInputService(remotePi4j))
                .addService(new DigitalOutputService(remotePi4j))
                .addService(new I2CService(remotePi4j))
                .addService(new SpiService(remotePi4j))
                .addService(new PwmService(remotePi4j))
                .build();

        server.start();
    }

    @Override
    public void afterEach(ExtensionContext context) {
        localPi4j.shutdown();

        // check that local shutdown has removed all remote devices
        var remoteDeviceCount = remotePi4j.registry().all().size();

        // continue shutting down and cleaning up remote pi4j context
        remotePi4j.shutdown();
        server.shutdown();

        // fail if remote devices were not removed
        if (remoteDeviceCount > 0) {
            throw new AssertionError("Not all remote devices were not removed after local shutdown: " + remoteDeviceCount);
        }
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        return (parameterContext.getParameter().getType() == Context.class) &&
                (parameterContext.isAnnotated(Local.class) || parameterContext.isAnnotated(Remote.class));
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
        if (parameterContext.isAnnotated(Local.class) && parameterContext.getParameter().getType() == Context.class ) {
            return localPi4j;
        } else if (parameterContext.isAnnotated(Remote.class) && parameterContext.getParameter().getType() == Context.class ) {
            return remotePi4j;
        }
        throw new ParameterResolutionException("Unsupported parameter type without annotation: " + parameterContext.getParameter().getType());
    }

    @Retention(RetentionPolicy.RUNTIME)
    @interface Local {}

    @Retention(RetentionPolicy.RUNTIME)
    @interface Remote {}
}
