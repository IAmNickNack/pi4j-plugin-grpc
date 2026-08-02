package io.github.iamnicknack.pi4j.grpc.server.context;

import com.pi4j.context.ContextConfig;
import com.pi4j.extension.Plugin;

public interface MinimalContextConfig extends ContextConfig {

    /**
     * User-specified {@link Plugin} providing providers.
     * @return the user-specified provider plugin
     */
    Plugin providerPlugin();

}
