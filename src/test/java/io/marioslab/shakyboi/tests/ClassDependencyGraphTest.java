package io.marioslab.shakyboi.tests;

import io.marioslab.shakyboi.graph.ClassDependencyGraph;
import io.marioslab.shakyboi.graph.ClassDependencyGraphGenerator;
import io.marioslab.shakyboi.lookup.ClassLoaderClassLookup;
import io.marioslab.shakyboi.lookup.JrtImageClassLookup;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClassDependencyGraphTest {
    @Test
    public void testSimple() throws IOException {
        var graph = ClassDependencyGraphGenerator.generate(new ClassLoaderClassLookup(), new JrtImageClassLookup(), "io/marioslab/shakyboi/tests/apps/simple/App");
        var appClasses = new HashMap<String, ClassDependencyGraph.ClassNode>();
        var bootstrapClasses = new HashMap<String, ClassDependencyGraph.ClassNode>();
        for (var entry : graph.reachableClasses.entrySet()) {
            var className = entry.getKey();
            var classNode = entry.getValue();
            if (classNode.isAppClass) appClasses.put(className, classNode);
            else bootstrapClasses.put(className, classNode);
        }
        assertEquals(4, appClasses.size());
        assertEquals(4, bootstrapClasses.size());

        List<String> expectedAppClassNames = Arrays.asList("io/marioslab/shakyboi/tests/apps/simple/App",
                "io/marioslab/shakyboi/tests/apps/simple/Bar",
                "io/marioslab/shakyboi/tests/apps/simple/Zap",
                "io/marioslab/shakyboi/tests/apps/simple/Zop");
        int foundAppClasses = 0;
        for (var className : expectedAppClassNames) {
            if (appClasses.containsKey(className)) foundAppClasses++;
        }
        assertEquals(4, foundAppClasses);

        List<String> expectedBootstrapClassNames = Arrays.asList("java/lang/Object",
                "java/lang/String",
                "java/lang/System",
                "java/io/PrintStream");
        int foundBootstrapClasses = 0;
        for (var className : expectedBootstrapClassNames) {
            if (bootstrapClasses.containsKey(className)) foundBootstrapClasses++;
        }
        assertEquals(4, foundBootstrapClasses);

        System.out.println(ClassDependencyGraphGenerator.generateDotFile(graph));
    }
}
