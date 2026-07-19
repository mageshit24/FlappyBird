
import module java.base;

/**
 * ActiveEffects.java
 *
 * Owns the set of currently-active power-up effects (Shield / Slow-Mo) and
 * every rule for activating, expiring, querying, and consuming them. Pulled out
 * of FlappyBird's game loop into its own class for two reasons:
 *
 * 1. Testability - this is the actual "feature" the shield/slow-mo power-ups
 * are built on, and it has zero dependency on Swing or a running game loop. It
 * can be exercised directly from a test with fabricated timestamps, with no
 * game panel required. 2. Single responsibility - FlappyBird's job is
 * orchestrating the game loop and rendering; the rules of "how long does a
 * shield last, what happens when a second one is picked up" belong here.
 */
public final class ActiveEffects {

    private final List<PowerUpEffect> effects = new ArrayList<>();

    /**
     * Activates a Shield effect expiring at {@code nowMillis + durationMs}.
     * Replaces any existing Shield rather than stacking - picking up a second
     * shield while one is active just refreshes its duration.
     */
    public void activateShield(long durationMs, long nowMillis) {
        effects.removeIf(e -> e instanceof PowerUpEffect.Shield);
        effects.add(new PowerUpEffect.Shield(nowMillis + durationMs));
    }

    /**
     * Activates a Slow-Mo effect, replacing any existing one the same way as
     * {@link #activateShield}.
     */
    public void activateSlowMo(long durationMs, long nowMillis) {
        effects.removeIf(e -> e instanceof PowerUpEffect.SlowMo);
        effects.add(new PowerUpEffect.SlowMo(nowMillis + durationMs));
    }

    /**
     * Drops every effect whose expiry has passed. Call once per game tick.
     */
    public void expire(long nowMillis) {
        effects.removeIf(effect -> nowMillis >= effect.expiresAt());
    }

    public boolean hasShield() {
        return effects.stream().anyMatch(PowerUpEffect.Shield.class::isInstance);
    }

    public boolean hasSlowMo() {
        return effects.stream().anyMatch(PowerUpEffect.SlowMo.class::isInstance);
    }

    /**
     * Removes and consumes the active Shield, if any. Returns whether one was
     * actually consumed.
     */
    public boolean consumeShield() {
        Iterator<PowerUpEffect> it = effects.iterator();
        while (it.hasNext()) {
            if (it.next() instanceof PowerUpEffect.Shield) {
                it.remove();
                return true;
            }
        }
        return false;
    }

    public void clear() {
        effects.clear();
    }

    public boolean isEmpty() {
        return effects.isEmpty();
    }

    /**
     * Read-only snapshot for rendering (e.g. HUD badges); never exposes the
     * live list.
     */
    public List<PowerUpEffect> snapshot() {
        return List.copyOf(effects);
    }
}
