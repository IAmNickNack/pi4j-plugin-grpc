package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInput;
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutput;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(Pi4jGrpcExtension.class)
class GrpcDigitalOutputTest {

    @Test
    void canCreateDevice(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        assertThat(remotePi4j.registry().exists("test-output")).isTrue();
        assertThat(device.state()).isEqualTo(DigitalState.LOW);

        var remoteOutput = remotePi4j.registry().get("test-output", MockDigitalOutput.class);
        remoteOutput.mockState(DigitalState.HIGH);
        assertThat(device.state()).isEqualTo(DigitalState.HIGH);

        localPi4j.registry().remove(device.id());
        assertThat(localPi4j.registry().exists("test-output")).isFalse();
        assertThat(remotePi4j.registry().exists("test-output")).isFalse();
    }

    @Test
    void canSetState(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) {
        var device = createTestDevice(localPi4j);
        var remoteOutput = remotePi4j.registry().get("test-output", MockDigitalOutput.class);

        device.state(DigitalState.HIGH);
        assertThat(remoteOutput.state()).isEqualTo(DigitalState.HIGH);

        device.state(DigitalState.LOW);
        assertThat(remoteOutput.state()).isEqualTo(DigitalState.LOW);
    }

    @Test
    void canListenToStateChanges(
            @Pi4jGrpcExtension.Local Context localPi4j,
            @Pi4jGrpcExtension.Remote Context remotePi4j
    ) throws InterruptedException {
        var device = createTestDevice(localPi4j);
        device.addListener(state -> {
            System.out.println(state);
        });

        Thread.sleep(500);

        var remoteOutput = remotePi4j.registry().get("test-output", MockDigitalOutput.class);
        remoteOutput.addListener(state -> {
            System.out.println("Did set state to " + state);
        });
        remoteOutput.state(DigitalState.HIGH);
//        assertThat(device.state()).isEqualTo(DigitalState.HIGH);
    }

    private DigitalOutput createTestDevice(Context pi4j) {
        return pi4j.create(
                DigitalOutput.newConfigBuilder(pi4j)
                        .id("test-output")
                        .bcm(4)
                        .initial(DigitalState.LOW)
                        .build()
        );
    }

}