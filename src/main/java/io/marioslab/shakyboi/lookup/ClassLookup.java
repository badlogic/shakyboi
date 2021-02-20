package io.marioslab.shakyboi.lookup;

/**
 * A class lookup provides the raw bytes of a class fetched from
 * some place.
 */
public interface ClassLookup {
    /**
     * Looks up the class with the given name and returns its
     * <code>.class</code> file as a byte array.
     *
     * @param name the binary class name, e.g. "java/lang/Object". @see <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.2.1">jvms-4.2.1</a>.
     * @return the classes bytes or null.
     * @throws RuntimeException in case an unrecoverable error happened.
     */
    byte[] findClass(String name);
}
