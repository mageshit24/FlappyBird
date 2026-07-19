
/**
 * SoundManagerTest.java — feature tests for the fail-safe sound layer.
 *
 * These deliberately don't (and can't reliably) assert that audio is
 * actually audible - that's not something a headless test process can
 * verify. What they verify is the contract that matters for gameplay:
 * SoundManager must never throw, on any machine, with or without an
 * audio device, muted or not.
 */
final class SoundManagerTest {

    private SoundManagerTest() {
    }

    static void constructionNeverThrows() {
        // If this sandbox/CI machine has no audio device (as this one
        // doesn't), construction must still complete without throwing -
        // see SoundManager's catch(RuntimeException) note.
        SoundManager manager = new SoundManager();
        TestRunner.assertFalse(manager.isMuted(), "a fresh SoundManager should start unmuted");
    }

    static void mutedPlayIsNoOp() {
        SoundManager manager = new SoundManager();
        manager.setMuted(true);
        // Must not throw, regardless of whether the underlying clip loaded.
        manager.play(SoundManager.Effect.FLAP);
        TestRunner.assertTrue(manager.isMuted(), "setMuted(true) should be reflected by isMuted()");
    }

    static void toggleMutedFlips() {
        SoundManager manager = new SoundManager();
        boolean before = manager.isMuted();
        manager.toggleMuted();
        TestRunner.assertEquals(!before, manager.isMuted(), "toggleMuted should flip the muted flag");
        manager.toggleMuted();
        TestRunner.assertEquals(before, manager.isMuted(), "toggling twice should return to the original state");
    }
}
