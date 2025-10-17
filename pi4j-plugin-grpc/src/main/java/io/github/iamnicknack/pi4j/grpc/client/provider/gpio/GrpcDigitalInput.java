package io.github.iamnicknack.pi4j.grpc.client.provider.gpio;

import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.gpio.digital.*;
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceIdRequest;
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceState;
import io.github.iamnicknack.pi4j.grpc.gen.device.DigitalInputServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.device.DigitalInputServiceGrpc.DigitalInputServiceBlockingStub;
import io.github.iamnicknack.pi4j.grpc.gen.device.GetDigitalStateRequest;
import io.grpc.Channel;
import org.slf4j.Logger;

public class GrpcDigitalInput extends DigitalInputBase {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(this.getClass());

    private final DigitalInputServiceBlockingStub deviceStub;
    private final GrpcDigitalInputProvider provider;

    public GrpcDigitalInput(
            Channel channel,
            GrpcDigitalInputProvider provider,
            DigitalInputConfig config
    ) {
        super(provider, config);
        this.deviceStub = DigitalInputServiceGrpc.newBlockingStub(channel);
        this.provider = provider;

        var callbackService = DigitalInputServiceGrpc.newStub(deviceStub.getChannel());
        callbackService.addListener(
                DeviceIdRequest.newBuilder().setDeviceId(id).build(),
                new DigitalStateResponseStreamObserver(state -> {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Digital Input state changed: {}", state);
                    }
                    dispatch(new DigitalStateChangeEvent<>(this, state));
                })
        );
    }

    @Override
    public DigitalInput initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public DigitalInput shutdownInternal(Context context) throws ShutdownException {
        provider.removeDevice(this);
        return super.shutdownInternal(context);
    }

    @Override
    public DigitalState state() {
        var request = GetDigitalStateRequest.newBuilder()
                .setDeviceId(this.config.id())
                .build();

        var response = deviceStub.getState(request);

        return response.getState() == DeviceState.HIGH ? DigitalState.HIGH : DigitalState.LOW;
    }
}
