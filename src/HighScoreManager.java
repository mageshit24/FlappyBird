import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HighScoreManager.java
 *
 * Persists the player's best score between runs.
 *
 * Security / robustness notes (this is the part of the game that
 * touches the file system, so it gets the most scrutiny):
 *
 *  1. Fixed, hard-coded file name (Constants.HIGH_SCORE_FILE_NAME) is
 *     always resolved under the current user's home directory. No path
 *     is ever built from external/untrusted input, so there is no
 *     path-traversal risk.
 *  2. All I/O uses try-with-resources so streams are always closed,
 *     even on exceptions - avoids file-handle leaks.
 *  3. Failures never crash the game and never print raw stack traces
 *     to the player (which could leak local file-system paths).
 *     They're routed to java.util.logging instead, and the game
 *     falls back to an in-memory high score of 0.
 *  4. The stored value is parsed defensively - a corrupted or
 *     hand-edited properties file can't throw an unhandled exception
 *     or inject unexpected types, it just resets to 0.
 */
public final class HighScoreManager {

    private static final Logger LOGGER = Logger.getLogger(HighScoreManager.class.getName());
    private static final String KEY = "highScore";

    private final Path storagePath;
    private int highScore;

    public HighScoreManager() {
        this.storagePath = Paths.get(System.getProperty("user.home"), Constants.HIGH_SCORE_FILE_NAME);
        this.highScore = load();
    }

    public int getHighScore() {
        return highScore;
    }

    /** Updates the in-memory + on-disk high score if the new score beats it. */
    public void reportScore(int score) {
        if (score > highScore) {
            highScore = score;
            save(highScore);
        }
    }

    private int load() {
        if (!Files.exists(storagePath)) {
            return 0;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(storagePath)) {
            props.load(in);
            String raw = props.getProperty(KEY, "0");
            return Math.max(0, Integer.parseInt(raw.trim()));
        } catch (IOException | NumberFormatException e) {
            // Corrupted or unreadable file: don't crash the game, just start fresh.
            LOGGER.log(Level.WARNING, "Could not read high score file, defaulting to 0", e);
            return 0;
        }
    }

    private void save(int score) {
        Properties props = new Properties();
        props.setProperty(KEY, String.valueOf(score));
        try (OutputStream out = Files.newOutputStream(storagePath)) {
            props.store(out, "Flappy Bird high score - auto-generated, safe to delete");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not save high score", e);
        }
    }
}
