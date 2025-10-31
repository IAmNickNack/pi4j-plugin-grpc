package io.github.iamnicknack.pi4j.grpc.server.config.args

/**
 * Container for details of a command line argument.
 * @param name the name of the argument
 * @param defaultValue the default value of the argument
 * @param description the description of the argument
 * @param systemProperty the system property name to use for the argument
 * @param isFlag whether the argument is a flag (i.e. no value associated with it)
 * @param value the value of the argument (or null if arguments are not yet parsed)
 */
data class CommandLineArgument(
    val name: String,
    val defaultValue: String? = null,
    val description: String? = null,
    val systemProperty: String? = null,
    val isFlag: Boolean = false,
    val value: String? = defaultValue,
) {
    inline fun <reified T> typed(): T? {
        return when (T::class) {
            String::class -> (value ?: defaultValue) as T?
            Int::class -> (value ?: defaultValue)?.toInt() as T?
            Boolean::class -> (value ?: defaultValue)?.toBoolean() as T?
            else -> null
        }
    }
}