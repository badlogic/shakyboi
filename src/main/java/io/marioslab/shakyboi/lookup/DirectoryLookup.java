package io.marioslab.shakyboi.lookup;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Lookup} that searches for files
 * in a directory.
 */
public class DirectoryLookup implements Lookup {
    private final File baseDirectory;

    /**
     * @param directory the directory to search for .class files.
     */
    public DirectoryLookup(File directory) {
        if (directory == null) throw new IllegalArgumentException("Directory must not be null.");
        if (!directory.exists() || directory.isFile())
            throw new IllegalArgumentException("Lookup directory " + directory.getAbsolutePath() + " does not exist.");
        this.baseDirectory = directory;
    }

    @Override
    public byte[] findClass(String name) {
        File classFile = new File(baseDirectory, name + ".class");
        if (classFile.exists() && classFile.isFile()) {
            try {
                return Files.readAllBytes(classFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public byte[] findResource(String name) {
        File classFile = new File(baseDirectory, name);
        if (classFile.exists() && classFile.isFile()) {
            try {
                return Files.readAllBytes(classFile.toPath());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return null;
        }
    }

    @Override
    public List<String> list() {
        var files = new ArrayList<String>();
        var baseDirPath = baseDirectory.toPath();
        try {
            Files.walk(baseDirPath)
                    .filter(Files::isRegularFile)
                    .forEach(f -> {
                        files.add(baseDirPath.relativize(f).toString());
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return files;
    }
}
