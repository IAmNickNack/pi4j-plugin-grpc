package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalInput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Pi4jGrpcExtension.class)
class GrpcDigitalInputTest {

    @Test
    void canCreateDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        assertThat(remotePi4j.registry().exists("test-input")).isTrue();
        assertThat(device.state()).isEqualTo(DigitalState.LOW);

        var remoteInput = remotePi4j.registry().get("test-input", MockDigitalInput.class);
        remoteInput.mockState(DigitalState.HIGH);

        assertThat(device.state()).isEqualTo(DigitalState.HIGH);

        localPi4j.registry().remove(device.id());
        assertThat(localPi4j.registry().exists("test-input")).isFalse();
        assertThat(remotePi4j.registry().exists("test-input")).isFalse();
    }

    @Test
    void deviceIsRemovedAtShutdown(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        assertThat(remotePi4j.registry().exists(device.id())).isTrue();

        localPi4j.shutdown();
        assertThat(remotePi4j.registry().exists(device.id())).isFalse();
    }

    @Test
    void canListenToStateChanges(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        device.addListener(state -> {
            System.out.println(state);
        });

        var remoteInput = remotePi4j.registry().get("test-input", MockDigitalInput.class);

        remoteInput.mockState(DigitalState.HIGH);
        assertThat(device.state()).isEqualTo(DigitalState.HIGH);
    }

    private DigitalInput createTestDevice(Context pi4j) {
        return pi4j.create(
                DigitalInput.newConfigBuilder(pi4j)
                        .id("test-input")
                        .address(4)
                        .build()
        );
    }
}