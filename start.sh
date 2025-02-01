#!/bin/bash
set -e

if [ ! -f "./gradlew" ]; then
  echo "Error: Gradle wrapper not found."
  exit 1
fi

echo "Building the application with Gradle..."
./gradlew clean build

JAR_FILE=$(find build/libs -type f -name "*.jar" | head -n 1)
if [ -z "$JAR_FILE" ]; then
  echo "Error: No jar file found in build/libs."
  exit 1
fi

echo "Starting the ATM simulation CLI..."
java -jar "$JAR_FILE"