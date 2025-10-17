package io.github.iamnicknack.pi4j.grpc.server.service

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.event.ShutdownListener
import com.pi4j.io.gpio.digital.DigitalOutput
import com.pi4j.io.gpio.digital.DigitalStateChangeListener
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceIdRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.DigitalOutputServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.gen.device.DigitalStateResponse
import io.github.iamnicknack.pi4j.grpc.gen.device.GetDigitalStateRequest
import io.github.iamnicknack.pi4j.grpc.gen.device.SetDigitalStateRequest
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.asDeviceState
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.asDigitalState
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.deviceOrThrow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.slf4j.LoggerFactory

class DigitalOutputService(
    private val pi4j: Context = Pi4J.newContextBuilder().add(MockDigitalInputProviderImpl()).build()
) : DigitalOutputServiceGrpcKt.DigitalOutputServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger(javaClass)

    override suspend fun getState(request: GetDigitalStateRequest): DigitalStateResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, DigitalOutput::class.java)

        return DigitalStateResponse.newBuilder()
            .setState(device.state().asDeviceState())
            .build()
    }

    override suspend fun setState(request: SetDigitalStateRequest): DigitalStateResponse {
        val device = pi4j.deviceOrThrow(request.deviceId, DigitalOutput::class.java)

        device.state(request.state.asDigitalState())

        return DigitalStateResponse.newBuilder()
            .setState(request.state)
            .build()
    }

    override fun addListener(request: DeviceIdRequest): Flow<DigitalStateResponse> = callbackFlow {
        val device = pi4j.deviceOrThrow(request.deviceId, DigitalOutput::class.java)
        val listener = DigitalStateChangeListener { event ->
            if (logger.isDebugEnabled) {
                logger.debug("Digital output state changed: {} ({}) = {}", event.source().name, event.source().address, event.state())
            }

            val state = event.state().asDeviceState()
            trySend(
                DigitalStateResponse.newBuilder()
                    .setState(state)
                    .build()
            )
        }
        device.addListener(listener)
        pi4j.addListener(ShutdownListener { this.close() })

        awaitClose { device.removeListener(listener) }
    }
}
