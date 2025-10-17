package io.github.iamnicknack.pi4j.grpc.server.service

import com.pi4j.Pi4J
import com.pi4j.context.Context
import com.pi4j.io.IO
import com.pi4j.io.IOConfig
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import com.pi4j.provider.Provider
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigPayload
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceConfigServiceGrpcKt
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceListRequest
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceListResponse
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceRequest
import io.github.iamnicknack.pi4j.grpc.gen.config.Empty
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.asIOType
import io.github.iamnicknack.pi4j.grpc.server.Pi4jGrpcExt.deviceOrThrow
import io.grpc.Status
import org.slf4j.LoggerFactory

@Suppress("FINITE_BOUNDS_VIOLATION_IN_JAVA")
internal class DeviceConfigServiceForType<
        T : IO<T, C, P>,
        C : IOConfig<C>,
        P : Provider<P, T, C>
        >(
    private val configFactory: ConfigFactory<C>,
    private val deviceFactory: DeviceFactory<C, T>,
    private val deviceType: Class<T>,
    private val pi4j: Context = Pi4J.newContextBuilder().add(MockDigitalInputProviderImpl()).build(),
) : DeviceConfigServiceGrpcKt.DeviceConfigServiceCoroutineImplBase() {

    private val logger = LoggerFactory.getLogger("${javaClass.name}.${deviceType.simpleName}")

    override suspend fun createDevice(request: DeviceConfigPayload): DeviceConfigPayload {
        logger.info("Creating device with config: ${request.configMap}")

        val config = configFactory.create(request.configMap)
        val deviceResult = runCatching { deviceFactory.create(pi4j, config) }

        val device = deviceResult
            .getOrElse {
                logger.error("Failed to create device", it)
                throw Status.INTERNAL
                    .withDescription(it.message ?: "Unknown error occurred")
                    .asException()
            }

        return DeviceConfigPayload.newBuilder()
            .putAllConfig(device.config().properties())
            .build()
    }

    override suspend fun removeDevice(request: DeviceRequest): Empty {
        logger.info("Removing device with id: ${request.deviceId}")

        pi4j.registry().remove<T>(request.deviceId)
        return Empty.getDefaultInstance()
    }

    override suspend fun fetchDeviceConfig(request: DeviceRequest): DeviceConfigPayload {
        logger.info("Fetching device config with id: ${request.deviceId}")

        val device = pi4j.deviceOrThrow(request.deviceId, deviceType)

        if (device.type() != request.deviceType.asIOType()) {
            logger.warn("Device is not of correct type: ${device.type()} != ${request.deviceType.asIOType()}")
            throw Status.ABORTED
                .withDescription("Device is not of correct type: ${device.type()} != ${request.deviceType.asIOType()}")
                .asException()
        }

        return DeviceConfigPayload.newBuilder()
            .setDeviceType(request.deviceType)
            .putAllConfig(device.config().properties())
            .build()
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun fetchCurrentDeviceConfigs(request: DeviceListRequest): DeviceListResponse {
        logger.info("Fetching all devices of type: ${request.deviceType}")

        val ioType = request.deviceType.asIOType()
        return pi4j.registry().allByIoType<P>(ioType)
            .map { (_, device) -> device.config() }
            .fold(DeviceListResponse.newBuilder()) { builder, config ->
                val payload = DeviceConfigPayload.newBuilder()
                    .setDeviceType(request.deviceType)
                    .putAllConfig(config.properties() as Map<String, String>)
                    .build()
                builder.addDevice(payload)
            }
            .build()
    }

    fun interface ConfigFactory<C> {
        fun create(properties: Map<String, String>): C
    }

    fun interface DeviceFactory<C, T> {
        fun create(pi4j: Context, config: C): T
    }
}
