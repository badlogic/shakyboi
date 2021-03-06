package io.marioslab.shakyboi;

import io.marioslab.shakyboi.lookup.*;
import io.marioslab.shakyboi.util.Pattern;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Command line driver for Shakyboi.
 */
public class ShakyboiCLI {
    public static void main(String[] args) throws IOException {
        long start = System.nanoTime();
        var settings = parseArgs(args);
        Shakyboi.Statistics stats = new Shakyboi().shake(settings);
        stats.warnings.forEach(s -> System.err.println("WARNING: " + s));
        if (settings.output != null) System.out.println("Output:                " + settings.output.getAbsolutePath());
        if (settings.htmlReport != null)
            System.out.println("HTML report:           " + settings.htmlReport.getAbsolutePath());
        if (settings.jsonReport != null)
            System.out.println("JSON report:           " + settings.jsonReport.getAbsolutePath());
        System.out.println("Total app classes:     " + stats.totalClasses);
        System.out.println("Reachable app classes: " + stats.reachableClasses);
        System.out.println("Reduction:             " + (int) ((1 - ((float) stats.reachableClasses / stats.totalClasses)) * 100) + "%");
        System.out.println("Took:                  " + (System.nanoTime() - start) / 1e9 + " secs");
    }

    static void printHelp() {
        System.out.println("Usage: shakyboi <options>");
        System.out.println("Options:");
        System.out.println();
        System.out.println("   --app <dir|jar>               A directory or .jar to lookup app class files in.");
        System.out.println();
        System.out.println("   --bootstrap <dir|jar|\"jrt\">   A directory, .jar, or \"jrt\" (Java runtime image)\n"
                + "                                 to lookup bootstrap class files in. \"jrt\" is the default.");
        System.out.println();
        System.out.println("   --root <class-name-pattern>   A root class name (pattern), e.g. my.package.App, **.Foo.");
        System.out.println("                                 You can specify multiple classes by using multiple --root.");
        System.out.println("                                 options.");
        System.out.println();
        System.out.println("   --output <jar-file>           The name of the output .jar file. Performs a dry-run if omitted.");
        System.out.println();
        System.out.println("   --html-report <html-file>     (Optional) The name of the .html file to write the report to.\n" +
                "                                 You can view it locally in a browser.");
        System.out.println();
        System.out.println("   --json-report <json-file>     (Optional) The name of the .json file to write the report to.");
    }

    static void error(String message) {
        error(message, false);
    }

    static void error(String message, boolean printHelp) {
        System.err.println(message);
        if (printHelp) printHelp();
        System.exit(-1);
    }

    static Shakyboi.Settings parseArgs(String[] args) {
        var appLookups = new ArrayList<Lookup>();
        var bootstrapLookups = new ArrayList<Lookup>();
        var rootClasses = new ArrayList<Pattern>();
        File output = null;
        File htmlReport = null;
        File jsonReport = null;

        for (int i = 0; i < args.length; i++) {
            var arg = args[i];
            if (i == args.length - 1) error("Missing value for argument:" + arg);
            if ("--app".equals(arg)) {
                var file = new File(args[++i]);
                if (!file.exists()) error("App class lookup file " + file.getAbsolutePath() + " does not exist");
                appLookups.add(file.isDirectory() ? new DirectoryLookup(file) : new JarLookup(file));
            } else if ("--bootstrap".equals(arg)) {
                arg = args[++i];
                if (arg.equals("jrt")) {
                    bootstrapLookups.add(new JrtImageLookup());
                } else {
                    var file = new File(arg);
                    if (!file.exists()) error("App class lookup file " + file.getAbsolutePath() + " does not exist");
                    bootstrapLookups.add(file.isDirectory() ? new DirectoryLookup(file) : new JarLookup(file));
                }
            } else if ("--root".equals(arg)) {
                var className = args[++i];
                className = className.replace(".", "/") + ".class";
                rootClasses.add(new Pattern(className));
            } else if ("--output".equals(arg)) {
                output = new File(args[++i]);
                var parent = output.getAbsoluteFile().getParentFile();
                if (output.exists() && output.isDirectory())
                    error("Output file " + output.getAbsolutePath() + " is a directory");
                if (!parent.exists()) {
                    if (!parent.mkdirs())
                        error("Couldn't create parent directory of output file " + output.getAbsolutePath());
                }
            } else if ("--html-report".equals(arg)) {
                htmlReport = new File(args[++i]);
                var parent = htmlReport.getAbsoluteFile().getParentFile();
                if (htmlReport.exists() && htmlReport.isDirectory())
                    error("HTML report file " + htmlReport.getAbsolutePath() + " is a directory");
                if (!parent.exists()) {
                    if (!parent.mkdirs())
                        error("Couldn't create parent directory of output file " + htmlReport.getAbsolutePath());
                }
            } else if ("--json-report".equals(arg)) {
                jsonReport = new File(args[++i]);
                var parent = jsonReport.getAbsoluteFile().getParentFile();
                if (jsonReport.exists() && jsonReport.isDirectory())
                    error("JSON report file " + jsonReport.getAbsolutePath() + " is a directory");
                if (!parent.exists()) {
                    if (!parent.mkdirs())
                        error("Couldn't create parent directory of output file " + jsonReport.getAbsolutePath());
                }
            } else {
                error("Unknown argument: " + arg, true);
            }
        }

        if (appLookups.size() == 0) error("No app class lookup given.", true);
        if (bootstrapLookups.size() == 0) {
            System.err.println("WARNING: No bootstrap classes specified, defaulting to JRT image.");
            bootstrapLookups.add(new JrtImageLookup());
        }
        if (rootClasses.size() == 0) error("No root classes given.", true);
        if (output == null) System.err.println("WARNING: No output file specified, performing dry run.");

        return new Shakyboi.Settings(new CombinedLookup(appLookups.toArray(new Lookup[appLookups.size()])),
                new CombinedLookup(bootstrapLookups.toArray(new Lookup[bootstrapLookups.size()])),
                rootClasses,
                output,
                htmlReport,
                jsonReport);
    }
}
