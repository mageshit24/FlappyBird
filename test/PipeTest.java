
import module java.base;
import module java.desktop;

/**
 * PipeTest.java — feature tests for Pipe movement, off-screen detection, and
 * pass/collision state.
 */
final class PipeTest {

    private PipeTest() {
    }

    private static Pipe newPipe(int startX) {
        return new Pipe(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), startX, 0,
                Constants.PIPE_WIDTH, Constants.PIPE_HEIGHT);
    }

    static void moveShiftsLeft() {
        Pipe pipe = newPipe(200);
        pipe.move(5);
        TestRunner.assertEquals(195, pipe.getX(), "move(5) should shift the pipe 5px left");
    }

    static void offScreenDetection() {
        Pipe pipe = newPipe(0);
        TestRunner.assertFalse(pipe.isOffScreen(), "a pipe at x=0 should still be on screen");
        pipe.move(Constants.PIPE_WIDTH + 1);
        TestRunner.assertTrue(pipe.isOffScreen(), "a pipe fully past the left edge should be off screen");
    }

    static void passedByDetection() {
        Pipe pipe = newPipe(100);
        int pipeRightEdge = 100 + Constants.PIPE_WIDTH;
        TestRunner.assertFalse(pipe.isPassedBy(pipeRightEdge - 1),
                "the bird hasn't cleared the pipe yet at its right edge");
        TestRunner.assertTrue(pipe.isPassedBy(pipeRightEdge + 1),
                "the bird has cleared the pipe once past its right edge");
    }

    static void markPassedTracksState() {
        Pipe pipe = newPipe(0);
        TestRunner.assertFalse(pipe.isPassed(), "a new pipe should not start out marked as passed");
        pipe.markPassed();
        TestRunner.assertTrue(pipe.isPassed(), "markPassed should be reflected by isPassed");
    }
}
