package io.github.iamnicknack.pi4j.grpc.server.context;

import com.pi4j.io.IOConfig;
import com.pi4j.io.IOType;
import com.pi4j.io.gpio.digital.DigitalInputProvider;
import com.pi4j.io.gpio.digital.DigitalOutputProvider;
import com.pi4j.io.i2c.I2CProvider;
import com.pi4j.io.pwm.PwmProvider;
import com.pi4j.io.spi.SpiProvider;
import com.pi4j.provider.Provider;
import com.pi4j.provider.ProviderGroup;
import com.pi4j.provider.Providers;
import com.pi4j.provider.exception.ProviderNotFoundException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * A minimal {@link Providers} implementation which assumes only a single provider for each {@link IOType}.
 */
public class MinimalProviders implements Providers, Iterable<Provider<?, ?, ?>> {

    /**
     * Index providers by {@link IOType}
     */
    private final Map<IOType, Provider<?, ?, ?>> ioTypeIndex = new HashMap<>();

    /**
     * Index providers by their class type
     */
    private final Map<Class<?>, Provider<?, ?, ?>> typeIndex = new HashMap<>();

    /**
     * Index providers by their id
     */
    private final Map<String, Provider<?, ?, ?>> idIndex = new HashMap<>();

    /**
     * With only a single plugin, there is no ambiguity about which provider to return.
     * This negates the need for the {@link ProviderGroup}.
     * <p>
     * Also, if the canonical method for constructing a device is via
     * {@link com.pi4j.context.Context#create(IOConfig, IOType)}, access to a provider instance is not required.
     */
    @Override
    public ProviderGroup<DigitalInputProvider> digitalInput() {
        throw new UnsupportedOperationException("DigitalInputProvider");
    }

    /**
     * @see #digitalInput()
     */
    @Override
    public ProviderGroup<DigitalOutputProvider> digitalOutput() {
        throw new UnsupportedOperationException("DigitalOutputProvider");
    }

    /**
     * @see #digitalInput()
     */
    @Override
    public ProviderGroup<PwmProvider> pwm() {
        throw new UnsupportedOperationException("PwmProvider");
    }

    /**
     * @see #digitalInput()
     */
    @Override
    public ProviderGroup<SpiProvider> spi() {
        throw new UnsupportedOperationException("SpiProvider");
    }

    /**
     * @see #digitalInput()
     */
    @Override
    public ProviderGroup<I2CProvider> i2c() {
        throw new UnsupportedOperationException("I2CProvider");
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Map<String, Provider> all() {
        return Map.copyOf(idIndex);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Provider> Map<String, T> all(Class<T> providerClass) throws ProviderNotFoundException {
        if (typeIndex.containsKey(providerClass)) {
            var provider = typeIndex.get(providerClass);
            return Map.of(provider.id(), (T) provider);
        }
        throw new ProviderNotFoundException(providerClass);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Provider> Map<String, T> all(IOType ioType) throws ProviderNotFoundException {
        if (ioTypeIndex.containsKey(ioType)) {
            var provider = ioTypeIndex.get(ioType);
            return Map.of(provider.id(), (T) provider);
        }
        throw new ProviderNotFoundException(ioType);
    }

    @Override
    public boolean exists(String providerId) {
        return idIndex.containsKey(providerId);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends Provider> T get(String providerId) throws ProviderNotFoundException {
        if (idIndex.containsKey(providerId)) {
            return (T) idIndex.get(providerId);
        }
        throw new ProviderNotFoundException(providerId);
    }

    @Override
    public Iterator<Provider<?, ?, ?>> iterator() {
        return idIndex.values().iterator();
    }

    public MinimalProviders add(Provider<?, ?, ?> provider) {
        var type = provider.type();
        var clazz = type.getProviderClass();
        ioTypeIndex.put(type, provider);
        typeIndex.put(clazz, provider);
        idIndex.put(provider.id(), provider);
        return this;
    }
}
