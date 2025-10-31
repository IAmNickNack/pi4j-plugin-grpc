package io.github.iamnicknack.pi4j.grpc.server.config.args

/**
 * Helper class for accessing parsed command line arguments.
 */
class CommandLineArgs(
    val values: Map<String, CommandLineArgument>,
) {
    /**
     * @param arg the argument name
     * @param T the type of the argument value
     * @return the value of the argument as [T]
     */
    inline fun <reified T> value(arg: String): T {
        return values[arg]?.typed<T>() ?: throw IllegalArgumentException("Missing value for argument: $arg")
    }

    /**
     * @param arg the argument name
     * @param T the type of the argument value
     * @return the value of the argument as [T] or null if the argument is not present
     */
    inline fun <reified T> valueOrNull(arg: String): T? {
        return values[arg]?.typed<T>()
    }

    /**
     * @param arg the flag name
     * @return true if the flag is present, false otherwise
     */
    fun flag(arg: String): Boolean {
        return values[arg]?.typed<Boolean>() ?: false
    }
}