package io.marioslab.shakyboi.lookup;

import java.util.List;

/**
 * A lookup provides the raw bytes of classes and resources fetched from
 * some place.
 */
public interface Lookup {
    /**
     * Looks up the class with the given name and returns its
     * <code>.class</code> file content as a byte array.
     *
     * @param name the binary class name, e.g. "java/lang/Object". @see <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.2.1">jvms-4.2.1</a>.
     * @return the classes bytes or null.
     * @throws RuntimeException in case an unrecoverable error happened.
     */
    byte[] findClass(String name);

    /**
     * Looks up the resource with the given name and returns its
     * content as a byte array.
     *
     * @param name the name of the resource, e.g. "java/lang/Object.class", or "images/bunny.png".
     * @return the contents or null.
     * @throws RuntimeException in case an unrecoverable error happened.
     */
    byte[] findResource(String name);

    /**
     * Lists all files contained in this lookup, both class and resource files.
     *
     * @return a list of all files in this lookup.
     */
    List<String> list();
}
