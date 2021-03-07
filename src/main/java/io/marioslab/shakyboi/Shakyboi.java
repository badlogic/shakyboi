package io.marioslab.shakyboi;

import io.marioslab.shakyboi.graph.ClassDependencyGraph;
import io.marioslab.shakyboi.graph.ClassDependencyGraphGenerator;
import io.marioslab.shakyboi.lookup.Lookup;
import io.marioslab.shakyboi.util.JarFileWriter;
import io.marioslab.shakyboi.util.Pattern;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Call {@link #shake(Settings)} with {@link Settings} to class tree shake one or more class files.
 *
 * @see ShakyboiCLI
 * @see <a href="https://marioslab.io/posts/shakyboi/shakyboi-part-1/">Shakyboi blog post series.</a>
 */
public class Shakyboi {
    /**
     * Applies class tree shaking to the app classes given as a {@link Lookup} in the {@link Settings}. Generates
     * an output <code>.jar</code> file containing all reachable classes from the app lookup, as well as any files
     * found in the app lookup. Optionally generates a HTML and/or JSON report file. See {@link Settings}.
     *
     * @param settings the {@link Settings} specifying input and output parameters for the class tree shaking.
     * @return {@link Statistics} generated during class tree shaking.
     * @throws IOException in case a file couldn't be read from a lookup.
     */
    public static Statistics shake(Settings settings) throws IOException {
        // expand root classes
        long timeRootClassExpansion = System.nanoTime();
        var rootClassNames = new ArrayList<String>();
        var inputClassesAndFiles = settings.appLookup.list();
        var inputClasses = inputClassesAndFiles.stream().filter(f -> f.endsWith(".class")).collect(Collectors.toList());
        var inputFiles = inputClassesAndFiles.stream().filter(f -> !f.endsWith(".class")).collect(Collectors.toList());
        for (var file : inputClasses) {
            for (var rootPattern : settings.rootClasses) {
                if (rootPattern.matchesPath(file)) {
                    rootClassNames.add(file.replace(".class", ""));
                    break;
                }
            }
        }
        if (rootClassNames.size() == 0) throw new IOException("No root classes found in app lookup.");
        timeRootClassExpansion = System.nanoTime() - timeRootClassExpansion;

        // Generate the class dependency graph and gather all reachable app classes.
        long timeClassDependencyGraph = System.nanoTime();
        var warnings = new ArrayList<String>();
        var classDependencyGraph = ClassDependencyGraphGenerator.generate(settings.appLookup,
                settings.bootstrapLookup,
                warnings,
                rootClassNames.toArray(new String[0]));
        var reachableAppClasses = classDependencyGraph.reachableClasses.values().stream().filter(cl -> cl.isAppClass).collect(Collectors.toList());
        timeClassDependencyGraph = System.nanoTime() - timeClassDependencyGraph;

        // Write output .jar file
        long timeWriteJar = System.nanoTime();
        if (settings.output != null) {
            try (var writer = new JarFileWriter(settings.output)) {
                for (var file : inputFiles)
                    writer.addFile(file, settings.appLookup.findResource(file));

                for (var clazz : reachableAppClasses)
                    writer.addFile(clazz.classFile.getName() + ".class", clazz.classFile.originalData);
            }
        }
        timeWriteJar = System.nanoTime() - timeWriteJar;

        // Create report if requested
        long timeReport = System.nanoTime();
        if (settings.htmlReport != null) generateHtmlReport(settings, inputClasses, classDependencyGraph);
        if (settings.jsonReport != null) generateJsonReport(settings, inputClasses, classDependencyGraph);
        timeReport = System.nanoTime() - timeReport;

        return new Statistics(inputClasses.size(), reachableAppClasses.size(), warnings, timeRootClassExpansion / 1e9f, timeClassDependencyGraph / 1e9f, timeWriteJar / 1e9f, timeReport / 1e9f);
    }

    private static String generateJson(List<String> inputClasses, ClassDependencyGraph classDependencyGraph) {
        var reachableJson = ClassDependencyGraphGenerator.generateJSON(classDependencyGraph, true);
        var removedClasses = inputClasses.stream()
                .map(s -> s.replace(".class", ""))
                .filter(c -> !classDependencyGraph.reachableClasses.containsKey(c)).sorted(String::compareTo).collect(Collectors.toList());
        var removedJson = new StringBuilder();
        removedJson.append("[\n");
        for (int i = 0; i < removedClasses.size(); i++) {
            removedJson.append('"').append(removedClasses.get(i).replace('/', '.')).append('"');
            if (i < removedClasses.size() - 1) removedJson.append(",\n");
            else removedJson.append("\n");
        }
        removedJson.append("]\n");
        return "\"reachableClasses\": " + reachableJson + ", \"removedClasses\": " + removedJson;
    }

    private static void generateJsonReport(Settings settings, List<String> inputClasses, ClassDependencyGraph classDependencyGraph) throws IOException {
        String json = generateJson(inputClasses, classDependencyGraph);
        try (FileWriter writer = new FileWriter(settings.jsonReport)) {
            writer.write("{" + json + "}");
        }
    }

    private static void generateHtmlReport(Settings settings, List<String> inputClasses, ClassDependencyGraph classDependencyGraph) throws IOException {
        var json = generateJson(inputClasses, classDependencyGraph);
        var template = new String(Shakyboi.class.getResourceAsStream("/htmlreport.html").readAllBytes(), StandardCharsets.UTF_8);
        template = template.replace("%data%", json);
        try (FileWriter out = new FileWriter(settings.htmlReport)) {
            out.write(template);
        }
    }

    /**
     * Statistics generated by {@link #shake(Settings)}.
     */
    public static class Statistics {
        /** Total number of app classes */
        public final int totalClasses;
        /** Number of reachable classes as determined by {@link ClassDependencyGraphGenerator} */
        public final int reachableClasses;
        /** Warning messages generated by {@link #shake(Settings)} */
        public final List<String> warnings;
        /** Time in seconds to generate the class dependency graph */
        public final float timeRootClassExpansion;
        /** Time in seconds to generate the class dependency graph */
        public final float timeClassDependencyGraph;
        /** Time in seconds to write the output .jar file */
        public final float timeWriteJar;
        /** Time in seconds to write the report(s) */
        public final float timeWriteReport;

        public Statistics(int totalClasses, int reachableClasses, List<String> warnings, float timeRootClassExpansion, float timeClassDependencyGraph, float timeWriteJar, float timeWriteReport) {
            this.totalClasses = totalClasses;
            this.reachableClasses = reachableClasses;
            this.warnings = warnings;
            this.timeRootClassExpansion = timeRootClassExpansion;
            this.timeClassDependencyGraph = timeClassDependencyGraph;
            this.timeWriteJar = timeWriteJar;
            this.timeWriteReport = timeWriteReport;
        }
    }

    /**
     * Specifies app {@link Lookup}, bootstrap {@link Lookup}, root classes, and output file for {@link Shakyboi}.
     * Optionally specify HTML and JSON report output files.
     */
    public static class Settings {
        /** The {@link io.marioslab.shakyboi.lookup.Lookup} to find app files in **/
        public final Lookup appLookup;
        /** The {@link io.marioslab.shakyboi.lookup.Lookup} to find bootstrap files in **/
        public final Lookup bootstrapLookup;
        /** List of root classes given as {@link io.marioslab.shakyboi.util.Pattern} instances */
        public final List<Pattern> rootClasses;
        /** Output file **/
        public final File output;
        /** Optionel HTML report file, may be null **/
        public final File htmlReport;
        /** Optionel JSON report file, may be null **/
        public final File jsonReport;

        /**
         * Creates a new settings instance to be passed to {@link #shake(Settings)}.
         *
         * @param appLookup       the {@link Lookup} to find app files in.
         * @param bootstrapLookup the {@link Lookup} to find bootstrap files in.
         * @param rootClasses     the list of root classes given as {@link Pattern} instances.
         * @param output          the output <code>.jar</code> file. The parent directory must exist.
         * @param htmlReport      optional file to write the HTML report to. May be null.
         * @param jsonReport      optional file to write the JSON report to. May be null.
         */
        public Settings(Lookup appLookup, Lookup bootstrapLookup, List<Pattern> rootClasses, File output, File htmlReport, File jsonReport) {
            this.appLookup = appLookup;
            this.bootstrapLookup = bootstrapLookup;
            this.rootClasses = rootClasses;
            this.output = output;
            this.htmlReport = htmlReport;
            this.jsonReport = jsonReport;
        }
    }
}
