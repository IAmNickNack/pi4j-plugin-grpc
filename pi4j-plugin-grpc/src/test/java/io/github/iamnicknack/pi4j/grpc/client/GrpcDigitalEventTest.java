package io.github.iamnicknack.pi4j.grpc.client;

import com.pi4j.context.Context;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@ExtendWith(Pi4jGrpcExtension.class)
public class GrpcDigitalEventTest {

    @Test
    void test(@Pi4jGrpcExtension.Local Context localPi4j) throws InterruptedException {
        var device = localPi4j.create(
                DigitalOutput.newConfigBuilder(localPi4j)
                        .id("test-output")
                        .bcm(4)
                        .initial(DigitalState.LOW)
                        .build()
        );

        // Not sure why this is needed.
        // Presumably to allow time for the device to be fully initialised
        Thread.sleep(500);

        var latch = new CountDownLatch(2);
        device.addListener(ignored -> latch.countDown());

        device.state(DigitalState.HIGH);
        device.state(DigitalState.LOW);

        assert latch.await(5, TimeUnit.SECONDS);
    }
}
