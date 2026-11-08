# Winter Wonderland

A cozy, immersive winter ambience mod for **Minecraft 26.3** (Fabric).
Designed to be combined with world-generation mods for a snowy winter playthrough.

> Status: early setup. See [DESIGN.md](DESIGN.md) for the planned features.

## Requirements

- Minecraft 26.3
- [Fabric Loader](https://fabricmc.net/use/) 0.20.1+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- Java 25

## Building

```bash
./gradlew build
```

The mod jar ends up in `build/libs/` (use the one without `-sources`).
Drop it into your `.minecraft/mods` folder together with Fabric API.

## Running a dev client

```bash
./gradlew runClient
```
