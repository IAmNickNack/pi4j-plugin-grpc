package io.github.iamnicknack.pi4j.grpc.client.provider.gpio;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pi4j.grpc.gen.device.*;
import io.grpc.Channel;
import org.slf4j.Logger;

public class GrpcDigitalOutput extends DigitalOutputBase {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final DigitalOutputServiceGrpc.DigitalOutputServiceBlockingStub deviceStub;
    private final GrpcDigitalOutputProvider provider;

    public GrpcDigitalOutput(
            Channel channel,
            GrpcDigitalOutputProvider provider,
            DigitalOutputConfig config
    ) {
        super(provider, config);
        this.deviceStub = DigitalOutputServiceGrpc.newBlockingStub(channel);
        this.provider = provider;

        // There may be a better way to do this.
        // This is currently done to allow us to provide a stream observer, as accepted by the
        // async API
        var callbackService = DigitalOutputServiceGrpc.newStub(deviceStub.getChannel());
        callbackService.addListener(
                DeviceIdRequest.newBuilder().setDeviceId(id).build(),
                new DigitalStateResponseStreamObserver(state -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Digital Output state changed: {}", state);
                    }
                    dispatch(new DigitalStateChangeEvent<>(this, state));
                })
        );
    }

    @Override
    public DigitalOutput initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public DigitalOutput shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        provider.removeDevice(this);
        return this;
    }

    @Override
    public DigitalState state() {
        var request = GetDigitalStateRequest.newBuilder()
                .setDeviceId(this.config.id())
                .build();
        var response = deviceStub.getState(request);
        return response.getState() == DeviceState.HIGH ? DigitalState.HIGH : DigitalState.LOW;
    }

    @Override
    public DigitalOutput state(DigitalState state) throws IOException {
        var request = SetDigitalStateRequest.newBuilder()
                .setDeviceId(this.config.id())
                .setState(state == DigitalState.HIGH ? DeviceState.HIGH : DeviceState.LOW)
                .build();
        var response = deviceStub.setState(request);
        assert response.getState() == (state == DigitalState.HIGH ? DeviceState.HIGH : DeviceState.LOW);
        return this;
    }
}
