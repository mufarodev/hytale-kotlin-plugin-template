# Hytale Kotlin Plugin Template

A modern, "batteries-included" template for creating Hytale server plugins using **Kotlin 2.3.0** and **Java 25**.

## Features

- **Latest Tech Stack**: Pre-configured for Java 25 and Kotlin 2.3.0.
- **Kotlin DSL**: All build logic uses `build.gradle.kts`.
- **Automatic Run Configuration**: Includes an IntelliJ run configuration to start the server with one click.
- **Smart Server Detection**: Automatically finds your local Hytale installation.
- **Version Management**: Automatically injects your project version into `manifest.json`.

## Prerequisites

1.  **Java 25 JDK**: Ensure you have JDK 25 installed.
2.  **Hytale**: You must have the Hytale client/server installed.

## Getting Started

1.  **Clone the repository**.
2.  **Open in IntelliJ IDEA**.
3.  **Sync Gradle**: Allow IntelliJ to import the project structure.
4.  **Run the Server**:
    - Look for the **"Run Server"** configuration in the top-right toolbar.
    - Click the green Play button (▶).
    - This will compile your code, package it, and start the Hytale server with your plugin installed.

## Project Structure

- `src/main/kotlin`: Your plugin source code.
- `src/main/resources/manifest.json`: Plugin metadata (version is updated automatically).
- `buildSrc`: Custom build logic for running the Hytale server.
- `build.gradle.kts`: Main project configuration.
- `gradle.properties`: Project properties (version, group ID, etc.).

## Configuration

You can customize your plugin in `gradle.properties`:

```properties
pluginGroup=com.example
pluginVersion=0.0.1
pluginDescription=My Awesome Hytale Plugin
```

## Troubleshooting

- **"HytaleServer.jar not found"**: The build script tries to find the server in `%APPDATA%/Hytale/install/release/...`. If your installation is in a custom location, copy `HytaleServer.jar` into a `run/` folder in the project root manually.
- **Java Version Errors**: Ensure your Gradle settings in IntelliJ are using JDK 25.
