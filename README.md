# 🐦 Flappy Bird — Java Edition

A faithful recreation of the classic **Flappy Bird** game, built from scratch in **Java Swing/AWT**. Flap through gaps in scrolling pipes, rack up your score, and try to beat your own high score — all running in a lightweight native desktop window.

🔗 **[Play the landing page / demo info](https://mageshit24.github.io/FlappyBird/)**

---

## ✨ Features

- 🕹️ Smooth physics-based bird movement (gravity + flap velocity)
- 🚧 Procedurally generated, randomly-spaced pipe pairs
- 💥 Pixel-accurate collision detection
- 🧮 Live score tracking (+0.5 per pipe passed)
- 🔁 Instant restart on Game Over — no need to relaunch
- 🖼️ Custom sprite assets for bird, pipes, and background
- ⚡ Runs at a smooth 60 FPS game loop
- 📦 Ships as a standalone runnable `.jar` — no installation needed

---

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Java |
| Rendering / UI | Java Swing & AWT |
| Game Loop | `javax.swing.Timer` (60 FPS) |
| Packaging | Executable `.jar` |
| IDE Project | IntelliJ IDEA (`.iml`) |

---

## 📦 Project Structure

```
FlappyBird/
├── resources/
│   └── assets/              # Image assets used in the game (bird, pipes, background)
├── src/
│   ├── App.java             # Entry point — creates the game window
│   ├── FlappyBird.java      # Core game logic, physics, rendering, collision
│   └── assets/              # Bundled assets for the compiled build
├── index.html                # GitHub Pages landing page for the project
├── FlappyBird.iml            # IntelliJ IDEA module file
└── FlappyBird.jar            # Compiled, ready-to-run executable
```

---

## 🚀 Getting Started

### Requirements
- Java JDK 17+ (LTS recommended) or newer — developed and tested on JDK 24
- No external libraries needed — pure Java Swing & AWT
- Java must be available on your system `PATH`

### Option 1 — Run the prebuilt JAR
```bash
java -jar FlappyBird.jar
```

### Option 2 — Compile and run from source
```bash
cd src
javac App.java
java App
```

The game window will open immediately — no build tools required.

---

## 🎮 Controls

| Key | Action |
|---|---|
| `SPACE` | Flap / Jump |
| `SPACE` (after Game Over) | Restart the game |

---

## 🧠 How It Works

- A `Timer`-driven game loop updates physics and repaints the canvas 60 times per second.
- Gravity continuously pulls the bird down; pressing `SPACE` gives it an upward velocity boost.
- A second timer spawns a new top/bottom pipe pair every 1.5 seconds at a randomized vertical gap.
- Each frame checks for rectangle-based collision between the bird and active pipes, and ends the game on contact or if the bird falls off-screen.
- Score increases by 0.5 each time the bird's x-position clears a pipe.

---

## 📸 Screenshots

<img width="338" alt="Flappy Bird gameplay" src="https://github.com/user-attachments/assets/7cc597be-dd3c-41dd-996f-0843d1d3a552" />
<img width="338" alt="Flappy Bird gameplay - pipes" src="https://github.com/user-attachments/assets/7cfaf854-57ff-4f9b-81eb-fb3b89bb27ff" />
<img width="338" alt="Flappy Bird gameplay - scoring" src="https://github.com/user-attachments/assets/7c36215c-0998-4613-a45d-9fd719624c21" />
<img width="338" alt="Flappy Bird gameplay - game over" src="https://github.com/user-attachments/assets/aabcdc5b-a4b0-435b-97e5-cf524fb119d4" />
<img width="338" alt="Flappy Bird gameplay - restart" src="https://github.com/user-attachments/assets/9aaa8a68-99dc-4e40-9279-55002e8b9b75" />

---

## 🔮 Future Improvements

- 🏆 Persistent high-score tracking (local file or simple save state)
- 🔊 Sound effects for flap, score, and collision
- 🎚️ Difficulty scaling (pipe speed/gap shrinks as score increases)
- 🖥️ Pause menu and start screen
- 🌐 Web-playable version (e.g. via a Java-to-WASM or Canvas/JS port)

---

## 👤 Contact

**Magesh Hariram K**
🔗 [LinkedIn](https://www.linkedin.com/in/magesh-hariram-k-6011132a4)
💻 [GitHub](https://github.com/mageshit24)

---

## 📄 License

This project is open source — feel free to use, modify, and build on it. Consider adding a `LICENSE` file (e.g. MIT) to make the terms explicit.
