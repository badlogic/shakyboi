package io.marioslab.shakyboi.util;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

/**
 * Writes entries to a .jar file.
 *
 * <code>
 * try(var writer = new JarFileWriter(new File(...))) {
 * writer.addFile("A.class", bytesOfA);
 * writer.addFile("B.class", bytesOfB);
 * }
 * </code>
 */
public class JarFileWriter implements Closeable {
    private final File jarFile;
    private final JarOutputStream out;

    public JarFileWriter(File jarFile) {
        if (jarFile == null) throw new IllegalArgumentException("Jar file must not be null.");
        this.jarFile = jarFile;
        try {
            out = new JarOutputStream(new FileOutputStream(jarFile));
        } catch (IOException e) {
            throw new RuntimeException("Couldn't create jar file " + jarFile.getAbsolutePath(), e);
        }
    }

    public void addFile(String name, byte[] data) {
        if (name == null) throw new IllegalArgumentException("Name must not be null.");
        if (data == null) throw new IllegalArgumentException("Data must not be null.");
        var entry = new ZipEntry(name);
        try {
            out.putNextEntry(entry);
            out.write(data);
            out.closeEntry();
        } catch (Throwable t) {
            throw new RuntimeException("Couldn't add file " + name + " to jar file " + jarFile, t);
        }
    }

    @Override
    public void close() throws IOException {
        out.close();
    }
}
