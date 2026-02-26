# Archer's Offhand

<p align="center">
  <a href="https://modrinth.com/mod/archers-offhand">
    <img src="https://img.shields.io/badge/Available%20on-Modrinth-1bd96a?style=for-the-badge&logo=modrinth&scale=1.5" alt="Available on Modrinth">
  </a>
</p>

A powerful, highly configurable, and ethically-minded Minecraft Fabric mod that automates offhand inventory management for bow and crossbow users. Never scramble for arrows in the middle of a fight again.

## ✨ Features

- **Smart Ammo Management**: Automatically fetches and equips arrows and fireworks to your offhand the moment you hold a bow or crossbow.
- **Dynamic Restoration**: Instantly restores your previous offhand item (like a Shield or Totem of Undying) as soon as you put your bow away.
- **Multiple Ammo Selection Modes**:
  - `REGULAR`: Grabs the first available stack of arrows or rockets.
  - `SHUFFLE`: Picks a random stack of special arrows or rockets to keep your enemies guessing.
  - `SERIAL`: Cycles methodically through your ammo types, one by one.
- **Advanced Delay Presets**: Choose between 5 preset delay profiles designed to balance performance, feel, and anti-cheat compliance:
  - `PERFORMANCE`: Higher ticks to minimize server/client impact.
  - `SPEED`: Zero delay for maximum responsiveness.
  - `BALANCED`: The default, fine-tuned vanilla experience.
  - `ADAPTIVE`: Dynamically randomizes timings acting as a decoy for strict anti-cheat plugins.
  - `CUSTOM`: Complete control over individual delay values via sliders.
- **Failsafe & Protection System**: Prevents replacing vital offhand items (Totems/Shields) without permission, and can automatically revert to a Totem if your health drops below a heavily configurable threshold.

## ⚙️ How It Works (Under the Hood)

Archer's Offhand is powered by a robust **State Machine** architecture ensuring reliable, lag-free performance:

- **Idle State**: Monitors your hands waiting for a ranged weapon.
- **Tracking State**: Actively manages your ammo supply while a weapon is equipped, adhering to your chosen `AmmoStrategy` (Regular, Shuffle, Serial) and scanning delays.
- **Cooldowns & Queues**: An advanced `InventoryManager` handles the actual item swapping. It utilizes a sophisticated click-queue system to deliberately space out inventory simulated clicks, defeating rapid-fire anti-cheat kicks while maintaining a smooth user experience.

## 📥 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) (0.18.4+)
2. Install [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api) (0.141.2+ for 1.21.11)
3. Install [Yet Another Config Lib (YACL)](https://modrinth.com/mod/yet-another-config-lib)
4. _(Optional but recommended)_ Install [ModMenu](https://modrinth.com/mod/modmenu) for an easy in-game configuration screen.
5. Drop the Archer's Offhand `.jar` into your Minecraft `mods` folder.

## 🔧 Configuration

Archer's Offhand features a rich, structured configuration screen powered by YACL. Access it via ModMenu to tweak:

- Master toggles and behavior types.
- Specific Arrow/Rocket prioritization.
- Failsafes and Low-Health threshold sliders.
- Action Delays (Equipping, Unequipping, Swapping, Scanning, and Movement).

## 🐛 Support & Contributions

Encounter an issue or have a suggestion?

- Report bugs or request features on the [GitHub Issues](https://github.com/Carloplayz/ArchersOffhand/issues) page.
- Contributions are always welcome! Feel free to fork, make your changes, and submit a PR.

---

_Built with love for the Minecraft Fabric community._
