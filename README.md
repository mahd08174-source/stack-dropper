# Stack Dropper

A client-side Fabric mod for **Minecraft 1.21.11** that automatically drops stacks from your inventory on a timer.

## Controls

| Key | Action |
| --- | --- |
| **V** | Toggle auto-drop on/off |
| **Tab** | Open / close the settings menu |

Both keys show up under **Options > Controls > Stack Dropper**.

## Defaults

- Drops **2 stacks** every **0.1 seconds**
- Hotbar stacks go first, then the main inventory
- Armor and offhand are never touched
- Drops pause while any screen (including the menu) is open

## Menu (Tab)

- **Auto-drop ON/OFF** toggle
- **Change drop keybind**: click, then press the new key (Esc cancels)
- **Stacks per drop**: 1 to 36
- **Whole inventory per drop**: drops all 36 inventory slots each time instead
- **Every (seconds)**: delay between drops, minimum 0.01

Settings are saved to `config/stack-dropper.json`.

## Building

Easiest: the **build** GitHub Action compiles it on every push. Open the Actions tab and download the `stack-dropper` artifact.

Locally (needs JDK 21 and Gradle 9.2+):

```
gradle build
```

The jar ends up in `build/libs/`. Drop it in your `mods` folder with Fabric Loader 0.19.2+ and Fabric API for 1.21.11.

## Notes

- Tab is also vanilla's player-list key, so both will trigger unless you rebind one in Controls.
- Timing runs on game ticks (20/s), so very short intervals are effectively 0.05s steps. Many servers kick or rate-limit rapid dropping; use it where that's allowed.
