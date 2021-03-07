package io.marioslab.shakyboi.graph;

import io.marioslab.shakyboi.classfile.ClassFile;
import io.marioslab.shakyboi.classfile.ClassFileReader;
import io.marioslab.shakyboi.lookup.Lookup;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Generates a {@link ClassDependencyGraph} graph based on a root class set. For each root class, the classes it depends on
 * are collected. This process is repeated recursively for the collected classes until no further classes are found.
 *
 * @see <a href="https://marioslab.io/posts/shakyboi/shakyboi-part-1/">the blog series</a> describing the details.
 */
public class ClassDependencyGraphGenerator {
    /**
     * Generates a new {@link ClassDependencyGraph}. For each provided root class, the set of classes
     * it directly depends on is evaluated. The process is then repeated for the newly identified classes
     * until no more new classes can be found.
     *
     * @param appLookup       the {@link Lookup} to search for application classes, like root classes.
     * @param bootstrapLookup the {@link Lookup} to search for boot classes, e.g. java.lang.Object.
     * @param rootClassNames  the names of root classes to start the search for all dependencies for. The names
     *                        must be given as {@link <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.2.1">internal names</a>}
     * @return the {@link ClassDependencyGraph}
     * @throws IOException in case a class could not be looked up or parsed.
     */
    public static ClassDependencyGraph generate(Lookup appLookup, Lookup bootstrapLookup, List<String> warnings, String... rootClassNames) throws IOException {
        var rootClasses = new ArrayList<ClassDependencyGraph.ClassNode>(); // the root classes nodes
        var reachableClasses = new HashMap<String, ClassDependencyGraph.ClassNode>(); // all reachable classes, processed and unprocessed
        var classesToProcess = new ArrayList<ClassDependencyGraph.ClassNode>(); // classes that still need to be processed

        // Lookup all root classes and add them to to the list of classes to be processed.
        for (String className : rootClassNames) {
            var classNode = lookupClassNode(className, reachableClasses, bootstrapLookup, appLookup);
            if (classNode == null)
                throw new IOException("Couldn't find root class " + className.replace('/', '.') + " in either app or bootstrap lookup.");
            classNode.isRootClass = true;
            rootClasses.add(classNode);
            classesToProcess.add(classNode);
        }

        // Process classes until there no more classes to process.
        while (classesToProcess.size() > 0) {
            var classNode = classesToProcess.remove(classesToProcess.size() - 1);

            // If this class has already been processed, continue with the next class.
            if (classNode.isProcessed)
                continue;

            // Mark the class as processed.
            classNode.isProcessed = true;

            // Don't collect dependencies of bootstrap classes
            if (!classNode.isAppClass)
                continue;

            // Collect the classes referenced by this class and add them to the list
            // of classes to be processed if they haven't been processed yet. Also
            // add the classes to this class' set of classes it depends on.
            Set<String> collectedClassNames = collectClassNames(classNode);
            for (String className : collectedClassNames) {
                var otherClassNode = lookupClassNode(className, reachableClasses, bootstrapLookup, appLookup);
                if (otherClassNode == null) {
                    warnings.add("Class " + classNode.classFile.getName().replace('/', '.') + " depends on " + className.replace('/', '.') + ", but " + className.replace('/', '.') + " could not be found in app or bootstrap lookup.");
                    continue;
                }
                // Don't depend on this class itself
                if (otherClassNode.classFile.getName().equals(classNode.classFile.getName()))
                    continue;
                if (!otherClassNode.isProcessed) {
                    classesToProcess.add(otherClassNode);
                }
                classNode.dependsOn.add(otherClassNode);
                otherClassNode.referencedBy.add(classNode.classFile.getName());
            }
        }
        return new ClassDependencyGraph(rootClasses, reachableClasses);
    }

    /**
     * Looks up a class file for the given name. The lookup order is as follows:
     * <ol>
     *    <li><code>knownClasses</code></li>
     *    <li><code>bootstrapClassLookup</code></li>
     *    <li><code>appClassLookup</code></li>
     * </ol>
     *
     * @param className       the name of the class to lookup
     * @param knownClasses    the classes that have been loaded so far
     * @param bootstrapLookup the {@link Lookup} to lookup bootstrap classes in
     * @param appLookup       the {@link Lookup} to lookup app classes in
     * @return the found {@link ClassDependencyGraph.ClassNode} or null
     * @throws IOException in case the class could not read
     */
    private static ClassDependencyGraph.ClassNode lookupClassNode(String className, Map<String, ClassDependencyGraph.ClassNode> knownClasses, Lookup bootstrapLookup, Lookup appLookup) throws IOException {
        if (knownClasses.containsKey(className)) return knownClasses.get(className);
        var bytes = bootstrapLookup.findClass(className);
        var isAppClass = bytes == null;
        if (bytes == null) bytes = appLookup.findClass(className);
        if (bytes == null) return null;
        var classFile = ClassFileReader.readClassFile(className, bytes);
        var classNode = new ClassDependencyGraph.ClassNode(classFile, isAppClass);
        knownClasses.put(className, classNode);
        return classNode;
    }

    /**
     * Collect all class names referenced in the given class. See <a href="https://marioslab.io/posts/shakyboi/shakyboi-part-2/">this blog post</a>
     * for details.
     *
     * @param classNode the class to collect the class names from
     * @return the set of class names found in the class
     */
    private static Set<String> collectClassNames(ClassDependencyGraph.ClassNode classNode) {
        var className = classNode.classFile.getName();
        var collectedClassNames = new HashSet<String>();

        // Collect class names from the constant pool
        var constantPool = classNode.classFile.constantPool;
        for (int i = 0; i < constantPool.size(); i++) {
            var entry = constantPool.get(i);
            if (entry == null)
                continue; // There might be null entries in the constant pool, i.e. for long and double entries.

            if (entry instanceof ClassFile.ClassInfoEntry) {
                var otherClassName = ((ClassFile.ClassInfoEntry) entry).getName();
                // A class info entry can also be an array descriptor. In this case, we
                // fetch the array element type.
                if (otherClassName.charAt(0) == '[') {
                    otherClassName = getClassFromFieldDescriptor(otherClassName);
                    if (otherClassName == null)
                        continue;
                }
                collectedClassNames.add(otherClassName);
            } else if (entry instanceof ClassFile.NameAndTypeEntry) {
                var nameAndTypeEntry = (ClassFile.NameAndTypeEntry) entry;
                String descriptor = nameAndTypeEntry.getDescriptor();
                if (descriptor.charAt(0) == '(') {
                    collectedClassNames.addAll(getClassesFromMethodDescriptor(descriptor));
                } else {
                    var otherClassName = getClassFromFieldDescriptor(descriptor);
                    if (otherClassName != null)
                        collectedClassNames.add(otherClassName);
                }
            } else if (entry instanceof ClassFile.MethodTypeEntry) {
                var methodTypeEntry = (ClassFile.MethodTypeEntry) entry;
                var descriptor = methodTypeEntry.getDescriptor();
                collectedClassNames.addAll(getClassesFromMethodDescriptor(descriptor));
            }
        }

        // Collect class names from fields
        var fields = classNode.classFile.fields;
        for (var field : fields) {
            var otherClassName = getClassFromFieldDescriptor(field.getDescriptor());
            if (otherClassName != null)
                collectedClassNames.add(otherClassName);
        }

        // Collect class names from methods
        var methods = classNode.classFile.methods;
        for (var method : methods) {
            collectedClassNames.addAll(getClassesFromMethodDescriptor(method.getDescriptor()));
        }

        return collectedClassNames;
    }

    /**
     * Returns the class name found in the <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.3.2">field descriptor</a>, or <code>null</code> if no
     * class name was found in the descriptor. For array descriptors, the element type is returned if it is a class name.
     *
     * @param descriptor the field descriptor
     * @return the class name found in the descriptor or null
     */
    public static String getClassFromFieldDescriptor(String descriptor) {
        // Skip array dimensions.
        int startIndex = 0;
        while (descriptor.charAt(startIndex) == '[') startIndex++;
        if (descriptor.charAt(startIndex) != 'L') return null;
        startIndex++;
        int endIndex = startIndex;
        for (; descriptor.charAt(endIndex) != ';'; endIndex++) ;
        return descriptor.substring(startIndex, endIndex);
    }

    /**
     * Returns all class names found in the <a href="https://docs.oracle.com/javase/specs/jvms/se15/html/jvms-4.html#jvms-4.3.3">method descriptor</a>. For array
     * descriptors, the element type is returned if it is a class name.
     *
     * @param descriptor the method descriptor
     * @return the list of class names found in the descriptor
     */
    public static List<String> getClassesFromMethodDescriptor(String descriptor) {
        var classes = new ArrayList<String>();
        int index = 1;
        while (descriptor.charAt(index) != ')') {
            while (descriptor.charAt(index) == '[') index++;
            if (descriptor.charAt(index) == 'L') {
                int startindex = index + 1;
                for (; descriptor.charAt(index) != ';'; index++) ;
                classes.add(descriptor.substring(startindex, index++));
            } else {
                index++;
            }
        }
        index++;
        var returnType = getClassFromFieldDescriptor(descriptor.substring(index));
        if (returnType != null) classes.add(returnType);
        return classes;
    }

    /**
     * Generates the contents of a <a href="https://en.wikipedia.org/wiki/DOT_(graph_description_language)">DOT file</a>
     * from the given {@link ClassDependencyGraph}.
     *
     * @param graph the graph
     * @return the DOT file contents
     */
    public static String generateDotFile(ClassDependencyGraph graph, boolean onlyAppClasses) {
        var bytes = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            out.println("digraph classDependencies {");
            out.println("node [shape=box, fontsize=16]");
            for (var classNodeEntry : graph.reachableClasses.entrySet()) {
                var className = classNodeEntry.getKey().replace('/', '.');
                var classNode = classNodeEntry.getValue();
                if (onlyAppClasses && !classNode.isAppClass) continue;

                if (classNode.isRootClass) out.println("\"" + className + "\" [color=#ff0000];");
                if (!classNode.isAppClass) out.println("\"" + className + "\" [color=#00ff00];");

                for (var otherClass : classNode.dependsOn) {
                    if (onlyAppClasses && !otherClass.isAppClass) continue;
                    out.println("\"" + className + "\" -> \"" + otherClass.classFile.getName() + "\";");
                }
            }
            out.println("}");
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }

    /**
     * Generates the contents of a JSON file from the given {@link ClassDependencyGraph}. An example.
     *
     * <code>
     * [
     * { "name": "some.class.Name", "isAppClass": false, "isRootClass": true, "dependsOn": [ "other.class.Name.", "and.another.One" ], "referencedBy": [ "other.class.Name.", "and.another.One" ]},
     * { "name": "some.class.Name", "isAppClass": false, "isRootClass": true, "dependsOn": [ "other.class.Name.", "and.another.One" ], "referencedBy": [ "other.class.Name.", "and.another.One" ]},
     * { "name": "some.class.Name", "isAppClass": false, "isRootClass": true, "dependsOn": [ "other.class.Name.", "and.another.One" ], "referencedBy": [ "other.class.Name.", "and.another.One" ]},
     * { "name": "some.class.Name", "isAppClass": false, "isRootClass": true, "dependsOn": [ "other.class.Name.", "and.another.One" ], "referencedBy": [ "other.class.Name.", "and.another.One" ]},
     * ...
     * ]
     * </code>
     *
     * @param graph the graph
     * @return the JSON file contents
     */
    public static String generateJSON(ClassDependencyGraph graph, boolean onlyAppClasses) {
        var bytes = new ByteArrayOutputStream();
        try (PrintStream out = new PrintStream(bytes, true, StandardCharsets.UTF_8)) {
            out.println("[");
            var classNodeEntries = graph.reachableClasses.entrySet().stream()
                    .filter(e -> onlyAppClasses ? e.getValue().isAppClass : true)
                    .sorted((a, b) -> a.getKey().compareTo(b.getKey()))
                    .iterator();
            while (classNodeEntries.hasNext()) {
                var classNodeEntry = classNodeEntries.next();
                var className = classNodeEntry.getKey();
                var classNode = classNodeEntry.getValue();
                out.print("{ \"name\": \"" + className.replace('/', '.') + "\", \"isAppClass\": " + classNode.isAppClass + ", \"isRootClass\": " + classNode.isRootClass + ", \"dependsOn\": [");

                var dependsOn = classNode.dependsOn.stream().filter(c -> onlyAppClasses ? c.isAppClass : true).sorted((a, b) -> a.classFile.getName().compareTo(b.classFile.getName())).iterator();
                while (dependsOn.hasNext()) {
                    var otherClass = dependsOn.next();
                    out.print("\"" + otherClass.classFile.getName().replace('/', '.') + (dependsOn.hasNext() ? "\", " : "\""));
                }
                out.print("], \"referencedBy\": [");
                var referencedBy = classNode.referencedBy.stream().sorted().iterator();
                while (referencedBy.hasNext()) {
                    var otherClass = referencedBy.next();
                    out.print("\"" + otherClass.replace('/', '.') + (referencedBy.hasNext() ? "\", " : "\""));
                }
                out.println(classNodeEntries.hasNext() ? "] }," : "] }");
            }
            out.println("]");
        }
        return bytes.toString(StandardCharsets.UTF_8);
    }
}
