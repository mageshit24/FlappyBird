
import module java.base;

/**
 * ActiveEffectsTest.java — feature tests for shield/slow-mo activation, expiry,
 * and consumption. This is the actual "feature" the two new power-ups are built
 * on, tested here with fabricated timestamps so it never depends on real
 * wall-clock timing or a running game loop.
 */
final class ActiveEffectsTest {

    private ActiveEffectsTest() {
    }

    static void activateShield() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(5000, 1_000_000);
        TestRunner.assertTrue(effects.hasShield(), "activateShield should make hasShield() true");
        TestRunner.assertFalse(effects.hasSlowMo(), "activating a shield should not also activate slow-mo");
    }

    static void activateSlowMo() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateSlowMo(3000, 1_000_000);
        TestRunner.assertTrue(effects.hasSlowMo(), "activateSlowMo should make hasSlowMo() true");
        TestRunner.assertFalse(effects.hasShield(), "activating slow-mo should not also activate a shield");
    }

    static void refreshNotStack() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0);
        effects.activateShield(1000, 500); // picked up a second shield mid-flight
        TestRunner.assertEquals(1, effects.snapshot().size(),
                "picking up the same effect kind twice should refresh, not stack, entries");
    }

    static void expireDropsOldEffects() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0); // expires at t=1000
        effects.expire(1000);
        TestRunner.assertFalse(effects.hasShield(), "an effect at/after its own expiry time should be dropped");
    }

    static void expireKeepsFreshEffects() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0); // expires at t=1000
        effects.expire(999);
        TestRunner.assertTrue(effects.hasShield(), "an effect should still be active just before its expiry time");
    }

    static void consumeShieldOnce() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0);
        TestRunner.assertTrue(effects.consumeShield(), "consumeShield should report true when a shield is active");
        TestRunner.assertFalse(effects.hasShield(), "the shield should be gone immediately after being consumed");
        TestRunner.assertFalse(effects.consumeShield(), "consuming twice in a row should report false the second time");
    }

    static void consumeShieldNoneActive() {
        ActiveEffects effects = new ActiveEffects();
        TestRunner.assertFalse(effects.consumeShield(), "consumeShield with no active shield should report false");
    }

    static void snapshotIsIndependent() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0);
        var snapshotBefore = effects.snapshot();
        effects.activateSlowMo(1000, 0);
        TestRunner.assertEquals(1, snapshotBefore.size(),
                "a snapshot taken earlier should not see effects activated afterward");
    }

    static void clearRemovesAll() {
        ActiveEffects effects = new ActiveEffects();
        effects.activateShield(1000, 0);
        effects.activateSlowMo(1000, 0);
        effects.clear();
        TestRunner.assertTrue(effects.isEmpty(), "clear() should remove every active effect");
    }
}
