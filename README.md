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

## Install

There are currently no binary distributions available, but both the client and server-side components can be built
from source for Java 24:

```bash
# Clone the repository and build the plugin
git clone https://github.com/iamnicknack/pi4j-plugin-grpc.git
# Build the plugin and publish it to the local Maven repository
./gradlew publishToMavenLocal   
```

## pi4j-plugin-grpc

Schema definitions are contained in 
[./pi4j-plugin-grpc-proto/src/main/proto/pi4j](./pi4j-grpc/pi4j-plugin-grpc-proto/src/main/proto/pi4j)

### Maven

```xml
<dependency>
    <groupId>io.github.iamnicknack.pi4j</groupId>
    <artifactId>pi4j-plugin-grpc</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.iamnicknack.pi4j:pi4j-plugin-grpc:0.0.1")
```

### Server Implementation

The server is simply a gRPC proxy exposing the Pi4j library. It is intended to be used in conjunction with the client
plugin to provide remote development support for projects use Pi4j but do not have access to a Pi4j compatible device.

Once built, as described above, the server can be started by invoking the shadow jar:

```bash
./pi4j-grpc/pi4j-plugin-grpc-server/start-server.sh <port>
```

## Examples

Examples contained in the `examples` directory should be reasonably self-explanatory and runnable as-is. 
At least within an IDE.

All examples are packaged as executable JARs and can be run directly from the command line after a successful build.

System properties can be provided to configure the examples depnding on the use case:

* `-Dpi4j.host` - the `host:port` to bind the plugin to
* `-Dpi4j.plugin` - the plugin to use (either `http` or `grpc`). If not provided, defaults to `mock` and runs
without any hardware access.

### Basic I2C

Demonstrates `DigitalOuptut` and `I2C` access.

```bash
java -Dpi4j.plugin=grpc -Dpij4.host=localhost:9090 -jar ./examples/basic-i2c/build/libs/basic-i2c-all.jar
```

### Gpio Events

Demonstrates `DigitalOuptut` access with event listeners.

```bash
java java -Dpi4j.plugin=grpc -Dpi4j.host=localhost:9090 -jar ./examples/gpio-events/build/libs/gpio-events-all.jar
```

### Seven Segment

Demonstrates `DigitalOuptut`, `Pwm` and `Spi` access in the unlikely event that you have same hardware setup as me, 
this example might display an incrementing counter on a seven-segment display.

```bash
java -Dpi4j.plugin=grpc -Dpi4j.host=localhost:9090 -jar ./examples/seven-segment/build/libs/seven-segment-all.jar
```
