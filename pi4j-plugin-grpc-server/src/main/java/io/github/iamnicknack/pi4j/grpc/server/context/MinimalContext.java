package io.github.iamnicknack.pi4j.grpc.server.context;

import com.pi4j.boardinfo.model.BoardInfo;
import com.pi4j.boardinfo.util.BoardInfoHelper;
import com.pi4j.context.Context;
import com.pi4j.context.ContextConfig;
import com.pi4j.context.impl.DefaultContext;
import com.pi4j.event.EventManager;
import com.pi4j.event.InitializedListener;
import com.pi4j.event.ShutdownEvent;
import com.pi4j.event.ShutdownListener;
import com.pi4j.exception.ShutdownException;
import com.pi4j.extension.Plugin;
import com.pi4j.extension.PluginService;
import com.pi4j.io.IO;
import com.pi4j.io.IOConfig;
import com.pi4j.io.IOType;
import com.pi4j.io.exception.IOAlreadyExistsException;
import com.pi4j.io.exception.IOInvalidIDException;
import com.pi4j.io.exception.IONotFoundException;
import com.pi4j.io.exception.IOShutdownException;
import com.pi4j.provider.Provider;
import com.pi4j.provider.ProviderGroup;
import com.pi4j.provider.Providers;
import com.pi4j.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ServiceLoader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * An opinionated, minimal implementation of the {@link Context} interface
 * <p>
 * Key differences to {@link DefaultContext}:
 * <ul>
 *     <li>Simplifies lifecycle management by taking responsibility of device creation and registration</li>
 *     <li>Allows the user to explicitly specify a chosen providers plugin via {@link MinimalContextConfig#providerPlugin()}</li>
 *     <li>Does not support multiple provider plugins, negating the need to either assess priority or manage
 *     ambiguities via {@link ProviderGroup}s</li>
 *     <li>Does not support initialisation events</li>
 * </ul>
 * @see MinimalContextConfig
 * @see MinimalProviders
 * @see MinimalRegistry
 * @see SimplePluginService
 * @see #removeAllInitializedListeners()
 */
public class MinimalContext implements Context {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ContextConfig config;
    private final MinimalProviders providers = new MinimalProviders();
    private final MinimalRegistry registry = new MinimalRegistry();

    private final EventManager<Context, ShutdownListener, ShutdownEvent> shutdownEventManager =
        new EventManager<>(this, ShutdownListener::onShutdown);

    private final ExecutorService runtimeExecutor = Executors.newSingleThreadScheduledExecutor(runnable ->
        new Thread(runnable, "Pi4J.RUNTIME")
    );

    private final BoardInfo boardInfo = BoardInfoHelper.current();

    private boolean isShutdown = false;

    public MinimalContext(MinimalContextConfig config) {
        this.config = config;

        logger.info("Detected board model: {}", boardInfo.getBoardModel().getLabel());
        logger.info("Running on: {}", boardInfo.getOperatingSystem());
        logger.info("With Java version: {}", boardInfo.getJavaInfo());

        logger.info("Initializing Pi4J context/runtime...");
        if (!config.providers().isEmpty()) {
            logger.debug("Adding provided providers");
            config.providers().forEach(providers::add);
        } else if (config.providerPlugin() != null) {
            logger.debug("Initialising provider plugin: {}", config.providerPlugin().getClass().getName());
            config.providerPlugin().initialize(new SimplePluginService());
        } else if (config.autoDetectProviders()) {
            var loader = ServiceLoader.load(Plugin.class);
            loader.findFirst().ifPresent(plugin -> {
                logger.debug("Detected plugin: [{}]", plugin.getClass().getName());
                plugin.initialize(new SimplePluginService());
            });
        }

        providers.forEach(provider -> {
            logger.info("Initializing provider: {}", provider.getClass().getName());
            provider.initialize(this);
        });

        if (config.enableShutdownHook()) {
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                runCatching(this::shutdown);
            }));
        }

        logger.info("Pi4J context/runtime successfully initialized.");
    }

    @Override
    public Context shutdown() throws ShutdownException {
        if (!this.isShutdown) {
            shutdownEventManager.dispatch(new ShutdownEvent(this), ShutdownListener::beforeShutdown);

            for (var p : providers) {
                logger.info("Shutting down provider: {}", p.id());
                runCatching(() -> p.shutdownInternal(this));
            }

            for (var io : registry) {
                logger.info("Shutting down IO: {}", io.id());
                runCatching(() -> io.shutdownInternal(this));
            }

            runtimeExecutor.shutdownNow();

            this.isShutdown = true;

            shutdownEventManager.dispatch(new ShutdownEvent(this));
        }
        return this;
    }

    /**
     * If the canonical device creation path is via the context, then it is the context's responsibility to
     * add the instance to the registry. Alternative paths to register a device should not be possible.
     * <p>
     * This method is currently called by providers after creating a device. However, as the owner of the registry
     * reference, it should be the context's responsibility to manage it. The provider's only responsibility should
     * be to construct the device instance.
     *
     * @param instance the I/O instance to register
     */
    @Override
    @Deprecated
    public void register(IO instance) {
        logger.warn("Ignoring attempt to register {} with ID: {} ({}, {})",
                instance.getClass().getSimpleName(),
                instance.id(),
                instance.type().name(),
                instance.config().getUniqueIdentifier()
        );
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <I extends IO> I create(IOConfig config, IOType ioType) {
        if (registry.exists(config.id())) {
            throw new IOAlreadyExistsException(config.id());
        }

        var provider = providers.get(ioType);
        var instance = provider.create(config);

        // Initialising here keeps initialisation as a responsibility of the context rather than rely on a secondary
        // component to be implemented appropriately to pass a reference to this on behalf of the context.
        // This could be unnecessary if the context could be passed to the instance during construction and
        // allow explicit initialisation.
        instance.initialize(this);

        registry.register(instance);

        return (I) instance;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public <T extends IO> T shutdown(String id) throws IOInvalidIDException, IONotFoundException, IOShutdownException {
        var io = registry.get(id);
        shutdown(io);
        return (T) io;
    }

    @SuppressWarnings("rawtypes")
    @Override
    public <T extends IO> void shutdown(T instance) throws IOInvalidIDException, IONotFoundException, IOShutdownException {
        instance.shutdownInternal(this);
        registry.remove(instance);
    }

    @Override
    public ContextConfig config() {
        return config;
    }

    @Override
    public Providers providers() {
        return providers;
    }

    @Override
    public Registry registry() {
        return registry;
    }

    @Override
    public Future<?> submitTask(Runnable task) {
        return runtimeExecutor.submit(task);
    }

    @Override
    public Future<Context> asyncShutdown() {
        return runtimeExecutor.submit(() -> {
            shutdown();
            return this;
        });
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public BoardInfo boardInfo() {
        return boardInfo;
    }

    /**
     * Marking all initialisation listener events as unsupported. If initialisation is done during construction,
     * (as in {@link DefaultContext}) the user has no opportunity to add a listener, and the collection will
     * always be empty.
     */
    @Override
    @Deprecated(forRemoval = true)
    public Context removeAllInitializedListeners() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see removeAllInitializedListeners
     */
    @Override
    @Deprecated(forRemoval = true)
    public Context addListener(InitializedListener... listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * @see removeAllInitializedListeners
     */
    @Override
    @Deprecated(forRemoval = true)
    public Context removeListener(InitializedListener... listener) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Context removeAllShutdownListeners() {
        return shutdownEventManager.clear();
    }

    @Override
    public Context addListener(ShutdownListener... listener) {
        return shutdownEventManager.add(listener);
    }

    @Override
    public Context removeListener(ShutdownListener... listener) {
        return shutdownEventManager.remove(listener);
    }

    /**
     * Utility function to catch and log non-fatal exceptions
     */
    private void runCatching(Runnable runnable) {
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Plugin service required for plugin detection.
     * <p>
     * This is a simplified implementation which registers providers directly with {@link MinimalContext#providers}
     * and doesn't use a {@link com.pi4j.extension.impl.PluginStore}
     */
    private class SimplePluginService implements PluginService {
        @Override
        public Context context() {
            return MinimalContext.this;
        }

        @Override
        public PluginService register(Provider... provider) {
            for (var p : provider) {
                MinimalContext.this.providers.add(p);
            }
            return this;
        }
    }
}
