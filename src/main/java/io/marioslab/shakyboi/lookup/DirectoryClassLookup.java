package io.marioslab.shakyboi.lookup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * A {@link ClassLookup} that searches for .class files
 * in a directory.
 */
public class DirectoryClassLookup implements ClassLookup {
    private final File baseDirectory;

    /**
     * @param directory the directory to search for .class files.
     */
    public DirectoryClassLookup(File directory) {
        if (directory == null) throw new IllegalArgumentException("Directory must not be null.");
        if (!directory.exists() || directory.isFile())
            throw new IllegalArgumentException("Class lookup directory " + directory.getAbsolutePath() + " does not exist.");
        this.baseDirectory = directory;
    }

    @Override
    public byte[] findClass(String name) {
        File classFile = new File(baseDirectory, name + ".class");
        if (classFile.exists() && classFile.isFile()) {
            try {
                return Files.readAllBytes(classFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException("Could not read bytes of class " + name);
            }
        } else {
            return null;
        }
    }
}
