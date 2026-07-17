<div align="center">

# 🐦 Flappy Bird - Java Edition

**A polished recreation of the classic Flappy Bird, built from scratch in Java Swing/AWT.**
Flap through a gap in scrolling pipes, chase a persistent high score, and watch the game get harder the longer you survive - all in a lightweight native desktop window.

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#-getting-started)
[![Swing](https://img.shields.io/badge/UI-Swing%20%26%20AWT-4C9BE8?style=for-the-badge&logo=java&logoColor=white)](#-tech-stack)
[![Build](https://img.shields.io/badge/Build-Jar-2E8B57?style=for-the-badge&logo=apache-maven&logoColor=white)](#-getting-started)
[![License](https://img.shields.io/badge/License-Open%20Source-informational?style=for-the-badge)](#-license)

🔗 **[Landing page / demo info](https://mageshit24.github.io/FlappyBird/)** • 💻 **[View on GitHub](https://github.com/mageshit24/FlappyBird)**

</div>

---

## 📖 Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Project Structure](#-project-structure)
- [Getting Started](#-getting-started)
- [Controls](#-controls)
- [How It Works](#-how-it-works)
- [Code Quality & Security](#-code-quality--security)
- [Screenshots](#-screenshots)
- [Future Improvements](#-future-improvements)
- [Contact](#-contact)
- [License](#-license)

---

## ✨ Features

| | |
|---|---|
| 🕹️ | Smooth, gravity-based bird physics with a subtle tilt animation |
| 🚧 | Procedurally generated pipe pairs with randomized gaps |
| 📈 | **Progressive difficulty** - pipes speed up and the gap narrows as your score climbs |
| 🏆 | **Persistent high score** - saved locally and shown on the HUD |
| ▶️ | **Start screen** and **pause menu** (`P` key) |
| 💥 | Pixel-accurate collision detection |
| ☁️ | Drawn parallax clouds and a ground strip for extra visual depth |
| 🎨 | Redesigned HUD - score panel, "Best" tracker, clean overlays |
| 🔁 | Instant restart on Game Over - no need to relaunch |
| ⚡ | Runs at a smooth 60 FPS game loop |
| 📦 | Ships as a standalone runnable `.jar` - no installation needed |

---

## 🛠️ Tech Stack

<div align="center">

| Component | Technology |
|:---:|:---:|
| Language | Java (JDK 17+, tested on 21 / 24) |
| Rendering | Java Swing & AWT (`Graphics2D`) |
| Game Loop | `javax.swing.Timer` @ 60 FPS |
| Persistence | `java.util.Properties` file |
| Packaging | Executable `.jar` |
| IDE Project | IntelliJ IDEA (`.iml`) |

</div>

---

## 📦 Project Structure

```
FlappyBird/
├── resources/
│   └── assets/                # Source-of-truth image assets
├── src/
│   ├── App.java                # Entry point — builds the window, starts the game safely
│   ├── FlappyBird.java          # Game panel: state machine, physics, rendering, input
│   ├── Bird.java                 # Encapsulated bird entity (position, velocity, tilt)
│   ├── Pipe.java                  # Encapsulated pipe entity (position, collision bounds)
│   ├── Constants.java              # All tunable game constants in one place
│   ├── HighScoreManager.java        # Safe, sandboxed high-score persistence
│   └── assets/                       # Bundled assets for the compiled build
├── index.html                 # GitHub Pages landing page
├── FlappyBird.iml              # IntelliJ IDEA module file
└── FlappyBird.jar               # Compiled, ready-to-run executable
```

> Each class has a single, clear responsibility, and every field is access-controlled - see [Code Quality & Security](#-code-quality--security).

---

## 🚀 Getting Started

### Requirements
- ☕ Java JDK 17+ (tested on JDK 21 and 24)
- 🚫 No external libraries — pure Java Swing & AWT
- Java must be on your system `PATH`

### ▶️ Option 1 — Run the prebuilt JAR
```bash
java -jar FlappyBird.jar
```

### 🔧 Option 2 — Compile and run from source
```bash
cd src
javac *.java
java App
```

The game window opens on the start screen — press `SPACE` to begin.

---

## 🎮 Controls

<div align="center">

| Key | Action |
|:---:|---|
| `SPACE` | Start the game / Flap / Restart after Game Over |
| `P` | Pause / Resume |

</div>

---

## 🧠 How It Works

- A `Timer`-driven game loop updates physics and repaints the canvas 60 times per second.
- An explicit `GameState` (`START`, `PLAYING`, `PAUSED`, `GAME_OVER`) drives input and rendering, instead of one ambiguous boolean flag.
- Gravity continuously pulls the bird down; `SPACE` gives it an upward flap impulse, and the sprite tilts based on vertical velocity.
- A second timer spawns a new pipe pair every 1.5 seconds at a randomized gap — while `PLAYING` only.
- Every `DIFFICULTY_STEP` points scored, pipe speed increases and the gap shrinks slightly, capped so the game never becomes unfair.
- Rectangle-based collision checks run each frame between the bird and every active pipe.
- On Game Over, the score is compared against the saved high score, and a new best is written to disk immediately.

---

## 🔒 Code Quality & Security

<table>
<tr><td>🔐</td><td><b>Encapsulation</b> - every entity field (<code>Bird</code>, <code>Pipe</code>, game state) is <code>private</code>. All changes go through methods that can validate/clamp values.</td></tr>
<tr><td>🧱</td><td><b>Centralized config</b> - <code>Constants.java</code> holds every tunable value as <code>public static final</code>, with a private constructor. No magic numbers scattered through game logic.</td></tr>
<tr><td>🖼️</td><td><b>Safe asset loading</b> - missing or corrupt images fail with a clear, specific error instead of an unhandled <code>NullPointerException</code>.</td></tr>
<tr><td>🚫</td><td><b>No leaked stack traces</b> - startup failures are caught, logged, and shown as a clean dialog instead of a raw console trace.</td></tr>
<tr><td>🗂️</td><td><b>Sandboxed file I/O</b> - <code>HighScoreManager</code> only reads/writes one fixed filename in the user's home directory. No path-traversal exposure; a corrupted save file safely resets to 0.</td></tr>
<tr><td>🧵</td><td><b>Correct threading</b> - the Swing UI is built on the Event Dispatch Thread via <code>SwingUtilities.invokeLater</code>.</td></tr>
</table>

---

## 📸 Screenshots

<div align="center">
<table>
<tr>
<td><img width="220" alt="Start screen" src="https://github.com/user-attachments/assets/0e62a44d-5289-4e78-b330-87188a48d4a1" /><br><sub>Start Screen</sub></td>
<td><img width="220" alt="Gameplay" src="https://github.com/user-attachments/assets/4632e53f-8a4a-4c9f-aaa7-fc27a8551423" /><br><sub>Gameplay</sub></td>
<td><img width="220" alt="Pause screen" src="https://github.com/user-attachments/assets/bca624cb-065c-42c5-aac9-dd5c4a82a61a" /><br><sub>Pause Screen</sub></td>
<td><img width="220" alt="Game over screen" src="https://github.com/user-attachments/assets/e168e35b-bf60-4d53-a9a2-0ce476656ace" /><br><sub>Game Over</sub></td>
</tr>
</table>
</div>

---

## 🔮 Future Improvements

- [ ] 🔊 Sound effects for flap, score, and collision
- [ ] 🖼️ Animated bird sprite (wing-flap frames)
- [ ] 🌐 Web-playable version (Java-to-WASM or Canvas/JS port)
- [ ] ⚙️ In-game settings screen (difficulty presets, mute toggle)

---

## 👤 Contact

<div align="center">

**Magesh Hariram K**

[![LinkedIn](https://img.shields.io/badge/LinkedIn-Connect-0A66C2?style=for-the-badge&logo=linkedin&logoColor=white)](https://www.linkedin.com/in/magesh-hariram-k-6011132a4)
[![GitHub](https://img.shields.io/badge/GitHub-Follow-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/mageshit24)

</div>

---

## 📄 License

This project is open source - feel free to use, modify, and build on it. Consider adding a `LICENSE` file (e.g. MIT) to make the terms explicit.

<div align="center">

⭐ **If you liked this project, consider giving it a star!** ⭐

</div>
