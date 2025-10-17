package io.github.iamnicknack.pi4j.grpc.client.provider.pwm;

import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.exception.IOException;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmBase;
import com.pi4j.io.pwm.PwmConfig;
import io.github.iamnicknack.pi4j.grpc.gen.device.PwmServiceGrpc;
import io.github.iamnicknack.pi4j.grpc.gen.device.BooleanDeviceRequest;
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceIdRequest;
import io.github.iamnicknack.pi4j.grpc.gen.device.IntegerDeviceRequest;
import io.grpc.Channel;

public class GrpcPwm extends PwmBase {

    private final PwmServiceGrpc.PwmServiceBlockingStub deviceStub;
    private final GrpcPwmProvider provider;

    public GrpcPwm(
            Channel channel,
            GrpcPwmProvider provider,
            PwmConfig config
    ) {
        super(provider, config);
        this.deviceStub = PwmServiceGrpc.newBlockingStub(channel);
        this.provider = provider;
    }

    @Override
    public Pwm shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        provider.removeDevice(this);
        return this;
    }

    @Override
    public Pwm on() throws IOException {
        var request = BooleanDeviceRequest.newBuilder()
                .setDeviceId(this.id)
                .setValue(true)
                .build();

        var result = deviceStub.setEnabled(request);
        assert result.getValue();
        return null;
    }

    @Override
    public Pwm off() throws IOException {
        var request = BooleanDeviceRequest.newBuilder()
                .setDeviceId(this.id)
                .setValue(false)
                .build();

        var result = deviceStub.setEnabled(request);
        assert !result.getValue();
        return null;
    }

    @Override
    public Pwm dutyCycle(Integer dutyCycle) throws IOException {
        var request = IntegerDeviceRequest.newBuilder()
                .setDeviceId(this.id)
                .setValue(dutyCycle)
                .build();

        var result = deviceStub.setDutyCycle(request);
        assert result.getValue() == dutyCycle;

        return this;
    }

    @Override
    public Integer dutyCycle() throws IOException {
        var request = DeviceIdRequest.newBuilder()
                .setDeviceId(this.id)
                .build();
        var response = deviceStub.getDutyCycle(request);
        return response.getValue();
    }

    @Override
    public Pwm frequency(int frequency) throws IOException {
        var request = IntegerDeviceRequest.newBuilder()
                .setDeviceId(this.id)
                .setValue(frequency)
                .build();
        var response = deviceStub.setFrequency(request);
        assert response.getValue() == frequency;
        return this;
    }

    @Override
    public int frequency() throws IOException {
        var request = DeviceIdRequest.newBuilder()
                .setDeviceId(this.id)
                .build();
        var response = deviceStub.getFrequency(request);
        return response.getValue();
    }
}
