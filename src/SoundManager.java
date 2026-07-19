
import module java.base;
import module java.desktop;
import module java.logging;

/**
 * SoundManager.java
 *
 * Loads and plays the game's short sound effects.
 *
 * Security / robustness notes: 1. Every clip is loaded from a fixed, hard-coded
 * classpath resource (Constants.*_SFX) - never from a path built out of
 * external or untrusted input, so there's no risk of loading an arbitrary file
 * off disk. 2. Audio hardware isn't guaranteed to exist (headless CI,
 * locked-down machines, missing drivers). Every failure mode - missing
 * resource, unsupported format, no mixer/line available - is caught here and
 * downgrades this manager to a silent no-op instead of crashing the game or the
 * whole startup sequence. 3. Clips are loaded once at construction and reused;
 * play() never opens a new file handle per call, so rapid-fire flapping can't
 * exhaust file descriptors.
 */
public final class SoundManager {

    /**
     * Identifies which effect to play; callers never touch a Clip directly.
     */
    public enum Effect {
        FLAP, SCORE, HIT, POWERUP, SHIELD_BREAK
    }

    private static final Logger LOGGER = Logger.getLogger(SoundManager.class.getName());

    private static final Map<Effect, String> RESOURCE_PATHS = new EnumMap<>(Effect.class);

    static {
        RESOURCE_PATHS.put(Effect.FLAP, Constants.FLAP_SFX);
        RESOURCE_PATHS.put(Effect.SCORE, Constants.SCORE_SFX);
        RESOURCE_PATHS.put(Effect.HIT, Constants.HIT_SFX);
        RESOURCE_PATHS.put(Effect.POWERUP, Constants.POWERUP_SFX);
        RESOURCE_PATHS.put(Effect.SHIELD_BREAK, Constants.SHIELD_BREAK_SFX);
    }

    private final Map<Effect, Clip> clips = new EnumMap<>(Effect.class);
    private boolean muted = false;

    public SoundManager() {
        for (Effect effect : Effect.values()) {
            loadClip(effect);
        }
    }

    private void loadClip(Effect effect) {
        String path = RESOURCE_PATHS.get(effect);
        URL url = getClass().getResource(path);
        if (url == null) {
            LOGGER.log(Level.WARNING, "Missing sound asset on classpath: {0}", path);
            return;
        }
        try (AudioInputStream audioIn = AudioSystem.getAudioInputStream(new BufferedInputStream(url.openStream()))) {
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clips.put(effect, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            // No audio device, unsupported format, or corrupt file: the game
            // stays fully playable, just silent for this effect.
            LOGGER.log(Level.WARNING, "Could not load sound effect " + effect, e);
        } catch (RuntimeException e) {
            // javax.sound.sampled is inconsistent across platforms/headless
            // environments: a missing/incompatible mixer can surface as an
            // unchecked IllegalArgumentException ("No line matching
            // interface Clip...") instead of the checked exceptions above.
            // Audio is a nice-to-have, never a reason to break the game, so
            // this is caught defensively too.
            LOGGER.log(Level.WARNING, "Audio system rejected sound effect " + effect, e);
        }
    }

    /**
     * Plays an effect from the start. Safe to call every frame; no-ops if muted
     * or unavailable.
     */
    public void play(Effect effect) {
        if (muted) {
            return;
        }
        Clip clip = clips.get(effect);
        if (clip == null) {
            return;
        }
        try {
            clip.stop();
            clip.setFramePosition(0);
            clip.start();
        } catch (RuntimeException e) {
            // A device disconnected mid-game, for example. Never let a
            // sound-effect failure interrupt actual gameplay.
            LOGGER.log(Level.WARNING, "Could not play sound effect " + effect, e);
        }
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
    }

    public boolean isMuted() {
        return muted;
    }

    public void toggleMuted() {
        this.muted = !this.muted;
    }
}
