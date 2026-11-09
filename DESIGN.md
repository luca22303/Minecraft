# Winter Wonderland – Design

A **purely cozy** winter ambience mod for Minecraft 26.3 (Fabric), for a
**singleplayer** playthrough together with world-generation mods and shaders.
No penalties, no freezing to death – just a snowy wonderland.

## Target setup (the "modpack")

| Mod | Purpose |
|---|---|
| **Winter Wonderland** (this mod) | Winter world, weather, ambience, cozy touches |
| [Fabric API](https://modrinth.com/mod/fabric-api) | Required |
| [Mod Menu](https://modrinth.com/mod/modmenu) | In-game settings screen |
| [Terralith](https://modrinth.com/mod/terralith) + [Tectonic](https://modrinth.com/mod/tectonic) | World generation: lots of biomes + dramatic terrain, vanilla blocks only |
| [Living Glass](https://www.curseforge.com/minecraft/mc-mods/living-glass) | Raindrops, frost, condensation and breath fog on windows |
| [Sodium](https://modrinth.com/mod/sodium) + [Iris](https://modrinth.com/mod/iris) + a shader pack | Performance and lighting |

All of the above list Minecraft 26.3 support.

## Features

### 1. Snowy world

- **Graded winter by biome.** Every overworld biome gets colder, but by how much
  depends on its original climate:
  - cold biomes (taiga, snowy plains, mountains…): heavy snow, frequent blizzards
  - temperate biomes (plains, forests…): regular snowfall, snow stays on the ground
  - warm biomes (desert, savanna, jungle, badlands): occasional light snow that
    doesn't really stick
  - Nether/End: untouched
  - Works with modded biomes (Terralith etc.) because it is based on each
    biome's temperature, not a hard-coded list.
- **Snow layers build up** to about 2–4 layers during snowfall and melt back a
  bit on clear days. Walking stays easy.
- **Frozen water:** lakes and rivers freeze over in cold areas; oceans only
  freeze along the shore.
- **Snowy leaves:** leaves in snowy areas get a frosted, white-topped look
  (visual, works with any tree incl. modded), **and** thin real snow layers
  settle on top of leaf canopies.
- **Wintry colors:** grass and foliage shift to muted, frosty winter colors.
- **Icicles grow naturally** under roof edges and overhangs in cold areas (on
  terrain and on your builds) and melt back near heat sources.
- **Snow on window sills:** snow piles up on top of glass panes and window
  ledges during snowfall (complements Living Glass, which does the glass itself).

### 2. Weather

- **Varied winter weather:** a mix of gentle snowfall, atmospheric blizzards and
  clear, crisp sunny winter days.
- **Blizzards (atmospheric, harmless):**
  - denser snowfall, thicker fog, howling wind
  - the wind has a direction and snow blows sideways
  - **subtle wind push:** walking into the wind is ~15% slower, with the wind at
    your back slightly faster; only when outdoors – sheltered spots are unaffected

### 3. Ambience

- **Drifting snowflakes** float in the air around you, even between snowfalls.
- **Frosty breath** for every living creature in the cold – players, villagers,
  animals and hostile mobs (but not undead like skeletons and zombies, or
  constructs like golems).
- **Winter fog & light:** soft misty fog during snowfall/blizzards, crisper and
  bluer winter skies. Must cooperate with shader packs.
- **Warm glow (subtle, shader-friendly):** a slight warm halo around lanterns,
  torches, campfires and lit windows, plus a slightly warmer block-light tint.
  Deliberately understated because shaders do most of the lighting.

### 4. Sounds

Vanilla sounds first, replaced/extended with free (CC0) `.ogg` files later.

- **Wind:** soft wind during snowfall, howling wind in blizzards, quieter indoors
- **Snow crunch:** crunchy footsteps on snow
- **Fire crackle:** near campfires, fireplaces and furnaces
- **Muffled indoors:** outdoor weather sounds get softer when you're inside

### 5. Settings

In-game settings screen via Mod Menu, with toggles and sliders for every
feature (snow depth, blizzard frequency, wind push strength, breath,
snowflakes, fog, glow, sounds, …).

### Art

New simple pixel-art textures where needed; reuse vanilla textures wherever
possible.

### Not planned for now

- Survival/temperature mechanics, freezing damage
- Decorations (string lights, wreaths, fireplace, …) – maybe later
- Winter food and drinks
- Rain/frost/breath on glass – handled by Living Glass
- Multiplayer settings sync (singleplayer focus)

## Version 0.1 – first playable build

**Status: implemented** (not yet playtested in-game). Notes on how it was built:

- Vanilla rain = snowfall, vanilla thunderstorm = blizzard, so the weather cycle and `/weather`
  commands keep working. Blizzards are silent (no lightning in snow).
- Graded winter works by lowering each biome's temperature by climate class (cold / temperate /
  warm, from the biome's base temperature). The Nether and End are excluded via biome tags.
- Snow on tree canopies is vanilla behaviour once a biome is cold; the frosted leaf *look* is done
  with wintry foliage colors (a white snow-top overlay on leaves is a possible later addition).
- Snow on window sills: glass blocks and full-block sills already collect snow. Snow on top of
  glass *panes* needs a custom model and is postponed.
- Warm glow is a subtle warm block-light tint; a visible halo around light sources is postponed.
- Crisper, bluer clear-day skies are postponed (fog tint is done).
- Sounds use vanilla sound files via our own sound events, ready to be swapped for custom files.

Known side effect: like vanilla thunderstorms, blizzards darken the sky enough for hostile mobs to
spawn during the day. *(Fixed in 0.2 with the "Calm Blizzards" option.)*

## Version 0.2 – the postponed parts

**Status: implemented** (not yet playtested in-game).

- **Snow on panes:** a `pane_snow` block that forms on top of glass panes and iron bars during
  snowfall, follows the pane's connections, and melts on clear days or near light.
- **Snowy treetops:** the vanilla leaf models of oak, spruce, birch, jungle, acacia, dark oak and
  mangrove get a second "snow cap" layer. Its tint is white on canopy tops under open sky in snowy
  biomes, and transparent everywhere else. Inventory icons keep the plain look.
- **Glow halos:** client-side halo particles around non-solid light sources such as lanterns,
  torches, candles, campfires and end rods. They are brighter at night, indoors and in snowfall.
- **Crisp winter sky:** a positional sky-color layer on the client that deepens the blue on clear
  days in wintry biomes.
- **Calm blizzards:** blizzards no longer count as thunderstorms for daytime monster spawning.

Ideas for later: snow on fences and walls, custom wind and fire sounds, decorations, winter food.

Original plan:

A first version of **every** feature above, so the whole mod can be felt early,
then each feature gets deepened in later versions based on playtesting.

Suggested build order inside 0.1:

1. Config + Mod Menu screen (everything below hooks into it)
2. Graded winter per biome + varied weather + blizzards
3. Snow layers, frozen water, snow on leaves and window sills, icicles
4. Wintry colors + frosted leaves
5. Snowflakes, frosty breath, fog, warm glow
6. Wind push
7. Sounds
