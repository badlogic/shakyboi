package io.marioslab.shakyboi.lookup;

import java.io.IOException;
import java.net.URL;

/**
 * A {@link ClassLookup} that searches for .class files
 * via the class loader used to load the {@link InMemoryClassLookup} instance.
 */
public class InMemoryClassLookup implements ClassLookup {
    @Override
    public byte[] findClass(String name) {
        name = name + ".class";
        URL resource = InMemoryClassLookup.class.getClassLoader().getResource(name);
        if (resource == null) return null;
        try {
            return resource.openStream().readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
