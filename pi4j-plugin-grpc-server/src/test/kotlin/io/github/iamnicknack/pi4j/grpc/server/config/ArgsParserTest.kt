package io.github.iamnicknack.pi4j.grpc.server.config

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import io.github.iamnicknack.pi4j.grpc.server.config.args.CommandLineArgument
import io.github.iamnicknack.pi4j.grpc.server.config.args.CommandLineParser
import org.junit.jupiter.api.Test

class ArgsParserTest {

    private val argWithDefaultValue = CommandLineArgument("with-default", "default value")
    private val argWithoutDefaultValue = CommandLineArgument("without-default")
    private val argWithFlag = CommandLineArgument("flag", isFlag = true)
    private val argWithSystemProperty = CommandLineArgument("with-system-property", systemProperty = "test.property")

    @Test
    fun `happy path`() {
        val parser = CommandLineParser.Builder()
            .arg(argWithDefaultValue)
            .arg(argWithFlag)
            .arg(argWithoutDefaultValue)
            .arg(argWithSystemProperty)
            .systemProperties(mapOf("test.property" to "test property"))
            .build()

        val args = parser.parse(arrayOf("--with-default", "test value", "--flag"))

        assertThat(args.value<String>(argWithDefaultValue.name)).isEqualTo("test value")
        assertThat(args.flag(argWithFlag.name)).isEqualTo(true)
        assertThat(args.valueOrNull<String>(argWithoutDefaultValue.name)).isNull()
        assertThat(args.valueOrNull<String>(argWithSystemProperty.name)).isEqualTo(System.getProperty("test property"))
    }
}