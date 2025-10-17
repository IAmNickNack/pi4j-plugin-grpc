package io.github.iamnicknack.pi4j.grpc.client.provider.spi;

import com.google.protobuf.ByteString;
import com.pi4j.context.Context;
import com.pi4j.exception.ShutdownException;
import com.pi4j.io.spi.Spi;
import com.pi4j.io.spi.SpiBase;
import com.pi4j.io.spi.SpiConfig;
import io.github.iamnicknack.pi4j.grpc.gen.device.DataRequest;
import io.github.iamnicknack.pi4j.grpc.gen.device.SpiServiceGrpc;
import io.grpc.Channel;

public class GrpcSpi extends SpiBase {

    private final SpiServiceGrpc.SpiServiceBlockingStub deviceStub;
    private final GrpcSpiProvider provider;

    public GrpcSpi(
            Channel channel,
            GrpcSpiProvider provider,
            SpiConfig config
    ) {
        super(provider, config);
        this.deviceStub = SpiServiceGrpc.newBlockingStub(channel);
        this.provider = provider;
    }

    @Override
    public Spi shutdownInternal(Context context) throws ShutdownException {
        super.shutdownInternal(context);
        provider.removeDevice(this);
        return this;
    }

    @Override
    public int transfer(byte[] write, int writeOffset, byte[] read, int readOffset, int numberOfBytes) {
        var request = DataRequest.newBuilder()
                .setDeviceId(config.id())
                .setPayload(ByteString.copyFrom(write, writeOffset, numberOfBytes))
                .build();

        var result = deviceStub.transfer(request);
        var bytes = result.getPayload().toByteArray();
        System.arraycopy(bytes, 0, read, readOffset, bytes.length);
        return bytes.length;
    }

    @Override
    public int read() {
        var buffer = new byte[1];
        this.read(buffer, 0, 1);
        return buffer[0];
    }

    @Override
    public int read(byte[] buffer, int offset, int length) {
        return transfer(new byte[buffer.length], offset, buffer, offset, length);
    }

    @Override
    public int write(byte b) {
        var sendBuffer = new byte[] { b };
        this.write(sendBuffer, 0, 1);
        return 1;
    }

    @Override
    public int write(byte[] data, int offset, int length) {
        return transfer(data, offset, new byte[length], 0, length);
    }
}
