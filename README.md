# Pi4J gRPC Plugin

This plugin intends to provide remote development support for PI4J in the absence of 
[pigpiod](https://abyz.me.uk/rpi/pigpio/pigpiod.html) support for the Raspberry Pi v5. It assumes that convenience 
is more important than performance during the development phase of a project and intends to make existing Pi4j APIs 
accessible via gRPC.

Current supported providers extend to:

* GPIO (with event listeners)
* PWM
* I2C
* SPI

The implementation focuses on providing functional client plugins together with a draft  gRPC schema. 
It is intended either as a functional Pi4J server, or to be used as a starting point for building alternative 
client or server implementations. 

This project includes a reference implementation of the server application, which uses the existing Pi4j 
library to provide hardware integration.

Schema definitions are contained in
[./pi4j-plugin-grpc-proto/src/main/proto/pi4j](./pi4j-grpc/pi4j-plugin-grpc-proto/src/main/proto/pi4j)


## Install From Source

Both the client and server-side components can be built from source for Java 24:

```bash
# Clone the repository and build the plugin
git clone https://github.com/iamnicknack/pi4j-plugin-grpc.git
# Build the plugin and publish it to the local Maven repository
./gradlew publishToMavenLocal   
```

### Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack.pi4j</groupId>
    <artifactId>pi4j-plugin-grpc</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.iamnicknack.pi4j:pi4j-plugin-grpc:0.0.1-SNAPSHOT")
```

## Install from Github Packages

Builds are currently published to Github Packages. In order to pull packages using Maven or Gradle, you will need
to add the registry configuration to your build,
as documented on Github for Maven [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry)
and Gradle [here](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-gradle-registry#using-a-published-package).

### Gradle Repository Configuration:

Gradle allows the use of `exclusiveContent`, meaning that the repository will only be queried for the specified
packages:

```kotlin
/**
 * When referencing the package manager for the Github repo, this needs to be configured to:
 * - Authenticate using a Github PAT
 * - Only pull packages contained in that repo
 */
repositories {
    exclusiveContent {
        forRepository {
            maven {
                url = uri("https://maven.pkg.github.com/iamnicknack/pi4j-grpc-plugin")
                credentials {
                    username = System.getenv("GITHUB_USERNAME")
                    password = System.getenv("GITHUB_TOKEN")
                }
            }
        }
        filter {
            includeModule("io.github.iamnicknack.pi4j", "pi4j-plugin-grpc")
            includeModule("io.github.iamnicknack.pi4j", "pi4j-plugin-grpc-server")
        }
    }
}
```

## Server Implementation

The server is simply a gRPC proxy exposing the Pi4j library. It is intended to be used in conjunction with the client
plugin to provide remote development support for projects use Pi4j but do not have access to a Pi4j compatible device.

A shadow jar will be generated as part of the build output, but is also downloadable from the 
[repository releases](https://github.com/IAmNickNack/pi4j-plugin-grpc/releases). 

Once built, as described above, the server can be started by invoking the shadow jar:

```bash
./pi4j-plugin-grpc-server/start-server.sh <port>
```

Alternatively, start the shadow jar:

```bash
java -jar ./pi4j-plugin-grpc-server/build/libs/pi4j-plugin-grpc-server-all.jar
```

### Server Plugin Detection

Unless otherwise specified, the server will start and auto-detect Pi4j plugins. 

Auto-detection can be overridden by providing the `pi4j.plugin` system property:

* `-Dpi4j.plugin=ffm`: forces the FFM plugin to be loaded
* `-Dpi4j.plugin=mock`: loads the mock plugin. This can be useful for testing and allows the server to run on machines
with no GPIO access.
* `-Dpi4j.plugin.grpc -Dpi4j.grpc.server=<hostname> -Dpi4j.grpc.port=<port>`: starts the server as a proxy to another
server instance. The utility of this is slightly questionable ;)

## Examples

Examples contained in the `examples` directory should be reasonably self-explanatory and runnable as-is. 
At least within an IDE.

All examples are packaged as executable JARs and can be run directly from the command line after a successful build.

System properties can be provided to configure the examples depending on the use case:

* `-Dpi4j.grpc.host` - the host to bind the plugin to
* `-Dpi4j.grpc.port` - the port to bind the plugin to
* `-Dpi4j.plugin` - the plugin to use (either `http` or `grpc`). If not provided, defaults to `grpc`. 
Any other value results in `mock` and runs without any hardware access.

### Basic I2C

Demonstrates `DigitalOuptut` and `I2C` access.

```bash
java -Dpi4j.grpc.host=localhost -Dpi4j.grpc.port=9090 -jar ./examples/basic-i2c/build/libs/basic-i2c-all.jar
```

### Gpio Events

Demonstrates `DigitalOuptut` access with event listeners.

```bash
java -Dpi4j.grpc.host=localhost -Dpi4j.grpc.port=9090 -jar ./examples/gpio-events/build/libs/gpio-events-all.jar
```

### Seven Segment

Demonstrates `DigitalOuptut`, `Pwm` and `Spi` access in the unlikely event that you have same hardware setup as me, 
this example might display an incrementing counter on a seven-segment display.

```bash
java -Dpi4j.grpc.host=localhost -Dpi4j.grpc.port=9090 -jar ./examples/seven-segment/build/libs/seven-segment-all.jar
```
