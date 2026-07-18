
/**
 * PowerUpKind.java
 *
 * The kind of collectible power-up spawned on the board. Kept separate
 * from {@link PowerUpEffect}: this enum identifies what a floating pickup *is*
 * before it's collected; PowerUpEffect represents the *active, timed effect*
 * after collection.
 */
public enum PowerUpKind {
    SHIELD,
    SLOW_MO
}
