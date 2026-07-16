/**
 * Constants.java
 *
 * Centralized, read-only configuration for the entire game.
 *
 * Why this class exists (encapsulation / "variable visibility" fix):
 * The original code scattered magic numbers (board size, gravity, pipe
 * speed, etc.) across FlappyBird.java as loose package-private fields.
 * That made every value mutable-by-accident from anywhere in the class
 * and gave no single place to tune the game.
 *
 * All values here are `public static final` (true constants, safe to
 * expose) and the constructor is private so the class can never be
 * instantiated. Nothing here can be reassigned at runtime, which removes
 * an entire class of accidental-state bugs.
 */
public final class Constants {

    // Prevent instantiation - this is a static-only utility class.
    private Constants() {
        throw new AssertionError("Constants is not meant to be instantiated");
    }

    // ---- Board ----
    public static final int BOARD_WIDTH = 360;
    public static final int BOARD_HEIGHT = 640;

    // ---- Bird ----
    public static final int BIRD_START_X = BOARD_WIDTH / 8;
    public static final int BIRD_START_Y = BOARD_HEIGHT / 2;
    public static final int BIRD_WIDTH = 34;
    public static final int BIRD_HEIGHT = 24;
    public static final int FLAP_VELOCITY = -9;
    public static final int GRAVITY = 1;
    public static final double MAX_FALL_SPEED = 14;
    public static final double MAX_TILT_DEGREES = 25;

    // ---- Pipes ----
    public static final int PIPE_WIDTH = 64;
    public static final int PIPE_HEIGHT = 512;
    public static final int BASE_PIPE_SPEED = 4;
    public static final int MAX_PIPE_SPEED = 9;
    public static final int BASE_PIPE_GAP = BOARD_HEIGHT / 4;
    public static final int MIN_PIPE_GAP = 140;
    public static final int PIPE_SPAWN_INTERVAL_MS = 1500;

    // ---- Difficulty scaling ----
    // Every DIFFICULTY_STEP points scored, speed nudges up and the gap
    // narrows slightly, until the MAX/MIN bounds above are reached.
    public static final int DIFFICULTY_STEP = 5;
    public static final int GAP_SHRINK_PER_STEP = 6;

    // ---- Ground ----
    public static final int GROUND_HEIGHT = 24;

    // ---- Frame rate ----
    public static final int FPS = 60;

    // ---- Persistence ----
    // File lives in the user's own home directory only - never a path
    // supplied by external/untrusted input (see HighScoreManager).
    public static final String HIGH_SCORE_FILE_NAME = ".flappybird_highscore.properties";

    // ---- Asset paths (classpath-relative, bundled inside the jar) ----
    public static final String BACKGROUND_IMG = "/assets/Background.png";
    public static final String BIRD_IMG = "/assets/Bird.png";
    public static final String TOP_PIPE_IMG = "/assets/TopPipe.png";
    public static final String BOTTOM_PIPE_IMG = "/assets/BottomPipe.png";
}
