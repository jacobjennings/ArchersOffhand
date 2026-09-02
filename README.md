# Archer's Offhand

<p align="center">
  <a href="https://modrinth.com/mod/archers-offhand">
    <img src="https://img.shields.io/badge/Available%20on-Modrinth-1bd96a?style=for-the-badge&logo=modrinth&scale=1.5" alt="Available on Modrinth">
  </a>
</p>

This is a client-only Minecraft **26.1.2** fork of [Archer's Offhand](https://github.com/Carloplayz/ArchersOffhand), originally created by Carloplayz. It loads a preferred crossbow projectile from the player inventory and restores the previous offhand item when it is safe to do so.

## Behavior

- Selection starts only while a **crossbow is held in the main hand**. Other held items do not activate the manager.
- The crossbow's supported projectiles are arrows and firework rockets. Inventory scanning can include or exclude the hotbar.
- Preferences are an ordered list: the first matching rule wins, and inventory order breaks ties.
- The fallback policy is evaluated only after every ordered preference fails. It can be:
  - `NONE` — leave the offhand unchanged.
  - `ANY_ARROW` — accept any arrow tagged for crossbows.
  - `ANY_FIREWORK` — accept any firework rocket.
  - `ANY_PROJECTILE` — accept any compatible arrow or firework rocket.

The preference catalog includes generic and specific rules:

- Any compatible projectile, any arrow, any tipped arrow, any firework rocket, or a specific item ID.
- Potion rules match the potion contents of tipped arrows.
- Firework rules can match flight duration, utility rockets with no explosions, explosive rockets, or rockets containing a particular explosion shape.
- An item observed in the inventory can be saved as an exact variant. Exact matching preserves the complete item components, including potion contents and every firework component: flight duration, all explosions, shapes, colors, fade colors, trail, and twinkle.

## Safe inventory handling

The mod uses one vanilla inventory `SWAP` operation between a player-inventory slot and the offhand. It does not emulate a pickup/drag sequence or operate through a chest, crafting table, or other open container.

Before a swap, the player inventory menu must be active, no screen may be open, and the carried cursor stack must be empty. The expected source and offhand stacks are observed before the operation, and the local result must be the exact exchange before the mod claims the projectile. If those conditions are not met, the attempt is deferred or abandoned safely.

Restoration is conservative as well:

- The original offhand stack is retained exactly, including its count and components.
- Restoration occurs after unequipping the crossbow, disabling the mod, or crossing the configured low-health threshold (when enabled).
- If the player replaces the managed projectile with a different nonempty offhand stack, the mod leaves that stack alone and does not overwrite it. The original item remains in the inventory when it can be identified safely.
- The original stack must still be found as an exact match. If it moved, the inventory is searched before restoration; if no safe exact match exists, restoration is blocked rather than guessing.
- Occupied-offhand replacement and low-health restoration are independently configurable.

## Configuration

The configuration screen is built with [Yet Another Config Lib (YACL)](https://modrinth.com/mod/yet-another-config-lib) and is available from [Mod Menu](https://modrinth.com/mod/modmenu), or with the default `O` key. It includes the ordered projectile list, fallback policy, inventory-scan scope, occupied-offhand protection, low-health threshold, equip/restore/click timing, and optional debug logging.

Keybindings are registered in Minecraft's Controls menu:

- `O` opens the configuration screen.
- Toggle and primary-preference-cycle actions are unbound by default and can be assigned by the player.

## Installation

This fork targets Minecraft **26.1.2** and requires:

1. Java **25**
2. [Fabric Loader 0.19.3 or newer](https://fabricmc.net/use/installer/)
3. [Fabric API 0.153.0+26.1.2](https://modrinth.com/mod/fabric-api)
4. [YACL 3.9.5+26.1-fabric](https://modrinth.com/mod/yet-another-config-lib)
5. [Mod Menu 18.0.0-beta.1 or newer](https://modrinth.com/mod/modmenu)

Put the fork's `.jar` in the client `mods` directory. The server does not need this client-only mod.

## Development

Use a Java 25 toolchain and run:

```text
./gradlew build
```

The build uses the canonical Mojang names supplied by Minecraft 26.1.2.

## Support and contributions

For this fork's source, bug reports, and pull requests, use [jacobjennings/ArchersOffhand](https://github.com/jacobjennings/ArchersOffhand) and its [issue tracker](https://github.com/jacobjennings/ArchersOffhand/issues).

Please credit the upstream [Carloplayz/ArchersOffhand](https://github.com/Carloplayz/ArchersOffhand) project when reusing its work.

---

_Built with love for the Minecraft Fabric community._
