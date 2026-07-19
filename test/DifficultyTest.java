
/** DifficultyTest.java — feature tests for the pure pipe speed/gap scaling curve. */
final class DifficultyTest {

    private DifficultyTest() {
    }

    static void baseScoreIsUnscaled() {
        Difficulty.Level level = Difficulty.forScore(0);
        TestRunner.assertEquals(Constants.BASE_PIPE_SPEED, level.pipeSpeed(), "score 0 should give the base speed");
        TestRunner.assertEquals(Constants.BASE_PIPE_GAP, level.pipeGap(), "score 0 should give the base gap");
    }

    static void speedIncreasesInSteps() {
        Difficulty.Level beforeStep = Difficulty.forScore(Constants.DIFFICULTY_STEP - 1);
        Difficulty.Level afterStep = Difficulty.forScore(Constants.DIFFICULTY_STEP);
        TestRunner.assertEquals(Constants.BASE_PIPE_SPEED, beforeStep.pipeSpeed(),
                "speed should not increase until a full DIFFICULTY_STEP is reached");
        TestRunner.assertEquals(Constants.BASE_PIPE_SPEED + 1, afterStep.pipeSpeed(),
                "speed should increase by 1 at each DIFFICULTY_STEP boundary");
    }

    static void gapNarrowsInSteps() {
        Difficulty.Level afterStep = Difficulty.forScore(Constants.DIFFICULTY_STEP);
        TestRunner.assertEquals(Constants.BASE_PIPE_GAP - Constants.GAP_SHRINK_PER_STEP, afterStep.pipeGap(),
                "gap should shrink by GAP_SHRINK_PER_STEP at each DIFFICULTY_STEP boundary");
    }

    static void speedClampsAtMax() {
        Difficulty.Level farInGame = Difficulty.forScore(Constants.DIFFICULTY_STEP * 1000);
        TestRunner.assertEquals(Constants.MAX_PIPE_SPEED, farInGame.pipeSpeed(),
                "speed should never exceed MAX_PIPE_SPEED, however high the score");
    }

    static void gapClampsAtMin() {
        Difficulty.Level farInGame = Difficulty.forScore(Constants.DIFFICULTY_STEP * 1000);
        TestRunner.assertEquals(Constants.MIN_PIPE_GAP, farInGame.pipeGap(),
                "gap should never shrink past MIN_PIPE_GAP, however high the score");
    }
}
