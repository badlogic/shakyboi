package io.marioslab.shakyboi.lookup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A {@link Lookup} that searches for files
 * in multiple other {@link Lookup} instances.
 */
public class CombinedLookup implements Lookup {
    private final List<Lookup> lookups;

    public CombinedLookup(Lookup... lookups) {
        if (lookups == null) throw new IllegalArgumentException("Lookups must not be null.");
        if (lookups.length == 0) throw new IllegalArgumentException("At least one lookup must be given.");
        this.lookups = Arrays.asList(lookups);
    }

    @Override
    public byte[] findClass(String name) {
        for (var cl : lookups) {
            byte[] bytes = cl.findClass(name);
            if (bytes != null) return bytes;
        }
        return null;
    }

    @Override
    public byte[] findResource(String name) {
        for (var cl : lookups) {
            byte[] bytes = cl.findResource(name);
            if (bytes != null) return bytes;
        }
        return null;
    }

    @Override
    public List<String> list() {
        var files = new ArrayList<String>();
        for (var cl : lookups) {
            files.addAll(cl.list());
        }
        return files;
    }
}
