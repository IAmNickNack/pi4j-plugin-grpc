package io.github.iamnicknack.pi4j.grpc.client.provider.gpio;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcDigitalOutputProvider extends DigitalOutputProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    public GrpcDigitalOutputProvider(Channel channel) {
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public DigitalOutputProvider initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public DigitalOutputProvider shutdownInternal(Context context) throws ShutdownException {
        return super.shutdownInternal(context);
    }

    @Override
    public DigitalOutput create(DigitalOutputConfig config) {
        logger.info("Creating new Digital Output device: {}", config.id());
        var payload = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.DIGITAL_OUTPUT)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(payload);
        var responseConfig = DigitalOutput.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        var device = new GrpcDigitalOutput(channel, this, responseConfig);
        context.registry().add(device);
        return device;
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(DigitalOutput device) {
        logger.info("Removing Digital Output device: {}", device.config().id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.DIGITAL_OUTPUT)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }
}
