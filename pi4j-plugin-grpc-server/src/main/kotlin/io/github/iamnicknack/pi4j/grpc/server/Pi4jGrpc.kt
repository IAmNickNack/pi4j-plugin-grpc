package io.github.iamnicknack.pi4j.grpc.server

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import io.github.iamnicknack.pi4j.grpc.server.config.ServerContext
import io.github.iamnicknack.pi4j.grpc.server.config.args.CommandLineArgument
import io.github.iamnicknack.pi4j.grpc.server.config.args.CommandLineParser
import org.slf4j.LoggerFactory

private val pluginArg = CommandLineArgument(
    "plugin",
    defaultValue = ServerContext.PLUGIN_PROPERTY_DEFAULT,
    systemProperty = ServerContext.PLUGIN_PROPERTY,
    description = "The name of the pi4j plugin to use: [auto, grpc, ffm]"
)
private val portArg = CommandLineArgument(
    "port",
    defaultValue = "9090",
    systemProperty = ServerContext.SERVER_PORT_PROPERTY,
    description = "The port to listen on"
)
private val proxyHostArg = CommandLineArgument(
    "proxy-host",
    systemProperty = ServerContext.PROXY_HOST_PROPERTY,
    description = "The host of the remote pi4j server when running as a proxy"
)
private val proxyPortArg = CommandLineArgument(
    "proxy-port",
    systemProperty = ServerContext.PROXY_PORT_PROPERTY,
    description = "The port of the remote pi4j server when running as a proxy"
)
private val debugArg = CommandLineArgument(
    "debug",
    isFlag = true,
    description = "Additional logging of IO activity"
)
private val helpArg = CommandLineArgument(
    "help",
    isFlag = true,
    description = "Display this help message"
)

private val parser = CommandLineParser.Builder()
    .arg(pluginArg)
    .arg(portArg)
    .arg(proxyHostArg)
    .arg(proxyPortArg)
    .arg(debugArg)
    .arg(helpArg)
    .build()

/**
 * Allow the log level to be set based on command line arguments
 */
fun setServiceLogLevel(level: String) {
    val ctx = LoggerFactory.getILoggerFactory() as LoggerContext
    ctx.getLogger("io.github.iamnicknack.pi4j.grpc").level = Level.toLevel(level)
}

fun main(args: Array<String>) {
    val commandLineArgs = parser.parse(args)

    if (commandLineArgs.flag(helpArg.name)) {
        parser.help(System.out)
    } else {
        if (commandLineArgs.flag(debugArg.name)) {
            setServiceLogLevel("DEBUG")
        }

        ServerContext(
            commandLineArgs.value<String>(pluginArg.name),
            commandLineArgs.value<Int>(portArg.name),
            commandLineArgs.valueOrNull(proxyHostArg.name),
            commandLineArgs.valueOrNull(proxyPortArg.name)
        ).start()
    }
}
