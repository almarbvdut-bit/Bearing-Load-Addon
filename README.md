
# Bearing Load Addon

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-2ea043?style=for-the-badge)](https://www.minecraft.net/)
[![NeoForge](https://img.shields.io/badge/NeoForge-21.1.x-orange?style=for-the-badge)](https://neoforged.net/)
[![Create](https://img.shields.io/badge/Create-6.0.10-blue?style=for-the-badge)](https://modrinth.com/mod/create)
[![Create: Aeronautics](https://img.shields.io/badge/Create%3A%20Aeronautics-1.3.2-blueviolet?style=for-the-badge)](https://github.com/)

</div>

---

## ⚙️ How It Works

**Bearing Load Addon** modifies the bearing stress calculation logic when used with **Create: Aeronautics**.

The addon modifies the existing mechanics while leaving rotation control and other processes to the original mods.

### 📐 Stress Calculation Formula

**Total Stress = Original Stress + Mass × Speed × K**

Where:

- **Original Stress** — the bearing's original stress value.
- **Mass** — the mass of the rotating structure.
- **Speed** — the structure's speed.
- **K** — a multiplier that determines how much mass and speed affect the total stress.

### 🔧 Key Features

- **Modified stress calculation** — adjusts the logic responsible for bearing stress.
- **Create: Aeronautics integration** — designed to work with the relevant mechanics of the addon.
- **Extension of the existing system** — extends the game's behavior without replacing the core mechanics of Create or Create: Aeronautics.

## 📦 Requirements

| Component | Exact Version |
|---|---|
| Minecraft | `1.21.1` |
| NeoForge | `21.1.x` |
| Create | `6.0.10` |
| Sable | `2.0.5` |
| Create: Aeronautics | `1.3.2` |

Use the specified dependency versions. Compatibility with other versions is not guaranteed.

## ⚙️ Configuration

**Configuration path:**

`Esc → Create Settings (goggles icon) → Other Mods → Bearing Load Addon → Server Config → Load Multiplier`

## 🚀 Installation

1. Install **Minecraft 1.21.1** and **NeoForge 21.1.x**.
2. Install **Create 6.0.10**, **Sable 2.0.5**, and **Create: Aeronautics 1.3.2**.
3. Download the addon's `.jar` file from the **Releases** section, if a release is available.
4. Place the `.jar` file in the `mods` folder of your Minecraft instance.
5. Launch the game. If an error occurs, check the game log.

If no prebuilt release is available, you can build the addon from source.

## 🛠️ Building from Source

You will need Git and a compatible version of the JDK.

```bash
git clone https://github.com/almarbvdut-bit/Bearing-Load-Addon.git
cd Bearing-Load-Addon
```

**Windows:**

```bat
gradlew.bat build
```

**Linux / macOS:**

```bash
./gradlew build
```

After a successful build, the mod file will be available in the `build/libs/` directory.

## 🧪 Testing

For testing, use the specified versions of Minecraft, NeoForge, Create, Sable, and Create: Aeronautics. Compatibility with other versions and additional mods is not guaranteed.

## 💬 Contact

For questions, bug reports, or suggestions, contact me on Discord: **[@almar_b_v_d](https://discord.com/users/856120201011789854?utm_source=chatgpt.com)**.

## 📄 License

This project is distributed under the [MIT License](LICENSE).
