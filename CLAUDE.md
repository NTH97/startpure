# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

DreamBot is a scripting project for the DreamBot OSRS client. It is a Java 11 multi-project Gradle build using Kotlin DSL build scripts. Each script is a separate Gradle subproject.

## Build Commands

```bash
# Build the project
gradlew.bat build

# Compile only
gradlew.bat compileJava

# Copy built JARs to ~/DreamBot/Scripts/
gradlew.bat copyScripts

# Clean build artifacts
gradlew.bat clean

# Build and deploy in one step
gradlew.bat build copyScripts
```

## Environment Setup

No special environment variables are required. The build depends on `C:/Users/nthju/DreamBot/BotData/repository2/dreambot-client.jar` being present (installed by the DreamBot client).

## DreamBot API Documentation

JavaDocs: https://dreambot.org/javadocs

This is a standard JavaDocs site. To find API details:

- **By package**: Navigate from the package list on the left. The root package is `org.dreambot.api` with sub-packages for each system.
- **By class**: Use the search bar or browse the "All Classes" index.
- **Key packages**:
  - `org.dreambot.api.script` — Script lifecycle (`AbstractScript` base class, `@ScriptManifest` annotation), event handling, task framework.
  - `org.dreambot.api.methods` — Primary game interaction methods (banking, combat, skills, walking, etc.).
  - `org.dreambot.api.wrappers` — Wrappers around game entities (NPCs, objects, items, players, widgets).
  - `org.dreambot.api.input` — Keyboard and mouse input.
  - `org.dreambot.api.methods.interactive` — Interactive game object handling.
  - `org.dreambot.api.methods.settings.PlayerSettings` — Varbit/Varp access for reading game state.
  - `org.dreambot.api.methods.widget.Widgets` — Widget tree interaction for UI elements.

When implementing script logic, look up the relevant class in the JavaDocs to find available methods and their signatures.

## Architecture

- **Build system**: Gradle Kotlin DSL multi-project build. A custom `BootstrapPlugin` (in `buildSrc/`) registers a `copyScripts` task that copies built JARs to `~/DreamBot/Scripts/`.
- **Subproject convention**: Each script is a directory at the project root (e.g., `dreambot-script/`) with a matching build file (`dreambot-script/dreambot-script.gradle.kts`). To add a new script: create a directory, add a `<name>.gradle.kts` build file inside it, and add the name to the `include()` block in `settings.gradle.kts`.
- **Entry point**: Each script's `Main.java` extends `AbstractScript` and is annotated with `@ScriptManifest`. The `onLoop()` method implements the main logic as a state machine — it returns a sleep duration (ms) and is called repeatedly by the DreamBot runtime.
- **Dependencies** (all `compileOnly`): DreamBot `dreambot-client.jar` (local), Lombok. Dependencies are `compileOnly` because the DreamBot client provides them at runtime.
- **Annotation processors**: Lombok (code generation for `@Getter`, `@Setter`, etc.).
- **Version catalog**: `gradle/libs.versions.toml` defines library versions referenced via `rootProject.libs.*`.
- **Group**: `org.dreambot.scripts`
- **Java**: Source and target compatibility Java 11, UTF-8 encoding.

## Script Patterns

- **Game state checks**: Use `PlayerSettings.getBitValue(varbitId)` to read varbits and determine game state.
- **UI interaction**: Use `Widgets.getWidget(group, child)` to interact with game interface elements.
- **Sleep/wait**: `onLoop()` return value controls delay between iterations. Use `sleepUntil()` for conditional waits within a loop iteration.
- **Data modeling**: Use enums to define furniture/item progressions with their requirements (see `Pool.java`, `JewelleryBox.java` for examples).

## Git Workflow

After every completed request, you MUST commit and push the changes:

1. Stage the changed files: `git add <files>`
2. Commit with a descriptive message: `git commit -m "description of changes"`
3. Push to remote: `git push`

Do not skip this step. Every request that modifies code or files must end with a commit and push.
