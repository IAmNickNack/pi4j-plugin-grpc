package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CImplementation;
import com.pi4j.plugin.mock.provider.i2c.MockI2C;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Pi4jGrpcExtension.class)
class GrpcI2CTest {

    @Test
    void canCreateDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        assertThat(remotePi4j.registry().exists(device.id())).isTrue();
        assertThat(localPi4j.registry().exists(device.id())).isTrue();

        localPi4j.registry().remove(device.id());
        assertThat(localPi4j.registry().exists(device.id())).isFalse();
        assertThat(remotePi4j.registry().exists(device.id())).isFalse();
    }

    @Test
    void canWrite(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), MockI2C.class);

        device.write(42);
        var result = remoteDevice.read();
        assertThat(result).isEqualTo(42);
    }

    @Test
    void canRead(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), MockI2C.class);

        remoteDevice.write(42);
        var result = device.read();
        assertThat(result).isEqualTo(42);
    }

    @Test
    void canWriteRegister(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), MockI2C.class);

        device.writeRegister(3, 73);
        var result = remoteDevice.readRegister(3);
        assertThat(result).isEqualTo(73);
    }

    @Test
    void canReadRegister(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), MockI2C.class);

        remoteDevice.writeRegister(3, 73);
        var result = device.readRegister(3);
        assertThat(result).isEqualTo(73);
    }

    @Test
    void canCreateDirectI2CDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var config = I2C.newConfigBuilder(localPi4j)
                .bus(1)
                .device(0x20)
                .i2cImplementation(I2CImplementation.DIRECT)
                .build();

        var device = localPi4j.create(config);
        assertThat(device.config().i2cImplementation()).isEqualTo(I2CImplementation.DIRECT);

        var remoteDevice = remotePi4j.registry().get(device.id(), I2C.class);
        assertThat(remoteDevice.config().i2cImplementation()).isEqualTo(I2CImplementation.DIRECT);
    }


    private I2C createTestDevice(Context pi4j) {
        return pi4j.create(
                I2C.newConfigBuilder(pi4j)
                        .id("test-i2c")
                        .device(0x20)
                        .bus(1)
                        .build()
        );
    }
}