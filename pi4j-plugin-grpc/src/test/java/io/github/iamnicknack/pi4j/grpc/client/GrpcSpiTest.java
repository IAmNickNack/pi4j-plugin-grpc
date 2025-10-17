package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.spi.Spi;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Pi4jGrpcExtension.class)
class GrpcSpiTest {

    @Test
    void canCreateDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), Spi.class);
        assertThat(remoteDevice.config().properties()).containsAllEntriesOf(device.config().properties());

        localPi4j.registry().remove(device.id());
        assertThat(localPi4j.registry().exists("test-spi")).isFalse();
        assertThat(remotePi4j.registry().exists("test-spi")).isFalse();
    }

    @Test
    void canRead(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);

        var remoteDevice = remotePi4j.registry().get(device.id(), Spi.class);
        remoteDevice.write(42);

        var result = device.read();
        assertThat(result).isEqualTo(42);
    }

    @Test
    void canWrite(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);

        device.write(42);

        var remoteDevice = remotePi4j.registry().get(device.id(), Spi.class);
        var result = remoteDevice.read();
        assertThat(result).isEqualTo(42);
    }

    @Test
    void canTransfer(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), Spi.class);

        var outBuffer = new byte[] { 1, 2, 3 };
        var inBuffer = new byte[] { 4, 5, 6 };

        remoteDevice.write(new byte[] { 7, 8, 9 });

        device.transfer(outBuffer, 0, inBuffer, 0, 3);
        assertThat(inBuffer).containsExactly(7, 8, 9);
        assertThat(outBuffer).containsExactly(1, 2, 3);
    }

    private Spi createTestDevice(Context pi4j) {
        return pi4j.create(
                Spi.newConfigBuilder(pi4j)
                        .id("test-spi")
                        .address(1)
                        .build()
        );
    }
}