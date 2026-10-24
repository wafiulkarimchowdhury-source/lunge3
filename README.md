# Left Click Lunge (Fabric, Minecraft 26.3, client-side)

Left click (tap or hold) performs the spear **Lunge attribute swap** for you, in a single game tick:
switch to your Lunge spear, jab, switch back. Hold the button to chain lunges every 5 ticks.

## Rules it follows
- **Only works when no player or mob is within 4 blocks of you.** If anyone is closer, left click behaves
  like normal Minecraft (so you can fight and mine normally).
- Needs a spear with the **Lunge** enchantment somewhere on your **hotbar** (highest Lunge level is used).
- Stays off when a menu is open, when you are eating/blocking, or when your food is too low for Lunge.
- **Toggle key** (default `O`): Options > Controls > Key Binds > "Left Click Lunge" > "Toggle Left Click Lunge".
  Rebind it there. The ON/OFF state is saved.

## Tips
- Fastest lunges: stand on an **empty hotbar slot** (or hold food/blocks) when you click. Whatever you hold
  between lunges sets the pace (empty hand ~5 ticks, sword ~12-13, mace ~33).
- While the mod is ON, left click is taken over whenever you have a Lunge spear and nobody is within 4 blocks,
  so mining with left click won't work then. Press the toggle key to mine normally.
- Each lunge costs hunger. Keep food handy.

## Settings
`.minecraft/config/leftclicklunge.json` (edit while the game is closed):

| Setting | Default | Meaning |
|---|---|---|
| `enabled` | true | master switch (the toggle key flips this) |
| `safeRadius` | 4.0 | no lunging if a player/mob is closer than this many blocks |
| `repeatIntervalTicks` | 5 | min ticks between lunges while holding (20 = 1 second) |
| `lowestFoodLevelToLunge` | 7 | stays off below this food level |
| `fixedSpearSlot` | -1 | -1 = auto-find spear, 0-8 = force a hotbar slot (0 = first slot) |
| `onlyLivingEntities` | true | false = dropped items, boats, minecarts etc. also count as "nearby" |
| `actionBarMessages` | true | show ON/OFF above the hotbar when toggling |

## Build the jar (pick one)

**A) No installs: let GitHub build it**
1. Make a free GitHub account, create a new repository, and upload everything in this folder
   (including the hidden `.github` folder).
2. Open the repository's **Actions** tab, click the latest "build" run, and wait for the green check.
3. Download **Artifacts** at the bottom of the run, unzip it, and use `leftclicklunge-1.0.0.jar`
   (not the `-sources` one).

**B) On your PC**
1. Install **JDK 25** (https://adoptium.net).
2. In this folder run `./gradlew build` (Windows: `gradlew.bat build`). First run downloads a lot.
3. The jar is `build/libs/leftclicklunge-1.0.0.jar` (not the `-sources` one).

Then install Fabric Loader (0.19.5+) for Minecraft **26.3** and **Fabric API**, and put the jar in `.minecraft/mods`.

## Notes
- Attribute swapping relies on a vanilla bug (MC-28289). Mojang has said it is unsupported. It was patched in
  26.2 pre-release 2 and brought back in pre-release 3. I could not confirm whether it works in 26.3, so test it:
  if lunges only happen at the spear's normal slow speed, the swap no longer works in your version.
- Many multiplayer servers treat swap macros as cheating. Check the rules before using it online.
