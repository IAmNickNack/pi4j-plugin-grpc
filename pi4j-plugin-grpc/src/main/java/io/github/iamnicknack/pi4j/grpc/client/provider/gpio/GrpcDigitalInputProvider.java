package io.github.iamnicknack.pi4j.grpc.client.provider.gpio;

import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider for digital input devices over gRPC.
 */
public class GrpcDigitalInputProvider extends DigitalInputProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    /**
     * @param channel the gRPC channel to use.
     */
    public GrpcDigitalInputProvider(Channel channel) {
        super("grpc-digital-input");
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    /**
     * Override {@link com.pi4j.io.IOBase#shutdownInternal(Context)} to prevent individual device shutdown invoked
     * by the base class.
     * <p>
     * This allows shutdown to be handled in a more controlled manner by the context.
     */
    @Override
    public DigitalInputProvider shutdownInternal(Context context) throws ShutdownException {
        return this;
    }

    /**
     * Create a new digital input device and be done.
     * @param config the configuration describing the I/O instance to create (address, id, options, etc.)
     * @return the created digital input device
     */
    @Override
    public DigitalInput create(DigitalInputConfig config) {
        logger.info("Creating new Digital Input device: {}", config.id());
        var payload = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.DIGITAL_INPUT)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(payload);
        var responseConfig = DigitalInput.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        return new GrpcDigitalInput(channel, this, responseConfig);
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(DigitalInput device) {
        logger.info("Removing Digital Input device: {}", device.id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.DIGITAL_INPUT)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }
}
