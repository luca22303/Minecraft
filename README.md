# Winter Wonderland

A cozy, immersive winter ambience mod for **Minecraft 26.3** (Fabric).
Designed to be combined with world-generation mods and shaders for a snowy winter playthrough.
See [DESIGN.md](DESIGN.md) for the full plan.

## Features (v0.2)

**Snowy world**
- **Graded winter by biome.** Cold biomes get heavy snow, temperate biomes regular snow, and warm
  biomes (desert, jungle, savanna...) only light snow that doesn't stick. The Nether and End stay
  untouched. It works with modded biomes (Terralith etc.) because it uses each biome's temperature.
- **New chunks generate snow-covered**, with frozen lakes and rivers. Oceans only freeze along the shore.
- **Deeper snow:** up to 3 layers by default. Deep snow melts back to one layer on clear days.
- **Snowy treetops:** canopies facing the open sky get white snow caps in snowy biomes, and real snow
  layers settle on them too.
- **Snow on panes:** a thin ridge of snow settles on top of glass panes and iron bars during snowfall
  and melts on clear days. Living Glass handles the glass itself.
- **Icicles** slowly grow under roof edges and overhangs. They melt near torches, fire and other light,
  and drip a little on sunny days.
- **Wintry colors:** grass and leaves (including spruce and birch) take on muted, frosty colors.
- **Crisp winter sky:** a deeper, cleaner blue on clear winter days.

**Weather**
- Vanilla rain becomes **snowfall** and vanilla thunderstorms become **blizzards**
  (`/weather rain` and `/weather thunder` work for testing).
- **Calm blizzards:** unlike thunderstorms, blizzards don't let monsters spawn in daylight.
- **Blizzards:** the wind has a direction and snow blows sideways. Walking into the wind is up to 15%
  slower, while the wind at your back makes you a little faster. Only outdoors.

**Ambience**
- Drifting, swaying **snowflakes** in the air, even between snowfalls.
- **Frosty breath** for players and creatures in the cold. Undead and golems don't breathe.
- **Winter fog:** soft haze in snowfall, a white-out in blizzards, with a cold blue-grey tint.
- **Warm glow:** a slightly warmer tint on torch, lantern and fire light, kept subtle for shaders.
- **Glow halos:** soft halos around lanterns, torches, candles and campfires. Strongest at night,
  indoors and in snowfall. Soul lights glow blue and copper lights green.

**Sounds** (vanilla sounds for now, see below)
- Wind during snowfall, howling wind and gusts in blizzards
- Crunchy footsteps on snow
- Crackling near fires, campfires and lit furnaces
- Weather sounds are muffled when you're indoors

**Settings:** every feature can be toggled and tuned in-game via [Mod Menu](https://modrinth.com/mod/modmenu)
(*Mods → Winter Wonderland → Configure*), or in `config/winterwonderland.json`.

## Recommended companions

| Mod | Why |
|---|---|
| [Fabric API](https://modrinth.com/mod/fabric-api) | **Required** |
| [Mod Menu](https://modrinth.com/mod/modmenu) | In-game settings screen |
| [Terralith](https://modrinth.com/mod/terralith) + [Tectonic](https://modrinth.com/mod/tectonic) | Beautiful biomes and terrain |
| [Living Glass](https://www.curseforge.com/minecraft/mc-mods/living-glass) | Frost, condensation and breath fog on windows |
| [Sodium](https://modrinth.com/mod/sodium) + [Iris](https://modrinth.com/mod/iris) | Performance and shaders |

**Resource packs:** the snowy treetops replace the vanilla block models of oak, spruce, birch, jungle,
acacia, dark oak and mangrove leaves. A resource pack that changes those models takes priority, and
those leaves then show no snow caps.

**Use a new world:** the winter terrain (snow cover, frozen lakes) is applied when chunks are
generated. In existing chunks snow builds up over time while it snows.

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
Drop it into your `.minecraft/mods` folder together with Fabric API (and optionally Mod Menu).

## Running a dev client

```bash
./gradlew runClient
```

Mod Menu is included automatically in the dev client.

## Custom sounds

All sounds are defined in `src/main/resources/assets/winterwonderland/sounds.json` and currently
point to vanilla sounds. To use your own, put `.ogg` files (mono, for positional sounds) into
`src/main/resources/assets/winterwonderland/sounds/` and reference them as
`"winterwonderland:<file name without .ogg>"`.

| Sound event | Plays | Currently uses |
|---|---|---|
| `ambient.wind_loop` | Looping wind (snowfall/blizzard) | Elytra wind loop, pitched down |
| `ambient.wind_gust` | Random gusts in blizzards | Dry grass wind |
| `block.snow.crunch` | Extra crunch on snow footsteps | Powder snow steps |
| `ambient.fire_crackle` | Near fires and furnaces | Campfire and furnace crackle |
