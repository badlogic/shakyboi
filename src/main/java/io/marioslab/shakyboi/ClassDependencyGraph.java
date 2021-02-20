package io.marioslab.shakyboi;

import io.marioslab.shakyboi.lookup.ClassLookup;

/**
 * A class dependency graph is created for a set of root classes. For each root class, the set
 * of classes it depends on is evaluated. The process is repeated recursively, until no more additional class
 * dependencies are found.
 * <p>
 * {@see ClassDependencyGraph#generate(ClassLookup, ClassLookup, String...)}
 */
public class ClassDependencyGraph {
    /**
     * Generates a new {@link ClassDependencyGraph}. For each provided root class, the set of classes
     * it directly depends on is evaluated. The process is then repeated for the newly identified classes
     * until no more new classes can be found.
     *
     * @param appClassLookup  the {@link ClassLookup} to search for application classes, like root classes.
     * @param bootClassLookup the {@link ClassLookup} to search for boot classes, e.g. java.lang.Object.
     * @param rootClasses     the names of root classes to start the search for all dependencies for. The names
     *                        must be given as {@link <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.2.1">internal names</a>}
     */
    public static ClassDependencyGraph generate(ClassLookup appClassLookup, ClassLookup bootClassLookup, String... rootClasses) {
        return new ClassDependencyGraph();
    }
}
