package io.marioslab.shakyboi.tests;

import io.marioslab.shakyboi.lookup.*;
import io.marioslab.shakyboi.util.JarFileWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;


public class ClassLookupTest {
    static File classFilesDir;
    static File jarFile;

    @BeforeAll
    public static void beforeAll() throws IOException {
        jarFile = Files.createTempFile("test", "jar").toFile();
        classFilesDir = Files.createTempDirectory("temp-classes").toFile();
        try (var writer = new JarFileWriter(jarFile)) {
            var classLookup = new ClassLoaderClassLookup();
            String[] classes = {
                    "io/marioslab/shakyboi/tests/apps/simple/App",
                    "io/marioslab/shakyboi/tests/apps/simple/Foo",
                    "io/marioslab/shakyboi/tests/apps/simple/Bar",
                    "io/marioslab/shakyboi/tests/apps/simple/Zip",
                    "io/marioslab/shakyboi/tests/apps/simple/Zap",
                    "io/marioslab/shakyboi/tests/apps/simple/Zop"
            };
            for (var clazz : classes) {
                var bytes = classLookup.findClass(clazz);
                writer.addFile(clazz + ".class", bytes);
                File classFile = new File(classFilesDir, clazz + ".class");
                classFile.getParentFile().mkdirs();
                Files.write(classFile.toPath(), bytes);
            }
        }
    }

    @AfterAll
    public static void afterAll() throws IOException {
        jarFile.delete();
    }

    @Test
    public void testClassLoaderClassLookup() {
        var classLookup = new ClassLoaderClassLookup();
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));
    }

    @Test
    public void testDirectoryClassLookup() {
        var classLookup = new DirectoryClassLookup(classFilesDir);
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));
    }

    @Test
    public void testJarClassLookup() throws IOException {
        File jarFile = Files.createTempFile("test", "jar").toFile();
        try (var writer = new JarFileWriter(jarFile)) {
            var classLookup = new ClassLoaderClassLookup();
            String[] classes = {
                    "io/marioslab/shakyboi/tests/apps/simple/App",
                    "io/marioslab/shakyboi/tests/apps/simple/Foo",
                    "io/marioslab/shakyboi/tests/apps/simple/Bar",
                    "io/marioslab/shakyboi/tests/apps/simple/Zip",
                    "io/marioslab/shakyboi/tests/apps/simple/Zap",
                    "io/marioslab/shakyboi/tests/apps/simple/Zop"
            };
            for (var clazz : classes) writer.addFile(clazz + ".class", classLookup.findClass(clazz));
        }

        var classLookup = new JarClassLookup(jarFile);
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));

        jarFile.delete();
    }

    @Test
    public void testJrtImageClassLookup() {
        var classLookup = new JrtImageClassLookup();
        assertNotNull(classLookup.findClass("java/lang/Object"));
        assertNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
    }

    @Test
    public void testCombinedClassLookup() {
        var classLookup = new CombinedClassLookup(new JarClassLookup(jarFile), new JrtImageClassLookup());
        assertNotNull(classLookup.findClass("java/lang/Object"));
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist.class"));
    }
}
