#!/bin/bash
export JAVA_HOME=/opt/jdk-21
GRADLE_PATH=/home/kaity/.gemini/tmp/gradle9/gradle-9.4.1/bin/gradle

echo "Starting build with Java 21 and Gradle 9.4.1..."
if ! $GRADLE_PATH clean shadowJar; then
  echo "Build failed! Aborting installation."
  exit 1
fi

JAR_PATH="build/libs/KatCombat-1.0.0.jar"
DEST_DIR="/home/kaity/Documents/Folia/plugins/"

if [ -d "$DEST_DIR" ]; then
  echo "Installing plugin to $DEST_DIR..."
  cp "$JAR_PATH" "$DEST_DIR/"
  echo "Done! Plugin installed."
else
  echo "Warning: Directory $DEST_DIR does not exist. Skipping installation."
fi
