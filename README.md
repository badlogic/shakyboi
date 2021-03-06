# Shakyboi
A Java class tree shaker to reduce your uber-jar file sizes by removing unused classes. See the [blog post series](https://marioslab.io/posts/shakyboi/shakyboi-part-1/) on the motivation and implementation details.

## Building
`mvn package`

This will generate `target/shakyboi.jar`

## Usage
Run `java -jar shakyboi.jar` to see the help text.

A simple usage example:

```
java -jar shakyboi.jar \
    --app myapp.jar \
    --root my.app.MainClass \
    --output myapp-shaky.jar
```

This will read `myapp.jar`, trace all class dependencies starting at the root class `my.app.MainClass`, and output all resource files found in `myapp.jar` as well as all `.class` files of all classes reachable from `my.app.MainClass` to `myapp-shaky.jar`.

Shakyboi is unable to find classes only referred to via reflection. For this case, add these classes as roots.

```
java -jar shakyboi.jar \
    --app myapp.jar \
    --root my.app.MainClass \
    --root my.app.ClassReferencedByReflection \
    --output myapp-shaky.jar
```

You can also specify root classes are patterns.

```
java -jar shakyboi.jar \
    --app myapp.jar \
    --root my.app.MainClass \
    --root my.app.**.*Reflection \
    --output myapp-shaky.jar
```

Shakyboi can also generate a report on included and removed classes, either as HTML or JSON. Use the `--html-report <file>` and `--json-report <file>` options to specify where to output the reports.

```
java -jar shakyboi.jar \
    --app myapp.jar \
    --root my.app.MainClass \
    --root my.app.**.*Reflection \
    --output myapp-shaky.jar \
    --html-report report.html \
    --json-report report.json
```

Shakyboi needs to be able to find bootstrap classes referenced by your app's classes, like `java.lang.Object`. These are usually found in the Java Runtime Class Library that ships with your JRE or JDK. By default, Shakyboi uses the class library of the JRE/JDK it is executed with. You can specify bootstrap class lookups explicitely via `--bootstrap`.

```
java -jar shakyboi.jar \
    --app myapp.jar \
    --root my.app.MainClass \
    --root my.app.**.*Reflection \
    --output myapp-shaky.jar \
    --html-report report.html \
    --json-report report.json \
    --bootstrap /opt/jdk8/lib/rt.jar
```