package io.github.iamnicknack.pi4j.grpc.server.config

import com.pi4j.Pi4J
import com.pi4j.boardinfo.util.BoardInfoHelper
import com.pi4j.context.Context
import com.pi4j.plugin.ffm.providers.gpio.DigitalInputFFMProviderImpl
import com.pi4j.plugin.ffm.providers.gpio.DigitalOutputFFMProviderImpl
import com.pi4j.plugin.ffm.providers.i2c.I2CFFMProviderImpl
import com.pi4j.plugin.ffm.providers.pwm.PwmFFMProviderImpl
import com.pi4j.plugin.ffm.providers.spi.SpiFFMProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalInputProviderImpl
import com.pi4j.plugin.mock.provider.gpio.digital.MockDigitalOutputProviderImpl
import com.pi4j.plugin.mock.provider.i2c.MockI2CProviderImpl
import com.pi4j.plugin.mock.provider.pwm.MockPwmProviderImpl
import com.pi4j.plugin.mock.provider.spi.MockSpiProviderImpl
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalInputProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.gpio.GrpcDigitalOutputProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.i2c.GrpcI2CProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.pwm.GrpcPwmProvider
import io.github.iamnicknack.pi4j.grpc.client.provider.spi.GrpcSpiProvider
import io.github.iamnicknack.pi4j.grpc.server.config.ServerContext.Companion.DEFAULT_CHANNEL_CONFIGURER
import io.github.iamnicknack.pi4j.grpc.server.service.DeviceConfigService
import io.github.iamnicknack.pi4j.grpc.server.service.DigitalInputService
import io.github.iamnicknack.pi4j.grpc.server.service.DigitalOutputService
import io.github.iamnicknack.pi4j.grpc.server.service.I2CService
import io.github.iamnicknack.pi4j.grpc.server.service.PwmService
import io.github.iamnicknack.pi4j.grpc.server.service.SpiService
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import io.grpc.Server
import io.grpc.ServerBuilder
import io.grpc.protobuf.services.ProtoReflectionServiceV1
import org.slf4j.LoggerFactory

/**
 * Server context responsible for starting and stopping the gRPC server.
 *
 * The context is responsible for identifying the requested plugins, configuring the pi4j context,
 * and starting the gRPC server.
 *
 * @param pluginPreference The preferred plugin to use. Defaults to "auto".
 * @param proxyHost The host of the pi4j server when running as a proxy. Defaults to null.
 * @param proxyPort The port of the pi4j server when running as a proxy. Defaults to null.
 * @param channelConfigurer A function to configure the gRPC channel. Defaults to [DEFAULT_CHANNEL_CONFIGURER].
 */
class ServerContext(
    pluginPreference: String = PLUGIN_PROPERTY_DEFAULT,
    private val serverPort: Int = System.getProperty(SERVER_PORT_PROPERTY, "9090").toInt(),
    private val proxyHost: String? = null,
    private val proxyPort: Int? = null,
    private val channelConfigurer: ChannelConfigurer = DEFAULT_CHANNEL_CONFIGURER
) {

    private val pluginPreference: String = pluginPreference.lowercase()

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * A gRPC channel to the pi4j server if required properties are set
     */
    private val proxyChannel: ManagedChannel? by lazy {
        if (proxyHost != null && proxyPort != null) {
            channelConfigurer.configure(
                ManagedChannelBuilder.forAddress(proxyHost, proxyPort)
            ).build()
        } else {
            null
        }
    }

    /**
     * A singleton instance of pi4j context with the configured plugins.
     */
    val pi4j: Context by lazy {
        logger.info("Plugin preference: ${this.pluginPreference}")

        val builder = Pi4J.newContextBuilder()

        val context = when (this.pluginPreference.lowercase()) {
            "ffm" if BoardInfoHelper.runningOnRaspberryPi()
                .also {
                    if (!it) logger.warn("Cannot load FFM. Compatible device not present")
                } -> {
                builder
                    .add(DigitalInputFFMProviderImpl())
                    .add(DigitalOutputFFMProviderImpl())
                    .add(I2CFFMProviderImpl())
                    .add(SpiFFMProviderImpl())
                    .add(PwmFFMProviderImpl())
                    .build()
            }
            "grpc" if (proxyChannel != null)
                .also {
                    if (!it) logger.warn("Cannot configure gRPC")
                } -> {
                builder
                    .add(GrpcDigitalInputProvider(proxyChannel))
                    .add(GrpcDigitalOutputProvider(proxyChannel))
                    .add(GrpcI2CProvider(proxyChannel))
                    .add(GrpcSpiProvider(proxyChannel))
                    .add(GrpcPwmProvider(proxyChannel))
                    .build()
            }
            "mock" -> {
                builder
                    .add(MockDigitalInputProviderImpl())
                    .add(MockDigitalOutputProviderImpl())
                    .add(MockI2CProviderImpl())
                    .add(MockSpiProviderImpl())
                    .add(MockPwmProviderImpl())
                    .build()
            }
            else -> {
                logger.info("Auto detecting providers")
                builder.autoDetect().build()
            }
        }

        context.providers().all
            .forEach { (key, value) -> logger.info("Provider: $key -> ${value.javaClass}") }

        context
    }

    /**
     * A singleton gRPC server instance.
     */
    val server: Server by lazy {
        ServerBuilder.forPort(serverPort)
            .addService(DigitalInputService(pi4j))
            .addService(DigitalOutputService(pi4j))
            .addService(PwmService(pi4j))
            .addService(SpiService(pi4j))
            .addService(I2CService(pi4j))
            .addService(DeviceConfigService(pi4j))
            .addService(ProtoReflectionServiceV1.newInstance())
            .build()
    }

    /**
     * Start the gRPC server.
     */
    fun start() {
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("*** shutting down gRPC server since JVM is shutting down")
                shutdown()
                logger.info("*** server shut down")
            }
        )

        server.start()
        logger.info("gRPC server listening on $serverPort")
        server.awaitTermination()
    }

    /**
     * Tidy up after ourselves
     */
    fun shutdown() {
        pi4j.shutdown()
        server.shutdown()
        proxyChannel?.shutdown()
    }

    companion object {
        const val PLUGIN_PROPERTY = "pi4j.plugin"
        const val PLUGIN_PROPERTY_DEFAULT = "auto"
        const val PROXY_HOST_PROPERTY = "pi4j.grpc.host"
        const val PROXY_PORT_PROPERTY = "pi4j.grpc.port"
        const val SERVER_PORT_PROPERTY = "pi4j.server.port"

        /**
         * By default, the channel is configured to use plaintext/unencrypted communication.
         */
        val DEFAULT_CHANNEL_CONFIGURER: ChannelConfigurer = ChannelConfigurer { it.usePlaintext() }
    }

    fun interface ChannelConfigurer {
        fun configure(builder: ManagedChannelBuilder<*>): ManagedChannelBuilder<*>
    }
}