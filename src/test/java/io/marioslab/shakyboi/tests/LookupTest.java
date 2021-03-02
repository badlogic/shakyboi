package io.marioslab.shakyboi.tests;

import io.marioslab.shakyboi.lookup.*;
import io.marioslab.shakyboi.util.JarFileWriter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;


public class LookupTest {
    static File classFilesDir;
    static Set<String> classFilesInDir;
    static File jarFile;

    @BeforeAll
    public static void beforeAll() throws IOException {
        jarFile = Files.createTempFile("test", "jar").toFile();
        classFilesDir = Files.createTempDirectory("temp-classes").toFile();
        classFilesInDir = new HashSet<String>();
        try (var writer = new JarFileWriter(jarFile)) {
            var classLookup = new ClassLoaderLookup();
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
                classFilesInDir.add(clazz + ".class");
            }
        }
    }

    @AfterAll
    public static void afterAll() throws IOException {
        jarFile.delete();
    }

    @Test
    public void testClassLoaderClassLookup() {
        var classLookup = new ClassLoaderLookup();
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));
    }

    @Test
    public void testDirectoryClassLookup() {
        var classLookup = new DirectoryLookup(classFilesDir);
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));
        assertNotNull(classLookup.findResource("io/marioslab/shakyboi/tests/apps/simple/App.class"));
        assertNull(classLookup.findResource("does/not/Exist"));

        var files = new HashSet<String>(classLookup.list());
        assertEquals(classFilesInDir, files);
    }

    @Test
    public void testJarClassLookup() throws IOException {
        File jarFile = Files.createTempFile("test", "jar").toFile();
        try (var writer = new JarFileWriter(jarFile)) {
            var classLookup = new ClassLoaderLookup();
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

        var classLookup = new JarLookup(jarFile);
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist"));
        assertNotNull(classLookup.findResource("io/marioslab/shakyboi/tests/apps/simple/App.class"));
        assertNull(classLookup.findResource("does/not/Exist.txt"));
        var files = new HashSet<String>(classLookup.list());
        assertEquals(classFilesInDir, files);

        jarFile.delete();
    }

    @Test
    public void testJrtImageClassLookup() {
        var classLookup = new JrtImageLookup();
        assertNotNull(classLookup.findClass("java/lang/Object"));
        assertNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNotNull(classLookup.findResource("java/lang/Object.class"));
        assertNull(classLookup.findResource("io/marioslab/shakyboi/tests/apps/simple/App.class"));
        var files = classLookup.list();
        assertTrue(files.size() > 20000);
    }

    @Test
    public void testCombinedClassLookup() {
        var classLookup = new CombinedLookup(new JarLookup(jarFile), new JrtImageLookup());
        assertNotNull(classLookup.findClass("java/lang/Object"));
        assertNotNull(classLookup.findClass("io/marioslab/shakyboi/tests/apps/simple/App"));
        assertNull(classLookup.findClass("does/not/Exist.class"));
        assertNotNull(classLookup.findResource("java/lang/Object.class"));
        assertNotNull(classLookup.findResource("io/marioslab/shakyboi/tests/apps/simple/App.class"));
        assertNull(classLookup.findResource("does/not/Exist.class"));

        var files = new HashSet<String>(classLookup.list());
        for (var clazz : classFilesInDir) {
            assertTrue(files.contains(clazz));
        }
    }
}
