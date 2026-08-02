package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.pwm.Pwm;
import com.pi4j.io.pwm.PwmConfigBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Pi4jGrpcExtension.class)
class GrpcPwmTest {

    @Test
    void canCreateDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        assertThat(remotePi4j.registry().exists(device.id())).isTrue();

        device.close();
        assertThat(localPi4j.registry().exists(device.id())).isFalse();
        assertThat(remotePi4j.registry().exists(device.id())).isFalse();
    }

    @Test
    void canEnable(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), Pwm.class);

        device.off();
        assertThat(remoteDevice.isOff()).isTrue();

        device.on();
        assertThat(remoteDevice.isOn()).isTrue();
    }

    @Test
    void canSetFrequency(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), Pwm.class);

        device.frequency(100);
        assertThat(remoteDevice.frequency()).isEqualTo(100);
        assertThat(device.frequency()).isEqualTo(100);
    }

    @Test
    void canSetDutyCycle(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteDevice = remotePi4j.registry().get(device.id(), Pwm.class);

        device.dutyCycle(100);
        assertThat(remoteDevice.dutyCycle()).isEqualTo(100);
        assertThat(device.dutyCycle()).isEqualTo(100);
    }

    private Pwm createTestDevice(Context pi4j) {
        return pi4j.create(PwmConfigBuilder.newInstance(pi4j)
                .id("test-pwm")
                .channel(4)
                .frequency(440)
                .dutyCycle(50)
                .build()
        );
    }
}