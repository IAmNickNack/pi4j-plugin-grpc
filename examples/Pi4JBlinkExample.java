///usr/bin/env jbang "$0" "$@" ; exit $?

//REPOS central,snapshots=https://central.sonatype.com/repository/maven-snapshots/
//DEPS org.slf4j:slf4j-simple:2.0.17
//DEPS io.github.iamnicknack:pi4j-plugin-grpc:0.0.1-SNAPSHOT
//DEPS com.pi4j:pi4j-plugin-mock:4.0.1
//JAVA_OPTIONS -Dpi4j.grpc.host=localhost -Dpi4j.grpc.port=9090

import com.pi4j.Pi4J;
import com.pi4j.io.gpio.digital.DigitalOutput;
import com.pi4j.io.gpio.digital.DigitalState;

public class Pi4JBlinkExample {

    private static final int PIN_LED = 5;

    public static void main(String[] args) throws Exception {

        var pi4j = Pi4J.newContextBuilder()
                .properties(System.getProperties())
                .autoDetect()
                .build();

        var outputConfig = DigitalOutput.newConfigBuilder(pi4j)
                .id("led")
                .name("Blinker")
                .address(PIN_LED)
                .shutdown(DigitalState.LOW)
                .initial(DigitalState.LOW);

        var output = pi4j.create(outputConfig);

        output.addListener(event -> System.out.println("Output state changed: " + event.state()));

        for (int i = 0; i < 10; i++) {
            output.high();
            Thread.sleep(500);
            output.low();
            Thread.sleep(500);
        }

        pi4j.shutdown();
    }
}