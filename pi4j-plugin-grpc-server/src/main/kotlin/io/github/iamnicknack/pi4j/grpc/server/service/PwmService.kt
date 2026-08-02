package io.github.iamnicknack.pi4j.grpc.server.service

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.pwm.Pwm
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import io.github.iamnicknack.pi4j.grpc.gen.device.BooleanDeviceRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.BooleanResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceIdRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.DoubleDeviceRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.DoubleResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.PwmServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.deviceOrThrow

class PwmService(
    private val pi4j: Context = Pi4J.newContextBuilder().add(MockDigitalInputProviderImpl()).build()
) : PwmServiceGrpcKt.PwmServiceCoroutineImplBase() {

    override suspend fun setEnabled(request: BooleanDeviceRequest): BooleanResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        if (request.value) {
            device.on()
        } else {
            device.off()
        }
        return BooleanResponse.newBuilder().setValue(request.value).build()
    }

    override suspend fun getEnabled(request: DeviceIdRequest): BooleanResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        return BooleanResponse.newBuilder().setValue(device.isOn).build()
    }

    override suspend fun setDutyCycle(request: DoubleDeviceRequest): DoubleResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        device.dutyCycle = request.value
        return DoubleResponse.newBuilder().setValue(request.value).build()
    }

    override suspend fun getDutyCycle(request: DeviceIdRequest): DoubleResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        return DoubleResponse.newBuilder().setValue(device.dutyCycle).build()
    }

    override suspend fun setFrequency(request: DoubleDeviceRequest): DoubleResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        device.frequency = request.value
        return DoubleResponse.newBuilder().setValue(request.value).build()
    }

    override suspend fun getFrequency(request: DeviceIdRequest): DoubleResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Pwm::class.java)
        return DoubleResponse.newBuilder().setValue(device.frequency).build()
    }
}
