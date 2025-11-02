package io.github.iamnicknack.pi4j.grpc.client.provider.spi;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiConfig;
import com.pi4j.io.spi.SpiProvider;
import com.pi4j.io.spi.SpiProviderBase;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcSpiProvider extends SpiProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    public GrpcSpiProvider(Channel channel) {
        super("grpc-spi");
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public SpiProvider initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public SpiProvider shutdownInternal(Context context) throws ShutdownException {
        return super.shutdownInternal(context);
    }

    @Override
    public Spi create(SpiConfig config) {
        logger.info("Creating new SPI device: {}", config.id());
        var request = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.SPI)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(request);
        var responseConfig = Spi.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        var device = new GrpcSpi(channel, this, responseConfig);
        context.registry().add(device);
        return device;
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(Spi device) {
        logger.info("Removing SPI device: {}", device.id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.SPI)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }
}
