#!/bin/bash
set -e

if [ -z "$JAVA_HOME" ]
then
  echo "Please set JAVA_HOME to a JDK 16+ standard JVM installation."
  exit 1;
fi

# Build shakyboi
mvn clean package -DskipTests

# Download a few app jars and apply shakyboi and ProGuard for comparison
mkdir -p apps
pushd apps
rm *.jar
wget http://libgdx.badlogicgames.com/demos/paxbritannica/paxbritannica.jar
wget http://libgdx.badlogicgames.com/demos/invaders/invaders.jar

# Execute shakyboi on the apps
java -jar ../target/shakyboi.jar --app paxbritannica.jar \
    --root de.swagner.paxbritannica.desktop.DesktopLauncher \
    --output paxbritannica-shaky.jar

# Execute proguard on the apps
cat > proguard.conf << EOF
-injars       paxbritannica.jar
-outjars      paxbritannica-proguard.jar
-libraryjars  $JAVA_HOME/jmods/(!**.jar;!module-info.class)
-dontobfuscate
-dontoptimize
-ignorewarnings

-keep public class de.swagner.paxbritannica.desktop.DesktopLauncher {
    public static void main(java.lang.String[]);
}

-keepclassmembers class * extends java.lang.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class org.lwjgl.LWJGLUtil {
	void log (java.lang.CharSequence);
	boolean getPrivilegedBoolean (java.lang.String);
}
-keep class org.lwjgl.LWJGLException { <init> (java.lang.String); }
-keep class org.lwjgl.BufferUtils { java.nio.ByteBuffer createByteBuffer (int); }
-keep class org.lwjgl.PointerWrapper { long getPointer (); }
-keep class org.lwjgl.opengl.DisplayMode {
	private int width;
	private int height;
	private int freq;
	<init> (int, int, int, int);
}
-keep class org.lwjgl.opengl.GlobalLock { static java.lang.Object lock; }
-keepclassmembers class org.lwjgl.opengl.PixelFormat { <fields>; }
-keepclassmembers class org.lwjgl.opengl.Display {
	void doHandleQuit ();
	void setScaleFactor (float);
	void mouseInsideWindow (boolean);
	java.lang.Object createDisplayMode (int, int, int, int);
	void addDisplayMode (java.lang.Object, int, int, int, int);
	static org.lwjgl.opengl.DisplayImplementation getImplementation ();
}
-keep class org.lwjgl.opengl.WindowsFileVersion { <init> (int, int); }
-keep class org.lwjgl.opengl.LinuxDisplay { int globalErrorHandler (long, long, long, long, long, long, long); }
# LWJGL is needed on Mac for audio.
-keepclassmembers class org.lwjgl.opengl.MacOSXDisplay {
	void doHandleQuit ();
	void setScaleFactor (float);
	void mouseInsideWindow (boolean);
	java.lang.Object createDisplayMode (int, int, int, int);
	void addDisplayMode (java.lang.Object, int, int, int, int);
}
-keepclassmembers class org.lwjgl.opengl.MacOSXNativeKeyboard {
	void keyPressed (int, java.lang.String, long);
	void keyReleased (int, java.lang.String, long);
}
-keepclassmembers class org.lwjgl.opengl.MacOSXNativeMouse {
	void setButton (int, int, long);
	void setButton (float, float, float, float, float, long);
	public void mouseMoved (float, float, float, float, float, long);
}
# Classes for prefs JSON.
-keep class com.esotericsoftware.spine.common.PrefsStart { public <fields>; }

# Keep scene2d.ui classes for skin JSON.
-keep class com.badlogic.gdx.graphics.Color { <fields>; }
-keep class com.badlogic.gdx.graphics.g2d.BitmapFont {
	public com.badlogic.gdx.graphics.g2d.BitmapFont$BitmapFontData data; # Needed for LauncherFont (made public by build).
}
-keep class com.badlogic.gdx.scenes.scene2d.ui.Skin$TintedDrawable {}
-keep class com.badlogic.gdx.scenes.scene2d.ui.**$*Style {
	<fields>;
	public <init> (**$*Style); # Copy constructors needed for Common.copyStyle.
}

# Keep style get/set methods for Common.copyStyle.
-keepclassmembers class * implements com.badlogic.gdx.scenes.scene2d.utils.Layout {
	public ** getStyle ();
	public void setStyle (**$*Style);
}
EOF
proguard.sh @proguard.conf > proguard.log
popd