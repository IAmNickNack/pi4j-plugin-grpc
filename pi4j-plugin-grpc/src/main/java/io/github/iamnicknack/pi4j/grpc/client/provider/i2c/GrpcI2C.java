package io.github.iamnicknack.pi4j.grpc.client.provider.i2c;

import com.google.protobuf.ByteString;
import com.pi4j.context.Context;
import com.pi4j.exception.InitializeException;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.i2c.*;
import io.github.iamnicknack.pi4j.grpc.gen.device.*;
import io.grpc.Channel;

import java.util.concurrent.Callable;

public class GrpcI2C extends I2CBase<GrpcI2C.NoopI2CBus> {
    private final I2CServiceGrpc.I2CServiceBlockingStub deviceStub;
    private final GrpcI2CProvider provider;

    public GrpcI2C(
            Channel channel,
            GrpcI2CProvider provider,
            I2CConfig config
    ) {
        super(provider, config, new NoopI2CBus());
        this.deviceStub = I2CServiceGrpc.newBlockingStub(channel);
        this.provider = provider;
    }

    @Override
    public I2C initialize(Context context) throws InitializeException {
        return super.initialize(context);
    }

    @Override
    public I2C shutdownInternal(Context context) throws ShutdownException {
        this.provider.removeDevice(this);
        return super.shutdownInternal(context);
    }

    @Override
    public int read() {
        var buffer = new byte[1];
        this.read(buffer, 0, 1);
        return buffer[0];
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        var response = deviceStub.read(
                IntegerDeviceRequest.newBuilder()
                        .setDeviceId(config.id())
                        .setValue(length)
                        .build()
        );
        var bytes = response.getPayload().toByteArray();
        System.arraycopy(bytes, 0, buffer, offset, length);
        return bytes.length;
    }

    @Override
    public int write(byte b) {
        var sendBuffer = new byte[] { b };
        return this.write(sendBuffer, 0, 1);
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        var request = DataRequest.newBuilder()
                .setDeviceId(config.id())
                .setPayload(ByteString.copyFrom(data, offset, length))
                .build();

        var response = deviceStub.write(request);
        return response.getValue();
    }

    @Override
    public int readRegister(int register) {
        var buffer = new byte[1];
        this.readRegister(register, buffer, 0, 1);
        return buffer[0];
    }

    @Override
    public int readRegister(byte[] register, byte[] buffer, int offset, int length) {
        return this.readRegister(register[0], buffer, offset, length);
    }

    @Override
    public int readRegister(int register, byte[] buffer, int offset, int length) {
        var request = ReadRegisterRequest.newBuilder()
                .setDeviceId(config.id())
                .setRegister(register)
                .setLength(length)
                .build();

        var response = deviceStub.readRegister(request);
        var bytes = response.getPayload().toByteArray();
        System.arraycopy(bytes, 0, buffer, offset, length);
        return response.getPayload().size();
    }

    @Override
    public int writeRegister(int register, byte b) {
        var sendBuffer = new byte[] { b };
        return writeRegister(register, sendBuffer, 0, 1);
    }

    @Override
    public int writeRegister(byte[] register, byte[] data, int offset, int length) {
        return writeRegister(register[0], data, offset, length);
    }

    @Override
    public int writeRegister(int register, byte[] data, int offset, int length) {
        var request = WriteRegisterRequest.newBuilder()
                .setDeviceId(config.id())
                .setRegister(register)
                .setPayload(ByteString.copyFrom(data, offset, length))
                .build();

        var response = deviceStub.writeRegister(request);
        return response.getValue();
    }

    /**
     * No-op implementation of I2CBus.
     */
    static class NoopI2CBus implements I2CBus {
        @Override
        public <R> R execute(I2C i2c, Callable<R> action) {
            throw new UnsupportedOperationException("HTTP I2C does not support bus operations");
        }
    }
}
