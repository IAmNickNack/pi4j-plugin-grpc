package io.github.iamnicknack.pi4j.grpc.server.service

import com.google.protobuf.kotlin.toByteString
import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.spi.Spi
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import io.github.iamnicknack.pi4j.grpc.gen.device.DataRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.DataResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.SpiServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.deviceOrThrow

class SpiService(
    private val pi4j: Context = Pi4J.newContextBuilder().add(MockDigitalInputProviderImpl()).build()
) : SpiServiceGrpcKt.SpiServiceCoroutineImplBase() {

    override suspend fun transfer(request: DataRequest): DataResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, Spi::class.java)

        val payload = request.payload.toByteArray()
        val buffer = ByteArray(payload.size)

        device.transfer(payload, buffer)

        return DataResponse.newBuilder()
            .setPayload(buffer.toByteString())
            .build()
    }
}
