package io.github.iamnicknack.pi4j.grpc.server

import com.pi4j.context.Context
import com.pi4j.io.IO
import com.pi4j.io.IOType
import com.pi4j.io.gpio.digital.DigitalState
import io.github.iamnicknack.pi4j.grpc.gen.config.DeviceType
import io.github.iamnicknack.pi4j.grpc.gen.device.DeviceState
import io.grpc.Status

object Pi4jGrpcExt {

    /**
     * Fetch Pi4J device by device id or throw grpc status exception if not found.
     */
    fun <T : IO<T, *, *>> Context.deviceOrThrow(deviceId: String, type: Class<T>): T {
        if (!this.registry().exists(deviceId)) {
            throw Status.NOT_FOUND
                .withDescription("${type.simpleName} device `$deviceId` not found")
                .asException()
        }

        return this.registry().get<T>(deviceId)
    }

    /**
     * Convert grpc DigitalState to Pi4J DeviceState
     */
    fun DigitalState.asDeviceState(): DeviceState = when (this) {
        DigitalState.HIGH -> DeviceState.HIGH
        DigitalState.LOW -> DeviceState.LOW
        else -> DeviceState.UNRECOGNIZED
    }

    /**
     * Convert Pi4J DeviceState to Pi4J grpc DigitalState
     */
    fun DeviceState.asDigitalState(): DigitalState = when (this) {
        DeviceState.HIGH -> DigitalState.HIGH
        DeviceState.LOW -> DigitalState.LOW
        else -> DigitalState.UNKNOWN
    }

    /**
     * Convert grpc DeviceType to Pi4J IOType
     */
    fun DeviceType.asIOType(): IOType = when (this) {
        DeviceType.DIGITAL_INPUT -> IOType.DIGITAL_INPUT
        DeviceType.DIGITAL_OUTPUT -> IOType.DIGITAL_OUTPUT
        DeviceType.PWM -> IOType.PWM
        DeviceType.I2C -> IOType.I2C
        DeviceType.SPI -> IOType.SPI
        DeviceType.UNRECOGNIZED ->
            throw Status.NOT_FOUND
                .withDescription("Device type `${this.name}` not found or not supported")
                .asException()
    }
}
