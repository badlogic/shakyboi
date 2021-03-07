package io.marioslab.shakyboi.graph;

import io.marioslab.shakyboi.classfile.ClassFile;
import io.marioslab.shakyboi.lookup.Lookup;

import java.util.*;

/**
 * A class dependency graph is created for a set of root classes. For each root class, the set
 * of classes it depends on is evaluated. The process is repeated recursively, until no more additional class
 * dependencies are found.
 * <p>
 *
 * @see {@link ClassDependencyGraphGenerator#generate(Lookup, Lookup, String...)}
 */
public class ClassDependencyGraph {
    /** The root classes as given to {@link ClassDependencyGraphGenerator#generate(Lookup, Lookup, String...)} **/
    public final List<ClassNode> rootClasses;
    /** All classes reachable by the class dependency graph **/
    public final Map<String, ClassNode> reachableClasses;

    /**
     * Constructs a new dependency graph from the given root classes and reachable classes.
     *
     * @param rootClasses      the root classes
     * @param reachableClasses the reachable classes, including the root classes
     */
    public ClassDependencyGraph(List<ClassNode> rootClasses, Map<String, ClassNode> reachableClasses) {
        this.rootClasses = rootClasses;
        this.reachableClasses = reachableClasses;
    }

    /**
     * A node in the class dependency graph, storing incoming and outgoing dependencies for
     * simple graph traversal. The lists storing the dependencies are mutable.
     */
    public static class ClassNode {
        /** The {@link ClassFile} this node represents */
        public final ClassFile classFile;
        /** Whether this class comes from the app class lookup or the bootstrap class lookup */
        public final boolean isAppClass;
        /** The list of classes this class depends on. Filled by  {@link ClassDependencyGraphGenerator#generate(Lookup, Lookup, List, String...)} */
        public final Set<String> referencedBy = new HashSet<>(16);
        /** The list of classes this class depends on. Filled by  {@link ClassDependencyGraphGenerator#generate(Lookup, Lookup, List, String...)} */
        public final List<ClassNode> dependsOn = new ArrayList<>(16);
        /** Whether this class is a root class */
        public boolean isRootClass;
        /** Whether this class has been processed by {@link ClassDependencyGraphGenerator#generate(Lookup, Lookup, List, String...)} **/
        public boolean isProcessed;

        public ClassNode(ClassFile classFile, boolean isAppClass) {
            this.classFile = classFile;
            this.isAppClass = isAppClass;
        }
    }
}
