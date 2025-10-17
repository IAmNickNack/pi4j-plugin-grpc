package io.github.iamnicknack.pi4j.grpc.server.service

import com.google.protobuf.ByteString
import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.i2c.I2C
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import io.github.iamnicknack.pi4j.grpc.gen.device.DataRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.DataResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.I2CServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.gen.device.IntegerDeviceRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.IntegerResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.ReadRegisterRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.WriteRegisterRequest
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.deviceOrThrow

class I2CService(
    private val pi4j: Context = Pi4J.newContextBuilder().add(MockDigitalInputProviderImpl()).build()
) : I2CServiceGrpcKt.I2CServiceCoroutineImplBase() {

    override suspend fun read(request: IntegerDeviceRequest): DataResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, I2C::class.java)

        val buffer = ByteArray(request.value)
        val length = device.read(buffer)

        val response = DataResponse.newBuilder()
            .setPayload(ByteString.copyFrom(buffer, 0, length))
            .build()

        return response
    }

    override suspend fun write(request: DataRequest): IntegerResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, I2C::class.java)

        val payload = request.payload.toByteArray()
        val length = device.write(payload, 0, payload.size)

        val response = IntegerResponse.newBuilder()
            .setValue(length)
            .build()

        return response
    }

    override suspend fun readRegister(request: ReadRegisterRequest): DataResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, I2C::class.java)

        val buffer = ByteArray(request.length)
        val length = device.readRegister(request.register, buffer, 0, request.length)

        val response = DataResponse.newBuilder()
            .setPayload(ByteString.copyFrom(buffer, 0, length))
            .build()

        return response
    }

    override suspend fun writeRegister(request: WriteRegisterRequest): IntegerResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, I2C::class.java)

        val payload = request.payload.toByteArray()
        val length = device.writeRegister(request.register, payload, 0, payload.size)

        val response = IntegerResponse.newBuilder()
            .setValue(length)
            .build()

        return response
    }
}
