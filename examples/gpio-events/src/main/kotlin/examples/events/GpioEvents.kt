package examples.events

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.plugin.ffm.providers.gpio.DigitalOutputFFMProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

class GpioEvents : AutoCloseable {

//    -Dpi4j.plugin=grpc -Dpi4j.host=10.0.0.2:9090

    val host: String by lazy {
        System.getProperty("pi4j.host", "localhost:8080")
    }

    val pi4j: Context by lazy {
        when (System.getProperty("pi4j.plugin", "mock")) {
            "grpc" -> {
                val channel = Grpc.newChannelBuilder(host, InsecureChannelCredentials.create()).build()
                Pi4J.newContextBuilder()
                    .add(GrpcDigitalOutputProvider(channel))
                    .build()
            }
            "ffm" -> Pi4J.newContextBuilder()
                .add(DigitalOutputFFMProviderImpl())
                .build()
            else -> Pi4J.newContextBuilder()
                .add(MockDigitalOutputProviderImpl())
                .build()
        }
    }

    val output: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("test-output")
            .address(5)
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
