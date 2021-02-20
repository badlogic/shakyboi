package io.marioslab.shakyboi.lookup;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * A {@link ClassLookup} that searches .class files in the JRT image
 * of the JVM it is executed in. See <a href="https://openjdk.java.net/jeps/220">JEP 220</a>.
 */
public class JrtImageClassLookup implements ClassLookup {
    private final Path modules = FileSystems.getFileSystem(URI.create("jrt:/")).getPath("/modules");
    private final FileSystem modulesFS = modules.getFileSystem();

    @Override
    public byte[] findClass(String name) {
        var fileName = name + ".class";
        try {
            Optional<Path> module = Files.list(modules).filter(m -> {
                return Files.exists(m.resolve(fileName));
            }).findFirst();
            if (module.isPresent()) return Files.readAllBytes(module.get().resolve(fileName));
            return null;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't search class " + name + " in Java runtime image.", e);
        }
    }
}
