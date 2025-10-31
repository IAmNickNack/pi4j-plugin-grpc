package io.github.iamnicknack.pi4j.grpc.server.config.args

import java.io.PrintStream
import java.util.*

/**
 * Basic command line argument parser.
 */
class CommandLineParser(
    private val argsMap: Map<String, CommandLineArgument>,
    private val systemProperties: Map<String, String> = System.getProperties().asMap(),
) {
    /**
     * Read the command line arguments and return a map of argument key to argument value.
     */
    fun parse(args: Array<String>): CommandLineArgs {
        val parsedArgs = mutableMapOf<String, String>()

        var i = 0
        while (i < args.size) {
            var token = args[i]
            // skip if this is not an argument specifier
            if (!token.startsWith("--")) {
                i++
                continue
            }

            token = token.removePrefix("--")

            // skip if argument doesn't exist
            if (!argsMap.containsKey(token)) {
                i++
                continue
            }

            val arg = argsMap[token]!!

            if (arg.isFlag) {
                parsedArgs[arg.name] = "true"
                i++
                continue
            }

            if (i < args.size - 1) {
                parsedArgs[arg.name] = args[++i]
            } else {
                throw IllegalArgumentException("Missing value for argument: ${arg.name}")
            }
        }

        for ((key, value) in argsMap.entries
            .filter { !parsedArgs.containsKey(it.key) && it.value.systemProperty != null }
            .mapNotNull { entry -> systemProperties[entry.value.systemProperty]?.let { entry.key to it } }
        ) {
            parsedArgs[key] = value
        }

        for ((key, value) in argsMap.entries
            .filter { !parsedArgs.containsKey(it.key) && it.value.defaultValue != null }
            .map { it.key to it.value.defaultValue!! }
        ) {
            parsedArgs[key] = value
        }

        return parsedArgs
            .map { it.key to argsMap[it.key]!!.copy(value = it.value) }
            .associate { it.first to it.second }
            .let(::CommandLineArgs)
    }

    /**
     * Output argument details to the specified [java.io.PrintStream].
     */
    fun help(writer: PrintStream) {
        writer.println("Arguments:")
        for (arg in argsMap.values) {
            writer.println("  --${arg.name}")
            if (arg.description != null) {
                writer.println("    ${arg.description}")
            }
            if (arg.defaultValue != null) {
                writer.println("    Default: ${arg.defaultValue}")
            }
            if (arg.systemProperty != null) {
                writer.println("    System property: ${arg.systemProperty}")
            }
        }
    }

    /**
     * Builder for [CommandLineParser].
     */
    class Builder {
        private val argsMap = mutableMapOf<String, CommandLineArgument>()
        private var systemProperties = System.getProperties()
            .map { it.key.toString() to it.value.toString() }
            .toMap()

        fun arg(arg: CommandLineArgument): Builder {
            argsMap[arg.name] = arg
            return this
        }

        fun systemProperties(systemProperties: Map<String, String>): Builder {
            this.systemProperties = systemProperties
            return this
        }

        fun build(): CommandLineParser = CommandLineParser(argsMap)
    }

    companion object {
        fun Properties.asMap(): Map<String, String> = this
            .map { it.key.toString() to it.value.toString() }
            .toMap()
    }
}