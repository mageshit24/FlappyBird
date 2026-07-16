# 🐦 Flappy Bird — Java Edition

A polished recreation of the classic **Flappy Bird**, built from scratch in **Java Swing/AWT**. Flap through a gap in scrolling pipes, chase a persistent high score, and watch the game get harder the longer you survive — all in a lightweight native desktop window.

🔗 **[Landing page / demo info](https://mageshit24.github.io/FlappyBird/)**

---

## ✨ Features

- 🕹️ Smooth, gravity-based bird physics with a subtle tilt animation
- 🚧 Procedurally generated pipe pairs with randomized gaps
- 📈 **Progressive difficulty** — pipes gradually speed up and the gap narrows as your score climbs
- 🏆 **Persistent high score** — saved to a small file in your home directory and shown on the HUD
- ▶️ **Start screen** and **pause menu** (`P` key) — no more being dropped straight into gameplay
- 💥 Pixel-accurate collision detection
- ☁️ Drawn parallax clouds and a ground strip for extra visual depth
- 🎨 Redesigned HUD: score panel, "Best" tracker, and clean game-over/pause overlays
- 🔁 Instant restart on Game Over — no need to relaunch
- ⚡ Runs at a smooth 60 FPS game loop
- 📦 Ships as a standalone runnable `.jar` — no installation needed

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java (targets JDK 17+, tested on JDK 21/24) |
| Rendering / UI | Java Swing & AWT (`Graphics2D`) |
| Game Loop | `javax.swing.Timer` (60 FPS) |
| Persistence | `java.util.Properties` file in the user's home directory |
| Packaging | Executable `.jar` |
| IDE Project | IntelliJ IDEA (`.iml`) |

---

## 📦 Project Structure

```
FlappyBird/
├── resources/
│   └── assets/              # Source-of-truth image assets (bird, pipes, background)
├── src/
│   ├── App.java              # Entry point — builds the window, starts the game safely
│   ├── FlappyBird.java        # Game panel: state machine, physics, rendering, input
│   ├── Bird.java               # Encapsulated bird entity (position, velocity, tilt)
│   ├── Pipe.java                # Encapsulated pipe entity (position, collision bounds)
│   ├── Constants.java            # All tunable game constants in one place
│   ├── HighScoreManager.java      # Safe, sandboxed high-score persistence
│   └── assets/                     # Bundled assets for the compiled build (classpath)
├── index.html                # GitHub Pages landing page for the project
├── FlappyBird.iml            # IntelliJ IDEA module file
└── FlappyBird.jar            # Compiled, ready-to-run executable
```

Each class now has a single, clear responsibility instead of one file owning everything, and every field is access-controlled (see **Code Quality & Security** below).

---

## 🚀 Getting Started

### Requirements
- Java JDK 17+ (LTS recommended) — developed and tested on JDK 21 and JDK 24
- No external libraries needed — pure Java Swing & AWT
- Java must be available on your system `PATH`

### Option 1 — Run the prebuilt JAR
```bash
java -jar FlappyBird.jar
```

### Option 2 — Compile and run from source
```bash
cd src
javac *.java
java App
```

The game window opens on the start screen — press `SPACE` to begin.

---

## 🎮 Controls

| Key | Action |
|---|---|
| `SPACE` | Start the game / Flap / Restart after Game Over |
| `P` | Pause / Resume |

---

## 🧠 How It Works

- A `Timer`-driven game loop updates physics and repaints the canvas 60 times per second.
- An explicit `GameState` (`START`, `PLAYING`, `PAUSED`, `GAME_OVER`) drives input handling and rendering, instead of a single ambiguous boolean flag.
- Gravity continuously pulls the bird down; `SPACE` gives it an upward flap impulse, and the sprite tilts based on vertical velocity.
- A second timer spawns a new top/bottom pipe pair every 1.5 seconds at a randomized vertical gap — while `PLAYING` only.
- Every `DIFFICULTY_STEP` points scored, pipe speed increases and the gap shrinks slightly, up to a capped maximum/minimum so the game never becomes unfair.
- Rectangle-based collision checks run each frame between the bird and every active pipe.
- On Game Over, the score is compared against the saved high score; a new best is written to disk immediately.

---

## 🔒 Code Quality & Security

A few things were tightened up compared to the original version:

- **Encapsulation / controlled visibility** — every entity field (`Bird`, `Pipe`, game state in `FlappyBird`) is `private`. External code can no longer reach in and mutate position, velocity, or score directly; all changes go through methods that can validate/clamp values.
- **Centralized, immutable configuration** — `Constants.java` holds every tunable value as `public static final`, with a private constructor so the class can never be instantiated. No more magic numbers scattered across game logic.
- **Safe asset loading** — missing or corrupt image assets now fail with a clear, specific error instead of an unhandled `NullPointerException` deep inside Swing internals.
- **No leaked stack traces** — `App.java` no longer declares `throws Exception` on `main`. Startup failures are caught, logged via `java.util.logging`, and shown to the player as a clean dialog — internal paths and class names are never printed to a raw console trace.
- **Sandboxed file I/O** — `HighScoreManager` only ever reads/writes a single, hard-coded file name inside the current user's home directory. No file path is ever built from external input, so there's no path-traversal exposure. All I/O uses try-with-resources so file handles are always released, and a corrupted save file safely resets to 0 instead of crashing the game.
- **Correct threading** — the Swing UI is now built on the Event Dispatch Thread via `SwingUtilities.invokeLater`, which the original entry point skipped.

---

## 🔮 Future Improvements

- 🔊 Sound effects for flap, score, and collision
- 🖼️ Animated bird sprite (wing-flap frames) instead of a static image
- 🌐 Web-playable version (e.g. via a Java-to-WASM or Canvas/JS port)
- ⚙️ In-game settings screen (difficulty presets, mute toggle)

---

## 👤 Contact

**Magesh Hariram K**
🔗 [LinkedIn](https://www.linkedin.com/in/magesh-hariram-k-6011132a4)
💻 [GitHub](https://github.com/mageshit24)

---

## 📄 License

This project is open source — feel free to use, modify, and build on it. Consider adding a `LICENSE` file (e.g. MIT) to make the terms explicit.

**Screenshots:**

<img width="453" height="846" alt="image" src="https://github.com/user-attachments/assets/0e62a44d-5289-4e78-b330-87188a48d4a1" />
<img width="450" height="837" alt="image" src="https://github.com/user-attachments/assets/4632e53f-8a4a-4c9f-aaa7-fc27a8551423" />
<img width="457" height="840" alt="image" src="https://github.com/user-attachments/assets/bca624cb-065c-42c5-aac9-dd5c4a82a61a" />
<img width="447" height="842" alt="image" src="https://github.com/user-attachments/assets/e168e35b-bf60-4d53-a9a2-0ce476656ace" />


