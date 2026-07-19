
import module java.base;
import module java.logging;

/**
 * HighScoreManager.java
 *
 * Persists the player's best score between runs.
 *
 * Security / robustness notes (this is the part of the game that touches the
 * file system, so it gets the most scrutiny):
 *
 * 1. Fixed, hard-coded file name (Constants.HIGH_SCORE_FILE_NAME) is always
 * resolved under the current user's home directory. No path is ever built from
 * external/untrusted input, so there is no path-traversal risk. 2. All I/O uses
 * try-with-resources so streams are always closed, even on exceptions - avoids
 * file-handle leaks. 3. Failures never crash the game and never print raw stack
 * traces to the player (which could leak local file-system paths). They're
 * routed to java.util.logging instead, and the game falls back to an in-memory
 * high score of 0. 4. The stored value is parsed defensively and clamped to [0,
 * Constants.MAX_PERSISTABLE_SCORE] - a corrupted or hand-edited properties file
 * can't throw an unhandled exception, inject unexpected types, or persist an
 * absurd out-of-range value. 5. Saves are atomic: the new value is written to a
 * sibling temp file, then moved into place with ATOMIC_MOVE. A crash or power
 * loss mid-write can never leave a half-written, corrupted score file behind -
 * readers only ever see the old or the new value. 6. On POSIX file systems the
 * score file is created with owner-only permissions (rw-------), so other local
 * users on a shared machine can't read or tamper with it. This is a best-effort
 * hardening step, not a hard requirement - Windows (this project's primary
 * target) has no POSIX permission model, so the call is skipped there rather
 * than failing.
 */
public final class HighScoreManager {

    private static final Logger LOGGER = Logger.getLogger(HighScoreManager.class.getName());
    private static final String KEY = "highScore";
    private static final Set<PosixFilePermission> OWNER_ONLY = PosixFilePermissions.fromString("rw-------");

    private final Path storagePath;
    private int highScore;

    public HighScoreManager() {
        this.storagePath = Paths.get(System.getProperty("user.home"), Constants.HIGH_SCORE_FILE_NAME);
        this.highScore = load();
    }

    public int getHighScore() {
        return highScore;
    }

    /**
     * Updates the in-memory + on-disk high score if the new score beats it.
     */
    public void reportScore(int score) {
        int clamped = Math.clamp(score, 0, Constants.MAX_PERSISTABLE_SCORE);
        if (clamped > highScore) {
            highScore = clamped;
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
            int parsed = Integer.parseInt(raw.trim());
            return Math.clamp(parsed, 0, Constants.MAX_PERSISTABLE_SCORE);
        } catch (IOException | NumberFormatException e) {
            // Corrupted or unreadable file: don't crash the game, just start fresh.
            LOGGER.log(Level.WARNING, "Could not read high score file, defaulting to 0", e);
            return 0;
        }
    }

    private void save(int score) {
        Path tempFile = storagePath.resolveSibling(storagePath.getFileName() + ".tmp");
        Properties props = new Properties();
        props.setProperty(KEY, String.valueOf(score));
        try (OutputStream out = Files.newOutputStream(tempFile)) {
            props.store(out, "Flappy Bird high score - auto-generated, safe to delete");
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not save high score", e);
            return;
        }
        restrictToOwnerIfSupported(tempFile);
        try {
            Files.move(tempFile, storagePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not finalize high score save", e);
        }
    }

    private void restrictToOwnerIfSupported(Path path) {
        if (!path.getFileSystem().supportedFileAttributeViews().contains("posix")) {
            return; // e.g. Windows: no POSIX permission model, nothing to do.
        }
        try {
            Files.setPosixFilePermissions(path, OWNER_ONLY);
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not restrict high score file permissions", e);
        }
    }
}
