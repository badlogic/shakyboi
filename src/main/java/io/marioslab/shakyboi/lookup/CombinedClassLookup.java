package io.marioslab.shakyboi.lookup;

import java.util.Arrays;
import java.util.List;

/**
 * A {@link ClassLookup} that searches for .class files
 * in multiple other {@link ClassLookup} instances.
 */
public class CombinedClassLookup implements ClassLookup {
    private final List<ClassLookup> classLookups;

    public CombinedClassLookup(ClassLookup... classLookups) {
        if (classLookups == null) throw new IllegalArgumentException("Class lookups must not be null.");
        if (classLookups.length == 0) throw new IllegalArgumentException("At least one class lookup must be given.");
        this.classLookups = Arrays.asList(classLookups);
    }

    @Override
    public byte[] findClass(String name) {
        for (var cl : classLookups) {
            byte[] bytes = cl.findClass(name);
            if (bytes != null) return bytes;
        }
        return null;
    }
}
