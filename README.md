<div align="center">

# 🐦 Flappy Bird - Java Edition

**A polished recreation of the classic Flappy Bird, built from scratch in Java Swing/AWT.**
Flap through a gap in scrolling pipes, chase a persistent high score, and watch the game get harder the longer you survive - all in a lightweight native desktop window.

[![Java](https://img.shields.io/badge/Java-25%20LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](#-getting-started)
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
- [Development](#-development)
- [Running the Feature Tests](#-running-the-feature-tests)
- [Controls](#-controls)
- [How It Works](#-how-it-works)
- [JDK 25 Migration Notes](#-jdk-25-migration-notes)
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
| 🔊 | **Sound effects** - flap, score, hit, and power-up cues, with an `M` mute toggle |
| 🛡️ | **Shield power-up** - absorbs one pipe collision instead of ending the run |
| ⏱️ | **Slow-Mo power-up** - temporarily eases pipe speed for breathing room |
| 🧪 | **Feature test suite** - 37 dependency-free tests covering physics, collisions, power-up rules, persistence, and audio fail-safety |

---

## 🛠️ Tech Stack

<div align="center">

| Component | Technology |
|:---:|:---:|
| Language | Java 25 LTS - module import declarations (JEP 511), flexible constructor bodies (JEP 513), sealed interfaces, records, pattern matching |
| Rendering | Java Swing & AWT (`Graphics2D`) |
| Audio | `javax.sound.sampled` (`Clip`) |
| Game Loop | `javax.swing.Timer` @ 60 FPS |
| Persistence | `java.util.Properties` file, atomic write |
| Testing | Custom dependency-free harness (`test/TestRunner.java`) - 37 tests |
| Packaging | Executable `.jar` |
| IDE Project | IntelliJ IDEA (`.iml`, language level 25, bundled run configurations) |

</div>

---

## 📦 Project Structure

```
FlappyBird/
├── resources/
│   └── assets/                # Source-of-truth image + sound assets
├── src/
│   ├── App.java                # Entry point — builds the window, starts the game safely
│   ├── FlappyBird.java          # Game panel: state machine, physics, rendering, input
│   ├── Bird.java                 # Encapsulated bird entity (position, velocity, tilt)
│   ├── Pipe.java                  # Encapsulated pipe entity (position, collision bounds)
│   ├── PowerUp.java                # Collectible power-up entity on the board
│   ├── PowerUpKind.java              # SHIELD / SLOW_MO pickup kind
│   ├── PowerUpEffect.java             # Sealed interface + records for active timed effects
│   ├── ActiveEffects.java              # Shield/slow-mo activation, expiry & consumption rules
│   ├── Difficulty.java                  # Pure pipe speed/gap scaling calculation
│   ├── SoundManager.java                 # Loads & plays sound effects, mute-safe, device-safe
│   ├── Constants.java                     # All tunable game constants in one place
│   ├── HighScoreManager.java                # Safe, sandboxed, atomic high-score persistence
│   └── assets/                               # Bundled assets for the compiled build
├── test/
│   ├── TestRunner.java          # Test harness entry point + assertion helpers
│   ├── BirdTest.java             # Physics/state feature tests
│   ├── PipeTest.java              # Movement/collision feature tests
│   ├── PowerUpTest.java            # Collectible entity feature tests
│   ├── ActiveEffectsTest.java        # Shield/slow-mo rule feature tests
│   ├── DifficultyTest.java            # Difficulty curve feature tests
│   ├── HighScoreManagerTest.java       # Persistence feature tests (sandboxed temp dir)
│   └── SoundManagerTest.java            # Audio fail-safety feature tests
├── dev/
│   └── logging.properties      # Optional dev-time logging config (see Development)
├── .idea/runConfigurations/    # Bundled IntelliJ run configs: "App (Game)", "Feature Tests"
├── index.html                 # GitHub Pages landing page
├── FlappyBird.iml              # IntelliJ IDEA module file
└── FlappyBird.jar               # Compiled, ready-to-run executable
```

> Each class has a single, clear responsibility, and every field is access-controlled - see [Code Quality & Security](#-code-quality--security).

---

## 🚀 Getting Started

### Requirements
- ☕ **Java JDK 25** (required - this project uses JDK 25-exclusive syntax, see [JDK 25 Migration Notes](#-jdk-25-migration-notes); it will not compile on 21/24)
- 🚫 No external libraries - pure Java Swing, AWT & `javax.sound.sampled`
- Java must be on your system `PATH`

### ▶️ Option 1 - Run the prebuilt JAR
```bash
java -jar FlappyBird.jar
```

### 🔧 Option 2 — Compile and run from source
```bash
cd src
javac --release 25 *.java
java App
```

### 🖱️ Option 3 — Open in IntelliJ IDEA
Open the project folder directly (it already has `.idea`/`.iml` files at language level 25). Two run configurations are bundled and ready to use from the dropdown: **App (Game)** and **Feature Tests**.

The game window opens on the start screen - press `SPACE` to begin.

---

## 🧰 Development

Two dev-oriented conveniences are bundled, both optional:

**`dev/logging.properties`** - a `java.util.logging` config that formats warnings as clean single lines instead of the JDK's default two-line format. Useful when watching `SoundManager`/`HighScoreManager` fall back gracefully (e.g. testing on a machine with no audio device). Wire it in with:
```bash
java -Djava.util.logging.config.file=dev/logging.properties -jar FlappyBird.jar
```

**`-ea`** (enable assertions) is safe to add during development - the codebase doesn't rely on `assert` for control flow, only as an optional defensive backstop, so running with or without it never changes behavior.

Both are already set as VM options on the bundled **App (Game)** IntelliJ run configuration, so opening the project and hitting Run picks them up automatically.

### Recommended dev build flags
```bash
javac --release 25 -Xlint:all -Werror -encoding UTF-8 -d out src/*.java
```
`-Werror` turns every lint warning into a build failure - useful to catch regressions early, since the shipped build is warning-free (`-Xlint:all` currently reports zero warnings).

---

## 🧪 Running the Feature Tests

The `test/` directory holds a small, dependency-free test harness (`TestRunner` + 7 test classes, 37 tests) covering bird physics, pipe/power-up collision math, the shield/slow-mo effect rules (`ActiveEffects`), the difficulty curve (`Difficulty`), high-score persistence, and `SoundManager`'s audio fail-safety. No JUnit or build tool required - just `javac`/`java`, in keeping with the project's "no external libraries" design.

```bash
# From the project root
javac --release 25 -d out-test src/*.java test/*.java
cp -r resources/assets out-test/
java -cp out-test TestRunner
```

Expected output ends with something like:
```
-------------------------------------------------
37 passed, 0 failed, 37 total
```

`HighScoreManagerTest` never touches your real save file - it temporarily redirects `user.home` to a throwaway temp directory for the duration of each test, then restores it. `SoundManagerTest` only asserts that construction and playback never throw, on any machine with or without audio hardware - it doesn't (and can't) verify a sound is actually audible.

From IntelliJ, just run the bundled **Feature Tests** configuration instead.

---

## 🎮 Controls

<div align="center">

| Key | Action |
|:---:|---|
| `SPACE` | Start the game / Flap / Restart after Game Over |
| `P` | Pause / Resume |
| `M` | Mute / Unmute sound effects |

</div>

---

## 🧠 How It Works

- A `Timer`-driven game loop updates physics and repaints the canvas 60 times per second.
- An explicit `GameState` (`START`, `PLAYING`, `PAUSED`, `GAME_OVER`) drives input and rendering, instead of one ambiguous boolean flag.
- Gravity continuously pulls the bird down; `SPACE` gives it an upward flap impulse, and the sprite tilts based on vertical velocity.
- A second timer spawns a new pipe pair every 1.5 seconds at a randomized gap - while `PLAYING` only.
- Every `DIFFICULTY_STEP` points scored, pipe speed increases and the gap shrinks slightly, capped so the game never becomes unfair - computed by the pure, unit-tested `Difficulty.forScore(score)`.
- Rectangle-based collision checks run each frame between the bird and every active pipe.
- On Game Over, the score is compared against the saved high score, and a new best is written to disk immediately.
- A third timer spawns a Shield or Slow-Mo pickup at an irregular interval (8-14s). Colliding with one activates a timed effect, modeled as a sealed `PowerUpEffect` (`Shield` / `SlowMo` records) and owned by `ActiveEffects` - a small class dedicated to activation/expiry/consumption rules, pulled out of the game loop specifically so it's testable on its own (see [Running the Feature Tests](#-running-the-feature-tests)):
  - **Shield** absorbs the next pipe collision instead of ending the run, then breaks (with a short invulnerability window so the same pipe can't immediately re-trigger it).
  - **Slow-Mo** halves effective pipe speed for a few seconds.
- `SoundManager` plays short cues for flap/score/hit/pickup/shield-break via `javax.sound.sampled`, and degrades to silent no-ops if no audio device is available - it never blocks or crashes gameplay.

---

## ☕ JDK 25 Migration Notes

This project targets JDK 25 specifically, not just "compiles fine on a recent JDK" - it uses two syntax features that are exclusive to 25 and simply won't compile on 21 or 24:

- **Module import declarations ([JEP 511](https://openjdk.org/jeps/511)), finalized in JDK 25.** Every file uses `import module java.desktop;` / `import module java.base;` / `import module java.logging;` instead of listing individual types. Where two modules export a same-named type - `java.util.Timer` vs `javax.swing.Timer`, and `java.util.List` vs the legacy AWT `java.awt.List` component - a single-type import disambiguates, exactly as the JEP intends. (These collisions aren't hypothetical: the real `javac 25` compiler rejected the first draft of this migration until they were resolved.)
- **Flexible constructor bodies ([JEP 513](https://openjdk.org/jeps/513)), finalized in JDK 25.** `FlappyBird`'s and `Bird`'s constructors now run validation (board-size sanity check, null-image check) *before* their `super()` call - a compile error on any earlier JDK, where the superclass call had to be the very first statement no matter what.
- **Unnamed pattern variables (`_`)** are used where a `switch` pattern's binding is never read (`case PowerUpEffect.Shield _ -> ...`). Finalized in JDK 22, so not 25-exclusive on its own, but part of keeping the pattern-matching code idiomatic for the target version.

Deliberately **not** used: JDK 25's preview features (e.g. primitive types in patterns, structured concurrency). Preview features require `--enable-preview` on every build and run, and their APIs can still change before finalization - a bad trade for a shipped, downloadable game. If a preview feature graduates to stable in a future JDK, it's a natural candidate to revisit.

Verification: every claim above was checked by compiling and running against an actual installed `javac`/`java` 25.0.3 - not just written to look 25-compatible and assumed correct.

---

<table>
<tr><td>🔐</td><td><b>Encapsulation</b> - every entity field (<code>Bird</code>, <code>Pipe</code>, game state) is <code>private</code>. All changes go through methods that can validate/clamp values.</td></tr>
<tr><td>🧱</td><td><b>Centralized config</b> - <code>Constants.java</code> holds every tunable value as <code>public static final</code>, with a private constructor. No magic numbers scattered through game logic.</td></tr>
<tr><td>🖼️</td><td><b>Safe asset loading</b> - missing or corrupt images fail with a clear, specific error instead of an unhandled <code>NullPointerException</code>.</td></tr>
<tr><td>🚫</td><td><b>No leaked stack traces</b> - startup failures are caught, logged, and shown as a clean dialog instead of a raw console trace.</td></tr>
<tr><td>🗂️</td><td><b>Sandboxed, atomic file I/O</b> - <code>HighScoreManager</code> only reads/writes one fixed filename in the user's home directory. No path-traversal exposure; saves go through a temp file + atomic move so a crash mid-write can never corrupt the score file; a corrupted or hand-edited file safely resets to 0.</td></tr>
<tr><td>🔢</td><td><b>Clamped persistence</b> - any score written to disk is clamped to a sane maximum, so a future scoring bug can't persist an absurd or corrupt value.</td></tr>
<tr><td>🔏</td><td><b>Owner-only permissions</b> - on POSIX file systems the high-score file is written with <code>rw-------</code> so other local users can't read or tamper with it (skipped safely on Windows, which has no POSIX permission model).</td></tr>
<tr><td>🔊</td><td><b>Fail-safe audio</b> - <code>SoundManager</code> loads every clip from a fixed classpath resource only, and catches every failure mode of <code>javax.sound.sampled</code> (missing asset, unsupported format, no audio device/line) so a machine with no sound hardware still runs the full game, just silently.</td></tr>
<tr><td>🧵</td><td><b>Correct threading</b> - the Swing UI is built on the Event Dispatch Thread via <code>SwingUtilities.invokeLater</code>.</td></tr>
<tr><td>🧩</td><td><b>Exhaustive effect handling</b> - active power-up effects are a sealed <code>PowerUpEffect</code> interface (<code>Shield</code> / <code>SlowMo</code> records); every <code>switch</code> over an effect is compiler-checked, so adding a new effect kind without updating every consumer fails the build instead of misbehaving at runtime.</td></tr>
<tr><td>🧪</td><td><b>Testable-by-design core logic</b> - <code>Difficulty</code> and <code>ActiveEffects</code> are pure/Swing-free on purpose, so the rules behind difficulty scaling and shield/slow-mo behavior can be exercised directly by the 37-test feature suite without a running game window.</td></tr>
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

- [ ] 🖼️ Animated bird sprite (wing-flap frames)
- [ ] 🌐 Web-playable version (Java-to-WASM or Canvas/JS port)
- [ ] ⚙️ In-game settings screen (difficulty presets, volume slider)
- [ ] 🏅 Local leaderboard (top N scores, not just a single best)

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
