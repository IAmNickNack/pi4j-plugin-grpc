package io.github.iamnicknack.pi4j.grpc.client.provider.pwm;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfig;
import com.pi4j.io.pwm.PwmProvider;
import com.pi4j.io.pwm.PwmProviderBase;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType;
import io.grpc.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrpcPwmProvider extends PwmProviderBase {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getName());

    private final Channel channel;
    private final DeviceConfigServiceGrpc.DeviceConfigServiceBlockingStub configStub;

    public GrpcPwmProvider(Channel channel) {
        super("grpc-pwm");
        this.channel = channel;
        this.configStub = DeviceConfigServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public PwmProvider initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public PwmProvider shutdownInternal(Context context) throws ShutdownException {
        return super.shutdownInternal(context);
    }

    @Override
    public Pwm create(PwmConfig config) {
        logger.info("Creating new PWM device: {}", config.id());
        var request = DeviceConfigPayload.newBuilder()
                .setDeviceType(DeviceType.PWM)
                .putAllConfig(config.properties())
                .build();

        var response = configStub.createDevice(request);
        var responseConfig = Pwm.newConfigBuilder(this.context)
                .load(response.getConfigMap())
                .build();

        var device = new GrpcPwm(channel, this, responseConfig);
        context.registry().add(device);
        return device;
    }

    /**
     * Remove a device from the pi4j context.
     * @param device the device to remove.
     */
    public void removeDevice(Pwm device) {
        logger.info("Removing SPI device: {}", device.id());
        var request = DeviceRequest.newBuilder()
                .setDeviceId(device.config().id())
                .setDeviceType(DeviceType.PWM)
                .build();

        var result = configStub.removeDevice(request);
        assert result != null;
    }}
