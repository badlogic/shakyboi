package io.marioslab.shakyboi.tests;

import io.marioslab.shakyboi.graph.ClassDependencyGraphGenerator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class DescriptorParsingTest {
    @Test
    public void testFieldDescriptor() {
        assertNull(ClassDependencyGraphGenerator.getClassFromFieldDescriptor("B"));
        assertEquals("java/lang/Object", ClassDependencyGraphGenerator.getClassFromFieldDescriptor("Ljava/lang/Object;"));
        assertEquals("java/lang/Object", ClassDependencyGraphGenerator.getClassFromFieldDescriptor("[[[[[Ljava/lang/Object;"));
    }

    @Test
    public void testMethodDescriptor() {
        assertEquals(Collections.EMPTY_LIST, ClassDependencyGraphGenerator.getClassesFromMethodDescriptor("()V"));
        assertEquals(Arrays.asList("java/lang/Object", "java/lang/Object", "java/lang/Object"),
                ClassDependencyGraphGenerator.getClassesFromMethodDescriptor("(BILjava/lang/Object;FF[[[Ljava/lang/Object;JJ)[[Ljava/lang/Object;"));
    }
}
