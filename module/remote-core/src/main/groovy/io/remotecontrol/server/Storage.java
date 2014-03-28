package io.remotecontrol.server;

import groovy.lang.GroovyObjectSupport;
import groovy.lang.MissingPropertyException;

import java.util.Map;

/**
 * Used for abitrary name/value storage, but throws MPE on get of non existant property.
 */
public class Storage extends GroovyObjectSupport {

    private final Map<String, Object> storage;

    public Storage(Map<String, Object> storage) {
        this.storage = storage;
    }

    public Object propertyMissing(String name) {
        if (storage.containsKey(name)) {
            return storage.get(name);
        } else {
            throw new MissingPropertyException("No property named \'" + name + "\' is is available in the context");
        }
    }

    public void propertyMissing(String name, Object value) {
        storage.put(name, value);
    }

}
