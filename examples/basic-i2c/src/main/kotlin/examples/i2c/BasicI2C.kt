package examples.i2c

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CImplementation
import com.pi4j.plugin.ffm.providers.gpio.FFMDigitalOutputProviderImpl
import com.pi4j.plugin.ffm.providers.i2c.FFMI2CProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl
import io.github.iamnicknack.pi4j.grpc.client.GrpcHostAndPort
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.i2c.GrpcI2CProvider
import io.grpc.Channel
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials


class BasicI2C : AutoCloseable {

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
                    .add(GrpcI2CProvider(channel))
                    .build()
            }
            "ffm" -> {
                Pi4J.newContextBuilder()
                    .add(FFMDigitalOutputProviderImpl())
                    .add(FFMI2CProviderImpl())
                    .build()
            }
            else -> Pi4J.newContextBuilder()
                .add(MockDigitalOutputProviderImpl())
                .add(MockI2CProviderImpl())
                .build()
        }
    }

    val i2c: I2C = pi4j.create(
        I2C.newConfigBuilder(pi4j)
            .id("mcp23008")
            .bus(1)
            .device(0x20)
            .i2cImplementation(I2CImplementation.DIRECT)
            .build()
    )

    val reset: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("mcp-reset")
            .bcm(17)
            .initial(DigitalState.HIGH)
            .shutdown(DigitalState.LOW)
            .build()
    )


    override fun close() {
        pi4j.shutdown()
    }
}

fun main() {
    BasicI2C().use { basicI2C ->
        basicI2C.reset.low()
        basicI2C.reset.high()

        basicI2C.i2c.writeRegister(0, 0)     // mcp data direction
        basicI2C.i2c.writeRegister(9, 0x55)  // mcp output latch

        repeat(8) {
            val value = basicI2C.i2c.readRegister(9) xor 0xff
            basicI2C.i2c.writeRegister(9, value)
            Thread.sleep(500)
        }
    }
}