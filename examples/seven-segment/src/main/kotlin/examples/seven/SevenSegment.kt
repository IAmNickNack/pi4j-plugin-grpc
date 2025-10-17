package examples.seven

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalState
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmType
import com.pi4j.io.spi.Spi
import com.pi4j.io.spi.SpiBus
import com.pi4j.io.spi.SpiMode
import com.pi4j.plugin.ffm.providers.gpio.DigitalOutputFFMProviderImpl
import com.pi4j.plugin.ffm.providers.pwm.PwmFFMProviderImpl
import com.pi4j.plugin.ffm.providers.spi.SpiFFMProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import com.pi4j.plugin.mock.provider.pwm.MockPwmProviderImpl
import com.pi4j.plugin.mock.provider.spi.MockSpiProviderImpl
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.pwm.GrpcPwmProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.spi.GrpcSpiProvider
import io.grpc.Grpc
import io.grpc.InsecureChannelCredentials

class SevenSegment : AutoCloseable {

    val pi4j: Context by lazy {
        when (System.getProperty("pi4j.plugin", "mock")) {
            "grpc" -> {
                val channel = Grpc.newChannelBuilderForAddress(
                    System.getProperty("pi4j.grpc.host"),
                    System.getProperty("pi4j.grpc.port", "9090").toInt(),
                    InsecureChannelCredentials.create()
                ).build()
                Pi4J.newContextBuilder()
                    .add(GrpcDigitalOutputProvider(channel))
                    .add(GrpcPwmProvider(channel))
                    .add(GrpcSpiProvider(channel))
                    .build()
            }
            "ffm" -> Pi4J.newContextBuilder()
                .add(DigitalOutputFFMProviderImpl())
                .add(PwmFFMProviderImpl())
                .add(SpiFFMProviderImpl())
                .build()
            else -> Pi4J.newContextBuilder()
                .add(MockDigitalOutputProviderImpl())
                .add(MockPwmProviderImpl())
                .add(MockSpiProviderImpl())
                .build()
        }
    }

    val resetPin: DigitalOutput = pi4j.create(
        DigitalOutput.newConfigBuilder(pi4j)
            .id("reset")
            .name("Reset Pin")
            .address(5)
            .shutdown(DigitalState.LOW)
            .initial(DigitalState.HIGH)
            .build()
    )

    val pwm: Pwm = pi4j.create(
        Pwm.newConfigBuilder(pi4j)
            .id("refresh")
            .name("Refresh PWM")
            // v3
//            .address(2)
//            .busNumber(0)
            // v4
            .address(0)
            .busNumber(2)
            .pwmType(PwmType.HARDWARE)
            .frequency(220)
            .dutyCycle(50)
            .shutdown(0)
            .build()
    )

    val spi: Spi = pi4j.create(
        Spi.newConfigBuilder(pi4j)
            .id("spi")
            .name("SPI")
            .baud(1000000)
            .bus(SpiBus.getByNumber(1))
            .mode(SpiMode.MODE_0)
            .address(0)
            .build()
    )

    override fun close() {
        pi4j.shutdown()
    }
}

fun main() {
    SevenSegment().use { sevenSegment ->
        sevenSegment.resetPin.high()
        sevenSegment.resetPin.low()
        sevenSegment.resetPin.high()

        sevenSegment.pwm.on()

        for (i in 0..10000) {
            sevenSegment.spi.write(Segments.toBytes(i))
        }
    }
}

object Segments {
    const val SEG_A: Int = 1
    val SEG_B: Int = 1 shl 2
    val SEG_C: Int = 1 shl 6
    val SEG_D: Int = 1 shl 4
    val SEG_E: Int = 1 shl 3
    val SEG_F: Int = 1 shl 1
    val SEG_G: Int = 1 shl 7
    val SEG_H: Int = 1 shl 5

    val SEVEN_SEGMENT_DIGITS: IntArray = intArrayOf(
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_E or SEG_F,  // 0
        SEG_B or SEG_C,  // 1
        SEG_A or SEG_B or SEG_D or SEG_E or SEG_G,  // 2
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_G,  // 3
        SEG_B or SEG_C or SEG_F or SEG_G,  // 4
        SEG_A or SEG_C or SEG_D or SEG_F or SEG_G,  // 5
        SEG_A or SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // 6
        SEG_A or SEG_B or SEG_C,  // 7
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // 8
        SEG_A or SEG_B or SEG_C or SEG_D or SEG_F or SEG_G,  // 9
        SEG_A or SEG_B or SEG_C or SEG_E or SEG_F or SEG_G,  // A
        SEG_C or SEG_D or SEG_E or SEG_F or SEG_G,  // b
        SEG_A or SEG_D or SEG_E or SEG_F,  // C
        SEG_B or SEG_C or SEG_D or SEG_E or SEG_G,  // d
        SEG_A or SEG_D or SEG_E or SEG_F or SEG_G,  // E
        SEG_A or SEG_E or SEG_F or SEG_G // F
    )

    fun toBytes(value: Int): ByteArray {
        return byteArrayOf(
            SEVEN_SEGMENT_DIGITS[(value % 10)].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 10) % 10].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 100) % 10].toByte(),
            SEVEN_SEGMENT_DIGITS[(value / 1000) % 10].toByte()
        )
    }
}