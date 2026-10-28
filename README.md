::: {align="center"}
# Bearing Load Addon

### Mass-aware stress calculations for Create swivel bearings

A lightweight compatibility addon that makes the stress impact of
bearing-mounted contraptions account for their load.

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-2ea043?style=for-the-badge)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/Mod%20Loader-NeoForge-orange?style=for-the-badge)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-blue?style=for-the-badge)](https://modrinth.com/mod/create)
:::

------------------------------------------------------------------------

## ✨ What is this?

**Bearing Load Addon** is a small addon for modded Minecraft focused on
the interaction between **Create**, rotating contraptions, and swivel
bearings.

The addon modifies the bearing stress calculation through a **Mixin**,
so the load associated with a contraption's mass can be taken into
account instead of relying only on the original calculation.

The goal is simple: make bearing-driven machinery behave in a way that
better reflects the weight it is carrying.

> This project is a compatibility/behaviour addon, not a standalone
> replacement for Create.

## ⚙️ How it works

At a high level, the addon hooks into the relevant bearing logic and
adjusts the stress calculation.

1.  **A bearing drives a contraption.** Create handles the bearing and
    contraption as usual.
2.  **The addon hooks into the calculation.** A Mixin injects into the
    target logic without replacing the whole Create mod.
3.  **Mass becomes part of the stress calculation.** The modified logic
    accounts for the contraption's load when calculating the bearing's
    stress impact.
4.  **Create continues handling the machinery.** The addon relies on
    Create's existing systems for the rest of the behaviour.

### Why use a Mixin?

A Mixin allows this addon to alter a specific part of another mod's code
at runtime. That keeps the change focused and avoids maintaining a full
fork of Create.

**Implementation note:** exact formula details and the precise injection
point depend on the source code for the current build. This README
describes the intended behaviour at a high level rather than claiming a
particular formula or multiplier.

## 📦 Requirements

The project is developed for the following environment:

  Component            Version
  -------------------- ----------------------------
  Minecraft            `1.21.1`
  NeoForge             Project-configured version
  Create               `6.0.10`
  Sable                `2.0.5`
  Create Aeronautics   `1.3.2`

Install the required mods in the Minecraft instance you want to use.
Keep the versions aligned with the ones listed above; other versions
have not been confirmed compatible.

## 🚀 Installation

1.  Install **Minecraft 1.21.1** with the NeoForge version required by
    the build.
2.  Install the required dependencies listed above.
3.  Download the addon `.jar` from the project's **Releases** page, if a
    release is available.
4.  Place the addon `.jar` in your instance's `mods` folder.
5.  Launch the game and check the log if the game fails to start.

If there is no published release yet, the project may need to be built
from source (see below).

## 🛠️ Building from source

You will need a compatible JDK for the Minecraft/NeoForge toolchain and
Git.

``` bash
git clone https://github.com/almarbvdut-bit/Bearing-Load-Addon.git
cd Bearing-Load-Addon
```

Build with the included Gradle wrapper:

**Windows**

``` bat
gradlew.bat build
```

**Linux / macOS**

``` bash
./gradlew build
```

If the build succeeds, look for the resulting mod JAR in:

``` text
build/libs/
```

The exact Java version and dependency setup are determined by the Gradle
configuration in this repository. If Gradle reports a missing
dependency, check the project setup and required local libraries before
changing versions.

## 🧪 Testing notes

When testing a build, compare the behaviour of a bearing-driven
contraption with different loads and keep the Minecraft, NeoForge,
Create, and addon versions consistent.

This project is focused on bearing stress behaviour. It does not promise
compatibility with every Create addon or every version combination.

## 🐛 Issues and contributions

Found a bug, a compatibility problem, or an unexpected stress result?

-   Open an
    [Issue](https://github.com/almarbvdut-bit/Bearing-Load-Addon/issues).
-   Include your Minecraft, NeoForge, Create, and addon versions.
-   Attach the relevant part of `latest.log` and a short description of
    how to reproduce the problem.

Pull requests and technical discussion are welcome. Please keep changes
focused and test them against the project's target environment.

## 📄 License

Check the repository for a `LICENSE` file before redistributing or
reusing this project. If no license is present, all rights remain with
the copyright holder by default.

------------------------------------------------------------------------

::: {align="center"}
**Built for heavier contraptions and more meaningful bearing stress.**
:::
# Bearing-Load-Addon
Adds mass-based stress calculations to Create Aeronautics swivel bearings, making heavier attached structures exert greater mechanical stress.
