package examples.events

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.plugin.ffm.providers.gpio.FFMDigitalOutputProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import io.github.iamnicknack.pi4j.grpc.client.GrpcHostAndPort
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider
import io.grpc.Channel
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class GpioEvents : AutoCloseable {

    val channel: Channel by lazy {
        val hostAndPort = GrpcHostAndPort.fromPropertiesWithDefaults(
            System.getProperties(),
            "localhost",
            9090
        )

        Grpc.newChannelBuilderForAddress(
            hostAndPort.host,
            hostAndPort.port,
            InsecureChannelCredentials.create()
        ).build()
    }

    val pi4j: Context by lazy {
        when (System.getProperty("pi4j.plugin", "grpc")) {
            "grpc" -> {
                Pi4J.newContextBuilder()
                    .add(GrpcDigitalOutputProvider(channel))
                    .build()
            }
            "ffm" -> Pi4J.newContextBuilder()
                .add(FFMDigitalOutputProviderImpl())
                .build()
            else -> Pi4J.newContextBuilder()
                .add(MockDigitalOutputProviderImpl())
                .build()
        }
    }

    val output: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("test-output")
            .bcm(5)
            .shutdown(DigitalState.LOW)
            .build()
    )

    override fun close() {
        pi4j.shutdown()
    }
}

fun main() {
    val logger = LoggerFactory.getLogger("main")
    val count = AtomicInteger(0)

    GpioEvents().use { gpioEvents ->

        gpioEvents.output.addListener({
            logger.info("############# Digital output state changed: ${it.state()} ############# ")
            count.incrementAndGet()
        })

        Thread.sleep(500)

        gpioEvents.output.high()
        Thread.sleep(1000)

        gpioEvents.output.low()
        Thread.sleep(1000)

        gpioEvents.output.high()
        Thread.sleep(1000)

    }

    logger.info("############# Finished ############# ")
    logger.info("############# Count: ${count.get()} ############# ")
}
