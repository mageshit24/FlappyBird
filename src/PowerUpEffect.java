
/**
 * PowerUpEffect.java
 *
 * Represents a power-up effect currently active on the bird.
 *
 * Modeled as a sealed interface with record implementations (Java 21+):
 *   - Compile-time exhaustiveness: every switch over PowerUpEffect below
 *     is checked by the compiler, so adding a new effect type without
 *     updating every consumer is a build failure, not a runtime bug.
 *   - Records give us immutable, self-documenting effect state (no
 *     mutable "shieldActive" + "slowMoUntil" boolean/long pair to keep
 *     in sync by hand).
 *   - Record deconstruction patterns let callers pull fields straight
 *     out of the switch label, e.g. `case SlowMo(var expiresAt) -> ...`.
 */
public sealed interface PowerUpEffect {

    /**
     * When this effect naturally expires, in System.currentTimeMillis() terms.
     */
    long expiresAt();

    /**
     * One-shot protection: absorbs the next pipe collision instead of ending
     * the game, then is consumed.
     */
    record Shield(long expiresAt) implements PowerUpEffect {

    }

    /**
     * Temporarily slows pipe movement, giving the player more reaction time.
     */
    record SlowMo(long expiresAt) implements PowerUpEffect {

    }
}
