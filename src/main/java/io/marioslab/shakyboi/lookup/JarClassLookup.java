package io.marioslab.shakyboi.lookup;

import java.io.File;
import java.io.IOException;
import java.util.jar.JarFile;
import java.util.zip.ZipFile;

/**
 * A {@link ClassLookup} searching for .class files in a .jar file.
 */
public class JarClassLookup implements ClassLookup {
    private final JarFile jarFile;

    public JarClassLookup(File jarFile) {
        if (jarFile == null) throw new IllegalArgumentException("Jar file must not be null.");
        if (!jarFile.exists() || jarFile.isDirectory())
            throw new IllegalArgumentException("Jar file " + jarFile.getAbsolutePath() + " does not exist.");
        try {
            this.jarFile = new JarFile(jarFile, false, ZipFile.OPEN_READ);
        } catch (IOException e) {
            throw new RuntimeException("Couldn't load jar file " + jarFile.getAbsolutePath(), e);
        }
    }

    @Override
    public byte[] findClass(String name) {
        var entry = jarFile.getEntry(name + ".class");
        if (entry == null) return null;
        try {
            return jarFile.getInputStream(entry).readAllBytes();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't read jar file entry " + name, e);
        }
    }
}
