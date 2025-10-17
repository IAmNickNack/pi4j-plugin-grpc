package io.github.iamnicknack.pi4j.grpc.server.service

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.gpio.digital.DigitalInput
import com.pi4j.io.gpio.digital.DigitalInputConfig
import com.pi4j.io.gpio.digital.DigitalInputProvider
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalOutputConfig
import com.pi4j.io.gpio.digital.DigitalOutputProvider
import com.pi4j.io.i2c.I2C
import com.pi4j.io.i2c.I2CConfig
import com.pi4j.io.i2c.I2CProvider
import com.pi4j.io.pwm.Pwm
import com.pi4j.io.pwm.PwmConfig
import com.pi4j.io.pwm.PwmProvider
import com.pi4j.io.spi.Spi
import com.pi4j.io.spi.SpiConfig
import com.pi4j.io.spi.SpiProvider
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceListRequest
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceListResponse
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType
import io.github.iamnicknack.pi4j.grpc.gen.config.Empty

class DeviceConfigService(
    private val pi4j: Context = Pi4J.newContextBuilder().build()
) : DeviceConfigServiceGrpcKt.DeviceConfigServiceCoroutineImplBase() {

    private val digitalInputService: DeviceConfigServiceForType<DigitalInput, DigitalInputConfig, DigitalInputProvider> = DeviceConfigServiceForType(
        { DigitalInput.newConfigBuilder(pi4j).load(it).build() },
        { context, config -> context.create(config) },
        DigitalInput::class.java,
        pi4j
    )

    private val digitalOutputService: DeviceConfigServiceForType<DigitalOutput, DigitalOutputConfig, DigitalOutputProvider> = DeviceConfigServiceForType(
        { DigitalOutput.newConfigBuilder(pi4j).load(it).build() },
        { context, config -> context.create(config) },
        DigitalOutput::class.java,
        pi4j
    )

    private val pwmService: DeviceConfigServiceForType<Pwm, PwmConfig, PwmProvider> = DeviceConfigServiceForType(
        { Pwm.newConfigBuilder(pi4j).load(it).build() },
        { context, config -> context.create(config) },
        Pwm::class.java,
        pi4j
    )

    private val spiService: DeviceConfigServiceForType<Spi, SpiConfig, SpiProvider> = DeviceConfigServiceForType(
        { Spi.newConfigBuilder(pi4j).load(it).build() },
        { context, config -> context.create(config) },
        Spi::class.java,
        pi4j
    )

    private val i2cService: DeviceConfigServiceForType<I2C, I2CConfig, I2CProvider> = DeviceConfigServiceForType(
        { I2C.newConfigBuilder(pi4j).load(it).build() },
        { context, config -> context.create(config) },
        I2C::class.java,
        pi4j
    )

    private val serviceMap: Map<DeviceType, DeviceConfigServiceForType<*, *, *>> = mapOf(
        DeviceType.DIGITAL_INPUT to digitalInputService,
        DeviceType.DIGITAL_OUTPUT to digitalOutputService,
        DeviceType.PWM to pwmService,
        DeviceType.SPI to spiService,
        DeviceType.I2C to i2cService
    )

    override suspend fun createDevice(request: DeviceConfigPayload): DeviceConfigPayload {
        return serviceMap[request.deviceType]
            ?.createDevice(request)
            ?: throw IllegalArgumentException("Unsupported device type: ${request.deviceType}")
    }

    override suspend fun removeDevice(request: DeviceRequest): Empty {
        return serviceMap[request.deviceType]
            ?.removeDevice(request)
            ?: throw IllegalArgumentException("Unsupported device type: ${request.deviceType}")
    }

    override suspend fun fetchDeviceConfig(request: DeviceRequest): DeviceConfigPayload {
        return serviceMap[request.deviceType]
            ?.fetchDeviceConfig(request)
            ?: throw IllegalArgumentException("Unsupported device type: ${request.deviceType}")
    }

    override suspend fun fetchCurrentDeviceConfigs(request: DeviceListRequest): DeviceListResponse {
        return serviceMap[request.deviceType]
            ?.fetchCurrentDeviceConfigs(request)
            ?: throw IllegalArgumentException("Unsupported device type: ${request.deviceType}")
    }
}
