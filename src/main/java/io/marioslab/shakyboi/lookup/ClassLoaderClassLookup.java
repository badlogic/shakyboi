package io.marioslab.shakyboi.lookup;

import java.io.IOException;
import java.net.URL;

/**
 * A {@link ClassLookup} that searches for .class files
 * via the class loader used to load the {@link ClassLoaderClassLookup} instance.
 */
public class ClassLoaderClassLookup implements ClassLookup {
    @Override
    public byte[] findClass(String name) {
        name = name + ".class";
        URL resource = ClassLoaderClassLookup.class.getClassLoader().getResource(name);
        if (resource == null) return null;
        try {
            return resource.openStream().readAllBytes();
        } catch (IOException e) {
            return null;
        }
    }
}
