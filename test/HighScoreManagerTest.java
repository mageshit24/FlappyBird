
import module java.base;

/**
 * HighScoreManagerTest.java — feature tests for score persistence.
 *
 * Every test runs against a throwaway temp directory (by temporarily overriding
 * the "user.home" system property HighScoreManager reads at construction time)
 * so these tests can never read, overwrite, or corrupt the real player's saved
 * high score.
 */
final class HighScoreManagerTest {

    private HighScoreManagerTest() {
    }

    /**
     * Runs {@code body} with "user.home" pointed at a fresh temp directory,
     * then restores it.
     */
    private static void withTempHome(Runnable body) {
        String originalHome = System.getProperty("user.home");
        try {
            Path tempDir = Files.createTempDirectory("flappybird-test-home");
            System.setProperty("user.home", tempDir.toString());
            body.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            System.setProperty("user.home", originalHome);
        }
    }

    static void freshInstallStartsAtZero() {
        withTempHome(() -> {
            HighScoreManager manager = new HighScoreManager();
            TestRunner.assertEquals(0, manager.getHighScore(), "a fresh install should start at 0");
        });
    }

    static void higherScorePersists() {
        withTempHome(() -> {
            HighScoreManager first = new HighScoreManager();
            first.reportScore(42);
            HighScoreManager second = new HighScoreManager(); // simulates relaunching the game
            TestRunner.assertEquals(42, second.getHighScore(), "a higher score should persist across instances");
        });
    }

    static void lowerScoreIsIgnored() {
        withTempHome(() -> {
            HighScoreManager manager = new HighScoreManager();
            manager.reportScore(50);
            manager.reportScore(10);
            TestRunner.assertEquals(50, manager.getHighScore(), "a lower score should never overwrite a higher one");
        });
    }

    static void scoreIsClamped() {
        withTempHome(() -> {
            HighScoreManager manager = new HighScoreManager();
            manager.reportScore(Constants.MAX_PERSISTABLE_SCORE + 999);
            TestRunner.assertEquals(Constants.MAX_PERSISTABLE_SCORE, manager.getHighScore(),
                    "a score above the max should be clamped down to MAX_PERSISTABLE_SCORE");
        });
    }

    static void corruptedFileResetsToZero() {
        withTempHome(() -> {
            try {
                Path scoreFile = Path.of(System.getProperty("user.home"), Constants.HIGH_SCORE_FILE_NAME);
                Files.writeString(scoreFile, "highScore=not-a-number\n");
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            HighScoreManager manager = new HighScoreManager();
            TestRunner.assertEquals(0, manager.getHighScore(),
                    "a corrupted/unparseable save file should reset to 0 instead of throwing");
        });
    }
}
