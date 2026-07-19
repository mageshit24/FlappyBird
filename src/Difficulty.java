
/**
 * Difficulty.java
 *
 * Pure calculation of pipe speed/gap for a given score. Extracted out
 * of FlappyBird's game loop specifically so it can be feature-tested
 * without needing a running Swing panel: given a score, the result is
 * always the same, with no hidden state.
 */
public final class Difficulty {

    private Difficulty() {
        // Utility class: no instances.
    }

    /**
     * Pipe speed and gap for a given score, as a value pair.
     */
    public record Level(int pipeSpeed, int pipeGap) {

    }

    /**
     * Computes the current pipe speed/gap: both scale in fixed steps as score
     * rises.
     */
    public static Level forScore(double score) {
        int step = (int) (score / Constants.DIFFICULTY_STEP);
        int speed = Math.min(Constants.BASE_PIPE_SPEED + step, Constants.MAX_PIPE_SPEED);
        int gap = Math.max(
                Constants.BASE_PIPE_GAP - step * Constants.GAP_SHRINK_PER_STEP,
                Constants.MIN_PIPE_GAP);
        return new Level(speed, gap);
    }
}
