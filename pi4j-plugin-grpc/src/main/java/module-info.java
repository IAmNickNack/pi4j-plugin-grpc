module pi4j.plugin.grpc {
    requires com.google.protobuf;
    requires com.google.common;
    requires com.pi4j;
    requires io.grpc;
    requires io.grpc.protobuf;
    requires io.grpc.stub;
    requires jakarta.annotation;
    requires org.slf4j;

    exports io.github.iamnicknack.pi4j.grpc.client;
    exports io.github.iamnicknack.pi4j.grpc.client.provider.gpio;
    exports io.github.iamnicknack.pi4j.grpc.client.provider.i2c;
    exports io.github.iamnicknack.pi4j.grpc.client.provider.pwm;
    exports io.github.iamnicknack.pi4j.grpc.client.provider.spi;

    provides com.pi4j.extension.Plugin
            with io.github.iamnicknack.pi4j.grpc.client.GrpcPlugin;
}