
import module java.base;

/**
 * TestRunner.java
 *
 * Minimal, dependency-free feature test harness for the game's core logic
 * classes (Bird, Pipe, PowerUp, ActiveEffects, Difficulty, HighScoreManager,
 * SoundManager).
 *
 * Deliberately not JUnit: the project's whole design principle is "no external
 * libraries - pure Java" (see README), so the test suite follows the same rule.
 * This also means anyone who clones the repo can run the tests with nothing but
 * a JDK - no build tool, no dependency download.
 *
 * Usage: see test/README.md or the root README's "Running the tests" section
 * for the compile/run commands.
 */
public final class TestRunner {

    private TestRunner() {
    }

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        System.out.println("Flappy Bird - Feature Test Suite");
        System.out.println("=================================");

        section("Bird");
        run("flap sets an upward (negative) velocity", BirdTest::flapSetsUpwardVelocity);
        run("gravity accumulates velocity over ticks", BirdTest::gravityAccumulates);
        run("gravity never exceeds terminal fall speed", BirdTest::gravityClampsAtMaxFallSpeed);
        run("bird never renders above the top of the board", BirdTest::yNeverGoesNegative);
        run("resetPosition restores the starting state", BirdTest::resetRestoresStartState);
        run("hasFallenBelow triggers only past the given line", BirdTest::hasFallenBelowThreshold);
        run("constructor rejects a null image", BirdTest::rejectsNullImage);

        section("Pipe");
        run("move shifts the pipe left by the given speed", PipeTest::moveShiftsLeft);
        run("isOffScreen once fully past the left edge", PipeTest::offScreenDetection);
        run("isPassedBy triggers once the bird clears the pipe", PipeTest::passedByDetection);
        run("markPassed is idempotent and reflected by isPassed", PipeTest::markPassedTracksState);

        section("PowerUp");
        run("move shifts the power-up left by the given speed", PowerUpTest::moveShiftsLeft);
        run("isOffScreen once fully past the left edge", PowerUpTest::offScreenDetection);
        run("bounds reflect the configured position and size", PowerUpTest::boundsAreAccurate);
        run("markCollected is reflected by isCollected", PowerUpTest::collectionTracksState);

        section("ActiveEffects");
        run("activating a shield makes hasShield true", ActiveEffectsTest::activateShield);
        run("activating slow-mo makes hasSlowMo true", ActiveEffectsTest::activateSlowMo);
        run("re-activating the same kind refreshes instead of stacking", ActiveEffectsTest::refreshNotStack);
        run("expire drops effects whose time has passed", ActiveEffectsTest::expireDropsOldEffects);
        run("expire keeps effects that haven't expired yet", ActiveEffectsTest::expireKeepsFreshEffects);
        run("consumeShield removes the shield and reports true once", ActiveEffectsTest::consumeShieldOnce);
        run("consumeShield reports false with no active shield", ActiveEffectsTest::consumeShieldNoneActive);
        run("snapshot is read-only and independent of later changes", ActiveEffectsTest::snapshotIsIndependent);
        run("clear removes every active effect", ActiveEffectsTest::clearRemovesAll);

        section("Difficulty");
        run("base score keeps the starting speed and gap", DifficultyTest::baseScoreIsUnscaled);
        run("speed increases in steps as score rises", DifficultyTest::speedIncreasesInSteps);
        run("gap narrows in steps as score rises", DifficultyTest::gapNarrowsInSteps);
        run("speed never exceeds the configured maximum", DifficultyTest::speedClampsAtMax);
        run("gap never shrinks past the configured minimum", DifficultyTest::gapClampsAtMin);

        section("HighScoreManager");
        run("a fresh install starts at zero", HighScoreManagerTest::freshInstallStartsAtZero);
        run("reporting a higher score persists across instances", HighScoreManagerTest::higherScorePersists);
        run("reporting a lower score does not overwrite the best", HighScoreManagerTest::lowerScoreIsIgnored);
        run("scores are clamped to the configured maximum", HighScoreManagerTest::scoreIsClamped);
        run("a corrupted save file resets safely to zero", HighScoreManagerTest::corruptedFileResetsToZero);

        section("SoundManager");
        run("construction never throws, even with no audio device", SoundManagerTest::constructionNeverThrows);
        run("play() is a safe no-op once muted", SoundManagerTest::mutedPlayIsNoOp);
        run("toggleMuted flips the muted flag", SoundManagerTest::toggleMutedFlips);

        System.out.println();
        System.out.println("-------------------------------------------------");
        System.out.println(passed + " passed, " + failed + " failed, " + (passed + failed) + " total");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void section(String name) {
        System.out.println();
        System.out.println(name + ":");
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("  PASS  " + name);
        } catch (Throwable t) {
            failed++;
            System.out.println("  FAIL  " + name + "  -->  " + t);
        }
    }

    // ---- Assertion helpers (intentionally not JVM `assert`, which is off by default) ----
    static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    static void assertFalse(boolean condition, String message) {
        assertTrue(!condition, message);
    }

    static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(message + " (expected <" + expected + ">, got <" + actual + ">)");
        }
    }
}
