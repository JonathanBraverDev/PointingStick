# PointingStick

A lightweight Minecraft plugin that allows players to point out locations to others using a magical "Pointing Stick".

## ✨ Features

- **The Pointing Stick**: A dedicated tool (`/pointingstick`) used to mark locations.
- **Visual Feedback**: highlights the exact face of the block you're looking at with vibrant particle effects.
- **Proximity Audio**: Plays a sound at the pinged location so nearby players are alerted.
- **Anti-Duplication**: Smart systems to prevent stick clutter:
    - Only one stick per player.
    - Dropped sticks are tagged; they can only be picked up by the owner and are destroyed if the owner already has a stick.
    - Previous dropped sticks are automatically removed when getting a new one.
- **Cooldowns**: Built-in 2-second cooldown to prevent ping spam.

## 🛠️ Commands

- `/pointingstick`: Gives you the Pointing Stick tool.

## 🔐 Permissions

- `pointingstick.use`: Allows use of the command and the tool. (Active for everyone by default).

## ⚠️ Limitations

- **Range**: Pings are effective up to **50 blocks**.
- **Chunk Loading**: Tracking of dropped sticks relies on the chunk being loaded.

## 📄 License

This project is licensed under the **GNU GPL v3** - see the [LICENSE](LICENSE) file for details.

---
*Created for the community by Jonathan Braver :D*
