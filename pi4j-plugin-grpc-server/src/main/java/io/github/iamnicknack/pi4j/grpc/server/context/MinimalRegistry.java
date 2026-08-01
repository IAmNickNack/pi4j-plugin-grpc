package io.github.iamnicknack.pi4j.grpc.server.context;

import com.pi4j.io.IO;
import com.pi4j.io.IOType;
import com.pi4j.io.exception.IOInvalidIDException;
import com.pi4j.io.exception.IONotFoundException;
import com.pi4j.registry.Registry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Registry implementation which is only responsible for maintaining references to IO instances and
 * does not get involved in lifecycle management.
 */
@SuppressWarnings("rawtypes")
public class MinimalRegistry implements Registry, Iterable<IO> {

    private final Logger logger = LoggerFactory.getLogger(MinimalRegistry.class);

    /**
     * Index devices by their unique identifier
     */
    private final HashMap<String, IO> indexById = new HashMap<>();

    /**
     * Index devices by their unique numeric identifier
     */
    private final HashMap<UniqueIdentifier, IO> indexByUniqueId = new HashMap<>();

    @Override
    public boolean exists(String id) {
        return indexById.containsKey(id);
    }

    @Override
    public boolean exists(IOType ioType, int identifier) {
        return indexByUniqueId.containsKey(new UniqueIdentifier(ioType, identifier));
    }

    @Override
    public Map<String, ? extends IO> all() {
        return Map.copyOf(indexById);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IO> T get(String id) throws IOInvalidIDException, IONotFoundException {
        if (indexById.containsKey(id)) {
            return (T) indexById.get(id);
        }
        throw new IONotFoundException(id);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IO> T get(String id, Class<T> type) throws IOInvalidIDException, IONotFoundException {
        if (indexById.containsKey(id)) {
            var io = indexById.get(id);
            if (type.isInstance(io)) {
                return (T) io;
            } else {
                throw new IOInvalidIDException();
            }
        }
        throw new IONotFoundException(id);
    }

    @Override
    public Iterator<IO> iterator() {
        return indexById.values().iterator();
    }

    public void register(IO io) {
        var uniqueIdentifier = new UniqueIdentifier(io.type(), io.config().getUniqueIdentifier());
        if (indexById.containsKey(io.id())) {
            throw new IllegalArgumentException("IO with ID " + io.id() + " already exists");
        }
        if (indexByUniqueId.containsKey(uniqueIdentifier)) {
            throw new IllegalArgumentException(io.type().name() + " with unique identifier " + uniqueIdentifier + " already exists");
        }

        logger.info("Registering {} with ID {} and unique identifier {}", io.getClass().getSimpleName(), io.id(), uniqueIdentifier);

        indexById.put(io.id(), io);
        indexByUniqueId.put(uniqueIdentifier, io);
    }

    public void remove(IO io) {
        if (indexById.containsKey(io.id())) {
            var uniqueIdentifier = new UniqueIdentifier(io.type(), io.config().getUniqueIdentifier());
            logger.info("Removing {} with ID {} and unique identifier {}", io.getClass().getSimpleName(), io.id(), uniqueIdentifier);
            indexById.remove(io.id());
            indexByUniqueId.remove(uniqueIdentifier);
        }
    }

    record UniqueIdentifier(IOType ioType, int identifier) {}
}
