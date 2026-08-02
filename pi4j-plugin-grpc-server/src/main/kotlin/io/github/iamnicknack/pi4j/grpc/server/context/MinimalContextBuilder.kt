package io.github.iamnicknack.pi4j.grpc.server.context

import com.pi4j.context.ContextBuilder
import com.pi4j.context.ContextConfig
import com.pi4j.extension.Plugin
import com.pi4j.provider.Provider

/**
 * Builder for creating a minimal context which exposes an additional [providerPlugin] property
 */
class MinimalContextBuilder : ContextBuilder() {
    private var providerPlugin: Plugin? = null

    /**
     * Specify the provider plugin to be used by the minimal context
     */
    fun providerPlugin(plugin: Plugin): MinimalContextBuilder {
        this.providerPlugin = plugin
        return this
    }

    /**
     * Build a minimal context with the current configuration
     */
    override fun build(): MinimalContext {
        val config = ConfigImpl(super.toConfig())
        return MinimalContext(config)
    }

    private inner class ConfigImpl(
        private val contextConfig: ContextConfig
    ) : ContextConfig by contextConfig, MinimalContextConfig {
        override fun getProviders(): Collection<Provider<*, *, *>> = contextConfig.providers

        override fun getAutoDetectProviders(): Boolean = contextConfig.autoDetectProviders

        override fun isAutoDetectProviders(): Boolean = contextConfig.isAutoDetectProviders

        override fun providerPlugin(): Plugin? = this@MinimalContextBuilder.providerPlugin
    }


}