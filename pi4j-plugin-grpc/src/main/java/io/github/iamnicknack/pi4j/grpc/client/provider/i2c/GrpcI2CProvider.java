package io.github.iamnicknack.pi4j.grpc.client.provider.i2c;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.i2c.I2CProviderBase;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcI2CProvider extends I2CProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    public GrpcI2CProvider(Channel channel) {
        super("grpc-i2c");
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public I2CProvider initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public I2CProvider shutdownInternal(Context context) throws ShutdownException {
        return super.shutdownInternal(context);
    }

    @Override
    public I2C create(I2CConfig config) {
        logger.info("Creating new I2C device: {}", config.id());

        var payload = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.I2C)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(payload);
        var responseConfig = I2C.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        var device = new GrpcI2C(channel, this, responseConfig);
        context.registry().add(device);
        return device;
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(I2C device) {
        logger.info("Removing I2C device: {}", device.config().id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.I2C)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }
}
