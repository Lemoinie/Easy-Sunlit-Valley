# Easy Sunlit Valley

An all-in-one automation and helper mod for the **Society: Sunlit Valley** Minecraft modpack (Forge 1.20.1).

It automates repetitive farming, fishing, and artisan tasks using simulated right-clicks without left-clicking, maintaining an organic interaction model.

---

## 🌟 Features

### 🎣 Auto Fishing
* **Casting & Reeling:** Automates the complete cast-and-reel loop.
* **Bite Detection:** Supports standard audio bite cues and direct nibble state tracking (requires the *Stardew Fishing* mod).
* **Minigame Automation:** Automatically plays and completes the fishing minigame.
* **Treasure Chasing:** Intelligently reels in treasure chests when safe to do so.
* **Low Durability Protection:** Automatically stops fishing or switches rods when durability is critical.
* **Anti-Detection:** Slight randomized delays to emulate human action.

### 🚜 Auto Harvesting
* **Crops & Fruits:** Right-clicks mature crops and tree fruits in a specified scan radius.
* **Supported Mods:** Fully compatible with *Farmer's Delight*, *Farm & Charm*, *Pam's HC2 Trees*, and *Vinery*.

### ⚡ Force Growth
* **Area Effect:** Automatically forces the growth of all crops in the area (Server-side, singleplayer).
* **On-Click Growth:** Instantly forces a crop's growth when right-clicked.

### 🛠️ Artisan Automation
* **Auto Tapper:** Automatically collects resources from finished tappers (`society:tapper` and `society:tapper_upgraded`).
* **Auto Preserves:** Collects finished preserves from jars (`society:preserves_jar` and `society:preserves_jar_upgraded`). If the player is holding an edible item (crop/fruit), it will automatically insert it into nearby empty jars.
* **Auto Wine:** Collects finished wine from kegs (`society:wine_keg` and `society:wine_keg_upgraded`). If the player is holding an edible item (crop/fruit), it will automatically insert it into nearby empty kegs.

### 📊 On-Screen HUD Overlay
An elegant, dynamic HUD overlay showing active modules, rod durability, session catches, total catches, and pending artisan counts.

---

## ⌨️ Controls

* **`H` (Default):** Open the Mod Configuration Screen.
* **`G` (Default):** Toggle Force Growth.
* **Quick-Enable Keys:** You can configure keybinds directly inside the config screen (**H**) to toggle individual modules (*Fishing, Harvesting, Tapping, Preserves, Wine*) instantly in-game.

---

## ⚙️ Configuration

The mod features a custom tabbed configuration screen where you can adjust:
* Scan Radii (in blocks)
* Cooldowns (in ticks)
* Anti-detection delays
* HUD Positions (Top Left, Top Right, Bottom Left, Bottom Right)
* Individual mod integrations