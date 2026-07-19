
import module java.base;
import module java.desktop;

/**
 * BirdTest.java — feature tests for Bird's physics and state, run via
 * TestRunner. A 1x1 BufferedImage stands in for the real sprite; these tests
 * never touch a display.
 */
final class BirdTest {

    private BirdTest() {
    }

    private static Bird newBird() {
        return new Bird(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
    }

    static void flapSetsUpwardVelocity() {
        Bird bird = newBird();
        int yBefore = bird.getY();
        bird.flap();
        bird.applyGravity(); // one tick to actually apply the velocity to position
        TestRunner.assertTrue(bird.getY() < yBefore, "flap should move the bird upward on the next tick");
    }

    static void gravityAccumulates() {
        Bird bird = newBird();
        bird.applyGravity();
        int yAfterOneTick = bird.getY();
        bird.applyGravity();
        int yAfterTwoTicks = bird.getY();
        TestRunner.assertTrue(yAfterTwoTicks > yAfterOneTick,
                "falling velocity should keep increasing the bird's y each tick");
    }

    static void gravityClampsAtMaxFallSpeed() {
        Bird bird = newBird();
        for (int i = 0; i < 100; i++) {
            bird.applyGravity();
        }
        int yAfterMany = bird.getY();
        bird.applyGravity();
        int yAfterOneMore = bird.getY();
        long delta = yAfterOneMore - yAfterMany;
        TestRunner.assertTrue(delta <= (long) Constants.MAX_FALL_SPEED,
                "per-tick fall distance should never exceed MAX_FALL_SPEED, was " + delta);
    }

    static void yNeverGoesNegative() {
        Bird bird = newBird();
        bird.flap();
        for (int i = 0; i < 20; i++) {
            bird.applyGravity();
            TestRunner.assertTrue(bird.getY() >= 0, "bird y should never go negative (above the board)");
        }
    }

    static void resetRestoresStartState() {
        Bird bird = newBird();
        bird.flap();
        for (int i = 0; i < 10; i++) {
            bird.applyGravity();
        }
        bird.resetPosition();
        TestRunner.assertEquals(Constants.BIRD_START_X, bird.getX(), "resetPosition should restore start X");
        TestRunner.assertEquals(Constants.BIRD_START_Y, bird.getY(), "resetPosition should restore start Y");
    }

    static void hasFallenBelowThreshold() {
        Bird bird = newBird();
        TestRunner.assertFalse(bird.hasFallenBelow(Constants.BOARD_HEIGHT),
                "a freshly spawned bird should not already be considered fallen");
        for (int i = 0; i < 200; i++) {
            bird.applyGravity();
        }
        TestRunner.assertTrue(bird.hasFallenBelow(Constants.BIRD_START_Y),
                "after enough gravity ticks the bird should fall below its own start line");
    }

    static void rejectsNullImage() {
        boolean threw = false;
        try {
            new Bird(null);
        } catch (NullPointerException expected) {
            threw = true;
        }
        TestRunner.assertTrue(threw, "constructing a Bird with a null image should throw NullPointerException");
    }
}
