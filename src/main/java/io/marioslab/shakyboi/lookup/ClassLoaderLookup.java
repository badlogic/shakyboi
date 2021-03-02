package io.marioslab.shakyboi.lookup;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * A {@link Lookup} that searches for files
 * via the class loader used to load the {@link ClassLoaderLookup} instance. Does not
 * support listing files.
 */
public class ClassLoaderLookup implements Lookup {
    @Override
    public byte[] findClass(String name) {
        name = name + ".class";
        URL resource = ClassLoaderLookup.class.getClassLoader().getResource(name);
        if (resource == null) return null;
        try {
            return resource.openStream().readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] findResource(String name) {
        var in = ClassLoaderLookup.class.getClassLoader().getResourceAsStream(name);
        if (in == null) return null;
        try {
            return in.readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> list() {
        throw new RuntimeException("Not supported.");
    }
}
