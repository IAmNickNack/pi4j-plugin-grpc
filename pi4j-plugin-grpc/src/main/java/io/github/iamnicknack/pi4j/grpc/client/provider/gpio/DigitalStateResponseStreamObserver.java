package io.github.iamnicknack.pi4j.grpc.client.provider.gpio;

import com.pi4j.io.gpio.digital.DigitalState;
import io.github.iamnicknack.pi4j.grpc.gen.device.DigitalStateResponse;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * Reusable stream observer for dispatching digital state updates
 */
class DigitalStateResponseStreamObserver implements StreamObserver<DigitalStateResponse> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Consumer<DigitalState> dispatcher;

    public DigitalStateResponseStreamObserver(Consumer<DigitalState> dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void onNext(DigitalStateResponse value) {
        DigitalState state = switch (value.getState()) {
            case HIGH -> DigitalState.HIGH;
            case LOW -> DigitalState.LOW;
            default -> DigitalState.UNKNOWN;
        };
        dispatcher.accept(state);
    }

    @Override
    public void onError(Throwable t) {
        logger.error("Error occurred while receiving digital state", t);
    }

    @Override
    public void onCompleted() {
        logger.info("Digital state stream completed");
    }
}
